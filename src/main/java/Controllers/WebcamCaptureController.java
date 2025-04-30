package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebcamCaptureController implements Initializable {
    @FXML
    private ImageView webcamView;
    
    @FXML
    private Button captureButton;
    
    @FXML
    private Label statusLabel;
    
    private VideoCapture capture;
    private ScheduledExecutorService timer;
    private boolean cameraActive = false;
    private static final String TEMP_IMAGE_PATH = "temp_capture.jpg";
    private static final String PYTHON_SCRIPT_PATH = "src/main/python/face_recognition.py";
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Load OpenCV library
            String opencvPath = System.getProperty("user.dir") + File.separator + "opencv_java249.dll";
            System.out.println("[DEBUG] Loading OpenCV from: " + opencvPath);
            System.load(opencvPath);
            
            // Initialize the camera
            initCamera();
            
        } catch (Exception e) {
            System.err.println("[ERROR] Error initializing camera: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initCamera() {
        try {
            System.out.println("[DEBUG] Initializing camera...");
            
            // Try different camera indices
            for (int i = 0; i < 10; i++) {
                capture = new VideoCapture(i);
                if (capture.isOpened()) {
                    System.out.println("[DEBUG] Camera opened successfully at index: " + i);
                    cameraActive = true;
                    startCameraFeed();
                    return;
                }
                capture.release();
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Error initializing camera: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void startCameraFeed() {
        timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(this::updateImageView, 0, 33, TimeUnit.MILLISECONDS);
    }
    
    private void updateImageView() {
        if (!cameraActive) return;
        
        try {
            Mat frame = new Mat();
            if (capture.read(frame)) {
                Image imageToShow = mat2Image(frame);
                javafx.application.Platform.runLater(() -> {
                    if (webcamView != null) {
                        webcamView.setImage(imageToShow);
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Error updating image view: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void captureImage() {
        if (!cameraActive) {
            System.err.println("[ERROR] Camera is not active");
            return;
        }
        
        try {
            Mat frame = new Mat();
            if (capture.read(frame)) {
                Highgui.imwrite(TEMP_IMAGE_PATH, frame);
                System.out.println("[DEBUG] Image captured and saved to: " + TEMP_IMAGE_PATH);
                
                // Check if Python is installed
                ProcessBuilder pythonCheck = new ProcessBuilder("python", "--version");
                Process pythonProcess = pythonCheck.start();
                int pythonExitCode = pythonProcess.waitFor();
                
                if (pythonExitCode != 0) {
                    System.err.println("[ERROR] Python is not installed or not in PATH");
                    return;
                }
                
                // Check if OpenCV module is installed
                ProcessBuilder moduleCheck = new ProcessBuilder("python", "-c", "import cv2");
                Process moduleProcess = moduleCheck.start();
                int moduleExitCode = moduleProcess.waitFor();
                
                if (moduleExitCode != 0) {
                    System.err.println("[ERROR] OpenCV Python module is not installed. Please run: pip install opencv-python");
                    return;
                }
                
                // Call Python script for face recognition
                ProcessBuilder pb = new ProcessBuilder("python", PYTHON_SCRIPT_PATH, TEMP_IMAGE_PATH);
                pb.redirectErrorStream(true); // Merge error stream with output stream
                Process process = pb.start();
                
                // Read the output from Python script
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()));
                String line;
                StringBuilder output = new StringBuilder();
                
                while ((line = reader.readLine()) != null) {
                    System.out.println("[PYTHON] " + line);
                    output.append(line);
                }
                
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    String result = output.toString();
                    if (result.equals("No match found")) {
                        System.err.println("[ERROR] No matching face found in database");
                    } else {
                        int userId = Integer.parseInt(result);
                        handleSuccessfulRecognition(userId);
                    }
                } else {
                    System.err.println("[ERROR] Face recognition failed. Python script exited with code: " + exitCode);
                    System.err.println("[ERROR] Python output: " + output.toString());
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Error capturing image: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleSuccessfulRecognition(int userId) {
        Stage stage = (Stage) webcamView.getScene().getWindow();
        stage.close();
    }
    
    private Image mat2Image(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Highgui.imencode(".png", frame, buffer);
        return new Image(new java.io.ByteArrayInputStream(buffer.toArray()));
    }
    
    public void stopCamera() {
        if (cameraActive) {
            cameraActive = false;
            if (timer != null && !timer.isShutdown()) {
                timer.shutdown();
            }
            if (capture != null) {
                capture.release();
            }
        }
    }
} 