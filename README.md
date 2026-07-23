# SPK Penentuan Karyawan Terbaik Menggunakan Metode TOPSIS

Project Tugas Akhir:

**Sistem Pendukung Keputusan Penentuan Karyawan Terbaik pada PT Indivara Group Menggunakan Metode TOPSIS**

## Teknologi

- Java 8, Java Swing, dan NetBeans 8.2 (Ant project)
- PostgreSQL pada Supabase
- PostgreSQL JDBC Driver 42.7.13
- Metode TOPSIS

## Fitur

1. Login admin.
2. Kelola data karyawan, kriteria, dan nilai penilaian per tahun.
3. Perhitungan TOPSIS beserta matriks antara dan ranking.
4. Cetak laporan karyawan, kriteria, penilaian, ranking, dan tren.

## Menyiapkan Supabase

1. Buat project Supabase PostgreSQL dan catat detail **Session Pooler** pada dashboard `Connect`.
2. Pada Supabase SQL Editor, jalankan berurutan:

   ```text
   database/db_topsis_indivara_group.sql
   database/seed_penilaian_2024.sql
   database/seed_penilaian_2025.sql
   ```

   Skrip utama membuat admin demo, 100 karyawan, enam kriteria, dan penilaian tahun berjalan. Dua skrip seed menambahkan periode 2024 dan 2025.
   Untuk database yang sudah ada, jalankan juga `database/migrasi_snapshot_perhitungan.sql`. Saat periode lampau pertama kali dibuka, aplikasi membuat snapshot TOPSIS yang membekukan kriteria, peserta, nilai, dan ranking periode tersebut.
3. Pada `database/buat_role_aplikasi.sql`, ganti `GANTI_DENGAN_PASSWORD_KUAT` dengan password khusus aplikasi, lalu jalankan sebagai pemilik project. Skrip memberi role `app_topsis` akses ke skema aplikasi, mengaktifkan Row Level Security (RLS), dan memberi policy hanya untuk role tersebut.
4. Isi `src/config.properties` memakai host, port, nama database, nama pengguna pooler, dan password role aplikasi. URL harus tetap memakai `sslmode=require`.

`config.properties` sengaja ikut source dan JAR karena DVD akademik harus langsung berjalan. Jangan unggah repository ke publik; jadikan repository GitHub **private** dan jangan gunakan kredensial pemilik Supabase pada file ini.

## Menjalankan aplikasi

1. Buka project di NetBeans.
2. Pastikan `lib/postgresql-42.7.13.jar` tersedia dan project memakai Java 8 atau lebih baru.
3. Jalankan **Clean and Build**, kemudian **Run**.
4. JAR ada pada `dist/SPK-TOPSIS-Indivara-Group.jar`. Jalankan dari folder hasil build agar folder `lib/` pada classpath manifest ikut tersedia.

Login demo:

```text
Username: admin
Password: admin123
```

## Operasional DVD dan reset

- Semua pengguna DVD memakai satu database demo Supabase; perubahan yang mereka buat akan tersimpan.
- Jika data perlu dikembalikan ke kondisi demo, pemilik project menjalankan kembali tiga skrip setup di atas. `database/reset_data_demo.sql` adalah pengingat prosedur tersebut.
- Supabase Free Plan dapat menjeda project yang tidak aktif. Sebelum demonstrasi, pemilik project harus memastikan project aktif atau melanjutkannya dari dashboard Supabase.
- Setelah mengisi kredensial nyata, buat ulang JAR dan uji dari folder distribusi dengan koneksi internet aktif.

## Rumus TOPSIS

1. Normalisasi: `r_ij = x_ij / sqrt(sum(x_ij^2))`
2. Normalisasi terbobot: `y_ij = w_j * r_ij`
3. Tentukan solusi ideal positif dan negatif.
4. Nilai preferensi: `V_i = D_i^- / (D_i^+ + D_i^-)`
5. Ranking mengikuti nilai preferensi terbesar.

## Catatan akademik

- Bobot dan data awal adalah contoh demonstrasi dan perlu divalidasi oleh HRD/pembimbing.
- Total bobot kriteria harus 1.
- Sistem memberi rekomendasi; keputusan akhir tetap berada pada manajemen.
