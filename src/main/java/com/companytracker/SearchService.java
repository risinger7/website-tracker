package com.companytracker;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for searching company websites using LangSearch API.
 * Uses strict matching to ensure the URL domain contains all words from the company name.
 */
public class SearchService {
    private final OkHttpClient client;
    private final String apiKey;

    private static final String API_URL = "https://api.langsearch.com/v1/web-search";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // Common Swedish company suffixes to ignore when matching
    private static final String[] IGNORE_WORDS = {
        "ab", "hb", "kb", "ek", "for", "i", "och", "aktiebolag", "handelsbolag"
    };

    public SearchService() {
        this.client = new OkHttpClient();
        this.apiKey = loadApiKey();
    }

    /** Load API key from .env.local or .env file */
    private String loadApiKey() {
        try {
            // Try .env.local first
            Dotenv dotenv = Dotenv.configure()
                    .filename(".env.local")
                    .ignoreIfMissing()
                    .load();
            String key = dotenv.get("LANGSEARCH_API_KEY");

            // Fall back to .env
            if (key == null) {
                dotenv = Dotenv.configure()
                        .filename(".env")
                        .ignoreIfMissing()
                        .load();
                key = dotenv.get("LANGSEARCH_API_KEY");
            }

            if (key != null && !key.equals("your_api_key_here")) {
                return key;
            }
        } catch (Exception e) {
            // Ignore, will return null
        }
        System.out.println("Warning: LANGSEARCH_API_KEY not found in .env.local or .env");
        return null;
    }

    /**
     * Search for a company's website.
     * Returns the first URL from search results that matches ALL words in the company name.
     *
     * @param companyName The company name to search for
     * @return SearchResult with website URL if found, or empty result if no match
     */
    public SearchResult search(String companyName) throws IOException {
        if (apiKey == null) {
            return new SearchResult(null); // No API key configured
        }

        // Call LangSearch API
        List<String> urls = callSearchApi(companyName + " website");

        // Find URL that matches the company name
        String matchedUrl = findMatchingUrl(companyName, urls);

        return new SearchResult(matchedUrl);
    }

    /** Call LangSearch API and return list of URLs from results */
    private List<String> callSearchApi(String query) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("query", query);
        body.addProperty("freshness", "noLimit");
        body.addProperty("count", 10);

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API error: " + response.code());
            }
            return parseUrls(response.body().string());
        }
    }

    /** Parse URL list from API response JSON */
    private List<String> parseUrls(String json) {
        List<String> urls = new ArrayList<>();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        // LangSearch wraps response in "data" object
        JsonObject data = root.has("data") ? root.getAsJsonObject("data") : root;

        if (data.has("webPages")) {
            JsonObject webPages = data.getAsJsonObject("webPages");
            if (webPages.has("value")) {
                JsonArray results = webPages.getAsJsonArray("value");
                for (int i = 0; i < results.size(); i++) {
                    urls.add(results.get(i).getAsJsonObject().get("url").getAsString());
                }
            }
        }
        return urls;
    }

    /**
     * Find URL where domain contains ALL significant words from company name.
     * Example: "Jakob Snickare" matches "jakobsnickare.se" but NOT "snickarenacka.se"
     */
    private String findMatchingUrl(String companyName, List<String> urls) {
        // Extract significant words from company name
        List<String> words = getSignificantWords(companyName);
        if (words.isEmpty()) {
            return null;
        }

        // Check each URL for a match
        for (String url : urls) {
            String domain = extractDomain(url);
            if (domainContainsAllWords(domain, words)) {
                return url;
            }
        }
        return null;
    }

    /** Extract significant words from company name (ignoring common suffixes).
     *  Swedish chars are normalized (å→a, ä→a, ö→o) so "Frisör" matches "frisor.se" */
    private List<String> getSignificantWords(String companyName) {
        List<String> words = new ArrayList<>();
        String[] parts = normalizeSwedish(companyName.toLowerCase())
                .replaceAll("[^a-z0-9\\s]", "")
                .split("\\s+");

        for (String word : parts) {
            if (word.length() > 1 && !isIgnoredWord(word)) {
                words.add(word);
            }
        }
        return words;
    }

    /** Replace Swedish chars with ASCII equivalents */
    private String normalizeSwedish(String text) {
        return text.replace("å", "a")
                   .replace("ä", "a")
                   .replace("ö", "o")
                   .replace("é", "e")
                   .replace("ü", "u");
    }

    /** Check if word is a common suffix that should be ignored */
    private boolean isIgnoredWord(String word) {
        for (String ignored : IGNORE_WORDS) {
            if (word.equals(ignored)) return true;
        }
        return false;
    }

    /** Extract domain from URL (without protocol, www, and TLD) */
    private String extractDomain(String url) {
        String domain = url.toLowerCase()
                .replaceAll("^https?://", "")
                .replaceAll("^www\\.", "");

        // Remove path
        int slash = domain.indexOf('/');
        if (slash > 0) domain = domain.substring(0, slash);

        // Remove TLD
        int dot = domain.lastIndexOf('.');
        if (dot > 0) domain = domain.substring(0, dot);

        return domain;
    }

    /** Check if domain contains all the given words */
    private boolean domainContainsAllWords(String domain, List<String> words) {
        for (String word : words) {
            if (!domain.contains(word)) return false;
        }
        return true;
    }

    /**
     * Result of a website search.
     */
    public static class SearchResult {
        private final String websiteUrl;

        public SearchResult(String websiteUrl) {
            this.websiteUrl = websiteUrl;
        }

        public boolean hasWebsite() {
            return websiteUrl != null;
        }

        public String getWebsiteUrl() {
            return websiteUrl;
        }
    }
}
