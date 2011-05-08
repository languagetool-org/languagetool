<?php
$page = "other";
$title = "LanguageTool";
$title2 = "Google Summer of Code 2011";
$lastmod = "2011-05-08 12:30:00 CET";
include("../../include/header.php");
?>
		
<p class="firstpara">LanguageTool participates in Google's Summer of Code 2011 (GSoC2011). What is it?
Quoting the <?=show_link("GSoC Homepage", "http://code.google.com/soc/", 0) ?>, "Google Summer 
of Code is a global program that offers student developers stipends to write code for various open source software projects." </p>

<p>Two LanguageTool projects have been accepted for this year:</p>

<ul>
    <li><?=show_link("Lucene Based Fast Rule Evaluation for LanguageTool with Chinese Language Support", "http://www.google-melange.com/gsoc/project/google/gsoc2011/taolin/8001", 0) ?> (Tao Lin)</li>
    <li><?=show_link("Adding Rule Conversion and Language Detection Functionality to Language Tool", "http://www.google-melange.com/gsoc/proposal/review/google/gsoc2011/mbryant/5001", 0) ?> (Michael Bryant)</li>
</ul>

<p>Other links:</p>

<ul>
    <li><?=show_link("LanguageTool Project ideas", "http://languagetool.wikidot.com/missing-features", 0) ?></li>
    <li><?=show_link("Google Summer of Code 2011 Homepage", "http://code.google.com/soc/", 0) ?></li>
    <li><?=show_link("Timeline", "http://www.google-melange.com/document/show/gsoc_program/google/gsoc2011/timeline", 0) ?></li>
</ul>

<?php
include("../../include/footer.php");
?>
