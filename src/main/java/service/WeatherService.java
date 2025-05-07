package service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class WeatherService {
    private static final String API_KEY = "5256e473a8f38e1c8baadf35d92a8c00";
    private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/weather";

    public String getWeatherForLocation(String location, LocalDate date) {
        try {
            // Format the URL with the location and API key
            String urlString = String.format("%s?q=%s&appid=%s&units=metric&lang=fr", 
                BASE_URL, location.replace(" ", "%20"), API_KEY);
            
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Simple string parsing instead of JSON
                String responseStr = response.toString();
                int tempIndex = responseStr.indexOf("\"temp\":") + 7;
                int tempEndIndex = responseStr.indexOf(",", tempIndex);
                double temperature = Double.parseDouble(responseStr.substring(tempIndex, tempEndIndex));

                int descIndex = responseStr.indexOf("\"description\":\"") + 15;
                int descEndIndex = responseStr.indexOf("\"", descIndex);
                String description = responseStr.substring(descIndex, descEndIndex);

                // Format the weather information
                return String.format("🌡️ Température: %.1f°C\n☁️ Météo: %s", 
                    temperature, description);
            }
        } catch (Exception e) {
            System.err.println("Error fetching weather data: " + e.getMessage());
        }
        return "❌ Données météo non disponibles";
    }
} 