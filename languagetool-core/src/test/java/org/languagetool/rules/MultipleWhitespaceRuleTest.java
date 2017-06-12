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
package org.languagetool.rules;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Marcin Milkowski
 */
public class MultipleWhitespaceRuleTest {

  @Test
  public void testRule() throws IOException {
    MultipleWhitespaceRule rule = new MultipleWhitespaceRule(TestTools.getEnglishMessages(), TestTools.getDemoLanguage());
    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(TestTools.getDemoLanguage());

    // correct sentences:
    matches = rule.match(langTool.getAnalyzedSentence("This is a test sentence."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("This is a test sentence..."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("\n\tThis is a test sentence..."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("Multiple tabs\t\tare okay"));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("\n This is a test sentence..."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("\n    This is a test sentence..."));
    assertEquals(0, matches.length);
    // Needs isParagraphStart creation. Excluding i = 1 will make the rule ignore multiple white spaces in middle senteces.
    // matches = rule.match(langTool.getAnalyzedSentence("    This is a test sentence..."));
    // assertEquals(0, matches.length);

    // incorrect sentences:
    matches = rule.match(langTool.getAnalyzedSentence("This  is a test sentence."));
    assertEquals(1, matches.length);
    assertEquals(4, matches[0].getFromPos());
    assertEquals(6, matches[0].getToPos());
    matches = rule.match(langTool.getAnalyzedSentence("\n   This  is a test sentence."));
    assertEquals(1, matches.length);
    assertEquals(8, matches[0].getFromPos());
    assertEquals(10, matches[0].getToPos());
    matches = rule.match(langTool.getAnalyzedSentence("This is a test   sentence."));
    assertEquals(1, matches.length);
    assertEquals(14, matches[0].getFromPos());
    assertEquals(17, matches[0].getToPos());
    matches = rule.match(langTool.getAnalyzedSentence("This is   a  test   sentence."));
    assertEquals(3, matches.length);
    assertEquals(7, matches[0].getFromPos());
    assertEquals(10, matches[0].getToPos());
    assertEquals(11, matches[1].getFromPos());
    assertEquals(13, matches[1].getToPos());
    assertEquals(17, matches[2].getFromPos());
    assertEquals(20, matches[2].getToPos());
    matches = rule.match(langTool.getAnalyzedSentence("\t\t\t    \t\t\t\t  "));
    assertEquals(1, matches.length);
    //with non-breakable spaces
    matches = rule.match(langTool.getAnalyzedSentence("This \u00A0is a test sentence."));
    assertEquals(1, matches.length);
    assertEquals(4, matches[0].getFromPos());
    assertEquals(6, matches[0].getToPos());    
  }

}
