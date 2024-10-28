/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Languages;
import org.languagetool.Language;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.tagging.Tagger;

import java.io.IOException;
import java.util.*;

/*
 *  Check that the words are English.
 *  Alternatively, verify that they have the desired postag
 */
public class IsEnglishWordFilter extends RuleFilter {

  private Language english = null;
  //private SpellingCheckRule spellingCheckRule = null;
  private Tagger tagger = null;

  public IsEnglishWordFilter() {
    try {
      english = Languages.getLanguageForShortCode("en-US");
    } catch (Exception e) {
    }
    if (english != null) {
      //spellingCheckRule = english.getDefaultSpellingRule();
      tagger = english.createDefaultTagger();
    }
  }

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> args, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens, List<Integer> tokenPositions) throws IOException {

    if (tagger == null) {
      return null;
    }
    String[] formPositions = getRequired("formPositions", args).split(",");
    List<String> forms = new ArrayList<>();
    for (String formPosition : formPositions) {
      forms.add(patternTokens[getSkipCorrectedReference(tokenPositions, Integer.parseInt(formPosition))].getToken());
    }
    boolean isEnglish = true;
    String postagsStr = getOptional("postags", args);
    if (postagsStr != null) {
      String [] postags = postagsStr.split(",");
      if (postags.length != forms.size()) {
        throw new RuntimeException("The number of forms and postags has to be the same in disambiguation rule with " +
            "filter IsEnglishWordFilter.");
      }
      for (int i = 0; i < postags.length; i++) {
        isEnglish = isEnglish && wordIsTaggedWith(forms.get(i), postags[i]);
      }
    } else {
      for (int i = 0; i < forms.size(); i++) {
        isEnglish = isEnglish && wordIsTagged(forms.get(i));
      }
    }
    return (isEnglish ? match : null);
  }

  private boolean wordIsTaggedWith(String word, String postag) throws IOException {
    return tagger.tag(Collections.singletonList(word)).get(0).matchesPosTagRegex(postag);
  }

  private boolean wordIsTagged(String word) throws IOException {
    return tagger.tag(Collections.singletonList(word)).get(0).isTagged();
  }

}
