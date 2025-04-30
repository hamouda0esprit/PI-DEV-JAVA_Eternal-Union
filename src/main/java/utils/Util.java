package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

public class Util {
    public static void saveBase64AsImageFile(String base64String, String filePath) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64String);
            
            File file = new File(filePath);
            // Create parent directory if it doesn't exist
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(decodedBytes);
            }
            
            System.out.println("Image saved to: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 