package Controllers;

import entite.Evenement;
import entite.User;
import service.IEvenementService;
import service.EvenementService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.stage.Modality;
import java.io.File;
import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;
import javafx.scene.Node;
import service.WeatherService;
import service.ParticipationService;

public class EvenementController implements Initializable {
    @FXML private GridPane calendarGrid;
    @FXML private Label monthYearLabel;
    @FXML private VBox upcomingEventsList;
    @FXML private Button calendarViewBtn;
    @FXML private Button listViewBtn;
    @FXML private HBox calendarView;
    @FXML private ScrollPane eventsListView;
    @FXML private FlowPane eventsGrid;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private TextField listSearchField;
    @FXML private Button listSearchButton;
    @FXML private TextField sidebarSearchField;
    @FXML private Button sidebarSearchButton;
    
    private IEvenementService evenementService;
    private YearMonth currentYearMonth;
    private Map<LocalDate, List<Evenement>> eventsByDate;
    private User currentUser;
    private boolean isProfessor;
    
    public EvenementController() {
        this.evenementService = new EvenementService();
        this.currentYearMonth = YearMonth.now();
        this.eventsByDate = new HashMap<>();
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        // Check if user is a professor
        this.isProfessor = user != null && 
            (user.getType().equalsIgnoreCase("teacher") || 
             user.getType().equalsIgnoreCase("professeur") ||
             user.getType().equalsIgnoreCase("prof") ||
             user.getType().equals("1"));
        
        loadEvents();
        updateCalendar();
        updateUpcomingEvents();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize the calendar and views
        currentUser = LoginController.getAuthenticatedUser();
        if (currentUser != null) {
            // Set isProfessor flag
            isProfessor = currentUser.getType().equalsIgnoreCase("teacher") || 
                         currentUser.getType().equalsIgnoreCase("professeur") ||
                         currentUser.getType().equalsIgnoreCase("prof") ||
                         currentUser.getType().equals("1");
            
            // Initialize search functionality for all views
            if (searchField != null) {
                searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                    filterEvents(newValue);
                });
            }
            
