package service;

import entite.Examen;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamenService {
    private Connection connection;

    public ExamenService() {
        try {
            connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            System.err.println("Erreur de connexion: " + e.getMessage());
        }
    }

    public boolean ajouter(Examen examen) {
        String query = "INSERT INTO examen (id_user_id, note, description, date, matiere, duree, nbr_essai, type, titre) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setObject(1, examen.getIdUser(), Types.INTEGER);
            ps.setObject(2, examen.getNote(), Types.DOUBLE);
            ps.setString(3, examen.getDescription());
            ps.setDate(4, examen.getDate() != null ? new java.sql.Date(examen.getDate().getTime()) : null);
            ps.setString(5, examen.getMatiere());
            ps.setInt(6, examen.getDuree());
            ps.setInt(7, examen.getNbrEssai());
            ps.setString(8, examen.getType());
            ps.setString(9, examen.getTitre());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout d'examen: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean modifier(Examen examen) {
        String query = "UPDATE examen SET id_user_id=?, note=?, description=?, date=?, matiere=?, duree=?, nbr_essai=?, type=?, titre=? WHERE id=?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setObject(1, examen.getIdUser(), Types.INTEGER);
            ps.setObject(2, examen.getNote(), Types.DOUBLE);
            ps.setString(3, examen.getDescription());
            ps.setDate(4, examen.getDate() != null ? new java.sql.Date(examen.getDate().getTime()) : null);
            ps.setString(5, examen.getMatiere());
            ps.setInt(6, examen.getDuree());
            ps.setInt(7, examen.getNbrEssai());
            ps.setString(8, examen.getType());
            ps.setString(9, examen.getTitre());
            ps.setInt(10, examen.getId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification de l'examen: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean supprimer(int id) {
        String query = "DELETE FROM examen WHERE id=?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'examen: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Examen> recupererTout() {
        List<Examen> examens = new ArrayList<>();
        String query = "SELECT * FROM examen";
        
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                examens.add(extractExamenFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des examens: " + e.getMessage());
            e.printStackTrace();
        }
        
        return examens;
    }

    public Examen recupererParId(int id) {
        String query = "SELECT * FROM examen WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractExamenFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'examen: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    private Examen extractExamenFromResultSet(ResultSet rs) throws SQLException {
        Examen examen = new Examen();
        examen.setId(rs.getInt("id"));
        examen.setIdUser(rs.getObject("id_user_id", Integer.class));
        examen.setNote(rs.getObject("note", Double.class));
        examen.setDescription(rs.getString("description"));
        
        Date sqlDate = rs.getDate("date");
        if (sqlDate != null) {
            examen.setDate(new java.util.Date(sqlDate.getTime()));
        }
        
        examen.setMatiere(rs.getString("matiere"));
        examen.setDuree(rs.getInt("duree"));
        examen.setNbrEssai(rs.getInt("nbr_essai"));
        examen.setType(rs.getString("type"));
        examen.setTitre(rs.getString("titre"));
        
        return examen;
    }
} 