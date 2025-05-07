package service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LocationService {
    private static final String BASE_URL = "https://nominatim.openstreetmap.org/search";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static List<String> searchLocations(String query) {
        List<String> locations = new ArrayList<>();
        try {
            // Encode the query for URL
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String urlString = String.format("%s?format=json&q=%s&limit=5", BASE_URL, encodedQuery);
            
            // Add user agent to comply with Nominatim's usage policy
            URL url = new URL(urlString);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "YourAppName/1.0");
            
            // Parse the JSON response
            JsonNode root = objectMapper.readTree(connection.getInputStream());
            
            // Extract display names from the response
            for (JsonNode node : root) {
                String displayName = node.get("display_name").asText();
                locations.add(displayName);
            }
            
            // Add a small delay to respect the rate limit
            Thread.sleep(1000);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return locations;
    }
} 