package com.mahadi.indivaragroup.service;

import com.mahadi.indivaragroup.model.Kriteria;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Rules for the agreed annual employee-evaluation configuration. */
public final class ValidasiEvaluasiTahunan {
    private static final int JUMLAH_KRITERIA = 6;
    private static final Map<String, String> TIPE_KRITERIA_BAKU = tipeKriteriaBaku();

    private ValidasiEvaluasiTahunan() {
    }

    public static void validasiKriteria(List<Kriteria> daftarKriteria) {
        if (daftarKriteria == null || daftarKriteria.size() != JUMLAH_KRITERIA) {
            throw new IllegalArgumentException("Evaluasi tahunan harus menggunakan tepat enam kriteria.");
        }

        Map<String, String> tipeDitemukan = new HashMap<String, String>();
        BigDecimal totalBobot = BigDecimal.ZERO;
        for (Kriteria kriteria : daftarKriteria) {
            if (kriteria == null || kriteria.getKode() == null || kriteria.getKode().trim().isEmpty()) {
                throw new IllegalArgumentException("Setiap kriteria harus memiliki kode.");
            }
            if (tipeDitemukan.put(kriteria.getKode(), kriteria.getTipe()) != null) {
                throw new IllegalArgumentException("Kode kriteria tidak boleh duplikat.");
            }
            if (!Double.isFinite(kriteria.getBobot()) || kriteria.getBobot() <= 0) {
                throw new IllegalArgumentException("Bobot setiap kriteria harus lebih dari 0.");
            }
            totalBobot = totalBobot.add(BigDecimal.valueOf(kriteria.getBobot()));
        }

        if (!TIPE_KRITERIA_BAKU.equals(tipeDitemukan)) {
            throw new IllegalArgumentException("Enam kriteria harus memakai kode dan tipe evaluasi tahunan yang baku.");
        }
        if (totalBobot.compareTo(BigDecimal.ONE) != 0) {
            throw new IllegalArgumentException("Total bobot enam kriteria harus tepat 1.00.");
        }
    }

    public static void validasiNilai(Kriteria kriteria, double nilai) {
        if (kriteria == null) {
            throw new IllegalArgumentException("Kriteria penilaian wajib tersedia.");
        }
        if (!Double.isFinite(nilai) || nilai < 0) {
            throw new IllegalArgumentException("Nilai tidak boleh kurang dari 0.");
        }
        if (Kriteria.BENEFIT.equals(kriteria.getTipe()) && nilai > 100) {
            throw new IllegalArgumentException("Nilai kriteria benefit tidak boleh lebih dari 100.");
        }
    }

    private static Map<String, String> tipeKriteriaBaku() {
        Map<String, String> tipe = new HashMap<String, String>();
        tipe.put("C1", Kriteria.BENEFIT);
        tipe.put("C2", Kriteria.BENEFIT);
        tipe.put("C3", Kriteria.BENEFIT);
        tipe.put("C4", Kriteria.BENEFIT);
        tipe.put("C5", Kriteria.COST);
        tipe.put("C6", Kriteria.BENEFIT);
        return tipe;
    }
}
