-- Seed: tambah periode (tahun) 2025 pada penilaian, dan tandai sebagian
-- karyawan sebagai NONAKTIF supaya Laporan Data Karyawan punya variasi status
-- untuk didemokan. Jalankan manual satu kali di database local.
-- Aman dijalankan ulang (UPDATE idempoten; INSERT dicegah duplikat lewat
-- ON DUPLICATE KEY UPDATE karena constraint uq_karyawan_kriteria_tahun).

USE db_topsis_indivara_group;

UPDATE karyawan SET status = 'NONAKTIF'
WHERE kode_karyawan IN ('K005','K015','K025','K035','K045','K055','K065','K075','K085','K095');

INSERT INTO penilaian (id_karyawan, id_kriteria, tahun, nilai)
SELECT k.id, c.id, 2025,
  CASE
    WHEN c.kode = 'C5' THEN MOD(k.id * 7 + c.id * 3, 11)
    WHEN c.kode = 'C6' THEN GREATEST(0, COALESCE(TIMESTAMPDIFF(YEAR, k.tanggal_masuk, '2025-12-31'), 0))
    ELSE 55 + MOD(k.id * 17 + c.id * 7, 41)
  END AS nilai
FROM karyawan k, kriteria c
ON DUPLICATE KEY UPDATE nilai = VALUES(nilai);
