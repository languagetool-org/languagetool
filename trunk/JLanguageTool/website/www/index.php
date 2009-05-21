<?php
$page = "homepage";
$title = "LanguageTool";
$title2 = "Open Source language checker";
$lastmod = "2009-05-22 00:50:00 CET";
include("../include/header.php");
?>
		
<p class="firstpara"><strong>An Open Source language checker for English, German, Polish, Dutch, and other languages.</strong>
This is a rule-based language checker that will find errors for which a rule is defined in its 
XML configuration files. Rules for more complicated errors can be  written in Java. You can think 
of LanguageTool as a tool to detect errors that a simple spell checker cannot detect, e.g. mixing
up <em>there/their</em>, <em>no/now</em> etc. It can also detect some
grammar mistakes. It does not include spell checking. See the <?=show_link("languages", "languages/", 0) ?> 
page for a list of supported languages.</p>

<p><strong>Update 2009-05-22:</strong> Released version 0.9.9. Changes include:
<ul>
	<li>Fixed a NullPointerException that could appear when using
  paragraph-level rules (<a href="https://sourceforge.net/tracker/?func=detail&aid=2787814&group_id=110216&atid=655717">#2787814</a>)</li>
	<li>Initial support for Icelandic</li>
	<li>More Dutch, English, Polish, Romanian and Slovenian grammar rules</li>
	<li>Fixed some bugs with pairing brackets</li>
	<li>Added sentence tokenizer that uses <a href="http://www.lisa.org/fileadmin/standards/srx20.html">SRX</a> format for 
  specifying end-of-sentence breaks</li>
</ul>

<p>For a more detailed list of changes, see
the <?=show_link("Changelog", "download/CHANGES.txt", 0) ?>.</p>

<div class="outerDownloadSection">
<strong>Download for OpenOffice.org 3.0.1 and 3.1:</strong><br />
<div class="downloadSection">
	<p class="warning">
	This version works only with OpenOffice.org 3.0.1 and 3.1, NOT with 3.0.0.
	Also, you must de-install
	LanguageTool 0.9.5 <strong>before</strong> upgrading to OpenOffice.org 3.0.1 because of compatibility
	issues (check <a href="http://languagetool.wikidot.com/removing-languagetool-0-9-5-from-openoffice-3-0-1">this
	page</a> if you forgot to do so).
	</p>
	<p><strong><?=show_link("LanguageTool 0.9.9 (for OpenOffice.org 3.0.1 and 3.1)", "download/LanguageTool-0.9.9.oxt", 0) ?></strong>, 13&nbsp;MB,
	requires <?=show_link("Java", "http://www.java.com/en/download/manual.jsp", 1)?>&nbsp;5.0
	or later. NOTE: this version <strong>only works with OpenOffice.org 3.0.1 or later</strong> 
	and you need to <strong>restart OpenOffice.org</strong> after installation of this extension</p>
</div>
</div>

<div class="outerDownloadSection">
<strong>Download for OpenOffice.org 3.0.0:</strong><br />
<div class="downloadSection">
	<p class="warning">
	This version works only with OpenOffice.org 3.0.0, NOT with 3.0.1 or its beta releases.
	Also, you must de-install
	LanguageTool 0.9.5 <strong>before</strong> upgrading to OpenOffice.org 3.0.1 because of compatibility
	issues (check <a href="http://languagetool.wikidot.com/removing-languagetool-0-9-5-from-openoffice-3-0-1">this
	page</a> if you forgot to do so).
	</p>
	<p><strong><?=show_link("LanguageTool 0.9.5 (for OpenOffice.org 3.0.0)", "download/LanguageTool-0.9.5.oxt", 0) ?></strong>, 10&nbsp;MB,
	requires <?=show_link("Java", "http://www.java.com/en/download/manual.jsp", 1)?>&nbsp;5.0
	or later. NOTE: this version currently <strong>only works with OpenOffice.org 3.0.0</strong> 
	and you need to <strong>restart OpenOffice.org</strong> after installation of this extension</p>
</div>
</div>

<!-- 
<div class="outerDownloadSection">
<strong>Download for OpenOffice.org 2.x:</strong><br />
<div class="downloadSection">
	<p><strong><?=show_link("LanguageTool 0.9.2 (for OpenOffice.org 2.x)", "download/LanguageTool-0.9.2.oxt", 0) ?></strong>, 9&nbsp;MB,
	requires <?=show_link("Java", "http://www.java.com/en/download/manual.jsp", 1)?>&nbsp;5.0
	or later. NOTE: this version does not work with OpenOffice.org 3.0</p>
</div>
</div>
 -->
 
<!-- -->
<p><strong>Try LanguageTool via Java WebStart:</strong><br />
<?=show_link("Start LanguageTool (13&nbsp;MB)", "webstart/LanguageTool.jnlp", 0) ?></p>
<!-- -->

<p><strong>Installation and Usage:</strong>

<ul>
	<li><strong>In OpenOffice.org</strong>:
	Double click <tt>LanguageTool-0.9.9.oxt</tt> to install it.
	If that doesn't work, call <em>Tools -&gt; Extension Manager -&gt; Add...</em>
	to install it. Close OpenOffice.org and re-start it. Type some text
	with an error (e.g. "This is an test." -- make sure the text language is set
	to English) and you should see a blue underline (version 0.9.5 or later only).
	For version 0.9.2, you'll need to use the "LanguageTool" entries in the "Tools"
	menu to check a text.</li>

	<li>Also see <?=show_link("Usage", "usage/", 0)?> for using LanguageTool outside of OpenOffice.org.</li>
</ul>

<p><strong>If integration into OpenOffice.org doesn't work:</strong></p>

<ul>
<li>LanguageTool installation fails if the name of your user account contains
	special characters. The only workaround so far seems to be to use a different
	user account. (Issue <a href="http://qa.openoffice.org/issues/show_bug.cgi?id=95162">95162</a>)</li>
<li>Did you restart OpenOffice.org - including the QuickStarter - after installation of LanguageTool? This is required,
	even if OpenOffice.org doesn't say so. (<a href="http://qa.openoffice.org/issues/show_bug.cgi?id=88692">Issue 88692</a>)</li>
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
</ul>

<p>The source is available <?=show_link("at Sourceforge", "http://sourceforge.net/projects/languagetool/", 1) ?>
 via CVS</p>

<p><strong>Known bugs:</strong> Please see the <?=show_link("README", "download/README.txt", 0)?> 
for a list of known problems.</p>

<p><strong>License:</strong> LanguageTool is freely available under the 
<?=show_link("LGPL", "http://www.fsf.org/licensing/licenses/lgpl.html#SEC1", 0)?>.</p>

<?php
include("../include/footer.php");
?>
