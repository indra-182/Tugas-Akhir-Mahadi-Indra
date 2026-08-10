package com.mahadi.indivaragroup.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Menghasilkan rentang baris untuk pagination laporan. */
public final class PaginasiLaporan {
    private PaginasiLaporan() {
    }

    public static List<RentangHalaman> buat(int jumlahBaris,
            int kapasitasHalamanPertama, int kapasitasHalamanBerikutnya) {
        return buatBagian(Collections.singletonList(jumlahBaris),
                kapasitasHalamanPertama, kapasitasHalamanBerikutnya);
    }

    public static List<RentangHalaman> buatBagian(List<Integer> jumlahBarisBagian,
            int kapasitasHalamanPertama, int kapasitasHalamanBerikutnya) {
        if (jumlahBarisBagian == null) {
            throw new IllegalArgumentException("Daftar jumlah baris wajib tersedia.");
        }
        if (kapasitasHalamanPertama <= 0 || kapasitasHalamanBerikutnya <= 0) {
            throw new IllegalArgumentException("Kapasitas halaman harus lebih besar dari nol.");
        }

        List<RentangHalaman> halaman = new ArrayList<RentangHalaman>();
        boolean halamanPertama = true;
        for (int indeksBagian = 0; indeksBagian < jumlahBarisBagian.size(); indeksBagian++) {
            Integer jumlahBarisNilai = jumlahBarisBagian.get(indeksBagian);
            if (jumlahBarisNilai == null || jumlahBarisNilai < 0) {
                throw new IllegalArgumentException("Jumlah baris tidak boleh negatif atau kosong.");
            }

            int jumlahBaris = jumlahBarisNilai;
            int barisMulai = 0;
            while (barisMulai < jumlahBaris) {
                int kapasitas = halamanPertama
                        ? kapasitasHalamanPertama : kapasitasHalamanBerikutnya;
                int barisAkhir = Math.min(jumlahBaris, barisMulai + kapasitas);
                halaman.add(new RentangHalaman(
                        indeksBagian, barisMulai, barisAkhir, halamanPertama));
                halamanPertama = false;
                barisMulai = barisAkhir;
            }
        }
        return Collections.unmodifiableList(halaman);
    }

    public static final class RentangHalaman {
        private final int indeksBagian;
        private final int barisMulai;
        private final int barisAkhir;
        private final boolean halamanPertama;

        private RentangHalaman(int indeksBagian, int barisMulai,
                int barisAkhir, boolean halamanPertama) {
            this.indeksBagian = indeksBagian;
            this.barisMulai = barisMulai;
            this.barisAkhir = barisAkhir;
            this.halamanPertama = halamanPertama;
        }

        public int getIndeksBagian() {
            return indeksBagian;
        }

        public int getBarisMulai() {
            return barisMulai;
        }

        public int getBarisAkhir() {
            return barisAkhir;
        }

        public boolean isHalamanPertama() {
            return halamanPertama;
        }
    }
}
