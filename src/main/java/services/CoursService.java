package services;

import models.Cours;
import utils.MyDabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CoursService implements IService<Cours> {

    private final Connection connection;

    public CoursService() {
        this.connection = MyDabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Cours cours) throws SQLException {
        String sql = "INSERT INTO course (user_id, title, image, subject, rate, last_update) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, cours.getUserId());
            ps.setString(2, cours.getTitle());
            ps.setString(3, cours.getImage());
            ps.setString(4, cours.getSubject());
            ps.setInt(5, cours.getRate());
            if (cours.getLastUpdate() != null) {
                ps.setTimestamp(6, Timestamp.valueOf(cours.getLastUpdate()));
            } else {
                ps.setNull(6, Types.TIMESTAMP);
            }

            ps.executeUpdate();
        }
    }

    @Override
    public void modifier(Cours cours) throws SQLException {
        String sql = "UPDATE course SET user_id = ?, title = ?, image = ?, subject = ?, rate = ?, last_update = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, cours.getUserId());
            ps.setString(2, cours.getTitle());
            ps.setString(3, cours.getImage());
            ps.setString(4, cours.getSubject());
            ps.setInt(5, cours.getRate());
            if (cours.getLastUpdate() != null) {
                ps.setTimestamp(6, Timestamp.valueOf(cours.getLastUpdate()));
            } else {
                ps.setNull(6, Types.TIMESTAMP);
            }
            ps.setInt(7, cours.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void sipprimer(int id) throws SQLException {
        String sql = "DELETE FROM course WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Cours> recuperer() throws SQLException {
        String sql = "SELECT * FROM course";
        List<Cours> coursList = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Cours cours = new Cours();
                cours.setId(rs.getInt("id"));
                cours.setUserId(rs.getInt("user_id"));
                cours.setTitle(rs.getString("title"));
                cours.setImage(rs.getString("image"));
                cours.setSubject(rs.getString("subject"));
                cours.setRate(rs.getInt("rate"));

                Timestamp ts = rs.getTimestamp("last_update");
                cours.setLastUpdate(ts != null ? ts.toLocalDateTime() : null);

                coursList.add(cours);
            }
        }

        return coursList;
    }
}
