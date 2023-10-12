package org.languagetool.rules.pt;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;


import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class MorfologikPortugueseSpellerRuleTest {
  private final MorfologikPortugueseSpellerRule br_rule = getBRSpellerRule();
  private final JLanguageTool br_lt = getBRLanguageTool();

  public MorfologikPortugueseSpellerRuleTest() throws IOException {
  }

  private MorfologikPortugueseSpellerRule getBRSpellerRule() throws IOException {
    return new MorfologikPortugueseSpellerRule(TestTools.getMessages("pt"),
      Languages.getLanguageForShortCode("pt-BR"), null, null);
  }

  private JLanguageTool getBRLanguageTool() {
    return new JLanguageTool(Languages.getLanguageForShortCode("pt-BR"));
  }

  private void assertErrorLength(String sentence, int length, JLanguageTool lt,
                                        MorfologikPortugueseSpellerRule rule, String[] suggestions) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
    assertEquals(length, matches.length);
    for (int i = 0; i < suggestions.length; i++) {
      assertEquals(suggestions[i], matches[0].getSuggestedReplacements().get(i));
    }
  }

  private void assertSingleErrorAndPos(String sentence, JLanguageTool lt, MorfologikPortugueseSpellerRule rule,
                                       String[] suggestions, int fromPos, int toPos) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
    assertEquals(1, matches.length);
    for (int i = 0; i < suggestions.length; i++) {
      assertEquals(suggestions[i], matches[0].getSuggestedReplacements().get(i));
    }
    assertEquals(fromPos, matches[0].getFromPos());
    assertEquals(toPos, matches[0].getToPos());
  }

  private void assertNoErrors(String sentence, JLanguageTool lt, MorfologikPortugueseSpellerRule rule) throws IOException {
    assertErrorLength(sentence, 0, lt, rule, new String[]{});
  }

  private void assertSingleError(String sentence, JLanguageTool lt,
                                        MorfologikPortugueseSpellerRule rule, String[] suggestions) throws IOException {
    assertErrorLength(sentence, 1, lt, rule, suggestions);
  }

  @Test
  public void testBrazilPortugueseSpelling() throws Exception {
    JLanguageTool lt = br_lt;
    MorfologikPortugueseSpellerRule rule = br_rule;

    assertSingleError("ShintaroW.", lt, rule, new String[]{});
    assertSingleError("SHINTAROW.", lt, rule, new String[]{});
    assertSingleError("Shintaro Wada", lt, rule, new String[]{"Shuntar"});

    assertNoErrors("A família.", lt, rule);
    assertSingleError("A familia.", lt, rule, new String[]{"família", "Família", "famílias", "familiar"});

    assertNoErrors("Covid-19, COVID-19, covid-19.", lt, rule);

    // 'ja' not corrected to 'já'!
    assertSingleError("eu ja fiz isso.", lt, rule, new String[]{"já"});
    assertSingleError("eu so", lt, rule, new String[]{"só"});
    assertSingleError("é so", lt, rule, new String[]{"só"});

    // corrected to bizarre 'autoconheci emen'
    assertSingleErrorAndPos("- Encontre no autoconheciemen", lt, rule, new String[]{"autoconhecimento"}, 14, 29);
    assertSingleErrorAndPos("Sr. Kato nos ensina inglês", lt, rule, new String[]{"Fato"}, 4, 8);
  }

  @Test
  public void testBrazilPortugueseSpellingDoesNotCheckHashtags() throws Exception {
    assertNoErrors("#CantadaBoBem", br_lt, br_rule);
  }

  @Test
  public void testBrazilPortugueseSpellingDoesNotCheckUserMentions() throws Exception {
    assertNoErrors("@nomeDoUsuario", br_lt, br_rule);
  }

  @Test
  public void testBrazilPortugueseSpellingDoesNotCheckUserCurrencyValues() throws Exception {
    assertNoErrors("R$45,00", br_lt, br_rule);
    assertNoErrors("US$1.000,00", br_lt, br_rule);
    assertNoErrors("€99,99", br_lt, br_rule);
  }

  @Test
  public void testEuropeanPortugueseSpelling() throws Exception {
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
}
