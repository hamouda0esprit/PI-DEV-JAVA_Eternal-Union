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
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        question.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de question: " + e.getMessage());
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
        try {
            connection.setAutoCommit(false);
            
            // 1. D'abord supprimer les réponses associées à cette question
            String deleteReponsesQuery = "DELETE FROM reponses WHERE questions_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(deleteReponsesQuery)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            
            // 2. Ensuite supprimer la question
            String deleteQuestionQuery = "DELETE FROM questions WHERE id = ?";
            try (PreparedStatement ps = connection.prepareStatement(deleteQuestionQuery)) {
                ps.setInt(1, id);
                int result = ps.executeUpdate();
                
                connection.commit();
                return result > 0;
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.err.println("Erreur lors du rollback: " + ex.getMessage());
                ex.printStackTrace();
            }
            System.err.println("Erreur lors de la suppression de la question et ses réponses: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Erreur lors de la restauration de l'autocommit: " + e.getMessage());
                e.printStackTrace();
            }
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
    
    public List<Question> recupererParExamen(int examenId) {
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
    
    public int getLastInsertedId() {
        String query = "SELECT LAST_INSERT_ID()";
        
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du dernier ID inséré: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }

    private Question extractQuestionFromResultSet(ResultSet rs) throws SQLException {
        Question question = new Question();
        question.setId(rs.getInt("id"));
        question.setExamen_id(rs.getObject("examen_id", Integer.class));
        question.setNbr_points(rs.getInt("nbr_points"));
        question.setQuestion(rs.getString("question"));
        
        return question;
    }
} 