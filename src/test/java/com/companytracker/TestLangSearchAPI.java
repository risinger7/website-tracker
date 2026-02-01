package com.companytracker;

import java.io.IOException;

/**
 * Simple test script to verify LangSearch API connection
 * Run this to test your API key before using the full application
 */
public class TestLangSearchAPI {

    public static void main(String[] args) {
        System.out.println("=== LangSearch API Connection Test ===\n");

        // Create SearchService instance (will load config from .env.local)
        SearchService searchService = new SearchService();

        // Test with a well-known company
        String testCompany = "Microsoft";

        System.out.println("Testing API with company: " + testCompany);
        System.out.println("Searching for: " + testCompany + " official website");
        System.out.println("-".repeat(60));

        try {
            // Make API call
            SearchService.SearchResult result = searchService.searchCompanyWebsite(testCompany);

            // Display results
            System.out.println("\n✓ API Call Successful!\n");
            System.out.println("Results:");
            System.out.println("  Company:     " + testCompany);
            System.out.println("  Has Website: " + (result.isHasWebsite() ? "Yes" : "No"));

            if (result.isHasWebsite()) {
                System.out.println("  Website URL: " + result.getWebsiteUrl());
            }

            System.out.println("\n" + "=".repeat(60));
            System.out.println("✓ LangSearch API is working correctly!");
            System.out.println("You can now run the full application: com.companytracker.App");
            System.out.println("=".repeat(60));

        } catch (IOException e) {
            System.err.println("\n✗ API Call Failed!");
            System.err.println("Error: " + e.getMessage());
            System.err.println("\nPossible issues:");
            System.err.println("1. Check your API key in .env.local");
            System.err.println("2. Verify your internet connection");
            System.err.println("3. Check if the API endpoint is correct");
            System.err.println("\nFull error details:");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("\n✗ Unexpected Error!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
