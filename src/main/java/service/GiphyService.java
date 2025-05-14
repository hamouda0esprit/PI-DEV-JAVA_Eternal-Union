package service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GiphyService {
    private static final String API_KEY = "FhlUs3ZlE9rFtGc9XSkXknpXzPghbrpf";
    private static final String BASE_URL = "https://api.giphy.com/v1/gifs";
    private final OkHttpClient client;

    public GiphyService() {
        this.client = new OkHttpClient();
    }

    public List<GiphyResult> searchGifs(String query, int limit) throws IOException {
        String url = String.format("%s/search?api_key=%s&q=%s&limit=%d&rating=g",
                BASE_URL, API_KEY, query, limit);

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected response " + response);

            String responseBody = response.body().string();
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray data = jsonResponse.getAsJsonArray("data");

            List<GiphyResult> results = new ArrayList<>();
            data.forEach(element -> {
                JsonObject gif = element.getAsJsonObject();
                JsonObject images = gif.getAsJsonObject("images");
                JsonObject original = images.getAsJsonObject("original");
                JsonObject preview = images.getAsJsonObject("fixed_height");

                results.add(new GiphyResult(
                    gif.get("id").getAsString(),
                    gif.get("title").getAsString(),
                    original.get("url").getAsString(),
                    preview.get("url").getAsString(),
                    preview.get("width").getAsString(),
                    preview.get("height").getAsString()
                ));
            });

            return results;
        }
    }

    public static class GiphyResult {
        private final String id;
        private final String title;
        private final String originalUrl;
        private final String previewUrl;
        private final String previewWidth;
        private final String previewHeight;

        public GiphyResult(String id, String title, String originalUrl, 
                         String previewUrl, String previewWidth, String previewHeight) {
            this.id = id;
            this.title = title;
            this.originalUrl = originalUrl;
            this.previewUrl = previewUrl;
            this.previewWidth = previewWidth;
            this.previewHeight = previewHeight;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getOriginalUrl() { return originalUrl; }
        public String getPreviewUrl() { return previewUrl; }
        public String getPreviewWidth() { return previewWidth; }
        public String getPreviewHeight() { return previewHeight; }
    }
} 