-- Seed periode 2025 dan variasi status karyawan untuk demonstrasi.
-- Jalankan di Supabase SQL Editor setelah db_topsis_indivara_group.sql.
-- Aman dijalankan ulang karena memakai PostgreSQL ON CONFLICT.

UPDATE karyawan SET status = 'NONAKTIF'
WHERE kode_karyawan IN ('K005','K015','K025','K035','K045','K055','K065','K075','K085','K095');

INSERT INTO penilaian (id_karyawan, id_kriteria, tahun, nilai)
SELECT k.id, c.id, 2025,
  CASE
    WHEN c.kode = 'C5' THEN (k.id * 7 + c.id * 3) % 11
    WHEN c.kode = 'C6' THEN GREATEST(0, COALESCE(EXTRACT(YEAR FROM AGE(DATE '2025-12-31', k.tanggal_masuk))::INTEGER, 0))
    ELSE 55 + ((k.id * 17 + c.id * 7) % 41)
  END AS nilai
FROM karyawan k
CROSS JOIN kriteria c
ON CONFLICT (id_karyawan, id_kriteria, tahun) DO UPDATE
SET nilai = EXCLUDED.nilai;
