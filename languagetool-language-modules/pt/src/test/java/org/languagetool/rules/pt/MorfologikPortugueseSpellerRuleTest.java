package org.languagetool.rules.pt;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;


import static org.junit.Assert.assertEquals;

public class MorfologikPortugueseSpellerRuleTest {

  @Test
  public void testPortugalPortugueseSpelling() throws Exception {
    MorfologikPortugueseSpellerRule rule = new MorfologikPortugueseSpellerRule(TestTools.getMessages("pt"),
      Languages.getLanguageForShortCode("pt-PT"), null, null);
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("pt-PT"));

    assertEquals(0, rule.match(lt.getAnalyzedSentence("A família.")).length);
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("A familia."));
    assertEquals(1, matches.length);
    assertEquals("família", matches[0].getSuggestedReplacements().get(0));
    assertEquals("famílias", matches[0].getSuggestedReplacements().get(1));
    assertEquals("familiar", matches[0].getSuggestedReplacements().get(2));

    assertEquals(0, rule.match(lt.getAnalyzedSentence("Covid-19, COVID-19, covid-19.")).length);

    matches = rule.match(lt.getAnalyzedSentence("eu ja fiz isso."));
    assertEquals(1, matches.length);
    assertEquals("já", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(lt.getAnalyzedSentence("eu so"));
    assertEquals(1, matches.length);
    assertEquals("só", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(lt.getAnalyzedSentence("é so"));
    assertEquals(1, matches.length);
    assertEquals("só", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(lt.getAnalyzedSentence("- Encontre no autoconheciemen"));
    assertEquals(1, matches.length);
    assertEquals("autoconhecimento", matches[0].getSuggestedReplacements().get(0));
    assertEquals(14, matches[0].getFromPos());
    assertEquals(29, matches[0].getToPos());
  }

  public void testBrazilPortugueseSpelling() throws Exception {
    MorfologikPortugueseSpellerRule rule = new MorfologikPortugueseSpellerRule(TestTools.getMessages("pt"),
      Languages.getLanguageForShortCode("pt-BR"), null, null);
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("pt-BR"));

    assertEquals(0, rule.match(lt.getAnalyzedSentence("A família.")).length);
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("A familia."));
    assertEquals(1, matches.length);
    assertEquals("família", matches[0].getSuggestedReplacements().get(0));
    assertEquals("famílias", matches[0].getSuggestedReplacements().get(1));
    assertEquals("familiar", matches[0].getSuggestedReplacements().get(2));

    assertEquals(0, rule.match(lt.getAnalyzedSentence("Covid-19, COVID-19, covid-19.")).length);

    matches = rule.match(lt.getAnalyzedSentence("eu ja fiz isso."));
    assertEquals(1, matches.length);
    assertEquals("já", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(lt.getAnalyzedSentence("eu so"));
    assertEquals(1, matches.length);
    assertEquals("só", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(lt.getAnalyzedSentence("é so"));
    assertEquals(1, matches.length);
    assertEquals("só", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(lt.getAnalyzedSentence("- Encontre no autoconheciemen"));
    assertEquals(1, matches.length);
    assertEquals("autoconhecimento", matches[0].getSuggestedReplacements().get(0));
    assertEquals(14, matches[0].getFromPos());
    assertEquals(29, matches[0].getToPos());
  }
}
