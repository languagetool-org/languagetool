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
      AnalyzedToken pronom = tokens[from].readingWithTagRegex("P[P0].*");
      if (pronom != null) {
        pronoms = pronom.getToken();
        numPronouns++;
      }
      if (from + 1 < tokens.length && !tokens[from + 1].isWhitespaceBefore()) {
        AnalyzedToken pronom2 = tokens[from + 1].readingWithTagRegex("P[P0].*");
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

}
