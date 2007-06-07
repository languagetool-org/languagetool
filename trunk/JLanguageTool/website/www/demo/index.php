<?php
$page = "demo";
$title = "LanguageTool";
$title2 = "Demo (Beta)";
$lastmod = "2007-06-07 15:00:00 CET";
include("../../include/header.php");
?>

<?php

# TODO:
# -highlighting incorrect when there are quotes etc in the text
# -add more languages
# -usability: show language used before displaying the results

$base_url = "http://localhost:8081/";
$limit = 20000;

$text = "";
$lang = "";
if(isset($_POST['text'])) {
	$lang = $_POST['lang'];
	if (! ($lang == "en" || $lang == "pl" || $lang == "de" || $lang == "fr" || $lang == "nl")) {
		print "Error: language not supported in online demo";
		return;
	}
	$text = $_POST['text'];
	if (strlen($text) > $limit) {
		$text = substr($text, 0, $limit);
		print "<p><b>Note:</b> Text was shortened to $limit bytes.</p>";
	}
	# GET:
	#$url = $base_url . "?language=".$_POST['lang']."&text=".urlencode(utf8_decode($text));
	# POST:
	$contents = post_request($base_url, "language=".$_POST['lang']."&text=".urlencode(utf8_decode($text)));
	if (!$contents) {
		print "Error: Cannot connect LanguageTool server";
		return;
	}
	# Example:
	# <error fromy="0" fromx="0" toy="0" tox="9" ruleId="DE_AGREEMENT" msg="Mögl..." replacements="" context="Das Test." contextoffset="0" errorlength="8"/>
	
	#debugging only:
	#print escape($contents);

	$lines = split("\n", $contents);
	print "<table border='0'>";
	$errorCount = 0;
	foreach ($lines as $line) {
		$line = preg_replace("/</", "&lt;", $line);
		$line = preg_replace("/>/", "&gt;", $line);
		if (strpos($line, "error") === false) {
			# ignore
		} else {
			$errorCount++;
			preg_match('/msg="(.*?)" replacements="(.*?)" context="(.*?)" contextoffset="(.*?)" errorlength="(.*?)"/', $line, $matches);
			print "<tr><td valign='top'><b>Message:</b></td> <td>". $matches[1] . "</td></tr>";
			print "<tr><td valign='top'><b>Replacements:</b></td> <td>" . $matches[2] . "</td></tr>";
			$context = $matches[3];
			$errorStart = $matches[4];
			$errorLen = $matches[5];
			$context = substr($context, 0, $errorStart) . "<span class='error'>" .
				substr($context, $errorStart, $errorLen) . "</span>" . 
				substr($context, $errorStart+$errorLen);
			print "<tr><td valign='top'><b>Context:</b></td> <td>" . unescape($context) . "</td></tr>";
			print "<tr><td>&nbsp;</td></tr>";
		}
	}
	print "</table>";
	if ($errorCount == 0) {
		print "<p><b>No matches found by LanguageTool.</b></p>";
	}
	print "<br/>";

}
?>

<form method="post" action="/demo/">

<table width="90%">
<tr>
	<td colspan="2">
		Text to check (max. 20KB):<br/>
		<textarea name="text" style="width:100%" rows="20"><?php print escape($text) ?></textarea><br/>
	</td>
</tr>
<tr>
	<td>
		Language:
		<select name="lang">
			<option value="en" <?php if ($lang == 'en') { ?> selected="selected"<?php } ?>>English</option>
			<option value="pl" <?php if ($lang == 'pl') { ?> selected="selected"<?php } ?>>Polish</option>
			<option value="de" <?php if ($lang == 'de') { ?> selected="selected"<?php } ?>>German</option>
			<option value="fr" <?php if ($lang == 'fr') { ?> selected="selected"<?php } ?>>French</option>
			<option value="nl" <?php if ($lang == 'nl') { ?> selected="selected"<?php } ?>>Dutch</option>
		</select>
	</td>
	<td align="right">
		<input type="submit" value="Check text"/>
	</td>
</tr>
</table>

</form>

<p>Please note:</p>

<ul>
<li>LanguageTool does not include spell checking</li>
<li>LanguageTool can only detect a limited number of errors for any language.
Some languages have a very small numbers of rules and thus only very few errors can
be detected. See <?php print show_link("the language page", "/languages/", 0) ?> for
a rough overview of how good a language is supported by LanguageTool.</li>
</ul>

<?php
include("../../include/footer.php");
?>
