package service;

import entite.Feedback;
import entite.Examen;
import entite.User;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service qui gère les opérations CRUD pour les feedbacks des examens
 */
public class FeedbackService {
    private Connection connection;
    private ExamenService examenService;
    private UserService userService;

    /**
     * Constructeur qui initialise la connexion à la base de données
     */
    public FeedbackService() {
        try {
            connection = DatabaseConnection.getConnection();
            examenService = new ExamenService();
            userService = new UserService();
            
            // Vérifier que la table existe, sinon la créer
            verifierTableFeedback();
        } catch (SQLException e) {
            System.err.println("Erreur de connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ajoute un nouveau feedback dans la base de données
     * @param feedback Le feedback à ajouter
     * @return true si l'ajout a réussi, false sinon
     */
    public boolean ajouter(Feedback feedback) {
        String query = "INSERT INTO feedback (examen_id, user_id, contenu, date_creation) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, feedback.getExamen_id(), Types.INTEGER);
            ps.setObject(2, feedback.getUser_id(), Types.INTEGER);
            ps.setString(3, feedback.getContenu());
            ps.setTimestamp(4, feedback.getDate_creation() != null ? 
                            new java.sql.Timestamp(feedback.getDate_creation().getTime()) : 
                            new java.sql.Timestamp(System.currentTimeMillis()));
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        feedback.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du feedback: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Récupère un feedback par son id
     * @param id L'id du feedback à récupérer
     * @return Le feedback correspondant ou null si aucun trouvé
     */
    public Feedback recupererParId(int id) {
        String query = "SELECT * FROM feedback WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractFeedbackFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du feedback: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Récupère tous les feedbacks d'un examen
     * @param examenId L'id de l'examen
     * @return La liste des feedbacks pour cet examen
     */
    public List<Feedback> recupererParExamen(int examenId) {
        List<Feedback> feedbacks = new ArrayList<>();
        String query = "SELECT * FROM feedback WHERE examen_id = ? ORDER BY date_creation DESC";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, examenId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    feedbacks.add(extractFeedbackFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des feedbacks par examen: " + e.getMessage());
            e.printStackTrace();
        }
        
        return feedbacks;
    }
    
    /**
     * Récupère tous les feedbacks d'un utilisateur
     * @param userId L'id de l'utilisateur
     * @return La liste des feedbacks pour cet utilisateur
     */
    public List<Feedback> recupererParUtilisateur(int userId) {
        List<Feedback> feedbacks = new ArrayList<>();
        String query = "SELECT * FROM feedback WHERE user_id = ? ORDER BY date_creation DESC";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    feedbacks.add(extractFeedbackFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des feedbacks par utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
        
        return feedbacks;
    }
    
    /**
     * Vérifie si un utilisateur a déjà donné un feedback pour un examen
     * @param userId L'id de l'utilisateur
     * @param examenId L'id de l'examen
     * @return true si un feedback existe, false sinon
     */
    public boolean verifierExistenceFeedback(int userId, int examenId) {
        String query = "SELECT COUNT(*) FROM feedback WHERE user_id = ? AND examen_id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, examenId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de l'existence du feedback: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Extrait un objet Feedback d'un ResultSet
     * @param rs Le ResultSet contenant les données
     * @return L'objet Feedback créé
     * @throws SQLException En cas d'erreur lors de l'extraction
     */
    private Feedback extractFeedbackFromResultSet(ResultSet rs) throws SQLException {
        Feedback feedback = new Feedback();
        feedback.setId(rs.getInt("id"));
        
        Integer examenId = rs.getObject("examen_id", Integer.class);
        feedback.setExamen_id(examenId);
        if (examenId != null) {
            Examen examen = examenService.recupererParId(examenId);
            feedback.setExamen(examen);
        }
        
        Integer userId = rs.getObject("user_id", Integer.class);
        feedback.setUser_id(userId);
        if (userId != null) {
            User user = userService.getUserById(userId);
            feedback.setUser(user);
        }
        
        feedback.setContenu(rs.getString("contenu"));
        
        Timestamp dateCreation = rs.getTimestamp("date_creation");
        if (dateCreation != null) {
            feedback.setDate_creation(new java.util.Date(dateCreation.getTime()));
        }
        
        return feedback;
    }
    
    /**
     * Vérifie si la table feedback existe et la crée si nécessaire
     * @return true si la vérification a réussi, false sinon
     */
    public boolean verifierTableFeedback() {
        try {
            // Vérifier si la table existe
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "feedback", null);
            
            if (!tables.next()) {
                // La table n'existe pas, on la crée
                return creerTableFeedback();
            }
            
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de la table feedback: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Crée la table feedback si elle n'existe pas
     * @return true si la création a réussi, false sinon
     */
    private boolean creerTableFeedback() {
        String createTableSQL = "CREATE TABLE feedback (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "examen_id INT, " +
                "user_id INT, " +
                "contenu TEXT, " +
                "date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (examen_id) REFERENCES examen(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE" +
                ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("Table feedback créée avec succès");
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de la table feedback: " + e.getMessage());
            e.printStackTrace();
            
            // Essayer sans les contraintes de clés étrangères au cas où
            String simpleCreateTableSQL = "CREATE TABLE feedback (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "examen_id INT, " +
                    "user_id INT, " +
                    "contenu TEXT, " +
                    "date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            
            try (Statement simpleStmt = connection.createStatement()) {
                simpleStmt.execute(simpleCreateTableSQL);
                System.out.println("Table feedback créée sans contraintes de clés étrangères");
                return true;
            } catch (SQLException e2) {
                System.err.println("Seconde erreur lors de la création de la table feedback: " + e2.getMessage());
                e2.printStackTrace();
                return false;
            }
        }
    }
} 