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
package de.danielnaber.languagetool.rules;

import java.io.IOException;

import junit.framework.TestCase;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.TestTools;

/**
 * @author Daniel Naber
 */
public class CommaWhitespaceRuleTest extends TestCase {

  public void testRule() throws IOException {
    CommaWhitespaceRule rule = new CommaWhitespaceRule(TestTools.getEnglishMessages());
    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(Language.ENGLISH);
    
    // correct sentences:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("This is a test sentence.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("This, is, a test sentence.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("This (foo bar) is a test!.")).length);
    //we get only entities into the comma rule, so let's test for entities:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("&quot;This is it,&quot; he said.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das kostet €2,45.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das kostet 50,- Euro")).length);
    //test OpenOffice field codes:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("In his book,\u0002 Einstein proved this to be true.")).length);
    
    //test thousand separators:    
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("This is $1,000,000.")).length);
    //test numbers:    
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("This is 1,5.")).length);
    
    //test two consecutive commas:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("This is a ,,test''.")).length);
    
    // errors:
    matches = rule.match(langTool.getAnalyzedSentence("This,is a test sentence."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("This , is a test sentence."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("This ,is a test sentence."));
    assertEquals(2, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence(",is a test sentence."));
    assertEquals(2, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("This ( foo bar) is a test!."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("This (foo bar ) is a test!."));      
    assertEquals(1, matches.length);
    
    //other brackets, first [
    matches = rule.match(langTool.getAnalyzedSentence("This [ foo bar) is a test!."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("This (foo bar ] is a test!."));      
    assertEquals(1, matches.length);
    //now {
    matches = rule.match(langTool.getAnalyzedSentence("This { foo bar) is a test!."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("This (foo bar } is a test!."));      
    assertEquals(1, matches.length);
    
    //full stop error:
    matches = rule.match(langTool.getAnalyzedSentence("This is a sentence with an orphaned full stop ."));
    assertEquals(1, matches.length);
    //full stop exception cases:
    matches = rule.match(langTool.getAnalyzedSentence("This is a sentence with ellipsis ..."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("This is a figure: .5 and it's correct."));
    assertEquals(0, matches.length);
    
    matches = rule.match(langTool.getAnalyzedSentence("ABB (  z.B. )"));
    // check match positions:
    assertEquals(2, matches.length);
    assertEquals(4, matches[0].getFromPos());
    assertEquals(6, matches[0].getToPos());
    assertEquals(11, matches[1].getFromPos());
    assertEquals(13, matches[1].getToPos());
    matches = rule.match(langTool.getAnalyzedSentence("This is a test with a OOo footnote\u0002, which is denoted by 0x2 in the text."));
    assertEquals(0, matches.length);
  }
  
}
