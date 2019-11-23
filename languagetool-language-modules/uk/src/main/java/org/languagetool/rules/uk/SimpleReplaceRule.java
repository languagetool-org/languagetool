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
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.AbstractSimpleReplaceRule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tagging.uk.IPOSTag;
import org.languagetool.tagging.uk.PosTagHelper;
import org.languagetool.tools.Tools;

/**
 * A rule that matches words which should not be used and suggests correct ones
 * instead.
 * 
 * Ukrainian implementations. Loads the relevant words from
 * <code>rules/uk/replace.txt</code>.
 * 
 * @author Andriy Rysin
 */
public class SimpleReplaceRule extends AbstractSimpleReplaceRule {

  private static final Map<String, List<String>> wrongWords = loadFromPath("/uk/replace.txt");
  private final MorfologikUkrainianSpellerRule morfologikSpellerRule;

  @Override
  protected Map<String, List<String>> getWrongWords() {
    return wrongWords;
  }

  public SimpleReplaceRule(ResourceBundle messages, MorfologikUkrainianSpellerRule morfologikSpellerRule) throws IOException {
    super(messages);
    setIgnoreTaggedWords();
    this.morfologikSpellerRule = morfologikSpellerRule;
  }

  @Override
  public final String getId() {
    return "UK_SIMPLE_REPLACE";
  }

  @Override
  public String getDescription() {
    return "Пошук помилкових слів";
  }

  @Override
  public String getShort() {
    return "Помилка?";
  }

  @Override
  public String getMessage(String tokenStr, List<String> replacements) {
    return tokenStr + " - помилкове слово, виправлення: "
        + String.join(", ", replacements) + ".";
  }

  @Override
  protected boolean isTagged(AnalyzedTokenReadings tokenReadings) {
    for (AnalyzedToken token: tokenReadings.getReadings()) {
      String posTag = token.getPOSTag();
      if (isGoodPosTag(posTag)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected List<RuleMatch> findMatches(AnalyzedTokenReadings tokenReadings, AnalyzedSentence sentence) {
    List<RuleMatch> matches = super.findMatches(tokenReadings, sentence);
    if( matches.isEmpty() ) {
      if( PosTagHelper.hasPosTag(tokenReadings, Pattern.compile(".*?adjp:actv.*?:bad.*")) ) {
        String msg = "Активні дієприкметники не властиві українській мові.";
        String url;

        if( tokenReadings.getAnalyzedToken(0).getLemma().endsWith("ший") ) {
          msg += " Їх можна замінити на що + дієслово (випавший сніг - сніг, що випав), або на форму з суфіксом -л- (промокший - промоклий)";
          url = "http://padaread.com/?book=53784&pg=94";
        }
        else { // -ючий, -ющий тощо
          msg += " Їх можна замінити питомими словами в різний спосіб: що + дієслово (роблячий  - що робить), дієслівний корінь+ суфікси -льн-, -лив- тощо (збираючий - збиральний, обтяжуючий - обтяжливий),"
          + " заміна іменником (завідуючий - завідувач), заміна прикметником із відповідним значенням (діюча модель - робоча модель), зміна конструкції (з наступаючим Новим роком - з настанням Нового року) тощо.";
          url = "http://nbuv.gov.ua/j-pdf/Nchnpu_8_2013_5_2.pdf";
        }

        RuleMatch match = new RuleMatch(this, sentence, tokenReadings.getStartPos(), tokenReadings.getStartPos()
            + tokenReadings.getToken().length(), msg, getShort());

        match.setUrl(Tools.getUrl(url));

        matches.add(match);
      }
      else {
        if( PosTagHelper.hasPosTagPart(tokenReadings, ":bad") ) {
          try {
            String msg = "Неправильно написане слово.";

            RuleMatch match = new RuleMatch(this, sentence, tokenReadings.getStartPos(), tokenReadings.getStartPos()
                + tokenReadings.getToken().length(), msg, getShort());
            
            RuleMatch[] spellerMatches = morfologikSpellerRule.match(new AnalyzedSentence(new AnalyzedTokenReadings[] {tokenReadings}));
            if( spellerMatches.length > 0 ) {
              match.setSuggestedReplacements(spellerMatches[0].getSuggestedReplacements());
            }

            matches.add(match);
          }
          catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
    else {
      if( PosTagHelper.hasPosTagPart(tokenReadings, ":subst") ) {
        for(int i=0; i<matches.size(); i++) {
          RuleMatch match = matches.get(i);
          RuleMatch newMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(), "Це розмовна просторічна форма");
          newMatch.setSuggestedReplacements(match.getSuggestedReplacements());
          matches.set(i, newMatch);
        }
      }
    }
    return matches;
  }

  private boolean isGoodPosTag(String posTag) {
    return posTag != null
        && !JLanguageTool.PARAGRAPH_END_TAGNAME.equals(posTag)
        && !JLanguageTool.SENTENCE_END_TAGNAME.equals(posTag)
        && !posTag.contains(IPOSTag.bad.getText())
        && !posTag.contains("subst")
        && !posTag.startsWith("<");
  }

  @Override
  public boolean isCaseSensitive() {
    return false;
  }

}
