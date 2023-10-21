<?php
    $con = mysqli_connect("deliveryrds.cqdp7841ahaa.ap-northeast-2.rds.amazonaws.com", "root", "!!insoo0128", "delivery");

    // Check connection
    if (mysqli_connect_errno()) {
        echo "Failed to connect to MySQL: " . mysqli_connect_error();
        exit();
    }

    $result = mysqli_query($con, "SELECT * FROM shopOrder ORDER BY shopAddress DESC;");
    $response = array();

    while ($row = mysqli_fetch_assoc($result)) {
        array_push($response, array(
            "userID" => $row["userID"],
            "userAddress" => $row["userAddress"],
            "shopName" => $row["shopName"],
            "shopAddress" => $row["shopAddress"]
        ));
    }

    echo json_encode(array("response" => $response));
    mysqli_close($con);
?>