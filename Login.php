<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

include('dbcon.php');

$dID = isset($_POST['dID']) ? $_POST['dID'] : '';
$dPassword = isset($_POST['dPassword']) ? $_POST['dPassword'] : '';
$failedLoginMessage = '';

$android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");

if ($dID != "") {

    // Use prepared statements to prevent SQL injection
    $sql = "SELECT * FROM user WHERE dID = :dID AND dPassword = SHA1(:dPassword)";
    $stmt = $con->prepare($sql);
    $stmt->bindParam(':dID', $dID);
    $stmt->bindParam(':dPassword', $dPassword);
    $stmt->execute();

    if ($stmt->rowCount() == 0) {
        if (!$android) {
            $failedLoginMessage = "'$dID' no id OR wrong password.";
        } else {
            echo "'$dID' no id OR wrong password.";
            exit;
        }
    } else {
        $data = array();

        while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
            extract($row);
            array_push($data, $row);
        }

        if (!$android) {
            print_r($data);
            exit;
        } else {
            header('Content-Type: application/json; charset=utf8');
            $json = json_encode(array("user" => $data), JSON_PRETTY_PRINT + JSON_UNESCAPED_UNICODE);
            echo $json;
            exit;
        }
    }
}

if (!$android) {
?>

<html>
<head>
    <meta charset="utf-8">
    <title>Login</title>
</head>
<body>
    <?php
    if($failedLoginMessage != '') {
        echo '<div style="color:red;">' . $failedLoginMessage . '</div>';
    }
    ?>
    <form action="<?php $_PHP_SELF ?>" method="POST">
        ID: <input type="text" name="dID">
        PASSWORD: <input type="text" name="dPassword">
        <input type="submit">
    </form>
</body>
</html>

<?php
}
?>