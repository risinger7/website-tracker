package com.companytracker;

import java.util.List;
import java.util.Scanner;

/**
 * Main application for tracking company websites.
 *
 * Workflow:
 * 1. Search Bolagsfakta for companies by business type
 * 2. Store companies in local database
 * 3. Search for websites of unchecked companies
 * 4. View results and manage data
 */
public class App {
    private final StorageService storage;
    private final BolagsfaktaService bolagsfakta;
    private final SearchService search;
    private final Scanner scanner;

    public App() throws Exception {
        this.storage = new DatabaseService();
        this.bolagsfakta = new BolagsfaktaService();
        this.search = new SearchService();
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println("=== Company Website Tracker ===\n");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> fetchCompanies();
                    case "2" -> checkWebsites();
                    case "3" -> listCompanies();
                    case "4" -> resetCompanies();
                    case "5" -> deleteAllCompanies();
                    case "6" -> running = false;
                    default -> System.out.println("Invalid option.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            System.out.println();
        }

        cleanup();
        System.out.println("Goodbye!");
    }

    private void printMenu() {
        System.out.println("1. Fetch companies from Bolagsfakta");
        System.out.println("2. Check unchecked companies for websites");
        System.out.println("3. List all companies");
        System.out.println("4. Reset all (uncheck companies, clear websites)");
        System.out.println("5. Delete all companies");
        System.out.println("6. Exit");
        System.out.print("Choice: ");
    }

    /**
     * Fetch companies from Bolagsfakta API and store in database.
     */
    private void fetchCompanies() throws Exception {
        System.out.print("Business type to search: ");
        String businessType = scanner.nextLine().trim();
        if (businessType.isEmpty()) {
            System.out.println("Business type cannot be empty.");
            return;
        }

        System.out.print("Number of pages (1-5): ");
        int pages = parseIntOrDefault(scanner.nextLine(), 1);

        System.out.println("\nFetching from Bolagsfakta...\n");

        try {
            List<BolagsfaktaCompany> companies = bolagsfakta.search(businessType, pages, 2000);

            System.out.println("\nStoring " + companies.size() + " companies...");
            for (BolagsfaktaCompany c : companies) {
                storage.addCompany(c.getCompanyName(), c.getAntalAnstallda());
            }
            System.out.println("Done!");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Interrupted.");
        }
    }

    /**
     * Check all unchecked companies for websites using search API.
     */
    private void checkWebsites() throws Exception {
        List<Company> unchecked = storage.getUncheckedCompanies();

        if (unchecked.isEmpty()) {
            System.out.println("No unchecked companies.");
            return;
        }

        System.out.println("Checking " + unchecked.size() + " companies for websites...\n");

        int found = 0;
        for (Company company : unchecked) {
            System.out.print(company.getName() + " ... ");

            SearchService.SearchResult result = search.search(company.getName());

            if (result.hasWebsite()) {
                storage.updateWebsite(company.getId(), result.getWebsiteUrl(), true);
                System.out.println("FOUND: " + result.getWebsiteUrl());
                found++;
            } else {
                storage.updateWebsite(company.getId(), null, false);
                System.out.println("not found");
            }

            Thread.sleep(500);  // Rate limiting
        }

        System.out.println("\nDone! Found " + found + " / " + unchecked.size() + " websites.");
    }

    /**
     * List all companies with their website status.
     */
    private void listCompanies() throws Exception {
        List<Company> companies = storage.getAllCompanies();

        if (companies.isEmpty()) {
            System.out.println("No companies in database.");
            return;
        }

        // Print header
        System.out.println();
        System.out.printf("%-4s %-35s %8s %8s %s%n", "ID", "Name", "Empl.", "Website", "URL");
        System.out.println("-".repeat(90));

        // Print companies
        int withWebsite = 0, withoutWebsite = 0, unchecked = 0;

        for (Company c : companies) {
            String name = truncate(c.getName(), 33);
            String url = c.hasWebsite() ? truncate(c.getWebsite(), 35) : "-";
            String status = !c.isChecked() ? "?" : (c.hasWebsite() ? "Yes" : "No");

            System.out.printf("%-4d %-35s %8.0f %8s %s%n",
                    c.getId(), name, c.getEmployees(), status, url);

            if (!c.isChecked()) unchecked++;
            else if (c.hasWebsite()) withWebsite++;
            else withoutWebsite++;
        }

        // Print summary
        System.out.println("-".repeat(90));
        System.out.printf("Total: %d | With website: %d | Without: %d | Unchecked: %d%n",
                companies.size(), withWebsite, withoutWebsite, unchecked);
    }

    /**
     * Reset all companies to unchecked state (clear website data).
     */
    private void resetCompanies() throws Exception {
        System.out.print("Reset all companies? This clears all website data. (yes/no): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
            System.out.println("Cancelled.");
            return;
        }

        storage.resetAllCompanies();
        System.out.println("All companies reset to unchecked.");
    }

    /**
     * Delete all companies from the database.
     */
    private void deleteAllCompanies() throws Exception {
        System.out.print("DELETE ALL companies? This cannot be undone! (yes/no): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
            System.out.println("Cancelled.");
            return;
        }

        storage.deleteAllCompanies();
        System.out.println("All companies deleted.");
    }

    private void cleanup() {
        try {
            storage.close();
            scanner.close();
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    /** Parse int from string, return default if invalid */
    private int parseIntOrDefault(String s, int defaultValue) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** Truncate string to max length with ellipsis */
    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen - 3) + "...";
    }

    public static void main(String[] args) {
        try {
            new App().run();
        } catch (Exception e) {
            System.err.println("Failed to start: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
