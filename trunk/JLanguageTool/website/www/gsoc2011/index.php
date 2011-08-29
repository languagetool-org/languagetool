<?php
$page = "other";
$title = "LanguageTool";
$title2 = "Google Summer of Code 2011";
$lastmod = "2011-08-28 12:30:00 CET";
include("../../include/header.php");
?>
		
<p class="firstpara">LanguageTool participated in Google's Summer of Code 2011 (GSoC2011). What is it?
Quoting the <?=show_link("GSoC Homepage", "http://code.google.com/soc/", 0) ?>, "Google Summer 
of Code is a global program that offers student developers stipends to write code for various open source software projects." </p>

<p>Both LanguageTool projects have been successfully completed this year:</p>

<p><b>Michael Bryant's</b> project was to add language identification and enable us to reuse
linguistic resources from other projects. These were the rules
included in the <?=show_link("After the Deadline grammar checker",
"http://www.afterthedeadline.com/", 0)?> (some of them will
be included in version 1.5 after some additional checking)
and conversion of <?=show_link("Constraint Grammar", "http://en.wikipedia.org/wiki/Constraint_Grammar", 0) ?> (CG) rules into the format of
disambiguation rules. CG is widely used for Scandinavian languages and
we hope that adding an easy option to convert them will enable further
steps to add deeper linguistic analysis or parsing to LanguageTool without
making it too heavy on resources. As far as we know, Michael's
conversion of CG rules is the first open-source Java implementation of
CG. It is also a practical proof that our disambiguation rules have
similar expressive power as CG.</p>

<p><b>Tao Lin's</b> project was twofold: the first part was to develop a Lucene-based indexing
tool that makes it possible to run a rule against a large amount of text. Usually checking
large texts needs a lot of time, but thanks to this tool, the rule can be tested within seconds.
The other part of Tao's project was to add support for Chinese to LanguageTool. The upcoming
version 1.5 of LanguageTool will thus contain more than 200 rules for Chinese text.</p>

<h3>Documentation by the GSoC participants</h3>

<ul>
    <li><?=show_link("How to Use Indexer and Searcher for Fast Rule Evaluation", "http://languagetool.wikidot.com/how-to-use-indexer-and-searcher-for-fast-rule-evaluation", 0) ?> (Tao Lin)</li>
    <li><?=show_link("Developing Chinese rules", "http://languagetool.wikidot.com/developing-chinese-rules", 0) ?> (Tao Lin)</li>
    <li><?=show_link("Adding A New Language To Automatic Language Detection", "http://languagetool.wikidot.com/adding-a-new-language-to-automatic-language-detection", 0) ?> (Michael Bryant)</li>
</ul>

<h3>Other links</h3>

<ul>
    <li><?=show_link("LanguageTool Project ideas", "http://languagetool.wikidot.com/missing-features", 0) ?></li>
    <li><?=show_link("Google Summer of Code 2011 Homepage", "http://code.google.com/soc/", 0) ?></li>
    <li><?=show_link("Google Summer of Code 2011 Timeline", "http://www.google-melange.com/document/show/gsoc_program/google/gsoc2011/timeline", 0) ?></li>
</ul>

<?php
include("../../include/footer.php");
?>
