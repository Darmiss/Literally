<?php

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    // Get input values from client-side
    $username = $_POST['username'];
    $pass = $_POST['pass'];
    $email = $_POST['email'];
    $firstName = $_POST['firstName'];
    $lastName = $_POST['lastName'];

    require_once 'connect.php';

    // Hash the password using the default algorithm
    $hashed_password = password_hash($pass, PASSWORD_DEFAULT);

    $sql = "INSERT INTO users (username, password, email, firstName, lastName, isUser) VALUES ('$username', '$hashed_password', '$email', '$firstName', '$lastName', 1)";

    if (mysqli_query($conn, $sql)) {
        $result["success"] = "1";
        $result["message"] = "success";

        echo json_encode($result);

        mysqli_close($conn);
    } else {
        $result["success"] = "0";
        $result["message"] = "failed";

        echo json_encode($result);

        mysqli_close($conn);
    }
}

?>