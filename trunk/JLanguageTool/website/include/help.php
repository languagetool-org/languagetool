<?php
function hl($xml, $class="xmlcode") {
	$geshi = new GeSHi($xml, "XML", "../../include/geshi/");
	$geshi->set_header_type(GESHI_HEADER_NONE);
	print "<div class='".$class."'>".$geshi->parse_code()."</div>";
}

function hljava($code, $class="xmlcode") {
	$geshi = new GeSHi($code, "Java5", "../../include/geshi/");
	$geshi->set_header_type(GESHI_HEADER_NONE);
	print "<div class='".$class."'>".$geshi->parse_code()."</div>";
}

function show_link($title, $url, $show_alt, $title_attr="") {
	global $homepage;
	$html = "";
	$alt = "";
	$img_path = "/images";
	if( $homepage ) {
		$img_path = "art";
	}
	if( strpos($url, "mailto:") === false && $show_alt ) {
		if( strpos($url, "http:") === false ) {
			$alt = "internal link to ".$title;
		} else {
			$alt = "external link to ".$title;
		}
	} else if ( $show_alt ) {
		$alt = "email";
	}
	if( $title_attr ) {
		$title_attr = 'title="'.$title_attr.'"';
	}
	if( strpos($url, "http:") === false ) {
		$html .= '<a '.$title_attr.' href="'.$url.'"><img src="'.$img_path.'/link.png" border="0" hspace="2" width="8" height="9" alt="'.$alt.'" />';
	} else {
		$html .= '<a '.$title_attr.' href="'.$url.'"><img src="'.$img_path.'/link_extern.png" border="0" hspace="2" width="7" height="9" alt="'.$alt.'" />';
	}
	$html .= $title.'</a>';
	return $html;
}

function getmicrotime() { 
	list($usec, $sec) = explode(" ",microtime()); 
	return ((float)$usec + (float)$sec); 
}

function send_last_modified_header() {
	global $last_update_full;
	$timestamp = strtotime($last_update_full);
	header("Last-Modified: ".date("D, j M Y G:i:s T", $timestamp));
}

/** Escape stuff that gets printed to page to avoid cross site scripting. */
function escape($string) {
	$string = preg_replace("/&/", "&amp;", $string);
	$string = preg_replace("/\"/", "&quot;", $string);
	$string = preg_replace("/'/", "&apos;", $string);
	$string = preg_replace("/</", "&lt;", $string);
	$string = preg_replace("/>/", "&gt;", $string);
	return $string;
}

/** Unescape stuff that gets printed to page to avoid cross site scripting. */
function unescape($string) {
	$string = preg_replace("/&quot;/", "\"", $string);
	$string = preg_replace("/&apos;/", "'", $string);
	$string = preg_replace("/&lt;/", "<", $string);
	$string = preg_replace("/&gt;/", ">", $string);
	$string = preg_replace("/&amp;/", "&", $string);
	return $string;
}

function post_request($url, $data, $optional_headers=null) {
	$params = array('http' => array(
		'method' => 'POST',
		'content' => $data));
	if ($optional_headers !== null) {
		$params['http']['header'] = $optional_headers;
	}
	$ctx = stream_context_create($params);
	$fp = @fopen($url, 'rb', false, $ctx);
	if (!$fp) {
		return "";
	}
	$resp = @stream_get_contents($fp);
	if ($resp === false) {
		return "";
	}
	return $resp;
}

// see http://www.php.net/manual/en/function.substr.php:
function utf8_substr($str, $start) { 
   preg_match_all("/./u", $str, $ar); 
   if (func_num_args() >= 3) { 
       $end = func_get_arg(2); 
       return join("",array_slice($ar[0], $start, $end)); 
   } else { 
       return join("",array_slice($ar[0], $start)); 
   } 
}

?>
