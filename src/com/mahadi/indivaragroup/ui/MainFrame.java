package com.mahadi.indivaragroup.ui;

import com.mahadi.indivaragroup.model.Pengguna;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MainFrame extends JFrame {
    private static final String KARTU_DASHBOARD = "dashboard";
    private static final String KARTU_KARYAWAN = "karyawan";
    private static final String KARTU_KRITERIA = "kriteria";
    private static final String KARTU_PENILAIAN = "penilaian";
    private static final String KARTU_PERHITUNGAN = "perhitungan";
    private static final String KARTU_LAPORAN = "laporan";

    private final Pengguna pengguna;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel kontenPanel = new JPanel(cardLayout);

    public MainFrame(Pengguna pengguna) {
        this.pengguna = pengguna;
        setTitle("SPK Penentuan Karyawan Terbaik - PT. Indivara Group");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 600));
        setLocationRelativeTo(null);
        buatTampilan();
    }

    private void buatTampilan() {
        JPanel panelUtama = TampilanUtil.buatPanelBergaris();
        panelUtama.setLayout(new BorderLayout());

        JLabel headerLabel = new JLabel("SPK Penentuan Karyawan Terbaik - PT. Indivara Group");
        headerLabel.setFont(TampilanUtil.FONT_TEBAL);
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(TampilanUtil.WARNA_HEADER);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TampilanUtil.WARNA_GARIS));
        headerPanel.add(headerLabel, BorderLayout.WEST);
        JLabel loginLabel = new JLabel("Login: " + pengguna.getNamaLengkap() + "  ");
        loginLabel.setForeground(Color.WHITE);
        headerPanel.add(loginLabel, BorderLayout.EAST);

        kontenPanel.setBackground(Color.WHITE);
        kontenPanel.add(new DashboardPanel(), KARTU_DASHBOARD);
        kontenPanel.add(new KaryawanPanel(), KARTU_KARYAWAN);
        kontenPanel.add(new KriteriaPanel(), KARTU_KRITERIA);
        kontenPanel.add(new PenilaianPanel(), KARTU_PENILAIAN);
        kontenPanel.add(new PerhitunganTopsisPanel(), KARTU_PERHITUNGAN);
        kontenPanel.add(new LaporanPanel(), KARTU_LAPORAN);

        panelUtama.add(headerPanel, BorderLayout.NORTH);
        panelUtama.add(buatSidebar(), BorderLayout.WEST);
        panelUtama.add(kontenPanel, BorderLayout.CENTER);
        setContentPane(panelUtama);
    }

    private JPanel buatSidebar() {
        JPanel sidebar = new JPanel(new GridLayout(8, 1, 0, 10));
        sidebar.setPreferredSize(new Dimension(145, 0));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, TampilanUtil.WARNA_GARIS),
                BorderFactory.createEmptyBorder(24, 16, 180, 16)));
        sidebar.setBackground(TampilanUtil.WARNA_SIDEBAR);

        sidebar.add(buatMenuButton("Dashboard", KARTU_DASHBOARD));
        sidebar.add(buatMenuButton("Data Karyawan", KARTU_KARYAWAN));
        sidebar.add(buatMenuButton("Data Kriteria", KARTU_KRITERIA));
        sidebar.add(buatMenuButton("Penilaian", KARTU_PENILAIAN));
        sidebar.add(buatMenuButton("Perhitungan", KARTU_PERHITUNGAN));
        sidebar.add(buatMenuButton("Laporan", KARTU_LAPORAN));

        JButton logoutButton = TampilanUtil.buatTombolAksi("Logout");
        logoutButton.addActionListener(e -> logout());
        sidebar.add(logoutButton);
        return sidebar;
    }

    private JButton buatMenuButton(String teks, String kartu) {
        JButton tombol = TampilanUtil.buatTombol(teks, Color.WHITE, TampilanUtil.WARNA_BIRU_TUA);
        tombol.addActionListener(e -> cardLayout.show(kontenPanel, kartu));
        return tombol;
    }

    private void logout() {
        new LoginFrame().setVisible(true);
        dispose();
    }
}
