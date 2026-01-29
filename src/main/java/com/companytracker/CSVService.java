package com.companytracker;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CSVService implements StorageService {
    private static final String CSV_FILE = "companies.csv";
    private static final String HEADER = "id,name,website,has_website,created_at";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CSVService() throws IOException {
        initializeFile();
    }

    private void initializeFile() throws IOException {
        File file = new File(CSV_FILE);
        if (!file.exists()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(HEADER);
                writer.newLine();
                System.out.println("Created CSV file: " + CSV_FILE);
            }
        } else {
            System.out.println("Connected to CSV file: " + CSV_FILE);
        }
    }

    @Override
    public void addCompany(Company company) throws IOException {
        List<Company> companies = getAllCompanies();

        // Check for duplicates
        for (Company existing : companies) {
            if (existing.getName().equalsIgnoreCase(company.getName())) {
                throw new IOException("Company already exists: " + company.getName());
            }
        }

        // Generate new ID
        int newId = companies.isEmpty() ? 1 : companies.get(companies.size() - 1).getId() + 1;
        company.setId(newId);
        company.setCreatedAt(LocalDateTime.now().format(DATE_FORMATTER));

        // Append to file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE, true))) {
            writer.write(companyToCsvLine(company));
            writer.newLine();
        }

        System.out.println("Added company: " + company.getName());
    }

    @Override
    public void updateCompanyWebsite(String companyName, String website, boolean hasWebsite) throws IOException {
        List<Company> companies = getAllCompanies();
        boolean found = false;

        for (Company company : companies) {
            if (company.getName().equalsIgnoreCase(companyName)) {
                company.setWebsite(website);
                company.setHasWebsite(hasWebsite);
                found = true;
                break;
            }
        }

        if (!found) {
            throw new IOException("Company not found: " + companyName);
        }

        // Rewrite entire file with updated data
        writeAllCompanies(companies);
        System.out.println("Updated website info for: " + companyName);
    }

    @Override
    public List<Company> getAllCompanies() throws IOException {
        List<Company> companies = new ArrayList<>();
        File file = new File(CSV_FILE);

        if (!file.exists()) {
            return companies;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // Skip header

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    Company company = parseCsvLine(line);
                    companies.add(company);
                } catch (Exception e) {
                    System.err.println("Error parsing line: " + line + " - " + e.getMessage());
                }
            }
        }

        return companies;
    }

    @Override
    public Company getCompanyByName(String name) throws IOException {
        List<Company> companies = getAllCompanies();

        for (Company company : companies) {
            if (company.getName().equalsIgnoreCase(name)) {
                return company;
            }
        }

        return null;
    }

    @Override
    public void removeCompany(String companyName) throws IOException {
        List<Company> companies = getAllCompanies();
        boolean found = false;

        for (int i = 0; i < companies.size(); i++) {
            if (companies.get(i).getName().equalsIgnoreCase(companyName)) {
                companies.remove(i);
                found = true;
                break;
            }
        }

        if (!found) {
            throw new IOException("Company not found: " + companyName);
        }

        // Rewrite entire file with remaining companies
        writeAllCompanies(companies);
        System.out.println("Removed company: " + companyName);
    }

    @Override
    public void close() {
        System.out.println("CSV file operations completed");
    }

    private Company parseCsvLine(String line) {
        String[] parts = line.split(",", -1);

        Company company = new Company();
        company.setId(Integer.parseInt(parts[0].trim()));
        company.setName(parts[1].trim());
        company.setWebsite(parts[2].trim().isEmpty() ? null : parts[2].trim());
        company.setHasWebsite(parts[3].trim().equalsIgnoreCase("true") || parts[3].trim().equals("1"));
        company.setCreatedAt(parts[4].trim());

        return company;
    }

    private String companyToCsvLine(Company company) {
        return String.format("%d,%s,%s,%s,%s",
                company.getId(),
                escapeCsv(company.getName()),
                company.getWebsite() != null ? escapeCsv(company.getWebsite()) : "",
                company.isHasWebsite() ? "true" : "false",
                company.getCreatedAt() != null ? company.getCreatedAt() : ""
        );
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // If value contains comma, quote, or newline, wrap in quotes and escape quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void writeAllCompanies(List<Company> companies) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE))) {
            writer.write(HEADER);
            writer.newLine();

            for (Company company : companies) {
                writer.write(companyToCsvLine(company));
                writer.newLine();
            }
        }
    }
}