            if (listSearchField != null) {
                listSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                    filterEvents(newValue);
                });
            }
            
            if (sidebarSearchField != null) {
                sidebarSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                    filterUpcomingEvents(newValue);
                });
            }
            
            // Load events and update views
            loadEvents();
            updateCalendar();
            updateUpcomingEvents("");
        }
        showCalendarView();
    }
    
    private void loadEvents() {
        eventsByDate.clear();
        List<Evenement> events = evenementService.getAll();
        for (Evenement event : events) {
            LocalDate date = event.getDateevent().toLocalDate();
            eventsByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(event);
        }
    }
    
    private void updateUpcomingEvents() {
        updateUpcomingEvents("");
    }
    
    private VBox createEventCard(Evenement event) {
        VBox card = new VBox(5);
        card.getStyleClass().add("event-card");
        
        // Event title
        Label titleLabel = new Label(event.getName());
        titleLabel.getStyleClass().add("event-title");
        
        // Event details
        Label dateLabel = new Label(formatEventDateTime(event));
        dateLabel.getStyleClass().add("event-details");
        
        Label locationLabel = new Label("📍 " + event.getLocation());
        locationLabel.getStyleClass().add("event-details");

        // Weather information
        WeatherService weatherService = new WeatherService();
        String weatherInfo = weatherService.getWeatherForLocation(event.getLocation(), event.getDateevent().toLocalDate());
        Label weatherLabel = new Label("🌡️ " + weatherInfo);
        weatherLabel.getStyleClass().add("event-details");

        // Participant count and capacity
        ParticipationService participationService = new ParticipationService();
        int currentParticipants = participationService.getParticipantCount(event.getId());
        Label capacityLabel = new Label("👥 Participants: " + currentParticipants + "/" + event.getCapacite());
        capacityLabel.getStyleClass().add("event-details");
        
        // Action buttons - only show for professors
        HBox actions = new HBox(5);
        actions.getStyleClass().add("event-actions");
        
        if (isProfessor) {
            Button updateBtn = new Button("Update");
            updateBtn.getStyleClass().add("update-button");
            updateBtn.setOnAction(e -> showUpdateEventDialog(event));
            
            Button deleteBtn = new Button("Delete");
            deleteBtn.getStyleClass().add("delete-button");
            deleteBtn.setOnAction(e -> handleDeleteEvent(event));
            
            actions.getChildren().addAll(updateBtn, deleteBtn);
        }
        
        card.getChildren().addAll(titleLabel, dateLabel, locationLabel, weatherLabel, capacityLabel, actions);
        return card;
    }
    
    private String formatEventDateTime(Evenement event) {
        LocalDate date = event.getDateevent().toLocalDate();
        LocalTime time = event.getTime().toLocalTime();
        return date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + " at " +
               time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
    
    private void showUpdateEventDialog(Evenement event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AddEventDialog.fxml"));
            AnchorPane dialogPane = loader.load();
            
            AddEventDialogController controller = loader.getController();
            controller.setEvent(event);
            
            // Create the dialog Stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Update Event");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(calendarView.getScene().getWindow());
            
            Scene scene = new Scene(dialogPane);
            dialogStage.setScene(scene);
            
            // Set the dialog stage in the controller
            controller.setDialogStage(dialogStage);
            
            // Show the dialog and wait for the user response
            dialogStage.showAndWait();
            
            if (controller.isSaved()) {
                // Update the event in the database
                Evenement updatedEvent = controller.getEvent();
                if (updatedEvent != null) {
                    evenementService.update(updatedEvent);
                    // Refresh the views
                    loadEvents();
                    updateCalendar();
                    updateUpcomingEvents();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load the dialog");
            alert.setContentText("An error occurred while loading the update event dialog.");
            alert.showAndWait();
        }
    }
    
    private void handleDeleteEvent(Evenement event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Event");
        alert.setHeaderText("Delete Event: " + event.getName());
        alert.setContentText("Are you sure you want to delete this event?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                evenementService.delete(event.getId());
                loadEvents();
                updateCalendar();
                updateUpcomingEvents();
            }
        });
    }
    
    private void updateCalendar() {
        // Update month/year label
        monthYearLabel.setText(currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        
        // Clear existing calendar cells
        calendarGrid.getChildren().removeIf(node -> GridPane.getRowIndex(node) > 0);
        
        // Get the first day of the month
        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;
        
        // Create calendar cells
        int day = 1;
        for (int row = 1; row < 7; row++) {
            for (int col = 0; col < 7; col++) {
                if (row == 1 && col < dayOfWeek || day > currentYearMonth.lengthOfMonth()) {
                    continue;
                }
                
                LocalDate date = currentYearMonth.atDay(day);
                VBox cell = createCalendarCell(date);
                calendarGrid.add(cell, col, row);
                day++;
            }
        }
    }
    
    private VBox createCalendarCell(LocalDate date) {
        VBox cell = new VBox(5);
        cell.getStyleClass().add("calendar-cell");
        
        // Add date label
        Label dateLabel = new Label(String.valueOf(date.getDayOfMonth()));
        cell.getChildren().add(dateLabel);
        
        // Add event indicators if there are events
        List<Evenement> events = eventsByDate.get(date);
        if (events != null && !events.isEmpty()) {
            cell.getStyleClass().add("calendar-cell-has-events");
            for (Evenement event : events) {
                Label eventLabel = new Label(event.getName());
                eventLabel.setStyle("-fx-text-fill: #5B9BD5;");
                cell.getChildren().add(eventLabel);
            }
        }
        
        // Highlight today
        if (date.equals(LocalDate.now())) {
            cell.getStyleClass().add("calendar-cell-today");
        }
        
        // Add click handler only for professors
        if (isProfessor) {
            cell.setOnMouseClicked(e -> showAddEventDialog(date));
        }
        
        // Add tooltip with event details
        if (events != null && !events.isEmpty()) {
            StringBuilder tooltipText = new StringBuilder();
            WeatherService weatherService = new WeatherService();
            ParticipationService participationService = new ParticipationService();
            
            for (Evenement event : events) {
                tooltipText.append("📅 ").append(event.getName())
                         .append("\n")
                         .append("⏰ ").append(formatEventDateTime(event))
                         .append("\n")
                         .append("📍 ").append(event.getLocation())
                         .append("\n");
                
                // Add weather information
                String weatherInfo = weatherService.getWeatherForLocation(event.getLocation(), event.getDateevent().toLocalDate());
                tooltipText.append("🌡️ ").append(weatherInfo)
                         .append("\n");
                
                // Add participant count
                int currentParticipants = participationService.getParticipantCount(event.getId());
                tooltipText.append("👥 Participants: ").append(currentParticipants)
                         .append("/").append(event.getCapacite())
                         .append("\n\n");
            }
            Tooltip tooltip = new Tooltip(tooltipText.toString().trim());
            Tooltip.install(cell, tooltip);
        }
        
        return cell;
    }
    
    @FXML
    private void previousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        updateCalendar();
    }
    
    @FXML
    private void nextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        updateCalendar();
    }
    
    private void showAddEventDialog(LocalDate date) {
        if (!isProfessor) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Access Denied");
            alert.setHeaderText("Permission Required");
            alert.setContentText("Only professors can add events.");
            alert.showAndWait();
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AddEventDialog.fxml"));
            AnchorPane dialogPane = loader.load();
            
            AddEventDialogController controller = loader.getController();
            controller.setDialogStage(new Stage());
            controller.setSelectedDate(date);
            
            // Create the dialog Stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Event");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(calendarView.getScene().getWindow());
            
            Scene scene = new Scene(dialogPane);
            dialogStage.setScene(scene);
            
            // Set the dialog stage in the controller
            controller.setDialogStage(dialogStage);
            
            // Show the dialog and wait for the user response
            dialogStage.showAndWait();
            
            if (controller.isSaved()) {
                // Save the event to the database
                Evenement event = controller.getEvent();
                if (event != null) {
                    evenementService.add(event);
                    // Refresh the calendar view
                    loadEvents();
                    updateCalendar();
                    updateUpcomingEvents();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load the dialog");
            alert.setContentText("An error occurred while loading the add event dialog.");
            alert.showAndWait();
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    @FXML
    private void showCalendarView() {
        calendarView.setVisible(true);
        eventsListView.setVisible(false);
        calendarViewBtn.getStyleClass().add("active");
        listViewBtn.getStyleClass().remove("active");
    }
    
    @FXML
    private void showListView() {
        System.out.println("Switching to list view");
        calendarView.setVisible(false);
        eventsListView.setVisible(true);
        eventsListView.setManaged(true);
        listViewBtn.getStyleClass().add("active");
        calendarViewBtn.getStyleClass().remove("active");
        calendarViewBtn.getStyleClass().remove("view-toggle-button");
        calendarViewBtn.getStyleClass().add("view-toggle-button");
        loadEventsList();
    }
    
    public void refreshViews() {
        loadEvents();
        updateCalendar();
        updateUpcomingEvents();
        loadEventsList();
    }
    
    private void loadEventsList() {
        eventsGrid.getChildren().clear();
        
        if (currentUser == null) {
            return;
        }
        
        // Get all events
        List<Evenement> events = evenementService.getAll();
        
        for (Evenement event : events) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EventCard.fxml"));
                Node eventCard = loader.load();
                EventCardController controller = loader.getController();
                controller.setMainController(this);
                controller.setCurrentUser(currentUser);
                controller.setEventData(event);
                eventsGrid.getChildren().add(eventCard);
            } catch (IOException e) {
                System.err.println("Error creating event card: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void toggleSearch() {
        boolean isVisible = searchField.isVisible();
        searchField.setVisible(!isVisible);
        searchField.setManaged(!isVisible);
        
        if (!isVisible) {
            searchField.requestFocus();
        } else {
            searchField.clear();
            filterEvents("");
        }
    }
    
    @FXML
    private void toggleSidebarSearch() {
        System.out.println("Toggling sidebar search");
        if (sidebarSearchField != null) {
            boolean isVisible = sidebarSearchField.isVisible();
            sidebarSearchField.setVisible(!isVisible);
            sidebarSearchField.setManaged(!isVisible);
            
            if (!isVisible) {
                sidebarSearchField.requestFocus();
            } else {
                sidebarSearchField.clear();
                updateUpcomingEvents("");
            }
        } else {
            System.out.println("Sidebar search field is null");
        }
    }
    
    public void filterEvents(String searchText) {
        System.out.println("Filtering events with search text: " + searchText);
        
        if (eventsListView.isVisible()) {
            // Filter events in list view
            eventsGrid.getChildren().clear();
            List<Evenement> events = evenementService.getAll().stream()
                .filter(event -> matchesSearch(event, searchText))
                .collect(Collectors.toList());
                
            System.out.println("Found " + events.size() + " matching events");
            
            for (Evenement event : events) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EventCard.fxml"));
                    Node eventCard = loader.load();
                    EventCardController controller = loader.getController();
                    controller.setMainController(this);
                    controller.setEventData(event);
                    eventsGrid.getChildren().add(eventCard);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            // Filter events in calendar view
            loadEvents();
            if (!searchText.isEmpty()) {
                eventsByDate.entrySet().removeIf(entry -> 
                    entry.getValue().stream().noneMatch(event -> matchesSearch(event, searchText))
                );
            }
            updateCalendar();
            updateUpcomingEvents();
        }
    }
    
    private boolean matchesSearch(Evenement event, String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            return true;
        }
        searchText = searchText.toLowerCase();
        return event.getName().toLowerCase().contains(searchText) ||
               (event.getDescription() != null && event.getDescription().toLowerCase().contains(searchText)) ||
               event.getLocation().toLowerCase().contains(searchText);
    }
    
    private void filterUpcomingEvents(String searchText) {
        System.out.println("Filtering upcoming events with: " + searchText);
        updateUpcomingEvents(searchText);
    }
    
    private void updateUpcomingEvents(String searchText) {
        System.out.println("Updating upcoming events with search: " + searchText);
        upcomingEventsList.getChildren().clear();
        
        // Get all events and sort them by date
        List<Evenement> upcomingEvents = evenementService.getAll().stream()
            .filter(e -> !e.getDateevent().toLocalDate().isBefore(LocalDate.now()))
            .filter(e -> searchText.isEmpty() || matchesSearch(e, searchText))
            .sorted(Comparator.comparing(Evenement::getDateevent))
            .collect(Collectors.toList());
        
        System.out.println("Found " + upcomingEvents.size() + " matching events");
        
        // Create event cards
        for (Evenement event : upcomingEvents) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EventCardTeacher.fxml"));
                Node eventCard = loader.load();
                EventCardTeacherController controller = loader.getController();
                controller.setMainController(this);
                controller.setCurrentUser(currentUser);
                controller.setEventData(event);
                upcomingEventsList.getChildren().add(eventCard);
            } catch (IOException e) {
                System.err.println("Error creating event card: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void handleListSearch() {
        String searchText = listSearchField.getText();
        filterEvents(searchText);
    }
} 