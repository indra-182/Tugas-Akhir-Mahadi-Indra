-- Migrasi PostgreSQL untuk database lama yang belum memiliki kolom periode.
-- Database baru cukup menggunakan db_topsis_indivara_group.sql.

ALTER TABLE penilaian ADD COLUMN IF NOT EXISTS tahun INTEGER;
UPDATE penilaian SET tahun = EXTRACT(YEAR FROM CURRENT_DATE)::INTEGER WHERE tahun IS NULL;
ALTER TABLE penilaian ALTER COLUMN tahun SET NOT NULL;
ALTER TABLE penilaian DROP CONSTRAINT IF EXISTS uq_karyawan_kriteria;
ALTER TABLE penilaian DROP CONSTRAINT IF EXISTS uq_karyawan_kriteria_tahun;
ALTER TABLE penilaian ADD CONSTRAINT uq_karyawan_kriteria_tahun UNIQUE (id_karyawan, id_kriteria, tahun);

ALTER TABLE hasil_ranking ADD COLUMN IF NOT EXISTS tahun INTEGER;
UPDATE hasil_ranking SET tahun = EXTRACT(YEAR FROM CURRENT_DATE)::INTEGER WHERE tahun IS NULL;
ALTER TABLE hasil_ranking ALTER COLUMN tahun SET NOT NULL;
ALTER TABLE hasil_ranking DROP CONSTRAINT IF EXISTS uq_karyawan_tahun;
ALTER TABLE hasil_ranking ADD CONSTRAINT uq_karyawan_tahun UNIQUE (id_karyawan, tahun);
