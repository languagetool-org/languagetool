<?php
$page = "links";
$title = "LanguageTool";
$title2 = "Links";
$lastmod = "2007-06-05 21:35:00 CET";
include("../../include/header.php");
?>

<p><strong>Contact:</strong><br />
LanguageTool was originally written by Daniel Naber and is now maintained by 
Daniel Naber and Marcin Miłkowski. To contact me, subscribe to 
the mailing list or see my homepage at <a href="http://www.danielnaber.de">www.danielnaber.de</a>.</p>

<p><strong>Mailing lists:</strong><br />
Development and user discussion:
 <?=show_link("Subscribe/Unsubscribe",  "http://lists.sourceforge.net/mailman/listinfo/languagetool-devel", 0) ?>
 (<?=show_link("archive", "http://sourceforge.net/mailarchive/forum.php?forum_name=languagetool-devel", 0)
?>)<br />
CVS commit messages: <?=show_link("Subscribe/Unsubscribe", "http://lists.sourceforge.net/mailman/listinfo/languagetool-cvs", 0) ?>
 (<?=show_link("archive", "http://sourceforge.net/mailarchive/forum.php?forum_name=languagetool-cvs", 0)
?>)</p>
		
<p class="firstpara"><strong>Links</strong> to other Open Source language tools:<br />
<?=show_link("An Gramad&oacute;ir", "http://borel.slu.edu/gramadoir/", 0)?>,
a grammar checker for the Irish language<br />
<?=show_link("CoGrOO", "http://cogroo.incubadora.fapesp.br/portal", 0)?>,
a Grammar Checker for Portuguese<br />
<?=show_link("GRAC", "http://grac.sourceforge.net/", 0)?>,
corpus-based grammar checker written in Python<br />
<?=show_link("Queequeg", "http://queequeg.sourceforge.net/index-e.html", 0)?>,
agreement checker written in Python<br />
<?=show_link("Old version of LanguageTool written in Python", "http://tkltrans.sourceforge.net/#r03", 1) ?>
</p>

<p>
<?=show_link("LanguageTool integration for LyX", "http://wiki.lyx.org/Tools/LyX-GrammarChecker", 1) ?>
</p>

<p>
<?=show_link("LanguageTool in Python", "http://tkltrans.sourceforge.net/#r03", 0) ?>, a much older
and less powerful version without OpenOffice.org integration but support for Hungarian<br />
</p>

<p><strong>Resources:</strong><br />
<?=show_link("XML file with 221 collected English grammar errors", "/download/errors.xml", 0) ?>, 23KB<br />
<?=show_link("Another English error collection", 
"http://languagetool.cvs.sourceforge.net/languagetool/JLanguageTool/resource/en/errors.txt?view=markup", 1) ?><br />
<?=show_link("German error collection", 
"http://languagetool.cvs.sourceforge.net/languagetool/JLanguageTool/resource/de/errors.txt?view=markup", 1) ?>
</p>

<p>Website design inspired by <!-- Please leave this if you use our template. Thank you -->
<a href="http://www.darjanpanic.com" target="_blank"
	title="Freelance Graphic artist">Darjan Panic</a> &amp; <a
	href="http://www.briangreens.com" target="_blank">Brian Green</a> <!-- Please leave this if you use our template. Thank you -->
</p>

<?php
include("../../include/footer.php");
?>
