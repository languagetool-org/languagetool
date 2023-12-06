/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Mark Baas
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
package org.languagetool.rules.nl;

import com.google.common.collect.ImmutableSet;
import org.languagetool.*;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tagging.Tagger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Accept Dutch compounds that are not accepted by the speller. This code
 * is supposed to accept more words in order to extend the speller, but it's not meant
 * to accept all valid compounds.
 * It works on 2-part compounds only for now.
 */
public class CompoundAcceptor {

  private static final Pattern acronymPattern = Pattern.compile("[A-Z]{2,4}-");
  private static final Pattern normalCasePattern = Pattern.compile("[A-Za-z][a-zé]*");
  private static final int MAX_WORD_SIZE = 35;

  // if part 1 ends with this, it always needs an 's' appended
  private final Set<String> alwaysNeedsS = ImmutableSet.of(
    "heids",
    "ings",
    "schaps",
    "teits"
  );
  // compound parts that need an 's' appended to be used as first part of the compound:
  private final Set<String> needsS = ImmutableSet.of(
    "afgods",
    "allemans",
    "arbeiders",
    "bedrijfs",
    "dorps",
    "eindejaars",
    "etens",
    "gevechts",
    "gezichts",
    "jongens",
    "levens",
    "lijdens",
    "meisjes",
    "onderhouds",
    "oorlogs",
    "overlijdens",
    "passagiers",
    "personeels",
    "varkens",
    "vrijwilligers"
  );

