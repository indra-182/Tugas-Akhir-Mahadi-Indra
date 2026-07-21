-- Migrasi: tambah konsep periode (tahun) ke penilaian & hasil_ranking.
-- Jalankan manual satu kali di tiap database (local MySQL dulu, baru Aiven).
-- Backfill pakai tahun berjalan (data lama dianggap milik tahun ini).

USE db_topsis_indivara_group;

ALTER TABLE penilaian ADD COLUMN tahun INT NULL AFTER id_kriteria;
UPDATE penilaian SET tahun = YEAR(CURDATE()) WHERE tahun IS NULL;
ALTER TABLE penilaian MODIFY COLUMN tahun INT NOT NULL;
ALTER TABLE penilaian DROP INDEX uq_karyawan_kriteria;
ALTER TABLE penilaian ADD CONSTRAINT uq_karyawan_kriteria_tahun UNIQUE (id_karyawan, id_kriteria, tahun);

ALTER TABLE hasil_ranking ADD COLUMN tahun INT NULL AFTER id_karyawan;
UPDATE hasil_ranking SET tahun = YEAR(CURDATE()) WHERE tahun IS NULL;
ALTER TABLE hasil_ranking MODIFY COLUMN tahun INT NOT NULL;
ALTER TABLE hasil_ranking ADD CONSTRAINT uq_karyawan_tahun UNIQUE (id_karyawan, tahun);
