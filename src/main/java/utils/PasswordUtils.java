package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utility class for password hashing and verification.
 * Uses SHA-256 with salt for secure password storage.
 * This implementation is compatible with the PHP equivalent.
 */
public class PasswordUtils {
    
    // The length of the salt in bytes
    private static final int SALT_LENGTH = 16;
    
    // The algorithm to use for hashing
    private static final String ALGORITHM = "SHA-256";
    
    // The number of iterations for PBKDF2
    private static final int ITERATIONS = 10000;
    
    /**
     * Hashes a password using SHA-256 with salt.
     * 
     * @param password The password to hash
     * @return The hashed password with salt in format "salt:hash"
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        try {
            // Generate a random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            // Hash the password with the salt
            byte[] hash = hashWithSalt(password, salt);
            
            // Convert salt and hash to Base64 for storage
            String saltStr = Base64.getEncoder().encodeToString(salt);
            String hashStr = Base64.getEncoder().encodeToString(hash);
            
            // Return the salt and hash combined, separated by a colon
            return saltStr + ":" + hashStr;
        } catch (NoSuchAlgorithmException e) {
            // This should never happen as SHA-256 is a standard algorithm
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Verifies that a password matches a hashed password.
     * 
     * @param password The password to check
     * @param hashedPassword The hashed password to check against (in format "salt:hash")
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        if (password == null || password.isEmpty() || hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        
        try {
            // Split the stored string to get the salt and hash
            String[] parts = hashedPassword.split(":");
            if (parts.length != 2) {
                return false;
            }
            
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] storedHash = Base64.getDecoder().decode(parts[1]);
            
            // Hash the input password with the same salt
            byte[] hash = hashWithSalt(password, salt);
            
            // Compare the calculated hash with the stored hash
            // This is a constant-time comparison to prevent timing attacks
            return MessageDigest.isEqual(hash, storedHash);
        } catch (IllegalArgumentException | NoSuchAlgorithmException e) {
            // If there's any error in decoding or hashing
            return false;
        }
    }
    
    /**
     * Internal method to hash a password with a given salt.
     * 
     * @param password The password to hash
     * @param salt The salt to use
     * @return The hashed password
     * @throws NoSuchAlgorithmException If the algorithm is not available
     */
    private static byte[] hashWithSalt(String password, byte[] salt) throws NoSuchAlgorithmException {
        // Create a message digest for SHA-256
        MessageDigest md = MessageDigest.getInstance(ALGORITHM);
        
        // Add the salt to the digest
        md.update(salt);
        
        // Add the password bytes to the digest
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        
        // Perform multiple iterations to increase security
        byte[] hash = md.digest(passwordBytes);
        for (int i = 0; i < ITERATIONS - 1; i++) {
            md.reset();
            hash = md.digest(hash);
        }
        
        return hash;
    }
    
    /**
     * Checks if a password needs to be rehashed.
     * Always returns false in this implementation since we don't have version markers.
     * 
     * @param hashedPassword The hashed password to check
     * @return true if the password should be rehashed
     */
    public static boolean needsRehash(String hashedPassword) {
        // This implementation doesn't support rehashing based on algorithm changes
        // since we don't store version information in the hash
        return false;
    }
} 