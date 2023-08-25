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
package org.languagetool.rules.uk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.AbstractSimpleReplaceRule;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tagging.uk.PosTagHelper;

/**
 * A rule that matches words that are written by 1992 spelling rules and suggests 2019 spelling instead.
 * Loads the relevant words from <code>rules/uk/replace_spelling_2019.txt</code>.
 * 
 * @author Andriy Rysin
 */
public class SimpleReplaceSpelling1992Rule extends AbstractSimpleReplaceRule {

  private static final Map<String, List<String>> WRONG_WORDS = loadFromPath("/uk/replace_spelling_2019.txt");
  private static final Map<String, String> dashPrefixes1992;

  static {
    dashPrefixes1992 = ExtraDictionaryLoader.loadMap("/uk/dash_prefixes.txt");
    dashPrefixes1992.entrySet().removeIf(entry -> ! entry.getValue().equals(":ua_1992") );
  }
  
  @Override
  public Map<String, List<String>> getWrongWords() {
    return WRONG_WORDS;
  }

  public SimpleReplaceSpelling1992Rule(ResourceBundle messages) throws IOException {
    super(messages);
    setLocQualityIssueType(ITSIssueType.Misspelling);
  }

  @Override
  public final String getId() {
    return "UK_SIMPLE_REPLACE_SPELLING_1992";
  }

  @Override
  public String getDescription() {
    return "Пошук слів, написаних за правописом 1992";
  }

  @Override
  public String getShort() {
    return "Слово, написане за правописом 1992";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    RuleMatch[] match = super.match(sentence);
    if( match.length == 0 ) {
      match = findTagged1922(sentence);
    }
    return match;
  }
  
  private RuleMatch[] findTagged1922(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

    for (int i = 1; i < tokens.length; i++) {
      AnalyzedTokenReadings tokenReadings = tokens[i];

      if( PosTagHelper.hasPosTagPartAll(tokenReadings, "ua_1992") ) {
        RuleMatch potentialRuleMatch = new RuleMatch(this, sentence, tokenReadings.getStartPos(), tokenReadings.getEndPos(), 
            getShort(), getShort());
        
        String token = tokenReadings.getToken();
        if( token.contains("-") ) {
          String[] parts = token.split("-", 2);
          if( parts.length > 1 && dashPrefixes1992.containsKey(parts[0]) ) {
            potentialRuleMatch.addSuggestedReplacement(token.substring(0, parts[0].length()) + token.substring(parts[0].length()+1));
          }
        }
        ruleMatches.add(potentialRuleMatch);
      }
    }
    
    return toRuleMatchArray(ruleMatches);
  }

  @Override
  public String getMessage(String tokenStr, List<String> replacements) {
    return "«" + tokenStr + "» — написання не відповідає чинній версії правопису, виправлення: "
        + StringUtils.join(replacements, ",") + ".";
  }

  @Override
  public boolean isCaseSensitive() {
    return false;
  }


}
