package com.companytracker;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService implements StorageService {
    private static final String DB_URL = "jdbc:sqlite:companies.db";
    private Connection connection;

    public DatabaseService() throws SQLException {
        connect();
        createTable();
    }

    private void connect() throws SQLException {
        connection = DriverManager.getConnection(DB_URL);
        System.out.println("Connected to SQLite database");
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS companies (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL UNIQUE," +
                "website TEXT," +
                "has_website INTEGER DEFAULT 0," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("Database table ready");
        }
    }

    public void addCompany(Company company) throws SQLException {
        String sql = "INSERT INTO companies(name) VALUES(?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, company.getName());
            pstmt.executeUpdate();
            System.out.println("Added company: " + company.getName());
        }
    }

    public void updateCompanyWebsite(String companyName, String website, boolean hasWebsite) throws SQLException {
        String sql = "UPDATE companies SET website = ?, has_website = ? WHERE name = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, website);
            pstmt.setInt(2, hasWebsite ? 1 : 0);
            pstmt.setString(3, companyName);
            pstmt.executeUpdate();
            System.out.println("Updated website info for: " + companyName);
        }
    }

    public List<Company> getAllCompanies() throws SQLException {
        List<Company> companies = new ArrayList<>();
        String sql = "SELECT * FROM companies";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                companies.add(mapResultSetToCompany(rs));
            }
        }

        return companies;
    }

    public Company getCompanyByName(String name) throws SQLException {
        String sql = "SELECT * FROM companies WHERE name = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToCompany(rs);
            }
        }

        return null;
    }

    private Company mapResultSetToCompany(ResultSet rs) throws SQLException {
        Company company = new Company();
        company.setId(rs.getInt("id"));
        company.setName(rs.getString("name"));
        company.setWebsite(rs.getString("website"));
        company.setHasWebsite(rs.getInt("has_website") == 1);
        company.setCreatedAt(rs.getString("created_at"));
        return company;
    }

    public void removeCompany(String companyName) throws SQLException {
        String sql = "DELETE FROM companies WHERE name = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, companyName);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Company not found: " + companyName);
            }

            System.out.println("Removed company: " + companyName);
        }
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("Database connection closed");
        }
    }
}
