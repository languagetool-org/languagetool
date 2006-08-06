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
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("This (foo bar) is a test(!).")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("\"This is it,\" he said.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das kostet €2,45.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das kostet 50,- Euro")).length);
    
    // errors:
    matches = rule.match(langTool.getAnalyzedSentence("This,is a test sentence."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("This , is a test sentence."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("This ,is a test sentence."));
    assertEquals(2, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence(",is a test sentence."));
    assertEquals(2, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("This ( foo bar) is a test(!)."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("This (foo bar ) is a test(!)."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("ABB (  z.B. )"));
    // check match positions:
    assertEquals(2, matches.length);
    assertEquals(4, matches[0].getFromPos());
    assertEquals(6, matches[0].getToPos());
    assertEquals(11, matches[1].getFromPos());
    assertEquals(13, matches[1].getToPos());
  }
  
}
