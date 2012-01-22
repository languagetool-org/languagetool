<?php
$page = "hejmpaĝo";
$title = "Lingvoilo";
$title2 = "Stila kaj gramatika kontrolilo";
$lastmod = "2012-01-22 16:08 CET";
include("../../include/header.php");
?>
		
<p class="firstpara"><strong>Lingvoilo (LanguageTool) estas libera plurlingva 
programo por kontroli stilon kaj gramatikon en Esperanto sed ankaŭ en 
<?=show_link("multaj aliaj lingvoj", "../languages/", 0) ?>.</strong>
Lingvoilo atentigas pri tiuj eraroj, kiujn literuma kontrolilo ne trovas, 
kiel eraroj de akuzativo (mi lernas <em>Esperanto</em>). Ĝi ne atentigas 
pri misliterumoj, sed eblas uzi Lingvoilon samtempe kune kun literuma 
kontrolilo.</p>

<h2>Provu Lingvoilo-n rete sen instali ĝin</h2>

<form name="checkform" action="http://community.languagetool.org" method="post">
    <textarea onfocus="javascript: if(document.checkform.text.value == 'Alglui vian kontrolendan tekston ĉi tie...') { document.checkform.text.value='' } " 
        style="width:100%; max-width:800px;height:100px" name="text">Alglui vian kontrolendan tekston ĉi tie...</textarea>
    <br />
    <input type="submit" name="_action_checkText" value="Kontroli tekston"/>
    Lingvo: <select name="lang" id="lang" >
    
        <option value="auto">aŭtomate detekti</option>
        <option value="en" >angla</option>
        <option value="ast" >asturia</option>
        <option value="be" >belarusa</option>
        <option value="br" >bretona</option>
        <option value="zh" >ĉina</option>
        <option value="da" >dana</option>
        <option selected value="eo" >Esperanto</option>
        <option value="fr" >franca</option>
        <option value="gl" >galega</option>
        <option value="de" >germana</option>
        <option value="es" >hispana</option>
        <option value="is" >islanda</option>
        <option value="it" >itala</option>
        <option value="ca" >kataluna</option>
        <option value="km" >kmera</option>
        <option value="lt" >litova</option>
        <option value="ml" >malajala</option>
        <option value="nl" >nederlanda</option>
        <option value="pl" >pola</option>
        <option value="ro" >rumana</option>
        <option value="ru" >rusa</option>
        <option value="sk" >slovaka</option>
        <option value="sl" >slovena</option>
        <option value="sv" >sveda</option>
        <option value="tl" >togaloga</option>
        <option value="uk" >ukraina</option>
    </select>
</form>

<h2>Elŝutado</h2>

<ul>
<li><?=show_link("Elŝuti Lingvoilo-1.6 (29&nbsp;MB)", "../download/LanguageTool-1.6.oxt", 0) ?>.</li>
<li>Pli freŝaj sed ne testitaj versioj de Lingvoilo ĝisdatigitaj ĉiutage
<?=show_link("haveblas tie", "../download/snapshots/", 0) ?>
(<?=show_link("CHANGES.txt", "http://languagetool.svn.sourceforge.net/viewvc/languagetool/trunk/JLanguageTool/CHANGES.txt", 0) ?>).</li>
</ul>

<h2>Instalo kaj uzado</h2>

<ul>
  <li><strong>En OpenOffice.org/LibreOffice</strong>:
  Duoble klaku sur <tt>LanguageTool-1.6.oxt</tt> por instali ĝin.
  Se tio ne funkcias, alvoku <em>Iloj → Aldonaĵa mastrumilo</em> ekde
  la ĉefa menuo de LibreOffice, kaj poste restartigu LibreOffice.
  </p>
  <li><strong>En komanda linio</strong>: 
<pre>  $ unzip <a href="../download/LanguageTool-1.6.oxt">LanguageTool-1.6.oxt</a>
  $ echo "Tio frazo enhavas eraron." | java -jar LanguageTool.jar -c utf-8 -l eo
  Expected text language: Esperanto
  Working on STDIN...
  1.) Line 1, column 1, Rule ID: IO_O[1]
  Message: Ne povas esti «Tio» antaŭ o-vorto «frazo». Ĉu vi intencis «Tiu frazo»?
  Tio frazo enhavas eraron. 
  ^^^^^^^^^                 
</pre>
  <li>Eblas ankaŭ uzi Lingvoilon <a href="http://www.vim.org/scripts/script.php?script_id=3223">en Vim</a>, <a href="http://wiki.lyx.org/Tools/LyX-GrammarChecker">en Lyx</a>, <a href="http://sourceforge.net/projects/omegat-plugins/files/OmegaT-LanguageTool/">en OmegaT</a>, <a href="http://www.opentag.com/okapi/wiki/index.php?title=CheckMate">en CheckMate</a> kaj <href="https://addons.mozilla.org/en-US/thunderbird/addon/grammar-checker/">en Thundirbird</a>.</li>
  </li>
  <li>Legu ankaŭ <?=show_link("Usage", "../usage/", 0)?>.</li>
</ul>

<h2>Ekrankopio en LibreOffice</h2>

<img src="images/Lingvoilo-LibreOffice.png" alt="Lingvoilo"/>

<h2>Ĉu vi bezonas helpon?</h2>

<p>Bonvolu vidi <?=show_link("liston de la plej oftaj problemoj", "../issues", 0)?>.</p>
Por plia helpo, vi povas demandi aŭ en <a href="http://www.languagetool.org/forum/">la
forumo</a>, aŭ en la <a href="https://lists.sourceforge.net/lists/listinfo/languagetool-devel">retpoŝta
dissendlisto/a> aŭ retpoŝti al la
<a href="mailto:dominique.pelle@gmail.com">verkisto de la Esperanta versio</a>.

<h2>Ligiloj al aliaj Esperantaj kontroliloj</h2>

Lingvoilo ne estas la nura Esperanta gramatika kontrolilo. Vidu ankaŭ:

<ul>
  <li><a href="http://lingvohelpilo.ikso.net/">Lingvohelpilo</a></li>
  <li><a href="http://www.esperantilo.org/index.html">Esperantilo</a></li>
</ul>

<h2>Permesilo kaj kodofonto</h2>

<p>Lingvoilo libere haveblas sub la permesilo <?=show_link("LGPL", "http://www.fsf.org/licensing/licenses/lgpl.html#SEC1", 0)?>.
Kodofonto elŝuteblas ĉe <?=show_link("Sourceforge", "http://sourceforge.net/projects/languagetool/", 1) ?> per SVN.
La enhavo de la hejmpaĝo haveblas sub la permesilo <?=show_link("CC BY-SA 3.0", "http://creativecommons.org/licenses/by-sa/3.0/", 1) ?>.</p>

<div style="height:50px"></div>

<?php
include("../../include/footer.php");
?>
