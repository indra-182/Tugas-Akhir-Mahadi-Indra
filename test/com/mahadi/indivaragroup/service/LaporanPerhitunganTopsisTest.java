package com.mahadi.indivaragroup.service;

import com.mahadi.indivaragroup.model.HasilRanking;
import com.mahadi.indivaragroup.model.Karyawan;
import com.mahadi.indivaragroup.model.Kriteria;
import com.mahadi.indivaragroup.service.PerhitunganTopsisService.PerhitunganDetail;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class LaporanPerhitunganTopsisTest {
    @Test
    public void membentukJejakPerhitunganDanRekomendasiBersamaUntukTahunTerpilih() {
        LaporanPerhitunganTopsis laporan = LaporanPerhitunganTopsis.buat(2026, detailDenganPeringkatSeri());

        Assert.assertEquals(2026, laporan.getTahun());
        Assert.assertEquals(Arrays.asList(
                "Kriteria dan Bobot", "Matriks Keputusan", "Matriks Normalisasi",
                "Matriks Normalisasi Terbobot", "Solusi Ideal", "Jarak dan Nilai Preferensi",
                "Hasil Ranking TOPSIS", "Rekomendasi"), laporan.getJudulBagian());
        Assert.assertEquals("1", laporan.getBagian("Hasil Ranking TOPSIS").getBaris().get(0)[0]);
        Assert.assertEquals("1", laporan.getBagian("Hasil Ranking TOPSIS").getBaris().get(1)[0]);
        Assert.assertEquals("Rekomendasi karyawan terbaik bersama: Andi, Bima", laporan.getRekomendasi());
    }

    private PerhitunganDetail detailDenganPeringkatSeri() {
        List<Karyawan> karyawan = Arrays.asList(karyawan("K001", "Andi"), karyawan("K002", "Bima"));
        List<Kriteria> kriteria = Arrays.asList(kriteria("C1", "Kedisiplinan", 0.25, Kriteria.BENEFIT));
        List<HasilRanking> ranking = Arrays.asList(ranking("K001", "Andi", 1, 0.75), ranking("K002", "Bima", 1, 0.75));
        return new PerhitunganDetail(karyawan, kriteria,
                new double[][]{{80}, {80}}, new double[]{113.1371}, new double[][]{{0.7071}, {0.7071}},
                new double[]{0.25}, new double[][]{{0.1768}, {0.1768}}, new double[]{0.1768},
                new double[]{0.1768}, new double[]{0.0, 0.0}, new double[]{0.0, 0.0},
                new double[]{0.75, 0.75}, ranking);
    }

    private Karyawan karyawan(String kode, String nama) {
        Karyawan karyawan = new Karyawan();
        karyawan.setKodeKaryawan(kode);
        karyawan.setNama(nama);
        return karyawan;
    }

    private Kriteria kriteria(String kode, String nama, double bobot, String tipe) {
        Kriteria kriteria = new Kriteria();
        kriteria.setKode(kode);
        kriteria.setNama(nama);
        kriteria.setBobot(bobot);
        kriteria.setTipe(tipe);
        return kriteria;
    }

    private HasilRanking ranking(String kode, String nama, int peringkat, double nilaiTopsis) {
        HasilRanking hasil = new HasilRanking();
        hasil.setKodeKaryawan(kode);
        hasil.setNamaKaryawan(nama);
        hasil.setPeringkat(peringkat);
        hasil.setNilaiTopsis(nilaiTopsis);
        return hasil;
    }
}
