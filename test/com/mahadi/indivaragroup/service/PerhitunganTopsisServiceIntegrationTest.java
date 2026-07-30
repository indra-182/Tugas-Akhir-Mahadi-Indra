package com.mahadi.indivaragroup.service;

import com.mahadi.indivaragroup.model.Karyawan;
import com.mahadi.indivaragroup.model.Kriteria;
import com.mahadi.indivaragroup.model.PerhitunganSnapshot;
import com.mahadi.indivaragroup.service.PerhitunganTopsisService.PerhitunganDetail;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class PerhitunganTopsisServiceIntegrationTest {
    private final PerhitunganTopsisService service = new PerhitunganTopsisService();

    @Test
    public void menghitungEnamKriteriaDenganAbsensiSebagaiCost() {
        PerhitunganDetail detail = service.hitungDetailDariData(karyawan(), kriteria(), nilai(1.0, 5.0));

        Assert.assertEquals(6, detail.getDaftarKriteria().size());
        Assert.assertEquals(1.0, totalBobot(detail.getDaftarKriteria()), 0.0);
        Assert.assertEquals("K001", detail.getDaftarHasilRanking().get(0).getKodeKaryawan());
        Assert.assertEquals(1.0, detail.getDaftarHasilRanking().get(0).getNilaiTopsis(), 0.000001);
        Assert.assertEquals(0.0, detail.getDaftarHasilRanking().get(1).getNilaiTopsis(), 0.000001);
    }

    @Test
    public void menghitungKembaliSnapshotHistorisDenganNilaiDanRankingYangSama() {
        Map<Integer, Map<Integer, Double>> nilaiHistoris = nilai(2.0, 2.0);
        PerhitunganSnapshot snapshot = new PerhitunganSnapshot(2025, karyawan(), kriteria(), nilaiHistoris);
        nilaiHistoris.get(1).put(5, 99.0);

        PerhitunganDetail detail = service.hitungDetailDariData(
                snapshot.getKaryawan(), snapshot.getKriteria(), snapshot.getPenilaian());

        Assert.assertEquals(1, detail.getDaftarHasilRanking().get(0).getPeringkat());
        Assert.assertEquals(1, detail.getDaftarHasilRanking().get(1).getPeringkat());
        Assert.assertEquals(detail.getDaftarHasilRanking().get(0).getNilaiTopsis(),
                detail.getDaftarHasilRanking().get(1).getNilaiTopsis(), 0.000001);
    }

    private List<Karyawan> karyawan() {
        return Arrays.asList(karyawan(1, "K001", "Andi"), karyawan(2, "K002", "Bima"));
    }

    private Karyawan karyawan(int id, String kode, String nama) {
        Karyawan karyawan = new Karyawan();
        karyawan.setId(id);
        karyawan.setKodeKaryawan(kode);
        karyawan.setNama(nama);
        return karyawan;
    }

    private List<Kriteria> kriteria() {
        return Arrays.asList(
                kriteria(1, "C1", 0.25, Kriteria.BENEFIT), kriteria(2, "C2", 0.25, Kriteria.BENEFIT),
                kriteria(3, "C3", 0.20, Kriteria.BENEFIT), kriteria(4, "C4", 0.15, Kriteria.BENEFIT),
                kriteria(5, "C5", 0.10, Kriteria.COST), kriteria(6, "C6", 0.05, Kriteria.BENEFIT));
    }

    private Kriteria kriteria(int id, String kode, double bobot, String tipe) {
        Kriteria kriteria = new Kriteria();
        kriteria.setId(id);
        kriteria.setKode(kode);
        kriteria.setNama(kode);
        kriteria.setBobot(bobot);
        kriteria.setTipe(tipe);
        return kriteria;
    }

    private Map<Integer, Map<Integer, Double>> nilai(double absensiAndi, double absensiBima) {
        Map<Integer, Map<Integer, Double>> hasil = new HashMap<Integer, Map<Integer, Double>>();
        hasil.put(1, nilaiKaryawan(absensiAndi));
        hasil.put(2, nilaiKaryawan(absensiBima));
        return hasil;
    }

    private Map<Integer, Double> nilaiKaryawan(double absensi) {
        Map<Integer, Double> hasil = new HashMap<Integer, Double>();
        for (int idKriteria = 1; idKriteria <= 6; idKriteria++) {
            hasil.put(idKriteria, idKriteria == 5 ? absensi : 80.0);
        }
        return hasil;
    }

    private double totalBobot(List<Kriteria> daftarKriteria) {
        double total = 0.0;
        for (Kriteria kriteria : daftarKriteria) {
            total += kriteria.getBobot();
        }
        return total;
    }
}
