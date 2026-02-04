package com.companytracker;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite implementation of StorageService.
 * Stores company data in a local SQLite database file.
 */
public class DatabaseService implements StorageService {
    private static final String DB_URL = "jdbc:sqlite:companies.db";
    private Connection connection;

    public DatabaseService() throws SQLException {
        connection = DriverManager.getConnection(DB_URL);
        createTable();
    }

    /**
     * Create the companies table if it doesn't exist.
     * Also runs migrations for existing databases.
     */
    private void createTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS companies (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE,
                employees REAL DEFAULT 0,
                is_checked INTEGER DEFAULT 0,
                has_website INTEGER DEFAULT 0,
                website TEXT
            )
            """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
        migrateIfNeeded();
    }

    /** Add missing columns for older databases */
    private void migrateIfNeeded() {
        String[] migrations = {
            "ALTER TABLE companies ADD COLUMN employees REAL DEFAULT 0",
            "ALTER TABLE companies ADD COLUMN is_checked INTEGER DEFAULT 0"
        };
        for (String sql : migrations) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(sql);
            } catch (SQLException e) {
                // Column already exists, ignore
            }
        }
    }

    @Override
    public void addCompany(String name, double employees) throws SQLException {
        // INSERT OR IGNORE prevents duplicates (name is UNIQUE)
        String sql = "INSERT OR IGNORE INTO companies(name, employees) VALUES(?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setDouble(2, employees);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Company> getAllCompanies() throws SQLException {
        String sql = "SELECT * FROM companies ORDER BY name";
        return queryCompanies(sql);
    }

    @Override
    public List<Company> getUncheckedCompanies() throws SQLException {
        String sql = "SELECT * FROM companies WHERE is_checked = 0 ORDER BY name";
        return queryCompanies(sql);
    }

    /** Helper method to execute a query and return list of companies */
    private List<Company> queryCompanies(String sql) throws SQLException {
        List<Company> companies = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                companies.add(mapRow(rs));
            }
        }
        return companies;
    }

    /** Map a database row to a Company object */
    private Company mapRow(ResultSet rs) throws SQLException {
        Company company = new Company();
        company.setId(rs.getInt("id"));
        company.setName(rs.getString("name"));
        company.setEmployees(rs.getDouble("employees"));
        company.setChecked(rs.getInt("is_checked") == 1);
        company.setHasWebsite(rs.getInt("has_website") == 1);
        company.setWebsite(rs.getString("website"));
        return company;
    }

    @Override
    public void updateWebsite(int companyId, String website, boolean hasWebsite) throws SQLException {
        String sql = "UPDATE companies SET website = ?, has_website = ?, is_checked = 1 WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, website);
            stmt.setInt(2, hasWebsite ? 1 : 0);
            stmt.setInt(3, companyId);
            stmt.executeUpdate();
        }
    }

    @Override
    public void resetAllCompanies() throws SQLException {
        String sql = "UPDATE companies SET is_checked = 0, has_website = 0, website = NULL";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    @Override
    public void deleteAllCompanies() throws SQLException {
        String sql = "DELETE FROM companies";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
