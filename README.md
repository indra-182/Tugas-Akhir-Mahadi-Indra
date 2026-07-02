# SPK Penentuan Karyawan Terbaik Menggunakan Metode TOPSIS

Project ini dibuat untuk Tugas Akhir dengan judul:

**Sistem Pendukung Keputusan Penentuan Karyawan Terbaik pada PT Indivara Group Menggunakan Metode TOPSIS**

Teknologi yang digunakan:

- Java 1.8
- Java Swing
- NetBeans 8.2 (Ant project)
- MySQL (lokal atau cloud Aiven)
- JDBC + MySQL Connector/J 8.0.33
- Metode TOPSIS

## Fitur

1. Login admin.
2. Kelola data karyawan sebagai alternatif.
3. Kelola data kriteria, bobot, dan tipe kriteria benefit/cost.
4. Input nilai penilaian karyawan per kriteria.
5. Perhitungan TOPSIS otomatis beserta tampilan matriks antara.
6. Ranking karyawan terbaik berdasarkan nilai preferensi terbesar.
7. Cetak laporan (karyawan, kriteria, penilaian, ranking) menggunakan fitur print Swing.
8. Validasi cetak: laporan ranking hanya bisa dicetak setelah perhitungan diproses.

## Struktur Project

```text
src/com/mahadi/indivaragroup
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

Jalankan MySQL lokal, lalu import file:

```text
database/db_topsis_indivara_group.sql
```

Bisa lewat phpMyAdmin atau command line:

```text
mysql -u root -p < database/db_topsis_indivara_group.sql
```

Database yang dibuat bernama `db_topsis_indivara_group`, berisi tabel `pengguna`,
`karyawan`, `kriteria`, `penilaian`, dan `hasil_ranking`, lengkap dengan data sample
(100 karyawan, 5 kriteria, 500 nilai penilaian).

### 2. Buat File Konfigurasi Database

File `src/config.properties` **tidak ikut repository** karena berisi kredensial.
Buat file tersebut secara manual dengan isi:

```properties
db.driver=com.mysql.cj.jdbc.Driver

# --- LOKAL (MySQL di komputer sendiri) ---
db.url=jdbc:mysql://localhost:3306/db_topsis_indivara_group?sslMode=DISABLED&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8&connectTimeout=10000
db.user=root
db.password=ISI_PASSWORD_MYSQL

# --- CLOUD (contoh: Aiven MySQL, wajib TLS) ---
#db.url=jdbc:mysql://HOST_CLOUD:PORT/db_topsis_indivara_group?sslMode=REQUIRED&useUnicode=true&characterEncoding=UTF-8&connectTimeout=10000&tcpKeepAlive=true&cachePrepStmts=true&prepStmtCacheSize=250&useLocalSessionState=true
#db.user=USER_CLOUD
#db.password=PASSWORD_CLOUD
```

Aktifkan salah satu blok (LOKAL atau CLOUD) dengan memindahkan tanda `#`.
File ini ter-bundle ke dalam JAR, jadi setiap kali diubah harus
`Clean and Build` ulang.

### 3. Library MySQL Connector/J

Driver sudah tersedia di `lib/mysql-connector-j-8.0.33.jar` dan sudah
terdaftar di project properties. Tidak perlu setup tambahan selama struktur
folder tidak diubah.

### 4. Jalankan Project

1. Buka NetBeans.
2. `File` -> `Open Project` -> pilih folder project ini.
3. Klik kanan project -> `Clean and Build` -> `Run`.

JAR hasil build ada di `dist/SPK-TOPSIS-Indivara-Group.jar` dan bisa dijalankan
langsung dengan double-click (butuh Java terpasang).

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
- Total bobot kriteria harus sama dengan 1.
- Nilai penilaian sebaiknya memakai skala 0 sampai 100 agar mudah dibaca.
- Tanggal masuk karyawan bersifat opsional. Jika diisi, formatnya harus `yyyy-MM-dd`.
- Koneksi database memakai satu koneksi bersama (shared connection) agar tetap cepat saat memakai database cloud.
- Keputusan akhir tetap berada pada pihak manajemen; sistem hanya alat bantu rekomendasi.
