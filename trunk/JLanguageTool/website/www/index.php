<?php
$page = "homepage";
$title = "LanguageTool";
$title2 = "Open Source language checker";
$lastmod = "2011-08-28 23:20:00 CET";
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
	<h2><?=show_link("Download LanguageTool 1.4 (18&nbsp;MB)", "download/LanguageTool-1.4.oxt", 0) ?></h2>
	<!--
	<table>
	<tr>
		<td><h2><?=show_link("Download LanguageTool 1.0.0 (18&nbsp;MB)", "download/LanguageTool-1.1.oxt", 0) ?></h2></td>
		<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
		<td><h2><?=show_link("Download LanguageTool 1.1beta (18&nbsp;MB)", "download//LanguageTool-1.1-beta1.oxt", 0) ?></h2></td>
	</tr>
	</table>
	-->
	<ul>
		<li>Requires <?=show_link("Java", "http://www.java.com/en/download/manual.jsp", 1)?>&nbsp;6.0
			or later. This version only works with OpenOffice.org 3.0.1 or later and LibreOffice
			and you need to <strong>restart OpenOffice.org/LibreOffice</strong> after installation of this extension.</li>
		<li>If you're upgrading from LanguageTool 0.9.5, you must de-install
			it <strong>before</strong> upgrading to a later version (check
			<a href="http://languagetool.wikidot.com/removing-languagetool-0-9-5-from-openoffice-3-0-1">this
			page</a> if you forgot to do so).</li>
		<li>Please report bugs to the <?=show_link("Sourceforge bug tracker", "http://sourceforge.net/tracker/?group_id=110216&amp;atid=655717", 1)?>
			or send an email to naber <i>at</i> danielnaber.de.</li>
	</ul>
</div>

<!-- -->
<p><strong>Try LanguageTool 1.4 without installation, using Java WebStart.</strong> Requires Java 1.6_04 or later.<br />
<strong><?=show_link("Start LanguageTool (18&nbsp;MB)", "webstart/web/LanguageTool.jnlp", 0) ?></strong></p>
<!-- -->

<p>Untested daily snapshots of the current development version are available at
<?=show_link("the snapshot directory", "download/snapshots/", 0) ?> (<?=show_link("CHANGES.txt", "http://languagetool.svn.sourceforge.net/viewvc/languagetool/trunk/JLanguageTool/CHANGES.txt", 0) ?>).

<h2>News</h2>

<p><strong>2011-08-28:</strong> Our <?=show_link("Google Summer of Code students", "gsoc2011", 0)?> have 
successfully finished their projects and the results will be part of LanguageTool 1.5, to be released in about one month.</p>

<p><strong>2011-06-26:</strong> Released LanguageTool 1.4. Changes include:
<ul>
	<li>Rule updates for English, French, German, Russian, and Esperanto.</li>
    <li>Support for Khmer</li>
    <li>Some internal cleanups and simplifications</li>
	<li>For a more detailed list of changes, see the <?=show_link("Changelog", "download/CHANGES.txt", 0) ?></li>
</ul>

<!--
<p><strong>2011-03-28:</strong> Released LanguageTool 1.3.1. This fixes a NullPointerException occurring with some inputs.</p>
<p><strong>2011-03-27:</strong> Released LanguageTool 1.3. Changes include:
<ul>
	<li>Rule updates for Spanish, French, Polish, Dutch, Russian, English, and Esperanto.</li>
    <li>Reduced false alarms for Spanish in a significant way.</li>
	<li>Some bug fixes.</li>
	<li>For a more detailed list of changes, see the <?=show_link("Changelog", "download/CHANGES.txt", 0) ?></li>
</ul>
-->

<p><strong>2011-02-19:</strong> The LanguageTool source code is now available from
<?=show_link("subversion", "development/#checkout", 0)?>. Please don't use the old CVS repository anymore.</p>

<!--
<p><strong>2011-01-02:</strong> Released LanguageTool 1.2. Changes include:
<ul>
	<li>Rule updates for Romanian, Dutch, Polish, German, Russian, Spanish, French and Danish.</li>
	<li>Added new scripts testwikipedia.sh and testwikipedia.bat to the distribution. These
		let you check a local Wikipedia XML dump.</li>
	<li>Added support for Esperanto.</li>
	<li>Small bug fixes.</li>
	<li>For a more detailed list of changes, see the <?=show_link("Changelog", "download/CHANGES.txt", 0) ?></li>
</ul>

<p><strong>2010-09-26:</strong> Released version 1.1. For a list of changes, see the <?=show_link("Changelog", "download/CHANGES.txt", 0) ?></p>

<p><strong>2010-08-29:</strong> 

There's a new <?=show_link("script to use LanguageTool from within vim", "http://www.vim.org/scripts/script.php?script_id=3223", 0)?>.</p>

<p><strong>2010-02-20:</strong> 

LanguageTool has been integrated into <?=show_link("After the Deadline", "http://open.afterthedeadline.com/", 0)?>,
a powerful English grammar checker. Thanks to LanguageTool it now also  supports French and German. 
This means that you can now use LanguageTool for these languages via the
<?=show_link("After the Deadline Firefox plugin", "http://firefox.afterthedeadline.com/", 0)?>.</p>
-->

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
