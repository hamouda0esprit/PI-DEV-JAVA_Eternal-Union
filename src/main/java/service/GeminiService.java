package service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * Service for interacting with Google's Gemini API
 */
public class GeminiService {
    private final HttpClient httpClient;
    private final String apiKey;
    
    /**
     * Constructor with API key
     * @param apiKey Gemini API key
     */
    public GeminiService(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }
    
    /**
     * Generate a quiz using Gemini AI
     * @param subject Subject of the quiz
     * @param difficulty Difficulty level
     * @param questionCount Number of questions
     * @param additionalInstructions Additional instructions
     * @return JSON string with the quiz data
     * @throws IOException If an error occurs during the request
     * @throws InterruptedException If the request is interrupted
     */
    public JSONObject generateQuizWithGemini(String subject, String difficulty, int questionCount, 
            String additionalInstructions) throws IOException, InterruptedException {
        
        String endpoint = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-pro:generateContent?key=" + apiKey;
        
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Génère un quiz de ").append(questionCount).append(" questions à choix multiples sur le sujet: ").append(subject).append(". ");
        promptBuilder.append("Niveau de difficulté: ").append(difficulty).append(". ");
        
        if (additionalInstructions != null && !additionalInstructions.isEmpty()) {
            promptBuilder.append("Instructions supplémentaires: ").append(additionalInstructions).append(". ");
        }
        
        promptBuilder.append("Pour chaque question, inclus une explication détaillée de la réponse correcte. ");
        promptBuilder.append("Format de réponse: JSON avec la structure suivante:\n");
        promptBuilder.append("{\n");
        promptBuilder.append("    \"questions\": [\n");
        promptBuilder.append("        {\n");
        promptBuilder.append("            \"question\": \"Texte de la question\",\n");
        promptBuilder.append("            \"options\": [\"Option A\", \"Option B\", \"Option C\", \"Option D\"],\n");
        promptBuilder.append("            \"correctIndex\": 0, // Index de la bonne réponse (0 pour A, 1 pour B, etc.)\n");
        promptBuilder.append("            \"explanation\": \"Explication détaillée de la réponse correcte\"\n");
        promptBuilder.append("        }\n");
        promptBuilder.append("    ]\n");
        promptBuilder.append("}");
        
        String prompt = promptBuilder.toString();
        
        // Prepare JSON request body
        JSONObject requestBody = new JSONObject();
        JSONArray contents = new JSONArray();
        
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();
        
        JSONObject part = new JSONObject();
        part.put("text", prompt);
        parts.put(part);
        
        content.put("parts", parts);
        contents.put(content);
        
        requestBody.put("contents", contents);
        
        JSONObject generationConfig = new JSONObject();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("topK", 40);
        generationConfig.put("topP", 0.95);
        generationConfig.put("maxOutputTokens", 8192);
        
        requestBody.put("generationConfig", generationConfig);
        
        // Build HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();
        
        // Send request and get response
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("Erreur API: " + response.statusCode() + " " + response.body());
        }
        
        JSONObject responseJson = new JSONObject(response.body());
        
        // Extract text content
        String textContent = responseJson
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text");
        
        // Extract JSON from content (handle cases where it might be wrapped in markdown code blocks)
        String jsonContent = textContent;
        if (textContent.contains("```json")) {
            int start = textContent.indexOf("```json") + 7;
            int end = textContent.lastIndexOf("```");
            jsonContent = textContent.substring(start, end).trim();
        } else if (textContent.contains("```")) {
            int start = textContent.indexOf("```") + 3;
            int end = textContent.lastIndexOf("```");
            jsonContent = textContent.substring(start, end).trim();
        }
        
        try {
            return new JSONObject(jsonContent);
        } catch (Exception e) {
            throw new IOException("Format de réponse invalide de l'API Gemini: " + e.getMessage());
        }
    }
    
    /**
     * Test the Gemini API with a simple query
     * @return Response from the API
     * @throws IOException If an error occurs during the request
     * @throws InterruptedException If the request is interrupted
     */
    public String testGeminiApi() throws IOException, InterruptedException {
        String endpoint = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-pro:generateContent?key=" + apiKey;
        
        JSONObject requestBody = new JSONObject();
        JSONArray contents = new JSONArray();
        
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();
        
        JSONObject part = new JSONObject();
        part.put("text", "Dis bonjour et réponds par une phrase courte.");
        parts.put(part);
        
        content.put("parts", parts);
        contents.put(content);
        
        requestBody.put("contents", contents);
        
        JSONObject generationConfig = new JSONObject();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 100);
        
        requestBody.put("generationConfig", generationConfig);
        
        // Build HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();
        
        // Send request and get response
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("Erreur API: " + response.statusCode() + " " + response.body());
        }
        
        JSONObject responseJson = new JSONObject(response.body());
        
        return responseJson
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text");
    }
} 