<?php
require_once 'db_functions.php';
$db = new DB_Functions();

if (isset($_POST['orderId']) && isset($_POST['userPhone'])) {
    // receiving the post params
    $orderId = $_POST['orderId'];
    $userPhone = $_POST['userPhone'];

    $result = false;
    $result = $db->cancelOrder($orderId, $userPhone);

    if ($result) {
        echo json_encode("Order has been cancelled!");
    } else {
        echo json_encode("Error while write to database!");
    }
} else {
    echo json_encode("Required parameters (orderId, userPhone) is missing!");
}
?>