package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for translating text using Google Translate API
 */
public class TranslationService {
    
    private static final String GOOGLE_TRANSLATE_API_URL = "https://translate.googleapis.com/translate_a/single";
    private String apiKey; // Your Google Cloud API key if using the paid version
    
    public TranslationService() {
        // Initialize with default settings
    }
    
    public TranslationService(String apiKey) {
        this.apiKey = apiKey;
    }
    
    /**
     * Translates text from the source language to the target language
     * This method uses Google Translate API
     * 
     * @param text Text to translate
     * @param targetLanguage Target language code (e.g., "en", "es", "ar")
     * @param sourceLanguage Source language code (e.g., "fr")
     * @return The translated text
     * @throws IOException If an error occurs during translation
     */
    public String translate(String text, String targetLanguage, String sourceLanguage) throws IOException {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        
        // If using the paid API version with API key
        if (apiKey != null && !apiKey.isEmpty()) {
            return translateWithApiKey(text, targetLanguage, sourceLanguage);
        } else {
            // Using the free version (not recommended for production)
            return translateWithoutApiKey(text, targetLanguage, sourceLanguage);
        }
    }
    
    /**
     * Translates text using Google Cloud Translation API (paid version)
     */
   /* */ private String translateWithApiKey(String text, String targetLanguage, String sourceLanguage) throws IOException {
        // Prepare URL
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
        URL url = new URL("https://translation.googleapis.com/language/translate/v2?key=" + apiKey);
        
        // Create connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);
        
        // Prepare JSON request body
        String jsonBody = String.format(
            "{\"q\":\"%s\",\"target\":\"%s\",\"source\":\"%s\",\"format\":\"text\"}",
            text.replace("\"", "\\\""),  // Escape double quotes
            targetLanguage,
            sourceLanguage
        );
        
        // Send request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        // Parse response
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        
        // Extract translated text from JSON response (simplified parsing)
        String jsonResponse = response.toString();
        int startIndex = jsonResponse.indexOf("\"translatedText\":");
        if (startIndex != -1) {
            startIndex += 17; // Length of "translatedText":"
            int endIndex = jsonResponse.indexOf("\"", startIndex);
            if (endIndex != -1) {
                return jsonResponse.substring(startIndex, endIndex);
            }
        }
        
        throw new IOException("Failed to parse translation response: " + jsonResponse);
    }/* */
    
    /**
     * Gets a map of supported languages and their codes
     * @return Map of language names to language codes
     */
    public Map<String, String> getSupportedLanguages() {
        Map<String, String> languages = new HashMap<>();
        languages.put("Français", "fr");
        languages.put("Anglais", "en");
        languages.put("Espagnol", "es");
        languages.put("Allemand", "de");
        languages.put("Italien", "it");
        languages.put("Arabe", "ar");
        languages.put("Arabe (Égypte)", "ar-eg");
        languages.put("Arabe (Arabie Saoudite)", "ar-sa");
        languages.put("Arabe (Maroc)", "ar-ma");
        languages.put("Chinois (Simplifié)", "zh-CN");
        languages.put("Chinois (Traditionnel)", "zh-TW");
        languages.put("Japonais", "ja");
        languages.put("Russe", "ru");
        languages.put("Coréen", "ko");
        languages.put("Hindi", "hi");
        languages.put("Portugais", "pt");
        languages.put("Turc", "tr");
        languages.put("Néerlandais", "nl");
        languages.put("Grec", "el");
        languages.put("Hébreu", "he");
        languages.put("Polonais", "pl");
        languages.put("Thaï", "th");
        languages.put("Suédois", "sv");
        languages.put("Danois", "da");
        languages.put("Finnois", "fi");
        languages.put("Norvégien", "no");
        languages.put("Tchèque", "cs");
        languages.put("Hongrois", "hu");
        languages.put("Roumain", "ro");
        languages.put("Vietnamien", "vi");
        languages.put("Indonésien", "id");
        languages.put("Malais", "ms");
        languages.put("Ukrainien", "uk");
        languages.put("Bengali", "bn");
        languages.put("Persan", "fa");
        
        return languages;
    }
    
