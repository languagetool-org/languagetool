<?php
$page = "homepage";
$title = "LanguageTool";
$title2 = "Open Source language checker";
$lastmod = "2011-09-25 16:20:00 CET";
include("../include/header.php");
?>
		
<p class="firstpara"><strong>An Open Source style and grammar checker for English, French, German, Polish, 
Dutch, Romanian, and <?=show_link("other languages", "languages/", 0) ?>.</strong>
You can think of LanguageTool as a software to detect errors that a simple spell checker cannot detect, e.g. mixing
up <em>there/their</em>, <em>no/now</em> etc. It can also detect some grammar mistakes. It does not include spell checking.</p>

<p>LanguageTool will only find errors for which a rule is defined in its 
XML configuration files. Rules for more complicated errors can be  written in Java.</p>


<h2>Download</h2>

<div class="downloadSection">
	<h2><?=show_link("Download LanguageTool 1.5 (28&nbsp;MB)", "download/LanguageTool-1.5.oxt", 0) ?></h2>
	<ul>
		<li>Requires <?=show_link("Java", "http://www.java.com/en/download/manual.jsp", 1)?>&nbsp;6.0
			or later. You need to <strong>restart OpenOffice.org/LibreOffice</strong> after installation of this extension.</li>
		<li>If you're upgrading from LanguageTool 0.9.5, you must de-install
			it <strong>before</strong> upgrading to a later version (check
			<a href="http://languagetool.wikidot.com/removing-languagetool-0-9-5-from-openoffice-3-0-1">this
			page</a> if you forgot to do so).</li>
		<li>Please report bugs to the <?=show_link("Sourceforge bug tracker", "http://sourceforge.net/tracker/?group_id=110216&amp;atid=655717", 1)?>
			or send an email to naber <i>at</i> danielnaber.de.</li>
	</ul>
</div>

<!-- -->
<p><strong>Try LanguageTool 1.5 without installation, using Java WebStart.</strong> Requires Java 1.6_04 or later.<br />
<strong><?=show_link("Start LanguageTool (28&nbsp;MB)", "webstart/web/LanguageTool.jnlp", 0) ?></strong></p>
<!-- -->

<p>Untested daily snapshots of the current development version are available at
<?=show_link("the snapshot directory", "download/snapshots/", 0) ?> (<?=show_link("CHANGES.txt", "http://languagetool.svn.sourceforge.net/viewvc/languagetool/trunk/JLanguageTool/CHANGES.txt", 0) ?>).

<h2>News</h2>

<p><strong>2011-09-25:</strong> Released LanguageTool 1.5. Changes include:
<ul>
    <li>Support for new languages: Chinese, Asturian, Breton, and Tagalog</li>
    <li>Automatic language detection (not relevant for OpenOffice.org/LibreOffice)</li>
    <li>Many rule updates for several languages</li>
    <li>For a more detailed list of changes, see the <?=show_link("Changelog", "download/CHANGES.txt", 0) ?></li>
</ul>

<p><strong>2011-08-28:</strong> Our <?=show_link("Google Summer of Code students", "gsoc2011", 0)?> have 
successfully finished their projects and the results will be part of LanguageTool 1.5, to be released in about one month.</p>


<h2>Installation and Usage</h2>

<ul>
	<li><strong>In OpenOffice.org/LibreOffice</strong>:
	Double click <tt>LanguageTool-1.4.oxt</tt> to install it.
	If that doesn't work, call <em>Tools -&gt; Extension Manager -&gt; Add...</em>
	to install it. Close OpenOffice.org and re-start it. Type some text
	with an error (e.g. "This is an test." &ndash; make sure the text language is set
	to English) and you should see a blue underline.</li>

	<li>Also see <?=show_link("Usage", "usage/", 0)?> for using LanguageTool outside of OpenOffice.org.</li>
</ul>


<h2>Need Help?</h2>

<p>Please see the <?=show_link("list of common problems", "issues", 0)?>.</p>


<h2>License &amp; Source Code</h2>

<p>LanguageTool is freely available under the <?=show_link("LGPL", "http://www.fsf.org/licensing/licenses/lgpl.html#SEC1", 0)?>.
The source is available <?=show_link("at Sourceforge", "http://sourceforge.net/projects/languagetool/", 1) ?> via SVN</p>

<div style="height:750px"></div>

<?php
include("../include/footer.php");
?>
