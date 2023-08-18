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

import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.patterns.PatternTokenBuilder;

import java.util.*;

import static java.util.Arrays.asList;
import static org.languagetool.rules.patterns.PatternRuleBuilderHelper.*;
import static org.languagetool.rules.patterns.PatternRuleBuilderHelper.tokenRegex;

class AgreementRuleAntiPatterns1 {

  final static String MONTH_NAMES_REGEX = "Jänner|Januar|Februar|März|April|Mai|Juni|Juli|August|September|Oktober|November|Dezember";

  static final List<List<PatternToken>> ANTI_PATTERNS = asList(
    asList(
      tokenRegex("der|des"),   // "Übernahme der früher selbständigen Gesellschaft"
      token("früher"),
      posRegex("ADJ:.*"),
      posRegex("SUB:.*")
    ),
    asList(
      posRegex("(ART|PRO):.*"),   // "Wie viele Kolleg/-innen haben sie?"
      new PatternTokenBuilder().posRegex("ADJ:.*").min(0).build(),
      posRegex("SUB:.*"),
      token("/"),
      tokenRegex("-in|-innen")
    ),
    asList(
      token("wegen"),   //  "...und hatte wegen des vielen Trinkens Kopfschmerzen." (#4695)
      token("des"),
      token("vielen"),
      posRegex("SUB:GEN:SIN:.*"),
      posRegex("SUB:GEN:.*")
    ),
    asList(
      tokenRegex("des"),   // "Chef des renommierten Institute for Fiscal Studies (IFS)"
      new PatternTokenBuilder().posRegex("ADJ:.*").min(0).build(),
      token("Institute"),
      tokenRegex("for|of")
    ),
    asList(
      token("zur"),   // "Bis zur Anfang Juni geplanten Eröffnung gebe es noch einiges zu tun."
      tokenRegex("Anfang|Ende|Mitte"),
      tokenRegex(MONTH_NAMES_REGEX),
      posRegex("PA2:.*"),
      posRegex("SUB:.*FEM.*")
    ),
    asList(  // "Jedes Grad zählt"
      token("jedes"),
      token("Grad")
    ),
    asList(
      token("euer"),
      token("Gnaden")
    ),
    asList(
      token("die"),  // "Die Adelmanns" (Nachname not tagged properly)
      new PatternTokenBuilder().pos("SUB:GEN:SIN:MAS").tokenRegex(".*manns").build()
    ),
    asList(
      token("für"),
      token("viele"),
      token("Grund"),
      token("genug")
    ),
    asList(
      token("sowas"),   // "Wir können sowas Mittwoch machen."
      tokenRegex("Montag|Dienstag|Mittwoch|Donnerstag|Freitag|Samstag|Sonntag")
    ),
    asList(
      token("bei"),   // "Es kann gut sein, dass bei sowas Probleme erkannt werden."
      token("sowas"),
      new PatternTokenBuilder().posRegex("ADJ:.*").min(0).build(),
      posRegex("SUB:.*PLU.*")
    ),
    asList(
      token("beides"),   // "Beides Grund genug, es mal zu probieren."
      token("Grund")
    ),
    asList(
      tokenRegex("der|die|den"),   // "Ein Haus für die weniger Glücklichen."
      tokenRegex("weniger|besser|mehr|schlechter"),
      posRegex("SUB:.*PLU:.*:ADJ")
    ),
    asList(
      tokenRegex("wusste|weiß"),   // "Da wusste keiner Bescheid"
      tokenRegex("keiner?"),
      token("Bescheid")
    ),
    asList(
      tokenRegex("keiner?"),  // "es braucht keiner Bescheid wissen"
      token("Bescheid"),
      token("wissen")
    ),
    asList(
      token("von"),  // "eine von manchem geforderte Übergewinnsteuer"
      tokenRegex("manche[nmr]?"),
      posRegex("PA2:.*"),
      posRegex("SUB:.*")
    ),
    asList(
      token("niemandem"),  // "hat niemandem wirkliches Leid zugefügt"
      posRegex("ADJ:NOM:SIN:NEU:.*"),
      posRegex("SUB:.*SIN.*")
    ),
    asList(   // "Eine mehrere hundert Meter lange Startbahn."
      tokenRegex("viele|mehrere"),
      pos("ZAL"),
      tokenRegex("Meter|.+meter"),
      tokenRegex("lange[ns]?|kurze[ns]?|große[ns]?|kleine[ns]?"),
      posRegex("SUB:.*")
    ),
    asList(  // "eine alles in allem spannende Geschichte"
      token("alles"),
      token("in"),
      token("allem")
    ),
    asList(  // "Man darf gespannt sein, wen Müller für"
      token("wen"),
      posRegex("EIG:.*")
    ),
    asList(  // "das Vereinslokal in welchem Zusammenkünfte"
      tokenRegex("in|zu"),
      tokenRegex("welche[nmrs]"),
      posRegex("SUB.*PLU.*")
    ),
    asList(  // "er lässt niemanden zu Wort kommen"
      token("niemanden"),
      token("zu"),
      token("Wort")
    ),
    asList(
      token("zu"),
      tokenRegex("Kopfe?|Zwecken?|Ohren|Fü(ß|ss)en|Fu(ß|ss)|Händen|Beginn|Anfang|Geld|Gesicht|Recht|Unrecht|.*stein")
    ),
    asList(   // "Zum anderen verringert Zuversicht seelische Belastungen"
      token("zum"),
      token("anderen"),
      posRegex("VER:3:SIN:(PRÄ|PRT):SFT"),
      posRegex("SUB:NOM:SIN:.*")
    ),
    asList(
      token("an"),
      token("allem"),
      token("Schuld")
    ),
    asList(
      posRegex("ART.*|PRO:POS.*"),
      token("zu"),
      tokenRegex("gleichen|gro(ß|ss)en|kleinen"),
      token("Teilen")
    ),
    asList(  //"Bald läppert sich das zu richtigem Geld zusammen."
      new PatternTokenBuilder().tokenRegex("läppern|summieren").matchInflectedForms().build(),
      token("sich"),
      posRegex("PRO:DEM.*"),
      token("zu"),
      posRegex("ADJ:DAT.*"),
      posRegex("SUB:DAT.*")
    ),
    asList(  //"Die Weimarer Parks laden ja förmlich ein zu Fotos im öffentlichen Raum."
      new PatternTokenBuilder().token("laden").matchInflectedForms().setSkip(-1).build(),
      token("ein"),
      token("zu"),
      posRegex("SUB:AKK.*")
    ),
    asList( //"Es is schwierig für mich, diese zu Sätzen zu verbinden."
      posRegex("PRO:DEM.*"),
      token("zu"),
      new PatternTokenBuilder().posRegex("ADJ:.*").min(0).build(),
      posRegex("SUB.*"),
      new PatternTokenBuilder().token("zu").min(0).build(),
      tokenRegex("verbinden|verhelfen|fähig")
    ),
    asList( //"Es kam zum einen zu technischen Problemen, zum anderen wurde es unübersichtlich."
      token("zum"),
      token("einen"),
      token("zu"),
      new PatternTokenBuilder().posRegex("ADJ:.*").min(0).build(),
      new PatternTokenBuilder().posRegex("SUB.*").setSkip(-1).build(),
      token("zum"),
      token("anderen")
    ),
    asList( //"Das Spiel wird durch den zu neuer Größe gewachsenen Torwart dominiert."
      posRegex("ART.*|PRO:POS.*"),
      token("zu"),
      new PatternTokenBuilder().posRegex("ADJ:.*").min(0).max(2).build(),
      posRegex("SUB.*"),
      posRegex("PA2.*")
    ),
    asList( //"Dort findet sich schlicht und einfach alles & das zu sagenhafter Hafenkulisse."
      tokenRegex("und|&"),
      posRegex("ART:DEF.*"),
      token("zu"),
      posRegex("ADJ:.*"),
      posRegex("SUB:.*")
    ),
    asList(  // "die zu basisdemokratischen Prozessen benötigte Mitbestimmung"
      token("die"),
      token("zu"),
      posRegex("ADJ.*"),
      posRegex("SUB:.*"),
      tokenRegex("nötigen?|benötigten?|erforderlichen?")
    ),
    asList(
      posRegex("PRO:.*|ART.*"),
      token("zu"),
      tokenRegex("wenige|viele|verschiedene|höheren|günstigeren"),
      posRegex("SUB:.*PLU.*")
    ),
    asList(
      posRegex("PRO:.*|ART.*"),
      token("zu"),
      token("Hause")
    ),
    asList(  // "Und das zu guter Qualität."
      posRegex("PRO:.*|ART.*"),
      token("zu"),
      posRegex("ADJ:DAT:SIN:FEM:GRU:SOL"),
      token("Qualität")
    ),
    asList(
      posRegex("PRO.*"),  // "Es gibt viele Stock Screener."
      posRegex("SUB:.*"),
      new PatternTokenBuilder().pos("UNKNOWN").csTokenRegex("[A-ZÖÄÜ][A-ZÖÄÜa-zöäüß\\-]+").build()
    ),
    asList(
      posRegex("PRP.*(DAT|AKK)"),  // "zur Learning Academy"
      posRegex("SUB:.*"),
      new PatternTokenBuilder().pos("UNKNOWN").csTokenRegex("[A-ZÖÄÜ][A-ZÖÄÜa-zöäüß\\-]+").build()
    ),
    asList(
      posRegex("PRP.*DAT"),  // "zur neuen Learning Academy"
      posRegex("ADJ.*DAT.*"),  
      posRegex("SUB:.*"),
      new PatternTokenBuilder().pos("UNKNOWN").csTokenRegex("[A-ZÖÄÜ][A-ZÖÄÜa-zöäüß\\-]+").build()
    ),
    asList(
      posRegex("PRP.*AKK"),  // "zur neuen Learning Academy"
      posRegex("ADJ.*AKK.*"),  
      posRegex("SUB:.*"),
      new PatternTokenBuilder().pos("UNKNOWN").csTokenRegex("[A-ZÖÄÜ][A-ZÖÄÜa-zöäüß\\-]+").build()
    ),
    asList(
      posRegex("PRO.*"),  // "Es gibt viele verschiedene Stock Screener."
      posRegex("(ADJ|PA2).*"),
      posRegex("SUB:.*"),
      new PatternTokenBuilder().pos("UNKNOWN").csTokenRegex("[A-ZÖÄÜ][A-ZÖÄÜa-zöäüß\\-]+").build()
    ),
    asList(
      tokenRegex("[(\\[]"),   // "... (ich meine Pfeil, nicht Raute) ..."
      token("ich"),
      token("meine"),
      posRegex("SUB:.*")
    ),
    asList(
      tokenRegex("ein|das"),   // "ein leichter handhabbares Logo"
      token("leichter"),
      posRegex("ADJ:NOM:SIN:NEU:GRU:IND"),
      pos("SUB:NOM:SIN:NEU")
    ),
    asList(
      tokenRegex("eine|die"),   // "eine leichter handhabbare Situation"
      token("leichter"),
      posRegex("ADJ:NOM:SIN:FEM:GRU:IND"),
      pos("SUB:NOM:SIN:FEM")
    ),
    asList(
      tokenRegex("der|die|das"),   // "Der solchen Einsätzen gegenüber kritische Müller ..."
      tokenRegex("solche[mn]|(eben)?diese[mn]"),
      posRegex("SUB:.*"),
      token("gegenüber"),
      posRegex("ADJ:.*"),
      posRegex("SUB:.*|EIG.*")
    ),
    asList(
      tokenRegex("des|der"),   // "des wenige Jahrzehnte zuvor verstorbenen Klostergründers"
      new PatternTokenBuilder().posRegex("ADJ:.*").min(0).build(),
      posRegex("SUB:.*|EIG.*"),
      token("zuvor"),
      posRegex("PA2:.*")
    ),
    asList(
      token("Ehre"),  // "Ehre, wem Ehre gebührt"
      token(","),
      token("wem"),
      token("Ehre")
    ),
    asList(
      token("in"),
      token("mehrerer"),
      token("Hinsicht")
    ),
    asList(
      tokenRegex("der|die|das"),   // "die [daraus] jedem zukommende Freiheit", "im Lichte der diesem zukommenden Repräsentationsaufgabe"
      new PatternTokenBuilder().posRegex("ADV:.*").min(0).build(),
      tokenRegex("jedem|(eben)?diesem"),
      posRegex("PA1:.*"),
      posRegex("SUB:.*")
    ),
    asList(
      tokenRegex("spendet|macht"),  // "Macht dir das Hoffnung?"
      tokenRegex("mir|euch|dir|uns|ihnen"),
      token("das"),
      posRegex("SUB:.*SIN.*")
    ),
    asList(
      tokenRegex("der|dem"),  // "Das Staatsoberhaupt ist der Verfassung zufolge der König."
      posRegex("SUB:.*SIN.*"),
      token("zufolge"),
      tokenRegex("der|die|das"),
      posRegex("SUB:.*SIN.*")
    ),
    asList(
      tokenRegex("Erkenntnis|Erkenntnissen"), // "Als Auslöser könnte den ersten Erkenntnissen zufolge ein Specht in Betracht kommen."
      token("zufolge"),
      posRegex("ART:.*")
    ),
    asList(
      // "die Anfang des 20. Jahrhunderts"
      tokenRegex("Anfang|Mitte|Ende"),
      tokenRegex("des"),
      tokenRegex("\\d+"),
      tokenRegex(".")
    ),
    asList(
      // "Das verlangt reifliche Überlegung.", "Die abnehmend aufwendige Gestaltung der Portale...",
      // "Eine ausreichend genaue Bestimmung"
      tokenRegex("(eben)?diese|der|die|das|ein|eine|dem|den|eine[ernm]|anderen?"),
      posRegex("PA[12]:.*VER|ADV:TMP"),
      posRegex("ADJ:.*"),
      posRegex("SUB:.*")
    ),
    asList(
      // "Und den dritten wenige Tage später."
      tokenRegex("den|die"),
      tokenRegex("ersten?|zweiten?|dritten?|vierten?|fünften?|sechsten?|siebten?|achten?|neuten?|zehnten?|elften?|zwölften?"),
      posRegex("ADJ:.*")
    ),
    asList(
      // "Die ersten Drei bekommen einen Preis." / "Die geheimen Sechs"
      tokenRegex("den|die"),
      tokenRegex(".+n"),
      csRegex("Zwei|Drei|Vier|Fünf|Sechs|Sieben|Acht|Neun|Zehn|Elf|Zwölf|Zwanzig|Drei(ß|ss)ig|Vierzig|Fünzig|Hundert|Tausend")
    ),
    asList(
      // "sie zog allem anderen kindliche Spiele vor"
      token("allem"),
      token("anderen")
    ),
    asList(
      // "Von denen die meisten erst Ende des 19. Jahrhunderts"
      token("denen"),
      token("die"),
      token("meisten")
    ),
    asList(
      // "Viele weniger bekannte Vorschläge", "Seine überwiegend raschen Walzer ...",
      // "Keiner erwähnte eigene Überprüfungen"
      new PatternTokenBuilder().posRegexWithStringException("PRO:(IND|POS).*", "eine[nm]").build(),
      posRegex("PA[12]:.*|ADV:TMP"),
      posRegex("ADJ:.*"),
      posRegex("SUB:.*")
    ),
    asList(
      tokenRegex("der|die|manche[mr]"), // "zog sich der Düsseldorfer schwere Verletzungen zu. "
      csRegex("[A-ZÖÄÜ].*"),
      posRegex("ADJ:.*"),
      posRegex("SUB:.*")
    ),
    asList(
      tokenRegex("einer?"),  // "Der als einer der ersten gängige Swingklischees vermied"
      token("der"),
      posRegex("ADJ:GEN:.*")
    ),
    asList(
      tokenRegex("der|ein|eine[rnms]?|des|die"),  // "Ein lose zusammengewürfelter Haufen"
      token("lose"),
      tokenRegex("zusammengewürfelte[rnms]?")
    ),
    asList(
      token("den"),  // Als Ersatz für den kleiner gewordenen Spielplatz.
      posRegex("ADJ:PRD:KOM"),
      posRegex("ADJ:AKK:SIN.*"),
      posRegex("SUB:AKK:SIN.*")
    ),
    asList(
      token("die"),  // Als Ersatz für die kleiner gewordenen Spielplätze.
      posRegex("ADJ:PRD:KOM"),
      posRegex("ADJ:AKK:PLU.*"),
      posRegex("SUB:AKK:PLU.*")
    ),
    asList(
      // "Andere weniger bekannte Vorschläge", "Ich habe mir das gerade letzte Woche zugelegt."
      posRegex("ART:.*|PRO:(POS|DEM|PER|IND).*"),
      tokenRegex("anscheinend|zunehmend|vorzugsweise|gekonnt|ausgeprägt|einige|solcher|solchen|typischerweise|hinreichend|nachgerade|vereinzelt|verheerend|hinreichend|zahlreiche|genauer|weiter|weniger|einzige|teilweise|anderen|sämtlicher|geringer|anderer|ausreichend|gerade|anhaltend|meisten"),
      posRegex("ADJ:.*"),
      posRegex("SUB:.*")
    ),
    asList(
      posRegex("PRO:DEM:.*"),  // "Diese definiert einzelne Genres ..."
      new PatternTokenBuilder().posRegexWithStringException("VER:[23]:.*", "eine").build(),
      posRegex("ADJ:.*"),
      posRegex("SUB:.*")
    ),
    asList(
      tokenRegex("aufs|beides|welcher"),  // "aufs äußerste grausamer Krieg"
      posRegex("ADJ:.*"),
      posRegex("ADJ:.*"),
      posRegex("SUB:.*")
    ),
    asList(
      tokenRegex("eine[mr]"),  // "Dieses Bild stammt von einem lange Zeit unbekannten Maler."
      pos("ADV:TMP"),
      pos("ADV:TMP"),
      posRegex("ADJ:.*"),
      posRegex("SUB:.*SIN.*")
    ),
    asList(
      tokenRegex("zu"),  // "Sie gehörte einst zu den besten Afrikas."
      tokenRegex("den"),
      posRegex("ADJ:.*"),
      posRegex("EIG:GEN:.*")
    ),
    asList(
      token("von"),  // "von denen viele Open-Source-Software sind"
      token("denen"),
      tokenRegex("viele|etliche|alle|einige|manche|mehrere|wenige"),
      new PatternTokenBuilder().posRegex("SUB:.*SIN:.*").setSkip(-1).build(),
      tokenRegex("sind|seien|sein|waren|wären")
    ),
    asList(
      token("von"),  // "von denen die meisten Open-Source-Software sind"
      token("denen"),
      token("die"),
      tokenRegex("meisten|wenigsten|besten"),
      new PatternTokenBuilder().posRegex("SUB:.*SIN:.*").setSkip(-1).build(),
      tokenRegex("sind|seien|sein|waren|wären")
    ),
    asList(
      tokenRegex("die|der|den"),  // "die späten 50er Jahre"
      tokenRegex("frühen|späten"),  // "die späten 50er Jahre"
      tokenRegex("\\d+er"),  // "die späten 50er Jahre"
      tokenRegex("Jahren?")
    ),
    asList(
      tokenRegex("die|der|den"),  // "die wilden 90er"
      new PatternTokenBuilder().posRegex("ADJ:.*").min(0).build(),
      tokenRegex("\\d+er")
    ),
    asList(
      posRegex("ART:.*"),  // "ein ausgesprochen unattraktiver Dienstort"
      token("ausgesprochen"),
      posRegex("ADJ:.*"),
      posRegex("SUB:.*")
    ),
    asList(
      tokenRegex("ein|eine|einen"),  // "bietet einen weniger genauen Zugriff"
      token("weniger"),
      posRegex("ADJ:AKK:SIN:.*:GRU:.*"),
      posRegex("SUB:.*SIN.*")
    ),
    asList(
      new PatternTokenBuilder().csToken("sein").matchInflectedForms().build(),
      token("das"),
      tokenRegex("Grund|Anlass|Auslöser|Ursache")
    ),
    asList(
      // "Vielleicht schreckt das Frauen ab"
      tokenRegex("schreckte?"),
      token("das"),
      posRegex("SUB:.*")
    ),
    asList(
      token("eine"),
      token("Zeitlang")
    ),
    asList(
      token("für"),  // "..., wann und für wen Impfungen vorgenommen werden könnten."
      token("wen"),
      posRegex("SUB:.*PLU.*")
    ),
    asList(
      token("der"),  // "der fließend Englisch sprechende Mitarbeiter"
      tokenRegex("flie(ß|ss)end"),
      tokenRegex(".*"),
      token("sprechende")
    ),
    asList(
      token("ein"),  // "Das wolkige und ein bisschen kühle Wetter..."
      token("bisschen"),
      posRegex("ADJ:.*"),
      posRegex("SUB:.*SIN.*")
    ),
    asList(
      token("ein"),  // "...um mal ein bisschen Einsichten zu bekommen" (ugs., aber okay)
      token("bisschen"),
      posRegex("SUB:.*PLU.*")
    ),
    asList(
      token("dem"),  // "dem Abhilfe zu schaffen"
      token("Abhilfe"),
      new PatternTokenBuilder().token("zu").min(0).build(),
      token("schaffen")
    ),
    asList(
      token("die"),  // "Die Müllers aus Hamburg"
      new PatternTokenBuilder().posRegex("EIG.*").tokenRegex(".*s").build()
    ),
    asList(
      token("die"),  // "Die Xi Jinping Ära ist …" -- should be 'Xi-Jinping-Ära', but don't detect here because of confusing error message
      posRegex("EIG:.*"),
      posRegex("SUB:.*")
    ),
    asList(
      token("die"),  // "Die Xi Ära ist …"  -- should be 'Xi-Ära', but don't detect here because of confusing error message
      posRegex("EIG:.*"),
      posRegex("EIG:.*"),
      posRegex("SUB:.*")
    ),
    asList(
      tokenRegex("ist|war|sei|wäre"),  // "war das Absicht"
      token("das"),
      new PatternTokenBuilder().posRegex("ADJ:.*").min(0).build(),
      tokenRegex("Absicht")
    ),
    asList(
      token("das"),  // "in das damalige Reichenbach in Schlesien"
      new PatternTokenBuilder().posRegex("ADJ:.*").min(0).build(),
      tokenRegex("Reichenbach|Albstadt|Arnstadt|Darmstadt|Duderstadt|Eberstadt|Eibelstadt|Erftstadt|Freudenstadt|Bergneustadt|" +
        "Neustadt|Burgkunstadt|Diemelstadt|Ebermannstadt|Eisenhüttenstadt|Friedrichstadt|Filderstadt|Freystadt|Florstadt|Glückstadt|" +
        "Grünstadt|Hallstadt|Halberstadt|Ingolstadt|Johanngeorgenstadt|Karlstadt")  // TODO: extend, https://de.wikipedia.org/wiki/Liste_der_St%C3%A4dte_in_Deutschland
    ),
    asList(
      token("das"),  // "Einwohnerzahl stieg um das Zweieinhalbfache"
      tokenRegex("(zwei|drei|vier|fünf|sechs|sieben|acht|neun|zehn|elf|zwölf).*fache")
    ),
    asList(
      token("ein"),  // "um ein vielfaches höhere Preise" -> Vielfaches, found by other rule
      tokenRegex("(viel|zwei|drei|vier|fünf|sechs|sieben|acht|neun|zehn|elf|zwölf).*faches"),
      posRegex("ADJ.*KOM.*")
    ),
    asList(
      tokenRegex("(eben)?diese"),  // "...damit diese ausreichend Sauerstoff geben."
      tokenRegex("genug|genügend|viel|hinreichend|ausreichend"),
      posRegex("SUB:NOM:SIN:.*"),
      posRegex("VER:.*")
    ),
    asList(
      tokenRegex("(eben)?diese[nmr]|andere[nm]"),  // "...um einer anderen genügend Platz zu schaffen"
      tokenRegex("genug|genügend|viel|hinreichend|ausreichend"),
      posRegex("SUB:NOM:SIN:.*"),
      token("zu"),
      posRegex("VER:.*")
    ),
    asList(
      posRegex("VER:MOD:.*"),  // "Sollten zu diesem weitere Informationen benötigt werden, ..."
      token("zu"),
      regex("(eben)?diese[mnr]"),
      new PatternTokenBuilder().posRegex("ADJ:.*").min(0).build(),
      posRegex("SUB:NOM:PLU:.*"),
      posRegex("PA2:.*")
    ),
    asList(
      regex("ein|das"),  // "Ein Geschenk, das Maßstäbe setzt" (#4043)
      pos("SUB:NOM:SIN:NEU"),
      token(","),
      token("das"),
      posRegex("SUB:NOM:PLU:.*"),
      posRegex("VER:3:.*")
    ),
    asList(
      token("uns"),  // "und wünschen uns allen Gesundheit."
      token("allen"),
      posRegex("SUB:.*:SIN:.*")
    ),
    asList(
      token("Domain"),
      token("Name"),
      tokenRegex("Systems?")
    ),
    asList(
      tokenRegex("der|das|die"),
      new PatternTokenBuilder().min(0).build(),
      token("Bad"),
      token("Homburger")
    ),
    asList(
      tokenRegex("der|die|das"),   // "Lieber jemanden, der einem Tipps/Hoffnung gibt." / "die 69er Revolte"
      csRegex("einem|[0-9]+er"),
      posRegex("SUB:.*")
    ),
    asList(
      tokenRegex("de[rs]"),   // "Die dauerhafte Abgrenzung des später Niedersachsen genannten Gebietes"
      posRegex("ADJ:.*"),
      posRegex("EIG:.*"),
      posRegex("PA2:.*")
    ),
    asList(
      posRegex("ART:.*FEM.*"),  // "Eine Lücke in der erneuerbare Energien eine sinnvolle Rolle spielen könnten"
      posRegex("SUB:.*FEM.*"),
      token("in"),
      token("der")
    ),
    asList(
      token("einem"),
      token("kalte"),
      token("Schauer")
    ),
    asList(
      regex("die|der"),  // "die Querwild GmbH"
      tokenRegex(".*"),
      token("GmbH")
    ),
    asList(
      token("Die"),
      regex("Waltons|Einen")
    ),
    asList(
      regex("Gro(ß|ss)e[sn]?"),
      regex("(Bundes)?Verdienstkreuz(es)?")
    ),
    asList( // "Adiponitril und Acetoncyanhydrin, beides Zwischenprodukte der Kunststoffproduktion."
      tokenRegex("[,–-]"),
      token("beides"),
      new PatternTokenBuilder().posRegex("ADJ:.*").min(0).build(),
      posRegex("SUB:.*")
    ),
    asList( // "In den Zwei Abhandlungen" (lowercase "zwei" is correct, but does not need to be found here)
      tokenRegex("Eins|Zwei|Drei|Vier|Fünf|Sechs|Sieben|Acht|Neun|Zehn|Elf|Zwölf"),
      posRegex("SUB:.*")
    ),
    asList( // "Eine Massengrenze, bis zu der Lithium nachgewiesen werden kann."
      token("bis"),
      token("zu"),
      token("der"),
      posRegex("SUB:.*"),
      posRegex("PA2:.*")
    ),
    asList(
      tokenRegex("jeder?"),
      token("Abitur")
    ),
    asList(
      token("Halle"),
      token("an"),
      token("der"),
      token("Saale")
    ),
    asList(  // "mehrere Tausend Menschen"
      tokenRegex("Dutzend|Hundert|Tausend"),
      new PatternTokenBuilder().posRegex("ADJ:.*").min(0).build(),
      posRegex("SUB:.*")
    ),
    asList(  // "Besonders reizen mich Fahrräder.", "weil mich psychische Erkrankungen aus der Bahn werfen"
      tokenRegex("dich|mich"),
      new PatternTokenBuilder().posRegex("ADJ:.*").min(0).build(),
      posRegex("SUB:.*")
    ),
    asList(  // "jenes Weges, den die Tausenden Juden 1945 ..."
      token("die"),
      token("Tausenden"),
      posRegex("SUB:.*PLU.*")
    ),
    asList(  // misspelling "Format"
      tokenRegex("das|ein"),
      token("Formart")
    ),
    asList(  // "... andere erfreut Tennis."
      regex("andere"),
      posRegex("VER:PA2.*"),
      posRegex("SUB:.*")
    ),
    asList(  // "Das eine bedeutet Gefahr und das andere Gelegenheit."
      regex("der|die|das"),
      new PatternTokenBuilder().token("eine").setSkip(-1).build(),
      regex("der|die|das"),
      token("andere"),
      posRegex("SUB:.*")
    ),
    asList(  // "... größeren Bekanntheitsgrad in der Bevölkerung als jeder andere Kandidat vor ihm"
      regex("jede[mnrs]?"),
      regex("anderen?"),
      posRegex("SUB:.*")
    ),
    asList(  // "... kein anderer Unrecht hat."
      regex("(eben)?diese[rs]?|keine?"),
      regex("anderer?"),
      posRegex("SUB:.*")
    ),
    asList(  // "Toleranz ist der Verdacht, dass der andere Recht hat."
      regex("der|die|das"),
      regex("anderen?"),
      token("Recht"),
      new PatternTokenBuilder().csToken("haben").matchInflectedForms().build()
    ),
    asList(  // "als einziger ein für die anderen unsichtbares Wunder zu sehen."
      token("für"),
      regex("den|die"),
      token("anderen")
    ),
    asList(  // "Wer auf eines anderen Schuhe wartet...", "...Auge darauf haben, dass keine der anderen Abbruch tue"
      regex("der|eine[sr]"),
      token("anderen"),
      posRegex("SUB:.*")
    ),
    asList(  // "wenn andere anderer Meinung sind"
      token("andere"),
      regex("anderer?"),
      posRegex("SUB:.*")
    ),
    asList(  // "Hat ein Schutzgut gegenüber den anderen Priorität?"
      token("gegenüber"),
      token("den"),
      token("anderen"),
      posRegex("SUB:.*")
    ),
    asList(  // "... ist des anderen Freiheitskämpfer", "... die anderen Volleyball"
      regex("des|die"),
      token("anderen"),
      posRegex("SUB:.*")
    ),
    asList(  // "Ein Esel schimpft den anderen Langohr."
      posRegex("VER:3:.*"),
      regex("den|die|das"),
      regex("anderen?"),
      posRegex("SUB:.*")
    ),
    asList(  // "... eine bessere Behandlung als andere Gefangene."
      token("als"),
      token("andere"),
      posRegex("SUB:.*PLU.*")
    ),
    asList(  // "was sein Klient für ein Mensch sei",
      // "Mir wird nicht ganz klar, was das bei 1:58 für ein Akkord ist."
      new PatternTokenBuilder().token("was").setSkip(5).build(),
      token("für"),
      token("ein"),
      new PatternTokenBuilder().posRegex("ADJ:.*(MAS|NEU).*").min(0).build(),
      posRegex("SUB:.*:(MAS|NEU).*")
    ),
    asList(  // "was sein Klient für ein Mensch sei",
      // "Mir wird nicht ganz klar, was das bei 1:58 für ein Akkord ist."
      new PatternTokenBuilder().token("was").setSkip(5).build(),
      token("für"),
      token("eine"),
      new PatternTokenBuilder().posRegex("ADJ:.*FEM.*").min(0).build(),
      posRegex("SUB:.*:FEM.*")
    ),
    asList(
      // "Wir zeigen die Gründe auf, wieso noch nicht jeder solche Anschlüsse hat."
      regex("jede[rsm]?"),
      regex("(eben)?solche[rsm]?"),
      posRegex("SUB.*PLU.*")
    ),
    asList(
      regex("als|um"),
      regex("(eben)?solche")
    ),
    asList(
      // "Solch frivolen Gedanken wollen wir gar nicht erst nachgehen."
      regex("(eben)?solch"),
      csRegex("[a-zäöüß]+en?"),
      posRegex("SUB.*PLU.*")
    ),
    asList(
      // "Solch frivolen ungewohnten Gedanken wollen wir gar nicht erst nachgehen."
      regex("(eben)?solch"),
      csRegex("[a-zäöüß]+en?"),
      csRegex("[a-zäöüß]+en?"),
      posRegex("SUB.*PLU.*")
    ),
    asList(
      // "Er erwartete solch aggressives Verhalten."
      regex("(eben)?solch"),
      csRegex("[a-zäöüß]+e[nms]"),
      posRegex("SUB.*SIN.*NEU.*")
    ),
    asList(
      // "Er erwartete solch aggressives ungewohntes Verhalten."
      regex("(eben)?solch"),
      csRegex("[a-zäöüß]+e[nms]"),
      csRegex("[a-zäöüß]+e[nms]"),
      posRegex("SUB.*SIN.*NEU.*")
    ),
    asList(
      // "Ein solch schöner Tisch."
      regex("(eben)?solch"),
      csRegex("[a-zäöüß]+e[nmr]"),
      posRegex("SUB.*SIN.*MAS.*")
    ),
    asList(
      // "Ein solch schöner neuer Tisch."
      regex("(eben)?solch"),
      csRegex("[a-zäöüß]+e[nmr]"),
      csRegex("[a-zäöüß]+e[nmr]"),
      posRegex("SUB.*SIN.*MAS.*")
    ),
    asList(
      // "Eine solch schöne Frau."
      regex("(eben)?solch"),
      csRegex("[a-zäöüß]+e[rn]?"),
      posRegex("SUB.*SIN.*FEM.*")
    ),
    asList(
      // "Eine solch schöne hübsche Frau."
      regex("(eben)?solch"),
      csRegex("[a-zäöüß]+e[rn]?"),
      csRegex("[a-zäöüß]+e[rn]?"),
      posRegex("SUB.*SIN.*FEM.*")
    ),
    asList(  
      // "Wenn ein Tiger einen Menschen tötet, ist das Grausamkeit."
      token(","),
      new PatternTokenBuilder().tokenRegex("dann|so").min(0).build(),
      csRegex("ist|wäre?"),
      csRegex("das(jenige)?|(der|die)jenige"),
      posRegex("SUB:NOM.*")
    ),
    asList(  
      // "Sind im Molekül mehrere Aminogruppen vertreten, so bestimmt dasjenige Kohlenstoff"
      new PatternTokenBuilder().tokenRegex("wenn|falls|sobald").matchInflectedForms().setSkip(-1).build(),
      token(","),
      new PatternTokenBuilder().tokenRegex("dann|so").min(0).build(),
      token("bestimmt"),
      csRegex("das(jenige)?"),
      posRegex("SUB:NOM.*")
    ),
    asList(  
      // "das Bände spricht"
      token("das"),
      token("Bände"),
      csRegex("spricht|sprach|sprechen")
    ),
    asList(  
      regex("des|der|den|dem|die"),
      csToken("Kommando"),
      csRegex("Spezialkräften?")
    ),
    asList(  
      token("auf"),
      csRegex("die|den|das"),
      csToken("Verlass"),
      csRegex("ist|war|wäre?")
    ),
    asList(  
      token("auf"),
      csRegex("die|den|das"),
      csToken("Verlass"),
      csToken("zu"),
      csToken("sein")
    )

  );

}
