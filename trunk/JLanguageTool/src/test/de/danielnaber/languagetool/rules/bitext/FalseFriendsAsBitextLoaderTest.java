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
package de.danielnaber.languagetool.rules.bitext;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.rules.patterns.bitext.BitextPatternRule;
import de.danielnaber.languagetool.rules.patterns.bitext.FalseFriendsAsBitextLoader;

/**
 * @author Marcin Miłkowski
 */
public class FalseFriendsAsBitextLoaderTest extends TestCase {


  public void testHintsForPolishTranslators() throws IOException, ParserConfigurationException, SAXException {
    JLanguageTool langTool = new JLanguageTool(Language.ENGLISH, Language.POLISH);
    JLanguageTool trgTool = new JLanguageTool(Language.POLISH);
    
    FalseFriendsAsBitextLoader ruleLoader = new FalseFriendsAsBitextLoader();
    final String name = "/false-friends.xml";
    final List<BitextPatternRule> rules = ruleLoader.
    getFalseFriendsAsBitext(        
        JLanguageTool.getDataBroker().getRulesDir() + name,         
        Language.ENGLISH, Language.POLISH);    
    
    assertErrors(1, rules, "This is an absurd.", "To absurd.", langTool, trgTool);       
    assertErrors(1, rules, "I have to speak to my advocate.", "Muszę porozmawiać z adwokatem.", langTool, trgTool);    
    assertErrors(1, rules, "This is not actual.", "To nie jest aktualne.", langTool, trgTool);
    assertErrors(0, rules, "This is not actual.", "To nie jest rzeczywiste.", langTool, trgTool);
  }
  
  private List<RuleMatch> check(final List<BitextPatternRule> bRules, 
      final String src, final String trg, 
      final JLanguageTool srcTool, final JLanguageTool trgTool) throws IOException {
    List<RuleMatch> allMatches = new ArrayList<RuleMatch>();
    for (BitextPatternRule bRule : bRules) {
     RuleMatch[] matches = match(bRule, src, trg, srcTool, trgTool);
     if (matches != null) {
       for (RuleMatch match : matches) {
         allMatches.add(match);
       }
     }
    }
    return allMatches;
  }
  
  private RuleMatch[] match(final BitextPatternRule rule, final String src, final String trg,
      final JLanguageTool srcLanguageTool,
      final JLanguageTool trgLanguageTool) throws IOException {
    final AnalyzedSentence srcText = srcLanguageTool.getAnalyzedSentence(src);
    final AnalyzedSentence trgText = trgLanguageTool.getAnalyzedSentence(trg);
    return rule.match(srcText, trgText);    
  }
  
  private void assertErrors(int errorCount, 
      final List<BitextPatternRule> rules, 
      final String src, final String trg, JLanguageTool srcTool, JLanguageTool trgTool) throws IOException {
    List<RuleMatch> matches = check(rules, src, trg, srcTool, trgTool);
    //System.err.println(matches);
    assertEquals(errorCount, matches.size());
  }   
}
