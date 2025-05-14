package examples;

import utils.PasswordUtils;

/**
 * Example class demonstrating how to use the PasswordUtils in registration and login scenarios.
 */
public class PasswordExample {
    
    public static void main(String[] args) {
        // Example user registration
        String plainPassword = "MySecurePassword123";
        
        // Hash the password for storage in the database
        String hashedPassword = PasswordUtils.hashPassword(plainPassword);
        System.out.println("Hashed password: " + hashedPassword);
        
        // At this point, you would store the username and hashedPassword in your database
        // saveUser("username", hashedPassword);
        
        // Example login scenario
        String loginPassword = "MySecurePassword123"; // Password entered by user during login
        
        // Retrieve the stored hashed password from the database
        // String storedHashedPassword = getUserHashedPassword("username");
        // For this example, we'll use the previously generated hash
        String storedHashedPassword = hashedPassword;
        
        // Verify the login password against the stored hash
        boolean passwordMatches = PasswordUtils.verifyPassword(loginPassword, storedHashedPassword);
        
        if (passwordMatches) {
            System.out.println("Login successful!");
        } else {
            System.out.println("Invalid username or password.");
        }
        
        // Example of incorrect password
        String wrongPassword = "WrongPassword";
        boolean wrongPasswordMatches = PasswordUtils.verifyPassword(wrongPassword, storedHashedPassword);
        
        if (wrongPasswordMatches) {
            System.out.println("Login successful!");
        } else {
            System.out.println("Invalid username or password.");
        }
    }
} 