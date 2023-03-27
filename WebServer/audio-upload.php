<?php

// Include your connect.php file to establish a database connection
require_once 'connect.php';

// Check if request method is POST
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    // Check if user_id and audio_file are set
    if (isset($_POST['user_id']) && isset($_FILES['audio_file'])) {
        $userId = $_POST['user_id'];
        $audioFile = $_FILES['audio_file'];

        // Define the upload directory and make sure it exists
        $uploadDir = 'uploads/audio/';
        if (!file_exists($uploadDir)) {
            mkdir($uploadDir, 0777, true);
        }

        // Sanitize the file name to avoid security risks
        $safeFileName = preg_replace('/[^A-Za-z0-9_\-]/', '_', $audioFile['name']);

        // Create a unique file name with user ID and current timestamp
        $uniqueFileName = $userId . '_' . time() . '_' . $safeFileName;

        // Move the uploaded file to the desired folder
        $uploadFilePath = $uploadDir . $uniqueFileName;
        if (move_uploaded_file($audioFile['tmp_name'], $uploadFilePath)) {
            // File uploaded successfully

            // Insert the file information into the database
            $sql = "INSERT INTO audio_clips (user_id, time_said, filepath) VALUES ('$userId', NOW(), '$uploadFilePath')";
            if (mysqli_query($conn, $sql)) {
                // Database insertion successful
                echo "1";
            } else {
                // Database insertion failed
                echo "0";
            }
        } else {
            // File upload failed
            echo "0";
        }
    } else {
        // Missing user_id or audio_file
        echo "0";
    }
} else {
    // Invalid request method
    echo "0";
}

// Close the database connection
mysqli_close($conn);

?>
