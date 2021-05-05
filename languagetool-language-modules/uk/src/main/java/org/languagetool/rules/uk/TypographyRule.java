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
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tagging.uk.PosTagHelper;

/**
 * A rule that matches short dash inside words
 */
public class TypographyRule extends Rule {

  public TypographyRule(ResourceBundle messages) throws IOException {
    super.setCategory(Categories.TYPOGRAPHY.getCategory(messages));
  }

  @Override
  public final String getId() {
    return "DASH";
  }

  @Override
  public String getDescription() {
    return "Коротка риска замість дефісу";
  }

  public String getShort() {
    return "Коротка риска";
  }

  @Override
  public final RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

    for (int i = 1; i < tokens.length; i++) {
//      if( i == 1 && "\u2013".equals(tokens[i].getToken()) )
//        continue;

      String shortDashToken = shortDashToken(tokens[i]);
      if( shortDashToken != null ) {
        List<String> replacements = new ArrayList<>();
        replacements.add( shortDashToken.replaceAll("[\u2013\u2014]", "-") );
        replacements.add( shortDashToken.replaceAll("[\u2013\u2014]", " \u2014 ") );

        // Правопис 2019 § 161. ТИРЕ (—), пп. 12-15
        String msg = "Риска всередині слова. Всередині слова вживайте дефіс, між словами виокремлюйте риску пробілами.";
        RuleMatch potentialRuleMatch = createRuleMatch(tokens[i], replacements, msg, sentence);
        ruleMatches.add(potentialRuleMatch);
      }
      else if( "\u2014".equals(tokens[i].getToken()) || "\u2013".equals(tokens[i].getToken()) ) {
        boolean noSpaceLeft = i > 1 && ! tokens[i].isWhitespaceBefore() 
            && ! ",".equals(tokens[i-1].getToken())
            && ! "«".equals(tokens[i-1].getToken());
        boolean noSpaceRight = i < tokens.length - 1 && ! tokens[i+1].isWhitespaceBefore()
            && ! ">".equals(tokens[i+1].getToken());

        if( noSpaceLeft || noSpaceRight ) {

          if( i > 1 && isNumber(tokens[i-1]) && i < tokens.length -1 && isNumber(tokens[i+1]) )
            continue;

          List<String> replacements = new ArrayList<>();
          
          if( i > 1 && i < tokens.length - 1
              && tokens[i-1].getToken().matches(".*[а-яїієґА-ЯІЇЄҐ].*")
              && tokens[i+1].getToken().matches(".*[а-яїієґА-ЯІЇЄҐ].*")) {
            replacements.add(tokens[i-1].getToken() + "-" + tokens[i+1].getToken());
          }
          
          int startPos;
          int endPos;
          String repl = "";
          if( i > 1 ) {
            repl = tokens[i-1].getToken() + " ";
            startPos = tokens[i-1].getStartPos();
          }
          else {
            startPos = tokens[i].getStartPos();
          }
          repl += "\u2014";
          if( i < tokens.length - 1 ) {
            repl += " " + tokens[i+1].getToken();
            endPos = tokens[i+1].getStartPos();
          }
          else {
            endPos = tokens[i].getEndPos();
          }

          replacements.add(repl);

          // Правопис 2019 § 161. ТИРЕ (—), пп. 12-15
          String msg = "Риска всередині слова. Всередині слова вживайте дефіс, між словами виокремлюйте риску пробілами.";
//          RuleMatch potentialRuleMatch = createRuleMatch(tokens[i], replacements, msg, sentence);
          RuleMatch potentialRuleMatch = new RuleMatch(this, sentence, startPos, endPos, msg, getShort());
          potentialRuleMatch.setSuggestedReplacements(replacements);
          ruleMatches.add(potentialRuleMatch);
        }
      }
    }
    
    return toRuleMatchArray(ruleMatches);
  }

  private static boolean isNumber(AnalyzedTokenReadings analyzedTokenReadings) {
    return PosTagHelper.hasPosTagStart(analyzedTokenReadings, "number");
  }

  private static final Pattern SHORT_DASH_WORD = Pattern.compile("[а-яіїєґ']{2,}([\u2013\u2014][а-яіїєґ']{2,})+", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern BAD_LATIN = Pattern.compile("[ХІXIV]+[\u2013\u2014][ХІXIV]+");
  private String shortDashToken(AnalyzedTokenReadings tokenReadings) {
    List<AnalyzedToken> readings = tokenReadings.getReadings();
    if( readings.size() == 0 || tokenReadings.getToken() == null )
      return null;

    String lastReadingToken = readings.get(readings.size()-1).getToken();
    return lastReadingToken != null 
        && (lastReadingToken.indexOf('\u2013') > 0 || lastReadingToken.indexOf('\u2014') > 0)
        && SHORT_DASH_WORD.matcher(lastReadingToken).matches()
        && ! BAD_LATIN.matcher(lastReadingToken).matches()
      ? lastReadingToken
      : null;
  }

  private RuleMatch createRuleMatch(AnalyzedTokenReadings readings, List<String> replacements, String msg, AnalyzedSentence sentence) {
    RuleMatch potentialRuleMatch = new RuleMatch(this, sentence, readings.getStartPos(), readings.getEndPos(), msg, getShort());
    potentialRuleMatch.setSuggestedReplacements(replacements);

    return potentialRuleMatch;
  }

}
