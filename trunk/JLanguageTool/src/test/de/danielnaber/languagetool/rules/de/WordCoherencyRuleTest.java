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
package de.danielnaber.languagetool.rules.de;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.RuleMatch;

/**
 * @author Daniel Naber
 */
public class WordCoherencyRuleTest extends TestCase {

  public void testRule() throws IOException {
    final WordCoherencyRule rule = new WordCoherencyRule(null);
    final JLanguageTool langTool = new JLanguageTool(Language.GERMAN);
    // correct sentences:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das ist aufwendig, aber nicht zu aufwendig.")).length);
    // as WordCoherencyRule keeps its state to check more than one sentence 
    // we need to create a new object each time:
    rule.reset();
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das ist aufwändig, aber nicht zu aufwändig.")).length);
    // errors:
    rule.reset();
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das ist aufwendig, aber nicht zu aufwändig.")).length);
    rule.reset();
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das ist aufwändig, aber nicht zu aufwendig.")).length);
  }
  
  public void testRuleCompleteTexts() throws IOException {
    final JLanguageTool langTool;
    // complete texts:
    List<RuleMatch> matches;
    //matches = langTool.check("Das ist aufwendig. Aber hallo. Es ist wirklich aufwendig.");
    //assertEquals(0, matches.size());
    langTool = new JLanguageTool(Language.GERMAN);
    matches = langTool.check("Das ist aufwändig. Aber hallo. Es ist wirklich aufwändig.");
    assertEquals(0, matches.size());
    
    matches = langTool.check("Das ist aufwendig. Aber hallo. Es ist wirklich aufwändig.");
    assertEquals(1, matches.size());
    
    matches = langTool.check("Das ist aufwändig. Aber hallo. Es ist wirklich aufwendig.");
    assertEquals(1, matches.size());
    
    // also find full forms:
    matches = langTool.check("Das ist aufwendig. Aber hallo. Es ist wirklich aufwendiger als...");
    assertEquals(0, matches.size());
    
    matches = langTool.check("Das ist aufwendig. Aber hallo. Es ist wirklich aufwändiger als...");
    assertEquals(1, matches.size());
    
    matches = langTool.check("Das ist aufwändig. Aber hallo. Es ist wirklich aufwendiger als...");
    assertEquals(1, matches.size());
    
    matches = langTool.check("Das ist das aufwändigste. Aber hallo. Es ist wirklich aufwendiger als...");
    assertEquals(1, matches.size());
    
    matches = langTool.check("Das ist das aufwändigste. Aber hallo. Es ist wirklich aufwendig.");
    assertEquals(1, matches.size());

    // cross-paragraph checks
    matches = langTool.check("Das ist das aufwändigste.\n\nAber hallo. Es ist wirklich aufwendig.");
    assertEquals(1, matches.size());
  }

}
