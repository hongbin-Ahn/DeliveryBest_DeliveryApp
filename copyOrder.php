<?php
    $con = mysqli_connect("deliveryrds.cqdp7841ahaa.ap-northeast-2.rds.amazonaws.com", "root", "!!insoo0128", "delivery");

    if (mysqli_connect_errno()) {
        echo "Failed to connect to MySQL: " . mysqli_connect_error();
        exit();
    }

    // Debugging: Print the received POST data
    echo "Received Data: ";
    print_r($_POST);
    echo "\n"; // Newline for clarity

    if (isset($_POST["userID"]) && isset($_POST["userAddress"]) && isset($_POST["shopName"]) && isset($_POST["shopAddress"])) {
        $userID = $_POST["userID"];
        $userAddress = $_POST["userAddress"];
        $shopName = $_POST["shopName"];
        $shopAddress = $_POST["shopAddress"];

        // Insert into Ordering table
        $stmt = $con->prepare("INSERT INTO Ordering (userID, userAddress, shopName, shopAddress) VALUES (?, ?, ?, ?)");
        $stmt->bind_param("ssss", $userID, $userAddress, $shopName, $shopAddress);
        $result = $stmt->execute();

        if ($result) {
            echo json_encode(array("response" => "success"));
        } else {
            echo json_encode(array("response" => "error"));
        }

        $stmt->close();
    } else {
        echo json_encode(array("response" => "invalid_request"));
    }

    mysqli_close($con);
?>