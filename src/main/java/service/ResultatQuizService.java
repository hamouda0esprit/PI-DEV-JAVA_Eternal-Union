package service;

import entite.ResultatQuiz;
import entite.Examen;
import entite.User;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service qui gère les opérations CRUD pour les résultats de quiz
 */
public class ResultatQuizService {
    private Connection connection;
    private ExamenService examenService;
    private UserService userService;

    /**
     * Constructeur qui initialise la connexion à la base de données
     */
    public ResultatQuizService() {
        try {
            connection = DatabaseConnection.getConnection();
            examenService = new ExamenService();
            userService = new UserService();
        } catch (SQLException e) {
            System.err.println("Erreur de connexion: " + e.getMessage());
        }
    }

    /**
     * Ajoute un nouveau résultat de quiz dans la base de données
     * @param resultat Le résultat à ajouter
     * @return true si l'ajout a réussi, false sinon
     */
    public boolean ajouter(ResultatQuiz resultat) {
        try {
            // Vérifier si la colonne nbr_essai existe dans la table
            boolean hasNbrEssaiColumn = checkIfColumnExists("resultat_quiz", "nbr_essai");
            
            String query;
            if (hasNbrEssaiColumn) {
                // Si la colonne existe, l'inclure dans la requête
                query = "INSERT INTO resultat_quiz (examen_id, id_user_id, score, total_points, date_passage, nbr_essai) VALUES (?, ?, ?, ?, ?, ?)";
            } else {
                // Si la colonne n'existe pas, utiliser une requête sans cette colonne
                query = "INSERT INTO resultat_quiz (examen_id, id_user_id, score, total_points, date_passage) VALUES (?, ?, ?, ?, ?)";
                // Et essayer d'ajouter la colonne pour les prochains appels
                try {
                    Statement stmt = connection.createStatement();
                    stmt.execute("ALTER TABLE resultat_quiz ADD COLUMN nbr_essai INT NOT NULL DEFAULT 0");
                    stmt.close();
                    System.out.println("Colonne nbr_essai ajoutée à la table resultat_quiz");
                } catch (SQLException e) {
                    System.err.println("Impossible d'ajouter la colonne nbr_essai: " + e.getMessage());
                }
            }
            
            // S'assurer qu'un nombre d'essais est défini, sinon le définir à partir de l'examen
            if (resultat.getNbrEssai() <= 0 && resultat.getExamen_id() != null) {
                ExamenService examenService = new ExamenService();
                Examen examen = examenService.recupererParId(resultat.getExamen_id());
                if (examen != null) {
                    int nbrEssaiInitial = examen.getNbrEssai() > 0 ? examen.getNbrEssai() - 1 : 0;
                    resultat.setNbrEssai(nbrEssaiInitial);
                    System.out.println("Nombre d'essais défini à partir de l'examen: " + nbrEssaiInitial);
                }
            }
            
            try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                ps.setObject(1, resultat.getExamen_id(), Types.INTEGER);
                ps.setObject(2, resultat.getId_user_id(), Types.INTEGER);
                ps.setInt(3, resultat.getScore());
                ps.setInt(4, resultat.getTotalPoints());
                ps.setTimestamp(5, resultat.getDatePassage() != null ? new java.sql.Timestamp(resultat.getDatePassage().getTime()) : null);
                
                if (hasNbrEssaiColumn) {
                    ps.setInt(6, resultat.getNbrEssai());
                }
                
                int affectedRows = ps.executeUpdate();
                
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            resultat.setId(generatedKeys.getInt(1));
                        }
                    }
                    return true;
                }
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du résultat de quiz: " + e.getMessage());
            e.printStackTrace();
            
            // Tentative d'insertion sans la colonne nbr_essai comme dernier recours
            try {
                String basicQuery = "INSERT INTO resultat_quiz (examen_id, id_user_id, score, total_points, date_passage) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement ps = connection.prepareStatement(basicQuery, Statement.RETURN_GENERATED_KEYS);
                ps.setObject(1, resultat.getExamen_id(), Types.INTEGER);
                ps.setObject(2, resultat.getId_user_id(), Types.INTEGER);
                ps.setInt(3, resultat.getScore());
                ps.setInt(4, resultat.getTotalPoints());
                ps.setTimestamp(5, resultat.getDatePassage() != null ? new java.sql.Timestamp(resultat.getDatePassage().getTime()) : null);
                
                int affectedRows = ps.executeUpdate();
                
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            resultat.setId(generatedKeys.getInt(1));
                        }
                    }
                    System.out.println("Insertion réussie sans la colonne nbr_essai");
                    return true;
                }
                return false;
            } catch (SQLException e2) {
                System.err.println("Échec de l'insertion basique: " + e2.getMessage());
                e2.printStackTrace();
                return false;
            }
        }
    }

    /**
     * Vérifie si une colonne existe dans une table
     * @param tableName Nom de la table
     * @param columnName Nom de la colonne
     * @return true si la colonne existe, false sinon
     */
    private boolean checkIfColumnExists(String tableName, String columnName) throws SQLException {
        DatabaseMetaData dbm = connection.getMetaData();
        ResultSet columns = dbm.getColumns(null, null, tableName, columnName);
        return columns.next();
    }

    /**
     * Met à jour un résultat de quiz existant
     * @param resultat Le résultat à mettre à jour
     * @return true si la mise à jour a réussi, false sinon
     */
    public boolean modifier(ResultatQuiz resultat) {
        try {
            // Vérifier si la colonne nbr_essai existe dans la table
            boolean hasNbrEssaiColumn = checkIfColumnExists("resultat_quiz", "nbr_essai");
            
            String query;
            if (hasNbrEssaiColumn) {
                // Si la colonne existe, l'inclure dans la requête
                query = "UPDATE resultat_quiz SET examen_id=?, id_user_id=?, score=?, total_points=?, date_passage=?, nbr_essai=? WHERE id=?";
            } else {
                // Si la colonne n'existe pas, utiliser une requête sans cette colonne
                query = "UPDATE resultat_quiz SET examen_id=?, id_user_id=?, score=?, total_points=?, date_passage=? WHERE id=?";
                // Et essayer d'ajouter la colonne pour les prochains appels
                try {
                    Statement stmt = connection.createStatement();
                    stmt.execute("ALTER TABLE resultat_quiz ADD COLUMN nbr_essai INT NOT NULL DEFAULT 0");
                    stmt.close();
                    System.out.println("Colonne nbr_essai ajoutée à la table resultat_quiz");
                    // Maintenant que la colonne existe, utiliser la requête complète
                    query = "UPDATE resultat_quiz SET examen_id=?, id_user_id=?, score=?, total_points=?, date_passage=?, nbr_essai=? WHERE id=?";
                    hasNbrEssaiColumn = true;
                } catch (SQLException e) {
                    System.err.println("Impossible d'ajouter la colonne nbr_essai: " + e.getMessage());
                }
            }
            
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setObject(1, resultat.getExamen_id(), Types.INTEGER);
                ps.setObject(2, resultat.getId_user_id(), Types.INTEGER);
                ps.setInt(3, resultat.getScore());
                ps.setInt(4, resultat.getTotalPoints());
                ps.setTimestamp(5, resultat.getDatePassage() != null ? new java.sql.Timestamp(resultat.getDatePassage().getTime()) : null);
                
                if (hasNbrEssaiColumn) {
                    ps.setInt(6, resultat.getNbrEssai());
                    ps.setInt(7, resultat.getId());
                } else {
                    ps.setInt(6, resultat.getId());
                }
                
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification du résultat: " + e.getMessage());
            e.printStackTrace();
            
            // Tentative de mise à jour sans la colonne nbr_essai comme dernier recours
            try {
                String basicQuery = "UPDATE resultat_quiz SET examen_id=?, id_user_id=?, score=?, total_points=?, date_passage=? WHERE id=?";
                PreparedStatement ps = connection.prepareStatement(basicQuery);
                ps.setObject(1, resultat.getExamen_id(), Types.INTEGER);
                ps.setObject(2, resultat.getId_user_id(), Types.INTEGER);
                ps.setInt(3, resultat.getScore());
                ps.setInt(4, resultat.getTotalPoints());
                ps.setTimestamp(5, resultat.getDatePassage() != null ? new java.sql.Timestamp(resultat.getDatePassage().getTime()) : null);
                ps.setInt(6, resultat.getId());
                
                boolean success = ps.executeUpdate() > 0;
                if (success) {
                    System.out.println("Mise à jour réussie sans la colonne nbr_essai");
                }
                return success;
            } catch (SQLException e2) {
                System.err.println("Échec de la mise à jour basique: " + e2.getMessage());
                e2.printStackTrace();
                return false;
            }
        }
    }

    /**
     * Supprime un résultat de quiz
     * @param id L'id du résultat à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    public boolean supprimer(int id) {
        String query = "DELETE FROM resultat_quiz WHERE id=?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du résultat: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Récupère tous les résultats de quiz
     * @return La liste de tous les résultats
     */
    public List<ResultatQuiz> recupererTout() {
        List<ResultatQuiz> resultats = new ArrayList<>();
        String query = "SELECT * FROM resultat_quiz";
        
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                resultats.add(extractResultatFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des résultats: " + e.getMessage());
            e.printStackTrace();
        }
        
        return resultats;
    }

    /**
     * Récupère un résultat de quiz par son id
     * @param id L'id du résultat à récupérer
     * @return Le résultat correspondant ou null si aucun trouvé
     */
    public ResultatQuiz recupererParId(int id) {
        String query = "SELECT * FROM resultat_quiz WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractResultatFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du résultat: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Récupère tous les résultats d'un utilisateur
     * @param userId L'id de l'utilisateur
     * @return La liste des résultats de l'utilisateur
     */
    public List<ResultatQuiz> recupererParUtilisateur(int userId) {
        List<ResultatQuiz> resultats = new ArrayList<>();
        String query = "SELECT * FROM resultat_quiz WHERE id_user_id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultats.add(extractResultatFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des résultats par utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
        
        return resultats;
    }
    
    /**
     * Récupère tous les résultats d'un examen
     * @param examenId L'id de l'examen
     * @return La liste des résultats de l'examen
     */
    public List<ResultatQuiz> recupererParExamen(int examenId) {
        List<ResultatQuiz> resultats = new ArrayList<>();
        String query = "SELECT * FROM resultat_quiz WHERE examen_id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, examenId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultats.add(extractResultatFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des résultats par examen: " + e.getMessage());
            e.printStackTrace();
        }
        
        return resultats;
    }
    
    /**
     * Vérifie si un résultat existe pour un utilisateur et un examen
     * @param userId L'id de l'utilisateur
     * @param examenId L'id de l'examen
     * @return true si un résultat existe, false sinon
     */
    public boolean verifierExistenceResultat(int userId, int examenId) {
        String query = "SELECT COUNT(*) FROM resultat_quiz WHERE id_user_id = ? AND examen_id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, examenId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de l'existence du résultat: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Extrait un objet ResultatQuiz d'un ResultSet
     * @param rs Le ResultSet contenant les données
     * @return L'objet ResultatQuiz créé
     * @throws SQLException En cas d'erreur lors de l'extraction
     */
    private ResultatQuiz extractResultatFromResultSet(ResultSet rs) throws SQLException {
        ResultatQuiz resultat = new ResultatQuiz();
        resultat.setId(rs.getInt("id"));
        
        Integer examenId = rs.getObject("examen_id", Integer.class);
        resultat.setExamen_id(examenId);
        if (examenId != null) {
            Examen examen = examenService.recupererParId(examenId);
            resultat.setExamen(examen);
        }
        
        Integer userId = rs.getObject("id_user_id", Integer.class);
        resultat.setId_user_id(userId);
        if (userId != null) {
            User user = userService.getUserById(userId);
            resultat.setIdUser(user);
        }
        
        resultat.setScore(rs.getInt("score"));
        resultat.setTotalPoints(rs.getInt("total_points"));
        
        Timestamp datePassage = rs.getTimestamp("date_passage");
        if (datePassage != null) {
            resultat.setDatePassage(new Date(datePassage.getTime()));
        }
        
        // Récupérer le nombre d'essais restants
        if (rs.getMetaData().getColumnCount() > 6) {
            try {
                resultat.setNbrEssai(rs.getInt("nbr_essai"));
            } catch (SQLException e) {
                // La colonne n'existe peut-être pas encore dans la base de données
                System.err.println("Attention: La colonne nbr_essai n'existe pas encore: " + e.getMessage());
                resultat.setNbrEssai(0); // Valeur par défaut
            }
        } else {
            resultat.setNbrEssai(0); // Valeur par défaut
        }
        
        return resultat;
    }

    /**
     * Teste la connexion à la base de données
     * @return true si la connexion est établie, false sinon
     */
    public boolean testerConnexion() {
        if (connection == null) {
            try {
                connection = DatabaseConnection.getConnection();
                return connection != null && !connection.isClosed();
            } catch (SQLException e) {
                System.err.println("Erreur lors du test de connexion: " + e.getMessage());
                return false;
            }
        }
        
        try {
            return !connection.isClosed();
        } catch (SQLException e) {
            System.err.println("Erreur lors du test de connexion: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Vérifie si la table resultat_quiz existe et la modifie si nécessaire
     * pour ajouter la colonne nbr_essai
     * @return true si la vérification a réussi, false sinon
     */
    public boolean verifierTableResultatQuiz() {
        try {
            // Vérifier si la table existe
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "resultat_quiz", null);
            
            if (!tables.next()) {
                // La table n'existe pas, on la crée
                return creerTableResultatQuiz();
            } else {
                // La table existe, on vérifie si la colonne nbr_essai existe
                ResultSet columns = dbm.getColumns(null, null, "resultat_quiz", "nbr_essai");
                
                if (!columns.next()) {
                    // La colonne n'existe pas, on l'ajoute avec une valeur par défaut
                    System.out.println("Ajout de la colonne nbr_essai à la table resultat_quiz");
                    try (Statement stmt = connection.createStatement()) {
                        stmt.execute("ALTER TABLE resultat_quiz ADD COLUMN nbr_essai INT DEFAULT 0 NOT NULL");
                        return true;
                    } catch (SQLException e) {
                        System.err.println("Erreur lors de l'ajout de la colonne nbr_essai: " + e.getMessage());
                        e.printStackTrace();
                        
                        // Essayer une autre syntaxe au cas où
                        try (Statement stmt = connection.createStatement()) {
                            stmt.execute("ALTER TABLE resultat_quiz ADD nbr_essai INT DEFAULT 0 NOT NULL");
                            return true;
                        } catch (SQLException e2) {
                            System.err.println("Seconde erreur lors de l'ajout de la colonne nbr_essai: " + e2.getMessage());
                            e2.printStackTrace();
                            return false;
                        }
                    }
                }
                
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de la table resultat_quiz: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Crée la table resultat_quiz si elle n'existe pas
     * @return true si la création a réussi, false sinon
     */
    private boolean creerTableResultatQuiz() {
        String createTableSQL = "CREATE TABLE resultat_quiz (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "examen_id INT, " +
                "id_user_id INT, " +
                "score INT, " +
                "total_points INT, " +
                "date_passage TIMESTAMP, " +
                "nbr_essai INT DEFAULT 0 NOT NULL, " +
                "FOREIGN KEY (examen_id) REFERENCES examen(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (id_user_id) REFERENCES user(id) ON DELETE CASCADE" +
                ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("Table resultat_quiz créée avec succès");
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de la table resultat_quiz: " + e.getMessage());
            e.printStackTrace();
            
            // Essayer sans les contraintes de clés étrangères au cas où
            String simpleCreateTableSQL = "CREATE TABLE resultat_quiz (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "examen_id INT, " +
                    "id_user_id INT, " +
                    "score INT, " +
                    "total_points INT, " +
                    "date_passage TIMESTAMP, " +
                    "nbr_essai INT DEFAULT 0 NOT NULL" +
                    ")";
            
            try (Statement simpleStmt = connection.createStatement()) {
                simpleStmt.execute(simpleCreateTableSQL);
                System.out.println("Table resultat_quiz créée sans contraintes de clés étrangères");
                return true;
            } catch (SQLException e2) {
                System.err.println("Seconde erreur lors de la création de la table: " + e2.getMessage());
                e2.printStackTrace();
                return false;
            }
        }
    }
    
    /**
     * Récupère le ResultatQuiz pour un utilisateur et un examen spécifiques
     * @param userId L'id de l'utilisateur
     * @param examenId L'id de l'examen
     * @return Le ResultatQuiz ou null si aucun n'est trouvé
     */
    public ResultatQuiz recupererParUtilisateurEtExamen(int userId, int examenId) {
        String query = "SELECT * FROM resultat_quiz WHERE id_user_id = ? AND examen_id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, examenId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractResultatFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du résultat: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Diminue le nombre d'essais restants pour un utilisateur sur un examen
     * @param userId L'id de l'utilisateur
     * @param examenId L'id de l'examen
     * @return true si la mise à jour a réussi, false sinon
     */
    public boolean diminuerNbrEssai(int userId, int examenId) {
        ResultatQuiz resultat = recupererParUtilisateurEtExamen(userId, examenId);
        
        if (resultat != null) {
            int nbrEssaiActuel = resultat.getNbrEssai();
            if (nbrEssaiActuel > 0) {
                resultat.setNbrEssai(nbrEssaiActuel - 1);
                return modifier(resultat);
            }
            // Pas de mise à jour si déjà à 0
            return true;
        }
        
        return false;
    }
} 