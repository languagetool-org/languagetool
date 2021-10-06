package org.languagetool.rules.en;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.TextLevelRule;

public class EnglishRepeatedWordsRuleTest {

  private TextLevelRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() {
    rule = new EnglishRepeatedWordsRule(TestTools.getEnglishMessages(), new AmericanEnglish());
    lt = new JLanguageTool(Languages.getLanguageForShortCode("en"));
  }

  @Test
  public void testRule() throws IOException {
    assertCorrectText("I suggested that, but he also suggests that.");
        
    RuleMatch[] matches=getRuleMatches("I suggested this. She suggests that.");
    assertEquals(1, matches.length);
    assertEquals(22, matches[0].getFromPos());
    assertEquals(30, matches[0].getToPos());
    assertEquals("proposes", matches[0].getSuggestedReplacements().get(0));
    assertEquals("recommends", matches[0].getSuggestedReplacements().get(1));
    assertEquals("submits", matches[0].getSuggestedReplacements().get(2));
      
    
    matches=getRuleMatches ("I suggested this. She suggests that. And they suggested that.");
    assertEquals(2, matches.length);
    assertEquals(22, matches[0].getFromPos());
    assertEquals(46, matches[1].getFromPos());
    assertEquals("proposes", matches[0].getSuggestedReplacements().get(0));
    assertEquals("proposed", matches[1].getSuggestedReplacements().get(0));
    
    matches=getRuleMatches ("The problem was global. And the solutions needed to be global.");
    assertEquals(1, matches.length);
    assertEquals("comprehensive", matches[0].getSuggestedReplacements().get(0));
    
  }
  
  private RuleMatch[] getRuleMatches(String sentences) throws IOException {
    AnnotatedText aText = new AnnotatedTextBuilder().addText(sentences).build();
    return rule.match(lt.analyzeText(sentences), aText);
  }

  private void assertCorrectText(String sentences) throws IOException {
    AnnotatedText aText = new AnnotatedTextBuilder().addText(sentences).build();
    RuleMatch[] matches = rule.match(lt.analyzeText(sentences), aText);
    assertEquals(0, matches.length);
  }


}
