<?php
$page = "homepage";
$title = "LanguageTool";
$title2 = "Open Source language checker";
$lastmod = "2008-02-17 19:35:00 CET";
include("../include/header.php");
?>
		
<p class="firstpara"><strong>An Open Source language checker for English, German, Polish, Dutch, and other languages.</strong>
This is a rule-based language checker that will find errors for which a rule is defined in its 
XML configuration files. Rules for more complicated errors can be  written in Java. You can think 
of LanguageTool as a tool to detect errors that a simple spell checker cannot detect, e.g. mixing
up <em>there/their</em>, <em>no/now</em> etc. It can also detect some
grammar mistakes. It does not include spell checking. See the <?=show_link("languages", "languages/", 0) ?> 
page for a list of supported languages.</p>

<p><strong>Update June 2008:</strong> Please visit our new
<?=show_link("LanguageTool Community website", "http://community.languagetool.org") ?>
 that lets you browse all rules for all languages. You can even create 
new (simple) rules if you're logged in.</p>

<p><strong>Update 2008-02-17:</strong> Released version 0.9.2:
In OpenOffice.org, LanguageTool is now part of the "Tools" menu. It is now
distributed as an *.oxt file so you can install it by double-clicking on it.
Added preliminary support for Swedish. For more changes, see
the <?=show_link("Changelog", "download/CHANGES.txt", 0) ?> for details.</p>


<p><strong>Download:</strong><br />
<strong><?=show_link("LanguageTool 0.9.2", "download/LanguageTool-0.9.2.oxt", 0) ?></strong>, 9&nbsp;MB,
requires <?=show_link("Java", "http://www.java.com/en/download/manual.jsp", 1)?>&nbsp;5.0
or later</p>

<!-- -->
<p><strong>Try LanguageTool via Java WebStart:</strong><br />
<?=show_link("Start LanguageTool (9&nbsp;MB)", "webstart/LanguageTool.jnlp", 0) ?></p>
<!-- -->

<p><strong>Installation and Usage:</strong>

<ul>
	<li><strong>In OpenOffice.org</strong>:
	Double click <tt>LanguageTool-0.9.2.oxt</tt> to install it.
	If that doesn't work, call <em>Tools -&gt; Extension Manager -&gt; Add...</em>
	to install it. Open a new window of OpenOffice.org (Ctrl-N)
	and you'll see a new menu entry "LanguageTool" in the "Tools" menu.</li>

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
