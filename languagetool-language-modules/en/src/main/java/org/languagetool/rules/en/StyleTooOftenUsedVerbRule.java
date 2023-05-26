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
package org.languagetool.rules.en;

import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.LinguServices;
import org.languagetool.UserConfig;
import org.languagetool.rules.AbstractStyleTooOftenUsedWordRule;

/**
 * A rule that gives Hints about too often used Verbs
 * @author Fred Kruse
 * @since 6.2
 */
public class StyleTooOftenUsedVerbRule extends AbstractStyleTooOftenUsedWordRule {
  
  private static final int DEFAULT_MIN_PERCENT = 5;

  String sentenceMessage = null;
  
  public StyleTooOftenUsedVerbRule(ResourceBundle messages, Language lang, UserConfig userConfig) {
    super(messages, lang, userConfig, DEFAULT_MIN_PERCENT);
    if (userConfig != null) {
      LinguServices linguServices = userConfig.getLinguServices();
      if (linguServices != null) {
        linguServices.setThesaurusRelevantRule(this);
      }
    }
  }
 
  @Override
  protected String getLimitMessage(int limit) {
    return "The verb is used more than " + limit + "% times of all verbs. " + 
        "It may be better to replace it with a synonym.";
  }

  @Override
  public String getId() {
    return "TOO_OFTEN_USED_VERB_EN";
  }

  @Override
  public String getDescription() {
    return "Statistical Style Analysis: Overused Verb";
  }

  @Override
  public String getConfigureText() {
    return "Show when a verb is used more often than ...% of all verbs:";
  }

  @Override
  protected boolean isToCountedWord(AnalyzedTokenReadings token) {
    return token.hasPosTagStartingWith("VB");
  }

  @Override
  protected boolean isException(AnalyzedTokenReadings token) {
    return token.hasAnyLemma("be", "have", "do") || token.hasPosTagStartingWith("IN") ||
        token.hasPosTagStartingWith("NN");
  }
  
  private String getLemmaForPosTagStartsWith(String startPos, AnalyzedTokenReadings token) {
    List<AnalyzedToken> readings = token.getReadings();
    for (AnalyzedToken reading : readings) {
      String posTag = reading.getPOSTag();
      if (posTag != null && posTag.startsWith(startPos)) {
        return reading.getLemma();
      }
    }
    return null;
  }

  @Override
  protected String toAddedLemma(AnalyzedTokenReadings token) {
    return getLemmaForPosTagStartsWith("VB", token);
  }

}
