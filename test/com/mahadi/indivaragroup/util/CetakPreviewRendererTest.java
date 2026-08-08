package com.mahadi.indivaragroup.util;

import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class CetakPreviewRendererTest {
    @Test
    public void rendersEveryPrintablePageUntilNoSuchPage() throws Exception {
        final List<Integer> requestedPages = new ArrayList<Integer>();
        Printable threePagePrintable = new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
                requestedPages.add(pageIndex);
                return pageIndex < 3 ? PAGE_EXISTS : NO_SUCH_PAGE;
            }
        };

        int renderedPageCount = CetakPreviewRenderer.renderAll(
                threePagePrintable, new PageFormat()).size();

        assertEquals(3, renderedPageCount);
        assertEquals(Arrays.asList(0, 1, 2, 3), requestedPages);
    }
}
