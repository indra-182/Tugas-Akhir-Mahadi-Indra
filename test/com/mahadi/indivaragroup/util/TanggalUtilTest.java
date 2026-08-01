package com.mahadi.indivaragroup.util;

import java.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;

public class TanggalUtilTest {
    @Test
    public void menerimaTanggalIsoYangValid() {
        Assert.assertEquals(LocalDate.of(2024, 2, 29),
                TanggalUtil.parseWajibIso("2024-02-29", "Tanggal masuk"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void menolakTanggalKosong() {
        TanggalUtil.parseWajibIso("  ", "Tanggal masuk");
    }

    @Test(expected = IllegalArgumentException.class)
    public void menolakFormatSelainIso() {
        TanggalUtil.parseWajibIso("29-02-2024", "Tanggal masuk");
    }

    @Test(expected = IllegalArgumentException.class)
    public void menolakTanggalKalenderTidakValid() {
        TanggalUtil.parseWajibIso("2025-02-29", "Tanggal masuk");
    }
}
