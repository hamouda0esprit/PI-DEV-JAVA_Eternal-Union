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
        try {
            connection.setAutoCommit(false);
            
            // 1. D'abord supprimer les entrées feedback liées à cet examen
            String deleteFeedbackQuery = "DELETE FROM feedback WHERE examen_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(deleteFeedbackQuery)) {
                ps.setInt(1, id);
                ps.executeUpdate();
                System.out.println("Suppression des feedbacks pour l'examen " + id);
            }
            
            // 2. Supprimer les résultats de quiz associés à cet examen
            String deleteResultatsQuery = "DELETE FROM resultat_quiz WHERE examen_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(deleteResultatsQuery)) {
                ps.setInt(1, id);
                ps.executeUpdate();
                System.out.println("Suppression des résultats de quiz pour l'examen " + id);
            }
            
            // 3. Supprimer les réponses associées aux questions de cet examen
            String deleteReponsesQuery = "DELETE FROM reponses WHERE questions_id IN (SELECT id FROM questions WHERE examen_id = ?)";
            try (PreparedStatement ps = connection.prepareStatement(deleteReponsesQuery)) {
                ps.setInt(1, id);
                ps.executeUpdate();
                System.out.println("Suppression des réponses pour l'examen " + id);
            }
            
            // 4. Ensuite supprimer les questions associées à cet examen
            String deleteQuestionsQuery = "DELETE FROM questions WHERE examen_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(deleteQuestionsQuery)) {
                ps.setInt(1, id);
                ps.executeUpdate();
                System.out.println("Suppression des questions pour l'examen " + id);
            }
            
            // 5. Finalement supprimer l'examen
            String deleteExamenQuery = "DELETE FROM examen WHERE id = ?";
            try (PreparedStatement ps = connection.prepareStatement(deleteExamenQuery)) {
                ps.setInt(1, id);
                int result = ps.executeUpdate();
                System.out.println("Suppression de l'examen " + id + " : " + (result > 0 ? "réussie" : "échouée"));
                
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
            System.err.println("Erreur lors de la suppression de l'examen et ses dépendances: " + e.getMessage());
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

    /**
     * Recherche des examens selon différents critères
     * 
     * @param searchTerm Terme de recherche pour le titre, la description ou la matière
     * @param idUser ID de l'utilisateur si on veut limiter les résultats à un utilisateur spécifique (peut être null)
     * @param matiere Matière spécifique à rechercher (peut être null)
     * @param type Type d'examen à rechercher (peut être null)
     * @return Liste des examens correspondant aux critères de recherche
     */
    public List<Examen> rechercherExamens(String searchTerm, Integer idUser, String matiere, String type) {
        List<Examen> examens = new ArrayList<>();
        
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT * FROM examen WHERE 1=1");
        
        // Liste pour stocker les paramètres
        List<Object> parameters = new ArrayList<>();
        
        // Ajouter les conditions selon les paramètres fournis
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String searchParam = "%" + searchTerm.trim() + "%";
            queryBuilder.append(" AND (titre LIKE ? OR description LIKE ? OR matiere LIKE ?)");
            parameters.add(searchParam);
            parameters.add(searchParam);
            parameters.add(searchParam);
        }
        
        if (idUser != null) {
            queryBuilder.append(" AND id_user_id = ?");
            parameters.add(idUser);
        }
        
        if (matiere != null && !matiere.trim().isEmpty()) {
            queryBuilder.append(" AND matiere = ?");
            parameters.add(matiere);
        }
        
        if (type != null && !type.trim().isEmpty()) {
            queryBuilder.append(" AND type = ?");
            parameters.add(type);
        }
        
        // Ajouter un tri par défaut
        queryBuilder.append(" ORDER BY date DESC");
        
        try (PreparedStatement ps = connection.prepareStatement(queryBuilder.toString())) {
            // Définir les paramètres
            for (int i = 0; i < parameters.size(); i++) {
                ps.setObject(i + 1, parameters.get(i));
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    examens.add(extractExamenFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des examens: " + e.getMessage());
            e.printStackTrace();
        }
        
        return examens;
    }
    
    /**
     * Récupère la liste des matières distinctes présentes dans les examens
     * @return Liste des matières
     */
    public List<String> recupererMatieresDistinctes() {
        List<String> matieres = new ArrayList<>();
        String query = "SELECT DISTINCT matiere FROM examen WHERE matiere IS NOT NULL AND matiere <> '' ORDER BY matiere";
        
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                matieres.add(rs.getString("matiere"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des matières: " + e.getMessage());
            e.printStackTrace();
        }
        
        return matieres;
    }
    
    /**
     * Récupère la liste des types d'examens distincts
     * @return Liste des types d'examens
     */
    public List<String> recupererTypesDistincts() {
        List<String> types = new ArrayList<>();
        String query = "SELECT DISTINCT type FROM examen WHERE type IS NOT NULL AND type <> '' ORDER BY type";
        
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                types.add(rs.getString("type"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des types d'examens: " + e.getMessage());
            e.printStackTrace();
        }
        
        return types;
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