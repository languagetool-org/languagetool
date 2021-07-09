/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.rules.de;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class AgreementRuleTest {

  private AgreementRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() {
    rule = new AgreementRule(TestTools.getMessages("de"), (GermanyGerman)Languages.getLanguageForShortCode("de-DE"));
    lt = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));
  }

  @Test
  public void testCompoundMatch() throws IOException {
    assertBad("Das ist die Original Mail", "die Originalmail", "die Original-Mail");
    assertBad("Das ist die neue Original Mail", "die neue Originalmail", "die neue Original-Mail");
    assertBad("Das ist die ganz neue Original Mail", "die ganz neue Originalmail", "die ganz neue Original-Mail");
    assertBad("Doch dieser kleine Magnesium Anteil ist entscheidend.", "dieser kleine Magnesiumanteil", "dieser kleine Magnesium-Anteil");
    assertBad("Doch dieser sehr kleine Magnesium Anteil ist entscheidend.", "dieser sehr kleine Magnesiumanteil", "dieser sehr kleine Magnesium-Anteil");
    assertBad("Die Standard Priorität ist 5.", "Die Standardpriorität", "Die Standard-Priorität");
    assertBad("Die derzeitige Standard Priorität ist 5.", "Die derzeitige Standardpriorität", "Die derzeitige Standard-Priorität");
    assertBad("Ein neuer LanguageTool Account", "Ein neuer LanguageTool-Account");
    assertBad("Danke für deine Account Daten", "deine Accountdaten", "deine Account-Daten");
    assertBad("Mit seinem Konkurrent Alistair Müller", "seinem Konkurrenten");
    assertBad("Wir gehen ins Fitness Studio", "ins Fitnessstudio", "ins Fitness-Studio");
    assertBad("Wir gehen durchs Fitness Studio", "durchs Fitnessstudio", "durchs Fitness-Studio");
    assertGood("Es gibt ein Sprichwort, dem zufolge der tägliche Genuss einer Mandel dem Gedächtnis förderlich sei.");
    assertGood("War das Eifersucht?");
    //assertBad("Die Bad Taste Party von Susi", "Die Bad-Taste-Party");   // not supported yet
    //assertBad("Die Update Liste.", "Die Updateliste");  // not accepted by speller
    List<RuleMatch> matches = lt.check("Er folgt damit dem Tipp des Autoren Michael Müller.");
    assertThat(matches.size(), is(1));
    assertFalse(matches.get(0).getMessage().contains("zusammengesetztes Nomen"));
  }
  
  @Test
  public void testDetNounRule() throws IOException {
    // correct sentences:
    assertGood("Die Einen sagen dies, die Anderen das.");
    assertGood("So ist es in den USA.");
    assertGood("Das ist der Tisch.");
    assertGood("Das ist das Haus.");
    assertGood("Das ist die Frau.");
    assertGood("Das ist das Auto der Frau.");
    assertGood("Das gehört dem Mann.");
    assertGood("Das Auto des Mannes.");
    assertGood("Das interessiert den Mann.");
    assertGood("Das interessiert die Männer.");
    assertGood("Das Auto von einem Mann.");
    assertGood("Das Auto eines Mannes.");
    assertGood("Des großen Mannes.");
    assertGood("Und nach der Nummerierung kommt die Überschrift.");
    assertGood("Sie wiesen dieselben Verzierungen auf.");
    assertGood("Die erwähnte Konferenz ist am Samstag.");
    assertGood("Sie erreichten 5 Prozent.");
    assertGood("Sie erreichten mehrere Prozent Zustimmung.");
    assertGood("Die Bestandteile, aus denen Schwefel besteht.");
    assertGood("Ich tat für ihn, was kein anderer Autor für ihn tat.");
    assertGood("Ich tat für ihn, was keine andere Autorin für ihn tat.");
    assertGood("Ich tat für ihn, was kein anderes Kind für ihn tat.");
    assertGood("Ich tat für ihn, was dieser andere Autor für ihn tat.");
    assertGood("Ich tat für ihn, was diese andere Autorin für ihn tat.");
    assertGood("Ich tat für ihn, was dieses andere Kind für ihn tat.");
    assertGood("Ich tat für ihn, was jener andere Autor für ihn tat.");
    assertGood("Ich tat für ihn, was jeder andere Autor für ihn tat.");
    assertGood("Ich tat für ihn, was jede andere Autorin für ihn tat.");
    assertGood("Ich tat für ihn, was jedes andere Kind für ihn tat.");
    assertGood("Klebe ein Preisschild auf jedes einzelne Produkt.");
    assertGood("Eine Stadt, in der zurzeit eine rege Bautätigkeit herrscht.");
    assertGood("... wo es zu einer regen Bautätigkeit kam.");
    assertGood("Mancher ausscheidende Politiker hinterlässt eine Lücke.");
    assertGood("Kern einer jeden Tragödie ist es, ..");
    assertGood("Das wenige Sekunden alte Baby schrie laut.");
    assertGood("Meistens sind das Frauen, die damit besser umgehen können.");
    assertGood("Er fragte, ob das Spaß macht.");
    assertGood("Das viele Geld wird ihr helfen.");
    assertGood("Er verspricht jedem hohe Gewinne.");
    assertGood("Er versprach allen Renditen jenseits von 15 Prozent.");
    assertGood("Sind das Eier aus Bodenhaltung?");
    assertGood("Dir macht doch irgendwas Sorgen.");
    assertGood("Sie fragte, ob das wirklich Kunst sei.");
    assertGood("Für ihn ist das Alltag.");
    assertGood("Für die Religiösen ist das Blasphemie und führt zu Aufständen.");
    assertGood("Das Orange ist schön.");
    assertGood("Dieses rötliche Orange gefällt mir am besten.");
    assertGood("Das ist ein super Tipp.");
    assertGood("Er nahm allen Mut zusammen und ging los.");
    assertGood("Sie kann einem Angst einjagen.");
    assertGood("Damit sollten zum einen neue Energien gefördert werden, zum anderen der Sozialbereich.");
    assertGood("Nichts ist mit dieser einen Nacht zu vergleichen.");
    assertGood("dann muss Schule dem Rechnung tragen.");
    assertGood("Das Dach von meinem Auto.");
    assertGood("Das Dach von meinen Autos.");
    assertGood("Da stellt sich die Frage: Ist das Science-Fiction oder moderne Mobilität?");
    assertGood("Er hat einen Post veröffentlicht.");
    assertGood("Eine lückenlose Aufklärung sämtlicher physiologischer Gehirnprozesse");
    assertGood("Sie fragte verwirrt: „Ist das Zucker?“");
    assertGood("Er versuchte sich vorzustellen, was sein Klient für ein Mensch sei.");
    assertGood("Sie legen ein Teilstück jenes Weges zurück, den die Tausenden Juden 1945 auf sich nehmen mussten.");
    assertGood("Aber das ignorierte Herr Grey bewusst.");
    assertGood("Aber das ignorierte Herr Müller bewusst.");
    assertGood("Ich werde mich zurücknehmen und mich frischen Ideen zuwenden.");
    assertGood("Das, plus ein eigener Firmenwagen.");
    assertGood("Dieses leise Summen stört nicht.");
    assertGood("Die Tiroler Küche");
    assertGood("Was ist denn das für ein ungewöhnlicher Name?");
    assertGood("Besonders reizen mich Fahrräder.");
    assertGood("Und nur, weil mich psychische Erkrankungen aus der Bahn werfen");
    assertGood("Das kostet dich Zinsen.");
    assertGood("Sie hatten keine Chance gegen das kleinere Preußen.");
    assertGood("Den 2019er Wert hatten sie geschätzt.");
    assertGood("Andere formale Systeme, deren Semantiken jeweils...");
    assertGood("Gesetz zur Änderung des Kündigungsrechts und anderer arbeitsrechtlicher Vorschriften");
    assertGood("Die dauerhafte Abgrenzung des später Niedersachsen genannten Gebietes von Westfalen begann im 12. Jahrhundert.");
    assertGood("Lieber jemanden, der einem Tipps gibt.");
    assertGood("Jainas ist sogar der Genuss jeglicher tierischer Nahrungsmittel strengstens untersagt.");
    assertGood("Es sind jegliche tierische Nahrungsmittel untersagt.");
    assertGood("Das reicht bis weit ins heutige Hessen.");
    assertGood("Die Customer Journey.");
    assertGood("Für dich gehört Radfahren zum perfekten Urlaub dazu?");
    assertGood(":D:D Leute, bitte!");
    assertGood("Es genügt, wenn ein Mann sein eigenes Geschäft versteht und sich nicht in das anderer Leute einmischt.");
    assertGood("Ich habe das einige Male versucht.");
    assertGood("Und keine Märchen erzählst, die dem anderen Hoffnungen machen können.");
    assertGood("Um diese Körpergrößen zu erreichen, war das Wachstum der Vertreter der Gattung Dinornis offenbar gegenüber dem anderer Moa-Gattungen beschleunigt");
    assertGood("Der Schädel entspricht in den Proportionen dem anderer Vulpes-Arten, besitzt aber sehr große Paukenhöhlen, ein typisches Merkmal von Wüstenbewohnern.");
    assertGood("Deuterium lässt sich aufgrund des großen Massenunterschieds leichter anreichern als die Isotope der anderer Elemente wie z. B. Uran.");
    assertGood("Unklar ist, ob er zwischen der Atemseele des Menschen und der anderer Lebewesen unterschied.");
    assertGood("Die Liechtensteiner Grenze ist im Verhältnis zu der anderer Länder kurz, da Liechtenstein ein eher kleines Land ist.");
    assertGood("Picassos Kunstwerke werden häufiger gestohlen als die anderer Künstler.");
    assertGood("Schreibe einen Artikel über deine Erfahrungen im Ausland oder die anderer Leute in deinem Land.");
    assertGood("Die Bevölkerungen Chinas und Indiens lassen die anderer Staaten als Zwerge erscheinen.");
    assertGood("Der eine mag Obst, ein anderer Gemüse, wieder ein anderer mag Fisch; allen kann man es nicht recht machen.");
    assertGood("Mittels eines Bootloaders und zugehöriger Software kann nach jedem Anstecken des Adapters eine andere Firmware-Varianten geladen werden");
    assertGood("Wenn sie eine andere Größe benötigen, teilen uns ihre speziellen Wünsche mit und wir unterbreiten ihnen ein Angebot über Preis und Lieferung.");
    assertGood("Dabei wird in einer Vakuumkammer eine einige Mikrometer dicke CVD-Diamantschicht auf den Substraten abgeschieden.");
    assertGood("1916 versuchte Gilbert Newton Lewis, die chemische Bindung durch Wechselwirkung der Elektronen eines Atoms mit einem anderen Atomen zu erklären.");
    assertGood("Vom einen Ende der Straße zum anderen.");
    assertGood("Er war müde vom vielen Laufen.");
    assertGood("Sind das echte Diamanten?");
    assertGood("Es wurde eine Verordnung erlassen, der zufolge jeder Haushalt Energie einsparen muss.");
    assertGood("Im Jahr 1922 verlieh ihm König George V. den erblichen Titel eines Baronet. ");
    assertGood("... der zu dieser Zeit aber ohnehin schon allen Einfluss verloren hatte.");
    assertGood("Ein Geschenk, das Maßstäbe setzt");
    assertGood("Einwohnerzahl stieg um das Zweieinhalbfache");
    assertGood("Die Müllers aus Hamburg.");

    assertGood("Wir machen das Januar.");
    assertGood("Wir teilen das Morgen mit.");
    assertGood("Wir präsentierten das vorletzten Sonnabend.");
    assertGood("Ich release das Vormittags.");
    assertGood("Sie aktualisieren das Montags.");
    assertGood("Kannst du das Mittags machen?");
    assertGood("Können Sie das nächsten Monat erledigen?");
    assertGood("Können Sie das auch nächsten Monat erledigen?");
    assertGood("War das Absicht?");

    assertGood("Das Dach meines Autos.");
    assertGood("Das Dach meiner Autos.");

    assertGood("Das Dach meines großen Autos.");
    assertGood("Das Dach meiner großen Autos.");

    assertGood("Dann schlug er so kräftig wie er konnte mit den Schwingen.");
    assertGood("Also wenn wir Glück haben, ...");
    assertGood("Wenn wir Pech haben, ...");
    assertGood("Ledorn öffnete eines der an ihr vorhandenen Fächer.");
    assertGood("Auf der einen Seite endlose Dünen");
    assertGood("In seinem Maul hielt er einen blutigen Fleischklumpen.");
    assertGood("Gleichzeitig dachte er intensiv an Nebelschwaden, aus denen Wolken ja bestanden.");
    assertGood("Warum stellte der bloß immer wieder dieselben Fragen?");
    assertGood("Bei der Hinreise.");
    assertGood("Schließlich tauchten in einem Waldstück unter ihnen Schienen auf.");

    assertGood("Das Wahlrecht, das Frauen damals zugesprochen bekamen.");
    assertGood("Es war Karl, dessen Leiche Donnerstag gefunden wurde.");

    assertGood("Erst recht ich Arbeiter.");
    assertGood("Erst recht wir Arbeiter.");
    assertGood("Erst recht wir fleißigen Arbeiter.");

    assertGood("Dann lud er Freunde ein.");
    assertGood("Dann lud sie Freunde ein.");
    assertGood("Aller Kommunikation liegt dies zugrunde.");
    assertGood("Pragmatisch wählt man solche Formeln als Axiome.");
    assertGood("Der eine Polizist rief dem anderen zu...");
    assertGood("Das eine Kind rief dem anderen zu...");
    assertGood("Er wollte seine Interessen wahrnehmen.");

    assertGood("... wo Krieg den Unschuldigen Leid und Tod bringt.");
    assertGood("Der Abschuss eines Papageien.");

    assertGood("Die Beibehaltung des Art. 1 ist geplant.");
    assertGood("Die Verschiebung des bisherigen Art. 1 ist geplant.");

    assertGood("In diesem Fall hatte das Vorteile.");
    assertGood("So hat das Konsequenzen.");

    assertGood("Ein für viele wichtiges Anliegen.");
    assertGood("Das weckte bei vielen ungute Erinnerungen.");
    assertGood("Etwas, das einem Angst macht.");
    assertGood("Einem geschenkten Gaul schaut man nicht ins Maul.");

    assertGood("Das erfordert Können.");
    assertGood("Ist das Kunst?");
    assertGood("Ist das Kunst oder Abfall?");
    assertGood("Die Zeitdauer, während der Wissen nützlich bleibt, wird kürzer.");
    assertGood("Es sollte nicht viele solcher Bilder geben");
    assertGood("In den 80er Jahren.");
    assertGood("Hast du etwas das Carina machen kann?");
    assertGood("Ein Artikel in den Ruhr Nachrichten.");
    assertGood("Ich wollte nur allen Hallo sagen.");
    assertGood("Ich habe deshalb allen Freund*innen Bescheid gegeben.");   // Gendersternchen, https://github.com/languagetool-org/languagetool/issues/2417
    assertGood("Ich habe deshalb allen Freund_innen Bescheid gegeben.");
    assertGood("Ich habe deshalb allen Freund:innen Bescheid gegeben.");
    assertGood("Sein*e Mitarbeiter*in ist davon auch betroffen.");
    assertGood("Jede*r Mitarbeiter*in ist davon betroffen.");
    assertGood("Alle Professor*innen");
    assertGood("Gleichzeitig wünscht sich Ihr frostresistenter Mitbewohner einige Grad weniger im eigenen Zimmer?");
    assertGood("Ein Trainer, der zum einen Fußballspiele sehr gut lesen und analysieren kann");
    assertGood("Eine Massengrenze, bis zu der Lithium nachgewiesen werden kann.");
    assertGood("Bei uns im Krankenhaus betrifft das Operationssäle.");
    assertGood("Macht dir das Freude?");
    assertGood("Das macht jedem Angst.");
    assertGood("Dann macht das Sinn.");
    assertGood("Das sind beides Lichtschalter.");

    // relative clauses:
    assertGood("Das Recht, das Frauen eingeräumt wird.");
    assertGood("Der Mann, in dem quadratische Fische schwammen.");
    assertGood("Der Mann, durch den quadratische Fische schwammen.");
    assertGood("Gutenberg, der quadratische Mann.");
    assertGood("Die größte Stuttgarter Grünanlage ist der Friedhof.");
    assertGood("Die meisten Lebensmittel enthalten das.");  // Lebensmittel has NOG as gender in Morphy
    // TODO: Find agreement errors in relative clauses
    assertBad("Gutenberg, die Genie.");
    //assertBad("Gutenberg, die größte Genie.");
    //assertBad("Gutenberg, die größte Genie aller Zeiten.");
    assertGood("Die wärmsten Monate sind August und September, die kältesten Januar und Februar.");
    // some of these used to cause false alarms:
    assertGood("Das Münchener Fest.");
    assertGood("Das Münchner Fest.");
    assertGood("Die Planung des Münchener Festes.");
    assertGood("Das Berliner Wetter.");
    assertGood("Den Berliner Arbeitern ist das egal.");
    assertGood("Das Haus des Berliner Arbeiters.");
    assertGood("Es gehört dem Berliner Arbeiter.");
    assertGood("Das Stuttgarter Auto.");
    assertGood("Das Bielefelder Radio.");
    assertGood("Das Gütersloher Radio.");
    assertGood("Das wirklich Wichtige kommt jetzt erst.");
    assertGood("Besonders wenn wir Wermut oder Absinth trinken.");
    assertGood("Ich wünsche dir alles Gute.");
    assertGood("Es ist nicht bekannt, mit welchem Alter Kinder diese Fähigkeit erlernen.");
    assertGood("Dieser ist nun in den Ortungsbereich des einen Roboters gefahren.");
    assertGood("Wenn dies großen Erfolg hat, werden wir es weiter fördern.");
    assertGood("Die Ereignisse dieses einen Jahres waren sehr schlimm.");
    assertGood("Er musste einen Hochwasser führenden Fluss nach dem anderen überqueren.");
    assertGood("Darf ich Ihren Füller für ein paar Minuten ausleihen?");
    assertGood("Bringen Sie diesen Gepäckaufkleber an Ihrem Gepäck an.");
    assertGood("Extras, die den Wert Ihres Autos erhöhen.");
    assertGood("Er hat einen 34-jährigen Sohn.");
    assertGood("Die Polizei erwischte die Diebin, weil diese Ausweis und Visitenkarte hinterließ.");
    assertGood("Dieses Versäumnis soll vertuscht worden sein - es wurde Anzeige erstattet.");
    assertGood("Die Firmen - nicht nur die ausländischen, auch die katalanischen - treibt diese Frage um.");
    // TODO: assertGood("Der Obst und Getränke führende Fachmarkt.");
    assertGood("Stell dich dem Leben lächelnd!");
    assertGood("Die Messe wird auf das vor der Stadt liegende Ausstellungsgelände verlegt.");
    assertGood("Sie sind ein den Frieden liebendes Volk.");
    assertGood("Zum Teil sind das Krebsvorstufen.");
    assertGood("Er sagt, dass das Rache bedeutet.");
    assertGood("Wenn das Kühe sind, bin ich ein Elefant.");
    assertGood("Karl sagte, dass sie niemandem Bescheid gegeben habe.");
    assertGood("Es blieb nur dieser eine Satz.");
    assertGood("Oder ist das Mathematikern vorbehalten?");
    assertGood("Wenn hier einer Fragen stellt, dann ich.");
    assertGood("Wenn einer Katzen mag, dann meine Schwester.");
    assertGood("Ergibt das Sinn?");
    assertGood("Sie ist über die Maßen schön.");
    assertGood("Ich vertraue ganz auf die Meinen.");
    assertGood("Was nützt einem Gesundheit, wenn man sonst ein Idiot ist?");
    assertGood("Auch das hatte sein Gutes.");
    assertGood("Auch wenn es sein Gutes hatte, war es doch traurig.");
    assertGood("Er wollte doch nur jemandem Gutes tun.");
    assertGood("und das erst Jahrhunderte spätere Auftauchen der Legende");
    assertGood("Texas und New Mexico, beides spanische Kolonien, sind...");
    assertGood("Unser Hund vergräbt seine Knochen im Garten.");
    assertGood("Ob das Mehrwert bringt?");
    assertGood("Warum das Sinn macht?");
    assertGood("Das hängt davon ab, ob die Deutsch sprechen");
    assertGood("Die meisten Coaches wissen nichts.");
    assertGood("Die Präsent AG.");
    assertGood("In New York war er der Titelheld in Richard III. und spielte den Mark Anton in Julius Cäsar.");
    assertGood("Vielen Dank fürs Bescheid geben.");
    assertGood("Welche Display Ads?");
    assertGood("Das letzte Mal war das Anfang der 90er Jahre des vergangenen Jahrhunderts");
    assertGood("Der vom Rat der Justizminister gefasste Beschluss zur Aufnahme von Vertriebenen...");
    assertGood("Der letzte Woche vom Rat der Justizminister gefasste Beschluss zur Aufnahme von Vertriebenen...");
    assertGood("Was war sie nur für eine dumme Person!");
    assertGood("Was war ich für ein Idiot!");
    assertGood("Was für ein Idiot!");
    assertGood("Was für eine blöde Kuh!");
    assertGood("Was ist sie nur für eine blöde Kuh!");
    assertGood("Wie viele Paar Stiefel brauche ich eigentlich?");
    assertGood("Dieses versuchten Mathematiker 400 Jahre lang vergeblich zu beweisen.");
    //assertGood("Bei dem Papierabzüge von Digitalbildern bestellt werden.");
    assertGood("Gemälde informieren uns über das Leben von den vergangenen Jahrhunderten…");
    assertGood("Die Partei, die bei den vorangegangenen Wahlen noch seine Politik unterstützt hatte.");
    assertGood("Bei Zunahme der aufgelösten Mineralstoffe, bei denen...");
    assertGood("Je mehr Muskelspindeln in einem Muskel vorhanden sind, desto feiner können die mit diesem verbundenen Bewegungen abgestimmt werden.");
    assertGood("Diese datentechnischen Operationen werden durch Computerprogramme ausgelöst, d. h. über entsprechende, in diesen enthaltene Befehle (als Teil eines implementierten Algorithmus') vorgegeben.");
    assertGood("Aus diesen resultierten Konflikte wie der Bauernkrieg und der Pfälzische Erbfolgekrieg.");
    assertGood("Die Staatshandlungen einer Mikronation und von dieser herausgegebene Ausweise, Urkunden und Dokumente gelten im Rechtsverkehr als unwirksam");
    assertGood("Auf der Hohen See und auf den mit dieser verbundenen Gewässern gelten die internationalen Kollisionsverhütungsregeln.");
    assertGood("Art. 11 Abs. 2 GGV setzt dem bestimmte Arten der außergemeinschaftlichen Zugänglichmachung gleich");
    assertGood("Grundsätzlich sind die Heilungschancen von Männern mit Brustkrebs nicht schlechter als die betroffener Frauen.");
    assertGood("In diesem Viertel bin ich aufgewachsen.");
    assertGood("Im November wurde auf dem Gelände der Wettbewerb ausgetragen.");
    assertGood("Er ist Eigentümer des gleichnamigen Schemas und stellt dieses interessierten Domänen zur Verfügung.");
    assertGood("Dort finden sie viele Informationen rund um die Themen Schwangerschaft, Geburt, Stillen, Babys und Kinder.");
    assertGood("Die Galerie zu den Bildern findet sich hier.");
    assertGood("Ganz im Gegensatz zu den Blättern des Brombeerstrauches.");
    assertGood("Er erzählte von den Leuten und den Dingen, die er auf seiner Reise gesehen hatte.");
    assertGood("Diese Partnerschaft wurde 1989 nach dem Massaker auf dem Platz des Himmlischen Friedens eingefroren.");
    assertGood("Die Feuergefahr hingegen war für für die Londoner Teil des Alltags.");
    assertGood("Was ist, wenn ein Projekt bei den Berliner Type Awards mit einem Diplom ausgezeichnet wird?");
    assertGood("Was ist mit dem Liechtensteiner Kulturleben los?");
    assertGood("Das ist der Mann den Präsident Xi Jinping verurteilte.");
    // incorrect sentences:
    assertBad("Ein Buch mit einem ganz ähnlichem Titel.");
    assertBad("Meiner Chef raucht.");
    assertBad("Er hat eine 34-jährigen Sohn.");
    assertBad("Es sind die Tisch.", "dem Tisch", "den Tisch", "der Tisch", "die Tische");
    assertBad("Es sind das Tisch.", "dem Tisch", "den Tisch", "der Tisch");
    assertBad("Es sind die Haus.", "das Haus", "dem Haus", "die Häuser");
    assertBad("Es sind der Haus.", "das Haus", "dem Haus", "der Häuser");
    assertBad("Es sind das Frau.", "der Frau", "die Frau");
    assertBad("Das Auto des Mann.", "dem Mann", "den Mann", "der Mann", "des Mannes", "des Manns");
    assertBad("Das interessiert das Mann.", "dem Mann", "den Mann", "der Mann");
    assertBad("Das interessiert die Mann.", "dem Mann", "den Mann", "der Mann", "die Männer");
    assertBad("Das Auto ein Mannes.", "ein Mann", "eines Mannes");
    assertBad("Das Auto einem Mannes.", "einem Mann", "einem Manne", "eines Mannes");
    assertBad("Das Auto einer Mannes.", "eines Mannes");
    assertBad("Das Auto einen Mannes.", "einen Mann", "eines Mannes");
    assertBad("Die Galerie zu den Bilder findet sich hier.");
    assertBad("Ganz im Gegensatz zu den Blätter des Brombeerstrauches.");
    //assertBad("Das erwähnt Auto bog nach rechts ab.");    // TODO
    assertGood("Das erlaubt Forschern, neue Versuche durchzuführen.");
    assertGood("Dies ermöglicht Forschern, neue Versuche durchzuführen.");
    assertGood("Je länger zugewartet wird, desto schwieriger dürfte es werden, die Jungtiere von den Elterntieren zu unterscheiden.");
    assertGood("Er schrieb ein von 1237 bis 1358 reichendes Geschichtswerk, dessen Schwerpunkt auf den Ereignissen in der Lombardei liegt.");
    assertBad("Die erwähnt Konferenz ist am Samstag.");
    assertBad("Die erwähntes Konferenz ist am Samstag.");
    assertBad("Die erwähnten Konferenz ist am Samstag.");
    assertBad("Die erwähnter Konferenz ist am Samstag.");
    assertBad("Die erwähntem Konferenz ist am Samstag.");
    assertBad("Die gemessen Werte werden in die länderspezifische Höhe über dem Meeresspiegel umgerechnet.");
    assertBad("Darüber hinaus haben wir das berechtigte Interessen, diese Daten zu verarbeiten.");
    assertBad("Eine Amnestie kann den Hingerichteten nicht das Leben und dem heimgesuchten Familien nicht das Glück zurückgeben.");
    //assertBad("Zu den gefährdete Vögeln Malis gehören der Strauß, Großtrappen und Perlhuhn.");
    //assertBad("Zu den gefährdete Vögel Malis gehören der Strauß, Großtrappen und Perlhuhn.");
    assertBad("Z. B. therapeutisches Klonen, um aus den gewonnen Zellen in vitro Ersatzorgane für den Patienten zu erzeugen");
    //assertBad("Gemälde informieren uns über das Leben von den vergangenen Jahrhunderte…");
    assertBad("Die Partei, die bei den vorangegangen Wahlen noch seine Politik unterstützt hatte.");
    assertBad("Bei Zunahme der aufgelösten Mineralstoffen, bei denen...");
    assertBad("Durch die große Vielfalt der verschiedene Linien ist für jeden Anspruch die richtige Brille im Portfolio.");
    assertBad("In diesen Viertel bin ich aufgewachsen.");
    assertBad("Im November wurde auf den Gelände der Wettbewerb ausgetragen.");
    assertBad("Dort finden sie Testberichte und viele Informationen rund um das Themen Schwangerschaft, Geburt, Stillen, Babys und Kinder.");
    assertBad("Je länger zugewartet wird, desto schwieriger dürfte es werden, die Jungtiere von den Elterntiere zu unterscheiden.");
    assertBad("Er schrieb ein von 1237 bis 1358 reichendes Geschichtswerk, dessen Schwerpunkt auf den Ereignisse in der Lombardei liegt.");
    assertBad("Des großer Mannes.");
    assertBad("Er erzählte von den Leute und den Dingen, die er gesehen hatte.");
    assertBad("Diese Partnerschaft wurde 1989 nach den Massaker auf dem Platz des Himmlischen Friedens eingefroren.");

    assertBad("Das Dach von meine Auto.", "meine Autos", "meinem Auto");
    assertBad("Das Dach von meinen Auto.", "meinem Auto", "meinen Autos");

    assertBad("Das Dach mein Autos.", "mein Auto", "meine Autos", "meinen Autos", "meiner Autos", "meines Autos");
    assertBad("Das Dach meinem Autos.", "meine Autos", "meinem Auto", "meinen Autos", "meiner Autos", "meines Autos");

    assertBad("Das Dach meinem großen Autos.");
    assertBad("Das Dach mein großen Autos.");

    assertBad("Das Klientel der Partei.", "Der Klientel", "Die Klientel");  // gender used to be wrong in Morphy data
    assertGood("Die Klientel der Partei.");

    assertBad("Der Haus ist groß", "Das Haus", "Dem Haus", "Der Häuser");
    assertBad("Aber der Haus ist groß", "das Haus", "dem Haus", "der Häuser");

    assertBad("Ich habe einen Feder gefunden.", "eine Feder", "einer Feder");

    assertGood("Wenn die Gott zugeschriebenen Eigenschaften stimmen, dann...");
    assertGood("Dieses Grünkern genannte Getreide ist aber nicht backbar.");
    assertGood("Außerdem unterstützt mich Herr Müller beim abheften");
    assertGood("Außerdem unterstützt mich Frau Müller beim abheften");
    assertBad("Der Zustand meiner Gehirns.");

    assertBad("Lebensmittel sind da, um den menschliche Körper zu ernähren.");
    assertBad("Geld ist da, um den menschliche Überleben sicherzustellen.");
    assertBad("Sie hatte das kleinen Kaninchen.");
    assertBad("Frau Müller hat das wichtigen Dokument gefunden.");
    assertBad("Ich gebe dir ein kleine Kaninchen.");
    assertBad("Ich gebe dir ein kleinen Kaninchen.");
    assertBad("Ich gebe dir ein kleinem Kaninchen.");
    assertBad("Ich gebe dir ein kleiner Kaninchen.");
    //assertBad("Ich gebe dir ein klein Kaninchen.");  // already detected by MEIN_KLEIN_HAUS
    assertGood("Ich gebe dir ein kleines Kaninchen.");

    assertBad("Ich gebe dir das kleinen Kaninchen.");
    assertBad("Ich gebe dir das kleinem Kaninchen.");
    assertBad("Ich gebe dir das kleiner Kaninchen.");
    assertBadWithNoSuggestion("Geprägt ist der Platz durch einen 142 Meter hoher Obelisken");
    //assertBad("Ich gebe dir das kleines Kaninchen.");  // already detected by ART_ADJ_SOL
    //assertBad("Ich gebe dir das klein Kaninchen.");  // already detected by MEIN_KLEIN_HAUS
    assertGood("Ich gebe dir das kleine Kaninchen.");
    assertGood("Die Top 3 der Umfrage");
    assertGood("Dein Vorschlag befindet sich unter meinen Top 5.");
    assertGood("Unter diesen rief das großen Unmut hervor.");
    assertGood("Bei mir löste das Panik aus.");
    assertGood("Sie können das machen in dem sie die CAD.pdf öffnen.");
    assertGood("Ich mache eine Ausbildung zur Junior Digital Marketing Managerin.");

    assertGood("Dann wird das Konsequenzen haben.");
    assertGood("Dann hat das Konsequenzen.");
    assertGood("Sollte das Konsequenzen nach sich ziehen?");
    assertGood("Der Echo Show von Amazon");
    assertGood("Die BVG kommen immer zu spät.");
    assertGood("In der Frühe der Nacht.");
    assertGood("Der TV Steinfurt.");
    assertGood("Ein ID 3 von Volkswagen.");
    assertGood("Der ID.3 von Volkswagen.");
    assertGood("Der ID3 von Volkswagen.");
    assertGood("Das bedeutet Krieg!");

    assertBad("Hier steht Ihre Text.");
    assertBad("Hier steht ihre Text.");

    assertBad("Das ist doch lächerlich, was ist denn das für ein Klinik?");
    assertGood("Das ist doch lächerlich, was ist denn das für eine Klinik?");
    assertGood("Was ist denn das für ein Typ?");
    assertGood("Hier geht's zur Customer Journey.");
    assertGood("Das führt zur Verbesserung der gesamten Customer Journey.");
    assertGood("Meint er das wirklich Ernst?");
    assertGood("Meinen Sie das Ernst?");
    assertGood("Die können sich in unserer Hall of Fame verewigen.");
    assertGood("Die können sich in unserer neuen Hall of Fame verewigen.");
    assertGood("Auch, wenn das weite Teile der Bevölkerung betrifft.");
    assertGood("Hat das Einfluss auf Ihr Trinkverhalten?");

    assertBad("Ich weiß nicht mehr, was unser langweiligen Thema war.");
    assertGood("Aber mein Wissen über die Antike ist ausbaufähig.");
    assertBad("Er ging ins Küche.");
    assertBad("Er ging ans Luft.");
    assertBad("Eine Niereninsuffizienz führt zur Störungen des Wasserhaushalts.");
    assertBad("Er stieg durchs Fensters.");
    assertBad("Ich habe heute ein Krankenwagen gesehen.");
    assertGood("Sie werden merken, dass das echte Nutzer sind.");
    assertGood("Dieses neue Mac OS trug den Codenamen Rhapsody.");
    assertGood("Das Mac OS is besser als Windows.");
    assertGood("Damit steht das Porsche Museum wie kaum ein anderes Museum für Lebendigkeit und Abwechslung.");
    assertGood("Weitere Krankenhäuser sind dass Eastern Shore Memorial Hospital, IWK Health Centre, Nova Scotia Hospital und das Queen Elizabeth II Health Sciences Centre.");
    assertGood("Ich bin von Natur aus ein sehr neugieriger Mensch.");
    assertGood("Ich bin auf der Suche nach einer Junior Developerin.");
    assertGood("War das Eifersucht?");
    assertGood("Waren das schwierige Entscheidungen?");
    assertGood("Soll das Demokratie sein?");
    assertGood("Hat das Spaß gemacht?");
    assertBad("Funktioniert das Software auch mit Windows?");
    assertGood("Soll das Sinn stiften?");
    assertGood("Soll das Freude machen?");
    assertGood("Die Trial ist ausgelaufen.");
    assertGood("Ein geworbener Neukunde interagiert zusätzlich mit dem Unternehmen.");
    // TODO: not yet detected:
    //assertBad("Erst recht wir fleißiges Arbeiter.");
    //assertBad("Erst recht ich fleißiges Arbeiter.");
    //assertBad("Das Dach meine großen Autos.");
    //assertBad("Das Dach meinen großen Autos.");
    //assertBad("Das Dach meine Autos.");
    //assertBad("Es ist das Haus dem Mann.");
    //assertBad("Das interessiert der Männer.");
    //assertBad("Das interessiert der Mann.");
    //assertBad("Das gehört den Mann."); // detected by DEN_DEM
    //assertBad("Es sind der Frau.");
  }

  @Test
  public void testZurReplacement() throws IOException {
    assertBad("Hier geht's zur Schrank.", "zum Schrank");
    assertBad("Hier geht's zur Schränken.", "zu den Schränken", "zum Schränken");
    assertBad("Hier geht's zur Männern.", "zu den Männern");
    assertBad("Hier geht's zur Portal.", "zum Portal");
    assertBad("Hier geht's zur Portalen.", "zu den Portalen");
    assertBad("Sie gehen zur Frauen.", "zu Frauen", "zu den Frauen", "zur Frau");
    assertBad("Niereninsuffizienz führt zur Störungen des Wasserhaushalts.", "zu Störungen", "zu den Störungen", "zur Störung");
    assertBad("Das Motiv wird in der Klassik auch zur Darstellungen übernommen.", "zu Darstellungen", "zu den Darstellungen", "zur Darstellung");
    assertGood("Hier geht's zur Sonne.");
    assertGood("Hier geht's zum Schrank.");
    assertGood("Niereninsuffizienz führt zu Störungen des Wasserhaushalts.");
  }

  @Test
  public void testVieleWenige() throws IOException {
    assertGood("Zusammenschluss mehrerer dörflicher Siedlungen an einer Furt");
    assertGood("Für einige markante Szenen");
    assertGood("Für einige markante Szenen baute Hitchcock ein Schloss.");
    assertGood("Haben Sie viele glückliche Erfahrungen in Ihrer Kindheit gemacht?");
    assertGood("Es gibt viele gute Sachen auf der Welt.");
    assertGood("Viele englische Wörter haben lateinischen Ursprung");
    assertGood("Ein Bericht über Fruchtsaft, einige ähnliche Erzeugnisse und Fruchtnektar");
    assertGood("Der Typ, der seit einiger Zeit immer wieder hierher kommt.");
    assertGood("Jede Schnittmenge abzählbar vieler offener Mengen");
    assertGood("Es kam zur Fusion der genannten und noch einiger weiterer Unternehmen.");
    assertGood("Zu dieser Fragestellung gibt es viele unterschiedliche Meinungen.");
  }

  @Test
  public void testDetNounRuleErrorMessages() throws IOException {
    // check detailed error messages:
    assertBadWithMessage("Das Fahrrads.", "des Kasus");
    assertBadWithMessage("Der Fahrrad.", "des Genus");
    assertBadWithMessage("Das Fahrräder.", "des Numerus");
    assertBadWithMessage("Die Tischen sind eckig.", "des Kasus");
    assertBadWithMessage("Die Tischen sind eckig.", "und Genus");
  }

  @Test
  public void testRegression() throws IOException {
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));
    // used to be not detected > 1.0.1:
    String str = "Und so.\r\nDie Bier.";
    List<RuleMatch> matches = lt.check(str);
    assertEquals(1, matches.size());
  }

  @Test
  public void testDetAdjNounRule() throws IOException {
    // correct sentences:
    assertGood("Das ist der riesige Tisch.");
    assertGood("Der riesige Tisch ist groß.");
    assertGood("Die Kanten der der riesigen Tische.");
    assertGood("Den riesigen Tisch mag er.");
    assertGood("Es mag den riesigen Tisch.");
    assertGood("Die Kante des riesigen Tisches.");
    assertGood("Dem riesigen Tisch fehlt was.");
    assertGood("Die riesigen Tische sind groß.");
    assertGood("Der riesigen Tische wegen.");
    assertGood("An der roten Ampel.");
    assertGood("Dann hat das natürlich Nachteile.");
    assertGood("Ihre erste Nr. 1");
    assertGood("Wir bedanken uns bei allen Teams.");
    assertGood("Als Heinrich versuchte, seinen Kandidaten für den Mailänder Bischofssitz durchzusetzen, reagierte der Papst sofort.");
    assertGood("Den neuen Finanzierungsweg wollen sie daher Hand in Hand mit dem Leser gehen.");
    assertGood("Lieber den Spatz in der Hand...");
    assertGood("Wir wollen sein ein einzig Volk von Brüdern");
    assertGood("Eine Zeitreise durch die 68er Revolte");
    assertGood("Ich besitze ein Modell aus der 300er Reihe.");
    assertGood("Aber ansonsten ist das erste Sahne");
    assertGood("...damit diese ausreichend Sauerstoff geben.");

    // incorrect sentences:
    assertBad("Es sind die riesigen Tisch.");
    //assertBad("Dort, die riesigen Tischs!");    // TODO: error not detected because of comma
    assertBad("Als die riesigen Tischs kamen.");
    assertBad("Als die riesigen Tisches kamen.");
    assertBad("Der riesigen Tisch und so.");
    assertBad("An der roter Ampel.");
    assertBad("An der rote Ampel.");
    assertBad("An der rotes Ampel.");
    assertBad("An der rotem Ampel.");
    assertBad("Er hatte ihn aus dem 1,4 Meter tiefem Wasser gezogen.");
    assertBad("Er hatte ihn aus dem 1,4 Meter tiefem Wasser gezogen.");
    assertBad("Er hatte eine sehr schweren Infektion.");
    assertBad("Ein fast 5 Meter hohem Haus.");
    assertBad("Ein fünf Meter hohem Haus.");
    assertBad("Es wurden Karavellen eingesetzt, da diese für die flachen Gewässern geeignet waren.");
    assertBad("Wir bedanken uns bei allem Teams.");
    assertBad("Dabei geht es um das altbekannte Frage der Dynamiken der Eigenbildung..");
    assertBad("Den neue Finanzierungsweg wollen sie daher Hand in Hand mit dem Leser gehen.");
    assertBad("Den neuen Finanzierungsweg wollen sie daher Hand in Hand mit dem Lesern gehen.");
    //assertBad("An der rot Ampel.");
  }

  private void assertGood(String s) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(s));
    assertEquals("Found unexpected match in sentence '" + s + "': " + Arrays.toString(matches), 0, matches.length);
  }

  private void assertBad(String s, String... expectedSuggestions) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(s));
    assertEquals("Did not find one match in sentence '" + s + "'", 1, matches.length);
    if (expectedSuggestions.length > 0) {
      RuleMatch match = matches[0];
      List<String> suggestions = match.getSuggestedReplacements();
      assertThat(suggestions, is(Arrays.asList(expectedSuggestions)));
    }
  }

  private void assertBadWithNoSuggestion(String s) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(s));
    assertEquals("Did not find one match in sentence '" + s + "'", 1, matches.length);
    RuleMatch match = matches[0];
    List<String> suggestions = match.getSuggestedReplacements();
    if (suggestions.size() != 0) {
      fail("Expected 0 suggestions for: " + s + ", got: " + suggestions);
    }
  }

  private void assertBadWithMessage(String s, String expectedErrorSubstring) throws IOException {
    assertEquals(1, rule.match(lt.getAnalyzedSentence(s)).length);
    String errorMessage = rule.match(lt.getAnalyzedSentence(s))[0].getMessage();
    assertTrue("Got error '" + errorMessage + "', expected substring '" + expectedErrorSubstring + "'",
            errorMessage.contains(expectedErrorSubstring));
  }

}
