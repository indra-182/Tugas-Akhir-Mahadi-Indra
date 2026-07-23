package com.mahadi.indivaragroup.model;

import java.util.List;
import java.util.Map;

public class PerhitunganSnapshot {
    private final int tahun;
    private final List<Karyawan> karyawan;
    private final List<Kriteria> kriteria;
    private final Map<Integer, Map<Integer, Double>> penilaian;

    public PerhitunganSnapshot(int tahun, List<Karyawan> karyawan, List<Kriteria> kriteria,
            Map<Integer, Map<Integer, Double>> penilaian) {
        this.tahun = tahun;
        this.karyawan = karyawan;
        this.kriteria = kriteria;
        this.penilaian = penilaian;
    }

    public int getTahun() { return tahun; }
    public List<Karyawan> getKaryawan() { return karyawan; }
    public List<Kriteria> getKriteria() { return kriteria; }
    public Map<Integer, Map<Integer, Double>> getPenilaian() { return penilaian; }
}
