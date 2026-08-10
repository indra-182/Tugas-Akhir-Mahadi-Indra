package com.mahadi.indivaragroup.util;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class PaginasiLaporanTest {
    @Test
    public void usesContinuationCapacityWithoutSkippingRows() {
        List<PaginasiLaporan.RentangHalaman> halaman = PaginasiLaporan.buat(10, 3, 4);

        assertEquals(3, halaman.size());
        assertEquals(0, halaman.get(0).getBarisMulai());
        assertEquals(3, halaman.get(0).getBarisAkhir());
        assertTrue(halaman.get(0).isHalamanPertama());
        assertEquals(3, halaman.get(1).getBarisMulai());
        assertEquals(7, halaman.get(1).getBarisAkhir());
        assertFalse(halaman.get(1).isHalamanPertama());
        assertEquals(7, halaman.get(2).getBarisMulai());
        assertEquals(10, halaman.get(2).getBarisAkhir());
    }

    @Test
    public void paginatesTopsisSectionsContiguously() {
        List<PaginasiLaporan.RentangHalaman> halaman = PaginasiLaporan.buatBagian(
                Arrays.asList(4, 3), 2, 3);

        assertEquals(3, halaman.size());
        assertEquals(0, halaman.get(0).getIndeksBagian());
        assertEquals(0, halaman.get(0).getBarisMulai());
        assertEquals(2, halaman.get(0).getBarisAkhir());
        assertEquals(0, halaman.get(1).getIndeksBagian());
        assertEquals(2, halaman.get(1).getBarisMulai());
        assertEquals(4, halaman.get(1).getBarisAkhir());
        assertEquals(1, halaman.get(2).getIndeksBagian());
        assertEquals(0, halaman.get(2).getBarisMulai());
        assertEquals(3, halaman.get(2).getBarisAkhir());
        assertFalse(halaman.get(2).isHalamanPertama());
    }

    @Test
    public void returnsNoRangesForEmptyRows() {
        assertTrue(PaginasiLaporan.buat(0, 3, 4).isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNonPositiveCapacity() {
        PaginasiLaporan.buat(1, 0, 4);
    }
}
