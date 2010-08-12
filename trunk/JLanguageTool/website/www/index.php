<?php
$page = "homepage";
$title = "LanguageTool";
$title2 = "Open Source language checker";
$lastmod = "2010-06-18 20:10:00 CET";
include("../include/header.php");
?>
		
<p class="firstpara"><strong>An Open Source language checker for English, French, German, Polish, Dutch, Romanian, and
<?=show_link("other languages", "languages/", 0) ?>.</strong>
This is a rule-based language checker that will find errors for which a rule is defined in its 
XML configuration files. Rules for more complicated errors can be  written in Java. You can think 
of LanguageTool as a tool to detect errors that a simple spell checker cannot detect, e.g. mixing
up <em>there/their</em>, <em>no/now</em> etc. It can also detect some
grammar mistakes. It does not include spell checking.</p>


<h2>Download</h2>
<div class="downloadSection">
	<p class="warning">
	If you're upgrading from LanguageTool 0.9.5, you must de-install
	it <strong>before</strong> upgrading to a later version (check
	<a href="http://languagetool.wikidot.com/removing-languagetool-0-9-5-from-openoffice-3-0-1">this
	page</a> if you forgot to do so).
	</p>
	<h2><?=show_link("Download LanguageTool 1.0.0 (17&nbsp;MB)", "download/LanguageTool-1.0.0.oxt", 0) ?></h2>
	<p>Requires <?=show_link("Java", "http://www.java.com/en/download/manual.jsp", 1)?>&nbsp;5.0
	or later (Java 6.0 if you want to compile the code yourself). This version only works with OpenOffice.org 3.0.1 or later 
	and you need to <strong>restart OpenOffice.org</strong> after installation of this extension.</p>
</div>

<!-- -->
<p><strong>Try LanguageTool via Java WebStart:</strong> (requires Java 1.6_04 or later)<br />
<?=show_link("Start LanguageTool (17&nbsp;MB)", "webstart/web/LanguageTool.jnlp", 0) ?></p>
<!-- -->


<h2>News</h2>

<p><strong>2010-02-20:</strong> 

LanguageTool has been integrated into <?=show_link("After the Deadline", "http://open.afterthedeadline.com/", 0)?>,
a powerful English grammar checker. Thanks to LanguageTool it now also  supports French and German. 
This means that you can now use LanguageTool for these languages via the
<?=show_link("After the Deadline Firefox plugin", "http://firefox.afterthedeadline.com/", 0)?>.</p>

<p><strong>2009-11-01:</strong> Released version 1.0.0. Changes include:
<ul>
	<li>Support for Danish, Catalan, and Galician</li>
	<li>Rule and dictionary fixes for Dutch, French, Italian, Polish, Spanish, Swedish, and Russian</li>
	<li>More rules for Dutch, Polish, Russian, Spanish, Slovenian, and English</li>
	<li>Several bug fixes</li>
	<li>For a more detailed list of changes, see the <?=show_link("Changelog", "download/CHANGES.txt", 0) ?>.</li>
</ul>


<h2>Installation and Usage</h2>

<ul>
	<li><strong>In OpenOffice.org</strong>:
	Double click <tt>LanguageTool-1.0.0.oxt</tt> to install it.
	If that doesn't work, call <em>Tools -&gt; Extension Manager -&gt; Add...</em>
	to install it. Close OpenOffice.org and re-start it. Type some text
	with an error (e.g. "This is an test." &ndash; make sure the text language is set
	to English) and you should see a blue underline.</li>

	<li>Also see <?=show_link("Usage", "usage/", 0)?> for using LanguageTool outside of OpenOffice.org.</li>
</ul>

<h2><a name="commonproblems"><strong>Common problems with OpenOffice.org integration</strong></a></h2>

<ul>
<li>Did you restart OpenOffice.org - including the QuickStarter - after installation of LanguageTool? This is required,
	even if OpenOffice.org doesn't say so. (<a href="http://qa.openoffice.org/issues/show_bug.cgi?id=88692">Issue 88692</a>)</li>
<li>LanguageTool installation fails if the name of your user account contains
	special characters. The only workaround so far seems to be to use a different
	user account. (Issue <a href="http://qa.openoffice.org/issues/show_bug.cgi?id=95162">95162</a>)</li>
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
<li>LanguageTool didn't work together with the <a href="http://extensions.services.openoffice.org/en/project/DeltaXMLODTCompare">DeltaXML 
	ODT Compare</a> extension - use version 1.2.0 of DeltaXML ODT Compare, which fixes the problem.</li>
<li style="margin-top:8px">If these hints don't help, please email <strong>naber at danielnaber de</strong> describing the problem
	and letting me know which version of LanguageTool, OpenOffice.org and which operating system you are using.</li>
</ul>

<p><strong>Known bugs:</strong> Please see the <?=show_link("README", "download/README.txt", 0)?> 
for a list of known problems.</p>

<h2>License &amp; Source Code</h2>

<p>LanguageTool is freely available under the <?=show_link("LGPL", "http://www.fsf.org/licensing/licenses/lgpl.html#SEC1", 0)?>.
The source is available <?=show_link("at Sourceforge", "http://sourceforge.net/projects/languagetool/", 1) ?> via CVS</p>

<div style="height:750px"></div>

<?php
include("../include/footer.php");
?>
