package org.languagetool.rules.uk;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

import org.junit.Before;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

public abstract class AbstractRuleTest {
  protected JLanguageTool lt;
  protected Rule rule;

  
  @Before
  public void setUpBase() throws IOException {
    lt = new JLanguageTool(new Ukrainian());
  }

  
  
  protected void assertEmptyMatch(String text) {
    try {
      assertEquals(Collections.<RuleMatch>emptyList(), Arrays.asList(rule.match(lt.getAnalyzedSentence(text))));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  
  protected void assertHasError(String text, String... suggestions) {
    try {
      AnalyzedSentence sent = lt.getAnalyzedSentence(text);
      RuleMatch[] match = rule.match(sent);
      assertEquals(1, match.length);
      if( suggestions.length > 0 ) {
        assertEquals(Arrays.asList(suggestions), match[0].getSuggestedReplacements());
      }
      
      for(String sugg: suggestions) {
        assertEmptyMatch(sugg);
      }
      
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }



  protected RuleMatch[] ruleMatch(String text) throws IOException {
    return rule.match(lt.getAnalyzedSentence(text));
  }



  protected void assertMatches(int num, String text, Consumer<String> c) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(text));
    assertEquals("Unexpected: " + Arrays.asList(matches), num, matches.length);
  
    if( c != null ) {
    for(RuleMatch match: matches) {
      c.accept(match.getMessage());
    }
    }
  }

//  protected void assertHasError(String text) {
//    try {
//      AnalyzedSentence sent = lt.getAnalyzedSentence(text);
//      assertEquals(1, rule.match(sent).length);
//    } catch (IOException e) {
//      throw new RuntimeException(e);
//    }
//  }

}
