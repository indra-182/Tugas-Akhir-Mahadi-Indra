package com.mahadi.indivaragroup.ui;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class LaporanPanelTest {
    @Test
    public void rendersReportHeaderOnlyForFirstPage() {
        assertTrue(LaporanPanel.tampilkanKop(0));
        assertFalse(LaporanPanel.tampilkanKop(1));
        assertFalse(LaporanPanel.tampilkanKop(2));
    }
}
