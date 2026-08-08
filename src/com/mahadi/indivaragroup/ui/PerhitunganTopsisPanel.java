package com.mahadi.indivaragroup.ui;

import com.mahadi.indivaragroup.dao.HasilRankingDao;
import com.mahadi.indivaragroup.dao.KaryawanDao;
import com.mahadi.indivaragroup.dao.KriteriaDao;
import com.mahadi.indivaragroup.dao.PenilaianDao;
import com.mahadi.indivaragroup.model.HasilRanking;
import com.mahadi.indivaragroup.model.Karyawan;
import com.mahadi.indivaragroup.model.Kriteria;
import com.mahadi.indivaragroup.service.PerhitunganTopsisService;
import com.mahadi.indivaragroup.service.PerhitunganTopsisService.PerhitunganDetail;
import com.mahadi.indivaragroup.service.PeringkatTopsis;
import com.mahadi.indivaragroup.util.DialogUtil;
import com.mahadi.indivaragroup.util.NumberUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.SQLException;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class PerhitunganTopsisPanel extends JPanel {
    private static final int TINGGI_TABEL_MINIMUM = 190;
    private static final int BATAS_DATA_BANYAK = 10;
    private static final int JUMLAH_BARIS_DATA_BANYAK = 25;

    private final KaryawanDao karyawanDao = new KaryawanDao();
    private final KriteriaDao kriteriaDao = new KriteriaDao();
    private final PenilaianDao penilaianDao = new PenilaianDao();
    private final HasilRankingDao hasilRankingDao = new HasilRankingDao();
    private final PerhitunganTopsisService topsisService = new PerhitunganTopsisService();

    private final JPanel tabelPanel = new JPanel();
    private final JLabel karyawanTerbaikLabel = new JLabel("Karyawan terbaik: -");
    private JComboBox<Integer> tahunComboBox;
    private JButton prosesButton;

    public PerhitunganTopsisPanel() {
        setLayout(new BorderLayout(10, 24));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(32, 32, 28, 32));
        buatTampilan();
        pasangEventPanel();
        muatDataAwal();
    }

    private void buatTampilan() {
        add(TampilanUtil.buatJudul("PROSES PERHITUNGAN TOPSIS"), BorderLayout.NORTH);

        JPanel isiPanel = new JPanel(new BorderLayout(10, 28));
        isiPanel.setBackground(Color.WHITE);
        JPanel tombolPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        tombolPanel.setBackground(Color.WHITE);
        prosesButton = TampilanUtil.buatTombolAksi("Proses");
        prosesButton.addActionListener(e -> prosesPerhitungan());
        tahunComboBox = TampilanUtil.buatComboBoxTahun(daftarTahunAman());
        tahunComboBox.addActionListener(e -> muatDataAwal());
        tombolPanel.add(new JLabel("Tahun:"));
        tombolPanel.add(tahunComboBox);
        tombolPanel.add(prosesButton);

        tabelPanel.setLayout(new BoxLayout(tabelPanel, BoxLayout.Y_AXIS));
        tabelPanel.setBackground(Color.WHITE);
        isiPanel.add(tombolPanel, BorderLayout.NORTH);
        isiPanel.add(new JScrollPane(tabelPanel), BorderLayout.CENTER);
        isiPanel.add(karyawanTerbaikLabel, BorderLayout.SOUTH);
        add(isiPanel, BorderLayout.CENTER);
    }

    private void pasangEventPanel() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                TampilanUtil.segarkanComboBoxTahun(tahunComboBox, daftarTahunAman());
                muatDataAwal();
            }
        });
    }

    private List<Integer> daftarTahunAman() {
        try {
            return penilaianDao.ambilDaftarTahun();
        } catch (SQLException ex) {
            return Collections.emptyList();
        }
    }

    private int tahunTerpilih() {
        Integer tahun = (Integer) tahunComboBox.getSelectedItem();
        return tahun != null ? tahun : Year.now().getValue();
    }

    private void muatDataAwal() {
        try {
            int tahun = tahunTerpilih();
            boolean tahunBerjalan = tahun == Year.now().getValue();
            prosesButton.setEnabled(tahunBerjalan);

            tabelPanel.removeAll();

            if (tahunBerjalan) {
                List<Kriteria> daftarKriteria = kriteriaDao.ambilSemua();
                Map<Integer, Map<Integer, Double>> matriksPenilaian = penilaianDao.ambilSemuaSebagaiMatriks(tahun);
                List<Karyawan> daftarKaryawan = karyawanDao.ambilAktif();
                tambahBagianTabel("Data Penilaian Awal", buatModelDataAwal(daftarKaryawan, daftarKriteria, matriksPenilaian));
                karyawanTerbaikLabel.setText("Karyawan terbaik: -");
            } else {
                PerhitunganDetail detail = topsisService.ambilDetailHistoris(tahun);
                tampilkanDetailPerhitungan(detail);
                List<HasilRanking> daftarHasilRanking = detail.getDaftarHasilRanking();
                if (daftarHasilRanking.isEmpty()) {
                    karyawanTerbaikLabel.setText("Karyawan terbaik: -");
                } else {
                    karyawanTerbaikLabel.setText(formatKaryawanTerbaik(daftarHasilRanking));
                }
            }

            tabelPanel.revalidate();
            tabelPanel.repaint();
        } catch (SQLException ex) {
            DialogUtil.showError(this, ex.getMessage());
        }
    }

    /**
     * Untuk tahun lampau, "Data Penilaian Awal" harus menampilkan karyawan
     * yang sama persis dengan yang muncul di "Hasil Ranking TOPSIS" (hasil
     * perhitungan yang sudah dibekukan) - bukan daftar karyawan aktif saat
     * ini, yang bisa saja sudah berubah (mis. dinonaktifkan) sejak tahun itu
     * dihitung.
     */
    private List<Karyawan> karyawanUntukHasilRanking(List<HasilRanking> daftarHasilRanking) throws SQLException {
        Set<Integer> idTerhitung = new HashSet<>();
        for (HasilRanking hasilRanking : daftarHasilRanking) {
            idTerhitung.add(hasilRanking.getIdKaryawan());
        }
        List<Karyawan> hasil = new ArrayList<>();
        for (Karyawan karyawan : karyawanDao.ambilSemua()) {
            if (idTerhitung.contains(karyawan.getId())) {
                hasil.add(karyawan);
            }
        }
        return hasil;
    }

    private Object[] buatKolomDataAwal(List<Kriteria> daftarKriteria) {
        Object[] kolom = new Object[daftarKriteria.size() + 3];
        kolom[0] = "Kode Karyawan";
        kolom[1] = "Nama Karyawan";
        kolom[2] = "Jabatan";
        for (int i = 0; i < daftarKriteria.size(); i++) {
            kolom[i + 3] = daftarKriteria.get(i).getKode();
        }
        return tambahKolomNo(kolom);
    }

    private void prosesPerhitungan() {
        try {
            PerhitunganDetail detail = topsisService.hitungDetailDanSimpan(tahunTerpilih());
            tampilkanDetailPerhitungan(detail);
            List<HasilRanking> daftarHasilRanking = detail.getDaftarHasilRanking();
            if (!daftarHasilRanking.isEmpty()) {
                karyawanTerbaikLabel.setText(formatKaryawanTerbaik(daftarHasilRanking));
            }
            DialogUtil.showInfo(this, "Perhitungan TOPSIS berhasil dilakukan.");
        } catch (SQLException ex) {
            DialogUtil.showError(this, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            DialogUtil.showWarning(this, ex.getMessage());
        }
    }

    private void tampilkanDetailPerhitungan(PerhitunganDetail detail) {
        tabelPanel.removeAll();
        tambahBagianTabel("1. Data Penilaian Karyawan", buatModelMatriksKeputusan(detail));
        tambahBagianTabel("2. Pembagi Normalisasi", buatModelPembagiNormalisasi(detail));
        tambahBagianTabel("3. Matriks Normalisasi", buatModelMatriksNormalisasi(detail));
        tambahBagianTabel("4. Matriks Normalisasi Terbobot", buatModelMatriksTerbobot(detail));
        tambahBagianTabel("5. Solusi Ideal Positif dan Negatif", buatModelSolusiIdeal(detail));
        tambahBagianTabel("6. Jarak Solusi dan Nilai Preferensi", buatModelJarakPreferensi(detail));
        tambahBagianTabel("7. Hasil Ranking TOPSIS", buatModelHasilRanking(detail.getDaftarHasilRanking()));
        tabelPanel.revalidate();
        tabelPanel.repaint();
    }

    private String formatKaryawanTerbaik(List<HasilRanking> daftarHasilRanking) {
        HasilRanking terbaik = daftarHasilRanking.get(0);
        StringBuilder namaKaryawan = new StringBuilder();
        for (HasilRanking hasilRanking : PeringkatTopsis.ambilPeringkatTerbaik(daftarHasilRanking)) {
            if (namaKaryawan.length() > 0) {
                namaKaryawan.append(", ");
            }
            namaKaryawan.append(hasilRanking.getNamaKaryawan());
        }

        String label = namaKaryawan.indexOf(", ") >= 0
                ? "Karyawan terbaik bersama: " : "Karyawan terbaik: ";
        return label + namaKaryawan + " dengan nilai TOPSIS "
                + NumberUtil.format(terbaik.getNilaiTopsis());
    }

    private void tambahBagianTabel(String judul, DefaultTableModel model) {
        JPanel bagianPanel = new JPanel(new BorderLayout(0, 8));
        bagianPanel.setBackground(Color.WHITE);
        bagianPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        JLabel judulLabel = new JLabel(judul);
        JTable tabelBagian = new JTable(model);
        tabelBagian.setAutoCreateRowSorter(true);
        TampilanUtil.rapikanTabel(tabelBagian);
        JScrollPane scrollPane = new JScrollPane(tabelBagian);
        int tinggi = hitungTinggiTabel(model.getRowCount(), tabelBagian.getRowHeight(),
                tabelBagian.getTableHeader().getPreferredSize().height);
        scrollPane.setPreferredSize(new Dimension(900, Math.max(110, tinggi)));

        bagianPanel.add(judulLabel, BorderLayout.NORTH);
        bagianPanel.add(scrollPane, BorderLayout.CENTER);
        tabelPanel.add(bagianPanel);
    }

    private DefaultTableModel buatModelDataAwal(List<Karyawan> daftarKaryawan, List<Kriteria> daftarKriteria,
            Map<Integer, Map<Integer, Double>> matriksPenilaian) {
        DefaultTableModel model = buatModelTidakBisaEdit(buatKolomDataAwal(daftarKriteria));
        for (int i = 0; i < daftarKaryawan.size(); i++) {
            Karyawan karyawan = daftarKaryawan.get(i);
            Object[] baris = new Object[daftarKriteria.size() + 3];
            baris[0] = karyawan.getKodeKaryawan();
            baris[1] = karyawan.getNama();
            baris[2] = karyawan.getJabatan();
            Map<Integer, Double> nilaiKaryawan = matriksPenilaian.get(karyawan.getId());
            for (int j = 0; j < daftarKriteria.size(); j++) {
                Kriteria kriteria = daftarKriteria.get(j);
                Double nilai = nilaiKaryawan == null ? null : nilaiKaryawan.get(kriteria.getId());
                baris[j + 3] = nilai == null ? "-" : NumberUtil.format(nilai);
            }
            model.addRow(tambahNomor(baris, i + 1));
        }
        return model;
    }

    private DefaultTableModel buatModelMatriksKeputusan(PerhitunganDetail detail) {
        return buatModelMatriksKaryawan(detail, detail.getMatriksKeputusan());
    }

    private DefaultTableModel buatModelMatriksNormalisasi(PerhitunganDetail detail) {
        return buatModelMatriksKaryawan(detail, detail.getMatriksNormalisasi());
    }

    private DefaultTableModel buatModelMatriksTerbobot(PerhitunganDetail detail) {
        return buatModelMatriksKaryawan(detail, detail.getMatriksTerbobot());
    }

    private DefaultTableModel buatModelMatriksKaryawan(PerhitunganDetail detail, double[][] matriks) {
        DefaultTableModel model = buatModelTidakBisaEdit(buatKolomDataAwal(detail.getDaftarKriteria()));
        for (int i = 0; i < detail.getDaftarKaryawan().size(); i++) {
            Karyawan karyawan = detail.getDaftarKaryawan().get(i);
            Object[] baris = new Object[detail.getDaftarKriteria().size() + 3];
            baris[0] = karyawan.getKodeKaryawan();
            baris[1] = karyawan.getNama();
            baris[2] = karyawan.getJabatan();
            for (int j = 0; j < detail.getDaftarKriteria().size(); j++) {
                baris[j + 3] = NumberUtil.format(matriks[i][j]);
            }
            model.addRow(tambahNomor(baris, i + 1));
        }
        return model;
    }

    private DefaultTableModel buatModelPembagiNormalisasi(PerhitunganDetail detail) {
        DefaultTableModel model = buatModelTidakBisaEdit(tambahKolomNo(
                new Object[]{"Kode", "Kriteria", "Bobot", "Pembagi"}));
        double[] pembagi = detail.getPembagiNormalisasi();
        for (int i = 0; i < detail.getDaftarKriteria().size(); i++) {
            Kriteria kriteria = detail.getDaftarKriteria().get(i);
            model.addRow(tambahNomor(new Object[]{
                kriteria.getKode(),
                kriteria.getNama(),
                NumberUtil.format(kriteria.getBobot()),
                NumberUtil.format(pembagi[i])
            }, i + 1));
        }
        return model;
    }

    private DefaultTableModel buatModelSolusiIdeal(PerhitunganDetail detail) {
        DefaultTableModel model = buatModelTidakBisaEdit(tambahKolomNo(
                new Object[]{"Kode", "Kriteria", "Tipe", "A+", "A-"}));
        for (int i = 0; i < detail.getDaftarKriteria().size(); i++) {
            Kriteria kriteria = detail.getDaftarKriteria().get(i);
            model.addRow(tambahNomor(new Object[]{
                kriteria.getKode(),
                kriteria.getNama(),
                kriteria.getTipe(),
                NumberUtil.format(detail.getSolusiIdealPositif()[i]),
                NumberUtil.format(detail.getSolusiIdealNegatif()[i])
            }, i + 1));
        }
        return model;
    }

    private DefaultTableModel buatModelJarakPreferensi(PerhitunganDetail detail) {
        DefaultTableModel model = buatModelTidakBisaEdit(tambahKolomNo(new Object[]{
            "Kode Karyawan", "Nama Karyawan", "D+", "D-", "Nilai Preferensi"
        }));
        for (int i = 0; i < detail.getDaftarKaryawan().size(); i++) {
            Karyawan karyawan = detail.getDaftarKaryawan().get(i);
            model.addRow(tambahNomor(new Object[]{
                karyawan.getKodeKaryawan(),
                karyawan.getNama(),
                NumberUtil.format(detail.getJarakPositif()[i]),
                NumberUtil.format(detail.getJarakNegatif()[i]),
                NumberUtil.format(detail.getNilaiPreferensi()[i])
            }, i + 1));
        }
        return model;
    }

    private DefaultTableModel buatModelHasilRanking(List<HasilRanking> daftarHasilRanking) {
        DefaultTableModel model = buatModelTidakBisaEdit(tambahKolomNo(new Object[]{
            "Peringkat", "Kode Karyawan", "Nama Karyawan", "Nilai TOPSIS"
        }));
        for (int i = 0; i < daftarHasilRanking.size(); i++) {
            HasilRanking hasilRanking = daftarHasilRanking.get(i);
            model.addRow(tambahNomor(new Object[]{
                hasilRanking.getPeringkat(),
                hasilRanking.getKodeKaryawan(),
                hasilRanking.getNamaKaryawan(),
                NumberUtil.format(hasilRanking.getNilaiTopsis())
            }, i + 1));
        }
        return model;
    }

    static Object[] tambahKolomNo(Object[] kolom) {
        Object[] hasil = new Object[kolom.length + 1];
        hasil[0] = "No";
        System.arraycopy(kolom, 0, hasil, 1, kolom.length);
        return hasil;
    }

    static Object[] tambahNomor(Object[] nilaiBaris, int nomor) {
        Object[] hasil = new Object[nilaiBaris.length + 1];
        hasil[0] = nomor;
        System.arraycopy(nilaiBaris, 0, hasil, 1, nilaiBaris.length);
        return hasil;
    }

    static int hitungTinggiTabel(int jumlahBaris, int tinggiBaris, int tinggiHeader) {
        int barisTampil = jumlahBaris > BATAS_DATA_BANYAK
                ? JUMLAH_BARIS_DATA_BANYAK : Math.max(1, jumlahBaris);
        int tinggiIsi = tinggiHeader + (barisTampil * tinggiBaris) + 4;
        return Math.max(TINGGI_TABEL_MINIMUM, tinggiIsi);
    }

    private DefaultTableModel buatModelTidakBisaEdit(Object[] kolom) {
        return new DefaultTableModel(new Object[][]{}, kolom) {
            @Override
            public boolean isCellEditable(int baris, int kolomIndex) {
                return false;
            }
        };
    }
}
