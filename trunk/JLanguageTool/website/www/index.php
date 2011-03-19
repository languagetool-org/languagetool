<?php
$page = "homepage";
$title = "LanguageTool";
$title2 = "Open Source language checker";
$lastmod = "2011-03-19 11:20:00 CET";
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
	<h2><?=show_link("Download LanguageTool 1.2 (18&nbsp;MB)", "download/LanguageTool-1.2.oxt", 0) ?></h2>
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
		<li>Requires <?=show_link("Java", "http://www.java.com/en/download/manual.jsp", 1)?>&nbsp;5.0
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
<p><strong>Try LanguageTool 1.2 without local installation, using Java WebStart.</strong> Requires Java 1.6_04 or later. You will get a security warning which you
need to ignore:<br />
<strong><?=show_link("Start LanguageTool (18&nbsp;MB)", "webstart/web/LanguageTool.jnlp", 0) ?></strong></p>
<!-- -->


<h2>News</h2>

<p><strong>2011-03-19:</strong> LanguageTool participates in the <?=show_link("Google Summer of Code 2011", "gsoc2011", 0)?></p>

<p><strong>2011-02-19:</strong> The LanguageTool source code is now available from
<?=show_link("subversion", "development/#checkout", 0)?>. Please don't use the old CVS repository anymore.</p>

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


<h2>Installation and Usage</h2>

<ul>
	<li><strong>In OpenOffice.org/LibreOffice</strong>:
	Double click <tt>LanguageTool-1.2.oxt</tt> to install it.
	If that doesn't work, call <em>Tools -&gt; Extension Manager -&gt; Add...</em>
	to install it. Close OpenOffice.org and re-start it. Type some text
	with an error (e.g. "This is an test." &ndash; make sure the text language is set
	to English) and you should see a blue underline.</li>

	<li>Also see <?=show_link("Usage", "usage/", 0)?> for using LanguageTool outside of OpenOffice.org.</li>
</ul>

<h2><a name="commonproblems"><strong>Common problems with OpenOffice.org/LibreOffice integration</strong></a></h2>

<ul>
<li>Did you restart OpenOffice.org - including the QuickStarter - after installation of LanguageTool? This is required,
	even if OpenOffice.org doesn't say so. (<a href="http://qa.openoffice.org/issues/show_bug.cgi?id=88692">Issue 88692</a>)</li>
<li>LanguageTool installation fails if the name of your user account contains
	special characters. The only workaround so far seems to be to use a different
	user account. (<a href="http://qa.openoffice.org/issues/show_bug.cgi?id=95162">Issue 95162</a>)</li>
<li>Make sure <a href="http://www.java.com/en/download/manual.jsp">Java 5.0 or later from Sun Microsystems</a>
	is installed on your system. Java versions which are not from Sun Microsystems may not work.</li>
<li>Make sure this version of Java is selected in OpenOffice.org
	(under <em>Tools -&gt; Options -&gt; Java</em>).</li>
<li>If LanguageTool doesn't start and you see no error message, please
	check if the extension is enabled in the Extension manager 
	(under <em>Tools -&gt; Extension Manager</em>).</li>
<li>On Ubuntu, when you get an error message during installation, you might need to
	install the <tt>openoffice.org-java-common</tt> package. See
	<a href="http://nancib.wordpress.com/2008/05/03/fixing-the-openofficeorg-grammar-glitch-in-ubuntu-hardy/">this blog posting</a>
	for details.</li>
<li>The menu items in OpenOffice.org get mixed up when both <a href="http://open.afterthedeadline.com/">After the Deadline</a>
	and LanguageTool are installed. This issue is tracked as <a href="http://openatd.trac.wordpress.org/ticket/215">ticket #215 at After the Deadline</a>.</li>
<li>LanguageTool didn't work together with the <a href="http://extensions.services.openoffice.org/en/project/DeltaXMLODTCompare">DeltaXML 
	ODT Compare</a> extension - use version 1.2.0 of DeltaXML ODT Compare, which fixes the problem.</li>
<li>If you get a message "Can not activate the factory for com.sun.star.help.HelpIndexer because java.lang.NoClassDefFoundError: org/apache/lucene/analysis/cjk/CJKAnalyzer":
	this was a bug In OpenOffice.org 3.1, it was fixed in version 3.2 (<a href="http://qa.openoffice.org/issues/show_bug.cgi?id=98680">Issue 98680</a>)</li>
<li>If you get "Failed to load rules for language English" when opening the configuration dialog, de-installed LanguageTool 1.2
    and install <a href="http://www.languagetool.org/download/LanguageTool-1.3-beta2.oxt">LanguageTool 1.3 beta2</a> (problem occurred on openSUSE 11.3 with LanguageTool 1.2 pre-installed)
    <!-- 2011-03-18 --></li>
<li style="margin-top:8px">If these hints don't help, please email <strong>naber at danielnaber de</strong> describing the problem
	and letting me know which version of LanguageTool, LibreOffice/OpenOffice.org and which operating system you are using.</li>
</ul>

<p><strong>Known bugs:</strong> Please also see the <?=show_link("README", "download/README.txt", 0)?> 
for a list of known problems.</p>

<h2>License &amp; Source Code</h2>

<p>LanguageTool is freely available under the <?=show_link("LGPL", "http://www.fsf.org/licensing/licenses/lgpl.html#SEC1", 0)?>.
The source is available <?=show_link("at Sourceforge", "http://sourceforge.net/projects/languagetool/", 1) ?> via CVS</p>

<div style="height:750px"></div>

<?php
include("../include/footer.php");
?>
