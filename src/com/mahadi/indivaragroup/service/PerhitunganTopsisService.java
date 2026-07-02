package com.mahadi.indivaragroup.service;

import com.mahadi.indivaragroup.dao.HasilRankingDao;
import com.mahadi.indivaragroup.dao.KaryawanDao;
import com.mahadi.indivaragroup.dao.KriteriaDao;
import com.mahadi.indivaragroup.dao.PenilaianDao;
import com.mahadi.indivaragroup.model.HasilRanking;
import com.mahadi.indivaragroup.model.Karyawan;
import com.mahadi.indivaragroup.model.Kriteria;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PerhitunganTopsisService {
    private static boolean perhitunganSudahDiproses = false;

    private final KaryawanDao karyawanDao = new KaryawanDao();
    private final KriteriaDao kriteriaDao = new KriteriaDao();
    private final PenilaianDao penilaianDao = new PenilaianDao();
    private final HasilRankingDao hasilRankingDao = new HasilRankingDao();

    public static boolean apakahPerhitunganSudahDiproses() {
        return perhitunganSudahDiproses;
    }

    public List<HasilRanking> hitungDanSimpan() throws SQLException {
        return hitungDetailDanSimpan().getDaftarHasilRanking();
    }

    public PerhitunganDetail hitungDetailDanSimpan() throws SQLException {
        PerhitunganDetail detail = hitungDetail();
        hasilRankingDao.gantiSemua(detail.getDaftarHasilRanking());
        perhitunganSudahDiproses = true;
        return detail;
    }

    public List<HasilRanking> hitung() throws SQLException {
        return hitungDetail().getDaftarHasilRanking();
    }

    public PerhitunganDetail hitungDetail() throws SQLException {
        List<Karyawan> daftarKaryawan = karyawanDao.ambilAktif();
        List<Kriteria> daftarKriteria = kriteriaDao.ambilSemua();
        Map<Integer, Map<Integer, Double>> matriksPenilaian = penilaianDao.ambilSemuaSebagaiMatriks();

        validasiInput(daftarKaryawan, daftarKriteria, matriksPenilaian);

        double[][] matriksKeputusan = buatMatriksKeputusan(daftarKaryawan, daftarKriteria, matriksPenilaian);
        double[] pembagiNormalisasi = hitungPembagiNormalisasi(matriksKeputusan);
        double[][] matriksNormalisasi = hitungMatriksNormalisasi(matriksKeputusan, pembagiNormalisasi);
        double[] bobotKriteria = ambilBobotKriteria(daftarKriteria);
        double[][] matriksTerbobot = hitungMatriksTerbobot(matriksNormalisasi, bobotKriteria);
        double[] solusiIdealPositif = hitungSolusiIdeal(matriksTerbobot, daftarKriteria, true);
        double[] solusiIdealNegatif = hitungSolusiIdeal(matriksTerbobot, daftarKriteria, false);
        double[] jarakPositif = hitungJarak(matriksTerbobot, solusiIdealPositif);
        double[] jarakNegatif = hitungJarak(matriksTerbobot, solusiIdealNegatif);
        double[] nilaiPreferensi = hitungNilaiPreferensi(jarakPositif, jarakNegatif);
        List<HasilRanking> daftarHasilRanking = buatHasilRanking(daftarKaryawan, nilaiPreferensi);

        return new PerhitunganDetail(daftarKaryawan, daftarKriteria, matriksKeputusan,
                pembagiNormalisasi, matriksNormalisasi, bobotKriteria, matriksTerbobot,
                solusiIdealPositif, solusiIdealNegatif, jarakPositif, jarakNegatif,
                nilaiPreferensi, daftarHasilRanking);
    }

    private double[][] buatMatriksKeputusan(List<Karyawan> daftarKaryawan, List<Kriteria> daftarKriteria,
            Map<Integer, Map<Integer, Double>> matriksPenilaian) {
        double[][] matriks = new double[daftarKaryawan.size()][daftarKriteria.size()];
        for (int i = 0; i < daftarKaryawan.size(); i++) {
            Karyawan karyawan = daftarKaryawan.get(i);
            Map<Integer, Double> nilaiKaryawan = matriksPenilaian.get(karyawan.getId());
            for (int j = 0; j < daftarKriteria.size(); j++) {
                Kriteria kriteria = daftarKriteria.get(j);
                matriks[i][j] = nilaiKaryawan.get(kriteria.getId());
            }
        }
        return matriks;
    }

    private double[] hitungPembagiNormalisasi(double[][] matriksKeputusan) {
        int jumlahKriteria = matriksKeputusan[0].length;
        double[] pembagi = new double[jumlahKriteria];
        for (int j = 0; j < jumlahKriteria; j++) {
            double totalKuadrat = 0.0;
            for (int i = 0; i < matriksKeputusan.length; i++) {
                totalKuadrat += Math.pow(matriksKeputusan[i][j], 2);
            }
            pembagi[j] = Math.sqrt(totalKuadrat);
        }
        return pembagi;
    }

    private double[][] hitungMatriksNormalisasi(double[][] matriksKeputusan, double[] pembagiNormalisasi) {
        double[][] normalisasi = new double[matriksKeputusan.length][matriksKeputusan[0].length];
        for (int i = 0; i < matriksKeputusan.length; i++) {
            for (int j = 0; j < matriksKeputusan[i].length; j++) {
                normalisasi[i][j] = pembagiNormalisasi[j] == 0 ? 0 : matriksKeputusan[i][j] / pembagiNormalisasi[j];
            }
        }
        return normalisasi;
    }

    private double[] ambilBobotKriteria(List<Kriteria> daftarKriteria) {
        double[] bobot = new double[daftarKriteria.size()];
        for (int i = 0; i < daftarKriteria.size(); i++) {
            bobot[i] = daftarKriteria.get(i).getBobot();
        }
        return bobot;
    }

    private double[][] hitungMatriksTerbobot(double[][] matriksNormalisasi, double[] bobotKriteria) {
        double[][] terbobot = new double[matriksNormalisasi.length][matriksNormalisasi[0].length];
        for (int i = 0; i < matriksNormalisasi.length; i++) {
            for (int j = 0; j < matriksNormalisasi[i].length; j++) {
                terbobot[i][j] = matriksNormalisasi[i][j] * bobotKriteria[j];
            }
        }
        return terbobot;
    }

    private double[] hitungSolusiIdeal(double[][] matriksTerbobot, List<Kriteria> daftarKriteria, boolean positif) {
        double[] solusi = new double[daftarKriteria.size()];
        for (int j = 0; j < daftarKriteria.size(); j++) {
            double nilaiTerpilih = matriksTerbobot[0][j];
            for (int i = 1; i < matriksTerbobot.length; i++) {
                if (Kriteria.COST.equals(daftarKriteria.get(j).getTipe())) {
                    nilaiTerpilih = positif
                            ? Math.min(nilaiTerpilih, matriksTerbobot[i][j])
                            : Math.max(nilaiTerpilih, matriksTerbobot[i][j]);
                } else {
                    nilaiTerpilih = positif
                            ? Math.max(nilaiTerpilih, matriksTerbobot[i][j])
                            : Math.min(nilaiTerpilih, matriksTerbobot[i][j]);
                }
            }
            solusi[j] = nilaiTerpilih;
        }
        return solusi;
    }

    private double[] hitungJarak(double[][] matriksTerbobot, double[] solusiIdeal) {
        double[] jarak = new double[matriksTerbobot.length];
        for (int i = 0; i < matriksTerbobot.length; i++) {
            double total = 0.0;
            for (int j = 0; j < solusiIdeal.length; j++) {
                total += Math.pow(matriksTerbobot[i][j] - solusiIdeal[j], 2);
            }
            jarak[i] = Math.sqrt(total);
        }
        return jarak;
    }

    private double[] hitungNilaiPreferensi(double[] jarakPositif, double[] jarakNegatif) {
        double[] preferensi = new double[jarakPositif.length];
        for (int i = 0; i < preferensi.length; i++) {
            double pembagi = jarakPositif[i] + jarakNegatif[i];
            preferensi[i] = pembagi == 0 ? 0 : jarakNegatif[i] / pembagi;
        }
        return preferensi;
    }

    private List<HasilRanking> buatHasilRanking(List<Karyawan> daftarKaryawan, double[] nilaiPreferensi) {
        List<HasilRanking> daftarHasilRanking = new ArrayList<HasilRanking>();
        for (int i = 0; i < daftarKaryawan.size(); i++) {
            Karyawan karyawan = daftarKaryawan.get(i);
            HasilRanking hasilRanking = new HasilRanking();
            hasilRanking.setIdKaryawan(karyawan.getId());
            hasilRanking.setKodeKaryawan(karyawan.getKodeKaryawan());
            hasilRanking.setNamaKaryawan(karyawan.getNama());
            hasilRanking.setDivisi(karyawan.getDivisi());
            hasilRanking.setNilaiTopsis(nilaiPreferensi[i]);
            daftarHasilRanking.add(hasilRanking);
        }

        Collections.sort(daftarHasilRanking, (HasilRanking pertama, HasilRanking kedua) -> {
            int banding = Double.compare(kedua.getNilaiTopsis(), pertama.getNilaiTopsis());
            if (banding != 0) {
                return banding;
            }
            return pertama.getNamaKaryawan().compareToIgnoreCase(kedua.getNamaKaryawan());
        });

        for (int i = 0; i < daftarHasilRanking.size(); i++) {
            daftarHasilRanking.get(i).setPeringkat(i + 1);
        }
        return daftarHasilRanking;
    }

    private void validasiInput(List<Karyawan> daftarKaryawan, List<Kriteria> daftarKriteria,
            Map<Integer, Map<Integer, Double>> matriksPenilaian) {
        if (daftarKaryawan.isEmpty()) {
            throw new IllegalArgumentException("Data karyawan aktif belum tersedia.");
        }
        if (daftarKriteria.isEmpty()) {
            throw new IllegalArgumentException("Data kriteria belum tersedia.");
        }

        for (Kriteria kriteria : daftarKriteria) {
            if (kriteria.getBobot() <= 0) {
                throw new IllegalArgumentException("Bobot kriteria " + kriteria.getKode() + " harus lebih dari 0.");
            }
            if (!Kriteria.BENEFIT.equals(kriteria.getTipe()) && !Kriteria.COST.equals(kriteria.getTipe())) {
                throw new IllegalArgumentException("Tipe kriteria " + kriteria.getKode() + " harus BENEFIT atau COST.");
            }
        }

        for (Karyawan karyawan : daftarKaryawan) {
            if (!matriksPenilaian.containsKey(karyawan.getId())) {
                throw new IllegalArgumentException("Nilai penilaian untuk " + karyawan.getNama() + " belum lengkap.");
            }
            Map<Integer, Double> nilaiKaryawan = matriksPenilaian.get(karyawan.getId());
            for (Kriteria kriteria : daftarKriteria) {
                if (!nilaiKaryawan.containsKey(kriteria.getId())) {
                    throw new IllegalArgumentException("Nilai " + kriteria.getKode()
                            + " untuk " + karyawan.getNama() + " belum diisi.");
                }
            }
        }
    }

    public static class PerhitunganDetail {
        private final List<Karyawan> daftarKaryawan;
        private final List<Kriteria> daftarKriteria;
        private final double[][] matriksKeputusan;
        private final double[] pembagiNormalisasi;
        private final double[][] matriksNormalisasi;
        private final double[] bobotKriteria;
        private final double[][] matriksTerbobot;
        private final double[] solusiIdealPositif;
        private final double[] solusiIdealNegatif;
        private final double[] jarakPositif;
        private final double[] jarakNegatif;
        private final double[] nilaiPreferensi;
        private final List<HasilRanking> daftarHasilRanking;

        private PerhitunganDetail(List<Karyawan> daftarKaryawan, List<Kriteria> daftarKriteria,
                double[][] matriksKeputusan, double[] pembagiNormalisasi,
                double[][] matriksNormalisasi, double[] bobotKriteria,
                double[][] matriksTerbobot, double[] solusiIdealPositif,
                double[] solusiIdealNegatif, double[] jarakPositif,
                double[] jarakNegatif, double[] nilaiPreferensi,
                List<HasilRanking> daftarHasilRanking) {
            this.daftarKaryawan = daftarKaryawan;
            this.daftarKriteria = daftarKriteria;
            this.matriksKeputusan = matriksKeputusan;
            this.pembagiNormalisasi = pembagiNormalisasi;
            this.matriksNormalisasi = matriksNormalisasi;
            this.bobotKriteria = bobotKriteria;
            this.matriksTerbobot = matriksTerbobot;
            this.solusiIdealPositif = solusiIdealPositif;
            this.solusiIdealNegatif = solusiIdealNegatif;
            this.jarakPositif = jarakPositif;
            this.jarakNegatif = jarakNegatif;
            this.nilaiPreferensi = nilaiPreferensi;
            this.daftarHasilRanking = daftarHasilRanking;
        }

        public List<Karyawan> getDaftarKaryawan() {
            return daftarKaryawan;
        }

        public List<Kriteria> getDaftarKriteria() {
            return daftarKriteria;
        }

        public double[][] getMatriksKeputusan() {
            return matriksKeputusan;
        }

        public double[] getPembagiNormalisasi() {
            return pembagiNormalisasi;
        }

        public double[][] getMatriksNormalisasi() {
            return matriksNormalisasi;
        }

        public double[] getBobotKriteria() {
            return bobotKriteria;
        }

        public double[][] getMatriksTerbobot() {
            return matriksTerbobot;
        }

        public double[] getSolusiIdealPositif() {
            return solusiIdealPositif;
        }

        public double[] getSolusiIdealNegatif() {
            return solusiIdealNegatif;
        }

        public double[] getJarakPositif() {
            return jarakPositif;
        }

        public double[] getJarakNegatif() {
            return jarakNegatif;
        }

        public double[] getNilaiPreferensi() {
            return nilaiPreferensi;
        }

        public List<HasilRanking> getDaftarHasilRanking() {
            return daftarHasilRanking;
        }
    }
}
