package com.companytracker;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class CompanyNameMatcher {

    private static final Set<String> COMPANY_SUFFIXES = new HashSet<>(Arrays.asList(
            "ab", "hb", "kb", "ek", "for", "enskild", "firma", "handelsbolag",
            "kommanditbolag", "aktiebolag", "ekonomisk", "forening"
    ));

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "och", "i", "the", "and", "of", "sweden", "sverige"
    ));

    public static boolean urlMatchesCompanyName(String url, String companyName) {
        String normalizedUrl = normalizeForMatching(extractDomainFromUrl(url));
        String normalizedName = normalizeCompanyName(companyName);

        // Direct match
        if (normalizedUrl.contains(normalizedName)) {
            return true;
        }

        // Check if significant words from company name are in URL
        String[] words = normalizedName.split("\\s+");
        int significantMatches = 0;
        int significantWords = 0;

        for (String word : words) {
            if (word.length() >= 3 && !STOP_WORDS.contains(word)) {
                significantWords++;
                if (normalizedUrl.contains(word)) {
                    significantMatches++;
                }
            }
        }

        // Match if at least one significant word matches and it's substantial
        if (significantWords > 0 && significantMatches > 0) {
            // For single-word names, require exact match
            if (significantWords == 1) {
                return normalizedUrl.contains(words[0]);
            }
            // For multi-word names, at least 50% of significant words should match
            return (double) significantMatches / significantWords >= 0.5;
        }

        return false;
    }

    public static String normalizeCompanyName(String name) {
        if (name == null) return "";

        String normalized = name.toLowerCase();

        // Remove company suffixes
        for (String suffix : COMPANY_SUFFIXES) {
            normalized = normalized.replaceAll("\\b" + suffix + "\\b", "");
        }

        // Normalize Swedish characters
        normalized = normalizeSwedish(normalized);

        // Remove special characters, keep only letters and spaces
        normalized = normalized.replaceAll("[^a-z0-9\\s]", "");

        // Collapse multiple spaces
        normalized = normalized.replaceAll("\\s+", " ").trim();

        return normalized;
    }

    public static String normalizeForMatching(String text) {
        if (text == null) return "";

        String normalized = text.toLowerCase();
        normalized = normalizeSwedish(normalized);
        normalized = normalized.replaceAll("[^a-z0-9]", "");

        return normalized;
    }

    private static String normalizeSwedish(String text) {
        // Replace Swedish characters with ASCII equivalents
        text = text.replace("å", "a")
                   .replace("ä", "a")
                   .replace("ö", "o")
                   .replace("é", "e")
                   .replace("è", "e")
                   .replace("ü", "u");

        // Also handle uppercase versions
        text = text.replace("Å", "a")
                   .replace("Ä", "a")
                   .replace("Ö", "o");

        // Use Normalizer to handle any remaining special characters
        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        text = pattern.matcher(text).replaceAll("");

        return text;
    }

    public static String extractDomainFromUrl(String url) {
        if (url == null) return "";

        // Remove protocol
        String domain = url.replaceFirst("^https?://", "");

        // Remove www.
        domain = domain.replaceFirst("^www\\.", "");

        // Remove path (everything after first /)
        int slashIndex = domain.indexOf('/');
        if (slashIndex > 0) {
            domain = domain.substring(0, slashIndex);
        }

        // Remove port if present
        int colonIndex = domain.indexOf(':');
        if (colonIndex > 0) {
            domain = domain.substring(0, colonIndex);
        }

        return domain;
    }

    public static MatchResult findMatchingUrl(java.util.List<String> urls, String companyName) {
        for (String url : urls) {
            if (urlMatchesCompanyName(url, companyName)) {
                return new MatchResult(true, url);
            }
        }
        return new MatchResult(false, null);
    }

    public static class MatchResult {
        private final boolean matched;
        private final String matchedUrl;

        public MatchResult(boolean matched, String matchedUrl) {
            this.matched = matched;
            this.matchedUrl = matchedUrl;
        }

        public boolean isMatched() {
            return matched;
        }

        public String getMatchedUrl() {
            return matchedUrl;
        }
    }

    public static void main(String[] args) {
        // Test the matcher
        System.out.println("=== Testing CompanyNameMatcher ===\n");

        testMatch("K Frisör AB", "https://www.kfrisor.se");
        testMatch("K Frisör AB", "https://www.google.com");
        testMatch("Linlugg frisör AB", "https://www.linlugg.se");
        testMatch("ByChris.se Frisör AB", "https://www.bychris.se");
        testMatch("RS Frisör AB", "https://www.rsfrisor.se");
        testMatch("ELON Group AB", "https://www.elon.se");
        testMatch("ELON Group AB", "https://www.elongroup.com");
        testMatch("Sami frisör AB", "https://www.eniro.se/sami-frisor");
    }

    private static void testMatch(String companyName, String url) {
        boolean matches = urlMatchesCompanyName(url, companyName);
        String normalizedName = normalizeCompanyName(companyName);
        String normalizedUrl = normalizeForMatching(extractDomainFromUrl(url));

        System.out.println("Company: " + companyName);
        System.out.println("URL: " + url);
        System.out.println("Normalized name: '" + normalizedName + "'");
        System.out.println("Normalized URL: '" + normalizedUrl + "'");
        System.out.println("Match: " + (matches ? "YES" : "NO"));
        System.out.println();
    }
}
