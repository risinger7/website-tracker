package com.companytracker;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service for fetching company data from Bolagsfakta.se API.
 * Retrieves company names and employee counts by business type.
 */
public class BolagsfaktaService {
    private final OkHttpClient client;
    private static final String API_URL = "https://www.bolagsfakta.se/api/search";

    public BolagsfaktaService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Search for companies by business type.
     *
     * @param businessType Type of business to search for (e.g., "Frisör", "Restaurang")
     * @param maxPages     Maximum pages to fetch (1-5)
     * @param delayMs      Delay between page requests in milliseconds
     * @return List of companies with name and employee count
     */
    public List<BolagsfaktaCompany> search(String businessType, int maxPages, long delayMs)
            throws IOException, InterruptedException {

        maxPages = Math.min(Math.max(1, maxPages), 5);  // Clamp to 1-5
        List<BolagsfaktaCompany> allCompanies = new ArrayList<>();

        for (int page = 1; page <= maxPages; page++) {
            System.out.println("Fetching page " + page + "...");

            List<BolagsfaktaCompany> pageCompanies = fetchPage(businessType, page);
            allCompanies.addAll(pageCompanies);

            System.out.println("  Found " + pageCompanies.size() + " companies (total: " + allCompanies.size() + ")");

            // Check if there are more pages
            if (pageCompanies.isEmpty()) {
                break;
            }

            // Delay between requests (except after last page)
            if (page < maxPages) {
                Thread.sleep(delayMs);
            }
        }

        return allCompanies;
    }

    /** Fetch a single page of results from the API */
    private List<BolagsfaktaCompany> fetchPage(String businessType, int page) throws IOException {
        String encodedQuery = URLEncoder.encode(businessType, StandardCharsets.UTF_8);
        String url = API_URL + "?what=" + encodedQuery + "&page=" + page;

        // Cloudflare requires realistic browser headers to allow the request
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json, text/plain, */*")
                .addHeader("Accept-Language", "sv-SE,sv;q=0.9,en-US;q=0.8,en;q=0.7")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .addHeader("Referer", "https://www.bolagsfakta.se/Search?what=" + encodedQuery)
                .addHeader("Origin", "https://www.bolagsfakta.se")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API error: " + response.code());
            }

            String body = response.body().string();

            // Guard against Cloudflare returning HTML instead of JSON
            if (!body.startsWith("{")) {
                System.out.println("  Warning: unexpected response from Bolagsfakta (not JSON)");
                System.out.println("  First 200 chars: " + body.substring(0, Math.min(200, body.length())));
                return new ArrayList<>();
            }

            return parseResponse(body);
        }
    }

    /** Parse API response JSON into list of companies */
    private List<BolagsfaktaCompany> parseResponse(String json) {
        List<BolagsfaktaCompany> companies = new ArrayList<>();

        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        if (!root.has("searchResultItems")) {
            return companies;
        }

        JsonArray items = root.getAsJsonArray("searchResultItems");
        for (JsonElement element : items) {
            JsonObject item = element.getAsJsonObject();
            BolagsfaktaCompany company = new BolagsfaktaCompany();

            if (item.has("companyName") && !item.get("companyName").isJsonNull()) {
                company.setCompanyName(item.get("companyName").getAsString());
            }
            if (item.has("antalAnstallda") && !item.get("antalAnstallda").isJsonNull()) {
                company.setAntalAnstallda(item.get("antalAnstallda").getAsDouble());
            }

            if (company.getCompanyName() != null) {
                companies.add(company);
            }
        }

        return companies;
    }
}
