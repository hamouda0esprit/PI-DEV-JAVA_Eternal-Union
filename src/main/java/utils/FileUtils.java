package utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class FileUtils {
    // Base directory in Documents
    private static final String DOCUMENTS_DIR = System.getProperty("user.home") + "\\Documents";
    // Relative path for media files
    private static final String RELATIVE_MEDIA_DIR = "\\LOE\\Media\\forum\\";
    
    public static String copyMediaFile(File sourceFile, String mediaType) throws IOException {
        // Create the full path including Documents directory
        Path baseDir = Paths.get(DOCUMENTS_DIR + RELATIVE_MEDIA_DIR);
        
        // Create the base directory if it doesn't exist
        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);
        }

        // Create a unique filename using timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String extension = getFileExtension(sourceFile.getName());
        String newFileName = mediaType + "_" + timestamp + extension;

        // Create the destination path
        Path destinationPath = baseDir.resolve(newFileName);

        // Copy the file
        Files.copy(sourceFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

        // Return only the relative path (without Documents directory)
        return RELATIVE_MEDIA_DIR + newFileName;
    }

    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex);
        }
        return "";
    }
    
    public static String getMediaUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return "";
        }
        try {
            // Convert relative path to full path in Documents
            String fullPath = DOCUMENTS_DIR + relativePath;
            File file = new File(fullPath);
            return file.toURI().toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getForumMediaPath() {
        return DOCUMENTS_DIR + RELATIVE_MEDIA_DIR;
    }

    public static String copyForumMedia(File sourceFile, String mediaType) throws IOException {
        // Create the target directory if it doesn't exist
        String targetDir = getForumMediaPath();
        File directory = new File(targetDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Generate a unique filename
        String extension = getFileExtension(sourceFile.getName());
        String uniqueFileName = UUID.randomUUID().toString() + extension;
        String relativePath = RELATIVE_MEDIA_DIR + uniqueFileName;
        String fullPath = targetDir + uniqueFileName;

        // Copy the file
        Files.copy(sourceFile.toPath(), new File(fullPath).toPath(), StandardCopyOption.REPLACE_EXISTING);

        return relativePath;
    }

    public static String getForumMediaUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }
        try {
            String fullPath = DOCUMENTS_DIR + relativePath;
            File file = new File(fullPath);
            return file.toURI().toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
} 