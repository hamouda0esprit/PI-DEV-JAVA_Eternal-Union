package service;

import entite.Rating;
import entite.Examen;
import entite.User;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service qui gère les opérations CRUD pour les évaluations des examens
 */
public class RatingService {
    private Connection connection;
    private ExamenService examenService;
    private UserService userService;

    /**
     * Constructeur qui initialise la connexion à la base de données
     */
    public RatingService() {
        try {
            connection = DatabaseConnection.getConnection();
            examenService = new ExamenService();
            userService = new UserService();
            
            // Vérifier que la table existe, sinon la créer
            if (!verifierTableRating()) {
                // Si la table existe mais a un mauvais schéma, on tente de la recréer
                recreerTableRating();
            }
        } catch (SQLException e) {
            System.err.println("Erreur de connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ajoute une nouvelle évaluation dans la base de données
     * @param rating L'évaluation à ajouter
     * @return true si l'ajout a réussi, false sinon
     */
    public boolean ajouter(Rating rating) {
        String query = "INSERT INTO rating (examen_id, student_id, stars, created_at) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, rating.getExamen_id(), Types.INTEGER);
            ps.setObject(2, rating.getUser_id(), Types.INTEGER);
            ps.setObject(3, rating.getStars(), Types.INTEGER);
            ps.setTimestamp(4, rating.getCreated_at() != null ? 
                            new java.sql.Timestamp(rating.getCreated_at().getTime()) : 
                            new java.sql.Timestamp(System.currentTimeMillis()));
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        rating.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de l'évaluation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Récupère une évaluation par son id
     * @param id L'id de l'évaluation à récupérer
     * @return L'évaluation correspondante ou null si aucune trouvée
     */
    public Rating recupererParId(int id) {
        String query = "SELECT * FROM rating WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractRatingFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'évaluation: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Récupère toutes les évaluations d'un examen
     * @param examenId L'id de l'examen
     * @return La liste des évaluations pour cet examen
     */
    public List<Rating> recupererParExamen(int examenId) {
        List<Rating> ratings = new ArrayList<>();
        String query = "SELECT * FROM rating WHERE examen_id = ? ORDER BY created_at DESC";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, examenId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ratings.add(extractRatingFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des évaluations par examen: " + e.getMessage());
            e.printStackTrace();
        }
        
        return ratings;
    }
    
    /**
     * Récupère l'évaluation d'un utilisateur pour un examen spécifique
     * @param userId L'id de l'utilisateur
     * @param examenId L'id de l'examen
     * @return L'évaluation correspondante ou null si aucune trouvée
     */
    public Rating recupererParUtilisateurEtExamen(int userId, int examenId) {
        String query = "SELECT * FROM rating WHERE student_id = ? AND examen_id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, examenId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractRatingFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'évaluation par utilisateur et examen: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Vérifie si un utilisateur a déjà donné une évaluation pour un examen
     * @param userId L'id de l'utilisateur
     * @param examenId L'id de l'examen
     * @return true si une évaluation existe, false sinon
     */
    public boolean verifierExistenceRating(int userId, int examenId) {
        String query = "SELECT COUNT(*) FROM rating WHERE student_id = ? AND examen_id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, examenId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de l'existence de l'évaluation: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Calcule la moyenne des évaluations pour un examen
     * @param examenId L'id de l'examen
     * @return La moyenne des évaluations ou 0 si aucune évaluation
     */
    public double calculerMoyenneExamen(int examenId) {
        String query = "SELECT AVG(stars) FROM rating WHERE examen_id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, examenId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul de la moyenne des évaluations: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Compte le nombre d'évaluations pour un examen
     * @param examenId L'id de l'examen
     * @return Le nombre d'évaluations
     */
    public int compterEvaluationsExamen(int examenId) {
        String query = "SELECT COUNT(*) FROM rating WHERE examen_id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, examenId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des évaluations: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }

    /**
     * Extrait un objet Rating d'un ResultSet
     * @param rs Le ResultSet contenant les données
     * @return L'objet Rating créé
     * @throws SQLException En cas d'erreur lors de l'extraction
     */
    private Rating extractRatingFromResultSet(ResultSet rs) throws SQLException {
        Rating rating = new Rating();
        rating.setId(rs.getInt("id"));
        
        Integer examenId = rs.getObject("examen_id", Integer.class);
        rating.setExamen_id(examenId);
        if (examenId != null) {
            Examen examen = examenService.recupererParId(examenId);
            rating.setExamen(examen);
        }
        
        Integer userId = rs.getObject("student_id", Integer.class);
        rating.setUser_id(userId);
        if (userId != null) {
            User user = userService.getUserById(userId);
            rating.setUser(user);
        }
        
        rating.setStars(rs.getObject("stars", Integer.class));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            rating.setCreated_at(new java.util.Date(createdAt.getTime()));
        }
        
        return rating;
    }
    
    /**
     * Vérifie si la table rating existe et a la bonne structure
     * Si elle n'existe pas, on la crée
     * @return true si la vérification est réussie, false sinon
     */
    public boolean verifierTableRating() {
        try {
            // Vérifier si la table existe
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "rating", null);
            
            if (!tables.next()) {
                // La table n'existe pas, on la crée
                return creerTableRating();
            } else {
                // La table existe, vérifier si elle a la bonne structure
                ResultSet columns = dbm.getColumns(null, null, "rating", "student_id");
                if (!columns.next()) {
                    // La colonne student_id n'existe pas, on doit recréer la table
                    System.out.println("La table rating existe mais n'a pas la bonne structure");
                    return false;
                }
            }
            
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de la table rating: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Recrée la table rating avec la bonne structure
     * @return true si la recréation a réussi, false sinon
     */
    public boolean recreerTableRating() {
        try {
            System.out.println("Tentative de recréation de la table rating...");
            
            // Supprimer l'ancienne table si elle existe
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS rating");
                System.out.println("Ancienne table rating supprimée");
            } catch (SQLException e) {
                System.err.println("Erreur lors de la suppression de l'ancienne table rating: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
            
            // Créer la nouvelle table
            return creerTableRating();
        } catch (Exception e) {
            System.err.println("Erreur lors de la recréation de la table rating: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Crée la table rating si elle n'existe pas
     * @return true si la création a réussi, false sinon
     */
    private boolean creerTableRating() {
        String createTableSQL = "CREATE TABLE rating (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "examen_id INT, " +
                "student_id INT, " +
                "stars INT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (examen_id) REFERENCES examen(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (student_id) REFERENCES user(id) ON DELETE CASCADE" +
                ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("Table rating créée avec succès");
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de la table rating: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Met à jour une évaluation existante
     * @param rating L'évaluation à mettre à jour
     * @return true si la mise à jour a réussi, false sinon
     */
    public boolean mettreAJour(Rating rating) {
        String query = "UPDATE rating SET stars = ? WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, rating.getStars());
            ps.setInt(2, rating.getId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de l'évaluation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Supprime une évaluation
     * @param id L'id de l'évaluation à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    public boolean supprimer(int id) {
        String query = "DELETE FROM rating WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'évaluation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
} 