  // exceptions to the list "alwaysNeedsS"
  private final Set<String> part1Exceptions = ImmutableSet.of(
    "belasting",
    "dating",
    "doping",
    "gaming",
    "grooming",
    "honing",
    "kleding",
    "kring",
    "matching",
    "outsourcing",
    "paling",
    "rekening",
    "spring",
    "styling",
    "tracking",
    "tweeling",
    "viking"
  );
  private final Set<String> part2Exceptions = ImmutableSet.of(
    "ding",
    "lijk",
    "lopen",
    "mara",
    "ping",
    "raat",
    "reek",
    "reen",
    "stag",
    "sten",
    "tand",
    "ting",
    "voor"
  );
  private final Set<String> acronymExceptions = ImmutableSet.of(
    "AIDS",
    "ALV",
    "AMVB",
    "ANW",
    "APK",
    "ARBO",
    "AWB",
    "BBL",
    "BEVI",
    "BOPZ",
    "BSO",
    "CAO",
    "DVD",
    "ECG",
    "GPS",
    "GSM",
    "HBS",
    "HIFI",
    "HIV",
    "HRM",
    "HSL",
    "HTS",
    "HVB",
    "HVW",
    "IPAD",
    "IPOD",
    "LBO",
    "LCD",
    "LTS",
    "MBO",
    "MDF",
    "MKB",
    "OPW",
    "OZB",
    "PDF",
    "PGB",
    "SMS",
    "SOA",
    "TBS",
    "UFO",
    "VIP",
    "VWO",
    "WABO",
    "WAZ",
    "WAZO",
    "WIFI",
    "WFT",
    "WIV",
    "WLZ",
    "WONW",
    "WRO",
    "WTA",
    "WVK",
    "WVS",
    "WWFT",
    "WWIK",
    "WZD",
    "XTC",
    "ZVW",
    "ZZP"
  );
  // compound parts that must not have an 's' appended to be used as first part of the compound:
  private final Set<String> noS = ImmutableSet.of(
    "aandeel",
    "aangifte",
    "aanname",
    "accessoire",
    "achtergrond",
    "adres",
    "afgifte",
    "afname",
    "aids",
    "akte",
    "algoritme",
    "allure",
    "ambulance",
    "analyse",
    "anekdote",
    "antenne",
    "attitude",
    "auto",
    "balustrade",
    "bediende",
    "behoefte",
    "belangen",
    "belofte",
    "bende",
    "beroerte",
    "bezoek",
    "bijdrage",
    "bijlage",
    "binnenste",
    "blessure",
    "boeren",
    "boete",
    "bolide",
    "breedte",
    "brigade",
    "café",
    "cantate",
    "cassette",
    "catastrofe",
    "collecte",
    "competitie",
    "contract",
    "controverse",
    "curve",
    "detective",
    "diagnose",
    "dienst",
    "diepte",
    "dikte",
    "douane",
    "droogte",
    "einde",
    "ellende",
    "energie",
    "episode",
    "estafette",
    "etappe",
    "expertise",
    "façade",
    "familie",
    "fanfare",
    "fase",
    "feest",
    "finale",
    "fluoride",
    "fractie",
    "gebergte",
    "geboorte",
    "gedaante",
    "gedachte",
    "gedeelte",
    "gehalte",
    "gemeente",
    "gemiddelde",
    "genade",
    "genocide",
    "gestalte",
    "gesteente",
    "gevaarte",
    "gewoonte",
    "gezegde",
    "gilde",
    "gravure",
    "groente",
    "grootte",
    "halte",
    "hectare",
    "holte",
    "hoofd",
    "hoogte",
    "horde",
    "hybride",
    "hypothese",
    "impasse",
    "informatie",
    "inname",
    "inzage",
    "kade",
    "karakter",
    "kazerne",
    "keuze",
    "kinder",
    "krapte",
    "kudde",
    "lade",
    "leegte",
    "legende",
    "lengte",
    "liefde",
    "literatuur",
    "lucht",
    "luchtvaart",
    "made",
    "mannen",
    "mascotte",
    "mechanisme",
    "mede",
    "menigte",
    "mensenrechten",
    "metamorfose",
    "methode",
    "meute",
    "module",
    "mythe",
    "novelle",
    "nuance",
    "oase",
    "offerte",
    "onderwijs",
    "oorkonde",
    "oplage",
    "opname",
    "orde",
    "organisatie",
    "organisme",
    "orgasme",
    "overname",
    "papier",
    "pauze",
    "pedicure",
    "periode",
    "piramide",
    "piste",
    "politie",
    "privé",
    "probleem",
    "productie",
    "prothese",
    "prototype",
    "psychose",
    "pyjama",
    "rente",
    "ritme",
    "ronde",
    "rotonde",
    "route",
    "ruimte",
    "ruimtevaart",
    "ruïne",
    "satire",
    "schaarste",
    "schade",
    "school",
    "seconde",
    "secretaresse",
    "sekte",
    "sterfte",
    "sterkte",
    "stilte",
    "straat",
    "studenten",
    "synagoge",
    "synode",
    "synthese",
    "telefoon",
    "televisie",
    "tenue",
    "terzijde",
    "theater",
    "toelage",
    "tombe",
    "trede",
    "tube",
    "type",
    "uiteinde",
    "uitgifte",
    "verloofde",
    "verte",
    "vete",
    "vitamine",
    "vlakte",
    "vogel",
    "volume",
    "voorbeeld",
    "voorbode",
    "voorhoede",
    "voorliefde",
    "voorronde",
    "vreugde",
    "vrouwen",
    "waarde",
    "warmte",
    "weduwe",
    "weergave",
    "weide",
    "wereld",
    "woning",
    "woord",
    "ziekte",
    "zijde",
    "zonde",
    "zwaarte",
    "zwakte",
    "zwanger"
  );
  // Make sure we don't allow compound words where part 1 ends with a specific vowel and part2 starts with one, for words like "politieeenheid".
  private final Set<String> collidingVowels = ImmutableSet.of(
    "aa", "ae", "ai", "au", "ee", "ée", "ei", "éi", "eu", "éu", "ie", "ii", "ij", "oe", "oi", "oo", "ou", "ui", "uu"
  );

