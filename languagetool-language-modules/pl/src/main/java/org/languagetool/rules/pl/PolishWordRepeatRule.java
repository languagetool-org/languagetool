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
package org.languagetool.rules.pl;

import java.util.Collections;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;

import org.languagetool.rules.AdvancedWordRepeatRule;
import org.languagetool.rules.Example;

/**
 * @author Marcin Miłkowski
 */
public class PolishWordRepeatRule extends AdvancedWordRepeatRule {

  /**
   * Excluded dictionary words.
   */
  private static final Set<String> EXC_WORDS;
  static {
    final Set<String> tempSet = new HashSet<>();
    tempSet.add("nie");
    tempSet.add("tuż");
    tempSet.add("aż");
    tempSet.add("to");
    tempSet.add("siebie");
    tempSet.add("być");
    tempSet.add("ani");
    tempSet.add("ni");
    tempSet.add("albo");
    tempSet.add("lub");
    tempSet.add("czy");
    tempSet.add("bądź");
    tempSet.add("jako");
    tempSet.add("zł");
    tempSet.add("np");
    tempSet.add("coraz");
    tempSet.add("bardzo");
    tempSet.add("bardziej");
    tempSet.add("proc");
    tempSet.add("ten");
    tempSet.add("jak");
    tempSet.add("mln");
    tempSet.add("tys");
    tempSet.add("swój");
    tempSet.add("mój");
    tempSet.add("twój");
    tempSet.add("nasz");
    tempSet.add("wasz");
    tempSet.add("i");
    tempSet.add("zbyt");
    tempSet.add("się");
    EXC_WORDS = Collections.unmodifiableSet(tempSet);
  }

  /**
   * Excluded part of speech classes.
   */
  private static final Pattern EXC_POS = Pattern.compile("prep:.*|ppron.*");

  /**
   * Excluded non-words (special symbols, Roman numerals etc.)
   */
  private static final Pattern EXC_NONWORDS = Pattern
      .compile("&quot|&gt|&lt|&amp|[0-9].*|"
          + "M*(D?C{0,3}|C[DM])(L?X{0,3}|X[LC])(V?I{0,3}|I[VX])$");

  public PolishWordRepeatRule(ResourceBundle messages) {
    super(messages);
    addExamplePair(Example.wrong("Mówiła długo, bo lubiła robić wszystko <marker>długo</marker>."),
                   Example.fixed("Mówiła długo, bo lubiła robić wszystko <marker>powoli</marker>."));
  }

  @Override
  public final String getDescription() {
    return "Powtórzenia wyrazów w zdaniu (monotonia stylistyczna)";
  }

  @Override
  protected Pattern getExcludedNonWordsPattern() {
    return EXC_NONWORDS;
  }

  @Override
  protected Pattern getExcludedPos() {
    return EXC_POS;
  }

  @Override
  protected Set<String> getExcludedWordsPattern() {
    return EXC_WORDS;
  }

  @Override
  public final String getId() {
    return "PL_WORD_REPEAT";
  }

  @Override
  public final String getMessage() {
    return "Powtórzony wyraz w zdaniu";
  }

  @Override
  public final String getShortMessage() {
    return "Powtórzenie wyrazu";
  }

}
