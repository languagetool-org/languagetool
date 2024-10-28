/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Jaume Ortolà
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

package org.languagetool.rules.ca;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PronomsFeblesHelper {

  enum PronounPosition {
    DAVANT, DAVANT_APOS, DARRERE, DARRERE_APOS, DARRERE_NOGUIONET_NOAPOS, DARRE_APOS_NOGUIONET_NOAPOS
  }
  final static private String[] pronomsFebles = {
    "el", "l'", "-lo", "'l", "lo", "l",
    "els el", "els l'", "-los-el", "'ls-el", "losel", "lsel",
    "els els", "els els", "-los-els", "'ls-els", "losels", "lsels",
    "els en", "els n'", "-los-en", "'ls-en", "losen", "lsen",
    "els hi", "els hi", "-los-hi", "'ls-hi", "loshi", "lshi",
    "els ho", "els ho", "-los-ho", "'ls-ho", "losho", "lsho",
    "els la", "els l'", "-los-la", "'ls-la", "losla", "lsla",
    "els les", "els les", "-los-les", "'ls-les", "losles", "lsles",
    "els", "els", "-los", "'ls", "los", "ls",
    "em", "m'", "-me", "'m", "me", "m",
    "en", "n'", "-ne", "'n", "ne", "n",
    "ens el", "ens l'", "-nos-el", "'ns-el", "nosel", "nsel",
    "ens els", "ens els", "-nos-els", "'ns-els", "nosels", "nsels",
    "ens en", "ens n'", "-nos-en", "'ns-en", "nosen", "nsen",
    "ens hi", "ens hi", "-nos-hi", "'ns-hi", "noshi", "nshi",
    "ens ho", "ens ho", "-nos-ho", "'ns-ho", "nosho", "nsho",
    "ens la", "ens l'", "-nos-la", "'ns-la", "nosla", "nsla",
    "ens les", "ens les", "-nos-les", "'ns-les", "nosles", "nsles",
    "ens li", "ens li", "-nos-li", "'ns-li", "nosli", "nsli",
    "ens", "ens", "-nos", "'ns", "nos", "ns",
    "es", "s'", "-se", "'s", "se", "s",
    "et", "t'", "-te", "'t", "te", "t",
    "hi", "hi", "-hi", "-hi", "hi", "hi",
    "ho", "ho", "-ho", "-ho", "ho", "ho",
    "l'en", "el n'", "-l'en", "-l'en", "len", "len",
    "l'hi", "l'hi", "-l'hi", "-l'hi", "lhi", "lhi",
    "la hi", "la hi", "-la-hi", "-la-hi", "lahi", "lahi",
    "la", "l'", "-la", "-la", "la", "la",
    "la'n", "la n'", "-la'n", "-la'n", "lan", "lan",
    "les en", "les n'", "-les-en", "-les-en", "lesen", "lesen",
    "les hi", "les hi", "-les-hi", "-les-hi", "leshi", "leshi",
    "les", "les", "-les", "-les", "les", "les",
    "li hi", "li hi", "-li-hi", "-li-hi", "lihi", "lihi",
    "li ho", "li ho", "-li-ho", "-li-ho", "liho", "liho",
    "li la", "li l'", "-li-la", "-li-la", "lila", "lila",
    "li les", "li les", "-li-les", "-li-les", "liles", "liles",
    "li", "li", "-li", "-li", "li", "li",
    "li'l", "li l'", "-li'l", "-li'l", "lil", "lil",
    "li'ls", "li'ls", "-li'ls", "-li'ls", "lils", "lils",
    "li'n", "li n'", "-li'n", "-li'n", "lin", "lin",
    "m'hi", "m'hi", "-m'hi", "-m'hi", "mhi", "mhi",
    "m'ho", "m'ho", "-m'ho", "-m'ho", "mho", "mho",
    "me la", "me l'", "-me-la", "-me-la", "mela", "mela",
    "me les", "me les", "-me-les", "-me-les", "meles", "meles",
    "me li", "me li", "-me-li", "-me-li", "meli", "meli",
    "me'l", "me l'", "-me'l", "-me'l", "mel", "mel",
    "me'ls", "me'ls", "-me'ls", "-me'ls", "mels", "mels",
    "me'n", "me n'", "-me'n", "-me'n", "men", "men",
    "n'hi", "n'hi", "-n'hi", "-n'hi", "nhi", "nhi",
    "s'hi", "s'hi", "-s'hi", "-s'hi", "shi", "shi",
    "s'ho", "s'ho", "-s'ho", "-s'ho", "sho", "sho",
    "se la", "se l'", "-se-la", "-se-la", "sela", "sela",
    "se les", "se les", "-se-les", "-se-les", "seles", "seles",
    "se li", "se li", "-se-li", "-se-li", "seli", "seli",
    "se us", "se us", "-se-us", "-se-us", "seus", "seus",
    "se vos", "se vos", "-se-vos", "-se-vos", "sevos", "sevos",
    "se'l", "se l'", "-se'l", "-se'l", "sel", "sel",
    "se'ls", "se'ls", "-se'ls", "-se'ls", "sels", "sels",
    "se'm", "se m'", "-se'm", "-se'm", "sem", "sem",
    "se'n", "se n'", "-se'n", "-se'n", "sen", "sen",
    "se'ns", "se'ns", "-se'ns", "-se'ns", "sens", "sens",
    "se't", "se t'", "-se't", "-se't", "set", "set",
    "t'hi", "t'hi", "-t'hi", "-t'hi", "thi", "thi",
    "t'ho", "t'ho", "-t'ho", "-t'ho", "tho", "tho",
    "te la", "te l'", "-te-la", "-te-la", "tela", "tela",
    "te les", "te les", "-te-les", "-te-les", "teles", "teles",
    "te li", "te li", "-te-li", "-te-li", "teli", "teli",
    "te'l", "te l'", "-te'l", "-te'l", "tel", "tel",
    "te'ls", "te'ls", "-te'ls", "-te'ls", "tels", "tels",
    "te'm", "te m'", "-te'm", "-te'm", "tem", "tem",
    "te'n", "te n'", "-te'n", "-te'n", "ten", "ten",
    "te'ns", "te'ns", "-te'ns", "-te'ns", "tens", "tens",
    "us el", "us l'", "-vos-el", "-us-el", "vosel", "usel",
    "us els", "us els", "-vos-els", "-us-els", "vosels", "usels",
    "us em", "us m'", "-vos-em", "-us-em", "vosem", "usem",
    "us en", "us n'", "-vos-en", "-us-en", "vosen", "usen",
    "us ens", "us ens", "-vos-ens", "-us-ens", "vosens", "usens",
    "us hi", "us hi", "-vos-hi", "-us-hi", "voshi", "ushi",
    "us ho", "us ho", "-vos-ho", "-us-ho", "vosho", "usho",
    "us la", "us l'", "-vos-la", "-us-la", "vosla", "usla",
    "us les", "us les", "-vos-les", "-us-les", "vosles", "usles",
    "us li", "us li", "-vos-li", "-us-li", "vosli", "usli",
    "us", "us", "-vos", "-us", "vos", "us"
    // pronoms erronis?
    //"et", "t'", "-t", "'t"
  };

  final static Map<String, String> dativePronoun = new HashMap<>();
  static {
    dativePronoun.put("1S", "em");
    dativePronoun.put("2S", "et");
    dativePronoun.put("3S", "li");
    dativePronoun.put("3C", "li"); // also "els"
    dativePronoun.put("1P", "ens");
    dativePronoun.put("2P", "us");
    dativePronoun.put("3P", "els");
  }

  static Pattern pApostropheNeeded = Pattern.compile("h?[aeiouàèéíòóú].*", Pattern.CASE_INSENSITIVE);
  static Pattern pApostropheNeededEnd = Pattern.compile(".*[aei]", Pattern.CASE_INSENSITIVE);

  private static Map<String, String> addEnApostrophe = new HashMap<>();
  static {
    addEnApostrophe.put("m'", "me n'");
    addEnApostrophe.put("t'", "te n'");
    addEnApostrophe.put("s'", "se n'");
    addEnApostrophe.put("ens", "ens n'");
    addEnApostrophe.put("us", "us n'");
    addEnApostrophe.put("vos", "vos n'");
    addEnApostrophe.put("li", "li n'");
    addEnApostrophe.put("els", "els n'");
    addEnApostrophe.put("se m'", "se me n'");
    addEnApostrophe.put("se t'", "se te n'");
    addEnApostrophe.put("se li", "se li n'");
    addEnApostrophe.put("se'ns", "se'ns n'");
    addEnApostrophe.put("se us", "se us n'");
    addEnApostrophe.put("se vos", "se vos n'");
    addEnApostrophe.put("se'ls", "se'ls n'");
    addEnApostrophe.put("hi", "n'hi ");
    addEnApostrophe.put("", "n'");
  }

  private static Map<String, String> addEn = new HashMap<>();
  static {
    addEn.put("em", "me'n ");
    addEn.put("et", "te'n ");
    addEn.put("es", "se'n ");
    addEn.put("se", "se'n ");
    addEn.put("ens", "ens en ");
    addEn.put("us", "us en ");
    addEn.put("li", "li'n ");
    addEn.put("els", "els en ");
    addEn.put("se'm", "se me'n ");
    addEn.put("se't", "se te'n ");
    addEn.put("se li", "se li'n ");
    addEn.put("se'ns", "se'ns en ");
    addEn.put("se us", "se us en ");
    addEn.put("se vos", "se vos en ");
    addEn.put("se'ls", "se'ls en ");
    addEn.put("hi", "n'hi ");
    addEn.put("", "en ");
  }

  private static Map<String, String> addHi = new HashMap<>();
  static {
    addHi.put("em", "m'hi");
    addHi.put("et", "t'hi");
    addHi.put("es", "s'hi");
    addHi.put("se", "s'hi");
    addHi.put("ens", "ens hi");
    addHi.put("us", "us hi");
    addHi.put("li", "li hi");
    addHi.put("els", "els hi");
    addHi.put("", "hi");
  }

  private static Map<String, String> removeReflexive = new HashMap<>();
  static {
    removeReflexive.put("em", "");
    removeReflexive.put("me", "");
    removeReflexive.put("m'", "");
    removeReflexive.put("et", "");
    removeReflexive.put("te", "");
    removeReflexive.put("t'", "");
    removeReflexive.put("es", "");
    removeReflexive.put("se", "");
    removeReflexive.put("s'", "");
    removeReflexive.put("ens", "");
    removeReflexive.put("us", "");
    removeReflexive.put("vos", "");
    removeReflexive.put("se'm", "em");
    removeReflexive.put("se m'", "m'");
    removeReflexive.put("se't", "et");
    removeReflexive.put("se t'", "t'");
    removeReflexive.put("se l'", "l'");
    removeReflexive.put("se la", "la");
    removeReflexive.put("se li", "li");
    removeReflexive.put("se'ns", "ens");
    removeReflexive.put("se us", "us");
    removeReflexive.put("se'ls", "els");
    removeReflexive.put("s'ho", "ho");
    removeReflexive.put("m'ho", "ho");
    removeReflexive.put("t'ho", "ho");
    removeReflexive.put("ens ho", "ho");
    removeReflexive.put("us ho", "ho");
    removeReflexive.put("vos ho", "ho");
    removeReflexive.put("-me'l", "-lo");
    removeReflexive.put("-te'l", "-lo");
    removeReflexive.put("-se'l", "-lo");
    removeReflexive.put("-vos-el", "-lo");
    removeReflexive.put("-nos-el", "-lo");
    removeReflexive.put("-me-la", "-la");
    removeReflexive.put("-te-la", "-la");
    removeReflexive.put("-se-la", "-la");
    removeReflexive.put("-vos-la", "-la");
    removeReflexive.put("-nos-la", "-la");
    removeReflexive.put("-m'ho", "-ho");
    removeReflexive.put("-t'ho", "-ho");
    removeReflexive.put("-s'ho", "-ho");
    removeReflexive.put("-vos-ho", "-ho");
    removeReflexive.put("-nos-ho", "-ho");
  }

  private static Map<String, String> addReflexiveVowel = new HashMap<>();
  static {
    addReflexiveVowel.put("1S", "m'");
    addReflexiveVowel.put("2S", "t'");
    addReflexiveVowel.put("3S", "s'");
    addReflexiveVowel.put("1P", "ens ");
    addReflexiveVowel.put("2P", "us ");
    addReflexiveVowel.put("3P", "s'");
  }

  private static Map<String, String> addReflexiveConsonant = new HashMap<>();
  static {
    addReflexiveConsonant.put("1S", "em ");
    addReflexiveConsonant.put("2S", "et ");
    addReflexiveConsonant.put("3S", "es ");
    addReflexiveConsonant.put("1P", "ens ");
    addReflexiveConsonant.put("2P", "us ");
    addReflexiveConsonant.put("3P", "es ");
  }

  private static Map<String, String> addReflexiveImperative = new HashMap<>();
  static {
    addReflexiveImperative.put("2S", "'t");
    addReflexiveImperative.put("3S", "'s");
    addReflexiveImperative.put("1P", "-nos");
    addReflexiveImperative.put("2P", "-vos");
    addReflexiveImperative.put("3P", "-se");
  }

  private static Map<String, String> addEsEn = new HashMap<>();
  static {
    addEsEn.put("m'", "se me'n ");
    addEsEn.put("em", "se me'n ");
    addEsEn.put("me", "se me'n ");
    addEsEn.put("t'", "se te'n ");
    addEsEn.put("et", "se te'n ");
    addEsEn.put("te", "se te'n ");
    addEsEn.put("li", "se li'n ");
    addEsEn.put("ens", "se'ns en ");
    addEsEn.put("us", "se us en ");
    addEsEn.put("vos", "se vos en ");
    addEsEn.put("els", "se'ls en ");
  }

  private static Map<String, String> addEsEnApostrophe = new HashMap<>();
  static {
    addEsEnApostrophe.put("m'", "se me n'");
    addEsEnApostrophe.put("em", "se me n'");
    addEsEnApostrophe.put("me", "se me n'");
    addEsEnApostrophe.put("t'", "se te n'");
    addEsEnApostrophe.put("et", "se te n'");
    addEsEnApostrophe.put("te", "se te n'");
    addEsEnApostrophe.put("li", "se li n'");
    addEsEnApostrophe.put("ens", "se'ns n'");
    addEsEnApostrophe.put("us", "se us n'");
    addEsEnApostrophe.put("vos", "se vos n'");
    addEsEnApostrophe.put("els", "se'ls n'");
  }

  private static Pattern pronomFeble = Pattern.compile("P0.{6}|PP3CN000|PP3NN000|PP3..A00|PP[123]CP000|PP3CSD00");
  private static Pattern infinitiuGerundiImperatiu = Pattern.compile("V.[GNM].*");


  PronomsFeblesHelper() {
  }

  public static String transform(String inputPronom, PronounPosition pronounPos) {
    int i = 0;
    while (i < pronomsFebles.length && !inputPronom.equalsIgnoreCase(pronomsFebles[i])) {
      i++;
    }
    int pfPos = PronounPosition.values().length*(i / PronounPosition.values().length) + pronounPos.ordinal();
    if (pfPos > pronomsFebles.length - 1) {
      // pronom inexistent, p.ex. -t
      return "";
    }
    String pronom = pronomsFebles[pfPos];
    if (pronounPos == PronounPosition.DAVANT || (pronounPos == PronounPosition.DAVANT_APOS)
      && !pronom.endsWith("'")) {
      pronom = pronom + " ";
    }
    return pronom;
  }

  public static String transformDavant(String inputPronom, String nextWord) {
    if (pApostropheNeeded.matcher(nextWord).matches()) {
      return transform(inputPronom, PronounPosition.DAVANT_APOS);
    } else {
      return transform(inputPronom, PronounPosition.DAVANT);
    }
  }

  public static String transformDarrere(String inputPronom, String previousWord) {
    if (pApostropheNeededEnd.matcher(previousWord).matches()) {
      return transform(inputPronom, PronounPosition.DARRERE_APOS);
    } else {
      return transform(inputPronom, PronounPosition.DARRERE);
    }
  }

  public static String[] getTwoNextPronouns(AnalyzedTokenReadings[] tokens, int from) {
    String[] result = new String[2];
    int numPronouns = 0;
    String pronoms = "";
    if (from < tokens.length && !tokens[from].isWhitespaceBefore()) {
      AnalyzedToken pronom = tokens[from].readingWithTagRegex(pronomFeble);
      if (pronom != null) {
        pronoms = pronom.getToken();
        numPronouns++;
      }
      if (from + 1 < tokens.length && !tokens[from + 1].isWhitespaceBefore()) {
        AnalyzedToken pronom2 = tokens[from + 1].readingWithTagRegex(pronomFeble);
        if (pronom2 != null) {
          pronoms = pronoms + pronom2.getToken();
          numPronouns++;
        }
      }
    }
    result[0] = pronoms;
    result[1] = String.valueOf(numPronouns);
    return result;
  }

  public static String[] getPreviousPronouns(AnalyzedTokenReadings[] tokens, int toIndex) {
    String[] result = new String[2];
    int numPronouns = 0;
    StringBuilder pronouns = new StringBuilder();
    int fromIndex = toIndex;
    boolean done = false;
    while (fromIndex > 0 && !done) {
      AnalyzedToken pronom = tokens[fromIndex].readingWithTagRegex(pronomFeble);
      if (pronom != null) {
        if (fromIndex - 1 > 0 && !tokens[fromIndex].isWhitespaceBefore() && tokens[fromIndex - 1].readingWithTagRegex(infinitiuGerundiImperatiu) != null) {
          done = true;
        } else if (fromIndex - 2 > 0 && !tokens[fromIndex].isWhitespaceBefore() && !tokens[fromIndex - 1].isWhitespaceBefore()
          && tokens[fromIndex - 1].readingWithTagRegex(pronomFeble) != null && tokens[fromIndex - 2].readingWithTagRegex(infinitiuGerundiImperatiu) != null) {
          done = true;
        }
        if (!done) {
          fromIndex--;
          numPronouns++;
        }
      } else {
        done = true;
      }
    }
    if (numPronouns > 0) {
      for (int j = fromIndex + 1; j <= toIndex; j++) {
        if (j > fromIndex + 1 && j <= toIndex) {
          if (tokens[j].isWhitespaceBefore()) {
            pronouns.append(" ");
          }
        }
        pronouns.append(tokens[j].getToken());
      }
    }
    result[0] = pronouns.toString();
    result[1] = String.valueOf(numPronouns);
    return result;
  }

  public static String doAddPronounEn(String firstVerb, String pronounsStr, String verbStr, boolean pronounsAfter) {
    Map<String, String> transform;
    String replacement = "";
    String between = "";
    if (pApostropheNeeded.matcher(firstVerb).matches()) {
      transform = addEnApostrophe;
    } else {
      transform = addEn;
      //between = " ";
    }
    String pronounsReplacement = transform.get(pronounsStr.toLowerCase());
    if (pronounsReplacement != null) {
      replacement = pronounsReplacement + between + verbStr.toLowerCase();
    }
    return replacement;
  }

  public static String doAddPronounHi(String firstVerb, String pronounsStr, String verbStr, boolean pronounsAfter) {
    String replacement = "";
    String between = " ";
    String pronounsReplacement = addHi.get(pronounsStr.toLowerCase());
    if (pronounsReplacement != null) {
      replacement = pronounsReplacement + between + verbStr.toLowerCase();
    }
    return replacement;
  }

  public static String doRemovePronounReflexive(String firstVerb, String pronounsStr, String verbStr, boolean pronounsAfter) {
    String replacement = "";
    String pronounsReplacement = removeReflexive.get(pronounsStr.toLowerCase());
    if (pronounsAfter) {
      replacement = verbStr;
      if (pronounsReplacement!=null) {
        replacement = verbStr + pronounsReplacement;
      }
      return replacement;
    }
    String between = " ";
    if (pronounsReplacement != null) {
      replacement = (pronounsReplacement + between + verbStr).trim().replaceAll("' ", "'");
    } else {
      replacement = verbStr;
    }

    return replacement;
  }


  private static Pattern containsReflexivePronoun = Pattern.compile(".*([mts][e']|[e'][mts]|vos|us|ens|-nos|-vos).*");

  public static String doAddPronounReflexive(String firstVerb, String pronounsStr, String verbStr,
                                       String firstVerbPersonaNumber, boolean pronounsAfter) {
    String replacement = "";
    if (pronounsAfter) {
      if (containsReflexivePronoun.matcher(pronounsStr.toLowerCase()).matches()) {
        return verbStr + pronounsStr;
      }
      if (verbStr.endsWith("r") || verbStr.endsWith("re")) {
        return verbStr + transformDarrere("-se", verbStr);
      } else {
        return verbStr;
      }
    }
    String pronounToAdd = "";
    if (pronounsStr.isEmpty()) {
      if (pApostropheNeeded.matcher(verbStr).matches()) {
        pronounToAdd = addReflexiveVowel.get(firstVerbPersonaNumber);
      } else {
        pronounToAdd = addReflexiveConsonant.get(firstVerbPersonaNumber);
      }
      if (pronounToAdd != null) {
        replacement = (pronounToAdd + verbStr).trim().replaceAll("' ", "'");
      }
    } else {
      //TODO: add reflexive pronoun to another pronoun
      // containsReflexivePronoun.matcher(pronounsStr.toLowerCase()).matches()
      replacement = (pronounsStr + " " + verbStr).trim().replaceAll("' ", "'");
    }
    return replacement;
  }

  public static String doAddPronounReflexiveEn(String firstVerb, String pronounsStr, String verbStr,
                                             String firstVerbPersonaNumber, boolean pronounsAfter) {
    String replacement = "";
    if (pronounsAfter) {
      if (containsReflexivePronoun.matcher(pronounsStr.toLowerCase()).matches()) {
        return verbStr + transformDarrere(pronounsStr + "'n", verbStr); // no sempre correcte
      }
      return verbStr + transformDarrere("-se'n", verbStr);
    }
    String pronounToAdd = "";
    boolean needsApostrophe = pApostropheNeeded.matcher(verbStr).matches();
    if (pronounsStr.isEmpty()) {
      if (needsApostrophe) {
        pronounToAdd = addEnApostrophe.get(addReflexiveVowel.get(firstVerbPersonaNumber).trim());
      } else {
        pronounToAdd = addEn.get(addReflexiveConsonant.get(firstVerbPersonaNumber).trim());
      }
      if (pronounToAdd != null) {
        replacement = (pronounToAdd + verbStr).trim().replaceAll("' ", "'");
      }
    } else {
      if (needsApostrophe) {
        pronounToAdd = addEsEnApostrophe.get(pronounsStr);
      } else {
        pronounToAdd = addEsEn.get(pronounsStr);
      }
      if (pronounToAdd != null) {
        replacement = (pronounToAdd + verbStr).trim().replaceAll("' ", "'");
      } else {
        replacement = (pronounsStr + " " + verbStr).trim().replaceAll("' ", "'");
      }
      //TODO: add reflexive pronoun to another pronoun
      // containsReflexivePronoun.matcher(pronounsStr.toLowerCase()).matches()
    }
    return replacement;
  }

  public static String doAddPronounReflexiveImperative(String firstVerb, String pronounsStr, String verbStr,
                                                 String firstVerbPersonaNumber) {
    String pronounToAdd = "";
    String replacement = "";
    if (pronounsStr.isEmpty()) {
      pronounToAdd = addReflexiveImperative.get(firstVerbPersonaNumber);
      if (pronounToAdd != null) {
        replacement = (verbStr + pronounToAdd).trim();
      }
    }
    return replacement;
  }

  public static String doReplaceEmEn(String firstVerb, String pronounsStr, String verbStr, boolean pronounsAfter) {
    String replacement = "";
    if (pronounsStr.equalsIgnoreCase("em")) {
      replacement = "en"+ " " + verbStr;
    }
    if (pronounsStr.equalsIgnoreCase("m'")) {
      replacement = "n'" + verbStr;
    }
    if (pronounsStr.equalsIgnoreCase("m'hi")) {
      replacement = "n'hi " + verbStr;
    }
    return replacement;
  }

  public static String convertPronounsForIntransitiveVerb(String s) {
    return s.replace("-se'l", "-se-li").replace("se'l ", "se li ")
      .replace("l'", "li ").replace("-lo", "-li")
      .replace("-la", "-li").replace("la ", "li ")
      .replace("el ", "li ").replace("ho", "hi");
  }

  private static Pattern de_wrong_apostrophation = Pattern.compile(".*d'[^aeiouh].*", Pattern.CASE_INSENSITIVE);
  private static Pattern pronoun_wrong_apostrophation = Pattern.compile("([mts])'([^aeiouh].*)",
    Pattern.CASE_INSENSITIVE);
  private static Pattern pronoun_missing_apostrophation = Pattern.compile("(.*)\\be([stm]) (h?[aeiouh].*)",
    Pattern.CASE_INSENSITIVE);
  private static Pattern pronoun_wrong_hypphen = Pattern.compile("(.*)(-[stm])e-(h[oi])",
    Pattern.CASE_INSENSITIVE);

  public static String fixApostrophes(String s) {
    if (de_wrong_apostrophation.matcher(s).matches()) {
      s = s.replace("d'", "de ");
    }
    Matcher matcher = pronoun_missing_apostrophation.matcher(s);
    if (matcher.matches()) {
      s = matcher.group(1) + matcher.group(2) + "'" + matcher.group(3);
    }
    matcher = pronoun_wrong_apostrophation.matcher(s);
    if (matcher.matches()) {
      s = "e" + matcher.group(1) + " " + matcher.group(2);
    }
    matcher = pronoun_wrong_hypphen.matcher(s);
    if (matcher.matches()) {
      s = matcher.group(1) + matcher.group(2) + "'" + matcher.group(3);
    }
    return s;
  }

}
