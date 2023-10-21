<?php
 
    error_reporting(E_ALL);
    ini_set('display_errors',1);
 
    include('dbcon.php');
 
    $android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");
 
    if( (($_SERVER['REQUEST_METHOD'] == 'POST') && isset($_POST['submit'])) || $android )
    {
        $dID = $_POST['dID'];
        $dPassword = $_POST['dPassword'];
        $demail = $_POST['demail'];
        $dphoneNumber = $_POST['dphoneNumber'];
        
        // userSort값을 사용자 입력이 아닌 DB의 마지막 값에서 1 증가시킨 값으로 설정
        $stmt = $con->prepare("SELECT dSort FROM user ORDER BY dSort DESC LIMIT 1");
        $stmt->execute();
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        $dSort = isset($row['dSort']) ? $row['dSort'] + 1 : 1; 

        if(empty($dID)) {
            $errMSG = "dID";
        } else if(empty($dPassword)) {
            $errMSG = "dpassword";
        } else if(empty($demail)) {
            $errMSG = "demail";
        } else if(empty($dphoneNumber)) {
            $errMSG = "dphonenumber";
        }

        if(!isset($errMSG)) {
            // 중복된 ID 체크
            $checkID = $con->prepare("SELECT dID FROM user WHERE dID=:dID");
            $checkID->bindParam(':dID', $dID);
            $checkID->execute();

            if ($checkID->fetch(PDO::FETCH_ASSOC)) {
                $errMSG = "DUPLICATE_ID";
            } else {
                try {
                    $stmt = $con->prepare('INSERT INTO user(dID, dPassword, demail, dphoneNumber, dSort) VALUES(:dID, SHA1(:dPassword), :demail, :dphoneNumber, :dSort)');
                    $stmt->bindParam(':dID', $dID);
                    $stmt->bindParam(':dPassword', $dPassword);
                    $stmt->bindParam(':demail', $demail);
                    $stmt->bindParam(':dphoneNumber', $dphoneNumber);
                    $stmt->bindParam(':dSort', $dSort);

                    if($stmt->execute()) {
                        $successMSG = "SUCCESS";
                    } else {
                        $errMSG = "FAIL";
                    }
                } catch(PDOException $e) {
                    die("Database error: " . $e->getMessage());
                }
            }
        }
    }

    if (isset($errMSG)) echo $errMSG;
    if (isset($successMSG)) echo $successMSG;

    $android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");
 
    if(!$android) {
?>
    <html>
       <body>
            <form action="<?php $_PHP_SELF ?>" method="POST">
                dID: <input type = "text" name = "dID" />
                dPassword: <input type = "text" name = "dPassword" />
                demail: <input type = "text" name = "demail" />
                dphonenumber: <input type = "text" name = "dphoneNumber" />
                <!-- usersort input을 제거하였습니다. -->
                <input type = "submit" name = "submit" />
            </form>
       </body>
    </html>
<?php
    }
?>