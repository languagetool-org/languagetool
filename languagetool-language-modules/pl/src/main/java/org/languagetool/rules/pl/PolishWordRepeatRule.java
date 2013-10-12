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

import org.languagetool.rules.AdvancedWordRepeatRule;

import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * @author Marcin Miłkowski
 */
public class PolishWordRepeatRule extends AdvancedWordRepeatRule {

  /**
   * Excluded dictionary words.
   */
  private static final Pattern EXC_WORDS = Pattern
      .compile("nie|tuż|aż|to|siebie|być|ani|ni|albo|"
          + "lub|czy|bądź|jako|zł|np|coraz"
          + "|bardzo|bardziej|proc|ten|jak|mln|tys|swój|mój|"
          + "twój|nasz|wasz|i|zbyt|się");

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

  public PolishWordRepeatRule(final ResourceBundle messages) {
    super(messages);
  }

  @Override
  public final String getId() {
    return "PL_WORD_REPEAT";
  }

  @Override
  public final String getDescription() {
    return "Powtórzenia wyrazów w zdaniu (monotonia stylistyczna)";
  }

  @Override
  protected Pattern getExcludedWordsPattern() {
    return EXC_WORDS;
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
  public final String getMessage() {
    return "Powtórzony wyraz w zdaniu";
  }

  @Override
  public final String getShortMessage() {
    return "Powtórzenie wyrazu";
  }

}
