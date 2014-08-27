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
package org.languagetool.rules.fa;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.Category;
import org.languagetool.rules.RuleMatch;

/**
 * A Persian rule that checks if there is a missing space begore some conjunctions.
 * 
 */
public class PersianSpaceBeforeRule extends PersianRule {

  private static final Pattern CONJUNCTIONS = Pattern.compile("و|به|با|تا|زیرا|چون|بنابراین|چونکه",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
   
  public PersianSpaceBeforeRule(final ResourceBundle messages, final Language language) {
    super.setCategory(new Category(messages.getString("category_misc")));
  }

  @Override
  public String getId() {
    return "Fa_SPACE_BEFORE_CONJUNCTION";
  }

  @Override
  public String getDescription() {
    return "بررسی‌کردن فاصله قبل از حرف ربط";
  }

  @Override
  public final RuleMatch[] match(final AnalyzedSentence sentence) {
    final List<RuleMatch> ruleMatches = new ArrayList<>();
    final AnalyzedTokenReadings[] tokens = sentence.getTokens();
    
    for (int i = 1; i < tokens.length; i++) {
      final String token = tokens[i].getToken();
      Matcher matcher = CONJUNCTIONS.matcher(token);
      if (matcher.matches()) {
        if (!tokens[i - 1].getToken().equals(" ")) {
          final String replacement = " " + token;
          final String msg = getSuggestion();
          final int pos = tokens[i].getStartPos();
          final RuleMatch potentialRuleMatch = new RuleMatch(this, pos, pos
              + token.length(), msg, getShort());
          potentialRuleMatch.setSuggestedReplacement(replacement);
          ruleMatches.add(potentialRuleMatch);
        }
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  private String getShort() {
    return "فاصلهٔ حذف‌شده";
  }

  private String getSuggestion() {
    return "فاصلهٔ قبل از حرف ربط حذف شده‌است";
  }

  @Override
  public void reset() {
  }  

}
