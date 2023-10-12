package org.languagetool.rules.pt;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;


import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class MorfologikPortugueseSpellerRuleTest {
  private final MorfologikPortugueseSpellerRule ruleBR = getSpellerRule("BR");
  private final JLanguageTool ltBR = getLT("BR");
  private final MorfologikPortugueseSpellerRule rulePT = getSpellerRule("PT");
  private final JLanguageTool ltPT = getLT("PT");

  public MorfologikPortugueseSpellerRuleTest() throws IOException {
  }

  private MorfologikPortugueseSpellerRule getSpellerRule(String countryCode) throws IOException {
    return new MorfologikPortugueseSpellerRule(TestTools.getMessages("pt"),
      Languages.getLanguageForShortCode("pt-" + countryCode), null, null);
  }

  private JLanguageTool getLT(String countryCode) {
    return new JLanguageTool(Languages.getLanguageForShortCode("pt-" + countryCode));
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
    assertSingleError("ShintaroW.", ltBR, ruleBR, new String[]{});
    assertSingleError("SHINTAROW.", ltBR, ruleBR, new String[]{});
    assertSingleError("Shintaro Wada", ltBR, ruleBR, new String[]{"Shuntar"});

    assertNoErrors("A família.", ltBR, ruleBR);
    assertSingleError("A familia.", ltBR, ruleBR, new String[]{"família", "Família", "famílias", "familiar"});

    assertNoErrors("Covid-19, COVID-19, covid-19.", ltBR, ruleBR);

    assertSingleError("eu so", ltBR, ruleBR, new String[]{"só"});
    assertSingleError("é so", ltBR, ruleBR, new String[]{"só"});

    assertSingleErrorAndPos("Sr. Kato nos ensina inglês", ltBR, ruleBR, new String[]{"Fato"}, 4, 8);
  }

  @Test
  public void testBrazilPortugueseSpellingDoesNotCheckHashtags() throws Exception {
    assertNoErrors("#CantadaBoBem", ltBR, ruleBR);
  }

  @Test
  public void testBrazilPortugueseSpellingDoesNotCheckUserMentions() throws Exception {
    assertNoErrors("@nomeDoUsuario", ltBR, ruleBR);
  }

  @Test
  public void testBrazilPortugueseSpellingDoesNotCheckCurrencyValues() throws Exception {
    assertNoErrors("R$45,00", ltBR, ruleBR);
    assertNoErrors("US$1.000,00", ltBR, ruleBR);
    assertNoErrors("€99,99", ltBR, ruleBR);
    assertNoErrors("US$", ltBR, ruleBR);
  }

  @Test
  public void testBrazilPortugueseSpellingDoesNotCheckNumberAbbreviations() throws Exception {
    assertNoErrors("Nº666", ltBR, ruleBR);  // superscript 'o'
    assertNoErrors("N°42189", ltBR, ruleBR);  // degree symbol, we'll do this in XML rules
    assertNoErrors("Nº 420", ltBR, ruleBR);
    assertNoErrors("N.º69", ltBR, ruleBR);
    assertNoErrors("N.º 80085", ltBR, ruleBR);
  }

  @Test
  public void testBrazilPortugueseSpellingSplitsEmoji() throws Exception {
    assertSingleError("☺☺☺Só", ltBR, ruleBR, new String[]{"☺☺☺ Só"});
  }

  @Test
  public void testBrazilPortugueseSpellingDoesNotCheckXForVezes() throws Exception {
    assertNoErrors("10X", ltBR, ruleBR);
    assertNoErrors("5x", ltBR, ruleBR);
  }

  @Test
  public void testBrazilPortugueseSpellingFailsWithModifierDiacritic() throws Exception {
    assertNoErrors("Não", ltBR, ruleBR);  // proper 'ã' char
    // this is acceptable because LT converts these compound chars to the proper ones
    assertSingleError("Não", ltBR, ruleBR, new String[]{"Não"});  // modifier tilde
  }

  @Test
  public void testBrazilPortugueseSpellingWorksWithRarePunctuation() throws Exception {
    assertNoErrors("⌈Herói⌋", ltBR, ruleBR);
    assertNoErrors("″Santo Antônio do Manga″", ltBR, ruleBR);
  }

  @Test
  public void testBrazilPortugueseSpellingMorfologikWeirdness() throws Exception {
    // 'ja' not corrected to 'já'!
    assertSingleError("eu ja fiz isso.", ltBR, ruleBR, new String[]{"já"});
    // corrected to bizarre 'autoconheci emen'
    assertSingleErrorAndPos("- Encontre no autoconheciemen", ltBR, ruleBR,
      new String[]{"autoconhecimento"}, 14, 29);
  }

  @Test
  public void testEuropeanPortugueseSpelling() throws Exception {
    assertEquals(0, rulePT.match(ltPT.getAnalyzedSentence("A família.")).length);
    RuleMatch[] matches = rulePT.match(ltPT.getAnalyzedSentence("A familia."));
    assertEquals(1, matches.length);
    assertEquals("família", matches[0].getSuggestedReplacements().get(0));
    assertEquals("famílias", matches[0].getSuggestedReplacements().get(1));
    assertEquals("familiar", matches[0].getSuggestedReplacements().get(2));

    assertEquals(0, rulePT.match(ltPT.getAnalyzedSentence("Covid-19, COVID-19, covid-19.")).length);

    matches = rulePT.match(ltPT.getAnalyzedSentence("eu ja fiz isso."));
    assertEquals(1, matches.length);
    assertEquals("já", matches[0].getSuggestedReplacements().get(0));

    matches = rulePT.match(ltPT.getAnalyzedSentence("eu so"));
    assertEquals(1, matches.length);
    assertEquals("só", matches[0].getSuggestedReplacements().get(0));

    matches = rulePT.match(ltPT.getAnalyzedSentence("é so"));
    assertEquals(1, matches.length);
    assertEquals("só", matches[0].getSuggestedReplacements().get(0));

    matches = rulePT.match(ltPT.getAnalyzedSentence("- Encontre no autoconheciemen"));
    assertEquals(1, matches.length);
    assertEquals("autoconhecimento", matches[0].getSuggestedReplacements().get(0));
    assertEquals(14, matches[0].getFromPos());
    assertEquals(29, matches[0].getToPos());
  }
}
