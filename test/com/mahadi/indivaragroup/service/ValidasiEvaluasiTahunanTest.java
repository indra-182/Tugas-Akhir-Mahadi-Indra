package com.mahadi.indivaragroup.service;

import com.mahadi.indivaragroup.model.Kriteria;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class ValidasiEvaluasiTahunanTest {
    @Test
    public void menerimaEnamKriteriaBakuDenganBobotSatu() {
        ValidasiEvaluasiTahunan.validasiKriteria(kriteriaBaku());
    }

    @Test(expected = IllegalArgumentException.class)
    public void menolakTotalBobotSelainSatu() {
        List<Kriteria> kriteria = kriteriaBaku();
        kriteria.get(0).setBobot(0.30);
        ValidasiEvaluasiTahunan.validasiKriteria(kriteria);
    }

    @Test(expected = IllegalArgumentException.class)
    public void menolakJumlahKriteriaSelainEnam() {
        List<Kriteria> kriteria = kriteriaBaku();
        kriteria.remove(5);
        ValidasiEvaluasiTahunan.validasiKriteria(kriteria);
    }

    @Test(expected = IllegalArgumentException.class)
    public void menolakAbsensiYangBukanCost() {
        List<Kriteria> kriteria = kriteriaBaku();
        kriteria.get(4).setTipe(Kriteria.BENEFIT);
        ValidasiEvaluasiTahunan.validasiKriteria(kriteria);
    }

    @Test
    public void menerimaNilaiBenefitNolSampaiSeratusDanAbsensiNonNegatif() {
        ValidasiEvaluasiTahunan.validasiNilai(kriteriaBaku().get(0), 100.0);
        ValidasiEvaluasiTahunan.validasiNilai(kriteriaBaku().get(4), 120.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void menolakBenefitDiAtasSeratus() {
        ValidasiEvaluasiTahunan.validasiNilai(kriteriaBaku().get(0), 100.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void menolakAbsensiNegatif() {
        ValidasiEvaluasiTahunan.validasiNilai(kriteriaBaku().get(4), -1.0);
    }

    private List<Kriteria> kriteriaBaku() {
        return new ArrayList<Kriteria>(Arrays.asList(
                kriteria("C1", "Kedisiplinan", 0.25, Kriteria.BENEFIT),
                kriteria("C2", "Kualitas Kerja", 0.25, Kriteria.BENEFIT),
                kriteria("C3", "Tanggung Jawab", 0.20, Kriteria.BENEFIT),
                kriteria("C4", "Kerja Sama", 0.15, Kriteria.BENEFIT),
                kriteria("C5", "Absensi", 0.10, Kriteria.COST),
                kriteria("C6", "Masa Kerja", 0.05, Kriteria.BENEFIT)
        ));
    }

    private Kriteria kriteria(String kode, String nama, double bobot, String tipe) {
        Kriteria kriteria = new Kriteria();
        kriteria.setKode(kode);
        kriteria.setNama(nama);
        kriteria.setBobot(bobot);
        kriteria.setTipe(tipe);
        return kriteria;
    }
}
