/* LanguageTool, a natural language style checker
 * Copyright (C) 2022 Daniel Naber (http://www.danielnaber.de)
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

import java.util.List;

import static java.util.Arrays.asList;
import static org.languagetool.rules.de.AgreementRuleAntiPatterns1.MONTH_NAMES_REGEX;
import static org.languagetool.rules.patterns.PatternRuleBuilderHelper.*;

class AgreementRuleAntiPatterns3 {

  static final List<List<PatternToken>> ANTI_PATTERNS = asList(
    asList(
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
    asList(
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
    asList(  // "wird das schwere Konsequenzen haben"
      token("das"),
      token("schwere"),
      token("Konsequenzen")
    ),
    asList(  // "der Chaos Computer Club"
      token("der"),
      token("Chaos"),
      token("Computer"),
      token("Club")
    ),
    asList(  // "der Echo Show" (Amazon device)
      token("Echo"),
      tokenRegex("Show|Dot")
    ),
    asList(  // "In einem App Store"
      tokenRegex("App|Play"),
      token("Store")
    ),
    asList(
      token("Knecht"),
      token("Ruprecht")
    ),
    asList(  // "in dem einen Jahr"
      token("dem"),
      token("einen"),
      pos("SUB:NOM:SIN:NEU")
    ),
    asList(  // -> "Kaffee" - handled by spell checker
      tokenRegex("eine[sn]"),
      tokenRegex("Kaffes?")
    ),
    asList(  // "Dies erlaubt Forschern, ..." aber auch "Dieses versuchten Mathematiker ..."
      pos("SENT_START"),
      posRegex("PRO:DEM:.+"),
      posRegex("VER:3:.+"),
      posRegex("SUB:(DAT|NOM):PLU.*")
    ),
    asList(  // "Das verkündete Premierminister Miller"
      pos("SENT_START"),
      token("das"),
      token("verkündete"),
      posRegex("SUB:.*")
    ),
    asList(  // "Das verkündete Premierminister Miller"
      token("wegen"),
      token("der"),
      token("vielen"),
      token("Arbeit")
    ),
    asList(  // "in denen Energie steckt"
      new PatternTokenBuilder().posRegex("SENT_START|VER:AUX:[123].+").negate().build(),
      posRegex("PRP:.+"),
      new PatternTokenBuilder().posRegex("PRO:DEM:(DAT|AKK).+").tokenRegex("der|dies").matchInflectedForms().build(),
      posRegex("SUB:...:PLU.*")
    ),
    asList(  // "ein für mich sehr peinlicher Termin"
      token("für"),
      token("mich"),
      pos("ADV:MOD"),
      posRegex("ADJ:.*"),
      posRegex("SUB:.*")
    ),
    asList(  // "für den Mailänder Bischofssitz"
      posRegex("PRP:.+"),
      new PatternTokenBuilder().posRegex("PRO:DEM:(DAT|AKK).+").tokenRegex("der|dies").matchInflectedForms().build(),
      csRegex("[A-ZÄÖÜ].+er"),
      new PatternTokenBuilder().posRegex("SUB.+").build()
    ),
    asList(
      posRegex("PRP:.+"),
      posRegex("PRO:DEM:(DAT|AKK).+"),
      posRegex("PA2:(DAT|AKK).+"),
      posRegex("SUB:(DAT|AKK):.*")
    ),
    asList( // Artikel 34 setzt dem bestimmte Formen gleich
      posRegex("VER:.*[123].*"),
      posRegex("PRO:DEM:DAT:SIN:NEU.*"),
      posRegex("PA2:AKK:PLU.+"),
      posRegex("SUB:AKK:PLU.+")
    ),
    asList( // Er stellt dieses interessierten Domänen zur Verfügung
      posRegex("VER:.*[123].*"),
      posRegex("PRO:DEM:AKK:SIN:NEU.*"),
      posRegex("PA2:DAT:PLU.+"),
      posRegex("SUB:DAT:PLU.+")
    ),
    asList(
      pos("ADJ:PRD:KOM"),
      csToken("als"),
      regex("d(er|ie|as)"),
      posRegex(".+:GEN:.+")
    ),
    asList(  // "Wir bereinigen das nächsten Dienstag."
      posRegex("VER:.*|UNKNOWN"),
      token("das"),
      csRegex("(über)?nächste[ns]?|kommende[ns]?|(vor)?letzten"),
      csRegex(MONTH_NAMES_REGEX + "|Montag|D(ien|onner)stag|Mittwoch|Freitag|S(ams|onn)tag|Sonnabend|Woche|Monat|Jahr|Morgens?|Abends|Übermorgen|Mittags?|Nachmittags?|Vormittags?|Spätabends?|Nachts?")
    ),
    asList(  // "Wir releasen das Montag.", "Wir präsentierten das Januar."
      posRegex("VER:.*|UNKNOWN"),
      csToken("das"),
      csRegex(MONTH_NAMES_REGEX + "|Montags?|D(ien|onner)stags?|Mittwochs?|Freitags?|S(ams|onn)tags?|Sonnabends?|Morgens?|Abends|Übermorgen|Mittags?|Nachmittags?|Vormittags?|Spätabends?|Nachts?")
    ),
    asList(  // "Kannst du das Mittags machen?"
      token("das"),
      tokenRegex(MONTH_NAMES_REGEX + "|Montags?|D(ien|onner)stags?|Mittwochs?|Freitags?|S(ams|onn)tags?|Sonnabends?|Morgens?|Abends|Übermorgen|Mittags?|Nachmittags?|Vormittags?|Spätabends?|Nachts?"),
      posRegex("VER:.*|UNKNOWN")
    ),
    asList(  // "Kannst du das nächsten Monat machen?"
      token("das"),
      tokenRegex("(über)?nächste[ns]?|kommende[ns]?|(vor)?letzten|vorigen"),
      csRegex(MONTH_NAMES_REGEX + "|Montag|D(ien|onner)stag|Mittwoch|Freitag|S(ams|onn)tag|Sonnabend|Woche|Monat|Jahr|Morgens?|Abends|Übermorgen|Mittags?|Nachmittags?|Vormittags?|Spätabends?|Nachts?"),
      posRegex("VER:.*|UNKNOWN")
    ),
    asList(
      token("das"),
      csRegex("Zufall|Sinn|Spa(ß|ss)|Freude"),
      token("?")
    ),
    asList(
      csRegex("w[äa]r|ist|sei"),
      token("das"),
      csRegex("Zufall|Spa(ß|ss)"),
      csRegex("\\.|\\?|!|,|…")
    ),
    asList(
      // Dann sei das Zufall gewesen
      csRegex("w[äa]r|ist|sei"),
      token("das"),
      csRegex("Zufall|Spa(ß|ss)"),
      csRegex("gewesen")
    ),
    asList(
       // "War das Zufall, dass es ging?"
      token("das"),
      csRegex("Zufall|Sinn|Spa(ß|ss)"),
      csToken(",")
    ),
    asList(
      token("in"),
      tokenRegex("d(ies)?em"),
      token("Fall"),
      tokenRegex("(?i:hat(te)?)"),
      token("das")
    ),
    asList( // "So hatte das Vorteile|Auswirkungen|Konsequenzen..."
      posRegex("ADV:.+"),
      tokenRegex("(?i:hat(te)?)"),
      csToken("das")
    ),
    asList(
      tokenRegex("von|bei"),
      csRegex("vielen|allen|etlichen"),
      posRegex("PA2:.*|ADJ:AKK:PLU:.*")  // "ein von vielen bewundertes Haus" / "Das weckte bei vielen ungute Erinnerungen."
    ),
    asList(
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
    asList(
      token("für"),
      csRegex("(viele|etliche|alle|[dm]ich|ihn|sie|uns|andere|jeden)"),
      posRegex("ADJ:NOM:.*")  // "Ein für viele wichtiges Anliegen."
    ),
    asList(
      new PatternTokenBuilder().tokenRegex("flö(ß|ss)en|machen|jagen").matchInflectedForms().build(),
      csRegex("einem|jedem|keinem"),
      csToken("Angst")  // "Dinge, die/ Etwas, das einem Angst macht"
    ),
    asList(
      // Kann ja jeder weiter Maske tragen, der will?
      csRegex("jede[sr]?"),
      csToken("weiter"),
      posRegex("SUB.*")
    ),
    asList(
      tokenRegex("einem|jedem|keinem"),
      csToken("Angst"),  // "Was einem Angst macht"
      new PatternTokenBuilder().tokenRegex("machen|ein(flö(ß|ss)en|jagen)").matchInflectedForms().build()
    ),
    asList(
      token("einem"),
      csToken("geschenkten"),
      csToken("Gaul")
    ),
    asList( // "Wir wollen sein ein einzig Volk von Brüdern" -- Schiller
      csToken("ein"),
      csToken("einzig"),
      csToken("Volk"),
      csToken("von")
    ),
    asList( // "Lieber den Spatz in der Hand"
      csToken("den"),
      csToken("Spatz"),
      csToken("in")
    ),
    asList(
      token("kein"),
      csToken("schöner"),
      csToken("Land")  // https://de.wikipedia.org/wiki/Kein_sch%C3%B6ner_Land
    ),
    asList(
      tokenRegex("die|der|das"),
      csRegex("Anfang|Mitte|Ende"),
      csRegex(MONTH_NAMES_REGEX + "|[12][0-9]{3}")
    ),
    asList( // Waren das schwierige Entscheidungen?
      csRegex("Ist|Sind|War|Waren|Macht|Wird|Werden"),
      csToken("das"),
      new PatternTokenBuilder().posRegex("ADJ:NOM.*").min(0).build(),
      posRegex("SUB:NOM.*"),
      posRegex("PKT|KON:NEB|PRP.+")// "Ist das Kunst?" / "Ist das Kunst oder Abfall?" / "Sind das Eier aus Bodenhaltung"
    ),
    asList( // Soll das Demokratie sein?
      posRegex("SENT_START|PKT|KON:NEB"),
      regex("soll|sollen|sollte|wird|werden|würde|kann|können|könnte|muss|müssen|müsste"),
      csToken("das"),
      new PatternTokenBuilder().posRegex("ADJ:NOM.*").min(0).build(),
      posRegex("SUB:NOM.*"),
      csRegex("sein|werden")
    ),
    asList( // Hat das Spaß gemacht?
      posRegex("SENT_START|PKT|KON:NEB"),
      regex("hat|hatte"),
      csToken("das"),
      new PatternTokenBuilder().posRegex("ADJ:NOM.*").min(0).build(),
      csRegex("Spa(ß|ss)|Freude|Sinn|Mehrwert"),
      csRegex("gemacht|ergeben|gestiftet")
    ),
    asList( // Eine Lösung die Spaß macht
      regex("die|der|das"),
      new PatternTokenBuilder().posRegex("ADJ:NOM.*").min(0).build(),
      csRegex("Spa(ß|ss)|Freude|Sinn|Mehrwert"),
      new PatternTokenBuilder().tokenRegex("machen|schaffen|stiften|ergeben").matchInflectedForms().build()
    ),
    asList( // Soll das Spaß machen?
      posRegex("SENT_START|PKT|KON:NEB"),
      regex("soll|sollte|wird|würde|kann|könnte"),
      csToken("das"),
      new PatternTokenBuilder().posRegex("ADJ:NOM.*").min(0).build(),
      csRegex("Spa(ß|ss)|Freude|Sinn|Mehrwert"),
      csRegex("machen|stiften|ergeben")
    ),
    asList( // Die Präsent AG ("Theater AG" is found via DE_COMPOUNDS)
      csRegex("[A-ZÄÖÜ].+"),
      csRegex("AG|GmbH|SE")
    ),
    asList( // Die Otto Christ AG
      csRegex("[A-ZÄÖÜ].+"),
      csRegex("[A-ZÄÖÜ].+"),
      csRegex("AG|GmbH|SE|KG")
    ),
    asList( // Die Otto Christ AG
      posRegex("ART.*"),
      csRegex("[A-ZÄÖÜ].+"),
      csRegex("[A-ZÄÖÜ].+"),
      csRegex("AG|GmbH|SE|KG")
    ),
    asList(// Die Ernst Klett Schulbuch AG
      csRegex("[A-ZÄÖÜ].+"),
      csRegex("[A-ZÄÖÜ].+"),
      csRegex("[A-ZÄÖÜ].+"),
      csRegex("AG|GmbH|SE|KG")
    ),
    asList( // Die damalige Klett AG
      token("die"),
      new PatternTokenBuilder().posRegex("ADJ:NOM:SIN:FEM.*").csTokenRegex("[a-zäöü].+").min(0).build(),
      csRegex("[A-ZÄÖÜ].+"),
      csRegex("AG|GmbH|SE")
    ),
    asList( // Die damalige Ernst Klett AG
      token("die"),
      new PatternTokenBuilder().posRegex("ADJ:NOM:SIN:FEM.*").csTokenRegex("[a-zäöü].+").min(0).build(),
      csRegex("[A-ZÄÖÜ].*"),
      csRegex("[A-ZÄÖÜ].*"),
      csRegex("AG|GmbH|SE")
    ),
    asList( // Die damalige Ernst Klett Schulbuch AG
      token("die"),
      new PatternTokenBuilder().posRegex("ADJ:NOM:SIN:FEM.*").csTokenRegex("[a-zäöü].+").min(0).build(),
      csRegex("[A-ZÄÖÜ].*"),
      csRegex("[A-ZÄÖÜ].*"),
      csRegex("[A-ZÄÖÜ].*"),
      csRegex("AG|GmbH|SE")
    ),
    asList(
      // like above, but with ":", as we don't interpret this as a sentence start (but it often is)
      csRegex("Meist(ens)?|Oft(mals)?|Häufig|Selten|Natürlich"),
      tokenRegex("sind|waren|ist"),
      token("das"),
      posRegex("SUB:.*") // Meistens sind das Frauen, die damit besser umgehen können.
    ),
    asList( // Natürlich ist das Quatsch!
      tokenRegex("ist|war"),
      token("das"),
      token("Quatsch")
    ),
    asList( // Eine Maßnahme die Vertrauen schafft
      tokenRegex("der|die"),
      token("Vertrauen"),
      new PatternTokenBuilder().matchInflectedForms().tokenRegex("schaffen").build()
    ),
    asList(
      token("des"),
      token("Lied"),
      token("ich") // Wes Brot ich ess, des Lied ich sing
    ),
    asList( // Es ist einige Grad kälter (see example on https://www.duden.de/rechtschreibung/Grad)
      token("einige"),
      token("Grad")
    ),
    asList( // Ein dickes Danke an alle die ...
      token("ein"),
      posRegex("ADJ:.+"),
      token("Danke")
    ),
    asList(
      pos(JLanguageTool.SENTENCE_START_TAGNAME),
      tokenRegex("D(a|ie)s"),
      posRegex("VER:[123]:.*"),
      posRegex("SUB:NOM:.*")// "Das erfordert Können und..." / "Dies bestätigte Polizeimeister Huber"
    ),
    asList(
      // like above, but with ":", as we don't interpret this as a sentence start (but it often is)
      token(":"),
      tokenRegex("D(a|ie)s"),
      posRegex("VER:[123]:.*"),
      posRegex("SUB:NOM:.*")// "Das erfordert Können und..." / "Dies bestätigte Polizeimeister Huber"
    ),
    asList(
      posRegex("ART:.+"), // "Das wenige Kilometer breite Tal"
      posRegex("ADJ:.+"),
      tokenRegex("(Kilo|Zenti|Milli)?meter|Jahre|Monate|Wochen|Tage|Stunden|Minuten|Sekunden")
    ),
    asList(
      token("van"), // https://de.wikipedia.org/wiki/Alexander_Van_der_Bellen
      token("der")
    ),
    asList(
      tokenRegex("mehrere|etliche"), // "mehrere Verwundete" http://forum.languagetool.org/t/de-false-positives-and-false-false/1516
      pos("SUB:NOM:SIN:FEM:ADJ")
    ),
    asList(
      token("allen"),
      tokenRegex("Besitz|Mut")
    ),
    asList(
      tokenRegex("d(ie|e[nr])|[md]eine[nr]?|(eure|unsere)[nr]?|diese[nr]?"),
      token("Top"),
      tokenRegex("\\d+")
    ),
    asList(
      tokenRegex("d(ie|e[nr])|[md]eine[nr]?|(eure|unsere)[nr]?|diese[nr]?"),
      posRegex("(ADJ|PA[12]).+"),
      token("Top"),
      tokenRegex("\\d+")
    ),
    asList( //"Unter diesen rief das großen Unmut hervor."
      posRegex("VER:3:SIN:.*"),
      token("das"),
      posRegex("ADJ:AKK:.*"),
      posRegex("SUB:AKK:.*"),
      pos("ZUS"),
      pos(JLanguageTool.SENTENCE_END_TAGNAME)
    ),
    asList( // "Bei mir löste das Panik aus."
      posRegex("VER:3:SIN:.+"),
      token("das"),
      posRegex("SUB:AKK:.+"),
      pos("ZUS"),
      pos(JLanguageTool.SENTENCE_END_TAGNAME)
    ),
    asList(
      token("der"),
      token("viele"),
      tokenRegex("Schnee|Regen")
    ),
    asList(
      tokenRegex("der|die"),
      tokenRegex("vielen?"),
      token("Aufmerksamkeit")
    ),
    asList(
      tokenRegex("Au(ß|ss)enring"),
      token("Autobahn")
    ),
    asList(
      tokenRegex("Senior|Junior"),
      tokenRegex("Leaders?"),
      tokenRegex("Days?")
    ),
    asList(
      // ich habe meine Projektidee (die riesiges finanzielles Potenzial hat) an einen Unternehmenspräsidenten geschickt
      posRegex("SUB.*(FEM|PLU).*|EIG.*FEM.*|UNKNOWN"),
      token("("),
      token("die")
    ),
    asList(
      // Wir sind immer offen für Mitarbeiter die Teil eines der traditionellsten Malerbetriebe auf dem Platz Zürich werden möchten.
      posRegex("PRP.*"),
      posRegex("SUB.*PLU.*"),
      token("die"),
      posRegex("SUB.*SIN.*")
    ),
    asList(
      posRegex("SUB.*MAS.*|EIG.*MAS.*|UNKNOWN"),
      token("("),
      token("de[rm]")
    ),
    asList(
      posRegex("SUB.*NEU.*|EIG.*NEU.*|UNKNOWN"),
      token("("),
      token("das")
    ),
    asList(
      pos("KON:UNT"), // "dass das komplett verschiedene Dinge sind"
      tokenRegex("der|das|dies"),
      new PatternTokenBuilder().pos("ADJ:PRD:GRU").min(0).build(),
      posRegex("ADJ.*PLU.*SOL|PA2.*PLU.*SOL:VER"),
      posRegex("SUB.*PLU.*")
    ),
    asList(
      pos("KON:UNT"), // "ob die wirklich zusätzliche Gebühren abdrücken"
      token("die"),
      new PatternTokenBuilder().pos("ADJ:PRD:GRU").min(0).build(),
      posRegex("ADJ.*(NOM|AKK):PLU.*SOL|PA2.*(NOM|AKK):PLU.*SOL:VER"),
      posRegex("SUB.*(NOM|AKK):PLU.*")
    ),
    asList(
      tokenRegex("Ende|Mitte|Anfang"), // "Ende 1923"
      tokenRegex("1[0-9]{3}|20[0-9]{2}")
    ),
    asList(
      tokenRegex("dann|so"),
      token("bedarf"),
      tokenRegex("das|dies")
    ),
    asList(
      posRegex("ART.*|PRO:POS.*"),
      posRegex("ADJ.*|PA[12].*"),
      tokenRegex("Windows|iOS"),
      tokenRegex("\\d+")
    ),
    asList(
      // Die letzte unter Windows 98 lauffähige Version ist 5.1.
      posRegex("ART.*|PRO:POS.*"),
      posRegex("ADJ.*|PA[12].*"),
      posRegex("ADJ.*|PA[12].*"),
      tokenRegex("Windows|iOS"),
      tokenRegex("\\d+")
    ),
    asList(
      posRegex("ART.*|PRO:POS.*"),
      tokenRegex("Windows|iOS"),
      tokenRegex("\\d+")
    ),
    // wird empfohlen, dass Unternehmen die gefährliche Güter benötigen ...
    asList(
      token("dass"),
      new PatternTokenBuilder().posRegex("ADJ.*|PA[12].*").min(0).build(),
      posRegex("SUB:.*PLU.*"),
      token("die"),
      posRegex("ADJ.*|PA[12].*"),
      posRegex("SUB:.*"),
      posRegex("VER:.*")
    ),
    asList( // des Handelsblatt Research Institutes
      csToken("Handelsblatt"),
      csToken("Research"),
      csRegex("Institute?s?")
    ),
    asList( // Ich arbeite bei der Shop Apotheke im Vertrieb
      csToken("Shop"),
      csToken("Apotheke")
    ),
    asList( // In den Prime Standard
      csToken("Prime"),
      csToken("Standard")
    ),
    asList( // Die Nord Stream 2 AG
      csToken("Nord"),
      csToken("Stream")
    ),
    asList( // Ein Mobiles Einsatzkommando
      posRegex("ART.*|PRO:POS.*"),
      csToken("Mobiles"),
      csToken("Einsatzkommando")
    ),
    asList( // Die Gen Z
      posRegex("ART.*|PRO:POS.*"),
      csToken("Gen"),
      tokenRegex("[XYZ]")
    ),
    asList( // Das veranlasste Bürgermeister Adam
      tokenRegex("das|dies"),
      csToken("veranlasste"),
      posRegex("SUB.*")
    ),
    asList( // In einem Eins gegen Eins
      tokenRegex("ein|einem"),
      token("Eins"),
      csToken("gegen"),
      token("Eins")
    ),
    asList( // Dann musst du das Schritt für Schritt …
      tokenRegex("das|dies"),
      token("Schritt"),
      csToken("für"),
      token("Schritt")
    ),
    asList( // Das hat etliche Zeit in Anspruch genommen
      token("etliche"),
      token("Zeit")
    ),
    asList( // Ich habe auf vieles Lust
      token("auf"),
      token("vieles"),
      tokenRegex("Lust|Bock")
    ),
    asList( // Ich habe für vieles Zeit
      token("für"),
      token("vieles"),
      token("Zeit")
    ),
    asList(
      // …, kann das Infektionen möglicherweise verhindern
      posRegex("KON.*|PKT|SENT_START"),
      new PatternTokenBuilder().posRegex("ADV.*").min(0).build(),
      posRegex("VER:MOD:3:SIN.*"),
      csToken("das"),
      posRegex("SUB.*"),
      new PatternTokenBuilder().posRegex("ADV.*").min(0).max(2).build(),
      posRegex("VER:INF:.*")
    ),
    asList(
      // die gegnerischen Shooting Guards
      posRegex("(ART|PRO:POS).*NOM:PLU"),
      posRegex("(ADJ|PA[12]).*NOM:PLU.*"),
      posRegex("SUB.*SIN.*"),
      new PatternTokenBuilder().posRegex("UNKNOWN").csTokenRegex("[A-ZÖÄÜ][A-ZÖÄÜa-zöäüß\\-]+").build()
    ),
    asList(
      // die gegnerischen Shooting Guards
      posRegex("(ART|PRO:POS).*GEN:PLU"),
      posRegex("(ADJ|PA[12]).*GEN:PLU.*"),
      posRegex("SUB.*SIN.*"),
      new PatternTokenBuilder().posRegex("UNKNOWN").csTokenRegex("[A-ZÖÄÜ][A-ZÖÄÜa-zöäüß\\-]+").build()
    ),
    asList(
      // die gegnerischen Shooting Guards
      posRegex("(ART|PRO:POS).*DAT:PLU"),
      posRegex("(ADJ|PA[12]).*DAT:PLU.*"),
      posRegex("SUB.*SIN.*"),
      new PatternTokenBuilder().posRegex("UNKNOWN").csTokenRegex("[A-ZÖÄÜ][A-ZÖÄÜa-zöäüß\\-]+").build()
    ),
    asList(
      // die gegnerischen Shooting Guards
      posRegex("(ART|PRO:POS).*AKK:PLU"),
      posRegex("(ADJ|PA[12]).*AKK:PLU.*"),
      posRegex("SUB.*SIN.*"),
      new PatternTokenBuilder().posRegex("UNKNOWN").csTokenRegex("[A-ZÖÄÜ][A-ZÖÄÜa-zöäüß\\-]+").build()
    ),
    asList(
      // den leidenschaftlichen Lobpreis der texanischen Gateway Church aus
      posRegex("(ART|PRO:POS).*DAT:SIN.*"),
      posRegex("(ADJ|PA[12]).*DAT:SIN.*"),
      posRegex("SUB.*SIN.*"),
      new PatternTokenBuilder().posRegex("UNKNOWN").csTokenRegex("[A-ZÖÄÜ][A-ZÖÄÜa-zöäüß\\-]+").build()
    ),
    asList(
      // den leidenschaftlichen Lobpreis des texanischen Gateway Church aus
      posRegex("(ART|PRO:POS).*GEN:SIN.*"),
      posRegex("(ADJ|PA[12]).*GEN:SIN.*"),
      posRegex("SUB.*SIN.*"),
      new PatternTokenBuilder().posRegex("UNKNOWN").csTokenRegex("[A-ZÖÄÜ][A-ZÖÄÜa-zöäüß\\-]+").build()
    ),
    asList(
      // den leidenschaftlichen Lobpreis des texanischen Gateway Church aus
      posRegex("(ART|PRO:POS).*NOM:SIN.*"),
      posRegex("(ADJ|PA[12]).*NOM:SIN.*"),
      posRegex("SUB.*SIN.*"),
      new PatternTokenBuilder().posRegex("UNKNOWN").csTokenRegex("[A-ZÖÄÜ][A-ZÖÄÜa-zöäüß\\-]+").build()
    ),
    asList(
      // den leidenschaftlichen Lobpreis des texanischen Gateway Church aus
      posRegex("(ART|PRO:POS).*AKK:SIN.*"),
      posRegex("(ADJ|PA[12]).*AKK:SIN.*"),
      posRegex("SUB.*SIN.*"),
      new PatternTokenBuilder().posRegex("UNKNOWN").csTokenRegex("[A-ZÖÄÜ][A-ZÖÄÜa-zöäüß\\-]+").build()
    ),
    asList(
      // Von der ersten Spielminute an machten die Münsteraner Druck und ...
      new PatternTokenBuilder().matchInflectedForms().tokenRegex("machen").build(),
      token("die"),
      posRegex("SUB.*PLU.*"),
      tokenRegex("Druck")
    ),
    asList(
      // Im Tun zu sein verhindert Prokrastination.
      token("zu"),
      token("sein"),
      posRegex("VER:3:SIN.*")
    ),
    asList(
      tokenRegex("Ende|Mitte|Anfang"), // "Der Ende der achtziger Jahre umgestaltete ..."
      new PatternTokenBuilder().posRegex("ART:DEF:GEN:.*").min(0).build(),
      new PatternTokenBuilder().posRegex("ADJ.*:(GEN|DAT):.*|ZAL").build(),
      tokenRegex("Woche|Monats|Jahr(es?|zehnts|hunderts|tausends)")
    ),
    asList(
      tokenRegex("Ende|Mitte|Anfang"), // "Ende letzten Jahres" "Ende der 50er Jahre"
      new PatternTokenBuilder().posRegex("ART:DEF:GEN:.*").min(0).build(),
      new PatternTokenBuilder().matchInflectedForms().tokenRegex("dieser|(vor)?letzter|[0-9]+er").build(),
      tokenRegex("Woche|Monats|Jahr(es?|zehnts|hunderts|tausends)")
    ),
    asList(
      token("das"),
      csToken("Boostern")
    ),
    asList(
      // Das Zeit.de-CMS / Das Zeit.de CMS
      token("das"),
      new PatternTokenBuilder().posRegex("(ADJ|PA[12]).+").min(0).build(),
      csToken("Zeit"),
      csToken("."),
      tokenRegex("de.*")
    ),
    asList(
      token("das"),
      csToken("verlangte"),
      tokenRegex("Ruhe|Zeit|Geduld")
    ),
    asList(
      csToken("BMW"),
      token("ConnectedDrive")
    ),
    asList(
      // https://www.jungewirtschaft.at/
      token("die"),
      csToken("Junge"),
      csToken("Wirtschaft")
    ),
    asList(
      token("der"),
      csToken("Jungen"),
      csToken("Wirtschaft")
    ),
    asList(
      // Das passiert, weil die Schiss haben.
      token("die"),
      csRegex("Schiss|Mut|Respekt"),
      tokenRegex("haben|h[äa]tten?|zeigt?en|zollt?en")
    ),
    asList(
      // "Inwiefern soll denn das romantische Hoffnungen begründen?"
      new PatternTokenBuilder().pos("ADV:MOD+INR").setSkip(-1).build(),
      new PatternTokenBuilder().posRegex("VER.*:[123]:SIN:.*").setSkip(1).build(),
      posRegex("PRO:DEM:.*SIN.*"),
      new PatternTokenBuilder().posRegex("ADJ:.*PLU.*").min(0).build(),
      posRegex("SUB:.*PLU.*"),
      posRegex("VER.*INF:.*")
    ),
    asList(
      // 1944 eroberte diese weite Teile von Südosteuropa.
      posRegex("VER.*"),
      tokenRegex("diese[sr]?"),
      token("weite"),
      token("Teile")
    )
  );

}
