package com.mahadi.indivaragroup.service;

import com.mahadi.indivaragroup.model.HasilRanking;
import com.mahadi.indivaragroup.model.Karyawan;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class PeringkatTopsisTest {
    @Test
    public void nilaiPreferensiSamaMendapatCompetitionRanking() {
        List<Karyawan> karyawan = Arrays.asList(
                karyawan(1, "K001", "Zahra"),
                karyawan(2, "K002", "Andi"),
                karyawan(3, "K003", "Bima")
        );

        List<HasilRanking> hasil = PeringkatTopsis.buat(karyawan, new double[]{0.90, 0.90, 0.80});

        Assert.assertEquals(1, hasil.get(0).getPeringkat());
        Assert.assertEquals(1, hasil.get(1).getPeringkat());
        Assert.assertEquals(3, hasil.get(2).getPeringkat());
        Assert.assertEquals(0.90, hasil.get(0).getNilaiTopsis(), 0.0);
        Assert.assertEquals(0.90, hasil.get(1).getNilaiTopsis(), 0.0);
    }

    @Test
    public void nilaiPreferensiBerbedaMendapatRankingBerurutan() {
        List<Karyawan> karyawan = Arrays.asList(
                karyawan(1, "K001", "Andi"),
                karyawan(2, "K002", "Bima"),
                karyawan(3, "K003", "Citra")
        );

        List<HasilRanking> hasil = PeringkatTopsis.buat(karyawan, new double[]{0.90, 0.80, 0.70});

        Assert.assertEquals(1, hasil.get(0).getPeringkat());
        Assert.assertEquals(2, hasil.get(1).getPeringkat());
        Assert.assertEquals(3, hasil.get(2).getPeringkat());
    }

    @Test(expected = IllegalArgumentException.class)
    public void menolakJumlahKaryawanDanNilaiPreferensiYangBerbeda() {
        PeringkatTopsis.buat(Arrays.asList(karyawan(1, "K001", "Andi")), new double[]{});
    }

    private Karyawan karyawan(int id, String kode, String nama) {
        Karyawan karyawan = new Karyawan();
        karyawan.setId(id);
        karyawan.setKodeKaryawan(kode);
        karyawan.setNama(nama);
        return karyawan;
    }
}
