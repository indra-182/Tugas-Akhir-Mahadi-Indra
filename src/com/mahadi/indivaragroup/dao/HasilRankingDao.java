package com.mahadi.indivaragroup.dao;

import com.mahadi.indivaragroup.model.HasilRanking;
import com.mahadi.indivaragroup.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HasilRankingDao {
    public void hapusSemua(int tahun) throws SQLException {
        String hapusSql = "DELETE FROM hasil_ranking WHERE tahun = ?";
        Connection koneksi = null;
        PreparedStatement perintahHapus = null;

        try {
            koneksi = DatabaseConnection.getConnection();
            perintahHapus = koneksi.prepareStatement(hapusSql);
            perintahHapus.setInt(1, tahun);
            perintahHapus.executeUpdate();
        } finally {
            DatabaseConnection.closeQuietly(perintahHapus);
            DatabaseConnection.closeQuietly(koneksi);
        }
    }

    public void gantiSemua(List<HasilRanking> daftarHasilRanking, int tahun) throws SQLException {
        String hapusSql = "DELETE FROM hasil_ranking WHERE tahun = ?";
        String tambahSql = "INSERT INTO hasil_ranking "
                + "(id_karyawan, tahun, nilai_topsis, peringkat) VALUES (?, ?, ?, ?)";

        Connection koneksi = null;
        PreparedStatement perintahHapus = null;
        PreparedStatement perintahTambah = null;

        try {
            koneksi = DatabaseConnection.getConnection();
            koneksi.setAutoCommit(false);

            perintahHapus = koneksi.prepareStatement(hapusSql);
            perintahHapus.setInt(1, tahun);
            perintahHapus.executeUpdate();

            perintahTambah = koneksi.prepareStatement(tambahSql);
            for (HasilRanking hasilRanking : daftarHasilRanking) {
                perintahTambah.setInt(1, hasilRanking.getIdKaryawan());
                perintahTambah.setInt(2, tahun);
                perintahTambah.setDouble(3, hasilRanking.getNilaiTopsis());
                perintahTambah.setInt(4, hasilRanking.getPeringkat());
                perintahTambah.addBatch();
            }
            perintahTambah.executeBatch();
            koneksi.commit();
        } catch (SQLException ex) {
            if (koneksi != null) {
                koneksi.rollback();
            }
            throw ex;
        } finally {
            DatabaseConnection.closeQuietly(perintahTambah);
            DatabaseConnection.closeQuietly(perintahHapus);
            if (koneksi != null) {
                koneksi.setAutoCommit(true);
            }
            DatabaseConnection.closeQuietly(koneksi);
        }
    }

    public List<HasilRanking> ambilSemua(int tahun) throws SQLException {
        String sql = "SELECT h.id_karyawan, h.tahun, b.kode_karyawan, b.nama, "
                + "h.nilai_topsis, h.peringkat "
                + "FROM hasil_ranking h "
                + "JOIN karyawan b ON h.id_karyawan = b.id "
                + "WHERE h.tahun = ? "
                + "ORDER BY h.peringkat";
        List<HasilRanking> daftarHasilRanking = new ArrayList<HasilRanking>();
        Connection koneksi = null;
        PreparedStatement perintah = null;
        ResultSet hasil = null;

        try {
            koneksi = DatabaseConnection.getConnection();
            perintah = koneksi.prepareStatement(sql);
            perintah.setInt(1, tahun);
            hasil = perintah.executeQuery();
            while (hasil.next()) {
                daftarHasilRanking.add(baca(hasil));
            }
            return daftarHasilRanking;
        } finally {
            DatabaseConnection.closeQuietly(hasil);
            DatabaseConnection.closeQuietly(perintah);
            DatabaseConnection.closeQuietly(koneksi);
        }
    }

    public List<HasilRanking> ambilRiwayatByKaryawan(int idKaryawan) throws SQLException {
        String sql = "SELECT h.id_karyawan, h.tahun, b.kode_karyawan, b.nama, "
                + "h.nilai_topsis, h.peringkat "
                + "FROM hasil_ranking h "
                + "JOIN karyawan b ON h.id_karyawan = b.id "
                + "WHERE h.id_karyawan = ? "
                + "ORDER BY h.tahun ASC";
        List<HasilRanking> daftarHasilRanking = new ArrayList<HasilRanking>();
        Connection koneksi = null;
        PreparedStatement perintah = null;
        ResultSet hasil = null;

        try {
            koneksi = DatabaseConnection.getConnection();
            perintah = koneksi.prepareStatement(sql);
            perintah.setInt(1, idKaryawan);
            hasil = perintah.executeQuery();
            while (hasil.next()) {
                daftarHasilRanking.add(baca(hasil));
            }
            return daftarHasilRanking;
        } finally {
            DatabaseConnection.closeQuietly(hasil);
            DatabaseConnection.closeQuietly(perintah);
            DatabaseConnection.closeQuietly(koneksi);
        }
    }

    private HasilRanking baca(ResultSet hasil) throws SQLException {
        HasilRanking hasilRanking = new HasilRanking();
        hasilRanking.setIdKaryawan(hasil.getInt("id_karyawan"));
        hasilRanking.setTahun(hasil.getInt("tahun"));
        hasilRanking.setKodeKaryawan(hasil.getString("kode_karyawan"));
        hasilRanking.setNamaKaryawan(hasil.getString("nama"));
        hasilRanking.setNilaiTopsis(hasil.getDouble("nilai_topsis"));
        hasilRanking.setPeringkat(hasil.getInt("peringkat"));
        return hasilRanking;
    }
}
