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
      new PatternTokenBuilder().token("sprechen").matchInflectedForms().build(),
      csRegex(LanguageNames.getAsRegex())
    ),
    Arrays.asList(
      tokenRegex("international"),
      csRegex("GmbH|gGmbH|AG|gAG|InvAG|OHG|KG|UG|eG|GbR")
    ),
    Arrays.asList(   // "die Spiegelblank GmbH"
      tokenRegex("die|der"),
      csRegex("[A-ZÄÜÖ].+"),
      csRegex("GmbH|gGmbH|AG|gAG|InvAG|OHG|KG|UG|eG|GbR")
    ),
    Arrays.asList( // Autohaus Dornig GmbH
      new PatternTokenBuilder().posRegex("EIG:.+|SUB:.+").csTokenRegex("[A-Z].+").build(),
      csRegex("[A-ZÄÜÖ].+"),
      csRegex("GmbH|gGmbH|AG|gAG|InvAG|OHG|KG|UG|eG|GbR")
    ),
    Arrays.asList(
      posRegex("ADJ:.*"),
      tokenRegex("&|and"),
      posRegex("ADJ:.*")
    ),
    Arrays.asList(
      tokenRegex("Deutschen?|Österreichischen?|Schweizerischen?"),  // TODO: extend
      tokenRegex(".*gesellschaft")
    ),
    Arrays.asList(
      csRegex("im|ins|ans?"),
      csRegex("Gestern|Vorgestern")
    ),
    Arrays.asList(
      csRegex("im|ins|ans?|das"),
      csRegex("Gestern|Vorgestern"),
      csRegex("und|&"),
      csRegex("Gestern|Vorgestern")
    ),
    Arrays.asList(
      csRegex("[Ii]m|[Dd]as|[Dd]em|[Ii]ns"),
      csRegex("Hier|Vorher"),
      csRegex("und|&"),
      csRegex("Jetzt|Nachher")
    ),
    Arrays.asList(
      csRegex("[Ii]m|[Dd]as|[Dd]em|[Ii]ns"),
      csRegex("Jetzt|Nachher"),
      csRegex("und|&"),
      csRegex("Hier|Vorher")
    ),
    Arrays.asList(
      csRegex("im|ins"),
      csRegex("Hier|Jetzt")
    ),
    Arrays.asList(
      csRegex("[Dd]ieses|das|k?ein"),
      new PatternTokenBuilder().posRegex("ADJ.*NEU.*").min(0).build(),
      csRegex("Rein"),
      csRegex("und|&"),
      csRegex("Raus")
    ),
    Arrays.asList(
      csRegex("Private[snm]|Familiäre[snm]"),
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
      csRegex("Bereich|Departement|Stabsstellen?|Dienststellen?|AG|Arbeitsgruppe|Edition"),
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
    Arrays.asList( // Mo.–So.
      csRegex("\\.|Mo|Di|Mi|Do|Fr|Sa"),
      csRegex("-|–"),
      csToken("So")
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
      // wenn sie das beantworten
      regex("wenn|falls|sobald"),
      posRegex("PRO.*|EIG.*"),
      csToken("das"),
      posRegex("VER:INF:.+"),
      regex("dann|,|und|oder|\\.|\\!|\\:")
    ),
    Arrays.asList(
      // wenn sie mir das beantworten
      regex("wenn|falls|sobald"),
      posRegex("PRO.*|EIG.*"),
      csRegex("mir|uns|ih[rm]"),
      csToken("das"),
      posRegex("VER:INF:.+"),
      regex("dann|,|und|oder|\\.|\\!|\\:")
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
      csRegex("Schulte|Junge?|Lange?|Braun|Groß|Gross|K(ü|ue)hne?|Schier|Becker|Schön|Sauer|Ernst|Fr(ö|oe)hlich|Kurz|Klein|Schick|Frisch|Kluge|Weigert|D(ü|ue)rr|Nagele|Hoppe|D(ö|oe)rre|G(ö|oe)ttlich|Stark|Fahle|Fromm(er)?|Reichert|Wiest|Klug|Greiser|Nasser")
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
      regex("@.+"),
      regex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      // @b_fischer Der Bonussemester-Antrag oder der Widerspruch?
      SENT_START,
      regex("@.+"),
      token("_"),
      regex("[a-z].*"),
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
      csRegex("innen|en?")
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
    Arrays.asList( // "Sa. oder So."
      csRegex("M[io]|D[io]||Fr|Sa"),
      token("."),
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
      csRegex("Matt|Will|Dick"),
      new PatternTokenBuilder().posRegex("EIG:.+|UNKNOWN").csTokenRegex("[A-Z].+").build()
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
      csRegex("[IA]m"),
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
      // Straßenname: "Im hohen Hain 6e"
      csToken("Im"),
      new PatternTokenBuilder().posRegex("ADJ:.+").csTokenRegex("[a-zäöü].+n").build(),
      csRegex("[A-ZÄÜÖ].*"),
      csRegex("\\d{1,3}[a-hA-H]|\\d")
    ),
    Arrays.asList(
      // Straßenname: "Am Wasserturm 6"
      csToken("Am"),
      posRegex("(EIG|SUB|UNKNOWN).*"),
      csRegex("\\d+[a-hA-H]?")
    ),
    Arrays.asList(
      // Straßenname: "Am Wasserturm 6"
      csRegex("[IA]m"),
      csRegex("[A-Z].*(pfad|weg|kamp|platz|tor|gasse|feld|berg|park)"),
      csRegex("\\d+[a-hA-H]?")
    ),
    Arrays.asList(
      // Straßenname: "Neue Kantstraße 6"
      csRegex("Neuen?|Gro(ß|ss)en?|Alten?|Oberen?|Unteren?"),
      csRegex("[A-Z].+stra(ss|ß)e|.*[kK]amp|.*[Tt]or|.*[Gg]asse|.*[Gg]raben|.*[Ff]eld|.*[Pp]latz|.*[Bb]erg|.*[Pp]ark"),
      csRegex("\\d{1,3}[a-hA-H]?|in")
    ),
    Arrays.asList(
      // Straßenname: "Neue Kantstr. 6"
      csRegex("Neuen?|Gro(ß|ss)en?|Alten?|Oberen?|Unteren?"),
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
      regex("▶︎|▶|▶️|►|⇒|→|•|★|⧪|⮞|✔︎|✓|✔️|✅|0️⃣|1️⃣|2️⃣|3️⃣|4️⃣|5️⃣|6️⃣|7️⃣|8️⃣|9️⃣|❤️|➡️|➔|⇨|☛|◼|▲|◆|▪|■|☞|❤|♥︎|✒︎|☑️|✗|✘|✖|➢|↑|=|>|\\}|❏|›|❖|·|▲|◄|⬢|\\||!|‼️|⚠️|√"),
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
      regex("[a-z0-9\\-äöüß]+"),
      token("]"),
      csRegex("[A-ZÄÜÖ].*")
    ),
    Arrays.asList(
      csRegex("Schritt|Punkt|Absatz"),
      regex("\\d+"),
      token(":"),
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
      regex("[\\)\\]\\}]"),
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
      // "(2c) Der Betrieb ist untersagt"
      SENT_START,
      regex("[\\[\\(\\{]"),
      regex("[a-z0-9]{1,5}"),
      token("."),
      regex("[a-z0-9]{1,5}"),
      regex("[\\]\\)\\}]")
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
      csRegex("Positives|Schlechtes|Gutes|Böses|Negatives|Folgendes|Neues|Altes|Schlimmes|Letzteres|Ersteres|Blödes|Schreckliches|Wesentliches|Falsches|Richtiges|Hässliches"),
      csRegex("[a-zäöü…\\.!\\?…].*")
    ),
    Arrays.asList(
      token("im"),
      csRegex("Wesentlichen|Vorab|Geringsten")
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
      csRegex("Stillen|Dunkeln|Dunklen|Trocke?nen|Hellen|Trüben|Kalten|Warmen|Geringsten|Entferntesten|Verborgenen"),
      csRegex("[a-zäöü…\\.!\\?…\\)\\(;].*")
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
      regex("(in|innen|en?).*")
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
    Arrays.asList( // Alles_Zutreffendes.csv
      token("_"),
      csRegex("[A-ZÄÖÜ].*")
    ),
    Arrays.asList( // Etwas anderes Lebendiges
      csRegex("anderes"),
      csRegex("[A-ZÄÖÜ].+es"),
      csRegex("[a-zäöü…\\.!:;,\\?…\\)].*")
    ),
    Arrays.asList( // Ich habe noch Dringendes mitzuteilen
      csRegex("Dringendes|Bares|Vertrautes|Positives|Negatives|Gelerntes|Neues|Altes|Besseres|Störendes|Schlechteres|Schlechtes|Schönes|Schöneres|Schlimmeres|Zutreffendes|Unzutreffendes|Gesehenes|Ungesehenes|Abgerissenes|Versoffenes|Empfehlenswertes|Entnommenes|Sü(ß|ss)es|Saures|Wesentliches|Gegenteiliges|Wegweisendes|Hochprozentiges|Erlerntes|Vergangenes|Unglaubliches|Schlimmes|Eingemachtes|Rares|Brauchbares|Unbrauchbares|Gesehenes|Erlebtes|Privates|Berufliches|Ungeheuerliches|Veganes|Vegetarisches|Eingemachtes|Erwünschtes|Äu(ß|ss)erstes|Äu(ß|ss)eres|Inhaltliches|Vernichtendes|Salziges|Sü(ß|ss)es|Selbstgemachtes|Inhaltliches|Au(ß|ss)erordentliches|Säuerliches|Göttliches||Hochprozentige[sm]|Erbrochene[ms]|Innere[mns]|Vorhandenes|Relevantes|Geübtes|Unmögliches|Mögliches|Näheres|Wissenswertes|Fundamentales|Interessantes|Uninteressantes|Entsetzliche[ms]|Hartnäckige[ms]|Ersparte[ms]|Halbgare[ms]|Universale[ms]|Finanzielle[ms]|Überraschendes|Grundlegendes|Gesunde[ms]|Ungesunde[ms]|Tagesaktuelle[ms]|Aktuelle[ms]|Geschehene[ms]|Ungeschehene[ms]|Böse[ms]|Gesagte[ms]|Aufregende[sm]|Ausgestelltes|Verschiedenes|Verborgenes|Soziales|Erfundenes|Gro(ß|ss)es|Neueres|Au(ß|ss)ergewöhnliche[ms]|Zukunftsfähige[sm]|Administrative[ms]|Beunruhigendes|Naturverträgliches|Nachhaltiges|Verderbliche[ms]|Sinnstiftendes|Unüberlegtes|Alltägliche[sm]|Geartetes?|Allgemeines?|Übernatürliches?|Juristisches?|Rechtliches?|Vielfältiges?|Kommunales|Wundervolles?|Abgelaufenes|Erstere[ms]|Zweitere[ms]|Letztere[ms]|Unvermeidliches?|Fressbares?|Essbares?|Erbrochene[sm]|Politische[sm]|Regionale[sm]|Recherchiertes|Höheres|Kleineres|Deftiges|Liebes|Grünes|Diverses|Machbare[ms]|Nachweisbare[ms]|Zerstörtes|Öffentliches|Produktives|Entbehrliches|Notwendiges|Sinnvolle[ms]|Bewährte[ms]|Nötiges|Erfreuliches|Frustrierendes|Vorübergehendes|Untaugliches|Rohes|Nettes|Blödes|Unerwartetes|Lesenswerte[ms]|Geplantes|Ungeplantes|Redaktionelles|Spezielle[ms]|Spezifische[ms]|Staatstragendes|Organisatorisches"),
      csRegex("(?!(und|oder))[a-zäöü…\\.!,\\?…\\)“„\"»«–\\-:;].*")
    ),
    Arrays.asList(
      // Already caught by SEIT_LAENGEREN
      token("seit"),
      token("Längeren")
    ),
    Arrays.asList(
      token("von"),
      csToken("Nichts"),
      csToken("zu"),
      csToken("Nichts")
    ),
    Arrays.asList(
      token("Vors"),
      token(".")
    ),
    Arrays.asList( // Immer mehr Ältere erkranken daran
      csRegex("Ältere[rn]?|Jüngere[rn]?|Zuschauende[rn]?|Angeklagte[rn]?|Referierende[rn]?|Schlafenden?|Moderierende[rn]?|Dunkelhäutige[rn]?|Verantwortliche[rn]?|Alleinlebende[rn]?|Alleinstehende[rn]?|Ungeübte[rn]?|Au(ß|ss)erirdische[rn]?|Berittene[rn]?|Heranwachsende[rn]?|Ganze[sn]?|Gefangene[rn]?|Steuerpflichtige[rn]?|Geschädigte[rn]?|Heimatvertriebenen?|Schwerverletzte[rn]?|Werbenden?|Au(ß|ss)enstehenden?|Forschenden?|Prominenten?|Pflegenden?|Beklagten?|Geistlichen?|Pflegebedürftigen?|(Teil|Voll)zeitbeschäftigten?|Fortgeschrittenen?|Promovierenden?|Schreibenden?|Ungeimpfte[nr]?|Geimpfte[nr]?|Tatverdächtige[nr]?|Pubertären?|Flüchtende[nr]?|Vortragende[nr]?|Besuchenden?|Vortragenden?|Verantwortliche[rn]?|Geflohene[rn]?|Sterbende[nr]?|Werbende[nr]?|Vortragende[nr]?|Alliierte[nr]?|Bedürftige[rn]?|Praktizierenden?|Geisteskranke[nr]?|Religiöse[rn]?|Kleinsten?|Dauerarbeitslose[rn]|Angesteckten?|Ortskundigen?|Steuerpflichtige[rn]?|Vorbehandelnden?|Gefährdeten?|Eingemachte|Geübten?|Schwimmenden?|Tauchenden?|Anständigen?|Liebenden?|Volljährigen?|Minderjährigen?|Zeichnungsberechtigte[rn]?|Zeichnungsbefugte[rn]?|Altbekannte[nmr]?|Hartnäckigen?|Unerfahrenen?|Arbeitenden?|Vortragende[nr]?|Dummen?|Fragenden?|Antwortenden?|Kriegs[gb]eschädigten?|Begünstigten?|Verfolgten?|Verwitweten?|Geschiedenen?|Asexuellen?|Liebsten?|(Rechts|Links)extremen?|(Aus|Ein)geschlossenen?|Betuchten?|Anteilnehmende[rn]?|Anbietenden?|Hochbetagten?|Seelenverwandte[nr]?|Gleichgestellten?|Gottlosen?|Inhaftierten?|Protestierenden?|Wohnungssuchenden?|Lesenden?|Schreibenden?|Beitragenden?|Superreichen?|Au(ß|ss)enstehenden?|Juryvorsitzende[rn]?|Introvertierten?|Extrovertierten?|.+begeisterten?|(Schwer|Seh)behinderten?|Unbekannten?|Anwesenden?|Personalverantwortlichen?|[NF]rühgeborenen?|Hörgeschädigten?|Gehorsamen?|Ungehorsamen?|Suchtkranken?|Bildbetrachtenden?|Uniformierten?|Bediensteten?|Gesetzlosen?|Vermummten?|(Schwer|Leicht)verletzten?|Untoten?|Hübschen?|Reisende[rn]?|Abtrünnigen?|Liebende[nr]?|Befehlenden?|Pubertierenden?|Lebenden?|Geistliche[rn]?|Klassenbeste[rn]?|Totgesagte[rn]?|Zivildienstleistende[rn]?|Nutzenden?|Kunstinteressierte[rn]?|Nachtaktive[nr]?|Bewerbenden?|Geliebter?|Unsterblichen?|Sterblichen?|Evangelikalen?|Gewaltbereiten?|Dozierenden?|Autofahenden?|Impfgeschädigten?"),
      csRegex("(?!(und|oder))[a-zäöü…\\.!:;,\\?…\\)\\*\\(“„\"»«–\\-].*")
    ),
    Arrays.asList(
      // wie oben, nur können die Adjektive auch als Verben gebraucht werden
      csRegex("[a-zäöü…\\.,:;0-9\\/$%].*"),
      csRegex("Vertraute[nr]?|Verwirrte[rn]?|Befragte[rn]?|Beschuldigte[rn]?|Interviewten?|Engagierten?|Beteiligte[nr]?|Verurteilte[rn]?"),
      csRegex("(?!(und|oder))[a-zäöü…\\.!:;,\\?…\\)\\*\\(“„\"»«–\\-].*")
    ),
    Arrays.asList( // Im Folgenden Kunde genannt
      token("im"),
      csRegex("Folgenden|Nachfolgenden")
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
    Arrays.asList(
      csToken("Für"),
      csToken("und"),
      csToken("Wider")
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
    Arrays.asList(
      token("("),
      csRegex("[!?]"),
      token(")"),
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
    ),
    Arrays.asList( // Ein Highlight für Klein und Groß!
      regex("für"),
      csToken("Klein"),
      csRegex("und|oder|&|/"),
      csToken("Groß")
    ),
    Arrays.asList(
      csToken("/"),
      csRegex("[A-ZÄÖÜ].+")
    ),
    Arrays.asList( // Ein Highlight für Groß und Klein!
      regex("für"),
      csToken("Groß"),
      csRegex("und|oder|&|/"),
      csToken("Klein")
    ),
    Arrays.asList( // Das sagen meine Kunden:
      posRegex("SENT_START|PKT"),
      token("das"),
      csRegex("sagen|beweisen|zeigen|schaffen|machen|halten|verstehen|versuchen"),
      new PatternTokenBuilder().posRegex("ADV.*").min(0).build(),
      csRegex("[dms]eine?|eure|die|unse?re|mehrere|einige|viele|manche|sonstige|diese|solche|andere|alle|wenige|jene|nicht|koennt?en|zwar|wird")
    ),
    Arrays.asList( // Das verstehen Deutsche eben nicht
      posRegex("SENT_START|PKT"),
      token("das"),
      csRegex("sagen|beweisen|zeigen|schaffen|machen|halten|verstehen|versuchen"),
      posRegex("SUB.+ADJ")
    ),
    Arrays.asList(
      csToken("das"),
      csRegex("sieben")
    ),
    Arrays.asList( // Das belegen mehrere Studien
      token("das"),
      csRegex("belegen")
    ),
    Arrays.asList( // (Gelächter) Das sind die …
      SENT_START,
      csRegex("\\(|\\{"),
      csRegex("[A-ZÄÖÜ].*"),
      csRegex("\\)|\\}"),
      csRegex("[A-ZÄÖÜ].*")
    ),
    Arrays.asList( // LaTeX
      SENT_START,
      token("\\"),
      csRegex("(no)?indent|item"),
      csRegex("[A-ZÄÖÜ].*")
    ),
    Arrays.asList( // [Gelächter] Das sind die …
      SENT_START,
      csToken("["),
      csRegex("[A-ZÄÖÜ].*"),
      csToken("]"),
      csRegex("[A-ZÄÖÜ].*")
    ),
    Arrays.asList( // 22:30 Das sind die …
      SENT_START,
      csRegex("\\d+"),
      csToken(":"),
      csRegex("\\d+"),
      csRegex("[A-ZÄÖÜ].*")
    ),
    Arrays.asList( // 12:00 - 12:30 Gemeinsames Mittagessen 
      SENT_START,
      csRegex("\\d+"),
      csToken(":"),
      csRegex("\\d+"),
      csRegex("[\\-–]"),
      csRegex("\\d+"),
      csToken(":"),
      csRegex("\\d+"),
      csRegex("[A-ZÄÖÜ].*")
    ),
    Arrays.asList( // (22:30) Das sind die …
      SENT_START,
      csRegex("\\(|\\["),
      csRegex("\\d+"),
      csToken(":"),
      csRegex("\\d+"),
      csRegex("\\)|\\]"),
      csRegex("[A-ZÄÖÜ].*")
    ),
    Arrays.asList( // Teil 1: Die Götter
      SENT_START,
      csToken("Teil"),
      csRegex("\\d+|I{1,3}|V|IV|VI{1,3}|IX|XI{1,3}"),
      csToken(":"),
      csRegex("[A-ZÄÖÜ].*")
    ),
    Arrays.asList( // ... ist das neue Normal.
      csToken("das"),
      csToken("neue"),
      csToken("Normal")
    ),
    Arrays.asList( // siehe hierzu: Argentinisches Antarktisterritorium
      csToken("siehe"),
      csToken("hierzu"),
      csToken(":"),
      csRegex("[A-ZÄÖÜ].*")
    ),
    Arrays.asList( // ... des vierten Offiziellen …
      csRegex("de[mrs]"),
      csRegex("vierten?"),
      csRegex("Offiziellen?")
    ),
    Arrays.asList( // Auf \foo{Weiter} klicken
      csRegex("[\\{\\[#]"),
      csRegex("[A-ZÄÖÜ].*")
    ),
    Arrays.asList( // Hallo, Kleines, wie geht es dir?
      token(","),
      new PatternTokenBuilder().posRegex("SUB.*SIN.*NEU.*ADJ|(ADJ|PA[12]).*SIN.*|UNKNOWN").csTokenRegex("[A-ZÄÖÜ].+es?").build(),
      token(",")
    ),
    Arrays.asList( // Es gibt mehr Neues
      csRegex("mehr|weniger|viel|nur"),
      new PatternTokenBuilder().posRegex("SUB.*SIN.*NEU.*ADJ|(ADJ|PA[12]).*SIN.*|UNKNOWN").csTokenRegex("(?!(Eines|Keines|Sonstiges|Anderes|Einiges))[A-ZÄÖÜ].+es").build()
    ),
    Arrays.asList(
      // Bei der Fülle an Vorgaben kann das schnell vergessen werden.
      csToken("das"),
      csRegex("halt|schnell|gar|sicher|bitte|gleich"),
      posRegex("VER:INF.*")
    ),
    Arrays.asList(
      // Dass du dir das gönnen tust
      csToken("das"),
      posRegex("VER:INF.*"),
      new PatternTokenBuilder().token("tun").matchInflectedForms().build()
    ),
    Arrays.asList(
      // Über das Gesagte Gedanken machen
      // Und das Vergangene Revue passieren lassen
      csRegex("das|dieses|[dmsk]ein"),
      new PatternTokenBuilder().posRegex("SUB.*SIN.*NEU.*ADJ|(ADJ|PA[12]).*SIN.*NEU.*|UNKNOWN").csTokenRegex("(?!(Die|Diese|Alle|Eine|Jene|[DMSK]eine|Andere|Eure|Unse?re|Sonstige|Einige|Manche|Ohne|Welche|Viele|Solche))[A-ZÄÖÜ].+e").build(),
      posRegex("SUB.*PLU.*(FEM|NEU|MAS)|SUB.*NOM.*SIN.*FEM")
    ),
    Arrays.asList(
      // Während der Befragte Geschichten erzählte
      csRegex("der|dieser|[msdk]ein|euer|unser|ihr"),
      new PatternTokenBuilder().posRegex("SUB.*SIN.*ADJ|(ADJ|PA[12]).*SIN.*|UNKNOWN").csTokenRegex("(?!(Die|Diese|Alle|Eine|Jene|[DMSK]eine|Andere|Eure|Unse?re|Sonstige|Einige|Manche|Ohne|Welche|Viele|Solche))[A-ZÄÖÜ].+e").build(),
      posRegex("SUB.*PLU.*(FEM|NEU|MAS)")
    ),
    Arrays.asList(
      // Während des Hochwassers den Eingeschlossenen Wasser und Nahrung bringen
      csRegex("den|diesen|[msdk]einen|unse?ren|euren|ihren"),
      new PatternTokenBuilder().posRegex("SUB.*SIN.*ADJ|(ADJ|PA[12]).*SIN.*|UNKNOWN").csTokenRegex("(?!(Den|Diesen|Allen|Einen|Jenen|[DMSK]einen|Anderen|Euren|Unse?ren|Sonstigen|Einigen|Manchen|Welchen|Vielen|Solchen))[A-ZÄÖÜ].+en").build(),
      posRegex("SUB.*NOM.*SIN.*(FEM|NEU)")
    ),
    Arrays.asList(
      // Während ein Befragter Geschichten erzählte
      csRegex("[msdk]?ein"),
      new PatternTokenBuilder().posRegex("SUB.*SIN.*ADJ|(ADJ|PA[12]).*SIN.*|UNKNOWN").csTokenRegex("(?!(Der|Dieser|Aller|Einer|Jener|[DMSK]einer|Anderer|Eurer|Unse?rer|Sonstiger|Einiger|Mancher|Welcher|Vieler|Solcher))[A-ZÄÖÜ].+er").build(),
      posRegex("SUB.*PLU.*(FEM|NEU|MAS)")
    ),
    Arrays.asList(
      // Während die Besagte Geld verdiente
      // Während die Besagte Geschichten erzählte
      csRegex("die|diese|[msdk]eine"),
      new PatternTokenBuilder().posRegex("SUB.*SIN.*ADJ|(ADJ|PA[12]).*SIN.*|UNKNOWN").csTokenRegex("(?!(Die|Diese|Alle|Eine|Jene|[DMSK]eine|Andere|Eure|Unse?re|Sonstige|Einige|Manche|Ohne|Welche|Viele|Solche))[A-ZÄÖÜ].+e").build(),
      posRegex("SUB.*NOM.*SIN.*(MAS|NEU)|SUB.*NOM.*PLU.*(FEM|NEU|MAS)")
    ),
    Arrays.asList(
      // Mit Gesagtem Geschichten schreiben
      new PatternTokenBuilder().posRegex("SUB.*SIN.*ADJ|(ADJ|PA[12]).*SIN.*|UNKNOWN").csTokenRegex("(?!(Diesem|Allem|Einem|Jenem|[DMSK]einem|Anderem|Eurem|Unse?rem|Sonstigem|Einigem|Manchem|Welchem|Vielem|Solchem))[A-ZÄÖÜ].+em").build(),
      posRegex("SUB.*SIN.*FEM|SUB.*PLU.*(FEM|NEU|MAS)")
    ),
    Arrays.asList(
      // Während Besagtes Probleme verursacht
      new PatternTokenBuilder().posRegex("SUB.*SIN.*ADJ|(ADJ|PA[12]).*SIN.*|UNKNOWN").csTokenRegex("(?!(Dieses|Alles|Eines|Jenes|[DMSK]eines|Anderes|Eures|Unse?res|Sonstiges|Einiges|Manches|Welches|Vieles|Solches))[A-ZÄÖÜ].+es").build(),
      new PatternTokenBuilder().posRegexWithStringException("SUB.*SIN.*(FEM|MAS)|SUB.*PLU.*(FEM|NEU|MAS)", "Band|Kapitel|Maß|.*[Vv]erbrechen|Orchester|Gestalten|Gebirge|.*[vV]orkommen|.*[Vv]erfahren|.*[gG]utachten|Schreiben|Bayern|Theater|Verlangen|.*[vV]erhalten|.*[Aa]benteuer|.*[wW]asser|Leben|Bauen|.*[gG]ewerbe|.*[Zz]immer|.*[Ee]ssen").build()
    ),
    Arrays.asList(
      // Hashtags
      token("#"),
      new PatternTokenBuilder().tokenRegex("[A-Z].*").setIsWhiteSpaceBefore(false).build()
    ),
    Arrays.asList(
      // Jetzt, wo Protestierende und Politiker sich streiten
      new PatternTokenBuilder().posRegex("SUB.*SIN.*NEU.*ADJ|(ADJ|PA[12]).*SIN.*NEU.*|UNKNOWN").csTokenRegex("(?!(Die|Diese|Alle|Eine|Jene|[DMSK]eine|Andere|Eure|Unse?re|Sonstige|Einige|Manche|Ohne|Welche|Viele|Solche))[A-ZÄÖÜ].+e").build(),
      csRegex("und|oder|&"),
      posRegex("SUB.*NOM.*PLU.*(MAS|FEM|NEU)")
    ),
    Arrays.asList(
      // Hier ist Text. (Und dann schreibe ich etwas in Klammern.) Nach der Klammer möchte LT klein weiterschreiben.
      csToken("."),
      csToken(")"),
      csRegex("[A-ZÄÖÜ].+")
    ),
    Arrays.asList(
      // Wenn Sie Strg+Umschalt+I drücken
      csRegex("Strg|STRG|Alt|ALT"),
      csRegex("und|&|oder|\\+"),
      csToken("Umschalt")
    ),
    Arrays.asList(
      // Wenn Sie Strg+Umschalt+I drücken
      csToken("Umschalt"),
      csRegex("und|&|oder|\\+")
    ),
    Arrays.asList(
      csRegex("[Ii]m"),
      csRegex("Inneren|Äu(ss|ß)eren")
    ),
    Arrays.asList(
      // denke aber, dass die das machen werden.
      csRegex("ob|dass|weswegen|damit|sofern|wie|wann|wo|wozu|warum"),
      csRegex("wir|[Ss]ie|ich|er|die|der|es|du|ihr"),
      csToken("das"),
      posRegex("VER:INF.*")
    ),
    Arrays.asList(
      posRegex("KON.*"),
      csRegex("wir|[Ss]ie|ich|er|die|der|es|du|ihr"),
      csToken("das"),
      posRegex("VER:INF.*")
    ),
    Arrays.asList(
      csRegex("ob|dass|weswegen|damit|sofern|wie|wann|wo|wozu|warum"),
      posRegex("EIG.*|UNKNOWN"),
      csToken("das"),
      posRegex("VER:INF.*")
    ),
    Arrays.asList(
      posRegex("KON.*"),
      posRegex("EIG.*|UNKNOWN"),
      csToken("das"),
      posRegex("VER:INF.*")
    ),
    Arrays.asList(
      // Filme drehen muss mir Spaß machen, und das machen Organisation, Finanzierung, Logistik nicht, deswegen sind meine Filme nicht aufwändig.
      csRegex("und|oder|&"),
      csToken("das"),
      posRegex("VER:INF.*"),
      posRegex("SUB.*")
    )
  );
}