  private static final MorfologikDutchSpellerRule speller;
  static {
    try {
      speller = new MorfologikDutchSpellerRule(JLanguageTool.getMessageBundle(), Languages.getLanguageForShortCode("nl"), null);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private final Tagger tagger;

  CompoundAcceptor() {
    tagger = Languages.getLanguageForShortCode("nl").getTagger();
  }

  public CompoundAcceptor(Tagger tagger) {
    this.tagger = tagger;
  }

  boolean acceptCompound(String word) throws IOException {
    if (word.length() > MAX_WORD_SIZE) {  // prevent long runtime
      return false;
    }
    for (int i = 3; i < word.length() - 3; i++) {
      String part1 = word.substring(0, i);
      String part2 = word.substring(i);
      if (acceptCompound(part1, part2)) {
        System.out.println(part1+part2 + " -> accepted");
        return true;
      }
    }
    return false;
  }

  public List<String> getParts(String word) {
    if (word.length() > MAX_WORD_SIZE) {  // prevent long runtime
      return Collections.emptyList();
    }
    for (int i = 3; i < word.length() - 3; i++) {
      String part1 = word.substring(0, i);
      String part2 = word.substring(i);
      if (acceptCompound(part1, part2)) {
        return Arrays.asList(part1, part2);
      }
    }
    return Collections.emptyList();
  }

  boolean acceptCompound(String part1, String part2) {
    try {
      String part1lc = part1.toLowerCase();
      // reject if it's in the exceptions list
      if (part1.endsWith("s") && !part1Exceptions.contains(part1.substring(0, part1.length() -1)) && !noS.contains(part1)) {
        for (String suffix : alwaysNeedsS) {
          if (part1lc.endsWith(suffix)) {
            return isNoun(part2) && spellingOk(part1.substring(0, part1.length() - 1)) && spellingOk(part2);
          }
        }
        return needsS.contains(part1lc) && isNoun(part2) && spellingOk(part1.substring(0, part1.length() - 1)) && spellingOk(part2);
      } else if (part1.endsWith("-")) { // abbreviations
        return abbrevOk(part1) && spellingOk(part2);
      } else if (part2.startsWith("-")) { // vowel collision
        part2 = part2.substring(1);
        return noS.contains(part1lc) && isNoun(part2) && spellingOk(part1) && spellingOk(part2) && hasCollidingVowels(part1, part2);
      } else {
        return (noS.contains(part1lc) || part1Exceptions.contains(part1lc)) && isNoun(part2) && spellingOk(part1) && spellingOk(part2) && !hasCollidingVowels(part1, part2);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  boolean isNoun(String word) throws IOException {
    List<AnalyzedTokenReadings> part2Readings = tagger.tag(Arrays.asList(word));
    return part2Readings.stream().anyMatch(k -> k.hasPosTagStartingWith("ZNW")) && !part2Exceptions.contains(word) ;
  }

  private boolean hasCollidingVowels(String part1, String part2) {
    char char1 = part1.charAt(part1.length() - 1);
    char char2 = part2.charAt(0);
    String vowels = String.valueOf(char1) + char2;
    return collidingVowels.contains(vowels.toLowerCase());
  }

  private boolean abbrevOk(String nonCompound) {
    // for compound words like IRA-akkoord, MIDI-bestanden, WK-finalisten
    if( acronymPattern.matcher(nonCompound).matches() ){
      return !acronymExceptions.contains(nonCompound.split("-")[0]);
    }
    return false;
  }

  private boolean spellingOk(String nonCompound) throws IOException {
    if (!normalCasePattern.matcher(nonCompound).matches()) {
      return false;   // e.g. kinderenHet -> split as kinder,enHet
    }
    AnalyzedSentence as = new AnalyzedSentence(new AnalyzedTokenReadings[] {
      new AnalyzedTokenReadings(new AnalyzedToken(nonCompound.toLowerCase(), "FAKE_POS", "fakeLemma"))
    });
    RuleMatch[] matches = speller.match(as);
    return matches.length == 0;
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: " + CompoundAcceptor.class.getName() + " <file>");
      System.exit(1);
    }
    CompoundAcceptor acceptor = new CompoundAcceptor();
    List<String> words = Files.readAllLines(Paths.get(args[0]));
    for (String word : words) {
      boolean accepted = acceptor.acceptCompound(word);
      System.out.println(accepted + " " + word);
    }
  }

}
