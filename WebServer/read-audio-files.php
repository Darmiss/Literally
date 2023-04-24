<?php

require_once 'connect.php';

// Get the user_id parameter from the function argument
$user_id = $_GET['user_id'];

// Select all audio files for the given user from the database
$sql = "SELECT * FROM audio_clips WHERE user_id = '$user_id' ORDER BY time_said DESC";
$result = mysqli_query($conn, $sql);

$response = array(); // Initialize the response array

if (!$result) {
    echo "MySQL Error: " . mysqli_error($conn);
} else {
    // Create an array to hold the data for each file
    $files = array();

    // Loop through each file and add its data to the array
    while ($row = mysqli_fetch_assoc($result)) {
        // Get the file path from the database
        $filePath = $row['filepath'];
        $timeSaid = $row['time_said'];
        $text = $row['textsaid'];
        $location = $row['location'];

        // Read the file data into a string
        $fileData = file_get_contents($filePath);

        // Add the file data and its metadata to the array
        $files[] = array(
            'name' => basename($filePath),
            'size' => filesize($filePath),
            'data' => base64_encode($fileData),
            'time_said' => $timeSaid,
            'textsaid' => $text,
            'location' => $location
        );
    }

    // Add the 'files' key to the response array
    $response['files'] = $files;

    // Convert the response array to a JSON string and send it to the requestor
    header('Content-Type: application/json');
    echo json_encode($response);
}
?>
