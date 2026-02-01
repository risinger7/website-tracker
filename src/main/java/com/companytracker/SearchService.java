package com.companytracker;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SearchService {
    private final OkHttpClient client;
    private String apiKey;
    private String apiUrl;
    private String freshness;
    private boolean summary;
    private int resultsCount;

    private static final String DEFAULT_API_URL = "https://api.langsearch.com/v1/web-search";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public SearchService() {
        this.client = new OkHttpClient();
        loadConfiguration();
    }

    public SearchService(String apiKey) {
        this.client = new OkHttpClient();
        this.apiKey = apiKey;
        this.apiUrl = DEFAULT_API_URL;
        this.freshness = "noLimit";
        this.summary = true;
        this.resultsCount = 10;
    }

    /**
     * Load configuration from environment variables (.env.local or .env) first,
     * then fall back to config.properties if not found
     */
    private void loadConfiguration() {
        // Try to load from .env.local first, then .env
        Dotenv dotenv = null;
        try {
            dotenv = Dotenv.configure()
                    .filename(".env.local")
                    .ignoreIfMissing()
                    .load();

            // If .env.local doesn't exist or doesn't have the key, try .env
            if (dotenv.get("LANGSEARCH_API_KEY") == null) {
                dotenv = Dotenv.configure()
                        .filename(".env")
                        .ignoreIfMissing()
                        .load();
            }
        } catch (Exception e) {
            // Dotenv not found, will try config.properties next
        }

        // Load from environment variables first
        if (dotenv != null && dotenv.get("LANGSEARCH_API_KEY") != null) {
            this.apiKey = dotenv.get("LANGSEARCH_API_KEY");
            this.apiUrl = dotenv.get("LANGSEARCH_API_URL", DEFAULT_API_URL);
            this.freshness = dotenv.get("LANGSEARCH_FRESHNESS", "noLimit");
            this.summary = Boolean.parseBoolean(dotenv.get("LANGSEARCH_SUMMARY", "true"));
            this.resultsCount = Integer.parseInt(dotenv.get("LANGSEARCH_RESULTS_COUNT", "10"));

            if (this.apiKey.equals("your_api_key_here")) {
                System.out.println("Warning: Please update LANGSEARCH_API_KEY in .env.local");
                this.apiKey = null;
            }
        } else {
            // Fall back to config.properties
            loadFromPropertiesFile();
        }
    }

    /**
     * Load configuration from config.properties file (fallback method)
     */
    private void loadFromPropertiesFile() {
        Properties properties = new Properties();

        try (InputStream input = new FileInputStream("config.properties")) {
            properties.load(input);
            this.apiKey = properties.getProperty("langsearch.api.key");
            this.apiUrl = properties.getProperty("langsearch.api.url", DEFAULT_API_URL);
            this.freshness = properties.getProperty("langsearch.freshness", "noLimit");
            this.summary = Boolean.parseBoolean(properties.getProperty("langsearch.summary", "true"));
            this.resultsCount = Integer.parseInt(properties.getProperty("langsearch.results.count", "10"));

            if (this.apiKey == null || this.apiKey.equals("YOUR-API-KEY-HERE")) {
                System.out.println("Warning: LangSearch API key not configured in config.properties");
                this.apiKey = null;
            }
        } catch (IOException e) {
            System.out.println("Warning: No configuration found. Using mock search results.");
            System.out.println("To use LangSearch API:");
            System.out.println("1. Copy .env.example to .env.local and add your API key, OR");
            System.out.println("2. Copy config.properties.example to config.properties and add your API key");
            System.out.println("Get your free API key from: https://langsearch.com/api-keys");
            this.apiKey = null;
        }
    }

    /**
     * Search for a company using LangSearch API
     * LangSearch provides free web search API optimized for AI applications
     * Get your free API key from: https://langsearch.com/api-keys
     */
    public SearchResult searchCompanyWebsite(String companyName) throws IOException {
        if (apiKey == null) {
            System.out.println("Warning: API key not configured. Using mock search results.");
            return mockSearch(companyName);
        }

        // Build search query
        String searchQuery = companyName + " company website";

        // Create JSON request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("query", searchQuery);
        requestBody.addProperty("freshness", freshness);
        requestBody.addProperty("summary", summary);
        requestBody.addProperty("count", resultsCount);

        RequestBody body = RequestBody.create(requestBody.toString(), JSON);

        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error details";
                throw new IOException("LangSearch API error (HTTP " + response.code() + "): " + errorBody);
            }

            String responseBody = response.body().string();
            return parseSearchResponse(responseBody);
        }
    }

    private SearchResult parseSearchResponse(String jsonResponse) {
        JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
        SearchResult result = new SearchResult();

        if (json.has("webPages") && json.getAsJsonObject("webPages").has("value")) {
            JsonArray results = json.getAsJsonObject("webPages").getAsJsonArray("value");

            if (results.size() > 0) {
                JsonObject firstResult = results.get(0).getAsJsonObject();
                result.setHasWebsite(true);
                result.setWebsiteUrl(firstResult.get("url").getAsString());
            }
        }

        return result;
    }

    private SearchResult mockSearch(String companyName) {
        SearchResult result = new SearchResult();
        result.setHasWebsite(true);
        result.setWebsiteUrl("https://www." + companyName.toLowerCase().replace(" ", "") + ".com");
        return result;
    }

    public static class SearchResult {
        private boolean hasWebsite;
        private String websiteUrl;

        public boolean isHasWebsite() {
            return hasWebsite;
        }

        public void setHasWebsite(boolean hasWebsite) {
            this.hasWebsite = hasWebsite;
        }

        public String getWebsiteUrl() {
            return websiteUrl;
        }

        public void setWebsiteUrl(String websiteUrl) {
            this.websiteUrl = websiteUrl;
        }
    }
}
