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
package org.languagetool.rules.de;

import java.util.ResourceBundle;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.LinguServices;
import org.languagetool.UserConfig;
import org.languagetool.rules.AbstractStyleTooOftenUsedWordRule;

/**
 * A rule that gives Hints about too often used Nouns
 * @author Fred Kruse
 * @since 6.2
 */
public class StyleTooOftenUsedNounRule extends AbstractStyleTooOftenUsedWordRule {
  
  private static final int DEFAULT_MIN_PERCENT = 5;

  public StyleTooOftenUsedNounRule(ResourceBundle messages, Language lang, UserConfig userConfig) {
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
    return "Das Substantiv wird häufiger verwendet als " + limit + "% aller Substantive. " + 
           "Möglicherweise ist es besser es durch ein Synonym zu ersetzen.";
  }

  @Override
  public String getId() {
    return "TOO_OFTEN_USED_NOUN_DE";
  }

  @Override
  public String getDescription() {
    return "Statistische Stilanalyse: Zu häufig genutztes Substantiv";
  }

  @Override
  public String getConfigureText() {
    return "Anzeigen wenn ein Substantiv häufiger verwendet wird als ...% aller Substantive:";
  }

  @Override
  protected boolean isToCountedWord(AnalyzedTokenReadings token) {
    return token.hasPosTagStartingWith("SUB:");
  }

  @Override
  protected boolean isException(AnalyzedTokenReadings token) {
    return token.hasPosTagStartingWith("PRO:") 
        || token.getToken().equals("Ich")
        || token.getToken().equals("Aber")
        || token.getToken().equals("Ja");
  }
  
  @Override
  protected String toAddedLemma(AnalyzedTokenReadings token) {
    return getLemmaForPosTagStartsWith("SUB:", token);
  }

}
