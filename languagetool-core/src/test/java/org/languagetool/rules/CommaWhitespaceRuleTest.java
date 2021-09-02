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

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class CommaWhitespaceRuleTest {

  private CommaWhitespaceRule rule;
  private JLanguageTool lt;
  
  @Before
  public void setUp() {
    rule = new CommaWhitespaceRule(TestTools.getEnglishMessages());
    lt = new JLanguageTool(TestTools.getDemoLanguage());
  }

  @Test
  public void testRule() throws IOException {
    assertMatches("This is a test sentence.", 0);
    assertMatches("This, is, a test sentence.", 0);
    assertMatches("This (foo bar) is a test!.", 0);
    assertMatches("Das kostet €2,45.", 0);
    assertMatches("Das kostet 50,- Euro", 0);
    assertMatches("This is a sentence with ellipsis ...", 0);
    assertMatches("This is a figure: .5 and it's correct.", 0);
    assertMatches("This is $1,000,000.", 0);
    assertMatches("This is 1,5.", 0);
    assertMatches("This is a ,,test''.", 0);
    assertMatches("Run ./validate.sh to check the file.", 0);
    assertMatches("This is,\u00A0really,\u00A0non-breaking whitespace.", 0);
    //test OpenOffice field codes:
    assertMatches("In his book,\u0002 Einstein proved this to be true.", 0);
    assertMatches("- [ ] A checkbox at GitHub", 0);
    assertMatches("- [x] A checked checkbox at GitHub", 0);
    assertMatches("A sentence 'with' ten \"correct\" examples of ’using’ quotation “marks” at «once» in it.", 0);
    assertMatches("I'd recommend resaving the .DOC as a PDF file.", 0);
    assertMatches("I'd recommend resaving the .mp3 as a WAV file.", 0);
    assertMatches("I'd suggest buying the .org domain.", 0);
    assertMatches("I live in .Los Angeles", 1);

    // errors:
    assertMatches("This,is a test sentence.", 1);
    assertMatches("This , is a test sentence.", 1);
    assertMatches("This ,is a test sentence.", 2);
    assertMatches(",is a test sentence.", 2);
    assertMatches("This ( foo bar) is a test!.", 1);
    assertMatches("This (foo bar ) is a test!.", 1);
    assertMatches("This [ foo bar) is a test!.", 1);
    assertMatches("This (foo bar ] is a test!.", 1);
    assertMatches("This { foo bar) is a test!.", 1);
    assertMatches("This (foo bar } is a test!.", 1);
    assertMatches("This is a sentence with an orphaned full stop .", 1);
    assertMatches("This is a test with a OOo footnote\u0002, which is denoted by 0x2 in the text.", 0);
    assertMatches("A sentence ' with ' ten \" incorrect \" examples of ’ using ’ quotation “ marks ” at « once » in it.", 10);
    assertMatches("A sentence ' with' one examples of wrong quotations marks in it.", 1);
    assertMatches("A sentence 'with ' one examples of wrong quotations marks in it.", 1);

    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("ABB (  z.B. )"));
    assertEquals(2, matches.length);
    assertEquals(4, matches[0].getFromPos());
    assertEquals(6, matches[0].getToPos());
    assertEquals(11, matches[1].getFromPos());
    assertEquals(13, matches[1].getToPos());
    
    matches = rule.match(lt.getAnalyzedSentence("This ,"));
    assertEquals(1, matches.length);
    assertEquals(",", matches[0].getSuggestedReplacements().get(0));
    matches = rule.match(lt.getAnalyzedSentence("This ,is a test sentence."));
    assertEquals(2, matches.length);
    assertEquals(", ", matches[0].getSuggestedReplacements().get(0));
    matches = rule.match(lt.getAnalyzedSentence("This , is a test sentence."));
    assertEquals(1, matches.length);
    assertEquals(",", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(lt.getAnalyzedSentence("You \" fixed\" it."));
    assertEquals(1, matches.length);
    assertEquals("\" ", matches[0].getSuggestedReplacements().get(0));
    assertEquals(" \"", matches[0].getSuggestedReplacements().get(1));
    assertEquals(3, matches[0].getFromPos());
    assertEquals(6, matches[0].getToPos());
    matches = rule.match(lt.getAnalyzedSentence("You \"fixed \" it."));
    assertEquals(1, matches.length);
    assertEquals("\" ", matches[0].getSuggestedReplacements().get(0));
    assertEquals(" \"", matches[0].getSuggestedReplacements().get(1));
    assertEquals(10, matches[0].getFromPos());
    assertEquals(13, matches[0].getToPos());

    assertMatches("Ellipsis . . . as suggested by The Chicago Manual of Style", 3);
    assertMatches("Ellipsis . . . . as suggested by The Chicago Manual of Style", 4);
  }

  private void assertMatches(String text, int expectedMatches) throws IOException {
    assertEquals(expectedMatches, rule.match(lt.getAnalyzedSentence(text)).length);
  }

}
