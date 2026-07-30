package com.mahadi.indivaragroup.service;

import com.mahadi.indivaragroup.model.HasilRanking;
import com.mahadi.indivaragroup.model.Karyawan;
import com.mahadi.indivaragroup.model.Kriteria;
import com.mahadi.indivaragroup.service.PerhitunganTopsisService.PerhitunganDetail;
import com.mahadi.indivaragroup.util.NumberUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Dokumen data untuk laporan evaluasi tahunan yang dapat ditelusuri. */
public final class LaporanPerhitunganTopsis {
    private final int tahun;
    private final List<Bagian> daftarBagian;
    private final String rekomendasi;

    private LaporanPerhitunganTopsis(int tahun, List<Bagian> daftarBagian, String rekomendasi) {
        this.tahun = tahun;
        this.daftarBagian = Collections.unmodifiableList(new ArrayList<Bagian>(daftarBagian));
        this.rekomendasi = rekomendasi;
    }

    public static LaporanPerhitunganTopsis buat(int tahun, PerhitunganDetail detail) {
        if (detail == null) {
            throw new IllegalArgumentException("Detail perhitungan wajib tersedia.");
        }

        List<Bagian> bagian = new ArrayList<Bagian>();
        bagian.add(buatKriteriaDanBobot(detail));
        bagian.add(buatMatriksKaryawan("Matriks Keputusan", detail, detail.getMatriksKeputusan()));
        bagian.add(buatMatriksKaryawan("Matriks Normalisasi", detail, detail.getMatriksNormalisasi()));
        bagian.add(buatMatriksKaryawan("Matriks Normalisasi Terbobot", detail, detail.getMatriksTerbobot()));
        bagian.add(buatSolusiIdeal(detail));
        bagian.add(buatJarakDanPreferensi(detail));
        bagian.add(buatHasilRanking(detail.getDaftarHasilRanking()));
        String rekomendasi = buatRekomendasi(detail.getDaftarHasilRanking());
        bagian.add(new Bagian("Rekomendasi", new String[]{"Rekomendasi"},
                Collections.singletonList(new String[]{rekomendasi})));
        return new LaporanPerhitunganTopsis(tahun, bagian, rekomendasi);
    }

    public int getTahun() {
        return tahun;
    }

    public List<Bagian> getDaftarBagian() {
        return daftarBagian;
    }

    public List<String> getJudulBagian() {
        List<String> judul = new ArrayList<String>();
        for (Bagian bagian : daftarBagian) {
            judul.add(bagian.getJudul());
        }
        return judul;
    }

    public Bagian getBagian(String judul) {
        for (Bagian bagian : daftarBagian) {
            if (bagian.getJudul().equals(judul)) {
                return bagian;
            }
        }
        throw new IllegalArgumentException("Bagian laporan tidak ditemukan: " + judul);
    }

    public String getRekomendasi() {
        return rekomendasi;
    }

    private static Bagian buatKriteriaDanBobot(PerhitunganDetail detail) {
        List<String[]> baris = new ArrayList<String[]>();
        for (Kriteria kriteria : detail.getDaftarKriteria()) {
            baris.add(new String[]{kriteria.getKode(), kriteria.getNama(), kriteria.getTipe(),
                NumberUtil.format(kriteria.getBobot())});
        }
        return new Bagian("Kriteria dan Bobot", new String[]{"Kode", "Kriteria", "Tipe", "Bobot"}, baris);
    }

    private static Bagian buatMatriksKaryawan(String judul, PerhitunganDetail detail, double[][] matriks) {
        List<String> kolom = new ArrayList<String>();
        kolom.add("Kode Karyawan");
        kolom.add("Nama Karyawan");
        for (Kriteria kriteria : detail.getDaftarKriteria()) {
            kolom.add(kriteria.getKode());
        }

        List<String[]> baris = new ArrayList<String[]>();
        for (int i = 0; i < detail.getDaftarKaryawan().size(); i++) {
            Karyawan karyawan = detail.getDaftarKaryawan().get(i);
            String[] nilai = new String[kolom.size()];
            nilai[0] = karyawan.getKodeKaryawan();
            nilai[1] = karyawan.getNama();
            for (int j = 0; j < detail.getDaftarKriteria().size(); j++) {
                nilai[j + 2] = NumberUtil.format(matriks[i][j]);
            }
            baris.add(nilai);
        }
        return new Bagian(judul, kolom.toArray(new String[kolom.size()]), baris);
    }

