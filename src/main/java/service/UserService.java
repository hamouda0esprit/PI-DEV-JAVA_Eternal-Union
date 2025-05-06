package service;
import entite.User;
import utils.DataSource;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DatabaseConnection;
import java.util.Map;
import java.sql.*;
import java.util.ArrayList;


public class UserService implements IService<User>{
    private static final Map<String, UserInfo> USER_CACHE = new HashMap<>();
    private Connection connection;

    public UserService() {
        connection = DataSource.getInstance().getConncetion();
    }

    public Connection getConnection() {
        return connection;
    }

    public void addUser(User user) throws SQLException {
        System.out.println("\n===== ADDING NEW USER TO DATABASE =====");

        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("Connection is null or closed, getting a new connection");
                connection = utils.DataSource.getInstance().getConncetion();
            }

            String query = "INSERT INTO user (name, email, date_of_birth, password, img, type, phone, rate, score, bio, verified, google_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, 0, 0.0, 0, 'Vous n''avez pas encore de bio', 0, NULL)";

            System.out.println("Preparing statement with query: " + query);
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setDate(3, new java.sql.Date(user.getDate_of_birth().getTime()));
            preparedStatement.setString(4, user.getPassword());
            preparedStatement.setString(5, user.getImg());
            preparedStatement.setString(6, user.getType());

            System.out.println("User data being inserted:");
            System.out.println("- Name: " + user.getName());
            System.out.println("- Email: " + user.getEmail());
            System.out.println("- Birth Date: " + user.getDate_of_birth());
            System.out.println("- Password: " + (user.getPassword() != null ? "[SECURE]" : "null"));
            System.out.println("- Image: " + user.getImg());
            System.out.println("- Type: " + user.getType());

            System.out.println("Executing insert statement...");
            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println("Query execution complete. Rows affected: " + rowsAffected);

            preparedStatement.close();

            // Verify the user was actually added by checking if we can retrieve it
            System.out.println("Verifying user was added by retrieving from database...");
            User retrievedUser = getUserByEmail(user.getEmail());
            if (retrievedUser != null) {
                System.out.println("User verification SUCCESSFUL - User with email " +
                        user.getEmail() + " found in database with ID: " + retrievedUser.getId());
            } else {
                System.out.println("WARNING: User verification FAILED - Could not find user with email: " +
                        user.getEmail() + " in the database after insertion!");
            }

            System.out.println("===== USER ADDED SUCCESSFULLY =====\n");
        } catch (SQLException e) {
            System.err.println("ERROR ADDING USER: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);

        while (resultSet.next()) {
            User user = new User();
            user.setId(resultSet.getInt("id"));
            user.setName(resultSet.getString("name"));
            user.setEmail(resultSet.getString("email"));
            user.setPhone(resultSet.getInt("phone"));
            user.setType(resultSet.getString("type"));
            user.setDate_of_birth(resultSet.getDate("date_of_birth"));
            user.setPassword(resultSet.getString("password"));
            user.setImg(resultSet.getString("img"));
            user.setScore(resultSet.getInt("score"));
            user.setBio(resultSet.getString("bio"));
            user.setVerified(resultSet.getBoolean("verified"));
            user.setGoogle_id(resultSet.getString("google_id"));

            users.add(user);
        }

        resultSet.close();
        statement.close();
        return users;
    }

    // Read user by ID
    public User getUserById(int id) throws SQLException {
        String query = "SELECT * FROM user WHERE id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, id);
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            User user = new User();
            user.setId(resultSet.getInt("id"));
            user.setName(resultSet.getString("name"));
            user.setEmail(resultSet.getString("email"));
            user.setPhone(resultSet.getInt("phone"));
            user.setType(resultSet.getString("type"));
            user.setDate_of_birth(resultSet.getDate("date_of_birth"));
            user.setPassword(resultSet.getString("password"));
            user.setImg(resultSet.getString("img"));
            user.setScore(resultSet.getInt("score"));
            user.setBio(resultSet.getString("bio"));
            user.setVerified(resultSet.getBoolean("verified"));
            user.setGoogle_id(resultSet.getString("google_id"));

            resultSet.close();
            preparedStatement.close();
            return user;
        }

        resultSet.close();
        preparedStatement.close();
        return null;
    }

    public User getUserByEmail(String email) throws SQLException {
        String query = "SELECT * FROM user WHERE email = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, email);
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            User user = new User();
            user.setId(resultSet.getInt("id"));
            user.setName(resultSet.getString("name"));
            user.setEmail(resultSet.getString("email"));
            user.setPhone(resultSet.getInt("phone"));
            user.setType(resultSet.getString("type"));
            user.setDate_of_birth(resultSet.getDate("date_of_birth"));
            user.setPassword(resultSet.getString("password"));
            user.setImg(resultSet.getString("img"));
            user.setScore(resultSet.getInt("score"));
            user.setBio(resultSet.getString("bio"));
            user.setVerified(resultSet.getBoolean("verified"));
            user.setGoogle_id(resultSet.getString("google_id"));

            resultSet.close();
            preparedStatement.close();
            return user;
        }

        resultSet.close();
        preparedStatement.close();
        return null;
    }

    // Update user with full profile editing capabilities
    public void updateUser(User user) throws SQLException {
        String query = "UPDATE user SET name = ?, email = ?, password = ?, img = ?, bio = ?, warnings = ? WHERE id = ?";

        System.out.println("Updating user with ID: " + user.getId());
        System.out.println("- Name: " + user.getName());
        System.out.println("- Email: " + user.getEmail());
        System.out.println("- Password: [SECURE]");
        System.out.println("- Image: " + user.getImg());
        System.out.println("- Bio: " + user.getBio());
        System.out.println("- Warnings: " + user.getWarnings());

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, user.getPassword());
            preparedStatement.setString(4, user.getImg());
            preparedStatement.setString(5, user.getBio());
            preparedStatement.setInt(6, user.getWarnings());
            preparedStatement.setInt(7, user.getId());

            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println("User update completed. Rows affected: " + rowsAffected);

            if (rowsAffected == 0) {
                throw new SQLException("User update failed, no rows affected.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            throw e;
        }
    }

    public void deleteUser(int id) throws SQLException {
        String query = "DELETE FROM user WHERE id = ?";

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, id);

        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
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
            PreparedStatement pst = connection.prepareStatement(sql);
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