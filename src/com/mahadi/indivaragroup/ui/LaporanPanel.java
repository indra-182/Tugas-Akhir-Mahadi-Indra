package com.mahadi.indivaragroup.ui;

import com.mahadi.indivaragroup.dao.HasilRankingDao;
import com.mahadi.indivaragroup.dao.KaryawanDao;
import com.mahadi.indivaragroup.dao.PenilaianDao;
import com.mahadi.indivaragroup.dao.PerhitunganSnapshotDao;
import com.mahadi.indivaragroup.model.HasilRanking;
import com.mahadi.indivaragroup.model.Karyawan;
import com.mahadi.indivaragroup.util.DialogUtil;
import com.mahadi.indivaragroup.util.NumberUtil;
import com.mahadi.indivaragroup.service.PerhitunganTopsisService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import javax.imageio.ImageIO;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Destination;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class LaporanPanel extends JPanel {
    private static final String LOKASI_LOGO = "src\\com\\mahadi\\indivaragroup\\assets\\logo indivara.jpg";
    private static final String NAMA_PERUSAHAAN = "PT. INDIVARA GROUP";
    private static final String ALAMAT_BARIS_1 = "Kirana Boutique Office Blok G, Jl. Kirana Avenue No.3";
    private static final String ALAMAT_BARIS_2 = "Klp. Gading Tim., Kec. Klp. Gading, Jkt Utara";
    private static final String ALAMAT_BARIS_3 = "Daerah Khusus Ibukota Jakarta 14240";
    private static final String TELEPON = "Telepon: (021) 22455773";
    private static final String NAMA_PENANDATANGAN = "Pimpinan PT. Indivara Group";

    private static final String LAPORAN_DATA_RANKING = "Laporan Data Ranking";
    private static final String LAPORAN_DATA_PENILAIAN = "Laporan Data Penilaian";
    private static final String LAPORAN_DATA_KARYAWAN = "Laporan Data Karyawan";
    private static final String LAPORAN_TREN_KARYAWAN = "Laporan Tren Kinerja Karyawan";

    private final KaryawanDao karyawanDao = new KaryawanDao();
    private final PenilaianDao penilaianDao = new PenilaianDao();
    private final HasilRankingDao hasilRankingDao = new HasilRankingDao();
    private final PerhitunganSnapshotDao snapshotDao = new PerhitunganSnapshotDao();
    private final PerhitunganTopsisService topsisService = new PerhitunganTopsisService();

    private final JComboBox<String> jenisLaporanComboBox = new JComboBox<>(new String[]{
        LAPORAN_DATA_RANKING,
        LAPORAN_DATA_PENILAIAN,
        LAPORAN_DATA_KARYAWAN,
        LAPORAN_TREN_KARYAWAN
    });
    private JComboBox<Integer> tahunComboBox;
    private JComboBox<Karyawan> karyawanComboBox;
    private final DefaultTableModel tableModel = new DefaultTableModel();
    private final JTable tabel = new JTable(tableModel);
    private final JPanel judulPanel = new JPanel(new BorderLayout());

    public LaporanPanel() {
        setLayout(new BorderLayout(10, 22));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(32, 32, 28, 32));
        buatTampilan();
        pasangEventPanel();
        muatLaporan();
    }

    private void buatTampilan() {
        judulPanel.setBackground(Color.WHITE);
        add(judulPanel, BorderLayout.NORTH);

        JPanel isiPanel = new JPanel(new BorderLayout(10, 18));
        isiPanel.setBackground(Color.WHITE);
        isiPanel.add(buatPanelAksi(), BorderLayout.NORTH);

        tabel.setAutoCreateRowSorter(true);
        TampilanUtil.rapikanTabel(tabel);
        isiPanel.add(new JScrollPane(tabel), BorderLayout.CENTER);
        add(isiPanel, BorderLayout.CENTER);
    }

    private JPanel buatPanelAksi() {
        JPanel panelAksi = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelAksi.setBackground(Color.WHITE);
        JButton cetakButton = TampilanUtil.buatTombolAksi("Tambah");
        cetakButton.setText("Cetak");

        tahunComboBox = TampilanUtil.buatComboBoxTahun(daftarTahunAman());
        karyawanComboBox = new JComboBox<>();
        muatKaryawanComboBox();

        jenisLaporanComboBox.addActionListener(e -> muatLaporan());
        tahunComboBox.addActionListener(e -> muatLaporan());
        karyawanComboBox.addActionListener(e -> muatLaporan());
        cetakButton.addActionListener(e -> cetakLaporanPdf());

        panelAksi.add(jenisLaporanComboBox);
        panelAksi.add(tahunComboBox);
        panelAksi.add(karyawanComboBox);
        panelAksi.add(cetakButton);
        return panelAksi;
    }

    private List<Integer> daftarTahunAman() {
        try {
            return penilaianDao.ambilDaftarTahun();
        } catch (SQLException ex) {
            return Collections.emptyList();
        }
    }

    private int tahunTerpilih() {
        Integer tahun = tahunComboBox == null ? null : (Integer) tahunComboBox.getSelectedItem();
        return tahun != null ? tahun : Year.now().getValue();
    }

    private void muatKaryawanComboBox() {
        try {
            karyawanComboBox.removeAllItems();
            LinkedHashMap<Integer, Karyawan> daftarKaryawan = new LinkedHashMap<Integer, Karyawan>();
            for (Karyawan karyawan : karyawanDao.ambilSemua()) daftarKaryawan.put(karyawan.getId(), karyawan);
            for (Karyawan karyawan : snapshotDao.ambilPesertaTersimpan()) {
                if (!daftarKaryawan.containsKey(karyawan.getId())) daftarKaryawan.put(karyawan.getId(), karyawan);
            }
            daftarKaryawan.values().forEach(karyawanComboBox::addItem);
        } catch (SQLException ex) {
            DialogUtil.showError(this, ex.getMessage());
        }
    }

    private void terapkanTampilanSelector(String jenis) {
        boolean tampilTahun = LAPORAN_DATA_RANKING.equals(jenis) || LAPORAN_DATA_PENILAIAN.equals(jenis);
        boolean tampilKaryawan = LAPORAN_TREN_KARYAWAN.equals(jenis);
        tahunComboBox.setVisible(tampilTahun);
        karyawanComboBox.setVisible(tampilKaryawan);
    }

    private void pasangEventPanel() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                TampilanUtil.segarkanComboBoxTahun(tahunComboBox, daftarTahunAman());
                jenisLaporanComboBox.setSelectedItem(LAPORAN_DATA_RANKING);
                muatLaporan();
            }
        });
    }

    private void muatLaporan() {
        try {
            String jenis = jenisLaporanComboBox.getSelectedItem().toString();
            judulPanel.removeAll();
            judulPanel.add(TampilanUtil.buatJudul(jenis.toUpperCase()), BorderLayout.CENTER);
            terapkanTampilanSelector(jenis);

            switch (jenis) {
                case LAPORAN_DATA_PENILAIAN:
                    muatLaporanPenilaian();
                    break;
                case LAPORAN_DATA_KARYAWAN:
                    muatLaporanKaryawan();
                    break;
                case LAPORAN_TREN_KARYAWAN:
                    muatLaporanTrenKaryawan();
                    break;
                default:
                    muatLaporanRanking();
                    break;
            }

            judulPanel.revalidate();
            judulPanel.repaint();
        } catch (Exception ex) {
            DialogUtil.showError(this, ex.getMessage());
        }
    }

    private void muatLaporanPenilaian() throws Exception {
        tableModel.setDataVector(new Object[][]{}, new Object[]{
            "No", "Kode Karyawan", "Nama Karyawan", "Kode Kriteria", "Kriteria", "Nilai"
        });
        List<Object[]> data = penilaianDao.ambilLaporanPenilaian(tahunTerpilih());
        for (int i = 0; i < data.size(); i++) {
            Object[] baris = data.get(i);
            Object[] barisBaru = new Object[baris.length + 1];
            barisBaru[0] = i + 1;
            System.arraycopy(baris, 0, barisBaru, 1, baris.length);
            tableModel.addRow(barisBaru);
        }
    }

    private void muatLaporanRanking() throws Exception {
        int tahun = tahunTerpilih();
        List<HasilRanking> daftarRanking;
        if (tahun == Year.now().getValue()) {
            daftarRanking = hasilRankingDao.ambilSemua(tahun);
        } else {
            topsisService.ambilDetailHistoris(tahun);
            daftarRanking = snapshotDao.ambilRanking(tahun);
        }
        if (daftarRanking.isEmpty()) {
            tampilkanPesanInfo(tahun == Year.now().getValue()
                    ? "Proses Perhitungan Dahulu untuk Menampilkan Data"
                    : "Belum ada data hasil perhitungan untuk tahun ini.");
            return;
        }

        tableModel.setDataVector(new Object[][]{}, new Object[]{
            "Peringkat", "Kode Karyawan", "Nama Karyawan", "Nilai TOPSIS"
        });
        daftarRanking.forEach((ranking) -> {
            tableModel.addRow(new Object[]{
                ranking.getPeringkat(),
                ranking.getKodeKaryawan(),
                ranking.getNamaKaryawan(),
                NumberUtil.format(ranking.getNilaiTopsis())
            });
        });
    }

    private void muatLaporanKaryawan() throws Exception {
        tableModel.setDataVector(new Object[][]{}, new Object[]{"No", "Kode Karyawan", "Nama", "Jabatan", "Status"});
        List<Karyawan> daftarKaryawan = karyawanDao.ambilSemua();
        for (int i = 0; i < daftarKaryawan.size(); i++) {
            Karyawan karyawan = daftarKaryawan.get(i);
            tableModel.addRow(new Object[]{
                i + 1,
                karyawan.getKodeKaryawan(),
                karyawan.getNama(),
                karyawan.getJabatan(),
                karyawan.getStatus()
            });
        }
    }

    private void muatLaporanTrenKaryawan() throws Exception {
        Karyawan karyawanTerpilih = (Karyawan) karyawanComboBox.getSelectedItem();
        if (karyawanTerpilih == null) {
            tampilkanPesanInfo("Pilih karyawan untuk menampilkan tren kinerjanya");
            return;
        }

        for (Integer tahun : penilaianDao.ambilDaftarTahunByKaryawan(karyawanTerpilih.getId())) {
            if (tahun < Year.now().getValue()) {
                try {
                    topsisService.ambilDetailHistoris(tahun);
                } catch (IllegalArgumentException ex) {
                    // An incomplete legacy period has no valid trend value; other periods remain visible.
                }
            }
        }
        List<HasilRanking> daftarRiwayat = snapshotDao.ambilRiwayat(karyawanTerpilih.getId());
        if (daftarRiwayat.isEmpty()) daftarRiwayat = hasilRankingDao.ambilRiwayatByKaryawan(karyawanTerpilih.getId());
        if (daftarRiwayat.isEmpty()) {
            tampilkanPesanInfo("Proses Perhitungan Dahulu untuk Menampilkan Data");
            return;
        }

        tableModel.setDataVector(new Object[][]{}, new Object[]{"Tahun", "Peringkat", "Nilai TOPSIS"});
        daftarRiwayat.forEach((ranking) -> {
            tableModel.addRow(new Object[]{
                ranking.getTahun(),
                ranking.getPeringkat(),
                NumberUtil.format(ranking.getNilaiTopsis())
            });
        });
    }

    private void tampilkanPesanInfo(String pesan) {
        tableModel.setDataVector(new Object[][]{
            {pesan}
        }, new Object[]{"Informasi"});
    }

    private boolean sedangMenampilkanPesanInfo() {
        return tableModel.getColumnCount() == 1 && "Informasi".equals(tableModel.getColumnName(0));
    }

    private void cetakLaporanPdf() {
        String jenis = jenisLaporanComboBox.getSelectedItem().toString();

        if (tableModel.getRowCount() == 0 || sedangMenampilkanPesanInfo()) {
            DialogUtil.showWarning(this, "Belum ada data laporan untuk dicetak.");
            return;
        }

        String namaLaporan = jenis;
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setJobName(namaLaporan);
        printerJob.setPrintable(new CetakLaporanPrintable());

        File folderUnduhan = new File(System.getProperty("user.home"), "Downloads");
        File fileHasil = new File(folderUnduhan, namaLaporan + ".pdf");
        PrintRequestAttributeSet atribut = new HashPrintRequestAttributeSet();
        atribut.add(new Destination(fileHasil.toURI()));

        try {
            if (!printerJob.printDialog(atribut)) {
                return;
            }
        } catch (HeadlessException ex) {
            DialogUtil.showError(this, ex.getMessage());
            return;
        }

        JDialog dialogProses = DialogUtil.tampilkanProses(this, "Sedang mendownload " + namaLaporan + "...");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws PrinterException {
                printerJob.print(atribut);
                return null;
            }

            @Override
            protected void done() {
                dialogProses.dispose();
                try {
                    get();
                    bukaFileHasil(fileHasil);
                } catch (Exception ex) {
                    Throwable penyebab = ex.getCause() != null ? ex.getCause() : ex;
                    DialogUtil.showError(LaporanPanel.this, penyebab.getMessage());
                }
            }
        }.execute();
    }

    private void bukaFileHasil(File file) {
        if (!file.exists() || !Desktop.isDesktopSupported()) {
            return;
        }
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException ex) {
            DialogUtil.showWarning(this, "File tersimpan, tetapi gagal dibuka otomatis: " + ex.getMessage());
        }
    }

    private class CetakLaporanPrintable implements Printable {
        private static final int MARGIN_KONTEN = 20;
        private static final int TINGGI_BARIS = 18;
        private static final int TINGGI_HEADER_TABEL = 22;
        private static final int LEBAR_LOGO = 90;
        private static final int TINGGI_LOGO = 75;

        private final Image logo = muatLogo();
        private final Font fontJudul = new Font("Serif", Font.BOLD, 14);
        private final Font fontSubJudul = new Font("Serif", Font.BOLD, 12);
        private final Font fontNormal = new Font("Serif", Font.PLAIN, 10);
        private final Font fontTabel = new Font("SansSerif", Font.PLAIN, 8);
        private final Font fontTabelHeader = new Font("SansSerif", Font.BOLD, 8);

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
            Graphics2D grafik = (Graphics2D) graphics;
            int xAwal = (int) pageFormat.getImageableX() + MARGIN_KONTEN;
            int yAwal = (int) pageFormat.getImageableY() + MARGIN_KONTEN;
            int lebarKonten = (int) pageFormat.getImageableWidth() - (MARGIN_KONTEN * 2);
            int tinggiKonten = (int) pageFormat.getImageableHeight() - (MARGIN_KONTEN * 2);
            int yTabel = gambarKopLaporan(grafik, xAwal, yAwal, lebarKonten);
            int tinggiAreaTabel = tinggiKonten - (yTabel - yAwal) - 150;
            int jumlahBarisPerHalaman = Math.max(1, (tinggiAreaTabel - TINGGI_HEADER_TABEL) / TINGGI_BARIS);
            int barisMulai = pageIndex * jumlahBarisPerHalaman;

            if (barisMulai >= tableModel.getRowCount()) {
                return NO_SUCH_PAGE;
            }

            int barisAkhir = Math.min(tableModel.getRowCount(), barisMulai + jumlahBarisPerHalaman);
            gambarTabel(grafik, xAwal, yTabel, lebarKonten, barisMulai, barisAkhir);

            if (barisAkhir >= tableModel.getRowCount()) {
                gambarTandaTangan(grafik, xAwal, yAwal + tinggiKonten - 130, lebarKonten);
            }
            return PAGE_EXISTS;
        }

        private Image muatLogo() {
            try {
                File fileLogo = new File(LOKASI_LOGO);
                return fileLogo.exists() ? ImageIO.read(fileLogo) : null;
            } catch (IOException ex) {
                return null;
            }
        }

        private int gambarKopLaporan(Graphics2D grafik, int xAwal, int yAwal, int lebarKonten) {
            grafik.setColor(Color.BLACK);
            grafik.drawRect(xAwal - 10, yAwal - 10, lebarKonten + 20, 760);

            if (logo != null) {
                grafik.drawImage(logo, xAwal + 18, yAwal + 8, LEBAR_LOGO, TINGGI_LOGO, null);
            }

            int tengah = xAwal + (lebarKonten / 2);
            int y = yAwal + 18;
            gambarTeksTengah(grafik, NAMA_PERUSAHAAN, tengah, y, fontJudul);
            y += 16;
            gambarTeksTengah(grafik, ALAMAT_BARIS_1, tengah, y, fontNormal);
            y += 13;
            gambarTeksTengah(grafik, ALAMAT_BARIS_2, tengah, y, fontNormal);
            y += 13;
            gambarTeksTengah(grafik, ALAMAT_BARIS_3, tengah, y, fontNormal);
            y += 13;
            gambarTeksTengah(grafik, TELEPON, tengah, y, fontNormal);
            y += 34;
            gambarTeksTengah(grafik, jenisLaporanComboBox.getSelectedItem().toString().toUpperCase(), tengah, y, fontSubJudul);
            return y + 20;
        }

        private void gambarTabel(Graphics2D grafik, int xAwal, int yAwal, int lebarKonten, int barisMulai, int barisAkhir) {
            int jumlahKolom = tableModel.getColumnCount();
            int lebarKolom = Math.max(35, lebarKonten / jumlahKolom);
            int y = yAwal;

            grafik.setFont(fontTabelHeader);
            grafik.setColor(new Color(248, 203, 203));
            grafik.fillRect(xAwal, y, lebarKolom * jumlahKolom, TINGGI_HEADER_TABEL);
            grafik.setColor(Color.BLACK);

            for (int kolom = 0; kolom < jumlahKolom; kolom++) {
                int x = xAwal + (kolom * lebarKolom);
                grafik.drawRect(x, y, lebarKolom, TINGGI_HEADER_TABEL);
                gambarTeksPotong(grafik, tableModel.getColumnName(kolom), x + 3, y + 14, lebarKolom - 6);
            }

            grafik.setFont(fontTabel);
            y += TINGGI_HEADER_TABEL;
            for (int baris = barisMulai; baris < barisAkhir; baris++) {
                for (int kolom = 0; kolom < jumlahKolom; kolom++) {
                    int x = xAwal + (kolom * lebarKolom);
                    grafik.drawRect(x, y, lebarKolom, TINGGI_BARIS);
                    Object nilai = tableModel.getValueAt(baris, kolom);
                    gambarTeksPotong(grafik, String.valueOf(nilai), x + 3, y + 13, lebarKolom - 6);
                }
                y += TINGGI_BARIS;
            }
        }

        private void gambarTandaTangan(Graphics2D grafik, int xAwal, int yAwal, int lebarKonten) {
            int lebarAreaTandaTangan = 190;
            int xTandaTangan = xAwal + lebarKonten - lebarAreaTandaTangan - 12;
            String tanggal = new SimpleDateFormat("EEEE d MMMM yyyy", new Locale("id", "ID")).format(new Date());

            grafik.setFont(fontNormal);
            grafik.setColor(Color.BLACK);
            gambarTeksTengah(grafik, "Jakarta, " + tanggal, xTandaTangan + (lebarAreaTandaTangan / 2), yAwal, fontNormal);
            gambarTeksTengah(grafik, "Mengetahui,", xTandaTangan + (lebarAreaTandaTangan / 2), yAwal + 32, fontNormal);
            gambarTeksTengah(grafik, NAMA_PENANDATANGAN, xTandaTangan + (lebarAreaTandaTangan / 2), yAwal + 116, fontNormal);
        }

        private void gambarTeksTengah(Graphics2D grafik, String teks, int tengah, int y, Font font) {
            grafik.setFont(font);
            int lebarTeks = grafik.getFontMetrics().stringWidth(teks);
            grafik.drawString(teks, tengah - (lebarTeks / 2), y);
        }

        private void gambarTeksPotong(Graphics2D grafik, String teks, int x, int y, int lebarMaksimum) {
            if (teks == null) {
                teks = "";
            }
            String teksTampil = teks;
            while (grafik.getFontMetrics().stringWidth(teksTampil) > lebarMaksimum && teksTampil.length() > 3) {
                teksTampil = teksTampil.substring(0, teksTampil.length() - 4) + "...";
            }
            grafik.drawString(teksTampil, x, y);
        }
    }
}
