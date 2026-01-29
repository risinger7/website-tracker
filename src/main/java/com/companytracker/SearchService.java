package com.companytracker;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class SearchService {
    private final OkHttpClient client;
    private String apiKey;
    private String searchEngineId;

    public SearchService() {
        this.client = new OkHttpClient();
        // TODO: Set your API credentials here or load from config file
        // this.apiKey = "YOUR_API_KEY";
        // this.searchEngineId = "YOUR_SEARCH_ENGINE_ID";
    }

    public SearchService(String apiKey, String searchEngineId) {
        this.client = new OkHttpClient();
        this.apiKey = apiKey;
        this.searchEngineId = searchEngineId;
    }

    /**
     * Search for a company using Google Custom Search API
     * You'll need to:
     * 1. Get an API key from: https://developers.google.com/custom-search/v1/overview
     * 2. Create a custom search engine: https://programmablesearchengine.google.com/
     */
    public SearchResult searchCompanyWebsite(String companyName) throws IOException {
        if (apiKey == null || searchEngineId == null) {
            System.out.println("Warning: API credentials not set. Using mock search results.");
            return mockSearch(companyName);
        }

        String url = String.format(
                "https://www.googleapis.com/customsearch/v1?key=%s&cx=%s&q=%s",
                apiKey,
                searchEngineId,
                companyName.replace(" ", "+")
        );

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }

            String responseBody = response.body().string();
            return parseSearchResponse(responseBody, companyName);
        }
    }

    private SearchResult parseSearchResponse(String jsonResponse, String companyName) {
        JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
        SearchResult result = new SearchResult();
        result.setCompanyName(companyName);

        if (json.has("items") && json.getAsJsonArray("items").size() > 0) {
            JsonObject firstResult = json.getAsJsonArray("items").get(0).getAsJsonObject();
            String link = firstResult.get("link").getAsString();

            result.setHasWebsite(true);
            result.setWebsiteUrl(link);
        } else {
            result.setHasWebsite(false);
        }

        return result;
    }

    /**
     * Mock search for testing without API credentials
     */
    private SearchResult mockSearch(String companyName) {
        SearchResult result = new SearchResult();
        result.setCompanyName(companyName);

        // Simple mock: assume companies have websites
        result.setHasWebsite(true);
        result.setWebsiteUrl("https://www." + companyName.toLowerCase().replace(" ", "") + ".com");

        return result;
    }

    public static class SearchResult {
        private String companyName;
        private boolean hasWebsite;
        private String websiteUrl;

        public String getCompanyName() {
            return companyName;
        }

        public void setCompanyName(String companyName) {
            this.companyName = companyName;
        }

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

        @Override
        public String toString() {
            return "SearchResult{" +
                    "companyName='" + companyName + '\'' +
                    ", hasWebsite=" + hasWebsite +
                    ", websiteUrl='" + websiteUrl + '\'' +
                    '}';
        }
    }
}
