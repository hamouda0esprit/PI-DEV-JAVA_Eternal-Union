package Controllers;

import entite.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import service.UserService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class RegisterDialogController implements Initializable {
    // Step 1 controls
    @FXML
    private Button teachButton;
    @FXML
    private Button learnButton;
    @FXML
    private Button closeButton;
    @FXML
    private Button nextButton;
    
    // Step 2 controls
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField emailField;
    @FXML
    private ComboBox<Integer> dayComboBox;
    @FXML
    private ComboBox<String> monthComboBox;
    @FXML
    private ComboBox<Integer> yearComboBox;
    @FXML
    private Button previousButton;
    
    // Step 3 controls
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label passwordErrorLabel;
    @FXML
    private Button finishButton;
    
    // Step 4 controls
    @FXML
    private FlowPane avatarContainer;
    @FXML
    private Button uploadButton;
    @FXML
    private Button step4ContinueButton;
    
    // Step 5 controls
    @FXML
    private ImageView selectedAvatarPreview;
    @FXML
    private Button finishRegistrationButton;
    
    private Stage dialogStage;
    private int currentStep = 1;
    private String selectedUserType = "";
    private String selectedAvatarPath = "A1.png"; // Default avatar
    private UserService userService;
    
    private User registrationData;
    private Pane overlay;
    private StackPane mainContainer;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userService = new UserService();
        registrationData = new User();
        
        // Initialize based on the current step's FXML file
        String fxmlFile = url.toExternalForm();
        System.out.println("Initializing FXML file: " + fxmlFile);
        
        if (fxmlFile.contains("RegisterDialogStep1.fxml")) {
            System.out.println("Setting up Step 1");
            setupStep1();
        } else if (fxmlFile.contains("RegisterDialogStep2.fxml")) {
            System.out.println("Setting up Step 2");
            setupStep2();
        } else if (fxmlFile.contains("RegisterDialogStep3.fxml")) {
            System.out.println("Setting up Step 3");
            setupStep3();
        } else if (fxmlFile.contains("RegisterDialogStep4.fxml")) {
            System.out.println("Setting up Step 4");
            setupStep4();
            
            // Check if continueButton is found
            if (step4ContinueButton == null) {
                System.out.println("WARNING: Continue button is NULL in Step 4");
            } else {
                System.out.println("Continue button is found in Step 4");
            }
        } else if (fxmlFile.contains("RegisterDialogStep5.fxml")) {
            System.out.println("Setting up Step 5");
            setupStep5();
        }
        
        // Common initialization for all steps
        if (closeButton != null) {
            closeButton.setOnAction(e -> closeDialog());
        }
    }
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public void setMainContainerOverlay(Pane overlay, StackPane mainContainer) {
        this.overlay = overlay;
        this.mainContainer = mainContainer;
    }
    
    private void setupStep1() {
        teachButton.setOnAction(e -> {
            selectedUserType = "TEACHER";
            teachButton.getStyleClass().add("selected-role");
            learnButton.getStyleClass().remove("selected-role");
        });
        
        learnButton.setOnAction(e -> {
            selectedUserType = "STUDENT";
            learnButton.getStyleClass().add("selected-role");
            teachButton.getStyleClass().remove("selected-role");
        });
        
        nextButton.setOnAction(e -> {
            if (selectedUserType.isEmpty()) {
                // Show error or alert for selecting a role
                System.out.println("Please select your role (teach or learn)");
                return;
            }
            
            registrationData.setType(selectedUserType);
            goToNextStep();
        });
    }
    
    private void setupStep2() {
        // Initialize date pickers
        initializeDatePickers();
        
        // Setup buttons
        previousButton.setOnAction(e -> goToPreviousStep());
        
        nextButton.setOnAction(e -> {
            if (validateStep2()) {
                // Save data
                registrationData.setName(fullNameField.getText().trim());
                registrationData.setEmail(emailField.getText().trim());
                
                // Process date
                try {
                    String dateStr = String.format("%d-%s-%d", 
                        dayComboBox.getValue(), 
                        getMonthNumber(monthComboBox.getValue()), 
                        yearComboBox.getValue());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    Date dob = dateFormat.parse(dateStr);
                    registrationData.setDate_of_birth(dob);
                } catch (ParseException ex) {
                    System.out.println("Date parsing error: " + ex.getMessage());
                    return;
                }
                
                goToNextStep();
            }
        });
    }
    
    private void setupStep3() {
        if (previousButton != null) {
            previousButton.setOnAction(e -> goToPreviousStep());
        }
        
        // Validate password as user types
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePassword();
        });
        
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePassword();
        });
        
        if (finishButton != null) {
            finishButton.setOnAction(e -> {
                if (validatePassword()) {
                    registrationData.setPassword(passwordField.getText().trim());
                    goToNextStep();
                }
            });
        }
    }
    
    private void setupStep4() {
        // Setup avatar selection buttons
        for (int i = 1; i <= 6; i++) {
            Button avatarButton = (Button) avatarContainer.lookup("#avatar" + i);
            if (avatarButton != null) {
                final String avatarPath = "A" + i + ".png";
                avatarButton.setOnAction(e -> {
                    selectedAvatarPath = avatarPath;
                    highlightSelectedAvatar(avatarButton);
                });
            }
        }
        
        // Setup upload button
        if (uploadButton != null) {
            uploadButton.setOnAction(e -> uploadCustomAvatar());
        }
        
        // Setup continue button - direct approach
        if (step4ContinueButton != null) {
            step4ContinueButton.setOnAction(e -> {
                System.out.println("Continue button clicked directly in setupStep4");
                goToStep5Directly();
            });
        }
    }
    
    private void setupStep5() {
        System.out.println("Setting up Step 5 with avatar: " + selectedAvatarPath);
        
        // Set the selected avatar image in the preview
        if (selectedAvatarPreview != null && selectedAvatarPath != null) {
            try {
                Image avatarImage = null;
                
                // Check if the avatar is a default avatar or a custom one
                if (selectedAvatarPath.startsWith("A")) {
                    // It's a default avatar from resources
                    String imagePath = "/Images/" + selectedAvatarPath;
                    System.out.println("Loading default avatar from: " + imagePath);
                    avatarImage = new Image(getClass().getResourceAsStream(imagePath));
                } else {
                    // It's a custom avatar from file system
                    File file = new File(selectedAvatarPath);
                    if (file.exists()) {
                        System.out.println("Loading custom avatar from: " + file.getAbsolutePath());
                        avatarImage = new Image(file.toURI().toString());
                    } else {
                        System.out.println("Custom avatar file not found: " + selectedAvatarPath);
                        // Fallback to default avatar
                        avatarImage = new Image(getClass().getResourceAsStream("/Images/A1.png"));
                    }
                }
                
                if (avatarImage != null) {
                    selectedAvatarPreview.setImage(avatarImage);
                    System.out.println("Avatar image set successfully");
                } else {
                    System.out.println("Failed to load avatar image");
                }
            } catch (Exception e) {
                System.out.println("Error loading avatar preview: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("selectedAvatarPreview or selectedAvatarPath is null");
        }
        
        if (finishRegistrationButton != null) {
            finishRegistrationButton.setOnAction(e -> {
                try {
                    completeRegistration();
                } catch (Exception ex) {
                    System.out.println("Registration error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
        } else {
            System.out.println("finishRegistrationButton is null in step 5");
        }
    }
    
    private void initializeDatePickers() {
        // Days 1-31
        for (int i = 1; i <= 31; i++) {
            dayComboBox.getItems().add(i);
        }
        dayComboBox.setValue(1);
        
        // Months
        String[] months = {"January", "February", "March", "April", "May", "June", 
                          "July", "August", "September", "October", "November", "December"};
        monthComboBox.getItems().addAll(months);
        monthComboBox.setValue("January");
        
        // Years (100 years back from current)
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear; i >= currentYear - 100; i--) {
            yearComboBox.getItems().add(i);
        }
        yearComboBox.setValue(currentYear - 20); // Default to 20 years ago
    }
    
    private String getMonthNumber(String monthName) {
        Map<String, String> monthMap = new HashMap<>();
        monthMap.put("January", "01");
        monthMap.put("February", "02");
        monthMap.put("March", "03");
        monthMap.put("April", "04");
        monthMap.put("May", "05");
        monthMap.put("June", "06");
        monthMap.put("July", "07");
        monthMap.put("August", "08");
        monthMap.put("September", "09");
        monthMap.put("October", "10");
        monthMap.put("November", "11");
        monthMap.put("December", "12");
        
        return monthMap.getOrDefault(monthName, "01");
    }
    
    private boolean validateStep2() {
        boolean isValid = true;
        
        // Validate name
        if (fullNameField.getText().trim().isEmpty()) {
            System.out.println("Full name is required");
            isValid = false;
        }
        
        // Validate email
        String email = emailField.getText().trim();
        if (email.isEmpty() || !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            System.out.println("Valid email is required");
            isValid = false;
        }
        
        try {
            // Check if email is already in use
            User existingUser = userService.getUserByEmail(email);
            if (existingUser != null) {
                System.out.println("Email already in use");
                isValid = false;
            }
        } catch (Exception e) {
            System.out.println("Error checking email: " + e.getMessage());
            e.printStackTrace();
        }
        
        return isValid;
    }
    
    private boolean validatePassword() {
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        boolean isValid = true;
        
        // Clear previous error
        passwordErrorLabel.setVisible(false);
        
        // Check if password is empty
        if (password.isEmpty()) {
            passwordErrorLabel.setText("Password is required");
            passwordErrorLabel.setVisible(true);
            isValid = false;
        } 
        // Check password length
        else if (password.length() < 6) {
            passwordErrorLabel.setText("Password must be at least 6 characters");
            passwordErrorLabel.setVisible(true);
            isValid = false;
        }
        // Check if password contains a number and a letter
        else if (!password.matches(".*\\d.*") || !password.matches(".*[a-z].*")) {
            passwordErrorLabel.setText("Password must contain a number and a lowercase letter");
            passwordErrorLabel.setVisible(true);
            isValid = false;
        }
        // Check if passwords match
        else if (!password.equals(confirmPassword)) {
            passwordErrorLabel.setText("Passwords do not match");
            passwordErrorLabel.setVisible(true);
            isValid = false;
        }
        
        return isValid;
    }
    
    private void uploadCustomAvatar() {
        System.out.println("Opening file chooser for custom avatar");
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Avatar Image");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            
            File selectedFile = fileChooser.showOpenDialog(dialogStage);
            if (selectedFile != null) {
                selectedAvatarPath = selectedFile.getAbsolutePath();
                System.out.println("Custom avatar selected: " + selectedAvatarPath);
                
                // Create and display a preview of the selected image
                try {
                    Image image = new Image(selectedFile.toURI().toString());
                    
                    // Create a new avatar button to display the custom image
                    ImageView imageView = new ImageView(image);
                    imageView.setFitHeight(60);
                    imageView.setFitWidth(60);
                    
                    Button customButton = new Button();
                    customButton.setGraphic(imageView);
                    customButton.getStyleClass().add("avatar-button");
                    customButton.getStyleClass().add("selected-avatar");
                    
                    // Clear previous selections
                    for (int i = 1; i <= 6; i++) {
                        Button avatarButton = (Button) avatarContainer.lookup("#avatar" + i);
                        if (avatarButton != null) {
                            avatarButton.getStyleClass().remove("selected-avatar");
                        }
                    }
                    
                    // Add the new button to the flow pane
                    boolean buttonAdded = false;
                    boolean customButtonFound = false;
                    for (Node node : avatarContainer.getChildren()) {
                        if (node instanceof Button) {
                            Button btn = (Button) node;
                            if (btn.getId() == null || (!btn.getId().startsWith("avatar"))) {
                                // Replace existing custom button
                                int index = avatarContainer.getChildren().indexOf(btn);
                                avatarContainer.getChildren().set(index, customButton);
                                buttonAdded = true;
                                customButtonFound = true;
                                break;
                            }
                        }
                    }
                    
                    if (!customButtonFound) {
                        // Try to add at the end
                        avatarContainer.getChildren().add(customButton);
                        buttonAdded = true;
                    }
                    
                    // Add event handler to the custom button
                    customButton.setOnAction(e -> {
                        selectedAvatarPath = selectedFile.getAbsolutePath();
                        highlightSelectedAvatar(customButton);
                    });
                    
                    // Show a success message
                    System.out.println("Custom avatar added successfully: " + buttonAdded);
                    
                    // Auto-select the custom avatar
                    selectedAvatarPath = selectedFile.getAbsolutePath();
                } catch (Exception e) {
                    System.out.println("Error loading custom image: " + e.getMessage());
                    e.printStackTrace();
                    
                    // Show error alert
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Image Upload Error");
                    alert.setContentText("Could not load the selected image: " + e.getMessage());
                    alert.showAndWait();
                }
            } else {
                System.out.println("No file selected for custom avatar");
            }
        } catch (Exception e) {
            System.out.println("Error in uploadCustomAvatar: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void highlightSelectedAvatar(Button selectedButton) {
        // Remove highlight from all avatar buttons
        for (int i = 1; i <= 6; i++) {
            Button avatarButton = (Button) avatarContainer.lookup("#avatar" + i);
            if (avatarButton != null) {
                avatarButton.getStyleClass().remove("selected-avatar");
            }
        }
        
        // Add highlight to the selected button
        selectedButton.getStyleClass().add("selected-avatar");
    }
    
    private void goToNextStep() {
        currentStep++;
        loadStep(currentStep);
    }
    
    private void goToPreviousStep() {
        currentStep--;
        loadStep(currentStep);
    }
    
    private void loadStep(int step) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RegisterDialogStep" + step + ".fxml"));
            Parent root = loader.load();
            
            RegisterDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.currentStep = step;
            controller.registrationData = this.registrationData;
            controller.selectedUserType = this.selectedUserType;
            controller.selectedAvatarPath = this.selectedAvatarPath;
            
            // Pass overlay reference to the next controller
            if (this.overlay != null && this.mainContainer != null) {
                controller.setMainContainerOverlay(this.overlay, this.mainContainer);
            }
            
            // Update scene
            dialogStage.getScene().setRoot(root);
            
            // Re-center dialog after each step
            Stage primaryStage = (Stage) dialogStage.getOwner();
            if (primaryStage != null) {
                double dialogWidth = 500; // Width from FXML
                double dialogHeight = 460; // Height from FXML
                double centerX = primaryStage.getX() + (primaryStage.getWidth() / 2) - (dialogWidth / 2);
                double centerY = primaryStage.getY() + (primaryStage.getHeight() / 2) - (dialogHeight / 2);
                
                dialogStage.setX(centerX);
                dialogStage.setY(centerY);
            }
            
        } catch (IOException e) {
            System.out.println("Error loading step " + step + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void completeRegistration() {
        try {
            System.out.println("===== STARTING REGISTRATION PROCESS =====");
            
            // Verify and set necessary fields
            if (registrationData.getName() == null || registrationData.getName().isEmpty()) {
                throw new Exception("Full name is required");
            }
            
            if (registrationData.getEmail() == null || registrationData.getEmail().isEmpty()) {
                throw new Exception("Email is required");
            }
            
            if (registrationData.getPassword() == null || registrationData.getPassword().isEmpty()) {
                throw new Exception("Password is required");
            }
            
            if (registrationData.getType() == null || registrationData.getType().isEmpty()) {
                throw new Exception("User type (Student or Teacher) is required");
            }
            
            if (registrationData.getDate_of_birth() == null) {
                throw new Exception("Date of birth is required");
            }
            
            // Ensure we have the avatar path set
            if (selectedAvatarPath == null || selectedAvatarPath.isEmpty()) {
                // Set default avatar if none selected
                selectedAvatarPath = "A1.png";
            }
            
            // Set the avatar path in the registration data
            registrationData.setImg(selectedAvatarPath);
            
            // Log the registration data
            System.out.println("Name: " + registrationData.getName());
            System.out.println("Email: " + registrationData.getEmail());
            System.out.println("Type: " + registrationData.getType());
            System.out.println("Date of Birth: " + registrationData.getDate_of_birth());
            System.out.println("Avatar: " + registrationData.getImg());
            
            // Use the UserService addUser method to insert the user
            userService.addUser(registrationData);
            
            System.out.println("===== USER SUCCESSFULLY REGISTERED =====");
            
            // Show success and close
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registration Complete");
            alert.setHeaderText("Welcome to League of Education!");
            alert.setContentText("Your account has been created successfully. You can now log in with your email and password.");
            alert.showAndWait();
            
            closeDialog();
        } catch (Exception e) {
            System.out.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Registration Error");
            alert.setHeaderText("Could not complete registration");
            alert.setContentText("An error occurred: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    @FXML
    public void handleNextStep() {
        System.out.println("=============================================");
        System.out.println("handleNextStep called directly from FXML");
        System.out.println("Current step: " + currentStep);
        System.out.println("Selected avatar: " + selectedAvatarPath);
        System.out.println("=============================================");
        
        try {
            // Make sure we have a selected avatar
            if (selectedAvatarPath == null || selectedAvatarPath.isEmpty()) {
                selectedAvatarPath = "A1.png"; // Default avatar
                System.out.println("Using default avatar: " + selectedAvatarPath);
            }
            
            // Save the selected avatar
            registrationData.setImg(selectedAvatarPath);
            
            // Go to the next step
            System.out.println("Moving to step: " + (currentStep + 1));
            goToStep5Directly(); // Use the direct method to ensure it works
        } catch (Exception ex) {
            System.out.println("ERROR in handleNextStep: " + ex.getMessage());
            ex.printStackTrace();
            
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Navigation Error");
            alert.setContentText("Could not proceed to the next step: " + ex.getMessage());
            alert.showAndWait();
        }
    }
    
    // Direct method to go to step 5
    private void goToStep5Directly() {
        System.out.println("Going to step 5 directly with avatar: " + selectedAvatarPath);
        try {
            // Save the avatar selection
            if (selectedAvatarPath == null || selectedAvatarPath.isEmpty()) {
                selectedAvatarPath = "A1.png"; // Default avatar
                System.out.println("Using default avatar in goToStep5Directly: " + selectedAvatarPath);
            }
            registrationData.setImg(selectedAvatarPath);
            
            // Manually load step 5
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RegisterDialogStep5.fxml"));
            Parent root = loader.load();
            
            RegisterDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.currentStep = 5;
            controller.registrationData = this.registrationData;
            controller.selectedUserType = this.selectedUserType;
            controller.selectedAvatarPath = this.selectedAvatarPath;
            
            System.out.println("Transferred avatar path to new controller: " + this.selectedAvatarPath);
            
            // Pass overlay reference to the next controller
            if (this.overlay != null && this.mainContainer != null) {
                controller.setMainContainerOverlay(this.overlay, this.mainContainer);
            }
            
            // Update scene
            dialogStage.getScene().setRoot(root);
            
            // Re-center dialog
            Stage primaryStage = (Stage) dialogStage.getOwner();
            if (primaryStage != null) {
                double dialogWidth = 500; // Width from FXML
                double dialogHeight = 460; // Height from FXML
                double centerX = primaryStage.getX() + (primaryStage.getWidth() / 2) - (dialogWidth / 2);
                double centerY = primaryStage.getY() + (primaryStage.getHeight() / 2) - (dialogHeight / 2);
                
                dialogStage.setX(centerX);
                dialogStage.setY(centerY);
            }
        } catch (Exception ex) {
            System.out.println("Error going to step 5: " + ex.getMessage());
            ex.printStackTrace();
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Could not navigate to next step");
            alert.setContentText("Error details: " + ex.getMessage());
            alert.showAndWait();
        }
    }
} 