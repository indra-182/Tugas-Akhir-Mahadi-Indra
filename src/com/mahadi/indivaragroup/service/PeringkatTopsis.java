package com.mahadi.indivaragroup.service;

import com.mahadi.indivaragroup.model.HasilRanking;
import com.mahadi.indivaragroup.model.Karyawan;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Membentuk competition ranking dari nilai preferensi TOPSIS, misalnya
 * nilai seri menghasilkan peringkat 1, 1, 3.
 */
public final class PeringkatTopsis {
    public static final String KONVENSI_PERINGKAT
            = "Peringkat sama memakai competition ranking (1, 1, 3).";

    private PeringkatTopsis() {
    }

    public static List<HasilRanking> buat(List<Karyawan> daftarKaryawan, double[] nilaiPreferensi) {
        if (daftarKaryawan == null || nilaiPreferensi == null
                || daftarKaryawan.size() != nilaiPreferensi.length) {
            throw new IllegalArgumentException("Jumlah karyawan dan nilai preferensi harus sama.");
        }

        List<HasilRanking> hasil = new ArrayList<HasilRanking>();
        for (int i = 0; i < daftarKaryawan.size(); i++) {
            Karyawan karyawan = daftarKaryawan.get(i);
            HasilRanking ranking = new HasilRanking();
            ranking.setIdKaryawan(karyawan.getId());
            ranking.setKodeKaryawan(karyawan.getKodeKaryawan());
            ranking.setNamaKaryawan(karyawan.getNama());
            ranking.setDivisi(karyawan.getDivisi());
            ranking.setNilaiTopsis(nilaiPreferensi[i]);
            hasil.add(ranking);
        }

        Collections.sort(hasil, (pertama, kedua) -> {
            int banding = Double.compare(kedua.getNilaiTopsis(), pertama.getNilaiTopsis());
            return banding != 0 ? banding
                    : pertama.getNamaKaryawan().compareToIgnoreCase(kedua.getNamaKaryawan());
        });

        int peringkatSebelumnya = 0;
        double nilaiSebelumnya = Double.NaN;
        for (int i = 0; i < hasil.size(); i++) {
            HasilRanking ranking = hasil.get(i);
            if (i == 0 || Double.compare(ranking.getNilaiTopsis(), nilaiSebelumnya) != 0) {
                peringkatSebelumnya = i + 1;
                nilaiSebelumnya = ranking.getNilaiTopsis();
            }
            ranking.setPeringkat(peringkatSebelumnya);
        }
        return hasil;
    }

    public static List<HasilRanking> ambilPeringkatTerbaik(List<HasilRanking> daftarRanking) {
        if (daftarRanking.isEmpty()) {
            return Collections.emptyList();
        }
        int peringkatTerbaik = daftarRanking.get(0).getPeringkat();
        List<HasilRanking> hasil = new ArrayList<HasilRanking>();
        for (HasilRanking ranking : daftarRanking) {
            if (ranking.getPeringkat() == peringkatTerbaik) {
                hasil.add(ranking);
            }
        }
        return hasil;
    }
}
