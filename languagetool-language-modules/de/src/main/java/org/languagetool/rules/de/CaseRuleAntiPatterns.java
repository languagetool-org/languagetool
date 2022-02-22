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

/**
 * Antipatterns for {@link CaseRule}.
 */
class CaseRuleAntiPatterns {

  private static final PatternToken SENT_START = new PatternTokenBuilder().posRegex(JLanguageTool.SENTENCE_START_TAGNAME).build();

  // also see case_rule_exceptions.txt:
  static final List<List<PatternToken>> ANTI_PATTERNS = Arrays.asList(
    Arrays.asList(
      token("Planten"),
      token("un"),
      regex("Blomens?")
    ),
    Arrays.asList(
      csRegex("im|ins|ans?"),
      csRegex("Gestern|Vorgestern")
    ),
    Arrays.asList(
      csRegex("im|ins|ans?"),
      csRegex("Gestern|Vorgestern"),
      csRegex("und|&"),
      csRegex("Gestern|Vorgestern")
    ),
    Arrays.asList(
      csRegex("im|ins"),
      csRegex("Hier"),
      csRegex("und|&"),
      csRegex("Jetzt")
    ),
    Arrays.asList(
      csRegex("Private[snm]"),
      csRegex("und|&"),
      csRegex("Berufliche[snm]|Geschäftliche[snm]")
    ),
    Arrays.asList(
      csRegex("Hin"),
      csRegex("und|&"),
      csRegex("Her")
    ),
    Arrays.asList(
      csRegex("k?ein"),
      csRegex("Richtig|Falsch"),
      csRegex("und|oder|&"),
      csRegex("Richtig|Falsch")
    ),
    Arrays.asList(
      csRegex("Tax"),
      csRegex("[au]nd|&"),
      csRegex("Legal")
    ),
    Arrays.asList(
      // Er sagte, Geradliniges und Krummliniges sei unvergleichbar.
      csRegex("[A-ZÄÖÜ].+es"),
      csRegex("und|oder|&"),
      csRegex("[A-ZÄÖÜ].+es"),
      csRegex("[a-zäöüß\\-,\\.\\!\\?…;:–\\)\\(]+")
    ),
    Arrays.asList(
      // … in Ägyptisch, Berberisch und Semitisch erfolgte zuletzt.
      csRegex("[A-ZÄÖÜ].+isch"),
      csRegex("und|oder|&"),
      csRegex("[A-ZÄÖÜ].+isch"),
      csRegex("[a-zäöüß\\-,\\.\\!\\?…;:–\\)\\(]+")
    ),
    Arrays.asList(
      // … in Ägyptisch, Berberisch und Semitisch erfolgte zuletzt.
      csRegex("[A-ZÄÖÜ].+em"),
      csRegex("und|oder|&"),
      csRegex("[A-ZÄÖÜ].+em"),
      csRegex("[a-zäöüß\\-,\\.\\!\\?…;:–\\)\\(]+")
    ),
    Arrays.asList(
      // Er arbeitet im Bereich Präsidiales.
      csRegex("Bereich|Departement|Stabsstellen?|Dienststellen?"),
      csRegex("[A-ZÄÖÜ].+es")
    ),
    Arrays.asList(
      csRegex("Berufliche[snm]"),
      csRegex("und|&"),
      csRegex("Private[snm]")
    ),
    Arrays.asList(
      token("des"),
      csToken("Weiteren")
    ),
    Arrays.asList(
      // "Tom ist ein engagierter, gutaussehender Vierzigjähriger, der..."
      posRegex("(ADJ:|PA[12]).*"),
      token(","),
      posRegex("(ADJ:|PA[12]).*"),
      regex("[A-ZÖÄÜ].+jährige[mnr]?"),
      posRegex("(?!SUB).*")
    ),
    Arrays.asList(
      // "Um das herauszubekommen..."
      token("das"),
      regex(".+zu.+")
    ),
    Arrays.asList(
      token("Rock"),
      regex("['’]"),
      token("n"),
      regex("['’]"),
      token("Roll")
    ),
    Arrays.asList(
      regex("Vitamin-[A-Z][0-9]?-reich(e|e[nms])?")
    ),
    Arrays.asList(
      // Auflistung
      csRegex("[A-ZÖÄÜ][a-zöäüß]+"),
      token(","),
      csRegex("[A-ZÖÄÜ][a-zöäüß]+"),
      tokenRegex(",|etc")
    ),
    Arrays.asList(
      regex("erste[nr]?"),
      csToken("Hilfe")
    ),
    Arrays.asList(
      // Names
      regex("Alfred|Emanuel|Günter|Immanuel|Johannes|Karl|Ludvig|Anton|Peter|Robert|Rolf"),
      csToken("Nobel")
    ),
    Arrays.asList(
      // https://github.com/languagetool-org/languagetool/issues/1663
      token("Großes"),
      new PatternTokenBuilder().tokenRegex("leisten|erreichen|schaffen").matchInflectedForms().build()
    ),
    Arrays.asList(
      // https://github.com/languagetool-org/languagetool/issues/1515
      SENT_START,
      regex("[:;]"),
      token(")"),
      regex(".*")
    ),
    Arrays.asList(
      // https://github.com/languagetool-org/languagetool/issues/1515
      SENT_START,
      regex("[;:]"),
      token("-"),
      token(")"),
      regex(".*")
    ),
    Arrays.asList(
      SENT_START,
      regex("[A-Z]"),
      token(")"),
      regex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // Commas used as lower quotes
      SENT_START,
      token(","),
      token(","),
      regex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // ignore uppercase word at beginning after a character that is not a letter or number (needed to ignore emojies or bullet points at the beginning of a sentence)
      SENT_START,
      regex("^[^A-Za-z0-9ÄÖÜäöüàÀß]{1,2}$"),
      csRegex("[A-ZÖÜÄ].*")
    ),
    Arrays.asList(
      SENT_START,
      token("*"),
      token("*"),
      regex(".*")
    ),
    Arrays.asList( // two single quotes (’’) that create one double quote (needs different rule)
      SENT_START,
      token("’"),
      token("’"),
      regex(".*")
    ),
    Arrays.asList( // wrong quote used as opening quote, leave to UNPAIRED_BRACKETS etc.
      token("“"),
      csRegex("[A-ZÖÜÄ].*")
    ),
    Arrays.asList( // => Hallo test
      SENT_START,
      regex("=|-"),
      token(">"),
      regex(".*")
    ),
    Arrays.asList(
      SENT_START,
      token("#"),
      regex("\\d+"),
      regex(".*")
    ),
    Arrays.asList(
      // GitHub / Markdown check lists
      SENT_START,
      regex("\\*|\\-"),
      token("["),
      regex("]"),
      regex(".*")
    ),
    Arrays.asList(
      // GitHub / Markdown check lists
      SENT_START,
      regex("\\*|\\-"),
      token("["),
      token("x"),
      regex("]"),
      regex(".*")
    ),
    Arrays.asList(
      // non-alphanumeric character
      SENT_START,
      regex("^[^a-zA-ZäöüÄÖÜ\\d\\s:]+$"),
      csRegex("[A-ZÄÖÜ].*")
    ),
    Arrays.asList(
      regex("Roten?"),
      regex("Bete")
    ),
    Arrays.asList(
      // see https://www.duden.de/suchen/dudenonline/u-f%C3%B6rmig
      regex("[A-Z]-förmig(e[mnrs]?)?")
    ),
    Arrays.asList(
      token("Geboten")
    ),
    Arrays.asList(
      regex("vor|den"),
      token("Gefahren")
    ),
    // names with english adjectives
    Arrays.asList(
      regex("Digital|Global|Smart|International|Trade|Private|Live|Urban|Man|Total|Native|Imperial|Modern|Responsive|Simple|Legend|Human|Light|Ministerial|National"),
      pos("UNKNOWN")
    ),
    Arrays.asList(
      token("International"),
      regex("Managements?")
    ),
    Arrays.asList(
      token("National"),
      regex("Boards?")
    ),
    Arrays.asList(
      regex("[kK]nock"),
      regex("[oO]ut")
    ),
    Arrays.asList(
      csToken("das"),
      posRegex("VER:INF:.+"),
      posRegex("KON:NEB|PKT")
    ),
    Arrays.asList(
      // Ich hatte das vergessen oder nicht ganz verstanden.
      csToken("das"),
      posRegex("ADV.*"),
      posRegex("VER:INF:.+"),
      posRegex("KON:NEB|PKT")
    ),
    // names with english adjectives
    Arrays.asList(
      pos("UNKNOWN"),
      regex("Digital|Global|Smart|International|Trade|Private|Live|Urban|Man|Total|Native|Imperial|Modern|Responsive|Simple|Legend|Human|Light|Ministerial")
    ),
    // names with english adjectives
    Arrays.asList(
      token("Smart"),
      posRegex("SUB.*")
    ),
    // names with english adjectives
    Arrays.asList(
      token("National"),
      regex("Sales|University")
    ),
    Arrays.asList(
      // see http://www.lektorenverband.de/die-deutsche-rechtschreibung-was-ist-neu/
      // and http://www.rechtschreibrat.com/DOX/rfdr_Woerterverzeichnis_2017.pdf
      regex("Goldenen?"),
      regex("Hochzeit(en)?")
    ),
    Arrays.asList(
      // see http://www.rechtschreibrat.com/DOX/rfdr_Woerterverzeichnis_2017.pdf
      regex("Graue[nr]?"),
      regex("Stars?|Eminenz")
    ),
    Arrays.asList(
      // see http://www.rechtschreibrat.com/DOX/rfdr_Woerterverzeichnis_2017.pdf
      regex("Große[nr]?"),
      regex("Strafkammer|Latinums?|Rats?")
    ),
    Arrays.asList(
      // see http://www.rechtschreibrat.com/DOX/rfdr_Woerterverzeichnis_2017.pdf
      csToken("Guten"),
      csToken("Tag")
    ),
    Arrays.asList(
      // see http://www.rechtschreibrat.com/DOX/rfdr_Woerterverzeichnis_2017.pdf
      regex("Höheren?"),
      regex("Schule|Mathematik")
    ),
    Arrays.asList(
      // see http://www.rechtschreibrat.com/DOX/rfdr_Woerterverzeichnis_2017.pdf
      regex("Künstliche[nr]?"),
      token("Intelligenz")
    ),
    Arrays.asList(
      // see http://www.rechtschreibrat.com/DOX/rfdr_Woerterverzeichnis_2017.pdf
      regex("Neue[ns]?"),
      regex("Jahr(s|es)?|Linken?")
    ),
    Arrays.asList(
      token("Neues"),
      token("\\?")
    ),
    Arrays.asList(
      token("Zahl"),
      pos("UNKNOWN")
    ),
    Arrays.asList(
      // "... und Expert*innnen ..."
      regex("[A-Z].+"),
      token("*"),
      token("innen")
    ),
    Arrays.asList(
      // Names: "Jeremy Schulte", "Alexa Jung", "Fiete Lang", "Dorian Klug" ...
      new PatternTokenBuilder().posRegex("EIG:.+|UNKNOWN").csTokenRegex("[A-ZÄÖÜ].+").build(),
      csRegex("Schulte|Junge?|Lange?|Braun|Groß|Gross|K(ü|ue)hne?|Schier|Becker|Schön|Sauer|Ernst|Fr(ö|oe)hlich|Kurz|Klein|Schick|Frisch|Kluge|Weigert|D(ü|ue)rr|Nagele|Hoppe|D(ö|oe)rre|G(ö|oe)ttlich|Stark|Fahle|Fromm(er)?|Reichert|Wiest|Klug|Greiser")
    ),
    Arrays.asList(
      token(","),
      posRegex(".*ADJ.*|UNKNOWN"),
      regex("[\\.?!)]")
    ),
    Arrays.asList(
      csToken(","),
      regex("[md]eine?|du"),
      posRegex(".*ADJ.*|UNKNOWN"),
      regex("[\\.?!]")
    ),
    Arrays.asList(
      posRegex(".*ADJ.*|UNKNOWN"),
      regex("Konstanten?")
    ),
    Arrays.asList(
      token("das"),
      posRegex("PA2:.+"),
      posRegex("VER:AUX:.+")
    ),
    Arrays.asList(
      // Er fragte,ob das gelingen wird.
      csToken("das"),
      posRegex("VER:.+"),
      posRegex("VER:AUX:.+"),
      posRegex("PKT|KON:NEB")
    ),
    Arrays.asList(
      // Er fragte, ob das gelingen oder scheitern wird.
      csToken("das"),
      posRegex("VER:.+"),
      new PatternTokenBuilder().pos("KON:NEB").setSkip(5).build(),
      posRegex("VER:(AUX|MOD):.*"),
      posRegex("PKT|KON:NEB")
    ),
    Arrays.asList(
      // um ihren eigenen Glauben an das Gute, Wahre und Schöne zu stärken.
      token("das"),
      posRegex("SUB:.+"),
      token(","),
      regex("[A-ZÄÖÜ][a-zäöü]+"),
      regex("und|oder")
    ),
    Arrays.asList(
      // "... weshalb ihr das wissen wollt."
      pos("VER:INF:NON"),
      pos("VER:MOD:2:PLU:PRÄ")
    ),
    Arrays.asList(
      pos("UNKNOWN"),
      token("und"),
      posRegex("SUB:.*")
    ),
    Arrays.asList(
      // "... wie ich das prüfen sollte."
      posRegex("VER:INF.+"),
      posRegex("VER:MOD:.+")
    ),
    Arrays.asList(
        // "... wie ich das prüfen würde."
        posRegex("VER:INF.+"),
        posRegex("VER:AUX:.:(SIN|PLU)(:KJ2)?")
    ),
    Arrays.asList(
     // "... etwas Interessantes und Spannendes suchte"
     regex("etwas|nichts|viel|wenig|allerlei|was"),
     regex("[A-ZÄÖÜ].*es"),
     regex("und|oder|,"),
     regex("[A-ZÄÖÜ].*es")
    ),
    Arrays.asList(
     // "... bringt Interessierte und Experten zusammen"
     posRegex("VER:.*[1-3]:.*"),
     posRegex("SUB:AKK:.+:ADJ"),
     regex("und|oder|,"),
     posRegex("SUB:AKK:.+:(NEU|FEM|MAS)|ART:.*")
    ),
    Arrays.asList(
      // "Das südöstlich von Berlin gelegene"
      regex("(süd|nord|ost|west).*lich"),
      token("von")
    ),
    Arrays.asList(
      // "Entscheiden 42,5 Millionen Stimmberechtigte über..."
      regex("Million(en)?"),
      posRegex("SUB:.*:ADJ")
    ),
    Arrays.asList(
      // "Vor Betreten des" / "Trotz Verboten seiner Eltern"
      posRegex("PRP:.+|ADV:MOD"),
      pos("VER:PA2:NON"),
      posRegex("(ART|PRO):(IND|DE[FM]|POS):GEN:.*")
    ),
    Arrays.asList(
      // "Er liebt UV-bestrahltes, Na-haltiges und Makeup-freies Obst."
      // "Er vertraut auf CO2-arme Wasserkraft"
      regex("[A-ZÄÖÜ0-9]+[a-zäöüß0-9]-[a-zäöüß]+")
    ),
    Arrays.asList(
     // "Das Aus für Italien kam unerwartet." / "Müller drängt auf Aus bei Pflichtmitgliedschaft"
     regex("auf|das|vor|a[mn]|vorzeitige[mns]?|frühe[mns]?|späte[mns]?"),
     csToken("Aus"),
     posRegex("^PRP:.+|VER:[1-3]:.+")
    ),
    Arrays.asList(
      // Das ist das Aus des Airbus A380.
      regex("das"),
      csToken("Aus"),
      tokenRegex("des|eines"),
      posRegex("EIG:.+|SUB:.*|UNKNOWN")
    ),
    /*Arrays.asList(
      // "...,die ins Nichts griff."
      new PatternTokenBuilder().csTokenRegex("ins|ans|vors|durchs|hinters").setSkip(1).build(),
      posRegex("^PRP:.+|VER:[1-3]:.+")
    ),*/
    Arrays.asList(
     // "Bündnis 90/Die Grünen"
     csToken("90"),
     csToken("/"),
     csToken("Die")
    ),
    Arrays.asList(
     // https://de.wikipedia.org/wiki/Neue_Mittelschule
     regex("Neue[nrs]?"),
     new PatternTokenBuilder().tokenRegex("Mitte(lschule)?|Rathaus|Testament|Welt|Markt|Rundschau").matchInflectedForms().build()
    ),
    Arrays.asList( // "Das schließen Forscher aus ..."
     token("das"),
     posRegex("VER:INF:(SFT|NON)"),
     posRegex("SUB:NOM:PLU:.+|ADV:MOD")
    ),
    Arrays.asList( // Das schaffen moderne E-Autos locker
     token("das"),
     posRegex("VER:INF:(SFT|NON)"),
     posRegex("ADJ:.+"),
     posRegex("SUB:NOM:PLU:.+|ADV:MOD")
    ),
    Arrays.asList( // Das schaffen moderne und effiziente E-Autos locker
     token("das"),
     posRegex("VER:INF:(SFT|NON)"),
     posRegex("ADJ:.+"),
     posRegex("KON:.+"),
     posRegex("ADJ:.+"),
     posRegex("SUB:NOM:PLU:.+|ADV:MOD")
    ),
    Arrays.asList( // "Tausende Gläubige kamen, um ihn zu sehen."
      tokenRegex("[tT]ausende?"),
      posRegex("SUB:NOM:.+"),
      posRegex(JLanguageTool.SENTENCE_END_TAGNAME+"|VER:[1-3]:.+")
    ),
    Arrays.asList( // "Man kann das generalisieren"
      posRegex("VER:MOD.*"),
      token("das"),
      posRegex("VER:INF:(SFT|NON)")
    ),
    Arrays.asList( // "Vielleicht kann er das generalisieren"
      posRegex("VER:MOD.*"),
      posRegex("PRO:.+"),
      token("das"),
      posRegex("VER:INF:(SFT|NON)")
    ),
    Arrays.asList( // "Er befürchtete Schlimmeres."
      regex("Schlimm(er)?es"),
      pos(JLanguageTool.SENTENCE_END_TAGNAME)
    ),
    Arrays.asList(
      regex("Angehörige[nr]?")
    ),
    Arrays.asList( // aus Alt wird neu
      csToken("Alt"),
      regex("mach|w[iu]rde?"),
      csToken("Neu")
    ),
    Arrays.asList( // see GermanTagger.getSubstantivatedForms
      pos("SUB:NOM:SIN:MAS:ADJ"),
      posRegex("PRP:.+")
    ),
    Arrays.asList( // Einen Tag nach Bekanntwerden des Skandals
      pos("ZUS"),
      csToken("Bekanntwerden")
    ),
    Arrays.asList( // Das ist also ihr Zuhause.
      posRegex(".+:(POS|GEN):.+"),
      csToken("Zuhause")
    ),
    Arrays.asList( // Ein anderes Zuhause habe ich nicht.
      regex("altes|anderes|k?ein|neues"),
      csToken("Zuhause")
    ),
    Arrays.asList( // Weil er das kommen sah, traf er Vorkehrungen.
      csToken("das"),
      csToken("kommen"),
      new PatternTokenBuilder().csToken("sehen").matchInflectedForms().build()
    ),
    Arrays.asList(
      token("auf"),
      csToken("die"),
      csToken("Schnelle")
    ),
    Arrays.asList( // denn es fehlt bis heute am Nötigsten
      new PatternTokenBuilder().csToken("fehlen").matchInflectedForms().setSkip(3).build(),
      csToken("am"),
      csToken("Nötigsten")
    ),
    Arrays.asList(
      csToken("am"),
      csToken("Nötigsten"),
      new PatternTokenBuilder().csToken("fehlen").matchInflectedForms().build()
    ),
    Arrays.asList(
      tokenRegex("im|ins"),
      csToken("Aus")
    ),
    Arrays.asList(
      token("im"),
      csToken("Ganzen")
    ),
    Arrays.asList( // Die Top Fünf (https://www.korrekturen.de/forum.pl/md/read/id/73791/sbj/top-top-fuenf-fuenf/)
      csToken("Top"),
      pos("ZAL")
    ),
    Arrays.asList( // Die Top Ten (https://www.korrekturen.de/forum.pl/md/read/id/73791/sbj/top-top-fuenf-fuenf/)
      csToken("Top"),
      csToken("Ten")
    ),
    Arrays.asList( // Dutch name (e.g. "Bert van den Brink")
      csToken("Van"),
      csToken("Den")
    ),
    Arrays.asList(
      csToken("Lasse"),
      posRegex("EIG:.*|UNKNOWN")
    ),
    Arrays.asList( // Spanish name (e.g. "Las Condes")
      csToken("Las"),
      new PatternTokenBuilder().pos("UNKNOWN").csTokenRegex("[A-Z].+").build()
    ),
    Arrays.asList(
      csToken("Just"),
      token("in"),
      csToken("Time")
    ),
    Arrays.asList( // Hey Süßer,
      regex("Hey|Hi|Hallo|Na|Moin|Servus"),
      regex("Süßer?|Hübscher?|Liebster?|Liebes"),
      pos("PKT")
    ),
    Arrays.asList( // Guten Morgen Liebste,
      csRegex("Guten?"),
      csRegex("Morgen|Abend|Mittag|Nacht"),
      regex("Süßer?|Hübscher?|Liebster?|Liebes"),
      pos("PKT")
    ),
    Arrays.asList( // Hey Matt (name),
      regex("Hey|Hi|Hallo|Na|Moin|Servus"),
      regex("Matt|Will|Per")
    ),
    Arrays.asList( // Hey mein Süßer,
      regex("Hey|Hi|Hallo|Na|Moin|Servus"),
      regex("du|meine?"),
      regex("Süßer?|Hübscher?"),
      pos("PKT")
    ),
    Arrays.asList( // Grüße aus Höchst, Ich wohne in Wohlen
      regex("aus|in"),
      regex("Höchst|Wohlen")
    ),
    Arrays.asList( // Am So. (Sonntag)
      regex(",|;|/|-|am|bis|vor|\\("),
      csToken("So"),
      token(".")
    ),
    Arrays.asList(
      // a.) Im Mittelpunkt ...
      SENT_START,
      regex("[a-z]"),
      token("."),
      token(")"),
      regex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // ---> Der USB  ...
      SENT_START,
      regex("[-]{1,}"),
      token(">"),
      regex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // # Was macht eigentlich Karl
      SENT_START,
      regex("[#]{1,}"),
      regex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // ## Was macht eigentlich Karl
      SENT_START,
      token("#"),
      token("#"),
      regex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // ### Was macht eigentlich Karl
      SENT_START,
      token("#"),
      token("#"),
      token("#"),
      regex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // = Schrittweise Erklärung ()
      SENT_START,
      token("="),
      regex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // == Schrittweise Erklärung
      SENT_START,
      token("="),
      token("="),
      regex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // === Schrittweise Erklärung
      SENT_START,
      token("="),
      token("="),
      token("="),
      regex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // ==== Schrittweise Erklärung
      SENT_START,
      token("="),
      token("="),
      token("="),
      token("="),
      regex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // ===== Schrittweise Erklärung
      SENT_START,
      token("="),
      token("="),
      token("="),
      token("="),
      token("="),
      regex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // § 1 Allgemeine Bedingungen
      SENT_START,
      token("§"),
      regex("\\d+"),
      regex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // §1 Allgemeine Bedingungen
      SENT_START,
      regex("§\\d+"),
      regex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // 3a) Deine Idee ...
      SENT_START,
      regex("[a-z0-9]{1,3}"),
      token(")"),
      regex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // @Peter Hast du morgen Zeit?
      SENT_START,
      regex("@[a-zA-Z0-9]+"),
      regex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // M3.2 Deine Idee ...
      SENT_START,
      regex("[A-Z]\\d+"),
      token("."),
      regex("\\d+"),
      regex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      csToken("Gefahren"),
      csToken("lauern")
    ),
    Arrays.asList(
      csRegex("[A-ZÄÜÖ].+"),
      new PatternTokenBuilder().tokenRegex("\\*|:").setIsWhiteSpaceBefore(false).build(),
      csToken("innen")
    ),
    Arrays.asList( // Am So 14:00 (should be "So." but that's a different error)
      csRegex("am|jeden"),
      csToken("So")
    ),
    Arrays.asList( // "Sa. oder So."
      csRegex("M[io]|D[io]||Fr|Sa"),
      token("."),
      csRegex("&|und|oder|-|,"),
      csToken("So"),
      token(".")
    ),
    Arrays.asList( // "Sa, So"
      csToken("Sa"),
      csRegex("&|und|oder|-|,"),
      csToken("So")
    ),
    Arrays.asList( // Es hatte 10,5 Ah
      csRegex("\\d+"),
      csToken("Ah")
    ),
    Arrays.asList( // Via Camparlungo (Straßennamen in der italienischen Schweiz)
      csToken("Via"),
      new PatternTokenBuilder().pos("UNKNOWN").csTokenRegex("[A-Z].+").build()
    ),
    Arrays.asList( // Geoghegan Hart
      new PatternTokenBuilder().pos("UNKNOWN").csTokenRegex("[A-Z].+").build(),
      csToken("Hart")
    ),
    Arrays.asList( // Namen mit "Matt" (e.g. Matt Gaetz, Will Smith)
      csRegex("Matt|Will"),
      new PatternTokenBuilder().posRegex("EIG:.+|UNKNOWN").csTokenRegex("[A-Z].+").build()
    ),
    Arrays.asList( // Autohaus Dornig GmbH
      new PatternTokenBuilder().posRegex("EIG:.+|SUB:.+").csTokenRegex("[A-Z].+").build(),
      csRegex("[A-ZÄÜÖ].+"),
      csRegex("Gmb[Hh]|AG|KG|UG")
    ),
    Arrays.asList( // Klicke auf Home > Mehr > Team
      csToken(">"),
      csRegex("[A-ZÄÜÖ].+"),
      csToken(">")
    ),
    Arrays.asList(
      // :D Auf dieses Frl. der apfel fällt ja doch nicht weit vom stamm!
      SENT_START,
      regex("[:;]"),
      regex("[DPO]"),
      regex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // :-D Auf dieses Frl. der apfel fällt ja doch nicht weit vom stamm!
      SENT_START,
      regex("[:;]"),
      token("-"),
      regex("[DPO]"),
      regex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // Straßenname: "Am Hohen Hain 6"
      csToken("Am"),
      csRegex("[A-ZÄÖÜ].+n"),
      posRegex("(EIG|SUB|UNKNOWN).*"),
      csRegex("\\d{1,3}[a-hA-H]?")
    ),
    Arrays.asList(
      // Straßenname: "Am hohen Hain 6"
      csToken("Am"),
      new PatternTokenBuilder().posRegex("ADJ:.+").csTokenRegex("[a-zäöü].+n").build(),
      csRegex("[A-ZÄÜÖ].*"),
      csRegex("\\d{1,3}[a-hA-H]?")
    ),
    Arrays.asList(
      // Straßenname: "Am Wasserturm 6"
      csToken("Am"),
      posRegex("(EIG|SUB|UNKNOWN).*"),
      csRegex("\\d+[a-hA-H]?")
    ),
    Arrays.asList(
      // Straßenname: "Neue Kantstraße 6"
      csRegex("Neuen?|Gro(ß|ss)en?|Alten?"),
      csRegex("[A-Z].+stra(ss|ß)e"),
      csRegex("\\d{1,3}[a-hA-H]?|in")
    ),
    Arrays.asList(
      // Straßenname: "Neue Kantstr. 6"
      csRegex("Neuen?|Gro(ß|ss)en?|Alten?"),
      csRegex("[A-Z].+str"),
      token("."),
      csRegex("\\d{1,3}[a-hA-H]?|in")
    ),
    Arrays.asList(
      SENT_START,
      // Listenpunkt https://github.com/languagetool-org/languagetool/issues/1515
      regex("\\*|-|/|_|%|o"),
      regex(".*")
    ),
    Arrays.asList(
      // Trennzeichen https://github.com/languagetool-org/languagetool/issues/1515
      regex("▶︎|▶|▶️|→|•|★|⧪|⮞|✔︎|✓|✔️|✅|➡️|➔|⇨|☛|◼|◆|▪|■|☞|❤|✒︎|☑️|✗|✘|✖|➢|=|>|❏|›|❖|·|▲|◄|⬢|\\|"),
      regex(".*")
    ),
    Arrays.asList(
      // Pfeil "=>"
      regex("[=\\-–]"),
      token(">"),
      csRegex("[A-ZÄÖÜ].*")
    ),
    Arrays.asList(
      // Zwei Kommas, die wie Anführungszeichen verwendet werden: ",,"
      new PatternTokenBuilder().token(",").build(),
      new PatternTokenBuilder().token(",").setIsWhiteSpaceBefore(false).build(),
      new PatternTokenBuilder().csTokenRegex("[A-ZÄÜÖ].*").setIsWhiteSpaceBefore(false).build()
    ),
    Arrays.asList(
      // Markup: "[H3] Die Headline"
      SENT_START,
      token("["),
      regex("[A-Z0-9]+"),
      token("]"),
      csRegex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // Markup: "H3 Die Headline"
      SENT_START,
      regex("H[1-6]"),
      csRegex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // "B.4 Neue Herstellervorgaben"
      SENT_START,
      regex("[a-z]"),
      token("."),
      regex("\\d+"),
      csRegex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // "1-) Ich bin ein Listenpunkt"
      SENT_START,
      regex("\\d+-"),
      regex("[\\)\\]]"),
      csRegex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // "1, Ich bin ebenfalls ein Listenpunkt"
      SENT_START,
      regex("[a-z0-9]"),
      token(","),
      csRegex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // "T = Das Ziel"
      SENT_START,
      regex("[A-Z0-9]+"),
      token("="),
      csRegex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // "(2c) Der Betrieb ist untersagt"
      SENT_START,
      regex("[\\[\\(\\{]"),
      regex("[a-z0-9]{1,5}"),
      regex("[\\]\\)\\}]"),
      csRegex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // "Sie/Er hat recht."
      SENT_START,
      csRegex("[A-ZÄÜÖ].*"),
      token("/"),
      csRegex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // "Sie/Er/Es hat recht."
      SENT_START,
      csRegex("[A-ZÄÜÖ].*"),
      token("/"),
      csRegex("[A-ZÄÜÖ].*"),
      token("/"),
      csRegex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // "Er trank ein paar Halbe."
      regex("paar|einige|zwei|drei|vier|\\d+"),
      token("Halbe")
    ),
    Arrays.asList(
      // "Wir machen das Sa So Mo" (fehlender Punkt)
      csToken("Sa"),
      csToken("So")
    ),
    Arrays.asList(
      // "Wir machen das Sa oder So" (fehlender Punkt)
      csToken("Sa"),
      regex("&|und|oder|-"),
      csToken("So")
    ),
    Arrays.asList(
      // Vielleicht reden wir später mit ein paar Einheimischen.
      token("ein"),
      token("paar"),
      new PatternTokenBuilder().posRegex(".*SUB.*").csTokenRegex("[A-ZÖÜÄ].+").build(),
      new PatternTokenBuilder().csTokenRegex("[a-zäöüß.,!?:;\\-–].*").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().csToken("Neues").setSkip(1).build(),
      new PatternTokenBuilder().token("wagen").matchInflectedForms().build()
    ),
    Arrays.asList( // Wir wagen Neues.
      new PatternTokenBuilder().token("wagen").matchInflectedForms().build(),
      token("Neues")
    ),
    Arrays.asList( // Das birgt zugleich Gefahren
      new PatternTokenBuilder().csToken("birgen").matchInflectedForms().setSkip(5).build(),
      token("Gefahren")
    ),
    Arrays.asList(
      // Du Ärmster!
      token("du"),
      csRegex("Ärmster?"),
      csRegex("[^A-ZÖÄÜ].*")
    ),
    Arrays.asList(
        // "... und das Zwischenmenschliche Hand in Hand."
        posRegex("ART:.*|PRO:POS:.*"),
        new PatternTokenBuilder().posRegex("SUB:.*:ADJ").csTokenRegex("[A-ZÖÜÄ].+").build(),
        csToken("Hand"),
        csToken("in"),
        csToken("Hand")
    ),
    Arrays.asList(
        // "Der Platz auf dem die Ahnungslosen Kopf and Kopf stehen.""
        posRegex("ART:.*|PRO:POS:.*"),
        new PatternTokenBuilder().posRegex("SUB:.*:ADJ").csTokenRegex("[A-ZÖÜÄ].+").build(),
        csToken("Kopf"),
        csToken("an"),
        csToken("Kopf")
    ),
    Arrays.asList(
        // "Der Platz auf dem die Ahnungslosen Schulter and Schulter stehen.""
        posRegex("ART:.*|PRO:POS:.*"),
        new PatternTokenBuilder().posRegex("SUB:.*:ADJ").csTokenRegex("[A-ZÖÜÄ].+").build(),
        csToken("Schulter"),
        csToken("an"),
        csToken("Schulter")
    ),
    Arrays.asList(
        // "Der Platz auf dem die Ahnungslosen Stück für Stück ...""
        posRegex("ART:.*|PRO:POS:.*"),
        new PatternTokenBuilder().posRegex("SUB:.*:ADJ").csTokenRegex("[A-ZÖÜÄ].+").build(),
        csToken("Stück"),
        csToken("für"),
        csToken("Stück")
    ),
    Arrays.asList(
        // "Der Platz auf dem die Ahnungslosen Schritt für Schritt ...""
        posRegex("ART:.*|PRO:POS:.*"),
        new PatternTokenBuilder().posRegex("SUB:.*:ADJ").csTokenRegex("[A-ZÖÜÄ].+").build(),
        csToken("Schritt"),
        csToken("für"),
        csToken("Schritt")
    ),
    Arrays.asList(
        // "Der Platz auf dem die Ahnungslosen Arm in Arm ...""
        posRegex("ART:.*|PRO:POS:.*"),
        new PatternTokenBuilder().posRegex("SUB:.*:ADJ").csTokenRegex("[A-ZÖÜÄ].+").build(),
        csToken("Arm"),
        csToken("in"),
        csToken("Arm")
    ),
    Arrays.asList(
      // ``Ich bin ein Anführungszeich
      SENT_START,
      token("`"),
      token("`"),
      csRegex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // "4b Ein Listenpunkt"
      SENT_START,
      regex("\\d{1,2}[a-z]"),
      csRegex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // "3.) Ein Listenpunkt"
      SENT_START,
      regex("\\d{1,3}[a-z]?"),
      csToken("."),
      regex("[\\]\\)\\}]"),
      csRegex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // "Es besteht aus Schülern, Arbeitstätigen und Studenten."
      posRegex("SUB:.+"),
      csToken(","),
      posRegex("SUB:.+"),
      csRegex("und|oder|&"),
      posRegex("SUB:.+:(MAS|FEM|NEU)")
    ),
    Arrays.asList(
      // Das denken zwar viele, ist aber total falsch.
      SENT_START,
      csToken("Das"),
      csToken("denken"),
      new PatternTokenBuilder().posRegex("ADV:.+").min(0).build(),
      csRegex("viele|manche|die|[dms]eine|ihre|eure|diese|jene|wenige")
    ),
    Arrays.asList(
      // Ich habe eine Menge Schlechtes über dich gehört
      csToken("Menge"),
      csRegex("Gutes|Schlechtes|Tolles|Böses|Schlimmes"),
      csRegex("[a-zäöüß].*|\\.|\\,|\\!|:|;")
    ),
    Arrays.asList(
      // Während 208 der Befragten Frau Baerbock bevorzugten, ...
      csRegex("\\d+%?|%|Prozent|meisten|wenige|einige|viele|Gro(ß|ss)teil"),
      csToken("der"),
      csRegex("Befragten|Teilnehmenden"),
      new PatternTokenBuilder().posRegex("SUB:.*").csTokenRegex("[A-ZÖÜÄ].+").build()
    ),
    Arrays.asList(
      // Dabei trauten die Befragten Frau Baerbock mehr zu.
      csToken("die"),
      new PatternTokenBuilder().posRegex("SUB:NOM:PLU.*:ADJ").csTokenRegex("[A-ZÖÜÄ].+").build(),
      new PatternTokenBuilder().posRegex("SUB:.*:SIN").csTokenRegex("[A-ZÖÜÄ].+").build()
    ),
    Arrays.asList(
      // Sie starrt ständig ins Nichts. 
      csRegex("vorm|ins|im"),
      csToken("Nichts")
    ),
    Arrays.asList(
      // zahlreiche Kulturschaffende, jungen Wilden
      csRegex("[a-zäöü].+en?"),
      new PatternTokenBuilder().posRegex("SUB:NOM:PLU.*").csTokenRegex("[A-ZÄÖÜ].+").build(),
      csRegex(",|und|oder|aber|\\.|!|\\?|…")
    ),
    Arrays.asList(
      // ignore uppercase words after invisible commas at sent start
      SENT_START,
      regex("\\u2063"),
      csRegex("[A-ZÄÖÜ].+")
    ),
    Arrays.asList(
      // ignore uppercase words after invisible commas at sent start
      SENT_START,
      regex("\\u2063"),
      regex("\\u2063"),
      csRegex("[A-ZÄÖÜ].+")
    ),
    Arrays.asList(
      // ignore uppercase words after invisible commas at sent start
      SENT_START,
      regex("\\u2063"),
      regex("\\u2063"),
      regex("\\u2063"),
      csRegex("[A-ZÄÖÜ].+")
    ),
    Arrays.asList(
      // ignore uppercase words after invisible commas at sent start
      SENT_START,
      regex("\\u2063"),
      regex("\\u2063"),
      regex("\\u2063"),
      regex("\\u2063"),
      csRegex("[A-ZÄÖÜ].+")
    ),
    Arrays.asList(
      SENT_START,
      regex("[\\\\/`´*„\"']"),
      regex("[\\\\/`´*„\"']"),
      csRegex("[A-ZÄÖÜ].+")
    ),
    Arrays.asList(
      regex("nur"),
      csRegex("Positives|Schlechtes|Gutes|Böses|Negatives|Folgendes|Neues|Altes|Schlimmes|Letzteres|Ersteres|Blödes|Schreckliches|Wesentliches|Falsches|Richtiges"),
      csRegex("[a-zäöü…\\.!\\?…].*")
    ),
    Arrays.asList(
      token("im"),
      csRegex("Wesentlichen")
    ),
    Arrays.asList(
      token("im"),
      csRegex("Allgemeinen"),
      csRegex("[a-zäöü…\\.!\\?…].*")
    ),
    Arrays.asList(
      token("im"),
      csRegex("Allgemeinen"),
      posRegex("SUB.*FEM.*")
    ),
    Arrays.asList(
      token("im"),
      csRegex("Allgemeinen"),
      posRegex("SUB.*PLU.*")
    ),
    Arrays.asList(
      token("im"),
      csRegex("Stillen|Dunkeln|Dunklen|Trocke?nen|Hellen|Trüben|Kalten|Warmen|Geringsten|Entferntesten"),
      csRegex("[a-zäöü…\\.!\\?…].*")
    ),
    Arrays.asList(
      regex("[\\ud83c\\udc00-\\ud83c\\udfff]+|[\\ud83d\\udc00-\\ud83d\\udfff]+|[\\u2600-\\u27ff]+"),
      csRegex("[A-ZÄÖÜ].+")
    ),
    Arrays.asList(
      // Es gibt S-Bahn-ähnliche(, günstige) Verkehrsmittel
      csRegex("[A-ZÄÖÜ]-[A-ZÄÖÜ].*-.*"),
      new PatternTokenBuilder().posRegex("PKT|KON:NEB|ADJ.*").min(0).max(2).build(),
      posRegex("SUB.*")
    ),
    Arrays.asList( // Das ist das Debakel und Aus für Podolski
      csToken("Aus"),
      csToken("für")
    ),
    Arrays.asList( // Frohes Neues!
      csRegex("[Ff]rohes|[Gg]esundes"),
      csToken("Neues")
    ),
    Arrays.asList( // Wir sollten das mal labeln
      csToken("das"),
      csToken("mal"),
      csRegex("[a-zäöüß].+n")
    ),
    Arrays.asList(
      regex("[^a-zäöüß\\-0-9]+"),
      csToken("["),
      csToken("…"),
      csToken("]"),
      csRegex("[A-ZÄÖÜ].+")
    ),
    Arrays.asList(
      regex("[^a-zäöüß\\-0-9]+"),
      csToken("["),
      csToken("."),
      csToken("."),
      csToken("."),
      csToken("]"),
      csRegex("[A-ZÄÖÜ].+")
    ),
    Arrays.asList( // Kund:in
      csToken("Kund"),
      csRegex("[:_*\\/]"),
      regex("in|innen")
    ),
    Arrays.asList( // Wie ein verstoßener Größenwahnsinniger.
      posRegex("ART:.*|PRO:POS:.*"),
      posRegex("PA[12].*"),
      posRegex("SUB.*ADJ"),
      csRegex("[a-zäöüß\\-,\\.\\!\\?…;:–\\)\\(]+")
    ),
    Arrays.asList( // Vorab das Wichtigste - ...
      posRegex("das"),
      posRegex("SUB.*NEU:ADJ"),
      csRegex("[a-zäöüß\\-,\\.\\!\\?…;:–\\)\\(]+")
    ),
    Arrays.asList( // Wichtiges/Lehrreiches/Großes/...
      token("/"),
      csRegex("[A-ZÄÖÜ].*"),
      token("/")
    ),
    Arrays.asList( // Etwas anderes Lebendiges
      csRegex("anderes"),
      csRegex("[A-ZÄÖÜ].+es"),
      csRegex("[a-zäöü…\\.!:;,\\?…\\)].*")
    ),
    Arrays.asList( // Ich habe noch Dringendes mitzuteilen
      csRegex("Dringendes|Bares|Vertrautes|Positives|Negatives|Gelerntes|Neues|Altes|Besseres|Schlechteres|Schöneres|Schlimmeres|Zutreffendes|Gesehenes|Abgerissenes"),
      csRegex("[a-zäöü…\\.!,\\?…\\)].*")
    ),
    Arrays.asList( // Immer mehr Ältere erkranken daran
      csRegex("[a-zäöü…\\.,:;0-9\\/].*"),
      csRegex("Ältere[rn]?|Jüngere[rn]?|Verwirrte[rn]?|Zuschauende[rn]?|Angeklagte[rn]?|Befragte[rn]?|Beschuldigte[rn]?|Referierende[rn]?|Moderierende[rn]?|Dunkelhäutige[rn]?|Verantwortliche[rn]?|Alleinlebende[rn]?|Ungeübte[rn]?|Außerirdische[rn]?|Berittene[rn]?|Heranwachsende[rn]?|Ganze[sn]?"),
      csRegex("[a-zäöü…\\.!:;,\\?…\\)\\*\\(].*")
    ),
    Arrays.asList( // Im Folgenden soll 
      token("im"),
      csRegex("Folgenden"),
      csRegex("[a-zäöü…\\.!:;,\\?…\\)\\*\\(].*")
    ),
    Arrays.asList( // § 12 Die Pflichtversicherung
      csToken("§"),
      csRegex("\\d+[a-z]{0,2}"),
      csRegex("[A-ZÄÖÜ].+")
    ),
    Arrays.asList( // § 12.1 Die Pflichtversicherung
      csToken("§"),
      regex("\\d+"),
      token("."),
      regex("\\d+"),
      csRegex("[A-ZÄÖÜ].+")
    ),
    Arrays.asList( // Etwas anderes Lebendiges
      csToken("zu"),
      csRegex("Angeboten|Gefahren|Kosten")
    ),
    Arrays.asList( // Die Gemeinde Nahe in Schleswig-Holstein
      csRegex("Gemeinden?"),
      csToken("Nahe")
    ),
    Arrays.asList( // Ein Haus // Eine Villa
      token("/"),
      token("/"),
      csRegex("[A-ZÄÖÜ].+")
    ),
    Arrays.asList( // Ein Haus // Eine Villa
      token("<"),
      token("<"),
      csRegex("[A-ZÄÖÜ].+")
    ),
    Arrays.asList( // Ein Haus // Eine Villa
      token(">"),
      token(">"),
      csRegex("[A-ZÄÖÜ].+")
    ),
    Arrays.asList( // [Weiterlesen]
      token("["),
      csRegex("[A-ZÄÖÜ].+")
    ),
    Arrays.asList( // Beim Hoch- und Runtertragen
      regex("beim|zum|im|am"),
      csRegex("[A-ZÄÖÜ].+-"),
      csRegex("und|oder|&|/"),
      csRegex("[A-ZÄÖÜ].+n")
    )
  );

}
