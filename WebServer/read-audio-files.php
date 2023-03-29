<?php
require_once 'connect.php';

$userId = $_GET['user_id'];

// Retrieve audio files for user ID
$stmt = $conn->prepare("SELECT * FROM audio_files WHERE user_id = ?");
$stmt->bind_param("i", $userId);
$stmt->execute();
$result = $stmt->get_result();

// Create array of audio files
$audioFiles = array();
while ($row = $result->fetch_assoc()) {
    $audioFile = array(
        'id' => $row['id'],
        'user_id' => $row['user_id'],
        'time_said' => $row['time_said'],
        'file_path' => $row['file_path']
    );
    array_push($audioFiles, $audioFile);
}

$stmt->close();
$conn->close();

// Return array of audio files
header('Content-Type: application/json');
echo json_encode($audioFiles);
?>