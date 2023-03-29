<?php
require_once 'connect.php';

$userId = $_POST['user_id'];
$targetDir = "uploads/";
$targetFile = $targetDir . basename($_FILES["audio_file"]["name"]);
$uploadOk = 1;
$audioFileType = strtolower(pathinfo($targetFile, PATHINFO_EXTENSION));

// Define the upload directory and make sure it exists
if (!file_exists($targetDir)) {
    mkdir($targetDir, 0777, true);
}

// Check if file already exists
if (file_exists($targetFile)) {
    echo "Sorry, file already exists.";
    $uploadOk = 0;
}

// Allow certain file formats
if($audioFileType != "mp3") {
    echo "Sorry, only MP3 files are allowed.";
    $uploadOk = 0;
}

// Check if $uploadOk is set to 0 by an error
if ($uploadOk == 0) {
    echo "Sorry, your file was not uploaded.";
// if everything is ok, try to upload file
} else {
    if (move_uploaded_file($_FILES["audio_file"]["tmp_name"], $targetFile)) {
        // Save relative path of file to database
        $filePath = $targetDir . basename($_FILES["audio_file"]["name"]);
        $stmt = $conn->prepare("INSERT INTO audio_clips (user_id, time_said, filepath) VALUES (?, ?, ?)");
        $stmt->bind_param("is", $userId, NOW(), $filePath);
        $stmt->execute();
        $stmt->close();
        echo "The file ". htmlspecialchars(basename($_FILES["audio_file"]["name"])). " has been uploaded.";
    } else {
        echo "Sorry, there was an error uploading your file.";
    }
}

$conn->close();
?>