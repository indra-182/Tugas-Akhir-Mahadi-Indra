package com.mahadi.indivaragroup.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
        this.karyawan = salinKaryawan(karyawan);
        this.kriteria = salinKriteria(kriteria);
        this.penilaian = salinPenilaian(penilaian);
    }

    public int getTahun() { return tahun; }
    public List<Karyawan> getKaryawan() { return salinKaryawan(karyawan); }
    public List<Kriteria> getKriteria() { return salinKriteria(kriteria); }
    public Map<Integer, Map<Integer, Double>> getPenilaian() { return salinPenilaian(penilaian); }

    private static List<Karyawan> salinKaryawan(List<Karyawan> sumber) {
        List<Karyawan> hasil = new ArrayList<Karyawan>();
        for (Karyawan item : sumber) {
            Karyawan salinan = new Karyawan();
            salinan.setId(item.getId());
            salinan.setKodeKaryawan(item.getKodeKaryawan());
            salinan.setNama(item.getNama());
            salinan.setDivisi(item.getDivisi());
            salinan.setJabatan(item.getJabatan());
            salinan.setTanggalMasuk(item.getTanggalMasuk());
            salinan.setStatus(item.getStatus());
            hasil.add(salinan);
        }
        return Collections.unmodifiableList(hasil);
    }

    private static List<Kriteria> salinKriteria(List<Kriteria> sumber) {
        List<Kriteria> hasil = new ArrayList<Kriteria>();
        for (Kriteria item : sumber) {
            Kriteria salinan = new Kriteria();
            salinan.setId(item.getId());
            salinan.setKode(item.getKode());
            salinan.setNama(item.getNama());
            salinan.setBobot(item.getBobot());
            salinan.setTipe(item.getTipe());
            salinan.setKeterangan(item.getKeterangan());
            hasil.add(salinan);
        }
        return Collections.unmodifiableList(hasil);
    }

    private static Map<Integer, Map<Integer, Double>> salinPenilaian(
            Map<Integer, Map<Integer, Double>> sumber) {
        Map<Integer, Map<Integer, Double>> hasil = new HashMap<Integer, Map<Integer, Double>>();
        for (Map.Entry<Integer, Map<Integer, Double>> entri : sumber.entrySet()) {
            hasil.put(entri.getKey(), Collections.unmodifiableMap(
                    new HashMap<Integer, Double>(entri.getValue())));
        }
        return Collections.unmodifiableMap(hasil);
    }
}
