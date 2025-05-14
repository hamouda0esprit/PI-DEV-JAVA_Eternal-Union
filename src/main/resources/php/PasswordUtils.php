<?php
/**
 * Utility class for password hashing and verification.
 * Uses SHA-256 with salt for secure password storage.
 * This implementation is compatible with the Java equivalent.
 */
class PasswordUtils {
    
    // The length of the salt in bytes
    private const SALT_LENGTH = 16;
    
    // The algorithm to use for hashing
    private const ALGORITHM = 'sha256';
    
    // The number of iterations for the hashing
    private const ITERATIONS = 10000;
    
    /**
     * Hashes a password using SHA-256 with salt.
     * 
     * @param string $password The password to hash
     * @return string The hashed password with salt in format "salt:hash"
     */
    public static function hashPassword($password) {
        if (empty($password)) {
            throw new \InvalidArgumentException("Password cannot be null or empty");
        }
        
        // Generate a random salt
        $salt = random_bytes(self::SALT_LENGTH);
        
        // Hash the password with the salt
        $hash = self::hashWithSalt($password, $salt);
        
        // Convert salt and hash to Base64 for storage
        $saltStr = base64_encode($salt);
        $hashStr = base64_encode($hash);
        
        // Return the salt and hash combined, separated by a colon
        return $saltStr . ':' . $hashStr;
    }
    
    /**
     * Verifies that a password matches a hashed password.
     * 
     * @param string $password The password to check
     * @param string $hashedPassword The hashed password to check against (in format "salt:hash")
     * @return bool true if the password matches, false otherwise
     */
    public static function verifyPassword($password, $hashedPassword) {
        if (empty($password) || empty($hashedPassword)) {
            return false;
        }
        
        try {
            // Split the stored string to get the salt and hash
            $parts = explode(':', $hashedPassword);
            if (count($parts) !== 2) {
                return false;
            }
            
            $salt = base64_decode($parts[0]);
            $storedHash = base64_decode($parts[1]);
            
            if ($salt === false || $storedHash === false) {
                return false;
            }
            
            // Hash the input password with the same salt
            $hash = self::hashWithSalt($password, $salt);
            
            // Compare the calculated hash with the stored hash
            // Use hash_equals for constant-time comparison to prevent timing attacks
            return hash_equals($storedHash, $hash);
        } catch (\Exception $e) {
            // If there's any error in decoding or hashing
            return false;
        }
    }
    
    /**
     * Internal method to hash a password with a given salt.
     * 
     * @param string $password The password to hash
     * @param string $salt The salt to use
     * @return string The hashed password
     */
    private static function hashWithSalt($password, $salt) {
        // Perform the initial hash
        $hash = hash(self::ALGORITHM, $salt . $password, true);
        
        // Perform multiple iterations to increase security
        for ($i = 0; $i < self::ITERATIONS - 1; $i++) {
            $hash = hash(self::ALGORITHM, $hash, true);
        }
        
        return $hash;
    }
    
    /**
     * Checks if a password needs to be rehashed.
     * Always returns false in this implementation since we don't have version markers.
     * 
     * @param string $hashedPassword The hashed password to check
     * @return bool true if the password should be rehashed
     */
    public static function needsRehash($hashedPassword) {
        // This implementation doesn't support rehashing based on algorithm changes
        // since we don't store version information in the hash
        return false;
    }
}
?> 