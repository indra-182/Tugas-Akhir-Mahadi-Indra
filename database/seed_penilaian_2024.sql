-- Seed: tambah periode (tahun) 2024 pada penilaian, supaya Laporan Tren
-- Kinerja Karyawan punya 3 tahun untuk dibandingkan (2024, 2025, 2026).
-- Jalankan manual satu kali di database local, sama seperti seed_penilaian_2025.sql.
-- Aman dijalankan ulang (INSERT dicegah duplikat lewat ON DUPLICATE KEY UPDATE
-- karena constraint uq_karyawan_kriteria_tahun).
--
-- Setelah ini, buka menu Perhitungan, pilih Tahun 2024, klik Proses (lalu
-- ulangi untuk Tahun 2025) supaya hasil_ranking benar-benar terisi lewat
-- algoritma TOPSIS yang asli.

USE db_topsis_indivara_group;

INSERT INTO penilaian (id_karyawan, id_kriteria, tahun, nilai)
SELECT k.id, c.id, 2024,
  CASE
    WHEN c.kode = 'C5' THEN MOD(k.id * 5 + c.id * 2, 12)
    WHEN c.kode = 'C6' THEN GREATEST(0, COALESCE(TIMESTAMPDIFF(YEAR, k.tanggal_masuk, '2024-12-31'), 0))
    ELSE 50 + MOD(k.id * 13 + c.id * 19, 46)
  END AS nilai
FROM karyawan k, kriteria c
ON DUPLICATE KEY UPDATE nilai = VALUES(nilai);
