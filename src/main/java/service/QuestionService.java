package service;

import entite.Question;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionService {
    private Connection connection;

    public QuestionService() {
        try {
            connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            System.err.println("Erreur de connexion: " + e.getMessage());
        }
    }

    public boolean ajouter(Question question) {
        String query = "INSERT INTO questions (examen_id, nbr_points, question) VALUES (?, ?, ?)";
        
        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, question.getExamen_id(), Types.INTEGER);
            ps.setInt(2, question.getNbr_points());
            ps.setString(3, question.getQuestion());
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                // Récupérer l'ID généré
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        question.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la question: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean modifier(Question question) {
        String query = "UPDATE questions SET examen_id=?, nbr_points=?, question=? WHERE id=?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setObject(1, question.getExamen_id(), Types.INTEGER);
            ps.setInt(2, question.getNbr_points());
            ps.setString(3, question.getQuestion());
            ps.setInt(4, question.getId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification de la question: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean supprimer(int id) {
        String query = "DELETE FROM questions WHERE id=?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la question: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Question> recupererTout() {
        List<Question> questions = new ArrayList<>();
        String query = "SELECT * FROM questions";
        
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                questions.add(extractQuestionFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des questions: " + e.getMessage());
            e.printStackTrace();
        }
        
        return questions;
    }

    public Question recupererParId(int id) {
        String query = "SELECT * FROM questions WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractQuestionFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la question: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    public List<Question> recupererParExamenId(int examenId) {
        List<Question> questions = new ArrayList<>();
        String query = "SELECT * FROM questions WHERE examen_id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, examenId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    questions.add(extractQuestionFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des questions par examen: " + e.getMessage());
            e.printStackTrace();
        }
        
        return questions;
    }

    private Question extractQuestionFromResultSet(ResultSet rs) throws SQLException {
        return new Question(
            rs.getInt("id"),
            rs.getObject("examen_id", Integer.class),
            rs.getInt("nbr_points"),
            rs.getString("question")
        );
    }
} 