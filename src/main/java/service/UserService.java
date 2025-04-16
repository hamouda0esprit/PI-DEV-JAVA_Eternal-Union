package service;

import entite.Responces;
import entite.User;
import utils.DataSource;

import java.sql.*;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import utils.DatabaseConnection;

public class UserService implements IService<User>{

    // Map pour stocker en cache les utilisateurs récupérés de la base de données
    private static final Map<String, UserInfo> USER_CACHE = new HashMap<>();
    private Connection cnx;
    private Statement ste;
    private PreparedStatement pst;
    private ResultSet rs;
    /**
     * Vérifie si l'ID est valide dans la base de données
     * @param id L'ID de l'utilisateur
     * @return true si l'ID est valide, false sinon
     */
    public boolean authenticateFast(String id) {
        System.out.println("Authentification avec ID: " + id);

        // Vérifier d'abord dans le cache
        if (USER_CACHE.containsKey(id)) {
            System.out.println("Utilisateur trouvé dans le cache");
            return true;
        }

        // Sinon, vérifier dans la base de données
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("Connexion à la base de données établie pour authentification");

            // Utiliser la requête adaptée à votre structure de base de données
            // Exemple: si vous avez une table "utilisateur" ou "user" au lieu de "utilisateurs"
            String query = "SELECT id, type FROM user WHERE id = ?";
            System.out.println("Exécution de la requête: " + query + " [id=" + id + "]");

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, id);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Stocker dans le cache pour les futures requêtes
                // Utiliser la colonne qui contient le rôle (type, role, etc.)
                String role = rs.getString("type");
                System.out.println("Utilisateur trouvé dans la base de données. Type: " + role);
                USER_CACHE.put(id, new UserInfo(role));
                return true;
            }
            System.out.println("Utilisateur non trouvé dans la base de données");
            return false;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'authentification: " + e.getMessage());
            e.printStackTrace();

            // En cas d'erreur de connexion, utiliser les utilisateurs de démonstration
            boolean fallbackResult = fallbackAuthenticate(id);
            System.out.println("Authentification de secours: " + (fallbackResult ? "réussie" : "échec"));
            return fallbackResult;
        }
    }

    /**
     * Récupère le rôle d'un utilisateur à partir de son ID
     * @param id L'ID de l'utilisateur
     * @return Le rôle de l'utilisateur ou null si l'ID n'existe pas
     */
    public String getFastUserRole(String id) {
        System.out.println("Récupération du rôle pour ID: " + id);

        // Vérifier d'abord dans le cache
        UserInfo cachedUser = USER_CACHE.get(id);
        if (cachedUser != null) {
            String role = cachedUser.getRole();
            System.out.println("Rôle trouvé dans le cache: " + role);
            return role;
        }

        // Sinon, vérifier dans la base de données
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("Connexion à la base de données établie pour récupération du rôle");

            // Adapter la requête à votre structure de base de données
            String query = "SELECT type FROM user WHERE id = ?";
            System.out.println("Exécution de la requête: " + query + " [id=" + id + "]");

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, id);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String roleFromDb = rs.getString("type");
                System.out.println("Rôle brut récupéré de la DB: " + roleFromDb);

                String mappedRole = mapRoleToApplication(roleFromDb);
                System.out.println("Rôle converti pour l'application: " + mappedRole);

                // Mettre en cache pour les requêtes futures
                USER_CACHE.put(id, new UserInfo(mappedRole));
                return mappedRole;
            }
            System.out.println("Aucun rôle trouvé dans la base de données");
            return null;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du rôle: " + e.getMessage());
            e.printStackTrace();

            // En cas d'erreur de connexion, utiliser la méthode de secours
            String fallbackRole = fallbackGetRole(id);
            System.out.println("Rôle de secours: " + fallbackRole);
            return fallbackRole;
        }
    }

    /**
     * Convertit le type d'utilisateur de la base de données en rôle dans l'application
     */
    private String mapRoleToApplication(String dbRole) {
        // Adapter selon les valeurs dans votre base de données
        if (dbRole == null) {
            System.out.println("Rôle de la base de données est null");
            return null;
        }

        System.out.println("Conversion du rôle: " + dbRole);

        switch (dbRole.toLowerCase()) {
            case "etudiant":
            case "student":
            case "0":
                return "student";
            case "professeur":
            case "prof":
            case "teacher":
            case "1":
                return "teacher";
            case "admin":
            case "administrateur":
            case "2":
                return "admin";
            default:
                System.out.println("Rôle non reconnu, valeur conservée: " + dbRole);
                return dbRole;
        }
    }

    /**
     * Méthode de secours pour l'authentification en cas d'erreur de base de données
     * Utilise des utilisateurs de démonstration
     */
    private boolean fallbackAuthenticate(String id) {
        // Quelques IDs prédéfinis pour la démonstration
        boolean result = id.equals("E001") || id.equals("E002") || id.equals("E003") ||
                id.equals("P001") || id.equals("P002") ||
                id.equals("A001") || id.equals("A002");

        System.out.println("Vérification d'ID de secours pour: " + id + " -> " + (result ? "valide" : "invalide"));
        return result;
    }

    /**
     * Méthode de secours pour récupérer le rôle en cas d'erreur de base de données
     * Utilise des rôles prédéfinis pour la démonstration
     */
    private String fallbackGetRole(String id) {
        String role = null;

        if (id.startsWith("E")) {
            role = "Étudiant";
        } else if (id.startsWith("P")) {
            role = "Professeur";
        } else if (id.startsWith("A")) {
            role = "admin";
        }

        System.out.println("Détermination du rôle de secours pour: " + id + " -> " + role);
        return role;
    }

    /**
     * Classe interne pour stocker les informations de l'utilisateur
     */
    private static class UserInfo {
        private final String role;

        public UserInfo(String role) {
            this.role = role;
        }

        public String getRole() {
            return role;
        }
    }
    @Override
    public void create(User user) {

    }

    @Override
    public void update(User user) {

    }

    @Override
    public void delete(User user) {

    }

    @Override
    public List<User> readAll() {
        return List.of();
    }

    @Override
    public User readById(int id) {
        String sql = "SELECT * FROM user WHERE id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(sql);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setImg(rs.getString("img"));
                // Set other user properties as needed
                return user;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void createPst(User forum) {

    }

    @Override
    public void ajouter(User user) throws SQLException {

    }

    @Override
    public void modifier(User user) throws SQLException {

    }

    @Override
    public void sipprimer(int id) throws SQLException {

    }

    @Override
    public List<User> recuperer() throws SQLException {
        return List.of();
    }
}