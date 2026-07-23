package com.mahadi.indivaragroup.dao;

import com.mahadi.indivaragroup.model.Karyawan;
import com.mahadi.indivaragroup.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KaryawanDao {
    private static final String KOLOM_KARYAWAN =
            "id, kode_karyawan, nama, divisi, jabatan, tanggal_masuk, status";

    public List<Karyawan> ambilSemua() throws SQLException {
        String sql = "SELECT " + KOLOM_KARYAWAN + " FROM karyawan ORDER BY kode_karyawan";
        return ambilDaftarKaryawan(sql, null);
    }

    public List<Karyawan> ambilAktif() throws SQLException {
        String sql = "SELECT " + KOLOM_KARYAWAN + " FROM karyawan WHERE status = ? ORDER BY kode_karyawan";
        return ambilDaftarKaryawan(sql, "AKTIF");
    }

    public List<Karyawan> ambilDinilaiPadaTahun(int tahun) throws SQLException {
        String kolomKaryawan = "k.id, k.kode_karyawan, k.nama, k.divisi, k.jabatan, k.tanggal_masuk, k.status";
        String sql = "SELECT DISTINCT " + kolomKaryawan + " FROM karyawan k "
                + "JOIN penilaian p ON p.id_karyawan = k.id WHERE p.tahun = ? ORDER BY k.kode_karyawan";
        return ambilDaftarKaryawanDenganTahun(sql, tahun);
    }

    public int hitungSemua() throws SQLException {
        String sql = "SELECT COUNT(*) AS jumlah FROM karyawan";
        return hitungData(sql);
    }

    public void tambah(Karyawan karyawan) throws SQLException {
        String sql = "INSERT INTO karyawan (kode_karyawan, nama, divisi, jabatan, tanggal_masuk, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        simpan(sql, karyawan, false);
    }

    public void ubah(Karyawan karyawan) throws SQLException {
        String sql = "UPDATE karyawan SET kode_karyawan = ?, nama = ?, divisi = ?, jabatan = ?, "
                + "tanggal_masuk = ?, status = ? WHERE id = ?";
        simpan(sql, karyawan, true);
    }

    public void hapus(int id) throws SQLException {
        String sql = "DELETE FROM karyawan WHERE id = ?";
        Connection koneksi = null;
        PreparedStatement perintah = null;
        try {
            koneksi = DatabaseConnection.getConnection();
            perintah = koneksi.prepareStatement(sql);
            perintah.setInt(1, id);
            perintah.executeUpdate();
        } finally {
            DatabaseConnection.closeQuietly(perintah);
            DatabaseConnection.closeQuietly(koneksi);
        }
    }

    private List<Karyawan> ambilDaftarKaryawan(String sql, String status) throws SQLException {
        List<Karyawan> daftarKaryawan = new ArrayList<Karyawan>();
        Connection koneksi = null;
        PreparedStatement perintah = null;
        ResultSet hasil = null;

        try {
            koneksi = DatabaseConnection.getConnection();
            perintah = koneksi.prepareStatement(sql);
            if (status != null) {
                perintah.setString(1, status);
            }
            hasil = perintah.executeQuery();
            while (hasil.next()) {
                daftarKaryawan.add(petakanKaryawan(hasil));
            }
            return daftarKaryawan;
        } finally {
            DatabaseConnection.closeQuietly(hasil);
            DatabaseConnection.closeQuietly(perintah);
            DatabaseConnection.closeQuietly(koneksi);
        }
    }

    private List<Karyawan> ambilDaftarKaryawanDenganTahun(String sql, int tahun) throws SQLException {
        List<Karyawan> daftarKaryawan = new ArrayList<Karyawan>();
        Connection koneksi = null; PreparedStatement perintah = null; ResultSet hasil = null;
        try {
            koneksi = DatabaseConnection.getConnection(); perintah = koneksi.prepareStatement(sql);
            perintah.setInt(1, tahun);
            hasil = perintah.executeQuery(); while (hasil.next()) daftarKaryawan.add(petakanKaryawan(hasil));
        } finally { DatabaseConnection.closeQuietly(hasil); DatabaseConnection.closeQuietly(perintah); DatabaseConnection.closeQuietly(koneksi); }
        return daftarKaryawan;
    }

    private int hitungData(String sql) throws SQLException {
        Connection koneksi = null;
        PreparedStatement perintah = null;
        ResultSet hasil = null;
        try {
            koneksi = DatabaseConnection.getConnection();
            perintah = koneksi.prepareStatement(sql);
            hasil = perintah.executeQuery();
            return hasil.next() ? hasil.getInt("jumlah") : 0;
        } finally {
            DatabaseConnection.closeQuietly(hasil);
            DatabaseConnection.closeQuietly(perintah);
            DatabaseConnection.closeQuietly(koneksi);
        }
    }

    private void simpan(String sql, Karyawan karyawan, boolean ubah) throws SQLException {
        Connection koneksi = null;
        PreparedStatement perintah = null;
        try {
            koneksi = DatabaseConnection.getConnection();
            perintah = koneksi.prepareStatement(sql);
            perintah.setString(1, karyawan.getKodeKaryawan());
            perintah.setString(2, karyawan.getNama());
            perintah.setString(3, karyawan.getDivisi());
            perintah.setString(4, karyawan.getJabatan());
            perintah.setString(5, kosongJadiNull(karyawan.getTanggalMasuk()));
            perintah.setString(6, karyawan.getStatus());
            if (ubah) {
                perintah.setInt(7, karyawan.getId());
            }
            perintah.executeUpdate();
        } finally {
            DatabaseConnection.closeQuietly(perintah);
            DatabaseConnection.closeQuietly(koneksi);
        }
    }

    private String kosongJadiNull(String nilai) {
        return nilai == null || nilai.trim().isEmpty() ? null : nilai.trim();
    }

    private Karyawan petakanKaryawan(ResultSet hasil) throws SQLException {
        Karyawan karyawan = new Karyawan();
        karyawan.setId(hasil.getInt("id"));
        karyawan.setKodeKaryawan(hasil.getString("kode_karyawan"));
        karyawan.setNama(hasil.getString("nama"));
        karyawan.setDivisi(hasil.getString("divisi"));
        karyawan.setJabatan(hasil.getString("jabatan"));
        karyawan.setTanggalMasuk(hasil.getString("tanggal_masuk"));
        karyawan.setStatus(hasil.getString("status"));
        return karyawan;
    }
}
