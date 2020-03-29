package org.languagetool.rules.ca;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Catalan;
import org.languagetool.rules.RuleMatch;

public class PronomFebleDuplicateRuleTest {
  private PronomFebleDuplicateRule rule;
  private JLanguageTool langTool;

  @Before
  public void setUp() throws IOException {
    rule = new PronomFebleDuplicateRule(TestTools.getEnglishMessages());
    langTool = new JLanguageTool(new Catalan());
  }

  @Test
  public void testRule() throws IOException { 
    
    assertCorrect("N'hi ha d'haver.");
    assertCorrect("Hi podria haver un error.");
    assertCorrect("Es divertien llançant-se pedres.");
    assertCorrect("Es recomana tapar-se la boca.");
    assertCorrect("S'ordena dutxar-se cada dia.");
    assertCorrect("Es va quedar barallant-se amb el seu amic.");
    assertCorrect("Es va quedar se");

    RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("S'ha de fer-se"));
    assertEquals(1, matches.length);
    assertEquals("Ha de fer-se", matches[0].getSuggestedReplacements().get(0));
    assertEquals("S'ha de fer", matches[0].getSuggestedReplacements().get(1));
    
    matches = rule.match(langTool.getAnalyzedSentence("N'ha d'haver-hi"));
    assertEquals(1, matches.length);
    
    matches = rule.match(langTool.getAnalyzedSentence("Hi ha d'haver-ne"));
    assertEquals(1, matches.length);
    
    matches = rule.match(langTool.getAnalyzedSentence("Es va continuar barallant-se amb el seu amic."));
    assertEquals(1, matches.length);
    assertEquals("Va continuar barallant-se", matches[0].getSuggestedReplacements().get(0));
    assertEquals("Es va continuar barallant", matches[0].getSuggestedReplacements().get(1));
    
    matches = rule.match(langTool.getAnalyzedSentence("Hi podria haver-hi"));
    assertEquals(1, matches.length);
    assertEquals("Podria haver-hi", matches[0].getSuggestedReplacements().get(0));
    assertEquals("Hi podria haver", matches[0].getSuggestedReplacements().get(1));
    
    matches = rule.match(langTool.getAnalyzedSentence("ho puc arreglar-ho"));
    assertEquals(1, matches.length);
    assertEquals("puc arreglar-ho", matches[0].getSuggestedReplacements().get(0));
    assertEquals("ho puc arreglar", matches[0].getSuggestedReplacements().get(1));
    
    matches = rule.match(langTool.getAnalyzedSentence("En vaig portar-ne quatre."));
    assertEquals(1, matches.length);
    assertEquals("Vaig portar-ne", matches[0].getSuggestedReplacements().get(0));
    assertEquals("En vaig portar", matches[0].getSuggestedReplacements().get(1));
    
    matches = rule.match(langTool.getAnalyzedSentence("Ho hem hagut de fer-ho."));
    assertEquals(1, matches.length);
    assertEquals("Hem hagut de fer-ho", matches[0].getSuggestedReplacements().get(0));
    assertEquals("Ho hem hagut de fer", matches[0].getSuggestedReplacements().get(1));
    
    matches = rule.match(langTool.getAnalyzedSentence("Hi hem hagut de continuar anant-hi."));
    assertEquals(1, matches.length);
    assertEquals("Hem hagut de continuar anant-hi", matches[0].getSuggestedReplacements().get(0));
    assertEquals("Hi hem hagut de continuar anant", matches[0].getSuggestedReplacements().get(1));
    
    matches = rule.match(langTool.getAnalyzedSentence("M'he de rentar-me les dents."));
    assertEquals(1, matches.length);
    assertEquals("He de rentar-me", matches[0].getSuggestedReplacements().get(0));
    assertEquals("M'he de rentar", matches[0].getSuggestedReplacements().get(1));
    
  }
    
    private void assertCorrect(String sentence) throws IOException {
      final RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence(sentence));
      assertEquals(0, matches.length);
    }

}
