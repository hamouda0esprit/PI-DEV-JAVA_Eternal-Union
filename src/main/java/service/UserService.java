package service;

import entite.User;
import utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private Connection connection;

    public UserService() {
        connection = DataSource.getInstance().getConnection();
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    public void addUser(User user) throws SQLException {
        System.out.println("\n===== ADDING NEW USER TO DATABASE =====");
        
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("Connection is null or closed, getting a new connection");
                connection = utils.DataSource.getInstance().getConnection();
            }
            
            // Handle default avatar path
            String imagePath = user.getImg();
            if (imagePath == null || imagePath.isEmpty() || imagePath.startsWith("A")) {
                // If it's a default avatar (starts with A), keep it as is
                imagePath = imagePath != null ? imagePath : "A1.png";
            }
            
            String query = "INSERT INTO user (name, email, date_of_birth, password, img, type, phone, rate, score, bio, verified, google_id,warnings) " +
                    "VALUES (?, ?, ?, ?, ?, ?, 0, 0.0, 0, 'Vous n''avez pas encore de bio', 0, NULL,0)";
            
            System.out.println("Preparing statement with query: " + query);
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setDate(3, new java.sql.Date(user.getDate_of_birth().getTime()));
            preparedStatement.setString(4, user.getPassword());
            preparedStatement.setString(5, imagePath);
            preparedStatement.setString(6, user.getType());
            
            System.out.println("User data being inserted:");
            System.out.println("- Name: " + user.getName());
            System.out.println("- Email: " + user.getEmail());
            System.out.println("- Birth Date: " + user.getDate_of_birth());
            System.out.println("- Password: " + (user.getPassword() != null ? "[SECURE]" : "null"));
            System.out.println("- Image: " + imagePath);
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
            user.setVerified(resultSet.getString("verified"));
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
            user.setVerified(resultSet.getString("verified"));
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
            user.setVerified(resultSet.getString("verified"));
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
        String query = "UPDATE user SET name = ?, email = ?, password = ?, img = ?, bio = ? WHERE id = ?";
        
        System.out.println("Updating user with ID: " + user.getId());
        System.out.println("- Name: " + user.getName());
        System.out.println("- Email: " + user.getEmail());
        System.out.println("- Password: [SECURE]");
        System.out.println("- Image: " + user.getImg());
        System.out.println("- Bio: " + user.getBio());
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, user.getPassword());
            preparedStatement.setString(4, user.getImg());
            preparedStatement.setString(5, user.getBio());
            preparedStatement.setInt(6, user.getId());
            
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

    public User getUserByUsername(String username) {
        String query = "SELECT * FROM user WHERE name = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return new User(
                    resultSet.getInt("id"),
                    resultSet.getString("name"),
                    resultSet.getString("email"),
                    resultSet.getInt("phone"),
                    resultSet.getString("type"),
                    resultSet.getDate("date_of_birth"),
                    resultSet.getString("password"),
                    resultSet.getString("img"),
                    resultSet.getInt("score"),
                    resultSet.getString("bio"),
                    resultSet.getString("verified"),
                    resultSet.getString("google_id"),
                    resultSet.getDouble("rate")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by username: " + e.getMessage());
        }
        return null;
    }

    public void updateUserVerification(String email, String verified) throws SQLException {
        String query = "UPDATE user SET verified = ? WHERE email = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, verified);
            preparedStatement.setString(2, email);
            
            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println("User verification update completed. Rows affected: " + rowsAffected);
            
            if (rowsAffected == 0) {
                throw new SQLException("User verification update failed, no rows affected.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating user verification: " + e.getMessage());
            throw e;
        }
    }
} 