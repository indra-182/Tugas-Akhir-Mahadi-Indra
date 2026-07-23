package com.mahadi.indivaragroup.ui;

import com.mahadi.indivaragroup.dao.HasilRankingDao;
import com.mahadi.indivaragroup.service.PerhitunganTopsisService;
import com.mahadi.indivaragroup.dao.KaryawanDao;
import com.mahadi.indivaragroup.model.Karyawan;
import com.mahadi.indivaragroup.util.DialogUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.util.regex.Pattern;

public class KaryawanPanel extends JPanel {
    private final KaryawanDao karyawanDao = new KaryawanDao();
    private final HasilRankingDao hasilRankingDao = new HasilRankingDao();
    private final PerhitunganTopsisService topsisService = new PerhitunganTopsisService();
    private final KaryawanTableModel tableModel = new KaryawanTableModel();
    private final JTable tabel = new JTable(tableModel);

    private final JTextField kodeField = new JTextField(18);
    private final JTextField namaField = new JTextField(18);
    private final JTextField jabatanField = new JTextField(18);
    private final JTextField pencarianField = new TeksPlaceholderField(
            "Cari berdasarkan kode karyawan", 18);
    private final JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"AKTIF", "NONAKTIF"});
    private final TableRowSorter<KaryawanTableModel> penyaringTabel = new TableRowSorter<>(tableModel);

    private int idTerpilih = 0;

    public KaryawanPanel() {
        setLayout(new BorderLayout(10, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(32, 32, 28, 32));
        buatTampilan();
        pasangEvent();
        muatData();
    }

    private void buatTampilan() {
        add(TampilanUtil.buatJudul("DATA KARYAWAN"), BorderLayout.NORTH);

        JPanel isiPanel = new JPanel(new BorderLayout(10, 26));
        isiPanel.setBackground(Color.WHITE);
        isiPanel.add(buatPanelInput(), BorderLayout.NORTH);

        TampilanUtil.rapikanTabel(tabel);
        tabel.setRowSorter(penyaringTabel);
        TampilanUtil.pasangKolomNomor(tabel);
        isiPanel.add(new JScrollPane(tabel), BorderLayout.CENTER);
        add(isiPanel, BorderLayout.CENTER);
    }

    private JPanel buatPanelInput() {
        JPanel panelInput = new JPanel(new GridBagLayout());
        panelInput.setBackground(Color.WHITE);
        GridBagConstraints panelBatas = new GridBagConstraints();
        panelBatas.insets = new Insets(0, 18, 0, 18);
        panelBatas.anchor = GridBagConstraints.NORTH;

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints batas = new GridBagConstraints();
        batas.insets = new Insets(5, 5, 5, 5);
        batas.fill = GridBagConstraints.HORIZONTAL;

        tambahField(formPanel, batas, 0, "Kode Karyawan", kodeField);
        tambahField(formPanel, batas, 1, "Nama", namaField);
        tambahField(formPanel, batas, 2, "Jabatan", jabatanField);
        tambahField(formPanel, batas, 3, "Status", statusComboBox);
        tambahField(formPanel, batas, 4, "Search Data", pencarianField);

        JPanel tombolPanel = new JPanel(new GridBagLayout());
        tombolPanel.setBackground(Color.WHITE);
        GridBagConstraints tombolBatas = new GridBagConstraints();
        tombolBatas.insets = new Insets(5, 5, 5, 5);
        JButton tambahButton = TampilanUtil.buatTombolAksi("Tambah");
        JButton simpanButton = TampilanUtil.buatTombolAksi("Simpan");
        JButton ubahButton = TampilanUtil.buatTombolAksi("Ubah");
        JButton hapusButton = TampilanUtil.buatTombolAksi("Hapus");

        tambahButton.addActionListener(e -> bersihkanForm());
        simpanButton.addActionListener(e -> simpan());
        ubahButton.addActionListener(e -> ubah());
        hapusButton.addActionListener(e -> hapus());

        tombolBatas.gridx = 0;
        tombolBatas.gridy = 0;
        tombolPanel.add(tambahButton, tombolBatas);
        tombolBatas.gridx = 1;
        tombolPanel.add(simpanButton, tombolBatas);
        tombolBatas.gridx = 2;
        tombolPanel.add(ubahButton, tombolBatas);
        tombolBatas.gridx = 0;
        tombolBatas.gridy = 1;
        tombolPanel.add(hapusButton, tombolBatas);

        panelBatas.gridx = 0;
        panelBatas.gridy = 0;
        panelInput.add(formPanel, panelBatas);
        panelBatas.gridx = 1;
        panelInput.add(tombolPanel, panelBatas);
        return panelInput;
    }

    private void tambahField(JPanel panel, GridBagConstraints batas, int baris, String label, java.awt.Component field) {
        batas.gridx = 0;
        batas.gridy = baris;
        batas.weightx = 0;
        panel.add(new JLabel(label), batas);
        batas.gridx = 1;
        batas.weightx = 1;
        panel.add(field, batas);
    }

    private void pasangEvent() {
        pencarianField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                saringData();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                saringData();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                saringData();
            }
        });

        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabel.getSelectedRow() >= 0) {
                int baris = tabel.convertRowIndexToModel(tabel.getSelectedRow());
                Karyawan karyawan = tableModel.ambilKaryawan(baris);
                idTerpilih = karyawan.getId();
                kodeField.setText(karyawan.getKodeKaryawan());
                namaField.setText(karyawan.getNama());
                jabatanField.setText(karyawan.getJabatan());
                statusComboBox.setSelectedItem(karyawan.getStatus());
            }
        });
    }

    private void saringData() {
        String kataKunci = pencarianField.getText().trim();
        if (kataKunci.isEmpty()) {
            penyaringTabel.setRowFilter(null);
        } else {
            final String pola = ".*" + Pattern.quote(kataKunci.toLowerCase()) + ".*";
            penyaringTabel.setRowFilter(new RowFilter<KaryawanTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends KaryawanTableModel, ? extends Integer> entry) {
                    String kodeKaryawan = String.valueOf(entry.getValue(1)).toLowerCase();
                    String nama = String.valueOf(entry.getValue(2)).toLowerCase();
                    return kodeKaryawan.matches(pola) || nama.matches(pola);
                }
            });
        }
    }

    private void muatData() {
        try {
            tableModel.setData(karyawanDao.ambilSemua());
        } catch (SQLException ex) {
            DialogUtil.showError(this, ex.getMessage());
        }
    }

    private void simpan() {
        try {
            karyawanDao.tambah(bacaForm());
            topsisService.batalkanTahunBerjalan(java.time.Year.now().getValue());
            DialogUtil.showInfo(this, "Data karyawan berhasil disimpan.");
            bersihkanForm();
            muatData();
        } catch (SQLException ex) {
            DialogUtil.showError(this, ex.getMessage());
        }
    }

    private void ubah() {
        if (idTerpilih == 0) {
            DialogUtil.showWarning(this, "Pilih data yang ingin diubah.");
            return;
        }
        try {
            Karyawan karyawan = bacaForm();
            karyawan.setId(idTerpilih);
            karyawanDao.ubah(karyawan);
            topsisService.batalkanTahunBerjalan(java.time.Year.now().getValue());
            DialogUtil.showInfo(this, "Data karyawan berhasil diubah.");
            bersihkanForm();
            muatData();
        } catch (SQLException ex) {
            DialogUtil.showError(this, ex.getMessage());
        }
    }

    private void hapus() {
        if (idTerpilih == 0) {
            DialogUtil.showWarning(this, "Pilih data yang ingin dihapus.");
            return;
        }
        if (!DialogUtil.confirm(this, "Hapus data karyawan terpilih?")) {
            return;
        }
        try {
            karyawanDao.hapus(idTerpilih);
            topsisService.batalkanTahunBerjalan(java.time.Year.now().getValue());
            DialogUtil.showInfo(this, "Data karyawan berhasil dihapus.");
            bersihkanForm();
            muatData();
        } catch (SQLException ex) {
            DialogUtil.showError(this, ex.getMessage());
        }
    }

    private Karyawan bacaForm() {
        if (kodeField.getText().trim().isEmpty()
                || namaField.getText().trim().isEmpty()
                || jabatanField.getText().trim().isEmpty()
                || statusComboBox.getSelectedItem() == null
                || statusComboBox.getSelectedItem().toString().trim().isEmpty()) {
            throw new IllegalArgumentException("Semua field data karyawan wajib diisi.");
        }

        Karyawan karyawan = new Karyawan();
        karyawan.setKodeKaryawan(kodeField.getText().trim());
        karyawan.setNama(namaField.getText().trim());
        karyawan.setJabatan(jabatanField.getText().trim());
        karyawan.setDivisi("Karyawan");
        karyawan.setTanggalMasuk("");
        karyawan.setStatus(statusComboBox.getSelectedItem().toString());
        return karyawan;
    }

    private void bersihkanForm() {
        idTerpilih = 0;
        kodeField.setText("");
        namaField.setText("");
        jabatanField.setText("");
        statusComboBox.setSelectedIndex(0);
        tabel.clearSelection();
    }

    private static class KaryawanTableModel extends AbstractTableModel {
        private final String[] kolom = {"No", "Kode Karyawan", "Nama", "Jabatan", "Tanggal Masuk", "Status"};
        private static final SimpleDateFormat FORMAT_DB = new SimpleDateFormat("yyyy-MM-dd");
        private static final SimpleDateFormat FORMAT_TAMPIL = new SimpleDateFormat("d MMMM yyyy", new Locale("id", "ID"));
        private List<Karyawan> data = new ArrayList<>();

        private String formatTanggalMasuk(String tanggalMasuk) {
            if (tanggalMasuk == null || tanggalMasuk.isEmpty()) {
                return "";
            }
            try {
                return FORMAT_TAMPIL.format(FORMAT_DB.parse(tanggalMasuk));
            } catch (ParseException ex) {
                return tanggalMasuk;
            }
        }

        public void setData(List<Karyawan> data) {
            this.data = data;
            fireTableDataChanged();
        }

        public Karyawan ambilKaryawan(int baris) {
            return data.get(baris);
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return kolom.length;
        }

        @Override
        public String getColumnName(int kolomIndex) {
            return kolom[kolomIndex];
        }

        @Override
        public Object getValueAt(int baris, int kolomIndex) {
            Karyawan karyawan = data.get(baris);
            switch (kolomIndex) {
                case 0: return baris + 1;
                case 1: return karyawan.getKodeKaryawan();
                case 2: return karyawan.getNama();
                case 3: return karyawan.getJabatan();
                case 4: return formatTanggalMasuk(karyawan.getTanggalMasuk());
                case 5: return karyawan.getStatus();
                default: return "";
            }
        }
    }
}
