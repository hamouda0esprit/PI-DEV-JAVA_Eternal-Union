package services;

import models.Cours;
import utils.MyDabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CoursService implements IService<Cours> {

    private Connection connection;

    public CoursService() {
        connection = MyDabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Cours cours) throws SQLException {
        String sql = "INSERT INTO course (user_id, title, image, subject, rate, last_update) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, cours.getUserId());
        preparedStatement.setString(2, cours.getTitle());
        preparedStatement.setString(3, cours.getImage());
        preparedStatement.setString(4, cours.getSubject());
        preparedStatement.setInt(5, cours.getRate());
        preparedStatement.setTimestamp(6, Timestamp.valueOf(cours.getLastUpdate()));

        preparedStatement.executeUpdate();
    }

    @Override
    public void modifier(Cours cours) throws SQLException {
        String sql = "UPDATE course SET user_id = ?, title = ?, image = ?, subject = ?, rate = ?, last_update = ? WHERE id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, cours.getUserId());
        preparedStatement.setString(2, cours.getTitle());
        preparedStatement.setString(3, cours.getImage());
        preparedStatement.setString(4, cours.getSubject());
        preparedStatement.setInt(5, cours.getRate());
        preparedStatement.setTimestamp(6, Timestamp.valueOf(cours.getLastUpdate()));
        preparedStatement.setInt(7, cours.getId());

        preparedStatement.executeUpdate();
    }

    @Override
    public void sipprimer(int id) throws SQLException {
        String sql = "DELETE FROM course WHERE id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, id);
        preparedStatement.executeUpdate();
    }

    @Override
    public List<Cours> recuperer() throws SQLException {
        String sql = "SELECT * FROM course";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        List<Cours> coursList = new ArrayList<>();

        while (rs.next()) {
            Cours cours = new Cours();
            cours.setId(rs.getInt("id"));
            cours.setUserId(rs.getInt("user_id"));
            cours.setTitle(rs.getString("title"));
            cours.setImage(rs.getString("image"));
            cours.setSubject(rs.getString("subject"));
            cours.setRate(rs.getInt("rate"));
            Timestamp timestamp = rs.getTimestamp("last_update");
            if (timestamp != null) {
                cours.setLastUpdate(timestamp.toLocalDateTime());
            }
            coursList.add(cours);
        }

        return coursList;
    }
}
