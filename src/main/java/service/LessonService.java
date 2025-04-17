package service;

import models.Lesson;
import utils.MyDabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LessonService {

    private Connection connection;

    public LessonService() {
        connection = MyDabase.getInstance().getConnection();
    }

    public void ajouter(Lesson lesson) throws SQLException {
        String sql = "INSERT INTO lesson (course_id, title, description) VALUES (?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, lesson.getCourseId());
        ps.setString(2, lesson.getTitle());
        ps.setString(3, lesson.getDescription());
        ps.executeUpdate();
    }

    public List<Lesson> recuperer() throws SQLException {
        List<Lesson> list = new ArrayList<>();
        String sql = "SELECT * FROM lesson";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            list.add(new Lesson(
                    rs.getInt("id"),
                    rs.getInt("course_id"),
                    rs.getString("title"),
                    rs.getString("description")
            ));
        }
        return list;
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM lesson WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public List<Lesson> getLessonsByCourse(int courseId) {
        List<Lesson> list = new ArrayList<>();
        try {
            String sql = "SELECT * FROM lesson WHERE course_id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, courseId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Lesson(
                        rs.getInt("id"),
                        rs.getInt("course_id"),
                        rs.getString("title"),
                        rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching lessons: " + e.getMessage());
            e.printStackTrace();
        }
        return list; // Returns empty list on error
    }

    public void update(Lesson lesson) throws SQLException {
        String sql = "UPDATE lesson SET title = ?, description = ? WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, lesson.getTitle());
        ps.setString(2, lesson.getDescription());
        ps.setInt(3, lesson.getId());
        ps.executeUpdate();
    }
}