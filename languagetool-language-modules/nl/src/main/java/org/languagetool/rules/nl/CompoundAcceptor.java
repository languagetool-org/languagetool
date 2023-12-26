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
import org.languagetool.tagging.nl.DutchTagger;

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
  private static final Pattern specialAcronymPattern = Pattern.compile("[A-Za-z]{2,4}-");
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
    "afstands",
    "allemans",
    "arbeids",
    "arbeiders",
    "bedrijfs",
    "beroeps",
    "dorps",
    "eindejaars",
    "etens",
    "gebruiks",
    "gebruikers",
    "geluids",
    "gevechts",
    "gezichts",
    "handels",
    "jongens",
    "langeafstands",
    "levens",
    "lijdens",
    "martelaars",
    "meisjes",
    "onderhouds",
    "onderzoeks",
    "oorlogs",
    "oudejaars",
    "ouderdoms",
    "overlijdens",
    "padvinders",
    "passagiers",
    "personeels",
    "rijks",
    "scheeps",
    "staats",
    "stads",
    "varkens",
    "verkeers",
    "volks",
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
    "actor",
    "actoren",
    "baar",
    "baren",
    "ding",
    "enen",
    "fries",
    "lijk",
    "loos",
    "lopen",
    "lozen",
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
    "aids",
    "alv",
    "AMvB",
    "Anw",
    "apk",
    "arbo",
    "Awb",
    "bbl",
    "Bevi",
    "Bopz",
    "bso",
    "btw",
    "cao",
    "cd",
    "dj",
    "dvd",
    "ecg",
    "gft",
    "ggz",
    "gps",
    "gsm",
    "hbs",
    "hifi",
    "hiv",
    "hr",
    "hrm",
    "hsl",
    "hts",
    "Hvb",
    "Hvw",
    "iMac",
    "iOS",
    "iPad",
    "iPod",
    "ivf",
    "lbo",
    "lcd",
    "lts",
    "mbo",
    "mdf",
    "mkb",
    "Opw",
    "ozb",
    "pc",
    "pdf",
    "pgb",
    "sms",
    "soa",
    "tbs",
    "tv",
    "ufo",
    "vip",
    "vwo",
    "Wabo",
    "Waz",
    "Wazo",
    "Wbp",
    "wifi",
    "Wft",
    "Wlz",
    "WvS",
    "Wwft",
    "Wzd",
    "xtc",
    "Zvw",
    "zzp"
  );
  // compound parts that must not have an 's' appended to be used as first part of the compound:
  private final Set<String> noS = ImmutableSet.of(
    "aandeel",
    "aangifte",
    "aanname",
    "aard",
    "aardappel",
    "aardbeien",
    "aardgas",
    "aardolie",
    "achter",
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
    "anders",
    "anekdote",
    "antenne",
    "attitude",
    "auto",
    "baby",
    "bal",
    "balustrade",
    "bank",
    "basis",
    "bediende",
    "beeld",
    "beeldhouw",
    "behoefte",
    "belangen",
    "belofte",
    "bende",
    "berg",
    "beroerte",
    "bestek",
    "bestel",
    "bezoek",
    "bier",
    "bijdrage",
    "bijlage",
    "binnenste",
    "blad",
    "blessure",
    "bloed",
    "bodem",
    "boeren",
    "boete",
    "bolide",
    "bom",
    "bouw",
    "brand",
    "breedte",
    "brigade",
    "buiten",
    "burger",
    "buurt",
    "café",
    "campagne",
    "camping",
    "cantate",
    "cassette",
    "centrum",
    "catastrofe",
    "collecte",
    "collega",
    "competitie",
    "computer",
    "contract",
    "contra",
    "contrast",
    "controverse",
    "cult",
    "cultuur",
    "curve",
    "dag",
    "data",
    "deel",
    "deeltijd",
    "demo",
    "demonstratie",
    "detective",
    "diagnose",
    "dienst",
    "diepte",
    "diepzee",
    "dikte",
    "disco",
    "doel",
    "douane",
    "droogte",
    "druk",
    "dubbel",
    "eind",
    "einde",
    "ellende",
    "energie",
    "episode",
    "estafette",
    "etappe",
    "expertise",
    "façade",
    "familie",
    "fan",
    "fanfare",
    "fantasie",
    "fase",
    "feest",
    "film",
    "finale",
    "fluoride",
    "formatie",
    "foto",
    "fractie",
    "gast",
    "gebergte",
    "geboorte",
    "gedaante",
    "gedachte",
    "gedeelte",
    "gehalte",
    "geld",
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
    "glas",
    "goederen",
    "goud",
    "gravure",
    "groente",
    "grond",
    "grootte",
    "haar",
    "half",
    "halte",
    "hand",
    "hectare",
    "holte",
    "hoofd",
    "hoog",
    "hoogte",
    "horde",
    "hout",
    "huis",
    "hulp",
    "hybride",
    "hyper",
    "hypothese",
    "impasse",
    "info",
    "informatie",
    "inname",
    "internet",
    "inzage",
    "jaar",
    "jeugd",
    "jongeren",
    "kade",
    "kamp",
    "kampeer",
    "kantoor",
    "karakter",
    "kazerne",
    "kerk",
    "kern",
    "kerst",
    "keuze",
    "kind",
    "kinder",
    "klei",
    "klim",
    "klimaat",
    "koffie",
    "kool",
    "koolmees",
    "koolstof",
    "koolzaad",
    "krapte",
    "kruis",
    "kudde",
    "kunst",
    "lade",
    "langetermijn",
    "leegte",
    "leer",
    "legende",
    "lengte",
    "licht",
    "liefde",
    "literatuur",
    "loon",
    "loop",
    "lucht",
    "luchtvaart",
    "maan",
    "maat",
    "machine",
    "made",
    "mannen",
    "markt",
    "martel",
    "mascotte",
    "massa",
    "massage",
    "materiaal",
    "mechanisme",
    "mede",
    "media",
    "melk",
    "menigte",
    "mensen",
    "mensenrechten",
    "merk",
    "meta",
    "metaal",
    "metamorfose",
    "methode",
    "meute",
    "micro",
    "midden",
    "mijn",
    "milieu",
    "mini",
    "mode",
    "model",
    "module",
    "moeder",
    "motor",
    "multi",
    "multimedia",
    "muziek",
    "mythe",
    "nacht",
    "natuur",
    "nieuws",
    "nood",
    "novelle",
    "nuance",
    "oase",
    "offerte",
    "olie",
    "onderwijs",
    "oorkonde",
    "oplage",
    "opname",
    "opper",
    "orde",
    "organisatie",
    "organisme",
    "orgasme",
    "ouderen",
    "overname",
    "padden",
    "papier",
    "park",
    "parkeer",
    "partij",
    "party",
    "passie",
    "pauze",
    "pedicure",
    "periode",
    "pers",
    "piramide",
    "piste",
    "plas",
    "plasma",
    "plastic",
    "polis",
    "politie",
    "portefeuille",
    "portiek",
    "portret",
    "post",
    "prijs",
    "privé",
    "probleem",
    "product",
    "productie",
    "proef",
    "prof",
    "project",
    "prothese",
    "prototype",
    "psychose",
    "pyjama",
    "radio",
    "rap",
    "reis",
    "regen",
    "regio",
    "rente",
    "rest",
    "restauratie",
    "rij",
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
    "scheer",
    "schild",
    "school",
    "schoon",
    "seconde",
    "secretaresse",
    "sekte",
    "ski",
    "slaap",
    "slaapkamer",
    "span",
    "spiegel",
    "spier",
    "spinnen",
    "spoor",
    "sport",
    "stamp",
    "stand",
    "standaard",
    "start",
    "steen",
    "stem",
    "ster",
    "stereo",
    "sterfte",
    "sterkte",
    "stilte",
    "stof",
    "straat",
    "stroom",
    "studenten",
    "super",
    "synagoge",
    "synode",
    "synthese",
    "taal",
    "telefoon",
    "televisie",
    "tenue",
    "terzijde",
    "test",
    "theater",
    "thuis",
    "toelage",
    "toer",
    "toeristen",
    "tombe",
    "trans",
    "transfer",
    "transport",
    "trede",
    "trek",
    "tube",
    "tuin",
    "type",
    "uiteinde",
    "uitgifte",
    "vakantie",
    "veld",
    "verloofde",
    "verte",
    "vete",
    "video",
    "vip",
    "vitamine",
    "vlakte",
    "voet",
    "voeten",
    "voetbal",
    "vogel",
    "volume",
    "voorbeeld",
    "voorbode",
    "voorhoede",
    "voorliefde",
    "voorronde",
    "vreugde",
    "vrienden",
    "vrouwen",
    "vuur",
    "waarde",
    "wand",
    "wandel",
    "warmte",
    "water",
    "waterstof",
    "wiel",
    "wieler",
    "wind",
    "winter",
    "wijn",
    "web",
    "weduwe",
    "week",
    "weer",
    "weergave",
    "weide",
    "wereld",
    "werk",
    "winkel",
    "woning",
    "woord",
    "zand",
    "ziekte",
    "zijde",
    "zomer",
    "zonde",
    "zorg",
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

  private DutchTagger dutchTagger = DutchTagger.INSTANCE;

  public CompoundAcceptor() {
  }

  boolean acceptCompound(String word) {
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
      // reject if it's in the exceptions list or if a wildcard is the entirety of part1
      if (part1.endsWith("s") && !part1Exceptions.contains(part1.substring(0, part1.length() -1)) && !alwaysNeedsS.contains(part1) && !noS.contains(part1) && !part1.contains("-")) {
        for (String suffix : alwaysNeedsS) {
          if (part1lc.endsWith(suffix)) {
            return isNoun(part2) && isExistingWord(part1lc.substring(0, part1lc.length() - 1)) && spellingOk(part2);
          }
        }
        return needsS.contains(part1lc) && isNoun(part2) && spellingOk(part1.substring(0, part1.length() - 1)) && spellingOk(part2);
      } else if (part1.endsWith("-")) { // abbreviations
        return acronymOk(part1) && spellingOk(part2);
      } else if (part2.startsWith("-")) { // vowel collision
        part2 = part2.substring(1);
        return noS.contains(part1lc) && isNoun(part2) && spellingOk(part1) && spellingOk(part2) && hasCollidingVowels(part1, part2);
      } else {
        return (noS.contains(part1lc) || part1Exceptions.contains(part1lc)) && isNoun(part2) && spellingOk(part1) && !hasCollidingVowels(part1, part2);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean isNoun(String word) throws IOException {
    return dutchTagger.getPostags(word).stream().anyMatch(k -> {
      assert k.getPOSTag() != null;
      return k.getPOSTag().startsWith("ZNW") && !part2Exceptions.contains(word);
    });
  }

  private boolean isExistingWord(String word) throws IOException {
    return dutchTagger.getPostags(word).stream().anyMatch(k -> k.getPOSTag() != null);
  }

  private boolean hasCollidingVowels(String part1, String part2) {
    char char1 = part1.charAt(part1.length() - 1);
    char char2 = part2.charAt(0);
    String vowels = String.valueOf(char1) + char2;
    return collidingVowels.contains(vowels.toLowerCase());
  }

  private boolean acronymOk(String nonCompound) {
    // for compound words like IRA-akkoord, MIDI-bestanden, WK-finalisten
    if ( acronymPattern.matcher(nonCompound).matches() ){
      return acronymExceptions.stream().noneMatch(exception -> exception.toUpperCase().equals(nonCompound.substring(0, nonCompound.length() -1)));
    } else if ( specialAcronymPattern.matcher(nonCompound).matches() ) {
      // special case acronyms that are accepted only with specific casing
      return acronymExceptions.contains(nonCompound.substring(0, nonCompound.length() -1));
    } else {
      return false;
    }
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
