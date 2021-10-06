<?php
require_once 'db_functions.php';
$db = new DB_Functions();

$banner = $db->getBanner();
echo json_encode($banner);

?>