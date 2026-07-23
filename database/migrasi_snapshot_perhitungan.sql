-- PostgreSQL migration for databases created before immutable calculation snapshots.
-- Safe to run more than once. Existing years are converted on first access by the application.
CREATE TABLE IF NOT EXISTS perhitungan_topsis_snapshot (
  tahun INTEGER PRIMARY KEY,
  dihitung_pada TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS perhitungan_snapshot_kriteria (
  tahun INTEGER NOT NULL REFERENCES perhitungan_topsis_snapshot(tahun) ON DELETE CASCADE,
  id_kriteria_asal INTEGER NOT NULL,
  kode VARCHAR(10) NOT NULL,
  nama VARCHAR(100) NOT NULL,
  bobot DECIMAL(10,4) NOT NULL,
  tipe VARCHAR(10) NOT NULL,
  keterangan VARCHAR(255),
  PRIMARY KEY (tahun, id_kriteria_asal)
);

CREATE TABLE IF NOT EXISTS perhitungan_snapshot_peserta (
  tahun INTEGER NOT NULL REFERENCES perhitungan_topsis_snapshot(tahun) ON DELETE CASCADE,
  id_karyawan_asal INTEGER NOT NULL,
  kode_karyawan VARCHAR(30) NOT NULL,
  nama VARCHAR(100) NOT NULL,
  divisi VARCHAR(100),
  jabatan VARCHAR(100),
  nilai_topsis DECIMAL(12,6) NOT NULL,
  peringkat INTEGER NOT NULL,
  PRIMARY KEY (tahun, id_karyawan_asal)
);

CREATE TABLE IF NOT EXISTS perhitungan_snapshot_penilaian (
  tahun INTEGER NOT NULL,
  id_karyawan_asal INTEGER NOT NULL,
  id_kriteria_asal INTEGER NOT NULL,
  nilai DECIMAL(10,4) NOT NULL,
  PRIMARY KEY (tahun, id_karyawan_asal, id_kriteria_asal),
  FOREIGN KEY (tahun, id_karyawan_asal) REFERENCES perhitungan_snapshot_peserta(tahun, id_karyawan_asal) ON DELETE CASCADE,
  FOREIGN KEY (tahun, id_kriteria_asal) REFERENCES perhitungan_snapshot_kriteria(tahun, id_kriteria_asal) ON DELETE CASCADE
);

-- The application connects as app_topsis; new tables need explicit RLS policies.
GRANT SELECT, INSERT, UPDATE, DELETE ON perhitungan_topsis_snapshot,
  perhitungan_snapshot_kriteria, perhitungan_snapshot_peserta,
  perhitungan_snapshot_penilaian TO app_topsis;

ALTER TABLE perhitungan_topsis_snapshot ENABLE ROW LEVEL SECURITY;
ALTER TABLE perhitungan_snapshot_kriteria ENABLE ROW LEVEL SECURITY;
ALTER TABLE perhitungan_snapshot_peserta ENABLE ROW LEVEL SECURITY;
ALTER TABLE perhitungan_snapshot_penilaian ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS app_topsis_penuh_perhitungan_topsis_snapshot ON perhitungan_topsis_snapshot;
DROP POLICY IF EXISTS app_topsis_penuh_perhitungan_snapshot_kriteria ON perhitungan_snapshot_kriteria;
DROP POLICY IF EXISTS app_topsis_penuh_perhitungan_snapshot_peserta ON perhitungan_snapshot_peserta;
DROP POLICY IF EXISTS app_topsis_penuh_perhitungan_snapshot_penilaian ON perhitungan_snapshot_penilaian;

CREATE POLICY app_topsis_penuh_perhitungan_topsis_snapshot ON perhitungan_topsis_snapshot
  FOR ALL TO app_topsis USING (true) WITH CHECK (true);
CREATE POLICY app_topsis_penuh_perhitungan_snapshot_kriteria ON perhitungan_snapshot_kriteria
  FOR ALL TO app_topsis USING (true) WITH CHECK (true);
CREATE POLICY app_topsis_penuh_perhitungan_snapshot_peserta ON perhitungan_snapshot_peserta
  FOR ALL TO app_topsis USING (true) WITH CHECK (true);
CREATE POLICY app_topsis_penuh_perhitungan_snapshot_penilaian ON perhitungan_snapshot_penilaian
  FOR ALL TO app_topsis USING (true) WITH CHECK (true);
