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

public class BolagsfaktaService {
    private final OkHttpClient client;

    private static final String BASE_URL = "https://www.bolagsfakta.se";
    private static final String SEARCH_ENDPOINT = "/api/search";

    public BolagsfaktaService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public SearchResponse searchCompanies(String query) throws IOException {
        return searchCompanies(query, 1);
    }

    public SearchResponse searchCompanies(String query, int page) throws IOException {
        return searchCompanies(query, page, null);
    }

    public SearchResponse searchCompanies(String query, int page, int[] employeeFilters) throws IOException {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        StringBuilder url = new StringBuilder(BASE_URL + SEARCH_ENDPOINT + "?what=" + encodedQuery + "&page=" + page);

        if (employeeFilters != null) {
            for (int filter : employeeFilters) {
                url.append("&e=").append(filter);
            }
        }

        Request request = new Request.Builder()
                .url(url.toString())
                .addHeader("Accept", "application/json, text/plain, */*")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .addHeader("Referer", "https://www.bolagsfakta.se/Search?what=" + encodedQuery)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API request failed with code: " + response.code());
            }

            String responseBody = response.body().string();
            return parseSearchResponse(responseBody);
        }
    }

    public List<BolagsfaktaCompany> searchCompaniesWithEmployeeFilter(String query, int maxEmployees) throws IOException {
        SearchResponse response = searchCompanies(query);
        List<BolagsfaktaCompany> filtered = new ArrayList<>();

        for (BolagsfaktaCompany company : response.getCompanies()) {
            if (company.getAntalAnstallda() <= maxEmployees) {
                filtered.add(company);
            }
        }

        return filtered;
    }

    private SearchResponse parseSearchResponse(String jsonResponse) {
        SearchResponse searchResponse = new SearchResponse();
        List<BolagsfaktaCompany> companies = new ArrayList<>();

        try {
            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();

            if (json.has("searchResultItems")) {
                JsonArray items = json.getAsJsonArray("searchResultItems");
                for (JsonElement element : items) {
                    JsonObject item = element.getAsJsonObject();
                    BolagsfaktaCompany company = parseCompany(item);
                    companies.add(company);
                }
            }

            if (json.has("paging")) {
                JsonObject paging = json.getAsJsonObject("paging");
                searchResponse.setTotalPages(getIntOrDefault(paging, "totalPage", 0));
                searchResponse.setTotalCount(getIntOrDefault(paging, "totalCount", 0));
                searchResponse.setCurrentPage(getIntOrDefault(paging, "currentPage", 1));
                searchResponse.setHasNextPage(getBoolOrDefault(paging, "hasNextPage", false));
                searchResponse.setHasPreviousPage(getBoolOrDefault(paging, "hasPreviousPage", false));
            }

        } catch (Exception e) {
            System.err.println("Error parsing response: " + e.getMessage());
        }

        searchResponse.setCompanies(companies);
        return searchResponse;
    }

    private BolagsfaktaCompany parseCompany(JsonObject item) {
        BolagsfaktaCompany company = new BolagsfaktaCompany();

        company.setOrgNr(getStringOrNull(item, "orgNr"));
        company.setPostNr(getStringOrNull(item, "postNr"));
        company.setCompanyCode(getIntOrDefault(item, "companyCode", 0));
        company.setCompanyName(getStringOrNull(item, "companyName"));
        company.setCompanyPopularName(getStringOrNull(item, "companyPopularName"));
        company.setAddress(getStringOrNull(item, "address"));
        company.setCompanyUrl(getStringOrNull(item, "companyUrl"));
        company.setSniText(getStringOrNull(item, "sniText"));
        company.setResponsibleCount(getIntOrDefault(item, "responsibleCount", 0));
        company.setOmsattning(getDoubleOrDefault(item, "omsattning", 0.0));
        company.setAntalAnstallda(getDoubleOrDefault(item, "antalAnstallda", 0.0));
        company.setBolagsform(getStringOrNull(item, "bolagsform"));
        company.setPostOrt(getStringOrNull(item, "postOrt"));

        return company;
    }

    private String getStringOrNull(JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return null;
    }

    private int getIntOrDefault(JsonObject obj, String key, int defaultValue) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsInt();
        }
        return defaultValue;
    }

    private double getDoubleOrDefault(JsonObject obj, String key, double defaultValue) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsDouble();
        }
        return defaultValue;
    }

    private boolean getBoolOrDefault(JsonObject obj, String key, boolean defaultValue) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsBoolean();
        }
        return defaultValue;
    }

    public static class SearchResponse {
        private List<BolagsfaktaCompany> companies;
        private int totalPages;
        private int totalCount;
        private int currentPage;
        private boolean hasNextPage;
        private boolean hasPreviousPage;

        public List<BolagsfaktaCompany> getCompanies() {
            return companies;
        }

        public void setCompanies(List<BolagsfaktaCompany> companies) {
            this.companies = companies;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        public boolean isHasNextPage() {
            return hasNextPage;
        }

        public void setHasNextPage(boolean hasNextPage) {
            this.hasNextPage = hasNextPage;
        }

        public boolean isHasPreviousPage() {
            return hasPreviousPage;
        }

        public void setHasPreviousPage(boolean hasPreviousPage) {
            this.hasPreviousPage = hasPreviousPage;
        }
    }

    public static void main(String[] args) {
        BolagsfaktaService service = new BolagsfaktaService();

        try {
            System.out.println("Searching for 'Frisör' with employee filter (0-9 employees)...");
            // e=0 means 0 employees, e=1 means 1-9 employees
            int[] employeeFilter = {0, 1};
            SearchResponse response = service.searchCompanies("Frisör", 1, employeeFilter);

            System.out.println("\n=== Search Results ===");
            System.out.println("Total count: " + response.getTotalCount());
            System.out.println("Total pages: " + response.getTotalPages());
            System.out.println("Current page: " + response.getCurrentPage());
            System.out.println("Has next page: " + response.isHasNextPage());
            System.out.println("\nCompanies found on this page: " + response.getCompanies().size());

            System.out.println("\n=== First 5 Companies ===");
            int count = 0;
            for (BolagsfaktaCompany company : response.getCompanies()) {
                if (count >= 5) break;
                System.out.println("- " + company.getCompanyName() +
                        " (Anställda: " + company.getAntalAnstallda() +
                        ", Org: " + company.getOrgNr() +
                        ", Bransch: " + company.getSniText() + ")");
                count++;
            }

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
