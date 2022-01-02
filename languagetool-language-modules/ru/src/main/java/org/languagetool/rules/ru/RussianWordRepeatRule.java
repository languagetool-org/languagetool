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
package org.languagetool.rules.ru;

import java.util.Collections;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;
import org.languagetool.rules.Example;

import org.languagetool.rules.AdvancedWordRepeatRule;

/**
 * @author Yakov Reztsov, based on code by Marcin Miłkowski
 */
public class RussianWordRepeatRule extends AdvancedWordRepeatRule {

  /**
   * Excluded dictionary words.
   */
  private static final Set<String> EXC_WORDS;
  static {
    final Set<String> tempSet = new HashSet<>();
    tempSet.add("не");
    tempSet.add("ни");
    tempSet.add("а");
    tempSet.add("их");
    tempSet.add("на");
    tempSet.add("в");
    tempSet.add("минута");
    tempSet.add("друг");
    tempSet.add("час");
    tempSet.add("секунда");
    EXC_WORDS = Collections.unmodifiableSet(tempSet);
  }
  /**
   * Excluded part of speech classes.
   */
  private static final Pattern EXC_POS = Pattern.compile("INTERJECTION|PRDC|PREP|CONJ|PARTICLE|NumC:.*|Num:.*"); 

  /**
   * Excluded non-words (special symbols, Roman numerals etc.) (remove from exclude PNN:.*)
   */
  private static final Pattern EXC_NONWORDS = Pattern
      .compile("&quot|&gt|&lt|&amp|[0-9].*|"
          + "M*(D?C{0,3}|C[DM])(L?X{0,3}|X[LC])(V?I{0,3}|I[VX])$");

  public RussianWordRepeatRule(ResourceBundle messages) {
    super(messages);
    addExamplePair(Example.wrong("Всё смешалось в <marker>доме доме</marker> Облонских."),
                   Example.fixed("Всё смешалось в <marker>доме</marker> Облонских."));
  }

  @Override
  public final String getId() {
    return "RU_WORD_REPEAT";
  }

  @Override
  public final String getDescription() {
    return "Повтор слов в предложении";
  }

  @Override
  protected Set<String> getExcludedWordsPattern() {
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
    return "Повтор слов в предложении";
  }

  @Override
  public final String getShortMessage() {
    return "Повтор слов в предложении";
  }

}
