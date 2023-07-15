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

import org.languagetool.tools.StringTools;

import java.util.regex.Pattern;

public class PronomsFeblesHelper {

  enum PronounPosition {
    DAVANT, DAVANT_APOS, DARRERE, DARRERE_APOS
  }
  final static private String[] pronomsFebles = {
    "el", "l'", "-lo", "'l",
    "els el", "els l'", "-los-el", "'ls-el",
    "els els", "els els", "-los-els", "'ls-els",
    "els en", "els n'", "-los-en", "'ls-en",
    "els hi", "els hi", "-los-hi", "'ls-hi",
    "els ho", "els ho", "-los-ho", "'ls-ho",
    "els la", "els l'", "-los-la", "'ls-la",
    "els les", "els les", "-los-les", "'ls-les",
    "els", "els", "-los", "'ls",
    "em", "m'", "-me", "'m",
    "en", "n'", "-ne", "'n",
    "ens el", "ens l'", "-nos-el", "'ns-el",
    "ens els", "ens els", "-nos-els", "'ns-els",
    "ens en", "ens n'", "-nos-en", "'ns-en",
    "ens hi", "ens hi", "-nos-hi", "'ns-hi",
    "ens ho", "ens ho", "-nos-ho", "'ns-ho",
    "ens la", "ens l'", "-nos-la", "'ns-la",
    "ens les", "ens les", "-nos-les", "'ns-les",
    "ens li", "ens li", "-nos-li", "'ns-li",
    "ens", "ens", "-nos", "'ns",
    "es", "s'", "-se", "'s",
    "et", "t'", "-te", "'t",
    "hi", "hi", "-hi", "-hi",
    "ho", "ho", "-ho", "-ho",
    "l'en", "el n'", "-l'en", "-l'en",
    "l'hi", "l'hi", "-l'hi", "-l'hi",
    "la hi", "la hi", "-la-hi", "-la-hi",
    "la", "l'", "-la", "-la",
    "la'n", "la n'", "-la'n", "-la'n",
    "les en", "les n'", "-les-en", "-les-en",
    "les hi", "les hi", "-les-hi", "-les-hi",
    "les", "les", "-les", "-les",
    "li hi", "li hi", "-li-hi", "-li-hi",
    "li ho", "li ho", "-li-ho", "-li-ho",
    "li la", "li l'", "-li-la", "-li-la",
    "li les", "li les", "-li-les", "-li-les",
    "li", "li", "-li", "-li",
    "li'l", "li l'", "-li'l", "-li'l",
    "li'ls", "li'ls", "-li'ls", "-li'ls",
    "li'n", "li n'", "-li'n", "-li'n",
    "m'hi", "m'hi", "-m'hi", "-m'hi",
    "m'ho", "m'ho", "-m'ho", "-m'ho",
    "me la", "me l'", "-me-la", "-me-la",
    "me les", "me les", "-me-les", "-me-les",
    "me li", "me li", "-me-li", "-me-li",
    "me'l", "me l'", "-me'l", "-me'l",
    "me'ls", "me'ls", "-me'ls", "-me'ls",
    "me'n", "me n'", "-me'n", "-me'n",
    "n'hi", "n'hi", "-n'hi", "-n'hi",
    "s'hi", "s'hi", "-s'hi", "-s'hi",
    "s'ho", "s'ho", "-s'ho", "-s'ho",
    "se la", "se l'", "-se-la", "-se-la",
    "se les", "se les", "-se-les", "-se-les",
    "se li", "se li", "-se-li", "-se-li",
    "se us", "se us", "-se-us", "-se-us",
    "se vos", "se vos", "-se-vos", "-se-vos",
    "se'l", "se l'", "-se'l", "-se'l",
    "se'ls", "se'ls", "-se'ls", "-se'ls",
    "se'm", "se m'", "-se'm", "-se'm",
    "se'n", "se n'", "-se'n", "-se'n",
    "se'ns", "se'ns", "-se'ns", "-se'ns",
    "se't", "se t'", "-se't", "-se't",
    "t'hi", "t'hi", "-t'hi", "-t'hi",
    "t'ho", "t'ho", "-t'ho", "-t'ho",
    "te la", "te l'", "-te-la", "-te-la",
    "te les", "te les", "-te-les", "-te-les",
    "te li", "te li", "-te-li", "-te-li",
    "te'l", "te l'", "-te'l", "-te'l",
    "te'ls", "te'ls", "-te'ls", "-te'ls",
    "te'm", "te m'", "-te'm", "-te'm",
    "te'n", "te n'", "-te'n", "-te'n",
    "te'ns", "te'ns", "-te'ns", "-te'ns",
    "us el", "us l'", "-vos-el", "-us-el",
    "us els", "us els", "-vos-els", "-us-els",
    "us em", "us m'", "-vos-em", "-us-em",
    "us en", "us n'", "-vos-en", "-us-en",
    "us ens", "us ens", "-vos-ens", "-us-ens",
    "us hi", "us hi", "-vos-hi", "-us-hi",
    "us ho", "us ho", "-vos-ho", "-us-ho",
    "us la", "us l'", "-vos-la", "-us-la",
    "us les", "us les", "-vos-les", "-us-les",
    "us li", "us li", "-vos-li", "-us-li",
    "us", "us", "-vos", "-us"
  };

  static Pattern pApostropheNeeded = Pattern.compile("h?[aeiouàèéíòóú].*", Pattern.CASE_INSENSITIVE);
  static Pattern pApostropheNeededEnd = Pattern.compile(".*[aei]", Pattern.CASE_INSENSITIVE);

  PronomsFeblesHelper() {
  }

  public static String transform (String inputPronom, PronounPosition pronounPos) {
    int i = 0;
    while (i < pronomsFebles.length && !inputPronom.equalsIgnoreCase(pronomsFebles[i])) {
      i++;
    }
    String pronom = pronomsFebles[4*(i / 4) + pronounPos.ordinal()];
    if (pronounPos == PronounPosition.DAVANT || (pronounPos == PronounPosition.DAVANT_APOS)
      && !pronom.endsWith("'")) {
      pronom = pronom + " ";
    }
    return pronom;
  }

  public static String transformDavant (String inputPronom, String nextWord) {
    if (pApostropheNeeded.matcher(nextWord).matches()) {
      return transform(inputPronom, PronounPosition.DAVANT_APOS);
    } else {
      return transform(inputPronom, PronounPosition.DAVANT);
    }
  }

  public static String transformDarrere (String inputPronom, String previousWord) {
    if (pApostropheNeededEnd.matcher(previousWord).matches()) {
      return transform(inputPronom, PronounPosition.DARRERE_APOS);
    } else {
      return transform(inputPronom, PronounPosition.DARRERE);
    }
  }

}
