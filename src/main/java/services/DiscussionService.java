package services;

import entities.Discussion;
import utils.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DiscussionService {
    private Connection conn;

    public DiscussionService() {
        conn = DataSource.getInstance().getCnx();
    }

    public List<Discussion> getDiscussionsForEvent(int eventId) {
        List<Discussion> eventDiscussions = new ArrayList<>();
        String query = "SELECT * FROM discussion WHERE event_id = ? ORDER BY created_at DESC";
        
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, eventId);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                Discussion discussion = new Discussion(
                    rs.getInt("id"),
                    rs.getInt("event_id"),
                    rs.getString("caption"),
                    rs.getString("media"),
                    rs.getTimestamp("created_at").toLocalDateTime()
                );
                eventDiscussions.add(discussion);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching discussions: " + e.getMessage());
        }
        
        return eventDiscussions;
    }

    public boolean addDiscussion(Discussion discussion) {
        String query = "INSERT INTO discussion (event_id, caption, media, created_at) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pst = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, discussion.getEventId());
            pst.setString(2, discussion.getCaption());
            pst.setString(3, discussion.getMedia());
            pst.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            
            int affectedRows = pst.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = pst.getGeneratedKeys();
                if (generatedKeys.next()) {
                    discussion.setId(generatedKeys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding discussion: " + e.getMessage());
        }
        
        return false;
    }

    public void removeDiscussion(int discussionId) {
        String query = "DELETE FROM discussion WHERE id = ?";
        
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, discussionId);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error removing discussion: " + e.getMessage());
        }
    }

    public void updateDiscussion(Discussion discussion) {
        String query = "UPDATE discussion SET caption = ?, media = ? WHERE id = ?";
        
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, discussion.getCaption());
            pst.setString(2, discussion.getMedia());
            pst.setInt(3, discussion.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating discussion: " + e.getMessage());
        }
    }

    public void deleteDiscussionsForEvent(int eventId) {
        try {
            String query = "DELETE FROM discussion WHERE event_id = ?";
            PreparedStatement ps = DataSource.getInstance().getCnx().prepareStatement(query);
            ps.setInt(1, eventId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error deleting discussions for event: " + e.getMessage());
        }
    }
} 