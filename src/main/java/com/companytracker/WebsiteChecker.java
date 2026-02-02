package com.companytracker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WebsiteChecker {

    private final BolagsfaktaService bolagsfaktaService;
    private final SearchService searchService;
    private int apiCallCount = 0;
    private int maxApiCalls;

    public WebsiteChecker() {
        this.bolagsfaktaService = new BolagsfaktaService();
        this.searchService = new SearchService();
        this.maxApiCalls = 20; // Default limit during testing
    }

    public WebsiteChecker(int maxApiCalls) {
        this.bolagsfaktaService = new BolagsfaktaService();
        this.searchService = new SearchService();
        this.maxApiCalls = maxApiCalls;
    }

    public List<CheckResult> findCompaniesWithoutWebsite(String searchQuery, int[] employeeFilter, int maxCompanies) {
        List<CheckResult> results = new ArrayList<>();

        try {
            System.out.println("Fetching companies from Bolagsfakta...");
            BolagsfaktaService.SearchResponse response =
                bolagsfaktaService.searchCompanies(searchQuery, 1, employeeFilter);

            System.out.println("Found " + response.getTotalCount() + " total companies");
            System.out.println("Checking first " + Math.min(maxCompanies, response.getCompanies().size()) + " companies...\n");

            int checked = 0;
            for (BolagsfaktaCompany company : response.getCompanies()) {
                if (checked >= maxCompanies) break;
                if (apiCallCount >= maxApiCalls) {
                    System.out.println("\n[!] Reached API call limit (" + maxApiCalls + "). Stopping.");
                    break;
                }

                CheckResult result = checkCompanyWebsite(company);
                results.add(result);
                checked++;

                // Small delay to avoid rate limiting
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

        } catch (IOException e) {
            System.err.println("Error fetching companies: " + e.getMessage());
        }

        return results;
    }

    private CheckResult checkCompanyWebsite(BolagsfaktaCompany company) {
        CheckResult result = new CheckResult();
        result.setCompany(company);

        System.out.print("Checking: " + company.getCompanyName() + "... ");

        try {
            apiCallCount++;
            SearchService.SearchResult searchResult =
                searchService.searchCompanyWebsite(company.getCompanyName());

            List<String> urls = searchResult.getAllUrls();
            CompanyNameMatcher.MatchResult matchResult =
                CompanyNameMatcher.findMatchingUrl(urls, company.getCompanyName());

            result.setHasWebsite(matchResult.isMatched());
            result.setMatchedUrl(matchResult.getMatchedUrl());
            result.setSearchUrls(urls);

            if (matchResult.isMatched()) {
                System.out.println("HAS WEBSITE: " + matchResult.getMatchedUrl());
            } else {
                System.out.println("NO WEBSITE FOUND");
                if (!urls.isEmpty()) {
                    System.out.println("  Search returned " + urls.size() + " URLs:");
                    for (int i = 0; i < Math.min(3, urls.size()); i++) {
                        System.out.println("    - " + urls.get(i));
                    }
                } else {
                    System.out.println("  (No URLs returned from search)");
                }
            }

        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
            result.setError(e.getMessage());
        }

        return result;
    }

    public void printSummary(List<CheckResult> results) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("SUMMARY");
        System.out.println("=".repeat(60));

        List<CheckResult> withWebsite = new ArrayList<>();
        List<CheckResult> withoutWebsite = new ArrayList<>();
        List<CheckResult> errors = new ArrayList<>();

        for (CheckResult result : results) {
            if (result.getError() != null) {
                errors.add(result);
            } else if (result.isHasWebsite()) {
                withWebsite.add(result);
            } else {
                withoutWebsite.add(result);
            }
        }

        System.out.println("\nTotal checked: " + results.size());
        System.out.println("With website: " + withWebsite.size());
        System.out.println("Without website: " + withoutWebsite.size());
        System.out.println("Errors: " + errors.size());
        System.out.println("API calls used: " + apiCallCount);

        if (!withoutWebsite.isEmpty()) {
            System.out.println("\n--- Companies WITHOUT a website ---");
            for (CheckResult result : withoutWebsite) {
                BolagsfaktaCompany c = result.getCompany();
                System.out.println("- " + c.getCompanyName());
                System.out.println("  Org.nr: " + c.getOrgNr());
                System.out.println("  Bransch: " + c.getSniText());
                System.out.println("  Adress: " + c.getAddress());
                System.out.println();
            }
        }

        if (!withWebsite.isEmpty()) {
            System.out.println("\n--- Companies WITH a website ---");
            for (CheckResult result : withWebsite) {
                BolagsfaktaCompany c = result.getCompany();
                System.out.println("- " + c.getCompanyName() + " -> " + result.getMatchedUrl());
            }
        }
    }

    public int getApiCallCount() {
        return apiCallCount;
    }

    public static class CheckResult {
        private BolagsfaktaCompany company;
        private boolean hasWebsite;
        private String matchedUrl;
        private List<String> searchUrls;
        private String error;

        public BolagsfaktaCompany getCompany() {
            return company;
        }

        public void setCompany(BolagsfaktaCompany company) {
            this.company = company;
        }

        public boolean isHasWebsite() {
            return hasWebsite;
        }

        public void setHasWebsite(boolean hasWebsite) {
            this.hasWebsite = hasWebsite;
        }

        public String getMatchedUrl() {
            return matchedUrl;
        }

        public void setMatchedUrl(String matchedUrl) {
            this.matchedUrl = matchedUrl;
        }

        public List<String> getSearchUrls() {
            return searchUrls;
        }

        public void setSearchUrls(List<String> searchUrls) {
            this.searchUrls = searchUrls;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("Website Checker - Finding small companies without websites");
        System.out.println("=".repeat(60));
        System.out.println();

        // Limit to 5 companies during initial testing (5 LangSearch API calls)
        int maxCompaniesToCheck = 5;
        int maxApiCalls = 20;

        WebsiteChecker checker = new WebsiteChecker(maxApiCalls);

        // Search for small companies (0-9 employees)
        // e=0 means 0 employees, e=1 means 1-9 employees
        int[] employeeFilter = {0, 1};

        List<CheckResult> results = checker.findCompaniesWithoutWebsite(
            "Frisör",
            employeeFilter,
            maxCompaniesToCheck
        );

        checker.printSummary(results);
    }
}
