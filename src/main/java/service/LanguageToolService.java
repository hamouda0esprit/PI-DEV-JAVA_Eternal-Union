package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LanguageToolService {
    private static final String API_URL = "https://api.languagetool.org/v2/check";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String checkText(String text) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(API_URL);
            
            // Format the request parameters as form data
            String formData = "text=" + java.net.URLEncoder.encode(text, "UTF-8") + 
                            "&language=auto";
            
            StringEntity entity = new StringEntity(formData);
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setHeader("Accept", "application/json");
            
            // Execute the request
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity responseEntity = response.getEntity();
                if (responseEntity != null) {
                    return EntityUtils.toString(responseEntity);
                }
            }
        }
        return null;
    }

    public String getSuggestions(String text) throws IOException {
        String response = checkText(text);
        if (response != null) {
            // Parse the response and extract suggestions
            // You can customize this based on your needs
            return response;
        }
        return "No suggestions available";
    }
} 