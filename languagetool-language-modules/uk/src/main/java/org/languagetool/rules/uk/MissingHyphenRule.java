/* LanguageTool, a natural language style checker 
 * Copyright (C) 2018 Daniel Naber (http://www.danielnaber.de)
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
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tagging.uk.PosTagHelper;
import org.languagetool.tools.StringTools;

/**
 *
 * @author Andriy Rysin
 */
public class MissingHyphenRule extends Rule {

  private static final String UA_1992_TAG_PART = ":ua_1992";
  private static final Map<String, String> dashPrefixes = ExtraDictionaryLoader.loadMap("/uk/dash_prefixes.txt");
  private static final Pattern ALL_LOWER = Pattern.compile("[а-яіїєґ'-]+");
  private WordTagger wordTagger;

  static {
    // these two generate too many false positives
    dashPrefixes.remove("блок");
    dashPrefixes.remove("рейтинг");
    dashPrefixes.entrySet().removeIf(entry -> !ALL_LOWER.matcher(entry.getKey()).matches() || entry.getValue().contains(":bad") );
  }

  public MissingHyphenRule(ResourceBundle messages, WordTagger wordTagger) throws IOException {
    super(messages);
    setLocQualityIssueType(ITSIssueType.Misspelling);
    this.wordTagger = wordTagger;
  }

  @Override
  public final String getId() {
    return "UK_MISSING_HYPHEN";
  }

  @Override
  public String getDescription() {
    return "Пропущений дефіс";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

    for (int i = 1; i < tokens.length-1; i++) {
      AnalyzedTokenReadings tokenReadings = tokens[i];
      AnalyzedTokenReadings nextTokenReadings = tokens[i + 1];
      
//      boolean isCapitalized = Character.isUpperCase(tokenReadings.getToken().charAt(0));
      boolean isCapitalized = StringTools.isCapitalizedWord(tokenReadings.getToken());

      if( PosTagHelper.hasPosTagStart(nextTokenReadings, "noun")
          && ! PosTagHelper.hasPosTagPart(nextTokenReadings, "&pron")
          //    && ! PosTagHelper.hasPosTag(nextTokenReadings, Pattern.compile("^(?!noun).*"))
          && ALL_LOWER.matcher(nextTokenReadings.getToken()).matches() ) {

        String extraTag = getPrefixExtraTag(tokenReadings, isCapitalized);
        if ( extraTag != null
            || (tokenReadings.getToken().toLowerCase().equals("тайм")
                && LemmaHelper.hasLemma(nextTokenReadings, "аут")) ) {

          // всі медіа країни
          if( "медіа".equalsIgnoreCase(tokenReadings.getToken()) 
              && nextTokenReadings.getToken().matches("країни|півострова"))
            continue;
          
          String suggested;
          String message;
          
          if( UA_1992_TAG_PART.equals(extraTag) ) {
            suggested = String.format("%s%s", tokenReadings.getToken(), nextTokenReadings.getToken());
            message = "Можливо, зайвий пробіл?";
          }
          else {
            suggested = String.format("%s-%s", tokenReadings.getToken(), nextTokenReadings.getToken());
            message = "Можливо, пропущено дефіс?";
          }
          
          String tokenToCheck = isCapitalized ? StringUtils.uncapitalize(suggested) : suggested;

          if (wordTagger.tag(tokenToCheck).size() > 0
              || (UA_1992_TAG_PART.equals(extraTag) && PosTagHelper.hasPosTagPart(nextTokenReadings, UA_1992_TAG_PART)) ) {
            RuleMatch potentialRuleMatch = new RuleMatch(this, sentence, tokenReadings.getStartPos(),
                nextTokenReadings.getEndPos(), message, getDescription());
            potentialRuleMatch.setSuggestedReplacement(suggested);

            ruleMatches.add(potentialRuleMatch);
          }

        }
      }      
    }
    
    return ruleMatches.toArray(new RuleMatch[0]);
  }

  private String getPrefixExtraTag(AnalyzedTokenReadings tokenReadings, boolean isCapitalized) {
    String token = tokenReadings.getToken();
    if( isCapitalized ) {
      token = StringUtils.uncapitalize(token);
    }
    return dashPrefixes.get(token);
  }

  
}
