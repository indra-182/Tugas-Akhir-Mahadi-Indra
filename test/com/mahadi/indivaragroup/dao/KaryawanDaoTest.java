package com.mahadi.indivaragroup.dao;

import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.junit.Test;

public class KaryawanDaoTest {
    @Test
    public void mengikatTanggalMasukSebagaiSqlDate() throws Exception {
        AtomicInteger indeks = new AtomicInteger();
        AtomicReference<Object> nilai = new AtomicReference<Object>();
        PreparedStatement perintah = (PreparedStatement) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{PreparedStatement.class}, (proxy, metode, argumen) -> {
                    if ("setDate".equals(metode.getName())) {
                        indeks.set((Integer) argumen[0]);
                        nilai.set(argumen[1]);
                        return null;
                    }
                    throw new UnsupportedOperationException(metode.getName());
                });

        KaryawanDao.isiTanggalMasuk(perintah, LocalDate.of(2024, 2, 29));

        Assert.assertEquals(5, indeks.get());
        Assert.assertEquals(java.sql.Date.valueOf("2024-02-29"), nilai.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void menolakTanggalMasukYangBelumDiisi() throws Exception {
        KaryawanDao.isiTanggalMasuk(null, null);
    }
}
