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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PronomsFeblesHelper {

  enum PronounPosition {
    DAVANT, DAVANT_APOS, DARRERE, DARRERE_APOS, DARRERE_NOGUIONET_NOAPOS, DARRE_APOS_NOGUIONET_NOAPOS, NORMALIZED
  }

  final static private String[] pronomsFebles = {
    "el", "l'", "-lo", "'l", "lo", "l", "el",
    "els el", "els l'", "-los-el", "'ls-el", "losel", "lsel", "els el",
    "els els", "els els", "-los-els", "'ls-els", "losels", "lsels", "els els",
    "els en", "els n'", "-los-en", "'ls-en", "losen", "lsen", "els en",
    "els hi", "els hi", "-los-hi", "'ls-hi", "loshi", "lshi", "els hi",
    "els ho", "els ho", "-los-ho", "'ls-ho", "losho", "lsho", "els ho",
    "els la", "els l'", "-los-la", "'ls-la", "losla", "lsla", "els la",
    "els les", "els les", "-los-les", "'ls-les", "losles", "lsles", "els les",
    "els", "els", "-los", "'ls", "los", "ls", "els",
    "em", "m'", "-me", "'m", "me", "m", "em",
    "en", "n'", "-ne", "'n", "ne", "n", "en",
    "ens el", "ens l'", "-nos-el", "'ns-el", "nosel", "nsel", "ens el",
    "ens els", "ens els", "-nos-els", "'ns-els", "nosels", "nsels", "ens els",
    "ens en", "ens n'", "-nos-en", "'ns-en", "nosen", "nsen", "ens en",
    "ens hi", "ens hi", "-nos-hi", "'ns-hi", "noshi", "nshi", "ens hi",
    "ens ho", "ens ho", "-nos-ho", "'ns-ho", "nosho", "nsho", "ens ho",
    "ens la", "ens l'", "-nos-la", "'ns-la", "nosla", "nsla", "ens la",
    "ens les", "ens les", "-nos-les", "'ns-les", "nosles", "nsles", "ens les",
    "ens li", "ens li", "-nos-li", "'ns-li", "nosli", "nsli", "ens li",
    "ens", "ens", "-nos", "'ns", "nos", "ns", "ens",
    "es", "s'", "-se", "'s", "se", "s", "es",
    "et", "t'", "-te", "'t", "te", "t", "et",
    "hi", "hi", "-hi", "-hi", "hi", "hi", "hi",
    "ho", "ho", "-ho", "-ho", "ho", "ho", "ho",
    "l'en", "el n'", "-l'en", "-l'en", "len", "len", "el en",
    "l'hi", "l'hi", "-l'hi", "-l'hi", "lhi", "lhi", "el hi",
    "la hi", "la hi", "-la-hi", "-la-hi", "lahi", "lahi", "la hi",
    "la", "l'", "-la", "-la", "la", "la", "la",
    "la'n", "la n'", "-la'n", "-la'n", "lan", "lan", "la en",
    "les en", "les n'", "-les-en", "-les-en", "lesen", "lesen", "les en",
    "les hi", "les hi", "-les-hi", "-les-hi", "leshi", "leshi", "les hi",
    "les", "les", "-les", "-les", "les", "les", "les",
    "li hi", "li hi", "-li-hi", "-li-hi", "lihi", "lihi", "li hi",
    "li ho", "li ho", "-li-ho", "-li-ho", "liho", "liho", "li ho",
    "li la", "li l'", "-li-la", "-li-la", "lila", "lila", "li la",
    "li les", "li les", "-li-les", "-li-les", "liles", "liles", "li les",
    "li", "li", "-li", "-li", "li", "li", "li",
    "li'l", "li l'", "-li'l", "-li'l", "lil", "lil", "li el",
    "li'ls", "li'ls", "-li'ls", "-li'ls", "lils", "lils", "li els",
    "li'n", "li n'", "-li'n", "-li'n", "lin", "lin", "li en",
    "m'hi", "m'hi", "-m'hi", "-m'hi", "mhi", "mhi", "em hi",
    "m'ho", "m'ho", "-m'ho", "-m'ho", "mho", "mho", "em ho",
    "me la", "me l'", "-me-la", "-me-la", "mela", "mela", "em la",
    "me les", "me les", "-me-les", "-me-les", "meles", "meles", "em les",
    "me li", "me li", "-me-li", "-me-li", "meli", "meli", "em li",
    "me'l", "me l'", "-me'l", "-me'l", "mel", "mel", "me'l",
    "me'ls", "me'ls", "-me'ls", "-me'ls", "mels", "mels", "em els",
    "me'n", "me n'", "-me'n", "-me'n", "men", "men", "em en",
    "n'hi", "n'hi", "-n'hi", "-n'hi", "nhi", "nhi", "en hi",
    "s'hi", "s'hi", "-s'hi", "-s'hi", "shi", "shi", "es hi",
    "s'ho", "s'ho", "-s'ho", "-s'ho", "sho", "sho", "es ho",
    "se la", "se l'", "-se-la", "-se-la", "sela", "sela", "es la",
    "se les", "se les", "-se-les", "-se-les", "seles", "seles", "es les",
    "se li", "se li", "-se-li", "-se-li", "seli", "seli", "es li",
    "se us", "se us", "-se-us", "-se-us", "seus", "seus", "es us",
    "se vos", "se vos", "-se-vos", "-se-vos", "sevos", "sevos", "es vos",
    "se'l", "se l'", "-se'l", "-se'l", "sel", "sel", "es el",
    "se'ls", "se'ls", "-se'ls", "-se'ls", "sels", "sels", "es els",
    "se'm", "se m'", "-se'm", "-se'm", "sem", "sem", "es em",
    "se'n", "se n'", "-se'n", "-se'n", "sen", "sen", "es en",
    "se'ns", "se'ns", "-se'ns", "-se'ns", "sens", "sens", "es ens",
    "se't", "se t'", "-se't", "-se't", "set", "set", "es et",
    "t'hi", "t'hi", "-t'hi", "-t'hi", "thi", "thi", "et hi",
    "t'ho", "t'ho", "-t'ho", "-t'ho", "tho", "tho", "et ho",
    "te la", "te l'", "-te-la", "-te-la", "tela", "tela", "et la",
    "te les", "te les", "-te-les", "-te-les", "teles", "teles", "et les",
    "te li", "te li", "-te-li", "-te-li", "teli", "teli", "et li",
    "te'l", "te l'", "-te'l", "-te'l", "tel", "tel", "et el",
    "te'ls", "te'ls", "-te'ls", "-te'ls", "tels", "tels", "et els",
    "te'm", "te m'", "-te'm", "-te'm", "tem", "tem", "et em",
    "te'n", "te n'", "-te'n", "-te'n", "ten", "ten", "et en",
    "te'ns", "te'ns", "-te'ns", "-te'ns", "tens", "tens", "et ens",
    "us el", "us l'", "-vos-el", "-us-el", "vosel", "usel", "us el",
    "us els", "us els", "-vos-els", "-us-els", "vosels", "usels", "us els",
    "us em", "us m'", "-vos-em", "-us-em", "vosem", "usem", "us em",
    "us en", "us n'", "-vos-en", "-us-en", "vosen", "usen", "us en",
    "us ens", "us ens", "-vos-ens", "-us-ens", "vosens", "usens", "us ens",
    "us hi", "us hi", "-vos-hi", "-us-hi", "voshi", "ushi", "us hi",
    "us ho", "us ho", "-vos-ho", "-us-ho", "vosho", "usho", "us ho",
    "us la", "us l'", "-vos-la", "-us-la", "vosla", "usla", "us la",
    "us les", "us les", "-vos-les", "-us-les", "vosles", "usles", "us les",
    "us li", "us li", "-vos-li", "-us-li", "vosli", "usli", "us li",
    "us", "us", "-vos", "-us", "vos", "us", "us",
    // combinacions de tres (incomplet)
    "se me'n", "se me n'", "-se-me'n", "-se-me'n", "semen", "semen", "es em en",
    "se te'n", "se te n'", "-se-te'n", "-se-te'n", "seten", "seten", "es et en",
    "se li'n", "se li n'", "-se-li'n", "-se-li'n", "selin", "selin", "es li en",
    "se'ns en", "se'ns n'", "-se'ns-en", "-se'ns-en", "sensen", "sensen", "es ens en",
    "se us en", "se us n'", "-se-us-en", "-se-us-en", "seusen", "seusen", "es us en",
    "se vos en", "se vos n'", "-se-vos-en", "-se-vos-en", "sevosen", "sevosen", "es vos en",
    "se'ls en", "se'ls n'", "-se'ls-en", "-se'ls-en", "selsen", "selsen", "es els en",
    // pronoms erronis?
  };

  final static Pattern pApostropheNeeded = Pattern.compile("h?[aeiouàèéíòóú].*", Pattern.CASE_INSENSITIVE);
  final static Pattern pApostropheNeededEnd = Pattern.compile(".*[aei]", Pattern.CASE_INSENSITIVE);
  public final static Pattern pPronomFeble = Pattern.compile("P0.{6}|PP3CN000|PP3NN000|PP3..A00|PP[123]CP000|PP3CSD00");

  final private static Map<String, String> reflexivePronoun = new HashMap<>();

  static {
    reflexivePronoun.put("1S", "em");
    reflexivePronoun.put("2S", "et");
    reflexivePronoun.put("3S", "es");
    reflexivePronoun.put("1P", "ens");
    reflexivePronoun.put("2P", "us");
    reflexivePronoun.put("3P", "es");
  }

  public static String getReflexivePronoun(String key) {
    return reflexivePronoun.getOrDefault(key, "");
  }

  final private static Map<String, String> dativePronoun = new HashMap<>();

  static {
    dativePronoun.put("1S", "em");
    dativePronoun.put("2S", "et");
    dativePronoun.put("3S", "li");
    dativePronoun.put("3C", "li"); // also "els"
    dativePronoun.put("1P", "ens");
    dativePronoun.put("2P", "us");
    dativePronoun.put("3P", "els");
  }

  public static String getDativePronoun(String key) {
    return dativePronoun.getOrDefault(key, "");
  }

  public static String transform(String inputPronom, PronounPosition pronounPos) {
    int i = 0;
    inputPronom = inputPronom.trim();
    while (i < pronomsFebles.length && !inputPronom.equalsIgnoreCase(pronomsFebles[i])) {
      i++;
    }
    int pfPos = PronounPosition.values().length * (i / PronounPosition.values().length) + pronounPos.ordinal();
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

  public static String doAddPronounEn(String pronounsStr, String firstVerb) {
    String pronounNormalized = transform(pronounsStr, PronounPosition.NORMALIZED);
    if (pronounNormalized.endsWith("hi")) {
      pronounNormalized = pronounNormalized.replace("hi", "en hi");
    } else {
      pronounNormalized += " en";
    }
    return transformDavant(pronounNormalized, firstVerb);
  }

  public static String doRemovePronounReflexive(String pronounsStr, String verbStr, boolean pronounsAfter) {
    String replacement;
    String pronounsReplacement = transform(pronounsStr.toLowerCase(), PronounPosition.NORMALIZED)
      .replaceFirst("(?i)(em|et|es|ens|us|vos)", "").trim();
    if (pronounsAfter) {
      replacement = verbStr;
      pronounsReplacement = transformDarrere(pronounsReplacement, verbStr);
      if (!pronounsReplacement.isEmpty()) {
        replacement = verbStr + pronounsReplacement;
      }
      return replacement;
    }
    replacement = verbStr;
    pronounsReplacement = transformDavant(pronounsReplacement, verbStr);
    if (!pronounsReplacement.isEmpty()) {
      replacement = pronounsReplacement + verbStr;
    }
    return replacement;
  }

  private final static Pattern pContainsReflexivePronoun = Pattern.compile(".*([mts][e']|[e'][mts]|vos|us|ens|-nos|-vos).*");
  public final static List<String> lReflexivePronouns = Arrays.asList("em", "et", "es", "ens", "us", "vos");

  public static String doAddPronounReflexive(String pronounsStr, String verbStr, String firstVerbPersonaNumber,
                                             boolean pronounsAfter) {
    String replacement = "";
    if (pronounsAfter) {
      if (pContainsReflexivePronoun.matcher(pronounsStr.toLowerCase()).matches()) {
        return verbStr + pronounsStr;
      }
      if (verbStr.endsWith("r") || verbStr.endsWith("re")) {
        return verbStr + transformDarrere("-se", verbStr);
      } else {
        return verbStr;
      }
    }
    String pronounToAdd;
    if (pronounsStr.isEmpty()) {
      pronounToAdd = getReflexivePronoun(firstVerbPersonaNumber);
      if (pronounToAdd != null) {
        replacement = transformDavant(pronounToAdd, verbStr) + verbStr;
      }
    } else {
      //TODO: add reflexive pronoun to another pronoun
      // containsReflexivePronoun.matcher(pronounsStr.toLowerCase()).matches()
      replacement = (pronounsStr + " " + verbStr).trim().replace("' ", "'");
    }
    return replacement;
  }

  public static String doAddPronounReflexiveEn(String pronounsStr, String verbStr, String firstVerbPersonaNumber,
                                               boolean pronounsAfter) {
    String replacement = "";
    if (pronounsAfter) {
      if (pContainsReflexivePronoun.matcher(pronounsStr.toLowerCase()).matches()) {
        return verbStr + transformDarrere(pronounsStr + "'n", verbStr); // no sempre correcte
      }
      return verbStr + transformDarrere("-se'n", verbStr);
    }
    String pronounToAdd;
    if (pronounsStr.isEmpty()) {
      pronounToAdd = transformDavant(getReflexivePronoun(firstVerbPersonaNumber) + " en", verbStr);
      if (pronounToAdd != null) {
        replacement = pronounToAdd + verbStr;
      }
    } else {
      pronounToAdd = transformDavant("es " + transform(pronounsStr, PronounPosition.NORMALIZED) + " en", verbStr);
      if (pronounToAdd != null) {
        replacement = pronounToAdd + verbStr;
      } else {
        replacement = transformDavant(pronounsStr, verbStr) + verbStr;
      }
      //TODO: add reflexive pronoun to another pronoun
      // containsReflexivePronoun.matcher(pronounsStr.toLowerCase()).matches()
    }
    return replacement;
  }

  public static String doAddPronounReflexiveImperative(String pronounsStr, String verbStr, String firstVerbPersonaNumber) {
    String pronounToAdd;
    String replacement = "";
    if (pronounsStr.isEmpty()) {
      pronounToAdd = transformDarrere(getReflexivePronoun(firstVerbPersonaNumber), verbStr);
      if (pronounToAdd != null) {
        replacement = verbStr + pronounToAdd;
      }
    }
    return replacement;
  }

  public static String doReplaceEmEn(String pronounsStr, String verbStr, boolean pronounsAfter) {
    String replacement = "";
    if (pronounsStr.equalsIgnoreCase("em")) {
      replacement = "en" + " " + verbStr;
    }
    if (pronounsStr.equalsIgnoreCase("m'")) {
      replacement = "n'" + verbStr;
    }
    if (pronounsStr.equalsIgnoreCase("m'hi")) {
      replacement = "n'hi " + verbStr;
    }
    return replacement;
  }

  public static String doReplaceHiEn(String pronounsStr, String verbStr, boolean pronounsAfter) {
    String replacement = "";
    if (pronounsStr.equalsIgnoreCase("hi")) {
      replacement = "en" + " " + verbStr;
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

  private final static Pattern de_wrong_apostrophation = Pattern.compile(".*d'[^aeiouh].*", Pattern.CASE_INSENSITIVE);
  private final static Pattern pronoun_wrong_apostrophation = Pattern.compile("([mts])'([^aeiouh].*)",
    Pattern.CASE_INSENSITIVE);
  private final static Pattern pronoun_missing_apostrophation = Pattern.compile("(.*)\\be([stm]) (h?[aeiouh].*)",
    Pattern.CASE_INSENSITIVE);
  private final static Pattern pronoun_wrong_hypphen = Pattern.compile("(.*)(-[stm])e-(h[oi])",
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
