package services;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

//import com.google.gson.*;

public class GeminiAIService {
 /*   private static final String API_KEY = "AIzaSyDmQEcJ5FRVf7sOiQcdUVETwk_r1ACDQnY";
    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=" + API_KEY;

    public static String explainText(String inputText) throws IOException {
        URL url = new URL(ENDPOINT);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        JsonObject prompt = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", "Explain this in simple terms:\n" + inputText);

        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        prompt.add("contents", contents);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = prompt.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
        JsonArray candidates = json.getAsJsonArray("candidates");
        if (candidates != null && candidates.size() > 0) {
            JsonObject first = candidates.get(0).getAsJsonObject();
            JsonObject contentObj = first.getAsJsonObject("content");
            JsonArray resultParts = contentObj.getAsJsonArray("parts");
            if (resultParts != null && resultParts.size() > 0) {
                return resultParts.get(0).getAsJsonObject().get("text").getAsString();
            }
        }
        return "No explanation received.";
    }
    */
}
