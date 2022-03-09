/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (www.danielnaber.de)
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

import org.languagetool.*;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.ConfusionString;
import org.languagetool.rules.ngrams.ConfusionProbabilityRule;
import org.languagetool.rules.patterns.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * False friends for non-native speakers who write English text, based on ngrams.
 * @since 4.9
 */
public abstract class EnglishForL2SpeakersFalseFriendRule extends ConfusionProbabilityRule {

  private static final Map<Language, List<AbstractPatternRule>> motherTongue2rules = new HashMap<>();

  private final Language lang;
  private final Language motherTongue;

  public EnglishForL2SpeakersFalseFriendRule(ResourceBundle messages, LanguageModel languageModel, Language motherTongue, Language lang)  {
    super(messages, languageModel, lang, 3);
    this.lang = Objects.requireNonNull(lang);
    this.motherTongue = Objects.requireNonNull(motherTongue);
  }

  private List<AbstractPatternRule> getRules() {
    synchronized (this) {
      if (!motherTongue2rules.containsKey(motherTongue)) {
        FalseFriendRuleLoader loader = new FalseFriendRuleLoader("\"{0}\" ({1}) means {2} ({3}).", "Did you maybe mean {0}?");
        try (InputStream is = JLanguageTool.getDataBroker().getFromRulesDirAsStream(JLanguageTool.FALSE_FRIEND_FILE)) {
          motherTongue2rules.put(motherTongue, loader.getRules(is, lang, motherTongue));
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
    return motherTongue2rules.get(motherTongue);
  }

  @Override
  protected String getMessage(ConfusionString textString, ConfusionString suggestion) {
    for (AbstractPatternRule rule : getRules()) {
      List<PatternToken> patternTokens = rule.getPatternTokens();
      for (PatternToken patternToken : patternTokens) {
        if (textString.getString().equals(patternToken.getString()) || isBaseformMatch(textString, patternToken)) {
          return rule.getMessage();
        }
      }
    }
    return super.getMessage(textString, suggestion);
  }

  private boolean isBaseformMatch(ConfusionString textString, PatternToken patternToken) {
    if (patternToken.isInflected()) {
      try {
        List<AnalyzedTokenReadings> readings = lang.getTagger().tag(Collections.singletonList(textString.getString()));
        for (AnalyzedTokenReadings reading : readings) {
          if (reading.getReadings().stream().anyMatch(k -> k.getLemma() != null && k.getLemma().equals(patternToken.getString()))) {
            return true;
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return false;
  }
}
