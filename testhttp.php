<?php

$message = $_REQUEST["message"];

if (!empty($message)) {
    $fp = fopen("record.txt", "a");
    fwrite($fp, $message);
    fwrite($fp, "\n");
    fclose($fp);

    $a = array("status" => 0, "reply" => "已记录" . $message);
    echo json_encode($a);
}

