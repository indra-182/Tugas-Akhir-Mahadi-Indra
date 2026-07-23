package com.mahadi.indivaragroup.dao;

import com.mahadi.indivaragroup.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PenilaianDao {
    public Map<Integer, Map<Integer, Double>> ambilSemuaSebagaiMatriks(int tahun) throws SQLException {
        String sql = "SELECT id_karyawan, id_kriteria, nilai FROM penilaian WHERE tahun = ?";
        Map<Integer, Map<Integer, Double>> matriks = new HashMap<>();
        Connection koneksi = null;
        PreparedStatement perintah = null;
        ResultSet hasil = null;

        try {
            koneksi = DatabaseConnection.getConnection();
            perintah = koneksi.prepareStatement(sql);
            perintah.setInt(1, tahun);
            hasil = perintah.executeQuery();
            while (hasil.next()) {
                int idKaryawan = hasil.getInt("id_karyawan");
                int idKriteria = hasil.getInt("id_kriteria");
                double nilai = hasil.getDouble("nilai");

                if (!matriks.containsKey(idKaryawan)) {
                    matriks.put(idKaryawan, new HashMap<>());
                }
                matriks.get(idKaryawan).put(idKriteria, nilai);
            }
            return matriks;
        } finally {
            DatabaseConnection.closeQuietly(hasil);
            DatabaseConnection.closeQuietly(perintah);
            DatabaseConnection.closeQuietly(koneksi);
        }
    }

    public Map<Integer, Double> ambilBerdasarkanKaryawan(int idKaryawan, int tahun) throws SQLException {
        String sql = "SELECT id_kriteria, nilai FROM penilaian WHERE id_karyawan = ? AND tahun = ?";
        Map<Integer, Double> daftarNilai = new HashMap<Integer, Double>();
        Connection koneksi = null;
        PreparedStatement perintah = null;
        ResultSet hasil = null;

        try {
            koneksi = DatabaseConnection.getConnection();
            perintah = koneksi.prepareStatement(sql);
            perintah.setInt(1, idKaryawan);
            perintah.setInt(2, tahun);
            hasil = perintah.executeQuery();
            while (hasil.next()) {
                daftarNilai.put(hasil.getInt("id_kriteria"), hasil.getDouble("nilai"));
            }
            return daftarNilai;
        } finally {
            DatabaseConnection.closeQuietly(hasil);
            DatabaseConnection.closeQuietly(perintah);
            DatabaseConnection.closeQuietly(koneksi);
        }
    }

    public void simpan(int idKaryawan, int idKriteria, int tahun, double nilai) throws SQLException {
        String sql = "INSERT INTO penilaian (id_karyawan, id_kriteria, tahun, nilai) VALUES (?, ?, ?, ?) "
                + "ON CONFLICT (id_karyawan, id_kriteria, tahun) DO UPDATE SET nilai = EXCLUDED.nilai";
        Connection koneksi = null;
        PreparedStatement perintah = null;

        try {
            koneksi = DatabaseConnection.getConnection();
            perintah = koneksi.prepareStatement(sql);
            perintah.setInt(1, idKaryawan);
            perintah.setInt(2, idKriteria);
            perintah.setInt(3, tahun);
            perintah.setDouble(4, nilai);
            perintah.executeUpdate();
        } finally {
            DatabaseConnection.closeQuietly(perintah);
            DatabaseConnection.closeQuietly(koneksi);
        }
    }

    public boolean apakahPenilaianLengkap(int tahun) throws SQLException {
        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM karyawan WHERE status = 'AKTIF') AS jumlah_karyawan, "
                + "(SELECT COUNT(*) FROM kriteria) AS jumlah_kriteria, "
                + "(SELECT COUNT(*) FROM penilaian p "
                + "JOIN karyawan k ON p.id_karyawan = k.id WHERE k.status = 'AKTIF' AND p.tahun = ?) AS jumlah_penilaian";
        Connection koneksi = null;
        PreparedStatement perintah = null;
        ResultSet hasil = null;

        try {
            koneksi = DatabaseConnection.getConnection();
            perintah = koneksi.prepareStatement(sql);
            perintah.setInt(1, tahun);
            hasil = perintah.executeQuery();
            if (!hasil.next()) {
                return false;
            }
            int jumlahKaryawan = hasil.getInt("jumlah_karyawan");
            int jumlahKriteria = hasil.getInt("jumlah_kriteria");
            int jumlahPenilaian = hasil.getInt("jumlah_penilaian");
            return jumlahKaryawan > 0 && jumlahKriteria > 0
                    && jumlahPenilaian == jumlahKaryawan * jumlahKriteria;
        } finally {
            DatabaseConnection.closeQuietly(hasil);
            DatabaseConnection.closeQuietly(perintah);
            DatabaseConnection.closeQuietly(koneksi);
        }
    }

    public List<Object[]> ambilLaporanPenilaian(int tahun) throws SQLException {
        String sql = "SELECT k.kode_karyawan, k.nama AS nama_karyawan, kr.kode AS kode_kriteria, "
                + "kr.nama AS nama_kriteria, p.nilai "
                + "FROM penilaian p "
                + "JOIN karyawan k ON p.id_karyawan = k.id "
                + "JOIN kriteria kr ON p.id_kriteria = kr.id "
                + "WHERE p.tahun = ? "
                + "ORDER BY k.kode_karyawan, kr.kode";
        List<Object[]> data = new ArrayList<Object[]>();
        Connection koneksi = null;
        PreparedStatement perintah = null;
        ResultSet hasil = null;

        try {
            koneksi = DatabaseConnection.getConnection();
            perintah = koneksi.prepareStatement(sql);
            perintah.setInt(1, tahun);
            hasil = perintah.executeQuery();
            while (hasil.next()) {
                data.add(new Object[]{
                    hasil.getString("kode_karyawan"),
                    hasil.getString("nama_karyawan"),
                    hasil.getString("kode_kriteria"),
                    hasil.getString("nama_kriteria"),
                    hasil.getDouble("nilai")
                });
            }
            return data;
        } finally {
            DatabaseConnection.closeQuietly(hasil);
            DatabaseConnection.closeQuietly(perintah);
            DatabaseConnection.closeQuietly(koneksi);
        }
    }

    public List<Integer> ambilDaftarTahun() throws SQLException {
        String sql = "SELECT DISTINCT tahun FROM penilaian ORDER BY tahun DESC";
        List<Integer> daftarTahun = new ArrayList<Integer>();
        Connection koneksi = null;
        PreparedStatement perintah = null;
        ResultSet hasil = null;

        try {
            koneksi = DatabaseConnection.getConnection();
            perintah = koneksi.prepareStatement(sql);
            hasil = perintah.executeQuery();
            while (hasil.next()) {
                daftarTahun.add(hasil.getInt("tahun"));
            }
            return daftarTahun;
        } finally {
            DatabaseConnection.closeQuietly(hasil);
            DatabaseConnection.closeQuietly(perintah);
            DatabaseConnection.closeQuietly(koneksi);
        }
    }

    public List<Integer> ambilDaftarTahunByKaryawan(int idKaryawan) throws SQLException {
        String sql = "SELECT DISTINCT tahun FROM penilaian WHERE id_karyawan = ? ORDER BY tahun ASC";
        List<Integer> daftar = new ArrayList<Integer>();
        Connection koneksi = null; PreparedStatement perintah = null; ResultSet hasil = null;
        try {
            koneksi = DatabaseConnection.getConnection(); perintah = koneksi.prepareStatement(sql);
            perintah.setInt(1, idKaryawan);
            hasil = perintah.executeQuery(); while (hasil.next()) daftar.add(hasil.getInt("tahun"));
        } finally { DatabaseConnection.closeQuietly(hasil); DatabaseConnection.closeQuietly(perintah); DatabaseConnection.closeQuietly(koneksi); }
        return daftar;
    }
}
