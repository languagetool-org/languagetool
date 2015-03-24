<?php
// LanguageTool Proxy Script
// requires curl for PHP - on Ubuntu, install with "sudo apt-get install php5-curl"
error_reporting(E_ALL);

function shutdown($curl) {
  // close curl even if the script was aborted, see
  // http://php.net/manual/en/features.connection-handling.php
  curl_close($curl);
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
  $postText = trim(file_get_contents('php://input'));
  $postText = html_entity_decode($postText, ENT_COMPAT, "UTF-8");
  
  $curl = curl_init();
  register_shutdown_function('shutdown', $curl);
  curl_setopt($curl, CURLOPT_URL, "http://127.0.0.1");
  curl_setopt($curl, CURLOPT_PORT, 8081);
  curl_setopt($curl, CURLOPT_POST, true);
  curl_setopt($curl, CURLOPT_POSTFIELDS, $postText);
  curl_setopt($curl, CURLOPT_HEADER, 0);
  if (isset($_SERVER['HTTP_REFERER'])) {
    curl_setopt($curl, CURLOPT_REFERER, $_SERVER['HTTP_REFERER']);
  }
  $realIp = $_SERVER['REMOTE_ADDR'];
  curl_setopt($curl, CURLOPT_HTTPHEADER, array("X-forwarded-for: $realIp"));

  header("Content-Type: text/xml; charset=utf-8");
  //for debugging:
  //header("Content-Type: text/plain");
  
  if (curl_exec($curl) === false) {
    $errorMessage = curl_error($curl);
    print "Error: " . $errorMessage;
    error_log("proxy.php error: $errorMessage");
  }
  curl_close($curl);
} else {
  print "Error: this proxy only supports POST";
}
?>
