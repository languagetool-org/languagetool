<?php
$page = "links";
$title = "LanguageTool";
$title2 = "Links &amp; Resources";
$lastmod = "2011-02-20 12:35:00 CET";
include("../../include/header.php");
?>

<p><strong>Contact:</strong><br />
LanguageTool was originally written by Daniel Naber and is now maintained by 
Daniel Naber (<strong>naber <span>a&#116;</span> danielnaber<span>.</span>de</strong>) and Marcin Miłkowski
(<a href="http://marcinmilkowski.pl/en/Contact/">contact form</a>).</p>

<p><strong>Issue tracking:</strong></p>

<ul style="list-style:none">
	<li><?=show_link("Bug reports", "http://sourceforge.net/tracker/?limit=25&amp;func=&amp;group_id=110216&amp;atid=655717&amp;assignee=&amp;status=&amp;category=&amp;artgroup=&amp;keyword=&amp;submitter=&amp;artifact_id=&amp;assignee=&amp;status=1&amp;category=&amp;artgroup=&amp;submitter=&amp;keyword=&amp;artifact_id=&amp;submit=Filter", 0)?></li>
	<li><?=show_link("Feature requests", "http://sourceforge.net/tracker/?limit=25&amp;func=&amp;group_id=110216&amp;atid=655720&amp;assignee=&amp;status=&amp;category=&amp;artgroup=&amp;keyword=&amp;submitter=&amp;artifact_id=&amp;assignee=&amp;status=1&amp;category=&amp;artgroup=&amp;submitter=&amp;keyword=&amp;artifact_id=&amp;submit=Filter", 0)?></li>
</ul>

<p><strong>Mailing lists:</strong></p>

<ul style="list-style:none">
	<li>Development and user discussion:
 		<?=show_link("Subscribe/Unsubscribe",  "http://lists.sourceforge.net/mailman/listinfo/languagetool-devel", 0) ?>,
 		<?=show_link("archive", "http://sourceforge.net/mailarchive/forum.php?forum_name=languagetool-devel", 0)?></li>
	<li>SVN commit messages: <?=show_link("Subscribe/Unsubscribe", "http://lists.sourceforge.net/mailman/listinfo/languagetool-cvs", 0) ?>,
 		<?=show_link("archive", "http://sourceforge.net/mailarchive/forum.php?forum_name=languagetool-cvs", 0)?></li>
</ul>

<p class="firstpara"><strong>LanguageTool integration</strong>:</p>

<ul style="list-style:none">
	<li><?=show_link("LanguageTool for vim", "http://www.vim.org/scripts/script.php?script_id=3223", 1) ?></li>
	<li><?=show_link("LanguageTool for LyX", "http://wiki.lyx.org/Tools/LyX-GrammarChecker", 1) ?></li>
	<li><?=show_link("LanguageTool plugin for OmegaT", "https://sourceforge.net/projects/omegat-plugins/files/OmegaT-LanguageTool/", 1)?>
		a plugin that enables grammar-checking in computer-aided translation tool OmegaT (open source)</li>
	<li><?=show_link("LanguageTool in CheckMate", "http://www.opentag.com/okapi/wiki/index.php?title=CheckMate", 1)?> used as a server to enhance translation QA</li>
	<li><?=show_link("LanguageTool for Thunderbird", "https://addons.mozilla.org/en-US/thunderbird/addon/14781", 1)?></li>
</ul>
		
<p class="firstpara"><strong>Links</strong> to other Open Source language tools:</p>

<ul style="list-style:none">
	<li><?=show_link("After the Deadline", "http://open.afterthedeadline.com/", 0)?>,
		a grammar checker for English which integrates LanguageTool to support German and French</li>
	<!-- <li><?=show_link("LangBot", "http://apoema.net/langbot/en/gc.lb", 0)?>,
		an on-line grammar checker which uses LanguageTool, and includes integration
		for Firefox via <?=show_link("Ubiquity", "http://ubiquity.mozilla.com/", 0) ?> plugin</li> -->				
	<li><?=show_link("An Gramad&oacute;ir", "http://borel.slu.edu/gramadoir/", 0)?>,
		a grammar checker for the Irish language</li>
	<li><?=show_link("CoGrOO", "http://cogroo.sourceforge.net/", 0)?>
		a Grammar Checker for Portuguese</li>
	<li><?=show_link("GRAC", "http://grac.sourceforge.net/", 0)?>
		corpus-based grammar checker written in Python</li>
	<li><?=show_link("Queequeg", "http://queequeg.sourceforge.net/index-e.html", 0)?>
		agreement checker written in Python</li>
	<li><?=show_link("LanguageTool in Python", "http://tkltrans.sourceforge.net/#r03", 0) ?>, a much older
		and less powerful version without OpenOffice.org integration but support for Hungarian</li>
</ul>

<p><strong>Resources:</strong></p>

<ul style="list-style:none">
	<li><?=show_link("XML file with 221 collected English grammar errors", "/download/errors.xml", 0) ?>, 23KB</li>
	<li><?=show_link("Another English error collection", 
		"http://languagetool.svn.sourceforge.net/viewvc/languagetool/trunk/JLanguageTool/src/resource/en/errors.txt?view=markup", 1) ?></li>
	<li><?=show_link("German error collection", 
		"http://languagetool.svn.sourceforge.net/viewvc/languagetool/trunk/JLanguageTool/src/resource/de/errors.txt?view=markup", 1) ?></li>
</ul>

<br /><br /><br />

<p>Website design inspired by <!-- Please leave this if you use our template. Thank you -->
<a href="http://www.darjanpanic.com" target="_blank"
	title="Freelance Graphic artist">Darjan Panic</a> &amp; <a
	href="http://www.briangreens.com" target="_blank">Brian Green</a> <!-- Please leave this if you use our template. Thank you -->
</p>

<?php
include("../../include/footer.php");
?>
