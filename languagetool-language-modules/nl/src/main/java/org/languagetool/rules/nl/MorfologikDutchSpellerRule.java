/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
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

package org.languagetool.rules.nl;

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.language.Dutch;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import static org.languagetool.JLanguageTool.getDataBroker;

public final class MorfologikDutchSpellerRule extends MorfologikSpellerRule {

  public MorfologikDutchSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig) throws IOException {
    this(messages, language, userConfig, Collections.emptyList());
  }
  
  public MorfologikDutchSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig, List<Language> altLanguages) throws IOException {
    super(messages, language, userConfig, altLanguages);
  }

  @Override
  protected boolean ignorePotentiallyMisspelledWord(String word) throws IOException {
    return Dutch.getCompoundAcceptor().acceptCompound(word);
  }

  @Override
  public String getFileName() {
    return "/nl/spelling/nl_NL.dict";
  }

  @Override
  public String getId() {
    return "MORFOLOGIK_RULE_NL_NL";
  }

  @Override
  protected String getIgnoreFileName() {
    return "/nl/spelling/ignore.txt";
  }

  @Override
  public String getSpellingFileName() {
    return "/nl/spelling/spelling.txt";
  }

  @Override
  protected String getProhibitFileName() {
    return "/nl/spelling/prohibit.txt";
  }

  @Override
  public List<RuleMatch> getRuleMatches(String word, int startPos, AnalyzedSentence sentence,
    List<RuleMatch> ruleMatchesSoFar, int idx,
    AnalyzedTokenReadings[] tokens) throws IOException {
      if (tokens[idx].hasPosTag("_english_ignore_")) {
        return Collections.emptyList();
      }
      return super.getRuleMatches(word, startPos, sentence, ruleMatchesSoFar, idx, tokens);
    }

}







