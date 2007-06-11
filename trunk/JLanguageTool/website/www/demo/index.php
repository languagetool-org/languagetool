<?php
$page = "demo";
$title = "LanguageTool";
$title2 = "Demo (Beta)";
$lastmod = "2007-06-10 15:00:00 CET";
include("../../include/header.php");
?>

<?php

# TODO:
# -gets confused if there's "&lt;" or "&gt;" in the input
# -show suggetions directly under the errors (monospaced font?)

# languages with a very small number of rules are commented out:
$langs = array();
#$langs["Czech"] = "cs";
$langs["Dutch"] = "nl";
$langs["English"] = "en";
$langs["French"] = "fr";
$langs["German"] = "de";
#$langs["Italian"] = "it";
#$langs["Lithuanian"] = "lt";
$langs["Polish"] = "pl";
#$langs["Slovenian"] = "sl";
#$langs["Spanish"] = "es";
$langs["Ukrainian"] = "uk";

$defaultText["en"] = "This is a example collection with errors. ".
	"After a comma,there must be whitespace. Because to much snow was on it. ".
	"Some would think you a fortunate man. Kyoto is the most oldest city. ".
	"Its a good opportunity.";

$base_url = "http://localhost:8081/";
$limit = 20000;

$text = "";
$lang = "";
if(isset($_GET['lang'])) {
	$lang = $_GET['lang'];
}
if(isset($_POST['text'])) {
	$lang = $_POST['lang'];
	$knownLang = 0;
	foreach ($langs as $knownLang) {
		if ($knownLang == $lang) {
			$knownLang = 1;
			break;
		}
	}
	if ($knownLang == 0) {
		print "Error: language not supported in online demo";
		return;
	}

	$text = $_POST['text'];
	if (strlen($text) > $limit) {
		$text = substr($text, 0, $limit);
		print "<p><b>Note:</b> Text was shortened to $limit bytes.</p>";
	}
	# POST:
	#$contents = post_request($base_url, "language=".$_POST['lang']."&text=".urlencode(utf8_decode($text)));
	$contents = post_request($base_url, "language=".$_POST['lang']."&text=".$text);
	if (!$contents) {
		print "Error: Cannot connect LanguageTool server";
		return;
	}

	# Example output from LanguageTool (in one line):
	# <error fromy="0" fromx="0" toy="0" tox="9" ruleId="DE_AGREEMENT" msg="Mögl..."
	# replacements="" context="Das Test." contextoffset="0" errorlength="8"/>
	
	#debugging only:
	#print escape($contents);

	print "<p>Results for checking text in ";
	foreach (array_keys($langs) as $knownLang) {
		if ($langs[$knownLang] == $lang) {
			print $knownLang;
		}
	}
	print ":</p>";

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
			$context = $matches[3];
			$errorStart = $matches[4];
			$errorLen = $matches[5];
			$context = utf8_substr($context, 0, $errorStart) .
				"<span class='error'>" .
				utf8_substr($context, $errorStart, $errorLen) .
				"</span>" . 
				utf8_substr($context, $errorStart+$errorLen, strlen($context));
			print "<tr><td valign='top'><b>Context:</b></td> <td>" . $context . "</td></tr>";
			$message = $matches[1];
			$message = preg_replace("/&lt;i&gt;/", "<em>", $message);
			$message = preg_replace("/&lt;\/i&gt;/", "</em>", $message);
			print "<tr><td valign='top'><b>Message:</b></td> <td>". $message . "</td></tr>";
			$repls = preg_split("/#/", $matches[2]);
			$repl = "";
			$i = 0;
			foreach ($repls as $r) {
				$r = preg_replace("/ /", "&nbsp;", $r);
				$repl .= "<span class='suggestion'>$r</span>";
				if ($i < sizeof($repls)-1) {
					$repl .= ", ";
				}
				$i++;
			}
			print "<tr><td valign='top'><b>Suggestion:</b></td> <td>" . $repl . "</td></tr>";
			print "<tr><td>&nbsp;</td></tr>";
		}
	}
	if ($errorCount == 0) {
		print "<tr><td><b>No matches found by LanguageTool.</b></td></tr>";
	}
	print "</table>";
	print "<br/>";

}
?>

<form name="ltcheck" method="post" action="/demo/">

<table width="90%">
<tr>
	<td colspan="2">
		Text to check (max. 20KB):<br/>
		<textarea name="text" style="width:100%" rows="20"><?php
			if ($text == "") {
				if ($lang == "en" || $lang == "") {
					print $defaultText["en"];
				} else {
					# empty form
				}
			} else {
				print escape($text);
			} ?></textarea><br/>
	</td>
</tr>
<tr>
	<td>
		Language:
		<select name="lang">
			<option value="en">English</option>
			<option value="">------</option>
			<?php
			reset($langs);
			foreach (array_keys($langs) as $knownLang) {
				$code = $langs[$knownLang];
				?>
					<option value="<?php print $code ?>"<?php if ($lang == $code) {
						?> selected="selected"<?php } ?>><?php print $knownLang ?></option>
				<?php
			}
			?>
		</select>
	</td>
	<td align="right">
		<input type="submit" value="Clear input"
			onclick="document.forms['ltcheck'].text.value='';return false;" />
			<noscript>(requires Javascript)</noscript>
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
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
<li>Some languages with a very small number of rules are not available on this page.</li>
</ul>

<?php
include("../../include/footer.php");
?>
