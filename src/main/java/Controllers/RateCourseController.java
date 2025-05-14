package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;
import models.Cours;
import services.RatingService;

public class RateCourseController {

    @FXML
    private Spinner<Integer> rateSpinner;

    @FXML
    private Label messageLabel;

    private Cours course;
    private int userId;

    public void setCourse(Cours course, int userId) {
        this.course = course;
        this.userId = userId;
    }

    @FXML
    public void initialize() {
        rateSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 0));
    }

    @FXML
    public void handleSubmit() {
        int rating = rateSpinner.getValue();

        RatingService service = new RatingService();
        boolean success = service.rateCourse(userId, course.getId(), rating);

        if (success) {
            messageLabel.setText("Rating saved!");
            ((Stage) rateSpinner.getScene().getWindow()).close(); // Close the popup
        } else {
            messageLabel.setText("Error saving rating.");
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }
}
