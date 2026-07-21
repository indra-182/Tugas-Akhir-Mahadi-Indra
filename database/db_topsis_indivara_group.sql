CREATE DATABASE IF NOT EXISTS db_topsis_indivara_group
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE db_topsis_indivara_group;

DROP TABLE IF EXISTS hasil_ranking;
DROP TABLE IF EXISTS penilaian;
DROP TABLE IF EXISTS kriteria;
DROP TABLE IF EXISTS karyawan;
DROP TABLE IF EXISTS pengguna;

CREATE TABLE pengguna (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash CHAR(64) NOT NULL,
  nama_lengkap VARCHAR(100) NOT NULL,
  role VARCHAR(30) NOT NULL DEFAULT 'ADMIN',
  dibuat_pada TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE karyawan (
  id INT AUTO_INCREMENT PRIMARY KEY,
  kode_karyawan VARCHAR(30) NOT NULL UNIQUE,
  nama VARCHAR(100) NOT NULL,
  divisi VARCHAR(100),
  jabatan VARCHAR(100),
  tanggal_masuk DATE NULL,
  status ENUM('AKTIF', 'NONAKTIF') NOT NULL DEFAULT 'AKTIF',
  dibuat_pada TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  diubah_pada TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE kriteria (
  id INT AUTO_INCREMENT PRIMARY KEY,
  kode VARCHAR(10) NOT NULL UNIQUE,
  nama VARCHAR(100) NOT NULL,
  bobot DECIMAL(10,4) NOT NULL,
  tipe ENUM('BENEFIT', 'COST') NOT NULL DEFAULT 'BENEFIT',
  keterangan VARCHAR(255),
  dibuat_pada TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  diubah_pada TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE penilaian (
  id INT AUTO_INCREMENT PRIMARY KEY,
  id_karyawan INT NOT NULL,
  id_kriteria INT NOT NULL,
  tahun INT NOT NULL,
  nilai DECIMAL(10,4) NOT NULL DEFAULT 0,
  dibuat_pada TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  diubah_pada TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_penilaian_karyawan FOREIGN KEY (id_karyawan) REFERENCES karyawan(id) ON DELETE CASCADE,
  CONSTRAINT fk_penilaian_kriteria FOREIGN KEY (id_kriteria) REFERENCES kriteria(id) ON DELETE CASCADE,
  CONSTRAINT uq_karyawan_kriteria_tahun UNIQUE (id_karyawan, id_kriteria, tahun)
) ENGINE=InnoDB;

CREATE TABLE hasil_ranking (
  id INT AUTO_INCREMENT PRIMARY KEY,
  id_karyawan INT NOT NULL,
  tahun INT NOT NULL,
  nilai_topsis DECIMAL(12,6) NOT NULL,
  peringkat INT NOT NULL,
  dihitung_pada TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_hasil_ranking_karyawan FOREIGN KEY (id_karyawan) REFERENCES karyawan(id) ON DELETE CASCADE,
  CONSTRAINT uq_karyawan_tahun UNIQUE (id_karyawan, tahun)
) ENGINE=InnoDB;

INSERT INTO pengguna (username, password_hash, nama_lengkap, role) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Administrator', 'ADMIN');

INSERT INTO karyawan (kode_karyawan, nama, divisi, jabatan, tanggal_masuk, status) VALUES
('K001', 'Aditya Pratama', 'HRD', 'Staff', '2018-01-01', 'AKTIF'),
('K002', 'Bima Kusuma', 'Finance', 'Analyst', '2019-02-02', 'AKTIF'),
('K003', 'Candra Maulana', 'Information Technology', 'Supervisor', '2020-03-03', 'AKTIF'),
('K004', 'Dewi Azzahra', 'Operasional', 'Officer', '2021-04-04', 'AKTIF'),
('K005', 'Eka Febriani', 'Marketing', 'Koordinator', '2022-05-05', 'NONAKTIF'),
('K006', 'Farhan Alamsyah', 'Sales', 'Senior Staff', '2023-06-06', 'AKTIF'),
('K007', 'Gita Suryani', 'Procurement', 'Admin', '2024-07-07', 'AKTIF'),
('K008', 'Hendra Fitria', 'Legal', 'Staff', '2018-08-08', 'AKTIF'),
('K009', 'Intan Hidayat', 'General Affairs', 'Analyst', '2019-09-09', 'AKTIF'),
('K010', 'Joko Firmansyah', 'Customer Service', 'Supervisor', '2020-10-10', 'AKTIF'),
('K011', 'Kartika Gunawan', 'HRD', 'Officer', '2021-11-11', 'AKTIF'),
('K012', 'Lukman Hakim', 'Finance', 'Koordinator', '2022-12-12', 'AKTIF'),
('K013', 'Maya Prameswari', 'Information Technology', 'Senior Staff', '2023-01-13', 'AKTIF'),
('K014', 'Naufal Fajri', 'Operasional', 'Admin', '2024-02-14', 'AKTIF'),
('K015', 'Oktavia Susanto', 'Marketing', 'Staff', '2018-03-15', 'NONAKTIF'),
('K016', 'Prasetyo Permata', 'Sales', 'Analyst', '2019-04-16', 'AKTIF'),
('K017', 'Qonita Ananda', 'Procurement', 'Supervisor', '2020-05-17', 'AKTIF'),
('K018', 'Rizky Rahmawati', 'Legal', 'Officer', '2021-06-18', 'AKTIF'),
('K019', 'Salsabila Novitasari', 'General Affairs', 'Koordinator', '2022-07-19', 'AKTIF'),
('K020', 'Taufik Yulianto', 'Customer Service', 'Senior Staff', '2023-08-20', 'AKTIF'),
('K021', 'Utami Wulandari', 'HRD', 'Admin', '2024-09-21', 'AKTIF'),
('K022', 'Vina Kencana', 'Finance', 'Staff', '2018-10-22', 'AKTIF'),
('K023', 'Wahyu Ramadhan', 'Information Technology', 'Analyst', '2019-11-23', 'AKTIF'),
('K024', 'Yuliana Siregar', 'Operasional', 'Supervisor', '2020-12-24', 'AKTIF'),
('K025', 'Zainal Kurniawan', 'Marketing', 'Officer', '2021-01-25', 'NONAKTIF'),
('K026', 'Ari Syahputra', 'Sales', 'Koordinator', '2022-02-26', 'AKTIF'),
('K027', 'Bella Ningrum', 'Procurement', 'Senior Staff', '2023-03-27', 'AKTIF'),
('K028', 'Dimas Handayani', 'Legal', 'Admin', '2024-04-01', 'AKTIF'),
('K029', 'Elisa Wardhana', 'General Affairs', 'Staff', '2018-05-02', 'AKTIF'),
('K030', 'Fauzi Lestari', 'Customer Service', 'Analyst', '2019-06-03', 'AKTIF'),
('K031', 'Galih Nugroho', 'HRD', 'Supervisor', '2020-07-04', 'AKTIF'),
('K032', 'Hanif Anggraini', 'Finance', 'Officer', '2021-08-05', 'AKTIF'),
('K033', 'Indah Maharani', 'Information Technology', 'Koordinator', '2022-09-06', 'AKTIF'),
('K034', 'Jihan Safitri', 'Operasional', 'Senior Staff', '2023-10-07', 'AKTIF'),
('K035', 'Kevin Pertiwi', 'Marketing', 'Admin', '2024-11-08', 'NONAKTIF'),
('K036', 'Lestari Melati', 'Sales', 'Staff', '2018-12-09', 'AKTIF'),
('K037', 'Mahendra Wibowo', 'Procurement', 'Analyst', '2019-01-10', 'AKTIF'),
('K038', 'Nabila Wijaya', 'Legal', 'Supervisor', '2020-02-11', 'AKTIF'),
('K039', 'Oscar Setiawan', 'General Affairs', 'Officer', '2021-03-12', 'AKTIF'),
('K040', 'Putri Pangestu', 'Customer Service', 'Koordinator', '2022-04-13', 'AKTIF'),
('K041', 'Rangga Hermawan', 'HRD', 'Senior Staff', '2023-05-14', 'AKTIF'),
('K042', 'Sari Cahyono', 'Finance', 'Admin', '2024-06-15', 'AKTIF'),
('K043', 'Teguh Nashir', 'Information Technology', 'Staff', '2018-07-16', 'AKTIF'),
('K044', 'Umar Saputra', 'Operasional', 'Analyst', '2019-08-17', 'AKTIF'),
('K045', 'Vicky Santoso', 'Marketing', 'Supervisor', '2020-09-18', 'NONAKTIF'),
('K046', 'Widya Puspitasari', 'Sales', 'Officer', '2021-10-19', 'AKTIF'),
('K047', 'Yusuf Fadillah', 'Procurement', 'Koordinator', '2022-11-20', 'AKTIF'),
('K048', 'Zahra Saputri', 'Legal', 'Senior Staff', '2023-12-21', 'AKTIF'),
('K049', 'Agus Rahayu', 'General Affairs', 'Admin', '2024-01-22', 'AKTIF'),
('K050', 'Bayu Pamungkas', 'Customer Service', 'Staff', '2018-02-23', 'AKTIF'),
('K051', 'Aditya Pratama', 'HRD', 'Analyst', '2019-03-24', 'AKTIF'),
('K052', 'Bima Kusuma', 'Finance', 'Supervisor', '2020-04-25', 'AKTIF'),
('K053', 'Candra Maulana', 'Information Technology', 'Officer', '2021-05-26', 'AKTIF'),
('K054', 'Dewi Azzahra', 'Operasional', 'Koordinator', '2022-06-27', 'AKTIF'),
('K055', 'Eka Febriani', 'Marketing', 'Senior Staff', '2023-07-01', 'NONAKTIF'),
('K056', 'Farhan Alamsyah', 'Sales', 'Admin', '2024-08-02', 'AKTIF'),
('K057', 'Gita Suryani', 'Procurement', 'Staff', '2018-09-03', 'AKTIF'),
('K058', 'Hendra Fitria', 'Legal', 'Analyst', '2019-10-04', 'AKTIF'),
('K059', 'Intan Hidayat', 'General Affairs', 'Supervisor', '2020-11-05', 'AKTIF'),
('K060', 'Joko Firmansyah', 'Customer Service', 'Officer', '2021-12-06', 'AKTIF'),
('K061', 'Kartika Gunawan', 'HRD', 'Koordinator', '2022-01-07', 'AKTIF'),
('K062', 'Lukman Hakim', 'Finance', 'Senior Staff', '2023-02-08', 'AKTIF'),
('K063', 'Maya Prameswari', 'Information Technology', 'Admin', '2024-03-09', 'AKTIF'),
('K064', 'Naufal Fajri', 'Operasional', 'Staff', '2018-04-10', 'AKTIF'),
('K065', 'Oktavia Susanto', 'Marketing', 'Analyst', '2019-05-11', 'NONAKTIF'),
('K066', 'Prasetyo Permata', 'Sales', 'Supervisor', '2020-06-12', 'AKTIF'),
('K067', 'Qonita Ananda', 'Procurement', 'Officer', '2021-07-13', 'AKTIF'),
('K068', 'Rizky Rahmawati', 'Legal', 'Koordinator', '2022-08-14', 'AKTIF'),
('K069', 'Salsabila Novitasari', 'General Affairs', 'Senior Staff', '2023-09-15', 'AKTIF'),
('K070', 'Taufik Yulianto', 'Customer Service', 'Admin', '2024-10-16', 'AKTIF'),
('K071', 'Utami Wulandari', 'HRD', 'Staff', '2018-11-17', 'AKTIF'),
('K072', 'Vina Kencana', 'Finance', 'Analyst', '2019-12-18', 'AKTIF'),
('K073', 'Wahyu Ramadhan', 'Information Technology', 'Supervisor', '2020-01-19', 'AKTIF'),
('K074', 'Yuliana Siregar', 'Operasional', 'Officer', '2021-02-20', 'AKTIF'),
('K075', 'Zainal Kurniawan', 'Marketing', 'Koordinator', '2022-03-21', 'NONAKTIF'),
('K076', 'Ari Syahputra', 'Sales', 'Senior Staff', '2023-04-22', 'AKTIF'),
('K077', 'Bella Ningrum', 'Procurement', 'Admin', '2024-05-23', 'AKTIF'),
('K078', 'Dimas Handayani', 'Legal', 'Staff', '2018-06-24', 'AKTIF'),
('K079', 'Elisa Wardhana', 'General Affairs', 'Analyst', '2019-07-25', 'AKTIF'),
('K080', 'Fauzi Lestari', 'Customer Service', 'Supervisor', '2020-08-26', 'AKTIF'),
('K081', 'Galih Nugroho', 'HRD', 'Officer', '2021-09-27', 'AKTIF'),
('K082', 'Hanif Anggraini', 'Finance', 'Koordinator', '2022-10-01', 'AKTIF'),
('K083', 'Indah Maharani', 'Information Technology', 'Senior Staff', '2023-11-02', 'AKTIF'),
('K084', 'Jihan Safitri', 'Operasional', 'Admin', '2024-12-03', 'AKTIF'),
('K085', 'Kevin Pertiwi', 'Marketing', 'Staff', '2018-01-04', 'NONAKTIF'),
('K086', 'Lestari Melati', 'Sales', 'Analyst', '2019-02-05', 'AKTIF'),
('K087', 'Mahendra Wibowo', 'Procurement', 'Supervisor', '2020-03-06', 'AKTIF'),
('K088', 'Nabila Wijaya', 'Legal', 'Officer', '2021-04-07', 'AKTIF'),
('K089', 'Oscar Setiawan', 'General Affairs', 'Koordinator', '2022-05-08', 'AKTIF'),
('K090', 'Putri Pangestu', 'Customer Service', 'Senior Staff', '2023-06-09', 'AKTIF'),
('K091', 'Rangga Hermawan', 'HRD', 'Admin', '2024-07-10', 'AKTIF'),
('K092', 'Sari Cahyono', 'Finance', 'Staff', '2018-08-11', 'AKTIF'),
('K093', 'Teguh Nashir', 'Information Technology', 'Analyst', '2019-09-12', 'AKTIF'),
('K094', 'Umar Saputra', 'Operasional', 'Supervisor', '2020-10-13', 'AKTIF'),
('K095', 'Vicky Santoso', 'Marketing', 'Officer', '2021-11-14', 'NONAKTIF'),
('K096', 'Widya Puspitasari', 'Sales', 'Koordinator', '2022-12-15', 'AKTIF'),
('K097', 'Yusuf Fadillah', 'Procurement', 'Senior Staff', '2023-01-16', 'AKTIF'),
('K098', 'Zahra Saputri', 'Legal', 'Admin', '2024-02-17', 'AKTIF'),
('K099', 'Agus Rahayu', 'General Affairs', 'Staff', '2018-03-18', 'AKTIF'),
('K100', 'Bayu Pamungkas', 'Customer Service', 'Analyst', '2019-04-19', 'AKTIF');

INSERT INTO kriteria (kode, nama, bobot, tipe, keterangan) VALUES
('C1', 'Kedisiplinan', 0.2500, 'BENEFIT', 'Tingkat kepatuhan terhadap aturan dan prosedur kerja.'),
('C2', 'Kualitas Kerja', 0.2500, 'BENEFIT', 'Mutu hasil pekerjaan yang diselesaikan karyawan.'),
('C3', 'Tanggung Jawab', 0.2000, 'BENEFIT', 'Kemampuan menyelesaikan tugas sesuai kewajiban.'),
('C4', 'Kerja Sama', 0.1500, 'BENEFIT', 'Kemampuan bekerja sama dengan rekan dan tim.'),
('C5', 'Absensi', 0.1000, 'COST', 'Jumlah hari tidak masuk kerja dalam periode penilaian.'),
('C6', 'Masa Kerja', 0.0500, 'BENEFIT', 'Lama bekerja karyawan dalam tahun sejak tanggal masuk.');

INSERT INTO penilaian (id_karyawan, id_kriteria, tahun, nilai)
SELECT k.id, c.id, YEAR(CURDATE()),
  CASE
    WHEN c.kode = 'C5' THEN MOD(k.id * 3 + c.id * 5, 13)
    WHEN c.kode = 'C6' THEN COALESCE(TIMESTAMPDIFF(YEAR, k.tanggal_masuk, CURDATE()), 0)
    ELSE 60 + MOD(k.id * 11 + c.id * 13, 41)
  END AS nilai
FROM karyawan k
CROSS JOIN kriteria c;
