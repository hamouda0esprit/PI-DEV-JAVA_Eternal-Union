package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class AI21Service {
    private static final String API_KEY = "h7GGimFtDFLSl7t4LGPsyAiHAk8NouzM";
    //private static final String API_KEY = "";
    private static final String API_URL = "https://api.ai21.com/studio/v1/chat/completions";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    // Singleton instance
    private static AI21Service instance;
    
    public AI21Service() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }
    
    // Static method to get the singleton instance
    public static AI21Service getInstance() {
        if (instance == null) {
            instance = new AI21Service();
        }
        return instance;
    }

    /**
     * Generates text using AI21's API.
     *
     * @param inputText The text prompt to send to the AI21 API
     * @return The generated text or an error message
     */
    public String generateText(String inputText) {
        try {
            // Create the request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "jamba-1.5-large");
            requestBody.put("messages", List.of(Map.of(
                "role", "user",
                "content", inputText
            )));
            requestBody.put("n", 1);
            requestBody.put("max_tokens", 2048);
            requestBody.put("temperature", 0.2);
            requestBody.put("top_p", 1);
            requestBody.put("stop", List.of());
            requestBody.put("response_format", Map.of("type", "text"));

            // Create the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                    objectMapper.writeValueAsString(requestBody)
                ))
                .build();

            // Send the request and get the response
            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());

            // Check the status code
            if (response.statusCode() != 200) {
                return "Error: Received status code " + response.statusCode() + 
                       ". Response: " + response.body();
            }

            // Parse the response
            Map<String, Object> responseData = objectMapper.readValue(
                response.body(), Map.class);

            // Extract the generated text
            if (responseData.containsKey("choices") && 
                ((List<?>)responseData.get("choices")).size() > 0) {
                Map<?, ?> choice = (Map<?, ?>)((List<?>)responseData.get("choices")).get(0);
                if (choice.containsKey("message")) {
                    Map<?, ?> message = (Map<?, ?>)choice.get("message");
                    if (message.containsKey("content")) {
                        return (String)message.get("content");
                    }
                }
            }

            return "Error: Unexpected response structure";

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
