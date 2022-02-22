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

class AgreementRuleAntiPatterns1 {

  static final List<List<PatternToken>> ANTI_PATTERNS = Arrays.asList(
    Arrays.asList(
      tokenRegex("der|des"),   // "Übernahme der früher selbständigen Gesellschaft"
      token("früher"),
      posRegex("ADJ:.*"),
      posRegex("SUB:.*")
    ),
    Arrays.asList(
      tokenRegex("der|die|das"),   // "Der solchen Einsätzen gegenüber kritische Müller ..."
      tokenRegex("solche[mn]|diese[mn]"),
      posRegex("SUB:.*"),
      token("gegenüber"),
      posRegex("ADJ:.*"),
      posRegex("SUB:.*|EIG.*")
    ),
    Arrays.asList(
      tokenRegex("des|der"),   // "des wenige Jahrzehnte zuvor verstorbenen Klostergründers"
      new PatternTokenBuilder().posRegex("ADJ:.*").min(0).build(),
      posRegex("SUB:.*|EIG.*"),
      token("zuvor"),
      posRegex("PA2:.*")
    ),
    Arrays.asList(
      token("Ehre"),  // "Ehre, wem Ehre gebührt"
      token(","),
      token("wem"),
      token("Ehre")
    ),
    Arrays.asList(
      token("in"),
      token("mehrerer"),
      token("Hinsicht")
    ),
    Arrays.asList(
      tokenRegex("der|die|das"),   // "die [daraus] jedem zukommende Freiheit", "im Lichte der diesem zukommenden Repräsentationsaufgabe"
      new PatternTokenBuilder().posRegex("ADV:.*").min(0).build(),
      tokenRegex("jedem|diesem"),
      posRegex("PA1:.*"),
      posRegex("SUB:.*")
    ),
    Arrays.asList(
      tokenRegex("spendet|macht"),  // "Macht dir das Hoffnung?"
      tokenRegex("mir|euch|dir|uns|ihnen"),
      token("das"),
      posRegex("SUB:.*SIN.*")
    ),
    Arrays.asList(
      tokenRegex("der|dem"),  // "Das Staatsoberhaupt ist der Verfassung zufolge der König."
      posRegex("SUB:.*SIN.*"),
      token("zufolge"),
      tokenRegex("der|die|das"),
      posRegex("SUB:.*SIN.*")
    ),
    Arrays.asList(
      // "die Anfang des 20. Jahrhunderts"
      tokenRegex("Anfang|Mitte|Ende"),
      tokenRegex("des"),
      tokenRegex("\\d+"),
      tokenRegex(".")
    ),
    Arrays.asList(
      // "Das verlangt reifliche Überlegung.", "Die abnehmend aufwendige Gestaltung der Portale...",
      // "Eine ausreichend genaue Bestimmung"
      tokenRegex("diese|der|die|das|ein|eine|dem|den|eine[ernm]|anderen?"),
      posRegex("PA[12]:.*VER|ADV:TMP"),
      posRegex("ADJ:.*"),
      posRegex("SUB:.*")
    ),
    Arrays.asList(
      // "Und den dritten wenige Tage später."
      tokenRegex("den|die"),
      tokenRegex("ersten?|zweiten?|dritten?|vierten?|fünften?|sechsten?|siebten?|achten?|neuten?|zehnten?|elften?|zwölften?"),
      posRegex("ADJ:.*")
    ),
    Arrays.asList(
      // "Die ersten Drei bekommen einen Preis."
      tokenRegex("den|die"),
      tokenRegex("ersten|nächsten|vorherigen|letzten"),
      csRegex("Zwei|Drei|Vier|Fünf|Sechs|Sieben|Acht|Neun|Zehn|Elf|Zwölf|Zwanzig|Dreißig|Vierzig|Fünzig|Hundert|Tausend")
    ),
    Arrays.asList(
      // "sie zog allem anderen kindliche Spiele vor"
      token("allem"),
      token("anderen")
    ),
    Arrays.asList(
      // "Von denen die meisten erst Ende des 19. Jahrhunderts"
      token("denen"),
      token("die"),
      token("meisten")
    ),
    Arrays.asList(
      // "Viele weniger bekannte Vorschläge", "Seine überwiegend raschen Walzer ...",
      // "Keiner erwähnte eigene Überprüfungen"
      new PatternTokenBuilder().posRegexWithStringException("PRO:(IND|POS).*", "eine[nm]").build(),
      posRegex("PA[12]:.*|ADV:TMP"),
      posRegex("ADJ:.*"),
      posRegex("SUB:.*")
    ),
    Arrays.asList(
      tokenRegex("der|die|manche[mr]"), // "zog sich der Düsseldorfer schwere Verletzungen zu. "
      csRegex("[A-ZÖÄÜ].*"),
      posRegex("ADJ:.*"),
      posRegex("SUB:.*")
    ),
    Arrays.asList(
      tokenRegex("einer?"),  // "Der als einer der ersten gängige Swingklischees vermied"
      token("der"),
      posRegex("ADJ:GEN:.*")
    ),
    Arrays.asList(
      tokenRegex("der|ein|eine[rnms]?|des|die"),  // "Ein lose zusammengewürfelter Haufen"
      token("lose"),
      tokenRegex("zusammengewürfelte[rnms]?")
    ),
    Arrays.asList(
      token("den"),  // Als Ersatz für den kleiner gewordenen Spielplatz.
      posRegex("ADJ:PRD:KOM"),
      posRegex("ADJ:AKK:SIN.*"),
      posRegex("SUB:AKK:SIN.*")
    ),
    Arrays.asList(
      token("die"),  // Als Ersatz für die kleiner gewordenen Spielplätze.
      posRegex("ADJ:PRD:KOM"),
      posRegex("ADJ:AKK:PLU.*"),
      posRegex("SUB:AKK:PLU.*")
    ),
    Arrays.asList(
      // "Andere weniger bekannte Vorschläge", "Ich habe mir das gerade letzte Woche zugelegt."
      posRegex("ART:.*|PRO:(POS|DEM|PER|IND).*"),
      tokenRegex("anscheinend|zunehmend|vorzugsweise|gekonnt|ausgeprägt|einige|solcher|solchen|typischerweise|hinreichend|nachgerade|vereinzelt|verheerend|hinreichend|zahlreiche|genauer|weiter|weniger|einzige|teilweise|anderen|sämtlicher|geringer|anderer|weniger|ausreichend|gerade|anhaltend|meisten"),
      posRegex("ADJ:.*"),
      posRegex("SUB:.*")
    ),
    Arrays.asList(
      posRegex("PRO:DEM:.*"),  // "Diese definiert einzelne Genres ..."
      posRegex("VER:[23]:.*"),
      posRegex("ADJ:.*"),
      posRegex("SUB:.*")
    ),
    Arrays.asList(
      tokenRegex("aufs|beides|welcher"),  // "aufs äußerste grausamer Krieg"
      posRegex("ADJ:.*"),
      posRegex("ADJ:.*"),
      posRegex("SUB:.*")
    ),
    Arrays.asList(
      tokenRegex("eine[mr]"),  // "Dieses Bild stammt von einem lange Zeit unbekannten Maler."
      pos("ADV:TMP"),
      pos("ADV:TMP"),
      posRegex("ADJ:.*"),
      posRegex("SUB:.*SIN.*")
    ),
    Arrays.asList(
      tokenRegex("zu"),  // "Sie gehörte einst zu den besten Afrikas."
      tokenRegex("den"),
      posRegex("ADJ:.*"),
      posRegex("EIG:GEN:.*")
    ),
    Arrays.asList(
      token("von"),  // "von denen viele Open-Source-Software sind"
      token("denen"),
      tokenRegex("viele|alle|einige|manche|mehrere|wenige"),
      new PatternTokenBuilder().posRegex("SUB:.*SIN:.*").setSkip(-1).build(),
      tokenRegex("sind|seien|sein|waren|wären")
    ),
    Arrays.asList(
      token("von"),  // "von denen die meisten Open-Source-Software sind"
      token("denen"),
      token("die"),
      tokenRegex("meisten|wenigsten|besten"),
      new PatternTokenBuilder().posRegex("SUB:.*SIN:.*").setSkip(-1).build(),
      tokenRegex("sind|seien|sein|waren|wären")
    ),
    Arrays.asList(
      tokenRegex("die|der|den"),  // "die späten 50er Jahre"
      tokenRegex("frühen|späten"),  // "die späten 50er Jahre"
      tokenRegex("\\d+er"),  // "die späten 50er Jahre"
      tokenRegex("Jahren?")
    ),
    Arrays.asList(
      tokenRegex("die|der|den"),  // "die wilden 90er"
      new PatternTokenBuilder().posRegex("ADJ:.*").min(0).build(),
      tokenRegex("\\d+er")
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
      tokenRegex("[,–-]"),
      token("beides"),
      new PatternTokenBuilder().posRegex("ADJ:.*").min(0).build(),
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
    Arrays.asList(
      token("Knecht"),
      token("Ruprecht")
    ),
    Arrays.asList(  // "in dem einen Jahr"
      token("dem"),
      token("einen"),
      pos("SUB:NOM:SIN:NEU")
    ),
    Arrays.asList(  // -> "Kaffee" - handled by spell checker
      tokenRegex("eine[sn]"),
      tokenRegex("Kaffes?")
    ),
    Arrays.asList(  // "Dies erlaubt Forschern, ..." aber auch "Dieses versuchten Mathematiker ..."
      pos("SENT_START"),
      posRegex("PRO:DEM:.+"),
      posRegex("VER:3:.+"),
      posRegex("SUB:(DAT|NOM):PLU.*")
    ),
    Arrays.asList(  // "Das verkündete Premierminister Miller"
      pos("SENT_START"),
      token("das"),
      token("verkündete"),
      posRegex("SUB:.*")
    ),
    Arrays.asList(  // "Das verkündete Premierminister Miller"
      token("wegen"),
      token("der"),
      token("vielen"),
      token("Arbeit")
    ),
    Arrays.asList(  // "in denen Energie steckt"
      new PatternTokenBuilder().posRegex("SENT_START|VER:AUX:[123].+").negate().build(),
      posRegex("PRP:.+"),
      new PatternTokenBuilder().posRegex("PRO:DEM:(DAT|AKK).+").tokenRegex("der|dies").matchInflectedForms().build(),
      posRegex("SUB:...:PLU.*")
    ),
    Arrays.asList(  // "ein für mich sehr peinlicher Termin"
      token("für"),
      token("mich"),
      pos("ADV:MOD"),
      posRegex("ADJ:.*"),
      posRegex("SUB:.*")
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
      regex("soll|sollte|wird|würde|kann|könnte"),
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
      csRegex("Meist(ens)?|Oft(mals)?|Häufig|Selten|Natürlich"),
      tokenRegex("sind|waren|ist"),
      token("das"),
      posRegex("SUB:.*") // Meistens sind das Frauen, die damit besser umgehen können.
    ),
    Arrays.asList( // Natürlich ist das Quatsch!
      tokenRegex("ist|war"),
      token("das"),
      token("Quatsch")
    ),
    Arrays.asList( // Eine Maßnahme die Vertrauen schafft
      tokenRegex("der|die"),
      token("Vertrauen"),
      new PatternTokenBuilder().matchInflectedForms().tokenRegex("schaffen").build()
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
      token("van"), // https://de.wikipedia.org/wiki/Alexander_Van_der_Bellen
      token("der")
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
    Arrays.asList(
      tokenRegex("d(ie|e[nr])|[md]eine[nr]?|(eure|unsere)[nr]?|diese[nr]?"),
      posRegex("(ADJ|PA[12]).+"),
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
      token("der"),
      token("viele"),
      tokenRegex("Schnee|Regen")
    ),
    Arrays.asList(
      token("Außenring"),
      token("Autobahn")
    ),
    Arrays.asList(
      tokenRegex("Senior|Junior"),
      tokenRegex("Leaders?"),
      tokenRegex("Days?")
    ),
    Arrays.asList(
      // ich habe meine Projektidee (die riesiges finanzielles Potenzial hat) an einen Unternehmenspräsidenten geschickt
      posRegex("SUB.*(FEM|PLU).*|EIG.*FEM.*|UNKNOWN"),
      token("("),
      token("die")
    ),
    Arrays.asList(
      posRegex("SUB.*MAS.*|EIG.*MAS.*|UNKNOWN"),
      token("("),
      token("de[rm]")
    ),
    Arrays.asList(
      posRegex("SUB.*NEU.*|EIG.*NEU.*|UNKNOWN"),
      token("("),
      token("das")
    ),
    Arrays.asList(
      pos("KON:UNT"), // "dass das komplett verschiedene Dinge sind"
      tokenRegex("der|das|dies"),
      new PatternTokenBuilder().pos("ADJ:PRD:GRU").min(0).build(),
      posRegex("ADJ.*PLU.*SOL|PA2.*PLU.*SOL:VER"),
      posRegex("SUB.*PLU.*")
    ),
    Arrays.asList(
      pos("KON:UNT"), // "ob die wirklich zusätzliche Gebühren abdrücken"
      token("die"),
      new PatternTokenBuilder().pos("ADJ:PRD:GRU").min(0).build(),
      posRegex("ADJ.*(NOM|AKK):PLU.*SOL|PA2.*(NOM|AKK):PLU.*SOL:VER"),
      posRegex("SUB.*(NOM|AKK):PLU.*")
    ),
    Arrays.asList(
      tokenRegex("Ende|Mitte|Anfang"), // "Ende 1923"
      tokenRegex("1[0-9]{3}|20[0-9]{2}")
    ),
    Arrays.asList(
      tokenRegex("dann|so"),
      token("bedarf"),
      tokenRegex("das|dies")
    ),
    Arrays.asList(
      posRegex("ART.*|PRO:POS.*"),
      posRegex("ADJ.*|PA[12].*"),
      tokenRegex("Windows|iOS"),
      tokenRegex("\\d+")
    ),
    Arrays.asList(
      // Die letzte unter Windows 98 lauffähige Version ist 5.1.
      posRegex("ART.*|PRO:POS.*"),
      posRegex("ADJ.*|PA[12].*"),
      posRegex("ADJ.*|PA[12].*"),
      tokenRegex("Windows|iOS"),
      tokenRegex("\\d+")
    ),
    Arrays.asList(
      posRegex("ART.*|PRO:POS.*"),
      tokenRegex("Windows|iOS"),
      tokenRegex("\\d+")
    ),
    // wird empfohlen, dass Unternehmen die gefährliche Güter benötigen ...
    Arrays.asList(
      token("dass"),
      new PatternTokenBuilder().posRegex("ADJ.*|PA[12].*").min(0).build(),
      posRegex("SUB:.*PLU.*"),
      token("die"),
      posRegex("ADJ.*|PA[12].*"),
      posRegex("SUB:.*"),
      posRegex("VER:.*")
    ),
    Arrays.asList( // des Handelsblatt Research Institutes
      csToken("Handelsblatt"),
      csToken("Research"),
      csRegex("Institute?s?")
    ),
    Arrays.asList( // Ich arbeite bei der Shop Apotheke im Vertrieb
      csToken("Shop"),
      csToken("Apotheke")
    ),
    Arrays.asList( // In den Prime Standard
      csToken("Prime"),
      csToken("Standard")
    ),
    Arrays.asList( // Die Nord Stream 2 AG
      csToken("Nord"),
      csToken("Stream")
    ),
    Arrays.asList( // Ein Mobiles Einsatzkommando
      posRegex("ART.*|PRO:POS.*"),
      csToken("Mobiles"),
      csToken("Einsatzkommando")
    ),
    Arrays.asList( // Die Gen Z
      posRegex("ART.*|PRO:POS.*"),
      csToken("Gen"),
      tokenRegex("[XYZ]")
    ),
    Arrays.asList( // Das veranlasste Bürgermeister Adam
      tokenRegex("das|dies"),
      csToken("veranlasste"),
      posRegex("SUB.*")
    ),
    // TODO: comment in
    // Arrays.asList(
    //   // die gegnerischen Shooting Guards
    //   posRegex("ART.*NOM:PLU"),
    //   posRegex("(ADJ|PA[12]).*NOM:PLU.*"),
    //   posRegex("SUB.*SIN.*"),
    //   new PatternTokenBuilder().posRegex("UNKNOWN").tokenRegex("(?i)[A-ZÄÖÜ].+").build()
    // ),
    // Arrays.asList(
    //   // die gegnerischen Shooting Guards
    //   posRegex("ART.*GEN:PLU"),
    //   posRegex("(ADJ|PA[12]).*GEN:PLU.*"),
    //   posRegex("SUB.*SIN.*"),
    //   new PatternTokenBuilder().posRegex("UNKNOWN").tokenRegex("(?i)[A-ZÄÖÜ].+").build()
    // ),
    // Arrays.asList(
    //   // die gegnerischen Shooting Guards
    //   posRegex("ART.*DAT:PLU"),
    //   posRegex("(ADJ|PA[12]).*DAT:PLU.*"),
    //   posRegex("SUB.*SIN.*"),
    //   new PatternTokenBuilder().posRegex("UNKNOWN").tokenRegex("(?i)[A-ZÄÖÜ].+").build()
    // ),
    // Arrays.asList(
    //   // die gegnerischen Shooting Guards
    //   posRegex("ART.*AKK:PLU"),
    //   posRegex("(ADJ|PA[12]).*AKK:PLU.*"),
    //   posRegex("SUB.*SIN.*"),
    //   new PatternTokenBuilder().posRegex("UNKNOWN").tokenRegex("(?i)[A-ZÄÖÜ].+").build()
    // ),
    // Arrays.asList(
    //   // den leidenschaftlichen Lobpreis der texanischen Gateway Church aus
    //   posRegex("ART.*DAT:SIN.*"),
    //   posRegex("(ADJ|PA[12]).*DAT:SIN.*"),
    //   posRegex("SUB.*SIN.*"),
    //   new PatternTokenBuilder().posRegex("UNKNOWN").tokenRegex("(?i)[A-ZÄÖÜ].+").build()
    // ),
    // Arrays.asList(
    //   // den leidenschaftlichen Lobpreis des texanischen Gateway Church aus
    //   posRegex("ART.*GEN:SIN.*"),
    //   posRegex("(ADJ|PA[12]).*GEN:SIN.*"),
    //   posRegex("SUB.*SIN.*"),
    //   new PatternTokenBuilder().posRegex("UNKNOWN").tokenRegex("(?i)[A-ZÄÖÜ].+").build()
    // ),
    // Arrays.asList(
    //   // den leidenschaftlichen Lobpreis des texanischen Gateway Church aus
    //   posRegex("ART.*NOM:SIN.*"),
    //   posRegex("(ADJ|PA[12]).*NOM:SIN.*"),
    //   posRegex("SUB.*SIN.*"),
    //   new PatternTokenBuilder().posRegex("UNKNOWN").tokenRegex("(?i)[A-ZÄÖÜ].+").build()
    // ),
    // Arrays.asList(
    //   // den leidenschaftlichen Lobpreis des texanischen Gateway Church aus
    //   posRegex("ART.*AKK:SIN.*"),
    //   posRegex("(ADJ|PA[12]).*AKK:SIN.*"),
    //   posRegex("SUB.*SIN.*"),
    //   new PatternTokenBuilder().posRegex("UNKNOWN").tokenRegex("(?i)[A-ZÄÖÜ].+").build()
    // ),
    Arrays.asList(
      // Von der ersten Spielminute an machten die Münsteraner Druck und ...
      new PatternTokenBuilder().matchInflectedForms().tokenRegex("machen").build(),
      token("die"),
      posRegex("SUB.*PLU.*"),
      tokenRegex("Druck")
    ),
    Arrays.asList(
      // Im Tun zu sein verhindert Prokrastination.
      token("zu"),
      token("sein"),
      posRegex("VER:3:SIN.*")
    ),
    Arrays.asList(
      tokenRegex("Ende|Mitte|Anfang"), // "Ende letzten Jahres" "Ende der 50er Jahre"
      new PatternTokenBuilder().posRegex("ART:DEF:GEN:.*").min(0).build(),
      new PatternTokenBuilder().posRegex("ADJ.*:(GEN|DAT):.*|ZAL").matchInflectedForms().tokenRegex("dieser|(vor)?letzter|[0-9]+er").build(),
      tokenRegex("Woche|Monats|Jahr(es?|zehnts|hunderts|tausends)")
    ),
    Arrays.asList(
      token("das"),
      csToken("Boostern")
    ),
    Arrays.asList(
      // Das Zeit.de-CMS / Das Zeit.de CMS
      token("das"),
      new PatternTokenBuilder().posRegex("(ADJ|PA[12]).+").min(0).build(),
      csToken("Zeit"),
      csToken("."),
      tokenRegex("de.*")
    ),
    Arrays.asList(
      token("das"),
      csToken("verlangte"),
      tokenRegex("Ruhe|Zeit|Geduld")
    ),
    Arrays.asList(
      csToken("BMW"),
      token("ConnectedDrive")
    ));

}
