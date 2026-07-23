-- Jalankan sebagai pemilik project di Supabase SQL Editor setelah skema dibuat.
-- Ganti GANTI_DENGAN_PASSWORD_KUAT sebelum menjalankan skrip ini.
-- Jalankan CREATE ROLE satu kali. Untuk mengganti password setelahnya, gunakan:
-- ALTER ROLE app_topsis WITH LOGIN PASSWORD 'PASSWORD_BARU';

CREATE ROLE app_topsis LOGIN PASSWORD 'GANTI_DENGAN_PASSWORD_KUAT';

GRANT USAGE ON SCHEMA public TO app_topsis;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_topsis;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO app_topsis;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO app_topsis;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT USAGE, SELECT ON SEQUENCES TO app_topsis;

ALTER TABLE pengguna ENABLE ROW LEVEL SECURITY;
ALTER TABLE karyawan ENABLE ROW LEVEL SECURITY;
ALTER TABLE kriteria ENABLE ROW LEVEL SECURITY;
ALTER TABLE penilaian ENABLE ROW LEVEL SECURITY;
ALTER TABLE hasil_ranking ENABLE ROW LEVEL SECURITY;
ALTER TABLE perhitungan_topsis_snapshot ENABLE ROW LEVEL SECURITY;
ALTER TABLE perhitungan_snapshot_kriteria ENABLE ROW LEVEL SECURITY;
ALTER TABLE perhitungan_snapshot_peserta ENABLE ROW LEVEL SECURITY;
ALTER TABLE perhitungan_snapshot_penilaian ENABLE ROW LEVEL SECURITY;

CREATE POLICY app_topsis_penuh_pengguna ON pengguna
  FOR ALL TO app_topsis USING (true) WITH CHECK (true);
CREATE POLICY app_topsis_penuh_karyawan ON karyawan
  FOR ALL TO app_topsis USING (true) WITH CHECK (true);
CREATE POLICY app_topsis_penuh_kriteria ON kriteria
  FOR ALL TO app_topsis USING (true) WITH CHECK (true);
CREATE POLICY app_topsis_penuh_penilaian ON penilaian
  FOR ALL TO app_topsis USING (true) WITH CHECK (true);
CREATE POLICY app_topsis_penuh_hasil_ranking ON hasil_ranking
  FOR ALL TO app_topsis USING (true) WITH CHECK (true);
CREATE POLICY app_topsis_penuh_perhitungan_topsis_snapshot ON perhitungan_topsis_snapshot
  FOR ALL TO app_topsis USING (true) WITH CHECK (true);
CREATE POLICY app_topsis_penuh_perhitungan_snapshot_kriteria ON perhitungan_snapshot_kriteria
  FOR ALL TO app_topsis USING (true) WITH CHECK (true);
CREATE POLICY app_topsis_penuh_perhitungan_snapshot_peserta ON perhitungan_snapshot_peserta
  FOR ALL TO app_topsis USING (true) WITH CHECK (true);
CREATE POLICY app_topsis_penuh_perhitungan_snapshot_penilaian ON perhitungan_snapshot_penilaian
  FOR ALL TO app_topsis USING (true) WITH CHECK (true);
