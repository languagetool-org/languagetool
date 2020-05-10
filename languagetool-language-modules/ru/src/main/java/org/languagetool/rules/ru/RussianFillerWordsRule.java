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

import java.util.Arrays;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JOptionPane;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.AbstractFillerWordsRule;

/**
 * A rule that gives Hints about the use of Russian filler words.
 * The Hints are only given when the percentage of filler words per paragraph exceeds the given limit.
 * A limit of 0 shows all used filler words. Direct speech or citation is excluded otherwise. 
 * This rule detects no grammar error but gives stylistic hints (default off).
 * @author Yakov Reztsov, based on German rule from Fred Kruse
 * @since 5.0
 */
public class RussianFillerWordsRule extends AbstractFillerWordsRule {

  private static final Set<String> fillerWords = new HashSet<>(Arrays.asList( "ах","эх",
      "бу","ох","эээ","э","ух-ты","ух"
  ));
  
  public RussianFillerWordsRule(ResourceBundle messages, Language lang, UserConfig userConfig) {
    super(messages, lang, userConfig);
  }

  @Override
  public String getId() {
    return RULE_ID + "_RU";
  }

  @Override
  protected boolean isFillerWord(String token) {
    return fillerWords.contains(token);
  }

  @Override
  public boolean isException(AnalyzedTokenReadings[] tokens, int num) {
//    if ("aber".equals(tokens[num].getToken()) && num >= 1 && ",".equals(tokens[num - 1].getToken())) {
//      return true;
//    }
    return false;
  }
  
}
