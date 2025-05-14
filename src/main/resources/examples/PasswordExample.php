<?php
// Example file for using PasswordUtils in Symfony

// In a real Symfony application, you would typically put this in a service class
// or a controller method. This is just a simplified example.

// Import the PasswordUtils class (in Symfony, you would use proper namespaces)
require_once __DIR__ . '/../php/PasswordUtils.php';

// Example user registration
$plainPassword = "MySecurePassword123";

// Hash the password for storage in the database
$hashedPassword = PasswordUtils::hashPassword($plainPassword);
echo "Hashed password: " . $hashedPassword . "\n";

// At this point, you would store the username and hashedPassword in your database
// In Symfony, you might use Doctrine:
// $user->setPassword($hashedPassword);
// $entityManager->persist($user);
// $entityManager->flush();

// Example login scenario
$loginPassword = "MySecurePassword123"; // Password entered by user during login

// Retrieve the stored hashed password from the database
// In Symfony, you might use Doctrine:
// $user = $repository->findOneBy(['username' => $username]);
// $storedHashedPassword = $user->getPassword();
// For this example, we'll use the previously generated hash
$storedHashedPassword = $hashedPassword;

// Verify the login password against the stored hash
$passwordMatches = PasswordUtils::verifyPassword($loginPassword, $storedHashedPassword);

if ($passwordMatches) {
    echo "Login successful!\n";
} else {
    echo "Invalid username or password.\n";
}

// Example of incorrect password
$wrongPassword = "WrongPassword";
$wrongPasswordMatches = PasswordUtils::verifyPassword($wrongPassword, $storedHashedPassword);

if ($wrongPasswordMatches) {
    echo "Login successful!\n";
} else {
    echo "Invalid username or password.\n";
}

// Cross-platform compatibility test
// Example hash generated from Java code
$javaGeneratedHash = "YOUR_HASH_FROM_JAVA"; // Replace with actual hash from Java
echo "Java-generated hash can be verified in PHP: " . 
    (PasswordUtils::verifyPassword($plainPassword, $javaGeneratedHash) ? "Yes" : "No") . "\n";
?> 