-- Perbaikan: migrasi_periode_tahun.sql seharusnya menghapus index lama
-- uq_karyawan_kriteria (id_karyawan, id_kriteria) saat menambah kolom tahun,
-- tapi index lama itu masih ada berbarengan dengan uq_karyawan_kriteria_tahun
-- yang baru. Akibatnya INSERT untuk tahun baru selalu "duplicate" terhadap
-- index lama (yang mengabaikan tahun) dan malah meng-UPDATE baris tahun 2026
-- yang sudah ada, bukan membuat baris baru untuk tahun 2025/2024.
--
-- Jalankan file ini satu kali di database local.

USE db_topsis_indivara_group;

-- 1) Hapus index lama yang sudah tidak relevan sejak ada kolom tahun.
ALTER TABLE penilaian DROP INDEX uq_karyawan_kriteria;

-- 2) Pulihkan nilai tahun 2026 ke rumus aslinya (aman dijalankan meskipun
--    datanya ternyata belum sempat tertimpa - hasilnya identik / no-op).
UPDATE penilaian p
JOIN karyawan k ON p.id_karyawan = k.id
JOIN kriteria c ON p.id_kriteria = c.id
SET p.nilai = CASE
    WHEN c.kode = 'C5' THEN MOD(k.id * 3 + c.id * 5, 13)
    WHEN c.kode = 'C6' THEN COALESCE(TIMESTAMPDIFF(YEAR, k.tanggal_masuk, CURDATE()), 0)
    ELSE 60 + MOD(k.id * 11 + c.id * 13, 41)
  END
WHERE p.tahun = 2026;

-- 3) Sekarang aman: seed ulang 2025 dan 2024 (index lama sudah tidak ada,
--    jadi INSERT ini akan benar-benar membuat baris baru, bukan menimpa 2026).
INSERT INTO penilaian (id_karyawan, id_kriteria, tahun, nilai)
SELECT k.id, c.id, 2025,
  CASE
    WHEN c.kode = 'C5' THEN MOD(k.id * 7 + c.id * 3, 11)
    WHEN c.kode = 'C6' THEN GREATEST(0, COALESCE(TIMESTAMPDIFF(YEAR, k.tanggal_masuk, '2025-12-31'), 0))
    ELSE 55 + MOD(k.id * 17 + c.id * 7, 41)
  END AS nilai
FROM karyawan k, kriteria c
ON DUPLICATE KEY UPDATE nilai = VALUES(nilai);

INSERT INTO penilaian (id_karyawan, id_kriteria, tahun, nilai)
SELECT k.id, c.id, 2024,
  CASE
    WHEN c.kode = 'C5' THEN MOD(k.id * 5 + c.id * 2, 12)
    WHEN c.kode = 'C6' THEN GREATEST(0, COALESCE(TIMESTAMPDIFF(YEAR, k.tanggal_masuk, '2024-12-31'), 0))
    ELSE 50 + MOD(k.id * 13 + c.id * 19, 46)
  END AS nilai
FROM karyawan k, kriteria c
ON DUPLICATE KEY UPDATE nilai = VALUES(nilai);
