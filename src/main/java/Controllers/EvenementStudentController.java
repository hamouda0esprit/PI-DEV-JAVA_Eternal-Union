package Controllers;

import entite.Evenement;
import entite.User;
import service.IEvenementService;
import service.EvenementService;
import service.ParticipationService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;

public class EvenementStudentController implements Initializable {
    @FXML private GridPane calendarGrid;
    @FXML private Label monthYearLabel;
    @FXML private VBox upcomingEventsList;
    @FXML private Button calendarViewBtn;
    @FXML private Button listViewBtn;
    @FXML private HBox calendarView;
    @FXML private ScrollPane eventsListView;
    @FXML private FlowPane eventsGrid;
    
    private IEvenementService evenementService;
    private ParticipationService participationService;
    private YearMonth currentYearMonth;
    private Map<LocalDate, List<Evenement>> eventsByDate;
    private User currentUser;
    
    public EvenementStudentController() {
        this.evenementService = new EvenementService();
        this.participationService = new ParticipationService();
        this.currentYearMonth = YearMonth.now();
        this.eventsByDate = new HashMap<>();
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        
        // Display user information
        if (user != null) {
            System.out.println("Current User Information:");
            System.out.println("Name: " + user.getName());
            System.out.println("Type: " + user.getType());
            
            // Show alert with user type
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("User Information");
            alert.setHeaderText("Logged in as: " + user.getName());
            alert.setContentText("User Type: " + user.getType() + "\nRole: Student");
            alert.showAndWait();
        }
        
        loadEvents();
        updateCalendar();
        updateUpcomingEvents();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize the calendar and views
        updateCalendar();
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
        upcomingEventsList.getChildren().clear();
        
        // Get all events and sort them by date
        List<Evenement> upcomingEvents = evenementService.getAll().stream()
            .filter(e -> !e.getDateevent().toLocalDate().isBefore(LocalDate.now()))
            .sorted(Comparator.comparing(Evenement::getDateevent))
            .collect(Collectors.toList());
        
        // Create event cards
        for (Evenement event : upcomingEvents) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EventCardStudent.fxml"));
                Node eventCard = loader.load();
                EventCardStudentController controller = loader.getController();
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
    
    private String formatEventDateTime(Evenement event) {
        LocalDate date = event.getDateevent().toLocalDate();
        LocalTime time = event.getTime().toLocalTime();
        return date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + " at " +
               time.format(DateTimeFormatter.ofPattern("HH:mm"));
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
            
            // Add tooltip with event details
            StringBuilder tooltipText = new StringBuilder();
            for (Evenement event : events) {
                tooltipText.append(event.getName())
                         .append("\n")
                         .append(formatEventDateTime(event))
                         .append("\n")
                         .append(event.getLocation())
                         .append("\n\n");
            }
            Tooltip tooltip = new Tooltip(tooltipText.toString().trim());
            Tooltip.install(cell, tooltip);
        }
        
        // Highlight today
        if (date.equals(LocalDate.now())) {
            cell.getStyleClass().add("calendar-cell-today");
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
    
    @FXML
    private void showCalendarView() {
        calendarView.setVisible(true);
        eventsListView.setVisible(false);
        calendarViewBtn.getStyleClass().add("active");
        listViewBtn.getStyleClass().remove("active");
    }
    
    @FXML
    private void showListView() {
        calendarView.setVisible(false);
        eventsListView.setVisible(true);
        eventsListView.setManaged(true);
        listViewBtn.getStyleClass().add("active");
        calendarViewBtn.getStyleClass().remove("active");
        loadEventsList();
    }
    
    private void loadEventsList() {
        eventsGrid.getChildren().clear();
        
        // Get only participating events for the current user
        List<Evenement> events = participationService.getParticipatingEvents(currentUser.getId());
        
        for (Evenement event : events) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EventCardStudent.fxml"));
                Node eventCard = loader.load();
                EventCardStudentController controller = loader.getController();
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

    public void refreshEvents() {
        loadEvents();
        updateCalendar();
        updateUpcomingEvents();
        if (eventsListView.isVisible()) {
            loadEventsList();
        }
    }
} 