-- Jalankan pada database yang sudah ada setelah memastikan semua tanggal masuk tersedia.
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM karyawan WHERE tanggal_masuk IS NULL) THEN
    RAISE EXCEPTION 'Tidak dapat mewajibkan tanggal_masuk: masih ada data karyawan tanpa tanggal masuk.';
  END IF;
END;
$$;

ALTER TABLE karyawan
  ALTER COLUMN tanggal_masuk SET NOT NULL;
