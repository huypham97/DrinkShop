<?php
require_once 'db_functions.php';
$db = new DB_Functions();

/*
 * Endpoint : http://<domain>/drinkshop/submitorder.php
 * Method : POST
 * Params : orderDetail, phone, address, comment, price
 * Result : JSON
 */
$response = array();
if (isset($_POST['orderDetail']) &&
        isset($_POST['phone']) &&
        isset($_POST['address']) &&
        isset($_POST['comment']) &&
        isset($_POST['price']) &&
        isset($_POST['paymentMethod'])) {
    $phone = $_POST['phone'];
    $orderDetail = $_POST['orderDetail'];
    $orderAddress = $_POST['address'];
    $orderComment = $_POST['comment'];
    $orderPrice = $_POST['price'];
    $paymentMethod = $_POST['paymentMethod'];

    $result = $db->insertNewOrder($orderPrice, $orderDetail, $orderComment, $orderAddress, $phone, $paymentMethod);
    if ($result)
        echo json_encode("true");
    else
        echo json_encode("error");
} else {
    echo json_encode("Required parameter (phone, detail, address, comment, price, paymentMethod) is missing!");
}
?>