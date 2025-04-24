package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class ProfanityFilterService {
    private static final String API_KEY = "";
    //private static final String API_KEY = "XmxRXGY6S1LgnEco4pQetQ==GBVAmTcFIGr1flRa";
    private static final String API_URL = "https://api.api-ninjas.com/v1/profanityfilter";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ProfanityFilterService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public String filterText(String text) throws Exception {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Input text cannot be null or empty");
        }

        try {
            String encodedText = java.net.URLEncoder.encode(text, "UTF-8");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "?text=" + encodedText))
                    .header("X-Api-Key", API_KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            //System.out.println("API Response Status: " + response.statusCode());
            //System.out.println("API Response Body: " + response.body());

            if (response.statusCode() != 200) {
                throw new Exception("API request failed with status: " + response.statusCode() + 
                                  ", Response: " + response.body());
            }

            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            String filteredText = (String) result.get("censored");
            
            if (filteredText == null || filteredText.trim().isEmpty()) {
                // If the API returns null or empty, return the original text
                System.out.println("Warning: API returned null or empty filtered text. Using original text.");
                return text;
            }
            
            return filteredText;
        } catch (Exception e) {
            System.err.println("Error in profanity filter: " + e.getMessage());
            e.printStackTrace();
            // If there's an error with the API, return the original text
            return text;
        }
    }
} 