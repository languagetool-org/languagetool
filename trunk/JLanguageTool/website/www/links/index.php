<?php
$page = "links";
$title = "LanguageTool";
$title2 = "Links";
$lastmod = "2010-02-21 21:35:00 CET";
include("../../include/header.php");
?>

<p><strong>Contact:</strong><br />
LanguageTool was originally written by Daniel Naber and is now maintained by 
Daniel Naber and Marcin Miłkowski. To contact me, subscribe to 
the mailing list or see my homepage at <a href="http://www.danielnaber.de">www.danielnaber.de</a>.</p>

<p><strong>Mailing lists:</strong></p>

<ul style="list-style:none">
	<li>Development and user discussion:
 		<?=show_link("Subscribe/Unsubscribe",  "http://lists.sourceforge.net/mailman/listinfo/languagetool-devel", 0) ?>
 		(<?=show_link("archive", "http://sourceforge.net/mailarchive/forum.php?forum_name=languagetool-devel", 0)
		?>)</li>
	<li>CVS commit messages: <?=show_link("Subscribe/Unsubscribe", "http://lists.sourceforge.net/mailman/listinfo/languagetool-cvs", 0) ?>
 		(<?=show_link("archive", "http://sourceforge.net/mailarchive/forum.php?forum_name=languagetool-cvs", 0)?>)</li>
</ul>
		
<p class="firstpara"><strong>Links</strong> to other Open Source language tools:</p>

<ul style="list-style:none">
	<li><?=show_link("After the Deadline", "http://open.afterthedeadline.com/", 0)?>,
		a grammar checker for English which integrates LanguageTool to support German and French</li>
	<li><?=show_link("LangBot", "http://apoema.net/langbot/en/gc.lb", 0)?>,
		an on-line grammar checker which uses LanguageTool, and includes integration
		for Firefox via <?=show_link("Ubiquity", "http://ubiquity.mozilla.com/", 0) ?> plugin</li>				
	<li><?=show_link("An Gramad&oacute;ir", "http://borel.slu.edu/gramadoir/", 0)?>,
		a grammar checker for the Irish language</li>
	<li><?=show_link("CoGrOO", "http://cogroo.sourceforge.net/", 0)?>
		a Grammar Checker for Portuguese</li>
	<li><?=show_link("GRAC", "http://grac.sourceforge.net/", 0)?>
		corpus-based grammar checker written in Python</li>
	<li><?=show_link("Queequeg", "http://queequeg.sourceforge.net/index-e.html", 0)?>
		agreement checker written in Python</li>
	<li><?=show_link("LanguageTool plugin for OmegaT", "https://sourceforge.net/projects/omegat-plugins/files/OmegaT-LanguageTool/", 1)?>
		a plugin that enables grammar-checking in computer-aided translation tool OmegaT (open source)</li>
	<li><?=show_link("LanguageTool integration for LyX", "http://wiki.lyx.org/Tools/LyX-GrammarChecker", 1) ?></li>
	<li><?=show_link("LanguageTool in Python", "http://tkltrans.sourceforge.net/#r03", 0) ?>, a much older
		and less powerful version without OpenOffice.org integration but support for Hungarian</li>
</ul>

<p><strong>Resources:</strong></p>

<ul style="list-style:none">
	<li><?=show_link("XML file with 221 collected English grammar errors", "/download/errors.xml", 0) ?>, 23KB</li>
	<li><?=show_link("Another English error collection", 
		"http://languagetool.cvs.sourceforge.net/languagetool/JLanguageTool/resource/en/errors.txt?view=markup", 1) ?></li>
	<li><?=show_link("German error collection", 
		"http://languagetool.cvs.sourceforge.net/languagetool/JLanguageTool/resource/de/errors.txt?view=markup", 1) ?></li>
</ul>

<p>Website design inspired by <!-- Please leave this if you use our template. Thank you -->
<a href="http://www.darjanpanic.com" target="_blank"
	title="Freelance Graphic artist">Darjan Panic</a> &amp; <a
	href="http://www.briangreens.com" target="_blank">Brian Green</a> <!-- Please leave this if you use our template. Thank you -->
</p>

<?php
include("../../include/footer.php");
?>
