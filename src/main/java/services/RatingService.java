package services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import utils.MyDabase;

public class RatingService {

    public boolean rateCourse(int userId, int courseId, int rate) {
        String checkQuery = "SELECT * FROM rate_course WHERE user_id = ? AND course_id = ?";
        String updateQuery = "UPDATE rate_course SET rate = ? WHERE user_id = ? AND course_id = ?";
        String insertQuery = "INSERT INTO rate_course (user_id, course_id, rate) VALUES (?, ?, ?)";

        try (Connection conn = MyDabase.getInstance().getConnection()) {
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, courseId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Already rated -> update
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setInt(1, rate);
                updateStmt.setInt(2, userId);
                updateStmt.setInt(3, courseId);
                updateStmt.executeUpdate();
            } else {
                // Not rated -> insert
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setInt(1, userId);
                insertStmt.setInt(2, courseId);
                insertStmt.setInt(3, rate);
                insertStmt.executeUpdate();
            }
            return true;

        } catch (Exception e) {
            System.out.println("Error rating course: " + e.getMessage());
            return false;
        }
    }
}
