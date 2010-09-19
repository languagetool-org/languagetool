<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><?php print $title." ".$title2 ?></title>
<link href="/css/style.css" rel="stylesheet" type="text/css" />
</head>
<body>

<?php
list($usec, $sec) = explode(" ", microtime()); 
$start_time = ((float)$usec + (float)$sec);
include("help.php");

function makeEntry($name, $visName) {
	global $page;
	if ($page == $name || ($name == "." && $page == "homepage")) {
		?>
		<p class="activeMenuitem"><? print $visName ?></p>
		<?php
	} else {
		$url = $name;
		if ($page == "homepage") {
			$url = $name;
		} else {
			if (substr($name, 0, 7) == "http://") {
				$url = $name;
			} else {
				$url = "../".$name;
			}
		}
		?>
		<p class="menuitem"><a href="<?php print $url ?>"><? print $visName ?></a></p>
		<?php
	}
}
?>

<table border="0">
<tr>
	<td></td>
	<td>
		<?php if ($page == "homepage") { ?>
			<h1><?php print $title ?></h1>
		<?php } else { ?>
			<h1 id="hplink"><a href="/"><?php print $title ?></a></h1>
		<?php } ?>
	</td>
	<td><h1 style="text-align:left"><?php print $title2 ?></h1></td>
</tr>
<tr>
	<td width="88"></td>
	<td width="201" valign="top">
		<div id="menu">
			<?php makeEntry(".", "Homepage"); ?>
			<?php makeEntry("screenshots", "Screenshots"); ?>
			<?php makeEntry("demo", "Demo"); ?>
			<?php makeEntry("languages", "Languages"); ?>
			<?php makeEntry("usage", "Usage"); ?>
			<?php makeEntry("development", "Development"); ?>
			<?php makeEntry("links", "Links &amp; Resources"); ?>
			<?php makeEntry("http://languagetool.wikidot.com/", "Wiki"); ?>
			
			<br/><br/><br/>
			<a href="http://twitter.com/languagetoolorg"><img border="0" style="margin-right:5px" src="/images/twitter_link16x16.png"/>Follow us on twitter</a>
		</div>
	</td>
	<td class="content">

		<!-- MAIN TEXT -->
