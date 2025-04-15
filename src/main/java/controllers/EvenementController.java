package Controllers;

import entite.Evenement;
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

public class EvenementController implements Initializable {
    @FXML private GridPane calendarGrid;
    @FXML private Label monthYearLabel;
    @FXML private VBox upcomingEventsList;
    @FXML private Button calendarViewBtn;
    @FXML private Button listViewBtn;
    @FXML private HBox calendarView;
    @FXML private ScrollPane eventsListView;
    @FXML private FlowPane eventsGrid;
    
    private IEvenementService evenementService;
    private YearMonth currentYearMonth;
    private Map<LocalDate, List<Evenement>> eventsByDate;
    
    public EvenementController() {
        this.evenementService = new EvenementService();
        this.currentYearMonth = YearMonth.now();
        this.eventsByDate = new HashMap<>();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadEvents();
        updateCalendar();
        updateUpcomingEvents();
        
        // Set initial view state
        calendarViewBtn.getStyleClass().add("active");
        eventsListView.setVisible(false);
        
        // Load events list
        loadEventsList();
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
        upcomingEventsList.getChildren().clear();
        
        // Get all events and sort them by date
        List<Evenement> upcomingEvents = evenementService.getAll().stream()
            .filter(e -> !e.getDateevent().toLocalDate().isBefore(LocalDate.now()))
            .sorted(Comparator.comparing(Evenement::getDateevent))
            .collect(Collectors.toList());
        
        // Create event cards
        for (Evenement event : upcomingEvents) {
            VBox eventCard = createEventCard(event);
            upcomingEventsList.getChildren().add(eventCard);
        }
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
        
        Label locationLabel = new Label(event.getLocation());
        locationLabel.getStyleClass().add("event-details");
        
        // Action buttons
        HBox actions = new HBox(5);
        actions.getStyleClass().add("event-actions");
        
        Button updateBtn = new Button("Update");
        updateBtn.getStyleClass().add("update-button");
        updateBtn.setOnAction(e -> showUpdateEventDialog(event));
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("delete-button");
        deleteBtn.setOnAction(e -> handleDeleteEvent(event));
        
        actions.getChildren().addAll(updateBtn, deleteBtn);
        
        card.getChildren().addAll(titleLabel, dateLabel, locationLabel, actions);
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
            VBox dialogContent = loader.load();
            
            AddEventDialogController controller = loader.getController();
            controller.setEvent(event); // Add this method to AddEventDialogController
            
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Update Event");
            
            Scene scene = new Scene(dialogContent);
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            
            dialogStage.setScene(scene);
            controller.setDialogStage(dialogStage);
            
            dialogStage.showAndWait();
            
            if (controller.isSaveClicked()) {
                evenementService.update(controller.getEvent());
                loadEvents();
                updateCalendar();
                updateUpcomingEvents();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load dialog: " + e.getMessage());
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
        
        // Add click handler
        cell.setOnMouseClicked(e -> showAddEventDialog(date));
        
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AddEventDialog.fxml"));
            VBox dialogContent = loader.load();
            
            AddEventDialogController controller = loader.getController();
            
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Add Event");
            
            Scene scene = new Scene(dialogContent);
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            
            dialogStage.setScene(scene);
            controller.setDialogStage(dialogStage);
            controller.setSelectedDate(date);
            
            dialogStage.showAndWait();
            
            if (controller.isSaveClicked()) {
                evenementService.add(controller.getEvent());
                loadEvents();
                updateCalendar();
                updateUpcomingEvents();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load dialog: " + e.getMessage());
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
        loadEventsList();
    }
    
    public void refreshViews() {
        loadEvents();
        updateCalendar();
        updateUpcomingEvents();
        loadEventsList();
    }
    
    private void loadEventsList() {
        System.out.println("Loading events list");
        eventsGrid.getChildren().clear();
        
        // Get events from the service
        List<Evenement> events = evenementService.getAllEvenements();
        System.out.println("Found " + events.size() + " events");
        
        for (Evenement event : events) {
            try {
                System.out.println("Creating card for event: " + event.getName());
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EventCard.fxml"));
                Node eventCard = loader.load();
                EventCardController controller = loader.getController();
                controller.setMainController(this); // Set the main controller reference
                controller.setEventData(event);
                eventsGrid.getChildren().add(eventCard);
            } catch (IOException e) {
                System.err.println("Error creating event card: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Make sure the grid is visible
        eventsGrid.setVisible(true);
        eventsGrid.setManaged(true);
    }
} 