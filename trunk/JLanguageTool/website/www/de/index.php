<?php
$page = "de";
$title = "LanguageTool";
$title2 = "Stil- und Grammatikprüfung";
$lastmod = "2012-01-15 23:05:00 CET";
include("../../include/header.php");
?>

<p>LanguageTool ist eine Open-Source Stil- und Grammatikprüfung.

<h2>Features</h2>

<ul>
    <li>Prüfung auf viele häufige Fehler, z.B.
        <span class="errorMarker" title="Meinten Sie E-Mail (elektronische Post) statt 'Email' (Schmelzüberzug)?">schick mir eine Email</span>
        (mit der Maus über dem Fehler wird die Fehlermeldung angezeigt)
    </li>
    <li>Prüfung einiger Grammatikphänomene, z.B. Übereinstimmung zwischen Artikel und Nomen bzw.
        Artikel, Adjektiv und Nomen:
        <span class="errorMarker" title="Möglicherweise fehlende grammatische Übereinstimmung zwischen Artikel und Nomen bezüglich Genus">der Haus</span>, 
        <span class="errorMarker" title="Möglicherweise fehlende grammatische Übereinstimmung zwischen Artikel, Adjektiv und Nomen bezüglich Kasus, Numerus oder Genus">das schöner Haus</span> 
    </li>
    <li>Prüfung der Großschreibung am Satzanfang</li>
    <li>Prüfung vieler Komposita auf Zusammenschreibung: <span class="errorMarker" title="Dieses Kompositum wird mit Bindestrich geschrieben">DLG prämierter</span></li>
    <li>Aber: LanguageTool hat <strong>keine Rechtschreibprüfung</strong></li>
</ul>

Die über 600 deutschen Fehler-Regeln können auf <a href="http://community.languagetool.org/rule/list?lang=de">der Community-Website</a>
angeschaut werden.


<h2>Download</h2>

<p>LanguageTool kann man auf der <a href="../">Homepage</a> herunterladen.</p>

<h2>Kontakt</h2>
<!-- TODO: direkter Kontakt? -->

<p>Fragen beantworten wir im <a href="../forum">Forum</a> - dort kann auch gerne auf Deutsch gepostet werden.</p>


<?php
include("../../include/footer.php");
?>