    private static Bagian buatSolusiIdeal(PerhitunganDetail detail) {
        List<String[]> baris = new ArrayList<String[]>();
        for (int i = 0; i < detail.getDaftarKriteria().size(); i++) {
            Kriteria kriteria = detail.getDaftarKriteria().get(i);
            baris.add(new String[]{kriteria.getKode(), kriteria.getNama(), kriteria.getTipe(),
                NumberUtil.format(detail.getSolusiIdealPositif()[i]),
                NumberUtil.format(detail.getSolusiIdealNegatif()[i])});
        }
        return new Bagian("Solusi Ideal", new String[]{"Kode", "Kriteria", "Tipe", "A+", "A-"}, baris);
    }

    private static Bagian buatJarakDanPreferensi(PerhitunganDetail detail) {
        List<String[]> baris = new ArrayList<String[]>();
        for (int i = 0; i < detail.getDaftarKaryawan().size(); i++) {
            Karyawan karyawan = detail.getDaftarKaryawan().get(i);
            baris.add(new String[]{karyawan.getKodeKaryawan(), karyawan.getNama(),
                NumberUtil.format(detail.getJarakPositif()[i]), NumberUtil.format(detail.getJarakNegatif()[i]),
                NumberUtil.format(detail.getNilaiPreferensi()[i])});
        }
        return new Bagian("Jarak dan Nilai Preferensi", new String[]{
            "Kode Karyawan", "Nama Karyawan", "D+", "D-", "Nilai Preferensi"}, baris);
    }

    private static Bagian buatHasilRanking(List<HasilRanking> daftarRanking) {
        List<String[]> baris = new ArrayList<String[]>();
        for (HasilRanking ranking : daftarRanking) {
            baris.add(new String[]{String.valueOf(ranking.getPeringkat()), ranking.getKodeKaryawan(),
                ranking.getNamaKaryawan(), NumberUtil.format(ranking.getNilaiTopsis())});
        }
        return new Bagian("Hasil Ranking TOPSIS", new String[]{
            "Peringkat", "Kode Karyawan", "Nama Karyawan", "Nilai TOPSIS"}, baris);
    }

    private static String buatRekomendasi(List<HasilRanking> daftarRanking) {
        if (daftarRanking.isEmpty()) {
            return "Belum ada rekomendasi karyawan terbaik.";
        }
        List<String> namaKaryawan = new ArrayList<String>();
        for (HasilRanking ranking : PeringkatTopsis.ambilPeringkatTerbaik(daftarRanking)) {
            namaKaryawan.add(ranking.getNamaKaryawan());
        }
        String awalan = namaKaryawan.size() == 1
                ? "Rekomendasi karyawan terbaik: " : "Rekomendasi karyawan terbaik bersama: ";
        return awalan + gabungkanNama(namaKaryawan);
    }

    private static String gabungkanNama(List<String> namaKaryawan) {
        StringBuilder hasil = new StringBuilder();
        for (String nama : namaKaryawan) {
            if (hasil.length() > 0) {
                hasil.append(", ");
            }
            hasil.append(nama);
        }
        return hasil.toString();
    }

    public static final class Bagian {
        private final String judul;
        private final String[] kolom;
        private final List<String[]> baris;

        private Bagian(String judul, String[] kolom, List<String[]> baris) {
            this.judul = judul;
            this.kolom = kolom.clone();
            this.baris = new ArrayList<String[]>();
            for (String[] barisBagian : baris) {
                this.baris.add(barisBagian.clone());
            }
        }

        public String getJudul() {
            return judul;
        }

        public String[] getKolom() {
            return kolom.clone();
        }

        public List<String[]> getBaris() {
            List<String[]> salinan = new ArrayList<String[]>();
            for (String[] barisBagian : baris) {
                salinan.add(barisBagian.clone());
            }
            return salinan;
        }
    }
}
