# SPK Penentuan Karyawan Terbaik Menggunakan Metode TOPSIS

Project ini dibuat untuk Tugas Akhir dengan judul:

**Sistem Pendukung Keputusan Penentuan Karyawan Terbaik pada PT Indivara Group Menggunakan Metode TOPSIS**

Teknologi yang digunakan:

- Java 1.8
- Java Swing
- NetBeans 8.2
- MySQL/XAMPP
- JDBC
- Metode TOPSIS

## Fitur

1. Login admin.
2. Kelola data karyawan sebagai alternatif.
3. Kelola data kriteria, bobot, dan tipe kriteria benefit/cost.
4. Input nilai penilaian karyawan per kriteria.
5. Perhitungan TOPSIS otomatis.
6. Ranking karyawan terbaik berdasarkan nilai preferensi terbesar.
7. Export laporan ranking ke CSV.
8. Print tabel ranking.

## Struktur Project

```text
src/com/mahadi/topsis
├── dao       -> akses database
├── model     -> entity/model data
├── service   -> business logic, termasuk TOPSIS
├── ui        -> Java Swing form/panel
└── util      -> helper koneksi, dialog, angka, password
```

Nama class utama memakai bahasa Indonesia, misalnya `Karyawan`, `Kriteria`, `Penilaian`,
`HasilRanking`, `KaryawanDao`, `KriteriaDao`, `PenilaianDao`, dan
`PerhitunganTopsisService`.

## Cara Menjalankan

### 1. Import Database

1. Buka XAMPP.
2. Start Apache dan MySQL.
3. Buka `http://localhost/phpmyadmin`.
4. Import file:

```text
database/db_topsis_karyawan.sql
```

Database yang dibuat bernama:

```text
db_topsis_karyawan
```

Catatan: versi refactor ini mengganti nama tabel dan kolom ke bahasa Indonesia.
Jika sebelumnya sudah pernah import database versi lama, import ulang file SQL ini
agar tabel `pengguna`, `karyawan`, `kriteria`, `penilaian`, dan `hasil_ranking`
tersedia sesuai query aplikasi.

### 2. Tambahkan MySQL Connector/J

Project ini membutuhkan MySQL Connector/J agar Java bisa terhubung ke MySQL.

Rekomendasi untuk Java 8 dan NetBeans 8.2:

```text
mysql-connector-java-5.1.49.jar
```

Cara menambahkan di NetBeans:

1. Klik kanan project.
2. Pilih `Properties`.
3. Pilih `Libraries`.
4. Klik `Add JAR/Folder`.
5. Pilih file `mysql-connector-java-5.1.49.jar`.
6. Klik `OK`.

Jika kamu menaruh file jar di folder `lib/` dengan nama `mysql-connector-java-5.1.49.jar`, project properties sudah diarahkan ke lokasi tersebut.

### 3. Cek Konfigurasi Database

File konfigurasi ada di:

```text
src/config.properties
```

Default konfigurasi:

```properties
db.url=jdbc:mysql://localhost:3306/db_topsis_karyawan?useSSL=false&useUnicode=true&characterEncoding=UTF-8
db.user=root
db.password=
db.driver=com.mysql.jdbc.Driver
```

Jika password MySQL kamu tidak kosong, isi bagian `db.password`.

### 4. Jalankan Project

1. Buka NetBeans 8.2.
2. `File` -> `Open Project`.
3. Pilih folder project ini.
4. Klik kanan project -> `Run`.

Default login:

```text
Username: admin
Password: admin123
```

## Rumus TOPSIS yang Digunakan

1. Membuat matriks keputusan dari nilai alternatif terhadap kriteria.
2. Normalisasi matriks:

```text
r_ij = x_ij / sqrt(sum(x_ij^2))
```

3. Matriks normalisasi terbobot:

```text
y_ij = w_j * r_ij
```

4. Menentukan solusi ideal positif dan solusi ideal negatif.
5. Menghitung jarak terhadap solusi ideal positif dan negatif.
6. Menghitung nilai preferensi:

```text
V_i = D_i^- / (D_i^+ + D_i^-)
```

7. Ranking ditentukan dari nilai preferensi terbesar ke terkecil.

## Catatan Pengembangan

- Kriteria dan bobot awal hanya contoh akademik. Validasi kembali dengan HRD atau pembimbing.
- Semua kriteria default bertipe benefit.
- Total bobot kriteria harus sama dengan 1.
- Nilai penilaian sebaiknya memakai skala 0 sampai 100 agar mudah dibaca.
- Tanggal masuk karyawan bersifat opsional. Jika diisi, formatnya harus `yyyy-MM-dd`.
- Data sample SQL menyediakan 150 data karyawan dan nilai penilaian lengkap untuk memenuhi kebutuhan minimal data Tugas Akhir.
- Keputusan akhir tetap berada pada pihak manajemen; sistem hanya alat bantu rekomendasi.
