package com.mahadi.indivaragroup.service;

import com.mahadi.indivaragroup.dao.PenggunaDao;
import com.mahadi.indivaragroup.model.Pengguna;
import java.sql.SQLException;

public class AutentikasiService {
    private final PenggunaDao penggunaDao = new PenggunaDao();

    public Pengguna login(String username, String password) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username wajib diisi.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password wajib diisi.");
        }
        return penggunaDao.login(username.trim(), password);
    }
}
