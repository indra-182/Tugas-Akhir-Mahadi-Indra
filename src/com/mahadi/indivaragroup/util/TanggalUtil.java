package com.mahadi.indivaragroup.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class TanggalUtil {
    private TanggalUtil() {
    }

    public static LocalDate parseWajibIso(String nilai, String namaField) {
        if (nilai == null || nilai.trim().isEmpty()) {
            throw new IllegalArgumentException(namaField + " wajib diisi.");
        }
        try {
            return LocalDate.parse(nilai.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(namaField + " harus berformat YYYY-MM-DD.");
        }
    }
}
