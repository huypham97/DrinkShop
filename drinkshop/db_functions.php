<?php

class DB_Functions {
    private $conn;

    function __construct() {
        require_once 'db_connect.php';
        $db = new DB_Connect();
        $this->conn = $db->connect();
    }

    function __destruct() {
    }

    /*
     * check user exists
     * return true/false
     */
    function checkExistsUser($phone) {
        $stmt = $this->conn->prepare("SELECT * FROM User WHERE Phone=?");
        $stmt->bind_param("s", $phone);
        $stmt->execute();
        $stmt->store_result();

        if ($stmt->num_rows > 0) {
            $stmt->close();
            return true;
        } else {
            $stmt->close();
            return false;
        }
    }

    /*
     * Register new user
     * return User object if user was created
     * return false and show error message if have exception
     */
    public function registerNewUser($phone, $name, $birthdate, $address) {
        $stmt = $this->conn->prepare("INSERT INTO User(Phone,Name,Birthdate,Address) VALUES (?,?,?,?)");
        $stmt->bind_param("ssss", $phone, $name, $birthdate, $address);
        $result = $stmt->execute();
        $stmt->close();
        
        if ($result) {
            $stmt = $this->conn->prepare("SELECT * FROM User WHERE Phone = ?");
            $stmt->bind_param("s", $phone);
            $stmt->execute();
            $user = $stmt->get_result()->fetch_assoc();
            $stmt->close();
            return $user;
        } else
            return false;
    }

    /*
     * Get User Information
     * return User object if user exists
     * return false if user is not exists
     */
    public function getUserInformation($phone) {
        $stmt = $this->conn->prepare("SELECT * FROM User WHERE Phone = ?");
        $stmt->bind_param("s", $phone);
        
        if ($stmt->execute()) {
            $user = $stmt->get_result()->fetch_assoc();
            $stmt->close();

            return $user;
        } else {
            return NULL;
        }

    }

    /*
     * Get Banner
     * return List of Banner
     */
    public function getBanner() {
        // Select 3 newest banners
        $result = $this->conn->query("SELECT * FROM Banner ORDER BY ID LIMIT 3");

        $banners = array();
        
        while ($item = $result->fetch_assoc()) {
            $banners[] = $item;
        }
        return $banners;
    }

    /*
     * Get Menu
     * return List of Menu
     */
    public function getMenu() {
        // Select 3 newest banners
        $result = $this->conn->query("SELECT * FROM Menu");

        $menu = array();
        
        while ($item = $result->fetch_assoc()) {
            $menu[] = $item;
        }
        return $menu;
    }

    /*
     * Get Drink base Menu ID
     * return List of Drink
     */
    public function getDrinkByMenuId($menuId) {
        $query = "SELECT * FROM Drink WHERE MenuId = '" . $menuId . "'";
        $result = $this->conn->query($query);

        $drinks = array();
        
        while ($item = $result->fetch_assoc()) {
            $drinks[] = $item;
        }
        return $drinks;
    }

    /*
     * Update Avatar url
     * return TRUE or FALSE
     */
    public function updateAvatar($phone, $fileName) {
        
        return $result = $this->conn->query("UPDATE user SET avatarUrl = '$fileName' WHERE Phone = '$phone'");
    }

    /*
     * GET ALL DRINKS
     * return List of Drink or empty
     */
    public function getAllDrinks() {
        $result = $this->conn->query("SELECT * FROM drink WHERE 1") or die($this->conn->error);

        $drinks = array();
        while ($item = $result->fetch_assoc())
            $drinks[] = $item;
        return $drinks;
    }

    /*
     * INSERT NEW ORDER
     * return TRUE or FALSE
     */
    public function insertNewOrder($orderPrice, $orderDetail, $orderComment, $orderAddress, $userPhone, $paymentMethod) {
        $stmt = $this->conn->prepare("INSERT INTO `order`(`OrderDate`, `OrderStatus`, `OrderPrice`, `OrderDetail`, `OrderComment`, `OrderAddress`, `UserPhone`, `PaymentMethod`) VALUES (NOW(),0,?,?,?,?,?,?)") or die($this->conn->error);
        $stmt->bind_param("ssssss", $orderPrice, $orderDetail, $orderComment, $orderAddress, $userPhone, $paymentMethod);
        $result = $stmt->execute();
        $stmt->close();

        if ($result)
            return true;
        else
            return false;
    }

    /*
     * GET ALL ORDER BASED ON USERPHONE ANND STATUS
     * Return List OR NULL
     */
    public function getOrderByStatus($userPhone, $status)
    {
        $query = "SELECT * FROM `order` WHERE `OrderStatus` = '" . $status . "' AND `UserPhone` = '" . $userPhone . "'";
        $result = $this->conn->query($query) or die($this->conn->error);

        $orders = array();
        while ($order = $result->fetch_assoc()) {
            $orders[] = $order;
        }
        return $orders;
    }

    /*
     * CANCELLED ORDER
     * Return true or false
     */
    public function cancelOrder($orderId, $userPhone)
    {
        $stmt = $this->conn->prepare("UPDATE `order` SET `OrderStatus` = -1 WHERE `OrderId` = ? AND `UserPhone` = ? AND `OrderStatus` = 0") or die($this->conn->error);
        $stmt->bind_param("ss", $orderId, $userPhone);
        $result = $stmt->execute() or die($stmt->error);
        return $result;
    }
}

?>