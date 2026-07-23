-- Seed periode 2024 untuk Laporan Tren Kinerja Karyawan.
-- Jalankan di Supabase SQL Editor setelah db_topsis_indivara_group.sql.
-- Aman dijalankan ulang karena memakai PostgreSQL ON CONFLICT.
--
-- Setelah ini, buka menu Perhitungan, pilih Tahun 2024, klik Proses (lalu
-- ulangi untuk Tahun 2025) supaya hasil_ranking benar-benar terisi lewat
-- algoritma TOPSIS yang asli.

INSERT INTO penilaian (id_karyawan, id_kriteria, tahun, nilai)
SELECT k.id, c.id, 2024,
  CASE
    WHEN c.kode = 'C5' THEN (k.id * 5 + c.id * 2) % 12
    WHEN c.kode = 'C6' THEN GREATEST(0, COALESCE(EXTRACT(YEAR FROM AGE(DATE '2024-12-31', k.tanggal_masuk))::INTEGER, 0))
    ELSE 50 + ((k.id * 13 + c.id * 19) % 46)
  END AS nilai
FROM karyawan k
CROSS JOIN kriteria c
ON CONFLICT (id_karyawan, id_kriteria, tahun) DO UPDATE
SET nilai = EXCLUDED.nilai;
