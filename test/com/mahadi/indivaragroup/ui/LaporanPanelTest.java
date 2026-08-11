package com.mahadi.indivaragroup.ui;

import com.mahadi.indivaragroup.model.HasilRanking;
import java.util.Collections;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class LaporanPanelTest {
    @Test
    public void rendersReportHeaderOnlyForFirstPage() {
        assertTrue(LaporanPanel.tampilkanKop(0));
        assertFalse(LaporanPanel.tampilkanKop(1));
        assertFalse(LaporanPanel.tampilkanKop(2));
    }

    @Test
    public void rankingReportUsesTableRendererEvenWhenCalculationDetailExists() {
        assertFalse(LaporanPanel.gunakanRendererPerhitungan("Laporan Data Ranking", true));
    }

    @Test
    public void trendReportRowsContainYearRankingAndScoreOnly() {
        HasilRanking ranking = new HasilRanking();
        ranking.setTahun(2025);
        ranking.setPeringkat(2);
        ranking.setNilaiTopsis(0.75);

        Object[][] baris = LaporanPanel.buatBarisTren(Collections.singletonList(ranking));

        assertEquals(1, baris.length);
        assertEquals(3, baris[0].length);
        assertEquals(2025, baris[0][0]);
        assertEquals(2, baris[0][1]);
        assertEquals("0.75", baris[0][2]);
    }
}