    /**
     * Translates text using free Google Translate (not recommended for production)
     */
    private String translateWithoutApiKey(String text, String targetLanguage, String sourceLanguage) throws IOException {
        // Vérifier si le texte est vide
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        
        // Adapter le code de langue pour la compatibilité avec l'API
        String apiTargetLanguage = targetLanguage;
        
        // Certains codes de langue comme ar-sa doivent être convertis en ar pour la traduction
        if (apiTargetLanguage.contains("-")) {
            apiTargetLanguage = apiTargetLanguage.split("-")[0];
        }
        
        // Build the URL with parameters
        StringBuilder urlBuilder = new StringBuilder(GOOGLE_TRANSLATE_API_URL);
        urlBuilder.append("?client=gtx");
        urlBuilder.append("&sl=").append(sourceLanguage);
        urlBuilder.append("&tl=").append(apiTargetLanguage);
        urlBuilder.append("&dt=t"); // Return translated text
        urlBuilder.append("&q=").append(URLEncoder.encode(text, StandardCharsets.UTF_8.name()));
        
        String urlString = urlBuilder.toString();
        System.out.println("Requête de traduction: " + urlString);
        
        // Create connection
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.setConnectTimeout(10000); // 10 secondes timeout
        connection.setReadTimeout(10000);    // 10 secondes timeout
        
        // Check response code
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("Erreur HTTP: " + responseCode + " - " + connection.getResponseMessage());
        }
        
        // Read response
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        
        // Parse response (it's a nested JSON array, but we'll do simple string extraction)
        String responseStr = response.toString();
        System.out.println("Réponse de l'API de traduction (partielle): " + 
                (responseStr.length() > 100 ? responseStr.substring(0, 100) + "..." : responseStr));
        
        try {
            // Format is typically: [[["translated text","original text",null,null,1]],null,"en"]
            // Extract first element which contains the translation
            int startIndex = responseStr.indexOf("\"") + 1;
            if (startIndex <= 0) {
                throw new IOException("Format de réponse invalide - guillemet de début non trouvé");
            }
            
            int endIndex = responseStr.indexOf("\"", startIndex);
            if (endIndex <= startIndex) {
                throw new IOException("Format de réponse invalide - guillemet de fin non trouvé");
            }
            
            String translatedText = responseStr.substring(startIndex, endIndex);
            
            // Correction pour la double échappement de guillemets et autres caractères spéciaux
            translatedText = translatedText.replace("\\\"", "\"").replace("\\n", "\n").replace("\\r", "\r");
            
            System.out.println("Texte traduit: " + translatedText);
            
            if (translatedText.isEmpty() && !text.isEmpty()) {
                // Tenter une autre approche d'extraction si le texte traduit est vide
                startIndex = responseStr.indexOf("[[[\"") + 4;
                if (startIndex > 4) {
                    endIndex = responseStr.indexOf("\"", startIndex);
                    if (endIndex > startIndex) {
                        translatedText = responseStr.substring(startIndex, endIndex);
                        translatedText = translatedText.replace("\\\"", "\"").replace("\\n", "\n").replace("\\r", "\r");
                        System.out.println("Texte traduit (méthode alternative): " + translatedText);
                    }
                }
            }
            
            // Si le texte est toujours vide, utiliser le texte original
            if (translatedText.isEmpty() && !text.isEmpty()) {
                System.out.println("Aucune traduction trouvée, utilisation du texte original");
                return text;
            }
            
            return translatedText;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Échec de l'analyse de la réponse: " + e.getMessage());
            System.err.println("Réponse brute: " + responseStr);
            
            // En cas d'erreur d'analyse, utiliser le texte source plutôt que de lancer une exception
            return text;
        }
    }
} 