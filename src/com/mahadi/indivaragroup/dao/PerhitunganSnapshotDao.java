package com.mahadi.indivaragroup.dao;

import com.mahadi.indivaragroup.model.Karyawan;
import com.mahadi.indivaragroup.model.Kriteria;
import com.mahadi.indivaragroup.model.PerhitunganSnapshot;
import com.mahadi.indivaragroup.model.HasilRanking;
import com.mahadi.indivaragroup.service.PerhitunganTopsisService.PerhitunganDetail;
import com.mahadi.indivaragroup.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerhitunganSnapshotDao {
    public boolean ada(int tahun) throws SQLException {
        String sql = "SELECT 1 FROM perhitungan_topsis_snapshot WHERE tahun = ?";
        Connection koneksi = null; PreparedStatement perintah = null; ResultSet hasil = null;
        try {
            koneksi = DatabaseConnection.getConnection(); perintah = koneksi.prepareStatement(sql);
            perintah.setInt(1, tahun);
            hasil = perintah.executeQuery(); return hasil.next();
        } finally { DatabaseConnection.closeQuietly(hasil); DatabaseConnection.closeQuietly(perintah); DatabaseConnection.closeQuietly(koneksi); }
    }

    public void hapus(int tahun) throws SQLException {
        Connection koneksi = null; PreparedStatement perintah = null;
        try {
            koneksi = DatabaseConnection.getConnection(); perintah = koneksi.prepareStatement("DELETE FROM perhitungan_topsis_snapshot WHERE tahun = ?");
            perintah.setInt(1, tahun);
            perintah.executeUpdate();
        } finally { DatabaseConnection.closeQuietly(perintah); DatabaseConnection.closeQuietly(koneksi); }
    }

    public void simpan(int tahun, PerhitunganDetail detail) throws SQLException {
        Connection koneksi = null;
        try {
            koneksi = DatabaseConnection.getConnection();
            koneksi.setAutoCommit(false);
            try (PreparedStatement hapus = koneksi.prepareStatement("DELETE FROM perhitungan_topsis_snapshot WHERE tahun = ?")) {
                hapus.setInt(1, tahun); hapus.executeUpdate();
            }
            try (PreparedStatement header = koneksi.prepareStatement("INSERT INTO perhitungan_topsis_snapshot (tahun) VALUES (?)")) {
                header.setInt(1, tahun); header.executeUpdate();
            }
            try (PreparedStatement kriteria = koneksi.prepareStatement("INSERT INTO perhitungan_snapshot_kriteria (tahun, id_kriteria_asal, kode, nama, bobot, tipe, keterangan) VALUES (?, ?, ?, ?, ?, ?, ?)");
                 PreparedStatement peserta = koneksi.prepareStatement("INSERT INTO perhitungan_snapshot_peserta (tahun, id_karyawan_asal, kode_karyawan, nama, divisi, jabatan, nilai_topsis, peringkat) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                 PreparedStatement nilai = koneksi.prepareStatement("INSERT INTO perhitungan_snapshot_penilaian (tahun, id_karyawan_asal, id_kriteria_asal, nilai) VALUES (?, ?, ?, ?)")) {
                for (Kriteria item : detail.getDaftarKriteria()) {
                    kriteria.setInt(1, tahun); kriteria.setInt(2, item.getId()); kriteria.setString(3, item.getKode());
                    kriteria.setString(4, item.getNama()); kriteria.setDouble(5, item.getBobot()); kriteria.setString(6, item.getTipe()); kriteria.setString(7, item.getKeterangan()); kriteria.addBatch();
                }
                kriteria.executeBatch();
                Map<Integer, Map<Integer, Double>> matriks = detail.getMatriksPenilaian();
                for (int i = 0; i < detail.getDaftarKaryawan().size(); i++) {
                    Karyawan item = detail.getDaftarKaryawan().get(i);
                    peserta.setInt(1, tahun); peserta.setInt(2, item.getId()); peserta.setString(3, item.getKodeKaryawan()); peserta.setString(4, item.getNama()); peserta.setString(5, item.getDivisi()); peserta.setString(6, item.getJabatan()); peserta.setDouble(7, detail.getNilaiPreferensi()[i]); peserta.setInt(8, cariPeringkat(detail, item.getId())); peserta.addBatch();
                    for (Kriteria ukuran : detail.getDaftarKriteria()) {
                        nilai.setInt(1, tahun); nilai.setInt(2, item.getId()); nilai.setInt(3, ukuran.getId()); nilai.setDouble(4, matriks.get(item.getId()).get(ukuran.getId())); nilai.addBatch();
                    }
                }
                peserta.executeBatch(); nilai.executeBatch();
            }
            koneksi.commit();
        } catch (SQLException ex) {
            if (koneksi != null) koneksi.rollback();
            throw ex;
        } finally {
            if (koneksi != null) { try { koneksi.setAutoCommit(true); } catch (SQLException ignored) { } DatabaseConnection.closeQuietly(koneksi); }
        }
    }

    public PerhitunganSnapshot ambil(int tahun) throws SQLException {
        List<Kriteria> kriteria = new ArrayList<Kriteria>();
        List<Karyawan> karyawan = new ArrayList<Karyawan>();
        Map<Integer, Map<Integer, Double>> matriks = new HashMap<Integer, Map<Integer, Double>>();
        Connection koneksi = null;
        try {
            koneksi = DatabaseConnection.getConnection();
            try (PreparedStatement perintah = koneksi.prepareStatement("SELECT id_kriteria_asal, kode, nama, bobot, tipe, keterangan FROM perhitungan_snapshot_kriteria WHERE tahun = ? ORDER BY kode")) {
                perintah.setInt(1, tahun); try (ResultSet hasil = perintah.executeQuery()) { while (hasil.next()) { Kriteria item = new Kriteria(); item.setId(hasil.getInt(1)); item.setKode(hasil.getString(2)); item.setNama(hasil.getString(3)); item.setBobot(hasil.getDouble(4)); item.setTipe(hasil.getString(5)); item.setKeterangan(hasil.getString(6)); kriteria.add(item); } }
            }
            try (PreparedStatement perintah = koneksi.prepareStatement("SELECT id_karyawan_asal, kode_karyawan, nama, divisi, jabatan FROM perhitungan_snapshot_peserta WHERE tahun = ? ORDER BY kode_karyawan")) {
                perintah.setInt(1, tahun); try (ResultSet hasil = perintah.executeQuery()) { while (hasil.next()) { Karyawan item = new Karyawan(); item.setId(hasil.getInt(1)); item.setKodeKaryawan(hasil.getString(2)); item.setNama(hasil.getString(3)); item.setDivisi(hasil.getString(4)); item.setJabatan(hasil.getString(5)); karyawan.add(item); matriks.put(item.getId(), new HashMap<Integer, Double>()); } }
            }
            try (PreparedStatement perintah = koneksi.prepareStatement("SELECT id_karyawan_asal, id_kriteria_asal, nilai FROM perhitungan_snapshot_penilaian WHERE tahun = ?")) {
                perintah.setInt(1, tahun); try (ResultSet hasil = perintah.executeQuery()) { while (hasil.next()) { Map<Integer, Double> nilai = matriks.get(hasil.getInt(1)); if (nilai != null) nilai.put(hasil.getInt(2), hasil.getDouble(3)); } }
            }
        } finally { DatabaseConnection.closeQuietly(koneksi); }
        return new PerhitunganSnapshot(tahun, karyawan, kriteria, matriks);
    }

    public List<HasilRanking> ambilRanking(int tahun) throws SQLException {
        String sql = "SELECT id_karyawan_asal, kode_karyawan, nama, nilai_topsis, peringkat FROM perhitungan_snapshot_peserta WHERE tahun = ? ORDER BY peringkat";
        List<HasilRanking> daftar = new ArrayList<HasilRanking>();
        Connection koneksi = null; PreparedStatement perintah = null; ResultSet hasil = null;
        try {
            koneksi = DatabaseConnection.getConnection(); perintah = koneksi.prepareStatement(sql);
            perintah.setInt(1, tahun);
            hasil = perintah.executeQuery(); while (hasil.next()) daftar.add(bacaRanking(hasil, tahun));
        } finally { DatabaseConnection.closeQuietly(hasil); DatabaseConnection.closeQuietly(perintah); DatabaseConnection.closeQuietly(koneksi); }
        return daftar;
    }

    public List<HasilRanking> ambilRiwayat(int idKaryawan) throws SQLException {
        String sql = "SELECT tahun, id_karyawan_asal, kode_karyawan, nama, nilai_topsis, peringkat FROM perhitungan_snapshot_peserta WHERE id_karyawan_asal = ? ORDER BY tahun";
        List<HasilRanking> daftar = new ArrayList<HasilRanking>();
        Connection koneksi = null; PreparedStatement perintah = null; ResultSet hasil = null;
        try {
            koneksi = DatabaseConnection.getConnection(); perintah = koneksi.prepareStatement(sql);
            perintah.setInt(1, idKaryawan);
            hasil = perintah.executeQuery(); while (hasil.next()) daftar.add(bacaRanking(hasil, hasil.getInt("tahun")));
        } finally { DatabaseConnection.closeQuietly(hasil); DatabaseConnection.closeQuietly(perintah); DatabaseConnection.closeQuietly(koneksi); }
        return daftar;
    }

    public List<Karyawan> ambilPesertaTersimpan() throws SQLException {
        String sql = "SELECT DISTINCT ON (id_karyawan_asal) id_karyawan_asal, kode_karyawan, nama, divisi, jabatan FROM perhitungan_snapshot_peserta ORDER BY id_karyawan_asal, tahun DESC";
        List<Karyawan> daftar = new ArrayList<Karyawan>();
        Connection koneksi = null; PreparedStatement perintah = null; ResultSet hasil = null;
        try {
            koneksi = DatabaseConnection.getConnection(); perintah = koneksi.prepareStatement(sql); hasil = perintah.executeQuery();
            while (hasil.next()) { Karyawan item = new Karyawan(); item.setId(hasil.getInt(1)); item.setKodeKaryawan(hasil.getString(2)); item.setNama(hasil.getString(3)); item.setDivisi(hasil.getString(4)); item.setJabatan(hasil.getString(5)); daftar.add(item); }
        } finally { DatabaseConnection.closeQuietly(hasil); DatabaseConnection.closeQuietly(perintah); DatabaseConnection.closeQuietly(koneksi); }
        return daftar;
    }

    private HasilRanking bacaRanking(ResultSet hasil, int tahun) throws SQLException {
        HasilRanking item = new HasilRanking(); item.setIdKaryawan(hasil.getInt("id_karyawan_asal")); item.setTahun(tahun);
        item.setKodeKaryawan(hasil.getString("kode_karyawan")); item.setNamaKaryawan(hasil.getString("nama"));
        item.setNilaiTopsis(hasil.getDouble("nilai_topsis")); item.setPeringkat(hasil.getInt("peringkat")); return item;
    }

    private int cariPeringkat(PerhitunganDetail detail, int idKaryawan) {
        for (com.mahadi.indivaragroup.model.HasilRanking hasil : detail.getDaftarHasilRanking()) if (hasil.getIdKaryawan() == idKaryawan) return hasil.getPeringkat();
        return 0;
    }
}
