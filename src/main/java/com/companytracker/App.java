package com.companytracker;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class App {
    private StorageService storageService;
    private SearchService searchService;
    private Scanner scanner;

    public App() throws IOException {
        this.storageService = new CSVService();
        this.searchService = new SearchService();
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println("=== Company Website Checker ===");
        System.out.println("Welcome! This app helps you track companies and check if they have websites.\n");

        boolean running = true;
        while (running) {
            displayMenu();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        addCompany();
                        break;
                    case "2":
                        checkCompanyWebsite();
                        break;
                    case "3":
                        listAllCompanies();
                        break;
                    case "4":
                        checkAllCompanies();
                        break;
                    case "5":
                        running = false;
                        System.out.println("Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println();
        }

        cleanup();
    }

    private void displayMenu() {
        System.out.println("Choose an option:");
        System.out.println("1. Add a company");
        System.out.println("2. Check if a company has a website");
        System.out.println("3. List all companies");
        System.out.println("4. Check all companies for websites");
        System.out.println("5. Exit");
        System.out.print("Your choice: ");
    }

    private void addCompany() throws Exception {
        System.out.print("Enter company name: ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println("Company name cannot be empty.");
            return;
        }

        Company company = new Company(name);
        storageService.addCompany(company);
        System.out.println("Company added successfully!");
    }

    private void checkCompanyWebsite() throws Exception {
        System.out.print("Enter company name: ");
        String name = scanner.nextLine().trim();

        Company company = storageService.getCompanyByName(name);
        if (company == null) {
            System.out.println("Company not found. Would you like to add it first? (y/n)");
            String response = scanner.nextLine().trim().toLowerCase();
            if (response.equals("y")) {
                addCompany();
                company = storageService.getCompanyByName(name);
            } else {
                return;
            }
        }

        System.out.println("Searching for website...");
        SearchService.SearchResult result = searchService.searchCompanyWebsite(name);

        storageService.updateCompanyWebsite(name, result.getWebsiteUrl(), result.isHasWebsite());

        if (result.isHasWebsite()) {
            System.out.println("✓ Website found: " + result.getWebsiteUrl());
        } else {
            System.out.println("✗ No website found for this company.");
        }
    }

    private void listAllCompanies() throws Exception {
        List<Company> companies = storageService.getAllCompanies();

        if (companies.isEmpty()) {
            System.out.println("No companies in storage yet.");
            return;
        }

        System.out.println("\n=== All Companies ===");
        System.out.println(String.format("%-5s %-30s %-10s %-50s", "ID", "Name", "Website?", "URL"));
        System.out.println("-".repeat(100));

        for (Company company : companies) {
            System.out.println(String.format(
                    "%-5d %-30s %-10s %-50s",
                    company.getId(),
                    company.getName(),
                    company.isHasWebsite() ? "Yes" : "No",
                    company.getWebsite() != null ? company.getWebsite() : "N/A"
            ));
        }

        System.out.println("\nTotal companies: " + companies.size());
    }

    private void checkAllCompanies() throws Exception {
        List<Company> companies = storageService.getAllCompanies();

        if (companies.isEmpty()) {
            System.out.println("No companies to check.");
            return;
        }

        System.out.println("Checking websites for all companies...\n");

        for (Company company : companies) {
            System.out.println("Checking: " + company.getName());
            SearchService.SearchResult result = searchService.searchCompanyWebsite(company.getName());
            storageService.updateCompanyWebsite(company.getName(), result.getWebsiteUrl(), result.isHasWebsite());

            if (result.isHasWebsite()) {
                System.out.println("  ✓ Found: " + result.getWebsiteUrl());
            } else {
                System.out.println("  ✗ Not found");
            }

            // Small delay to avoid rate limiting
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("\nFinished checking all companies!");
    }

    private void cleanup() {
        try {
            storageService.close();
            scanner.close();
        } catch (Exception e) {
            System.out.println("Error closing storage: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            App app = new App();
            app.run();
        } catch (Exception e) {
            System.out.println("Failed to initialize storage: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
