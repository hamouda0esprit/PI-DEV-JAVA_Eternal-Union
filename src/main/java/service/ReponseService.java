package service;

import entite.Reponse;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReponseService {
    private Connection connection;

    public ReponseService() {
        try {
            connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            System.err.println("Erreur de connexion: " + e.getMessage());
        }
    }

    public boolean ajouter(Reponse reponse) {
        String query = "INSERT INTO reponses (questions_id, reponse, etat) VALUES (?, ?, ?)";
        
        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, reponse.getQuestions_id(), Types.INTEGER);
            ps.setString(2, reponse.getReponse());
            ps.setInt(3, reponse.getEtat());
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                // Récupérer l'ID généré
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        reponse.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la réponse: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean modifier(Reponse reponse) {
        String query = "UPDATE reponses SET questions_id=?, reponse=?, etat=? WHERE id=?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setObject(1, reponse.getQuestions_id(), Types.INTEGER);
            ps.setString(2, reponse.getReponse());
            ps.setInt(3, reponse.getEtat());
            ps.setInt(4, reponse.getId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification de la réponse: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean supprimer(int id) {
        String query = "DELETE FROM reponses WHERE id=?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la réponse: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Reponse> recupererTout() {
        List<Reponse> reponses = new ArrayList<>();
        String query = "SELECT * FROM reponses";
        
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                reponses.add(extractReponseFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des réponses: " + e.getMessage());
            e.printStackTrace();
        }
        
        return reponses;
    }

    public Reponse recupererParId(int id) {
        String query = "SELECT * FROM reponses WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractReponseFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la réponse: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    public List<Reponse> recupererParQuestionId(int questionId) {
        List<Reponse> reponses = new ArrayList<>();
        String query = "SELECT * FROM reponses WHERE questions_id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, questionId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reponses.add(extractReponseFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des réponses par question: " + e.getMessage());
            e.printStackTrace();
        }
        
        return reponses;
    }
    
    // Méthode pour marquer une réponse comme correcte (état = 1)
    public boolean marquerCommeCorrecte(int reponseId) {
        String query = "UPDATE reponses SET etat=1 WHERE id=?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, reponseId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors du marquage de la réponse comme correcte: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Méthode pour marquer une réponse comme incorrecte (état = 0)
    public boolean marquerCommeIncorrecte(int reponseId) {
        String query = "UPDATE reponses SET etat=0 WHERE id=?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, reponseId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors du marquage de la réponse comme incorrecte: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private Reponse extractReponseFromResultSet(ResultSet rs) throws SQLException {
        return new Reponse(
            rs.getInt("id"),
            rs.getObject("questions_id", Integer.class),
            rs.getString("reponse"),
            rs.getInt("etat")
        );
    }
} 