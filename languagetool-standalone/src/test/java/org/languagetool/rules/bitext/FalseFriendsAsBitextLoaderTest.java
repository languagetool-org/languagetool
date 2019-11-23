/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.bitext;

import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.language.English;
import org.languagetool.language.Polish;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.bitext.BitextPatternRule;
import org.languagetool.rules.patterns.bitext.FalseFriendsAsBitextLoader;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FalseFriendsAsBitextLoaderTest {

  @Test
  public void testHintsForPolishTranslators() throws IOException, ParserConfigurationException, SAXException {
    Polish polish = new Polish();
    English english = new English();
    JLanguageTool lt = new JLanguageTool(english, polish);
    JLanguageTool trgTool = new JLanguageTool(polish);
    
    FalseFriendsAsBitextLoader ruleLoader = new FalseFriendsAsBitextLoader();
    String name = "/false-friends.xml";
    List<BitextPatternRule> rules = ruleLoader.
    getFalseFriendsAsBitext(name, english, polish);
    
    assertErrors(1, rules, "This is an absurd.", "To absurd.", lt, trgTool);       
    assertErrors(1, rules, "I have to speak to my advocate.", "Muszę porozmawiać z adwokatem.", lt, trgTool);    
    assertErrors(1, rules, "This is not actual.", "To nie jest aktualne.", lt, trgTool);
    assertErrors(0, rules, "This is not actual.", "To nie jest rzeczywiste.", lt, trgTool);
  }
  
  private List<RuleMatch> check(List<BitextPatternRule> bRules, 
      String src, String trg, 
      JLanguageTool srcTool, JLanguageTool trgTool) throws IOException {
    List<RuleMatch> allMatches = new ArrayList<>();
    for (BitextPatternRule bRule : bRules) {
     RuleMatch[] matches = match(bRule, src, trg, srcTool, trgTool);
     if (matches != null) {
       Collections.addAll(allMatches, matches);
     }
    }
    return allMatches;
  }
  
  private RuleMatch[] match(BitextPatternRule rule, String src, String trg,
      JLanguageTool srcLanguageTool,
      JLanguageTool trgLanguageTool) throws IOException {
    AnalyzedSentence srcText = srcLanguageTool.getAnalyzedSentence(src);
    AnalyzedSentence trgText = trgLanguageTool.getAnalyzedSentence(trg);
    return rule.match(srcText, trgText);    
  }
  
  private void assertErrors(int errorCount, 
      List<BitextPatternRule> rules, 
      String src, String trg, JLanguageTool srcTool, JLanguageTool trgTool) throws IOException {
    List<RuleMatch> matches = check(rules, src, trg, srcTool, trgTool);
    assertEquals(errorCount, matches.size());
  }   
}
