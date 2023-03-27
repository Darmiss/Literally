<?php

session_start();

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $username_or_email = $_POST['username_or_email'];
    $password = $_POST['password'];

    require_once 'connect.php';

    // Verify password

    $sql = "SELECT user_id, firstName, lastName, username, isUser, password FROM users WHERE username='$username_or_email' OR email='$username_or_email'";
    $result = mysqli_query($conn, $sql);
    $row = mysqli_fetch_assoc($result);

    if (mysqli_num_rows($result) == 1 && password_verify($password, $row['password'])) {
        // User is authenticated

        // Data to add to user session
        $_SESSION['user_id'] = $row['user_id'];
        $_SESSION['username'] = $row['username'];
        $_SESSION['firstName'] = $row['firstName'];
        $_SESSION['lastName'] = $row['lastName'];

        // get session data as JSON
        $sessionData = array(
            'user_id' => $_SESSION['user_id'],
            'username' => $_SESSION['username'],
            'firstName' => $_SESSION['firstName'],
            'lastName' => $_SESSION['lastName']
        );

        $response["success"] = 1;
        $response["message"] = "success";
        $response["sessionData"] = $sessionData;

        echo json_encode($response);

        mysqli_close($conn);
    } else {
        // Authentication failed
        $response["success"] = 0;
        $response["message"] = "invalid username/email or password";

        echo json_encode($response);

        mysqli_close($conn);
    }
}

?>