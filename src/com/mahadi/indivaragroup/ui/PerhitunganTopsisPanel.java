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

            List<Kriteria> daftarKriteria = kriteriaDao.ambilSemua();
            Map<Integer, Map<Integer, Double>> matriksPenilaian = penilaianDao.ambilSemuaSebagaiMatriks(tahun);

            tabelPanel.removeAll();

            if (tahunBerjalan) {
                List<Karyawan> daftarKaryawan = karyawanDao.ambilAktif();
                tambahBagianTabel("Data Penilaian Awal", buatModelDataAwal(daftarKaryawan, daftarKriteria, matriksPenilaian));
                karyawanTerbaikLabel.setText("Karyawan terbaik: -");
            } else {
                List<HasilRanking> daftarHasilRanking = hasilRankingDao.ambilSemua(tahun);
                List<Karyawan> daftarKaryawanTerhitung = karyawanUntukHasilRanking(daftarHasilRanking);
                tambahBagianTabel("Data Penilaian Awal", buatModelDataAwal(daftarKaryawanTerhitung, daftarKriteria, matriksPenilaian));
                tambahBagianTabel("Hasil Ranking TOPSIS", buatModelHasilRanking(daftarHasilRanking));
                if (daftarHasilRanking.isEmpty()) {
                    karyawanTerbaikLabel.setText("Karyawan terbaik: -");
                } else {
                    HasilRanking terbaik = daftarHasilRanking.get(0);
                    karyawanTerbaikLabel.setText("Karyawan terbaik: " + terbaik.getNamaKaryawan()
                            + " dengan nilai TOPSIS " + NumberUtil.format(terbaik.getNilaiTopsis()));
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
        return kolom;
    }

    private void prosesPerhitungan() {
        try {
            PerhitunganDetail detail = topsisService.hitungDetailDanSimpan(tahunTerpilih());
            tampilkanDetailPerhitungan(detail);
            List<HasilRanking> daftarHasilRanking = detail.getDaftarHasilRanking();
            if (!daftarHasilRanking.isEmpty()) {
                HasilRanking terbaik = daftarHasilRanking.get(0);
                karyawanTerbaikLabel.setText("Karyawan terbaik: " + terbaik.getNamaKaryawan()
                        + " dengan nilai TOPSIS " + NumberUtil.format(terbaik.getNilaiTopsis()));
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

    private void tambahBagianTabel(String judul, DefaultTableModel model) {
        JPanel bagianPanel = new JPanel(new BorderLayout(0, 8));
        bagianPanel.setBackground(Color.WHITE);
        bagianPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        JLabel judulLabel = new JLabel(judul);
        JTable tabelBagian = new JTable(model);
        tabelBagian.setAutoCreateRowSorter(true);
        TampilanUtil.rapikanTabel(tabelBagian);
        JScrollPane scrollPane = new JScrollPane(tabelBagian);
        int tinggi = Math.min(190, 58 + (model.getRowCount() * 24));
        scrollPane.setPreferredSize(new Dimension(900, Math.max(110, tinggi)));

        bagianPanel.add(judulLabel, BorderLayout.NORTH);
        bagianPanel.add(scrollPane, BorderLayout.CENTER);
        tabelPanel.add(bagianPanel);
    }

    private DefaultTableModel buatModelDataAwal(List<Karyawan> daftarKaryawan, List<Kriteria> daftarKriteria,
            Map<Integer, Map<Integer, Double>> matriksPenilaian) {
        DefaultTableModel model = buatModelTidakBisaEdit(buatKolomDataAwal(daftarKriteria));
        for (Karyawan karyawan : daftarKaryawan) {
            Object[] baris = new Object[daftarKriteria.size() + 3];
            baris[0] = karyawan.getKodeKaryawan();
            baris[1] = karyawan.getNama();
            baris[2] = karyawan.getJabatan();
            Map<Integer, Double> nilaiKaryawan = matriksPenilaian.get(karyawan.getId());
            for (int i = 0; i < daftarKriteria.size(); i++) {
                Kriteria kriteria = daftarKriteria.get(i);
                Double nilai = nilaiKaryawan == null ? null : nilaiKaryawan.get(kriteria.getId());
                baris[i + 3] = nilai == null ? "-" : NumberUtil.format(nilai);
            }
            model.addRow(baris);
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
            model.addRow(baris);
        }
        return model;
    }

    private DefaultTableModel buatModelPembagiNormalisasi(PerhitunganDetail detail) {
        DefaultTableModel model = buatModelTidakBisaEdit(new Object[]{"Kode", "Kriteria", "Bobot", "Pembagi"});
        double[] pembagi = detail.getPembagiNormalisasi();
        for (int i = 0; i < detail.getDaftarKriteria().size(); i++) {
            Kriteria kriteria = detail.getDaftarKriteria().get(i);
            model.addRow(new Object[]{
                kriteria.getKode(),
                kriteria.getNama(),
                NumberUtil.format(kriteria.getBobot()),
                NumberUtil.format(pembagi[i])
            });
        }
        return model;
    }

    private DefaultTableModel buatModelSolusiIdeal(PerhitunganDetail detail) {
        DefaultTableModel model = buatModelTidakBisaEdit(new Object[]{"Kode", "Kriteria", "Tipe", "A+", "A-"});
        for (int i = 0; i < detail.getDaftarKriteria().size(); i++) {
            Kriteria kriteria = detail.getDaftarKriteria().get(i);
            model.addRow(new Object[]{
                kriteria.getKode(),
                kriteria.getNama(),
                kriteria.getTipe(),
                NumberUtil.format(detail.getSolusiIdealPositif()[i]),
                NumberUtil.format(detail.getSolusiIdealNegatif()[i])
            });
        }
        return model;
    }

    private DefaultTableModel buatModelJarakPreferensi(PerhitunganDetail detail) {
        DefaultTableModel model = buatModelTidakBisaEdit(new Object[]{
            "Kode Karyawan", "Nama Karyawan", "D+", "D-", "Nilai Preferensi"
        });
        for (int i = 0; i < detail.getDaftarKaryawan().size(); i++) {
            Karyawan karyawan = detail.getDaftarKaryawan().get(i);
            model.addRow(new Object[]{
                karyawan.getKodeKaryawan(),
                karyawan.getNama(),
                NumberUtil.format(detail.getJarakPositif()[i]),
                NumberUtil.format(detail.getJarakNegatif()[i]),
                NumberUtil.format(detail.getNilaiPreferensi()[i])
            });
        }
        return model;
    }

    private DefaultTableModel buatModelHasilRanking(List<HasilRanking> daftarHasilRanking) {
        DefaultTableModel model = buatModelTidakBisaEdit(new Object[]{
            "Peringkat", "Kode Karyawan", "Nama Karyawan", "Nilai TOPSIS"
        });
        for (HasilRanking hasilRanking : daftarHasilRanking) {
            model.addRow(new Object[]{
                hasilRanking.getPeringkat(),
                hasilRanking.getKodeKaryawan(),
                hasilRanking.getNamaKaryawan(),
                NumberUtil.format(hasilRanking.getNilaiTopsis())
            });
        }
        return model;
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
