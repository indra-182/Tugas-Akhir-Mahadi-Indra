package com.mahadi.indivaragroup;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.Test;

/** Menjalankan tes JUnit 4 tanpa dependency Hamcrest tambahan. */
public final class TestRunner {
    private TestRunner() {
    }

    public static void main(String[] namaKelasTes) throws Exception {
        int gagal = 0;
        for (String namaKelas : namaKelasTes) {
            gagal += jalankanKelas(namaKelas);
        }
        if (gagal > 0) {
            throw new AssertionError(gagal + " test gagal.");
        }
        System.out.println("Semua test berhasil.");
    }

    private static int jalankanKelas(String namaKelas) throws Exception {
        Class<?> kelas = Class.forName(namaKelas);
        Object instance = kelas.newInstance();
        int gagal = 0;
        for (Method metode : kelas.getDeclaredMethods()) {
            Test anotasi = metode.getAnnotation(Test.class);
            if (anotasi != null && !jalankanMetode(instance, metode, anotasi)) {
                gagal++;
            }
        }
        return gagal;
    }

    private static boolean jalankanMetode(Object instance, Method metode, Test anotasi) {
        Class<? extends Throwable> diharapkan = anotasi.expected();
        try {
            metode.invoke(instance);
            if (!Test.None.class.equals(diharapkan)) {
                System.err.println(metode + " seharusnya melempar " + diharapkan.getName());
                return false;
            }
            return true;
        } catch (IllegalAccessException ex) {
            System.err.println(metode + " tidak dapat dijalankan: " + ex.getMessage());
            return false;
        } catch (InvocationTargetException ex) {
            Throwable penyebab = ex.getCause();
            if (diharapkan.isInstance(penyebab)) {
                return true;
            }
            System.err.println(metode + " gagal: " + penyebab);
            return false;
        }
    }
}
