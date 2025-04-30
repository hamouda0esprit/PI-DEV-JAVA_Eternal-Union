package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DataSource;
import entite.Evenement;
import entite.User;

public class ParticipationService {
    private Connection conn;

    public ParticipationService() {
        conn = DataSource.getInstance() .getConncetion();
    }

    public void addParticipation(int evenementId, int userId) {
        String query = "INSERT INTO evenement_user (evenement_id, user_id) VALUES (?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, evenementId);
            pst.setInt(2, userId);
            pst.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("Error adding participation: " + ex.getMessage());
        }
    }

    public void removeParticipation(int evenementId, int userId) {
        String query = "DELETE FROM evenement_user WHERE evenement_id = ? AND user_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, evenementId);
            pst.setInt(2, userId);
            pst.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("Error removing participation: " + ex.getMessage());
        }
    }

    public boolean isParticipating(int evenementId, int userId) {
        String query = "SELECT * FROM evenement_user WHERE evenement_id = ? AND user_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, evenementId);
            pst.setInt(2, userId);
            ResultSet rs = pst.executeQuery();
            return rs.next();
        } catch (SQLException ex) {
            System.out.println("Error checking participation: " + ex.getMessage());
            return false;
        }
    }

    public List<Evenement> getParticipatingEvents(int userId) {
        List<Evenement> events = new ArrayList<>();
        String query = "SELECT e.* FROM evenement e " +
                      "JOIN evenement_user eu ON e.id = eu.evenement_id " +
                      "WHERE eu.user_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Evenement event = new Evenement();
                event.setId(rs.getInt("id"));
                event.setName(rs.getString("name"));
                event.setDateevent(rs.getDate("dateevent"));
                event.setTime(rs.getTime("time"));
                event.setLocation(rs.getString("location"));
                event.setDescription(rs.getString("description"));
                event.setPhoto(rs.getString("photo"));
                events.add(event);
            }
        } catch (SQLException ex) {
            System.out.println("Error getting participating events: " + ex.getMessage());
        }
        return events;
    }

    public int getParticipantCount(int evenementId) {
        String query = "SELECT COUNT(*) as count FROM evenement_user WHERE evenement_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, evenementId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException ex) {
            System.out.println("Error getting participant count: " + ex.getMessage());
        }
        return 0;
    }
} 