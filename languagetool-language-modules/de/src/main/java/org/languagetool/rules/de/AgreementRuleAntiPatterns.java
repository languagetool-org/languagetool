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

import org.languagetool.JLanguageTool;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.patterns.PatternTokenBuilder;

import java.util.*;

import static org.languagetool.rules.patterns.PatternRuleBuilderHelper.*;

class AgreementRuleAntiPatterns {

  static final List<List<PatternToken>> ANTI_PATTERNS = Arrays.asList(
    Arrays.asList(
      tokenRegex("spendet|macht"),  // "Macht dir das Hoffnung?"
      tokenRegex("mir|euch|dir|uns|ihnen"),
      token("das"),
      posRegex("SUB:.*SIN.*")
    ),
    Arrays.asList(
      tokenRegex("die|der|den"),  // "die späten 50er Jahre"
      tokenRegex("frühen|späten"),  // "die späten 50er Jahre"
      tokenRegex("\\d+er"),  // "die späten 50er Jahre"
      tokenRegex("Jahren?")
    ),
    Arrays.asList(
      posRegex("ART:.*"),  // "ein ausgesprochen unattraktiver Dienstort"
      token("ausgesprochen"),
      posRegex("ADJ:.*"),
      posRegex("SUB:.*")
    ),
    Arrays.asList(
      tokenRegex("ein|eine|einen"),  // "bietet einen weniger genauen Zugriff"
      token("weniger"),
      posRegex("ADJ:AKK:SIN:.*:GRU:.*"),
      posRegex("SUB:.*SIN.*")
    ),
    Arrays.asList(
      new PatternTokenBuilder().csToken("sein").matchInflectedForms().build(),
      token("das"),
      tokenRegex("Grund|Anlass|Auslöser|Ursache")
    ),
    Arrays.asList(
      // "Vielleicht schreckt das Frauen ab"
      tokenRegex("schreckte?"),
      token("das"),
      posRegex("SUB:.*")
    ),
    Arrays.asList(
      token("eine"),
      token("Zeitlang")
    ),
    Arrays.asList(
      token("für"),  // "..., wann und für wen Impfungen vorgenommen werden könnten."
      token("wen"),
      posRegex("SUB:.*PLU.*")
    ),
    Arrays.asList(
      token("der"),  // "der fließend Englisch sprechende Mitarbeiter"
      token("fließend"),
      tokenRegex(".*"),
      token("sprechende")
    ),
    Arrays.asList(
      token("ein"),  // "Das wolkige und ein bisschen kühle Wetter..."
      token("bisschen"),
      posRegex("ADJ:.*"),
      posRegex("SUB:.*SIN.*")
    ),
    Arrays.asList(
      token("ein"),  // "...um mal ein bisschen Einsichten zu bekommen" (ugs., aber okay)
      token("bisschen"),
      posRegex("SUB:.*PLU.*")
    ),
    Arrays.asList(
      token("dem"),  // "dem Abhilfe zu schaffen"
      token("Abhilfe"),
      new PatternTokenBuilder().token("zu").min(0).build(),
      token("schaffen")
    ),
    Arrays.asList(
      token("die"),  // "Die Müllers aus Hamburg"
      new PatternTokenBuilder().posRegex("EIG.*").tokenRegex(".*s").build()
    ),
    Arrays.asList(
      tokenRegex("ist|war|sei|wäre"),  // "war das Absicht"
      token("das"),
      new PatternTokenBuilder().posRegex("ADJ:.*").min(0).build(),
      tokenRegex("Absicht")
    ),
    Arrays.asList(
      token("das"),  // "in das damalige Reichenbach in Schlesien"
      new PatternTokenBuilder().posRegex("ADJ:.*").min(0).build(),
      tokenRegex("Reichenbach|Albstadt|Arnstadt|Darmstadt|Duderstadt|Eberstadt|Eibelstadt|Erftstadt|Freudenstadt|Bergneustadt|" +
        "Neustadt|Burgkunstadt|Diemelstadt|Ebermannstadt|Eisenhüttenstadt|Friedrichstadt|Filderstadt|Freystadt|Florstadt|Glückstadt|" +
        "Grünstadt|Hallstadt|Halberstadt|Ingolstadt|Johanngeorgenstadt|Karlstadt")  // TODO: extend, https://de.wikipedia.org/wiki/Liste_der_St%C3%A4dte_in_Deutschland
    ),
    Arrays.asList(
      token("das"),  // "Einwohnerzahl stieg um das Zweieinhalbfache"
      tokenRegex("(zwei|drei|vier|fünd|sechs|sieben|acht|neun|zehn|elf|zwölf).*fache")
    ),
    Arrays.asList(
      token("diese"),  // "...damit diese ausreichend Sauerstoff geben."
      tokenRegex("genug|genügend|viel|hinreichend|ausreichend"),
      posRegex("SUB:NOM:SIN:.*"),
      posRegex("VER:.*")
    ),
    Arrays.asList(
      posRegex("VER:MOD:.*"),  // "Sollten zu diesem weitere Informationen benötigt werden, ..."
      token("zu"),
      regex("diese[mnr]"),
      new PatternTokenBuilder().posRegex("ADJ:.*").min(0).build(),
      posRegex("SUB:NOM:PLU:.*"),
      posRegex("PA2:.*")
    ),
    Arrays.asList(
      regex("ein|das"),  // "Ein Geschenk, das Maßstäbe setzt" (#4043)
      pos("SUB:NOM:SIN:NEU"),
      token(","),
      token("das"),
      posRegex("SUB:NOM:PLU:.*"),
      posRegex("VER:3:.*")
    ),
    Arrays.asList(
      token("uns"),  // "und wünschen uns allen Gesundheit."
      token("allen"),
      posRegex("SUB:.*:SIN:.*")
    ),
    Arrays.asList(
      token("Domain"),
      token("Name"),
      tokenRegex("Systems?")
    ),
    Arrays.asList(
      tokenRegex("der|das|die"),
      new PatternTokenBuilder().min(0).build(),
      token("Bad"),
      token("Homburger")
    ),
    Arrays.asList(
      tokenRegex("der|die|das"),   // "Lieber jemanden, der einem Tipps/Hoffnung gibt." / "die 69er Revolte"
      csRegex("einem|[0-9]+er"),
      posRegex("SUB:.*")
    ),
    Arrays.asList(
      tokenRegex("de[rs]"),   // "Die dauerhafte Abgrenzung des später Niedersachsen genannten Gebietes"
      posRegex("ADJ:.*"),
      posRegex("EIG:.*"),
      posRegex("PA2:.*")
    ),
    Arrays.asList(
      posRegex("ART:.*FEM.*"),  // "Eine Lücke in der erneuerbare Energien eine sinnvolle Rolle spielen könnten"
      posRegex("SUB:.*FEM.*"),
      token("in"),
      token("der")
    ),
    Arrays.asList(
      token("einem"),
      token("kalte"),
      token("Schauer")
    ),
    Arrays.asList(
      regex("die|der"),  // "die Querwild GmbH"
      posRegex("SUB:.*"),
      token("GmbH")
    ),
    Arrays.asList(
      token("Die"),
      regex("Waltons|Einen")
    ),
    Arrays.asList(
      regex("Große[sn]?"),
      regex("(Bundes)?Verdienstkreuz(es)?")
    ),
    Arrays.asList( // "Adiponitril und Acetoncyanhydrin, beides Zwischenprodukte der Kunststoffproduktion."
      token(","),
      token("beides"),
      posRegex("SUB:.*")
    ),
    Arrays.asList( // "In den Zwei Abhandlungen" (lowercase "zwei" is correct, but does not need to be found here)
      tokenRegex("Eins|Zwei|Drei|Vier|Fünf|Sechs|Sieben|Acht|Neun|Zehn|Elf|Zwölf"),
      posRegex("SUB:.*")
    ),
    Arrays.asList( // "Eine Massengrenze, bis zu der Lithium nachgewiesen werden kann."
      token("bis"),
      token("zu"),
      token("der"),
      posRegex("SUB:.*"),
      posRegex("PA2:.*")
    ),
    Arrays.asList(
      tokenRegex("jeder?"),
      token("Abitur")
    ),
    Arrays.asList(
      token("Halle"),
      token("an"),
      token("der"),
      token("Saale")
    ),
    Arrays.asList(  // "mehrere Tausend Menschen"
      tokenRegex("Dutzend|Hundert|Tausend"),
      new PatternTokenBuilder().posRegex("ADJ:.*").min(0).build(),
      posRegex("SUB:.*")
    ),
    Arrays.asList(  // "Besonders reizen mich Fahrräder.", "weil mich psychische Erkrankungen aus der Bahn werfen"
      tokenRegex("dich|mich"),
      new PatternTokenBuilder().posRegex("ADJ:.*").min(0).build(),
      posRegex("SUB:.*")
    ),
    Arrays.asList(  // "jenes Weges, den die Tausenden Juden 1945 ..."
      token("die"),
      token("Tausenden"),
      posRegex("SUB:.*PLU.*")
    ),
    Arrays.asList(  // misspelling "Format"
      tokenRegex("das|ein"),
      token("Formart")
    ),
    Arrays.asList(  // "... andere erfreut Tennis."
      regex("andere"),
      posRegex("VER:PA2.*"),
      posRegex("SUB:.*")
    ),
    Arrays.asList(  // "Das eine bedeutet Gefahr und das andere Gelegenheit."
      regex("der|die|das"),
      new PatternTokenBuilder().token("eine").setSkip(-1).build(),
      regex("der|die|das"),
      token("andere"),
      posRegex("SUB:.*")
    ),
    Arrays.asList(  // "... größeren Bekanntheitsgrad in der Bevölkerung als jeder andere Kandidat vor ihm"
      regex("jede[mnrs]?"),
      regex("anderen?"),
      posRegex("SUB:.*")
    ),
    Arrays.asList(  // "... kein anderer Unrecht hat."
      regex("diese[rs]?|keine?"),
      regex("anderer?"),
      posRegex("SUB:.*")
    ),
    Arrays.asList(  // "Toleranz ist der Verdacht, dass der andere Recht hat."
      regex("der|die|das"),
      regex("anderen?"),
      token("Recht"),
      new PatternTokenBuilder().csToken("haben").matchInflectedForms().build()
    ),
    Arrays.asList(  // "als einziger ein für die anderen unsichtbares Wunder zu sehen."
      token("für"),
      regex("den|die"),
      token("anderen")
    ),
    Arrays.asList(  // "Wer auf eines anderen Schuhe wartet...", "...Auge darauf haben, dass keine der anderen Abbruch tue"
      regex("der|eine[sr]"),
      token("anderen"),
      posRegex("SUB:.*")
    ),
    Arrays.asList(  // "wenn andere anderer Meinung sind"
      token("andere"),
      regex("anderer?"),
      posRegex("SUB:.*")
    ),
    Arrays.asList(  // "Hat ein Schutzgut gegenüber den anderen Priorität?"
      token("gegenüber"),
      token("den"),
      token("anderen"),
      posRegex("SUB:.*")
    ),
    Arrays.asList(  // "... ist des anderen Freiheitskämpfer", "... die anderen Volleyball"
      regex("des|die"),
      token("anderen"),
      posRegex("SUB:.*")
    ),
    Arrays.asList(  // "Ein Esel schimpft den anderen Langohr."
      posRegex("VER:3:.*"),
      regex("den|die|das"),
      regex("anderen?"),
      posRegex("SUB:.*")
    ),
    Arrays.asList(  // "... eine bessere Behandlung als andere Gefangene."
      token("als"),
      token("andere"),
      posRegex("SUB:.*PLU.*")
    ),
    Arrays.asList(  // "was sein Klient für ein Mensch sei",
      // "Mir wird nicht ganz klar, was das bei 1:58 für ein Akkord ist."
      new PatternTokenBuilder().token("was").setSkip(5).build(),
      token("für"),
      token("ein"),
      new PatternTokenBuilder().posRegex("ADJ:.*(MAS|NEU).*").min(0).build(),
      posRegex("SUB:.*:(MAS|NEU).*")
    ),
    Arrays.asList(  // "was sein Klient für ein Mensch sei",
      // "Mir wird nicht ganz klar, was das bei 1:58 für ein Akkord ist."
      new PatternTokenBuilder().token("was").setSkip(5).build(),
      token("für"),
      token("eine"),
      new PatternTokenBuilder().posRegex("ADJ:.*FEM.*").min(0).build(),
      posRegex("SUB:.*:FEM.*")
    ),
    Arrays.asList(
      // "Was in aller Welt soll das denn für ein Satz sein?"
      token("was"),
      token("in"),
      token("aller"),
      new PatternTokenBuilder().token("Welt").setSkip(4).build(),
      token("für"),
      token("ein"),
      new PatternTokenBuilder().posRegex("ADJ:.*(MAS|NEU).*").min(0).build(),
      posRegex("SUB:.*:(MAS|NEU).*")
    ),
    Arrays.asList(
      // "Was in aller Welt soll das denn für ein Satz sein?"
      token("was"),
      token("in"),
      token("aller"),
      new PatternTokenBuilder().token("Welt").setSkip(4).build(),
      token("für"),
      token("eine"),
      new PatternTokenBuilder().posRegex("ADJ:.*FEM.*").min(0).build(),
      posRegex("SUB:.*:FEM.*")
    ),
    Arrays.asList(  // "wird das schwere Konsequenzen haben"
      token("das"),
      token("schwere"),
      token("Konsequenzen")
    ),
    Arrays.asList(  // "der Chaos Computer Club"
      token("der"),
      token("Chaos"),
      token("Computer"),
      token("Club")
    ),
    Arrays.asList(  // "der Echo Show" (Amazon device)
      token("Echo"),
      tokenRegex("Show|Dot")
    ),
    Arrays.asList(  // "In einem App Store"
      tokenRegex("App|Play"),
      token("Store")
    ),
    Arrays.asList(  // "in dem einen Jahr"
      token("dem"),
      token("einen"),
      pos("SUB:NOM:SIN:NEU")
    ),
    Arrays.asList(  // "Dies erlaubt Forschern, ..." aber auch "Dieses versuchten Mathematiker ..."
      pos("SENT_START"),
      posRegex("PRO:DEM:.+"),
      posRegex("VER:3:.+"),
      posRegex("SUB:(DAT|NOM):PLU.*")
    ),
    Arrays.asList(  // "in denen Energie steckt"
      new PatternTokenBuilder().posRegex("SENT_START|VER:AUX:[123].+").negate().build(),
      posRegex("PRP:.+"),
      new PatternTokenBuilder().posRegex("PRO:DEM:(DAT|AKK).+").tokenRegex("der|dies").matchInflectedForms().build(),
      posRegex("SUB:...:PLU.*")
    ),
    Arrays.asList(  // "für den Mailänder Bischofssitz"
      posRegex("PRP:.+"),
      new PatternTokenBuilder().posRegex("PRO:DEM:(DAT|AKK).+").tokenRegex("der|dies").matchInflectedForms().build(),
      new PatternTokenBuilder().csTokenRegex("[A-ZÄÖÜ].+er").build(),
      new PatternTokenBuilder().posRegex("SUB.+").build()
    ),
    Arrays.asList(
      posRegex("PRP:.+"),
      posRegex("PRO:DEM:(DAT|AKK).+"),
      posRegex("PA2:(DAT|AKK).+"),
      posRegex("SUB:(DAT|AKK):.*")
    ),
    Arrays.asList( // Artikel 34 setzt dem bestimmte Formen gleich
      posRegex("VER:.*[123].*"),
      posRegex("PRO:DEM:DAT:SIN:NEU.*"),
      posRegex("PA2:AKK:PLU.+"),
      posRegex("SUB:AKK:PLU.+")
    ),
    Arrays.asList( // Er stellt dieses interessierten Domänen zur Verfügung
      posRegex("VER:.*[123].*"),
      posRegex("PRO:DEM:AKK:SIN:NEU.*"),
      posRegex("PA2:DAT:PLU.+"),
      posRegex("SUB:DAT:PLU.+")
    ),
    Arrays.asList(
      pos("ADJ:PRD:KOM"),
      csToken("als"),
      regex("d(er|ie|as)"),
      posRegex(".+:GEN:.+")
    ),
    Arrays.asList(  // "Wir bereinigen das nächsten Dienstag."
      posRegex("VER:.*|UNKNOWN"),
      token("das"),
      csRegex("(über)?nächste[ns]?|kommende[ns]?|(vor)?letzten"),
      csRegex("Januar|Februar|März|April|Mai|Juni|Juli|August|September|Oktober|November|Dezember|Montag|D(ien|onner)stag|Mittwoch|Freitag|S(ams|onn)tag|Sonnabend|Woche|Monat|Jahr|Morgens?|Abends|Übermorgen|Mittags?|Nachmittags?|Vormittags?|Spätabends?|Nachts?")
    ),
    Arrays.asList(  // "Wir releasen das Montag.", "Wir präsentierten das Januar."
      posRegex("VER:.*|UNKNOWN"),
      csToken("das"),
      csRegex("Januar|Februar|März|April|Mai|Ju[nl]i|August|September|Oktober|November|Dezember|Montags?|D(ien|onner)stags?|Mittwochs?|Freitags?|S(ams|onn)tags?|Sonnabends?|Morgens?|Abends|Übermorgen|Mittags?|Nachmittags?|Vormittags?|Spätabends?|Nachts?")
    ),
    Arrays.asList(  // "Kannst du das Mittags machen?"
      token("das"),
      tokenRegex("Januar|Februar|März|April|Mai|Ju[nl]i|August|September|Oktober|November|Dezember|Montags?|D(ien|onner)stags?|Mittwochs?|Freitags?|S(ams|onn)tags?|Sonnabends?|Morgens?|Abends|Übermorgen|Mittags?|Nachmittags?|Vormittags?|Spätabends?|Nachts?"),
      posRegex("VER:.*|UNKNOWN")
    ),
    Arrays.asList(  // "Kannst du das nächsten Monat machen?"
      token("das"),
      tokenRegex("(über)?nächste[ns]?|kommende[ns]?|(vor)?letzten|vorigen"),
      csRegex("Januar|Februar|März|April|Mai|Ju[nl]i|August|September|Oktober|November|Dezember|Montag|D(ien|onner)stag|Mittwoch|Freitag|S(ams|onn)tag|Sonnabend|Woche|Monat|Jahr|Morgens?|Abends|Übermorgen|Mittags?|Nachmittags?|Vormittags?|Spätabends?|Nachts?"),
      posRegex("VER:.*|UNKNOWN")
    ),
    Arrays.asList(
      token("das"),
      csRegex("Zufall|Sinn|Spa(ß|ss)|Freude"),
      token("?")
    ),
    Arrays.asList(
       // "War das Zufall, dass es ging?"
      token("das"),
      csRegex("Zufall|Sinn|Spa(ß|ss)"),
      csToken(",")
    ),
    Arrays.asList(
      token("in"),
      tokenRegex("d(ies)?em"),
      token("Fall"),
      tokenRegex("(?i:hat(te)?)"),
      token("das")
    ),
    Arrays.asList( // "So hatte das Vorteile|Auswirkungen|Konsequenzen..."
      posRegex("ADV:.+"),
      tokenRegex("(?i:hat(te)?)"),
      csToken("das")
    ),
    Arrays.asList(
      tokenRegex("von|bei"),
      csRegex("vielen|allen"),
      posRegex("PA2:.*|ADJ:AKK:PLU:.*")  // "ein von vielen bewundertes Haus" / "Das weckte bei vielen ungute Erinnerungen."
    ),
    Arrays.asList(
      // "Der letzte Woche vom Rat der Justizminister gefasste Beschluss..."
      tokenRegex("de[mnr]|die|das"),
      csRegex("letzte[ns]?|vorige[ns]?"),
      csRegex("Woche|Monat|Jahr(zehnt|hundert)?"),
      posRegex("PRP:.*"),
      posRegex("SUB:.*"),
      posRegex("ART:.*"),
      posRegex("SUB:.*"),
      posRegex("PA2:.*")
    ),
    Arrays.asList(
      token("für"),
      csRegex("(viele|alle|[dm]ich|ihn|sie|uns|andere|jeden)"),
      posRegex("ADJ:NOM:.*")  // "Ein für viele wichtiges Anliegen."
    ),
    Arrays.asList(
      new PatternTokenBuilder().tokenRegex("flößen|machen|jagen").matchInflectedForms().build(),
      csRegex("einem|jedem|keinem"),
      csToken("Angst")  // "Dinge, die/ Etwas, das einem Angst macht"
    ),
    Arrays.asList(
      tokenRegex("einem|jedem|keinem"),
      csToken("Angst"),  // "Was einem Angst macht"
      new PatternTokenBuilder().tokenRegex("machen|ein(flößen|jagen)").matchInflectedForms().build()
    ),
    Arrays.asList(
      token("einem"),
      csToken("geschenkten"),
      csToken("Gaul")
    ),
    Arrays.asList( // "Wir wollen sein ein einzig Volk von Brüdern" -- Schiller
      csToken("ein"),
      csToken("einzig"),
      csToken("Volk"),
      csToken("von")
    ),
    Arrays.asList( // "Lieber den Spatz in der Hand"
      csToken("den"),
      csToken("Spatz"),
      csToken("in")
    ),
    Arrays.asList(
      token("kein"),
      csToken("schöner"),
      csToken("Land")  // https://de.wikipedia.org/wiki/Kein_sch%C3%B6ner_Land
    ),
    Arrays.asList(
      tokenRegex("die|der|das"),
      csRegex("Anfang|Mitte|Ende"),
      csRegex("Januar|Jänner|Februar|März|April|Mai|Ju[ln]i|August|September|Oktober|November|Dezember|[12][0-9]{3}")
    ),
    Arrays.asList( // Waren das schwierige Entscheidungen?
      csRegex("Ist|Sind|War|Waren|Macht|Wird|Werden"),
      csToken("das"),
      new PatternTokenBuilder().posRegex("ADJ:NOM.*").min(0).build(),
      posRegex("SUB:NOM.*"),
      posRegex("PKT|KON:NEB|PRP.+")// "Ist das Kunst?" / "Ist das Kunst oder Abfall?" / "Sind das Eier aus Bodenhaltung"
    ),
    Arrays.asList( // Soll das Demokratie sein?
      posRegex("SENT_START|PKT|KON:NEB"),
      regex("soll|sollen|sollte|wird|werden|würde|kann|können|könnte|muss|müssen|müsste"),
      csToken("das"),
      new PatternTokenBuilder().posRegex("ADJ:NOM.*").min(0).build(),
      posRegex("SUB:NOM.*"),
      csRegex("sein|werden")
    ),
    Arrays.asList( // Hat das Spaß gemacht?
      posRegex("SENT_START|PKT|KON:NEB"),
      regex("hat|hatte"),
      csToken("das"),
      new PatternTokenBuilder().posRegex("ADJ:NOM.*").min(0).build(),
      csRegex("Spa(ß|ss)|Freude|Sinn|Mehrwert"),
      csRegex("gemacht|ergeben|gestiftet")
    ),
    Arrays.asList( // Eine Lösung die Spaß macht
      regex("die|der|das"),
      new PatternTokenBuilder().posRegex("ADJ:NOM.*").min(0).build(),
      csRegex("Spa(ß|ss)|Freude|Sinn|Mehrwert"),
      new PatternTokenBuilder().tokenRegex("machen|schaffen|stiften|ergeben").matchInflectedForms().build()
    ),
    Arrays.asList( // Soll das Spaß machen?
      posRegex("SENT_START|PKT|KON:NEB"),
      regex("soll|sollte|wird|würde|kann|lönnte"),
      csToken("das"),
      new PatternTokenBuilder().posRegex("ADJ:NOM.*").min(0).build(),
      csRegex("Spa(ß|ss)|Freude|Sinn|Mehrwert"),
      csRegex("machen|stiften|ergeben")
    ),
    Arrays.asList( // Die Präsent AG ("Theater AG" is found via DE_COMPOUNDS)
      csRegex("[A-ZÄÖÜ].+"),
      csRegex("AG|GmbH|SE")
    ),
    Arrays.asList( // Die Otto Christ AG
      csRegex("[A-ZÄÖÜ].+"),
      csRegex("[A-ZÄÖÜ].+"),
      csRegex("AG|GmbH|SE")
    ),
    Arrays.asList(// Die Ernst Klett Schulbuch AG
      csRegex("[A-ZÄÖÜ].+"),
      csRegex("[A-ZÄÖÜ].+"),
      csRegex("[A-ZÄÖÜ].+"),
      csRegex("AG|GmbH|SE")
    ),
    Arrays.asList( // Die damalige Klett AG
      token("die"),
      new PatternTokenBuilder().posRegex("ADJ:NOM:SIN:FEM.*").csTokenRegex("[a-zäöü].+").min(0).build(),
      csRegex("[A-ZÄÖÜ].+"),
      csRegex("AG|GmbH|SE")
    ),
    Arrays.asList( // Die damalige Ernst Klett AG
      token("die"),
      new PatternTokenBuilder().posRegex("ADJ:NOM:SIN:FEM.*").csTokenRegex("[a-zäöü].+").min(0).build(),
      csRegex("[A-ZÄÖÜ].*"),
      csRegex("[A-ZÄÖÜ].*"),
      csRegex("AG|GmbH|SE")
    ),
    Arrays.asList( // Die damalige Ernst Klett Schulbuch AG
      token("die"),
      new PatternTokenBuilder().posRegex("ADJ:NOM:SIN:FEM.*").csTokenRegex("[a-zäöü].+").min(0).build(),
      csRegex("[A-ZÄÖÜ].*"),
      csRegex("[A-ZÄÖÜ].*"),
      csRegex("[A-ZÄÖÜ].*"),
      csRegex("AG|GmbH|SE")
    ),
    Arrays.asList(
      // like above, but with ":", as we don't interpret this as a sentence start (but it often is)
      csRegex("Meist(ens)?|Oft(mals)?|Häufig|Selten"),
      tokenRegex("sind|waren|ist"),
      token("das"),
      posRegex("SUB:.*") // Meistens sind das Frauen, die damit besser umgehen können.
    ),
    Arrays.asList(
      token("des"),
      token("Lied"),
      token("ich") // Wes Brot ich ess, des Lied ich sing
    ),
    Arrays.asList( // Es ist einige Grad kälter (see example on https://www.duden.de/rechtschreibung/Grad)
      token("einige"),
      token("Grad")
    ),
    Arrays.asList( // Ein dickes Danke an alle die ...
      token("ein"),
      posRegex("ADJ:.+"),
      token("Danke")
    ),
    Arrays.asList(
      pos(JLanguageTool.SENTENCE_START_TAGNAME),
      tokenRegex("D(a|ie)s"),
      posRegex("VER:[123]:.*"),
      posRegex("SUB:NOM:.*")// "Das erfordert Können und..." / "Dies bestätigte Polizeimeister Huber"
    ),
    Arrays.asList(
      // like above, but with ":", as we don't interpret this as a sentence start (but it often is)
      token(":"),
      tokenRegex("D(a|ie)s"),
      posRegex("VER:[123]:.*"),
      posRegex("SUB:NOM:.*")// "Das erfordert Können und..." / "Dies bestätigte Polizeimeister Huber"
    ),
    Arrays.asList(
      posRegex("ART:.+"), // "Das wenige Kilometer breite Tal"
      posRegex("ADJ:.+"),
      tokenRegex("(Kilo|Zenti|Milli)?meter|Jahre|Monate|Wochen|Tage|Stunden|Minuten|Sekunden")
    ),
    Arrays.asList(
      token("Van"), // https://de.wikipedia.org/wiki/Alexander_Van_der_Bellen
      token("der"),
      tokenRegex("Bellens?")
    ),
    Arrays.asList(
      token("mehrere"), // "mehrere Verwundete" http://forum.languagetool.org/t/de-false-positives-and-false-false/1516
      pos("SUB:NOM:SIN:FEM:ADJ")
    ),
    Arrays.asList(
      token("allen"),
      tokenRegex("Besitz|Mut")
    ),
    Arrays.asList(
      tokenRegex("d(ie|e[nr])|[md]eine[nr]?|(eure|unsere)[nr]?|diese[nr]?"),
      token("Top"),
      tokenRegex("\\d+")
    ),
    Arrays.asList( //"Unter diesen rief das großen Unmut hervor."
      posRegex("VER:3:SIN:.*"),
      token("das"),
      posRegex("ADJ:AKK:.*"),
      posRegex("SUB:AKK:.*"),
      pos("ZUS"),
      pos(JLanguageTool.SENTENCE_END_TAGNAME)
    ),
    Arrays.asList( // "Bei mir löste das Panik aus."
      posRegex("VER:3:SIN:.+"),
      token("das"),
      posRegex("SUB:AKK:.+"),
      pos("ZUS"),
      pos(JLanguageTool.SENTENCE_END_TAGNAME)
    ),
    Arrays.asList(
      token("Außenring"),
      token("Autobahn")
    ),
    Arrays.asList( // "Ehre, wem Ehre gebührt"
      tokenRegex("[dw]em"),
      csToken("Ehre"),
      csToken("gebührt")
    ),
    Arrays.asList(
      token("Eurovision"),
      token("Song"),
      token("Contest")
    ),
    Arrays.asList(
      token("Account"),
      tokenRegex("Managers?")
    ),
    Arrays.asList(
      token("Display"),
      tokenRegex("Ads?|Advertising")
    ),
    Arrays.asList(
      token("Private"),
      tokenRegex("Equitys?|Clouds?")
    ),
    Arrays.asList(
      token("Personal"),
      tokenRegex("Agents?|Computers?|Data|Firewalls?")
    ),
    Arrays.asList(
      token("Junge"),
      tokenRegex("Union|Freiheit|Welt|Europäische|Alternative|Volkspartei|Akademie")
    ),
    Arrays.asList( // "Das Holocaust Memorial Museum."
      posRegex("ART:.+"),
      posRegex("SUB:.+"),
      pos("UNKNOWN")
    ),
    Arrays.asList( // "Er fragte, ob das Spaß macht."
      csToken(","),
      posRegex("KON:UNT|ADV:INR"),
      csToken("das"),
      posRegex("SUB:.+"),
      posRegex("VER:3:SIN.*")
    ),
    Arrays.asList( // "Es gibt viele solcher Bilder"
      tokenRegex("viele|wenige|einige|mehrere"),
      csToken("solcher"),
      posRegex("SUB:GEN:PLU:.*")
    ),
    Arrays.asList( // "der französischen First Lady"
      tokenRegex("[dD](ie|er)"),
      csToken("First"),
      csToken("Lady")
    ),
    Arrays.asList( // "der französischen First Lady"
      tokenRegex("[dD](ie|er)"),
      posRegex("ADJ:.*"),
      csToken("First"),
      csToken("Lady")
    ),
    Arrays.asList( // "der Super Nintendo"
      tokenRegex("[dD](ie|er)"),
      csToken("Super"),
      csToken("Nintendo")
    ),
    Arrays.asList( // Firmenname
      csToken("Pizza"),
      csToken("Hut")
    ),
    Arrays.asList( // Texas und New Mexico, beides spanische Kolonien, sind
      csToken(","),
      csToken("beides"),
      new PatternTokenBuilder().posRegex("ADJ:NOM:PLU.+").min(0).build(),
      posRegex("SUB:NOM:PLU.+"),
      pos("PKT")
    ),
    Arrays.asList(
      tokenRegex("[dD]e[rn]"),
      csToken("Gold"),
      csToken("Cup")
    ),
    Arrays.asList(
      token("das"),
      tokenRegex("viele|wenige"),
      posRegex("SUB:.*")
    ),
    Arrays.asList(
      token("das"),
      posRegex("SUB:.+"),
      new PatternTokenBuilder().csToken("dauern").matchInflectedForms().build()
    ),
    Arrays.asList( // "Er verspricht allen/niemandem/jedem hohe Gewinne."
      tokenRegex("allen|(nieman|je(man)?)dem"),
      posRegex("ADJ:AKK:PLU:.*"),
      posRegex("SUB:AKK:PLU:.*")
    ),
    Arrays.asList( // "Er verspricht allen/niemandem/jedem Gewinne von über 15 Prozent."
      tokenRegex("allen|(nieman|je(man)?)dem"),
      posRegex("SUB:AKK:PLU:.*")
    ),
    Arrays.asList( // "Für ihn ist das Alltag." / "Für die Religiösen ist das Blasphemie und führt zu Aufständen."
      new PatternTokenBuilder().posRegex("PRP:.+|ADV:MOD").setSkip(2).build(),
      new PatternTokenBuilder().token("sein").matchInflectedForms().build(),
      csToken("das"),
      posRegex("SUB:NOM:.*"),
      posRegex("PKT|SENT_END|KON.*")
    ),
    Arrays.asList( // "Meistens sind das faktenfreie Behauptungen."
      regex("sind|waren|wären"),
      csToken("das"),
      posRegex("ADJ:NOM:PLU.*"),
      posRegex("SUB:NOM:PLU.*"),
      posRegex("PKT|KON.*")
    ),
    Arrays.asList( // "Meistens ist das reine Formsache."
      regex("ist|war|wär"),
      csToken("das"),
      posRegex("ADJ:NOM:SIN.*"),
      posRegex("SUB:NOM:SIN.*"),
      posRegex("PKT|KON.*")
    ),
    Arrays.asList( // "Aber ansonsten ist das erste Sahne"
      new PatternTokenBuilder().token("sein").matchInflectedForms().build(),
      csToken("das"),
      csToken("erste"),
      csToken("Sahne")
    ),
    Arrays.asList( // "Sie sagte, dass das Rache bedeuten würden", "Sie werden merken, dass das echte Nutzer sind."
      pos("KON:UNT"),
      csToken("das"),
      new PatternTokenBuilder().posRegex("ADJ:.*").min(0).build(),
      posRegex("SUB:.+"),
      new PatternTokenBuilder().tokenRegex("bedeuten|sein").matchInflectedForms().build()
    ),
    Arrays.asList( // "Sie fragte, ob das wirklich Rache bedeuten würde"
      pos("KON:UNT"),
      csToken("das"),
      pos("ADV:MOD"),
      posRegex("SUB:.+"),
      new PatternTokenBuilder().tokenRegex("bedeuten|sein").matchInflectedForms().build()
    ),
    Arrays.asList( // "Karl sagte, dass sie niemandem Bescheid gegeben habe."
      new PatternTokenBuilder().token("niemand").matchInflectedForms().build(),
      posRegex("SUB:.+")
    ),
    Arrays.asList(
      token("alles"),
      csToken("Walzer")
    ),
    Arrays.asList( // "ei der Daus"
      csToken("der"),
      csToken("Daus")
    ),
    Arrays.asList( // "Das Orange ist meine Lieblingsfarbe"
      posRegex("PRO:...:...:SIN:NEU.*"),
      csToken("Orange")
    ),
    Arrays.asList( // "Dieses rötliche Orange gefällt mir am besten"
      posRegex("PRO:...:...:SIN:NEU.*"),
      posRegex("ADJ:.+"),
      csToken("Orange")
    ),
    Arrays.asList(
      csToken("dem"),
      new PatternTokenBuilder().csToken("Achtung").setSkip(1).build(),
      new PatternTokenBuilder().csToken("schenken").matchInflectedForms().build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().csToken("schenken").matchInflectedForms().build(),
      csToken("dem"),
      csToken("Achtung")
    ),
    Arrays.asList(
      csToken("dem"),
      new PatternTokenBuilder().csToken("Rechnung").setSkip(1).build(),
      new PatternTokenBuilder().csToken("tragen").matchInflectedForms().build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().csToken("tragen").matchInflectedForms().build(),
      csToken("dem"),
      csToken("Rechnung")
    ),
    Arrays.asList(
      csToken("zum"),
      csToken("einen"),
      posRegex("ADJ:.+")
    ),
    Arrays.asList(
      token("auf"),
      csToken("die"),
      csToken("Lauer")
    ),
    Arrays.asList(
      token("dieser"),
      csToken("eine"),
      pos("SUB:NOM:SIN:MAS")
    ),
    Arrays.asList(
      token("das"),
      posRegex("SUB:DAT:.+"),
      token("vorbehalten")
    ),
    Arrays.asList( // Wenn hier einer Geld hat, dann ich.
      new PatternTokenBuilder().token("wenn").setSkip(1).build(),
      csToken("einer"),
      posRegex("SUB:AKK:.+"),
      posRegex("VER:(MOD:)?3:SIN:.+"),
      csToken(",")
    ),
    Arrays.asList( // Es ist nicht eines jeden Bestimmung
      tokenRegex("eine[rs]"),
      tokenRegex("jed(wed)?en")
    ),
    Arrays.asList( // Ich vertraue auf die Meinen.
      token("die"),
      tokenRegex("[MDS]einen")
    ),
    Arrays.asList( // Sie ist über die Maßen schön.
      csToken("über"),
      csToken("die"),
      csToken("Maßen")
    ),
    Arrays.asList( // Was nützt einem Gesundheit, wenn man sonst ein Idiot ist?
      token("was"),
      new PatternTokenBuilder().csToken("nützen").matchInflectedForms().build(),
      csToken("einem"),
      posRegex("SUB:NOM:.+")
    ),
    Arrays.asList( // Auch das hat sein Gutes.
      new PatternTokenBuilder().csToken("haben").matchInflectedForms().build(),
      csToken("sein"),
      csToken("Gutes")
    ),
    Arrays.asList( // Auch wenn es sein Gutes hatte.
      csToken("Gutes"),
      new PatternTokenBuilder().tokenRegex("haben|tun").matchInflectedForms().build()
    ),
    Arrays.asList(
      csToken("dieser"),
      csToken("einen"),
      pos("SUB:DAT:SIN:FEM")
    ),
    Arrays.asList(
      csToken("Rede"),
      csToken("und"),
      csToken("Antwort")
    ),
    Arrays.asList(
      posRegex("ABK:.+:SUB")
    ),
    Arrays.asList(
      tokenRegex("(all|je(d|glich))en"),
      csToken("Reiz")
    ),
    Arrays.asList(
      tokenRegex("wieso|ob|warum|w[ae]nn"),
      token("das"),
      tokenRegex("sinn|mehrwert"),
      tokenRegex("macht|ergibt|stiftet|bringt")
    ),
    Arrays.asList(
      tokenRegex("hat|hätte|kann|wird|dürfte|muss|soll(te)?|könnte|müsste|würde"),
      token("das"),
      token("Konsequenzen")
    ),
    Arrays.asList(
      new PatternTokenBuilder().posRegex("VER:.*[1-3]:.+").setSkip(1).build(),
      csToken("vermehrt")
    ),
    Arrays.asList( // In den Ruhr Nachrichten
      csToken("Ruhr"),
      csToken("Nachrichten")
    ),
    Arrays.asList(
      csToken("Joint"),
      tokenRegex("Ventures?|Cares?")
    ),
    Arrays.asList(
      csToken("Premier"),
      csToken("League")
    ),
    Arrays.asList(
      // Common job title
      csToken("Software"),
      tokenRegex("Engineers?|Developer[sn]?|(Back|Front)end")
    ),
    Arrays.asList(
      csToken("Mark"),
      posRegex("EIG:.*")
    ),
    Arrays.asList(
      csToken("Sales"),
      tokenRegex("Agent")
    ),
    Arrays.asList(
      csToken("Total"),
      tokenRegex("Tankstellen?")
    ),
    Arrays.asList(
      csToken("Real"),
      tokenRegex("Madrid|Valladolid|Mallorca")
    ),
    Arrays.asList( // Eng.
      csToken("Real"),
      pos("UNKNOWN")
    ),
    Arrays.asList(
      csToken("Hammer"),
      tokenRegex("Stra(ß|ss)e")
    ),
    Arrays.asList( // https://www.duden.de/rechtschreibung/Personal_Trainer
      csToken("Personal"),
      tokenRegex("Trainers?")
    ),
    Arrays.asList( // Ich wollte erstmal allen Hallo sagen.
      token("Hallo"),
      new PatternTokenBuilder().csToken("sagen").matchInflectedForms().build()
    ),
    Arrays.asList( // "ob die Deutsch sprechen"
      token("die"),
      tokenRegex("Deutsch|Englisch|Spanisch|Französisch|Russisch|Polnisch|Holländisch|Niederländisch|Portugiesisch"),
      new PatternTokenBuilder().csToken("sprechen").matchInflectedForms().build()
    ),
    Arrays.asList( // "Ein Trainer, der zum einen Fußballspiele sehr gut lesen und analysieren kann"
      token("zum"),
      token("einen"),
      posRegex("SUB:.*")
    ),
    Arrays.asList( // https://www.duden.de/suchen/dudenonline/Fake%20News
      csToken("Fake"),
      posRegex("News")
    ),
    Arrays.asList(
      tokenRegex("Steinberg|Park"),
      csToken("Apotheke")
    ),
    Arrays.asList(
      csToken("IT"),
      csToken("Finanzmagazin")
    ),
    Arrays.asList(
      csToken("Golden"),
      csToken("Gate")
    ),
    Arrays.asList( // Vielen Dank fürs Bescheid geben
      token("fürs"),
      token("Bescheid"),
      tokenRegex("geben|sagen")
    ),
    Arrays.asList( // "Los" ist ein deutsches Substantiv
      token("Los"),
      tokenRegex("Angeles|Zetas")
    ),
    Arrays.asList( // https://www.autozeitung.de/
      csToken("Auto"),
      csToken("Zeitung")
    ),
    Arrays.asList( // "Das letzte Mal war das Ende der..."
      csToken("Mal"),
      new PatternTokenBuilder().token("sein").matchInflectedForms().build(),
      csToken("das"),
      posRegex("SUB:NOM:.*")
    ),
    Arrays.asList(
      csToken("FC"), // Die FC Bayern München Hymne (Vorschlag macht keinen Sinn "FC-Bayern")
      csToken("Bayern")
    ),
    Arrays.asList(
      csToken("Super"),
      csToken("Mario")
    ),
    Arrays.asList(
      tokenRegex(".+"),
      tokenRegex(".+"),
      csToken("Toyota"),
      csToken("Motor"),
      tokenRegex("Corp(oration)?|Company")
    ),
    Arrays.asList(
      tokenRegex(".+"),
      tokenRegex(".+"),
      csToken("Metropolitan"),
      tokenRegex("Police|Community|City|Books")
    ),
    Arrays.asList(
      tokenRegex("Office|Microsoft"),
      csToken("365")
    ),
    Arrays.asList(
      csToken("Prinz"),
      tokenRegex("Charles|William")
    ),
    Arrays.asList(
      token(":"),
      csToken("D")
    ),
    Arrays.asList(
      tokenRegex("ist|war(en)?|sind|wird|werden"),
      csToken("das"),
      csToken("reine"),
      posRegex("SUB:NOM:.*")
    ),
    Arrays.asList( // Eine Android Watch
      csToken("Android"),
      tokenRegex("Wear|Watch(es)?|Smartwatch(es)?|OS")
    ),
    Arrays.asList( // "Bitte öffnen Sie die CAD.pdf"
      tokenRegex("\\w+"),
      new PatternTokenBuilder().token(".").setIsWhiteSpaceBefore(false).build(),
      new PatternTokenBuilder().tokenRegex("pdf|zip|jpe?g|gif|png|rar|mp[34]|mpe?g|avi|docx?|xlsx?|pptx?|html?").setIsWhiteSpaceBefore(false).build()
    ),
    Arrays.asList( // "Ich mache eine Ausbildung zur Junior Digital Marketing Managerin"
      new PatternTokenBuilder().tokenRegex("Junior|Senior").setSkip(3).build(),
      tokenRegex("Manager[ns]?|Managerin(nen)?|Developer(in)?")
    ),
    Arrays.asList(
      new PatternTokenBuilder().tokenRegex("Junior|Senior").build(),
      token("Software"),
      tokenRegex("Engineers?|Architects?|Managers?|Directors?")
    ),
    Arrays.asList(
      new PatternTokenBuilder().tokenRegex("Junior|Senior").build(),
      token("Engineering"),
      tokenRegex("Manager[ns]?|Directors?")
    ),
    Arrays.asList( // "Angel" is tagged like the "Die Angel" for fishing
      csToken("Business"),
      tokenRegex("Angel[ns]?|Cases?")
    ),
    Arrays.asList( // "des Manager Magazins"
      csToken("Manager"),
      tokenRegex("Magazins?")
    ),
    Arrays.asList(
      csToken("Junior"),
      tokenRegex("Suite[sn]?")
    ),
    Arrays.asList( // Deine Abt.
      tokenRegex("die|eine|unsere|meine|ihre|eure|diese|seine|deine"),
      csToken("Abt"),
      token("."),
      tokenRegex(".+")
    ),
    Arrays.asList(
      tokenRegex(".+"),
      tokenRegex(".+"),
      tokenRegex("Customer|User"),
      tokenRegex("Journeys?|Service")
    ),
    Arrays.asList(
      tokenRegex(".+"),
      tokenRegex(".+"),
      token("Hall"),
      token("of"),
      token("Fame")
    ),
    Arrays.asList( // Wir trinken ein kühles Blondes
      token("kühles"),
      token("Blondes")
    ),
    Arrays.asList(
      tokenRegex("Vitamin|Buchstabe"),
      tokenRegex("D|B|B[1-9]|B12")
    ),
    Arrays.asList( // "Bei uns im Krankenhaus betrifft das Operationssäle."
      new PatternTokenBuilder().token("betreffen").matchInflectedForms().build(),
      csToken("das"),
      posRegex("SUB:AKK:PLU:.*")
    ),
    Arrays.asList( // "Was für ein Narr"
      token("was"),
      csToken("für"),
      csToken("ein"),
      new PatternTokenBuilder().posRegex("ADJ:NOM:SIN:(MAS|NEU).*").min(0).build(),
      posRegex("SUB:NOM:SIN:(MAS|NEU)")
    ),
    Arrays.asList( // "Was für ein liebe Frau"
      token("was"),
      csToken("für"),
      csToken("eine"),
      new PatternTokenBuilder().posRegex("ADJ:NOM:SIN:FEM.*").min(0).build(),
      pos("SUB:NOM:SIN:FEM")
    ),
    Arrays.asList( // "Was war ich für ein Narr"
      token("was"),
      new PatternTokenBuilder().token("sein").matchInflectedForms().setSkip(2).build(),
      csToken("für"),
      csToken("ein"),
      new PatternTokenBuilder().posRegex("ADJ:NOM:SIN:(MAS|NEU).*").min(0).build(),
      posRegex("SUB:NOM:SIN:(MAS|NEU)")
    ),
    Arrays.asList( // "Was war sie nur für eine dumme Person"
      token("was"),
      new PatternTokenBuilder().token("sein").matchInflectedForms().setSkip(2).build(),
      csToken("für"),
      csToken("eine"),
      new PatternTokenBuilder().posRegex("ADJ:NOM:SIN:FEM.*").min(0).build(),
      pos("SUB:NOM:SIN:FEM")
    ),
    Arrays.asList( // "Wie viele Paar Schuhe braucht er?"
      csRegex("vielen?|wenigen?|einigen?"),
      csToken("Paar"),
      posRegex("SUB:NOM:PLU:...")
    ),
    Arrays.asList( // Dann macht das Sinn.
      csRegex("machte?|ergibt|ergab|stiftete?"),
      csToken("das"),
      csToken("Sinn")
    ),
    Arrays.asList( // Mir machte das Spaß
      csRegex("machte?"),
      csToken("das"),
      csRegex("Spa(ß|ss)|Freude")
    ),
    Arrays.asList( // Das sind beides Lichtschalter; Wasser und Luft sind beides Fluide.
      csRegex("sind|waren"),
      csToken("beides"),
      new PatternTokenBuilder().posRegex("ADJ:.*").min(0).build(),
      posRegex("SUB:NOM:PLU:.*")
    ),
    Arrays.asList( // Heinrich von der Haar (https://de.wikipedia.org/wiki/Heinrich_von_der_Haar)
      token("Heinrich"),
      token("von"),
      token("der"),
      csRegex("Haars?")
    ),
    Arrays.asList(
      token("Präsident"),
      token("Xi")
    ),
    Arrays.asList(
      token("Porsche"),
      token("Museum")
    ),
    Arrays.asList(
      token("Queen"),
      posRegex("EIG:.*")
    ),
    Arrays.asList(
      token("King"),
      posRegex("EIG:.*")
    ),
    Arrays.asList( // des Sturm und Drangs
      token("des"),
      token("Sturm"),
      token("und"),
      csRegex("Drangs?")
    ),
    Arrays.asList( // die Funke Mediengruppe
      token("die"),
      token("Funke"),
      token("Mediengruppe")
    ),
    Arrays.asList(
      new PatternTokenBuilder().csToken("meinen").matchInflectedForms().setSkip(3).build(),
      token("das"),
      new PatternTokenBuilder().token("wirklich").min(0).build(),
      token("Ernst")
    ),
    Arrays.asList(
      csRegex("das|es|dies"),
      csRegex("bedeutete?"),
      token("Krieg")
    ),
    Arrays.asList(
      csRegex("das|es|dies"),
      csRegex("weite"),
      token("Teile")
    ),
    Arrays.asList(
      token("hat"),
      token("das"),
      csRegex("Einfluss|Auswirkungen"),
      csRegex("auf|darauf")
    ),
    Arrays.asList( // ein Auto, das schnell fährt und in **das Menschen** gerne einsteigen
      posRegex("VER.*[123].*"),
      tokenRegex("und|oder|aber"),
      new PatternTokenBuilder().posRegex("PRP.*").min(0).build(),
      posRegex("ART:DEF.*")
    ),
    Arrays.asList( // weil man oft bei **anderen schreckliches Essen** vorgesetzt bekommt
      tokenRegex("bei|zum"),
      token("anderen"),
      posRegex("ADJ.*"),
      posRegex("SUB.*")
    ),
    Arrays.asList( // dass jeder LanguageTool benutzen sollte
      token("jeder"),
      posRegex("SUB:(AKK|DAT).*"),
      posRegex("VER.*")
    ),
    Arrays.asList( // der fließend Französisch sprechende Trudeau
      posRegex("(ART|PRO:DEM).*"),
      posRegex("ADV.*"),
      posRegex("SUB.*"),
      posRegex("PA[12].*")
    ),
    Arrays.asList( // Spricht dieser fließend Französisch
      posRegex("VER.*[123].*"),
      posRegex("(ART|PRO:DEM).*"),
      posRegex("ADV.*"),
      posRegex("SUB.*")
    ),
    Arrays.asList( // Der Deutsch Langhaar ist ein mittelgroßer Jagdhund
      token("Deutsch"),
      token("Langhaar")
    ),
    Arrays.asList( // Einige nennen das
      new PatternTokenBuilder().csToken("nennen").matchInflectedForms().build(),
      token("das"),
      posRegex("SUB:NOM:SIN:(FEM|MAS)")
    ),
    Arrays.asList( // Das ist bestimmt kein Made in Germany
      csToken("Made"),
      csToken("in"),
      csToken("Germany")
    ),
    Arrays.asList( // Des Plan de XXX
      csRegex("[A-Z].+"),
      csRegex("del?"),
      csRegex("[A-Z].+")
    ),
    Arrays.asList( // Des Plan de XXX
      csRegex("[A-Z].+"),
      csToken("de"),
      regex("l[ao]s?"),
      csRegex("[A-Z].+")
    )
  );

}
