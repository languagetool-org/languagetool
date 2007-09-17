<?php
$page = "homepage";
$title = "LanguageTool";
$title2 = "Open Source language checker";
$lastmod = "2007-06-05 20:35:00 CET";
include("../include/header.php");
?>
		
<p class="firstpara"><strong>An Open Source language checker for English, German, Polish, Dutch, and other languages.</strong>
This is a rule-based language checker that will find errors for which a rule is defined in its 
XML configuration files. Rules for more complicated errors can be  written in Java. You can think 
of LanguageTool as a tool to detect errors that a simple spell checker cannot detect, e.g. mixing
up <em>there/their</em>, <em>no/now</em> etc. It can also detect some
grammar mistakes. It does not include spell checking. See the <?=show_link("languages", "languages/", 0) ?> 
page for a list of supported languages.</p>

<p><strong>Update 2007-09-17:</strong> Released version 0.9.1:
Works with OpenOffice.org 2.3, improved rules (mostly Polish and English),
bug fixes. See the <?=show_link("Changelog", "download/CHANGES.txt", 0) ?> for details.</p>

<p><strong>Download:</strong><br />
<strong><?=show_link("LanguageTool 0.9.1", "download/LanguageTool-0.9.1.zip", 0) ?></strong>, 9&nbsp;MB,
requires <?=show_link("Java", "http://www.java.com/en/download/manual.jsp", 1)?>&nbsp;5.0 (also called 1.5) or later<br />
<!-- 
<?=show_link("LanguageTool 0.9 with source code", "download/LanguageTool-0.9-src.zip", 0) ?>, 8.5&nbsp;MB<br />
 -->
<?=show_link("Project page at Sourceforge", "http://sourceforge.net/projects/languagetool/", 1) ?>
 (includes latest version in CVS)<br />
</p>

<!-- -->
<p><strong>Try LanguageTool via Java WebStart:</strong><br />
<?=show_link("Start LanguageTool (9&nbsp;MB)", "webstart/LanguageTool.jnlp", 0) ?></p>
<!-- -->
    
<p><strong>Installation and Usage:</strong>

<ul>
	<li><strong>In OpenOffice.org</strong>:
	Do <em>not</em> unzip the archive, just call <em>Tools -&gt; Extension Manager -&gt; Add...</em>
	to install <tt>LanguageTool-0.9.1.zip</tt> (note that the menu item is called <em>Package Manager</em>
	in OpenOffice.org 2.0.x). Open a new window of OpenOffice.org (Ctrl-N)
	and you'll see a new menu entry "LanguageTool" that will check the current text.</li>

	<li>Also see <?=show_link("Usage", "usage/", 0)?> for using LanguageTool outside of OpenOffice.org.</li>
</ul>

<p><strong>If installation fails:</strong>
Make sure <a href="http://www.java.com/en/download/manual.jsp">Java 5.0</a>
or later is installed on your system. Then make sure this version of Java is configured in OpenOffice.org
(under <em>Tools -&gt; Options -&gt; Java</em>).</p>


<p><strong>Known bugs:</strong> Please see the <?=show_link("README", "download/README.txt", 0)?> 
for a list of known problems.</p>

<p><strong>License:</strong> LanguageTool is freely available under the 
<?=show_link("LGPL", "http://www.fsf.org/licensing/licenses/lgpl.html#SEC1", 0)?>.</p>

<?php
include("../include/footer.php");
?>
