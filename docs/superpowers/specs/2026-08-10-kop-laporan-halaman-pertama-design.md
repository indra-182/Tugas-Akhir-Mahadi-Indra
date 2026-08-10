# Design: Kop Laporan Hanya pada Halaman Pertama

## Tujuan

Pada seluruh jenis laporan yang tersedia di `LaporanPanel`, kop laporan hanya
ditampilkan pada halaman pertama. Preview dan file PDF harus menggunakan
aturan layout yang sama karena keduanya dirender dari `CetakLaporanPrintable`
yang sama.

Jenis laporan yang tercakup:

- Laporan Data Ranking
- Laporan Data Penilaian
- Laporan Data Karyawan
- Laporan Tren Kinerja Karyawan
- Laporan Evaluasi TOPSIS

## Kondisi Saat Ini

`CetakLaporanPrintable.print(...)` memanggil `gambarKopLaporan(...)` setiap
kali `Printable` diminta merender halaman. Akibatnya, kop digambar ulang pada
`pageIndex` yang lebih besar dari nol, baik ketika halaman dirender untuk
preview maupun ketika `PrinterJob` menghasilkan PDF.

Pagination saat ini juga memakai kapasitas yang sama untuk semua halaman.
Kapasitas tersebut dikurangi oleh tinggi kop, sehingga halaman lanjutan tidak
dapat menggunakan ruang yang kosong setelah kop dihilangkan.

## Desain yang Disepakati

### Sumber rendering tunggal

`CetakLaporanPrintable` tetap menjadi sumber rendering untuk preview dan PDF.
Tidak ada logika khusus di dialog preview atau proses download untuk mengubah
isi halaman. Perubahan hanya dilakukan pada layout dan pagination di
`Printable`.

### Layout halaman biasa

- Halaman pertama menggambar kop, lalu tabel dimulai di bawah kop.
- Halaman berikutnya tidak menggambar kop dan tabel dimulai dari area atas
  halaman.
- Header kolom tabel tetap digambar pada setiap halaman tabel.
- Tanda tangan tetap digambar pada halaman terakhir laporan.

### Layout laporan evaluasi TOPSIS

- Halaman pertama menggambar kop dan bagian TOPSIS pertama dimulai di bawah
  kop.
- Halaman berikutnya, termasuk halaman lanjutan dari bagian pertama maupun
  bagian TOPSIS berikutnya, tidak menggambar kop.
- Judul bagian dan header tabel tetap digambar pada setiap halaman bagian.
- Rentang halaman dihitung berdasarkan kapasitas halaman pertama dan kapasitas
  halaman lanjutan, sehingga perpindahan antarbagian tidak melompati atau
  menggandakan baris.
- Tanda tangan tetap hanya digambar ketika bagian terakhir selesai dirender.

### Pagination

Tambahkan utilitas pagination kecil yang menghasilkan rentang halaman dari
jumlah baris, kapasitas halaman pertama, dan kapasitas halaman lanjutan.
Untuk laporan TOPSIS, rentang juga menyimpan indeks bagian laporan.

Algoritmanya:

1. Hitung kapasitas halaman pertama dengan memperhitungkan tinggi kop.
2. Hitung kapasitas halaman lanjutan tanpa tinggi kop.
3. Buat rentang baris pertama menggunakan kapasitas halaman pertama.
4. Buat rentang berikutnya menggunakan kapasitas halaman lanjutan sampai
   seluruh baris selesai.
5. Gunakan rentang tersebut untuk menentukan apakah `pageIndex` memiliki
   halaman; jika tidak, kembalikan `NO_SUCH_PAGE` tanpa menggambar halaman.

Kapasitas tetap memiliki batas minimum satu baris, mengikuti perilaku
sekarang. Perhitungan tanda tangan mempertahankan ruang bawah yang sudah
digunakan oleh renderer saat ini agar isi tidak bertabrakan dengan tanda
tangan.

## Pengujian

Test unit untuk utilitas pagination akan memastikan:

- halaman pertama dan halaman lanjutan memakai kapasitas yang berbeda;
- seluruh rentang baris berurutan tanpa kelompatan atau duplikasi;
- halaman terakhir memiliki batas baris yang benar;
- beberapa bagian TOPSIS berpindah ke halaman berikutnya secara benar;
- jumlah halaman yang tidak ada menghasilkan kondisi akhir yang dapat dikenali.

Test `CetakPreviewRenderer` yang sudah ada tetap dijalankan untuk memastikan
semua halaman `Printable` dirender sampai `NO_SUCH_PAGE`.

Verifikasi akhir:

- test terarah untuk pagination dan renderer;
- seluruh test melalui `ant test`;
- kompilasi/package melalui `ant clean jar`.

## Batasan Perubahan

- Tidak mengubah sumber data, DAO, database, atau format isi laporan.
- Tidak menambah dependency.
- Tidak mengubah alur tombol preview dan download selain hasil layout halaman.
- Perubahan kode dibatasi pada renderer laporan, utilitas pagination, dan
  test terkait.
