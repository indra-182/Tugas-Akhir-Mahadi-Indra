package com.mahadi.indivaragroup.model;

public class HasilRanking {
    private int idKaryawan;
    private String kodeKaryawan;
    private String namaKaryawan;
    private String divisi;
    private double nilaiTopsis;
    private int peringkat;

    public int getIdKaryawan() {
        return idKaryawan;
    }

    public void setIdKaryawan(int idKaryawan) {
        this.idKaryawan = idKaryawan;
    }

    public String getKodeKaryawan() {
        return kodeKaryawan;
    }

    public void setKodeKaryawan(String kodeKaryawan) {
        this.kodeKaryawan = kodeKaryawan;
    }

    public String getNamaKaryawan() {
        return namaKaryawan;
    }

    public void setNamaKaryawan(String namaKaryawan) {
        this.namaKaryawan = namaKaryawan;
    }

    public String getDivisi() {
        return divisi;
    }

    public void setDivisi(String divisi) {
        this.divisi = divisi;
    }

    public double getNilaiTopsis() {
        return nilaiTopsis;
    }

    public void setNilaiTopsis(double nilaiTopsis) {
        this.nilaiTopsis = nilaiTopsis;
    }

    public int getPeringkat() {
        return peringkat;
    }

    public void setPeringkat(int peringkat) {
        this.peringkat = peringkat;
    }
}
