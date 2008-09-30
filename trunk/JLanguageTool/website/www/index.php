<?php
$page = "homepage";
$title = "LanguageTool";
$title2 = "Open Source language checker";
$lastmod = "2008-09-30 23:35:00 CET";
include("../include/header.php");
?>
		
<p class="firstpara"><strong>An Open Source language checker for English, German, Polish, Dutch, and other languages.</strong>
This is a rule-based language checker that will find errors for which a rule is defined in its 
XML configuration files. Rules for more complicated errors can be  written in Java. You can think 
of LanguageTool as a tool to detect errors that a simple spell checker cannot detect, e.g. mixing
up <em>there/their</em>, <em>no/now</em> etc. It can also detect some
grammar mistakes. It does not include spell checking. See the <?=show_link("languages", "languages/", 0) ?> 
page for a list of supported languages.</p>

<p><strong>Update 2008-09-30:</strong> Released version 0.9.4:
<ul>
	<li>Fixed a bug with <a href="http://www.openoffice.org">OpenOffice.org</a> 3.0 integration. <strong>NOTE</strong>: you
		need OpenOffice.org 3.0rc3 or later, earlier versions have bugs that make them crash
		with LanguageTool</li>
	<li>Updated rules for French, Swedish, and Russian</li>
	<li>Moved LanguageTool button in Openoffice.org next to
	the "Spelling and Grammar" button</li>
</ul>

<p>For a slightly more detailed list of changes, see
the <?=show_link("Changelog", "download/CHANGES.txt", 0) ?> for details.</p>

<p><strong>Update June 2008:</strong> Please visit our new
<?=show_link("LanguageTool Community website", "http://community.languagetool.org", 0) ?>
 that lets you browse all rules for all languages. You can even create 
new (simple) rules if you're logged in.</p>

<p><strong>Download for OpenOffice.org 3.0 (rc3 or later only):</strong><br />
<strong><?=show_link("LanguageTool 0.9.4 (for OpenOffice.org 3.0)", "download/LanguageTool-0.9.4.oxt", 0) ?></strong>, 10&nbsp;MB,
requires <?=show_link("Java", "http://www.java.com/en/download/manual.jsp", 1)?>&nbsp;5.0
or later. NOTE: this version currently <strong>only</strong> works with OpenOffice.org 3.0rc3 or later</p>

<p><strong>Download for OpenOffice.org 2.x:</strong><br />
<strong><?=show_link("LanguageTool 0.9.2 (for OpenOffice.org 2.x)", "download/LanguageTool-0.9.2.oxt", 0) ?></strong>, 9&nbsp;MB,
requires <?=show_link("Java", "http://www.java.com/en/download/manual.jsp", 1)?>&nbsp;5.0
or later -- NOTE: this version doesn't work with OpenOffice.org 3.0 beta</p>

<!-- -->
<p><strong>Try LanguageTool via Java WebStart:</strong><br />
<?=show_link("Start LanguageTool (10&nbsp;MB)", "webstart/LanguageTool.jnlp", 0) ?></p>
<!-- -->

<p><strong>Installation and Usage:</strong>

<ul>
	<li><strong>In OpenOffice.org</strong>:
	Double click <tt>LanguageTool-0.9.4.oxt</tt> to install it.
	If that doesn't work, call <em>Tools -&gt; Extension Manager -&gt; Add...</em>
	to install it. Close OpenOffice.org and re-start it. Type some text
	with an error (e.g. "This is an test." -- make sure the text language is set
	to English) and you should see a blue underline (version 0.9.4 only).
	For version 0.9.2, you'll need to use the "LanguageTool" entries in the "Tools"
	menu to check a text.</li>

	<li>Also see <?=show_link("Usage", "usage/", 0)?> for using LanguageTool outside of OpenOffice.org.</li>
</ul>

<p><strong>If installation fails:</strong>
Make sure <a href="http://www.java.com/en/download/manual.jsp">Java 5.0 or later from Sun Microsystems</a>
is installed on your system. Java versions which are not from Sun Microsystems may not work.
Then make sure this version of Java is selected in OpenOffice.org
(under <em>Tools -&gt; Options -&gt; Java</em>). If LanguageTool doesn't start after
selecting the version of Java in OpenOffice.org and you see no error message,
check if the extension is enabled in the Extension manager 
(under <em>Tools -&gt; Extension Manager</em>).</p>

<p>The source is available <?=show_link("at Sourceforge", "http://sourceforge.net/projects/languagetool/", 1) ?>
 via CVS</p>

<p><strong>Known bugs:</strong> Please see the <?=show_link("README", "download/README.txt", 0)?> 
for a list of known problems.</p>

<p><strong>License:</strong> LanguageTool is freely available under the 
<?=show_link("LGPL", "http://www.fsf.org/licensing/licenses/lgpl.html#SEC1", 0)?>.</p>

<?php
include("../include/footer.php");
?>
