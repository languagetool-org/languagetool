package org.languagetool.rules.pt;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;


import java.io.IOException;
import java.util.Arrays;

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
    // TODO: just debugging, must delete later!
    if (matches.length > 0) {
      System.out.println(matches[0].getSuggestedReplacements());
    }
    assertEquals(length, matches.length);
    if (matches.length > 0) {
      assert matches[0].getSuggestedReplacements().containsAll(Arrays.asList(suggestions));
    }
  }

  private void assertSingleErrorAndPos(String sentence, JLanguageTool lt, MorfologikPortugueseSpellerRule rule,
                                       String[] suggestions, int fromPos, int toPos) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
    for (int i = 0; i < suggestions.length; i++) {
      assertEquals(suggestions[i], matches[0].getSuggestedReplacements().get(i));
    }
    assertEquals(1, matches.length);
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

  private void assertTwoWayDialectError(String sentenceBR, String sentencePT) throws IOException {
    assertNoErrors(sentenceBR, ltBR, ruleBR);
    assertSingleError(sentenceBR, ltPT, rulePT, new String[]{sentencePT});
    assertNoErrors(sentencePT, ltPT, rulePT);
    assertSingleError(sentencePT, ltBR, ruleBR, new String[]{sentenceBR});
  }

  @Test
  public void testSanity() throws Exception {
    assertNoErrors("oogaboogatestword", ltBR, ruleBR);
    assertNoErrors("oogaboogatestwordBR", ltBR, ruleBR);
    assertNoErrors("oogaboogatestword", ltPT, rulePT);
    assertNoErrors("oogaboogatestwordPT", ltPT, rulePT);
    assertNoErrors("oogaboogatestwordPT90", ltPT, rulePT);
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
  public void testPortugueseHyphenatedClitics() throws Exception {
    assertNoErrors("diz-se", ltBR, ruleBR);
    assertNoErrors("fá-lo-á", ltBR, ruleBR);
    assertNoErrors("dir-lhe-ia", ltBR, ruleBR);
    assertNoErrors("amar-nos-emos", ltBR, ruleBR);
    assertNoErrors("dê-mo", ltBR, ruleBR);
  }

  // FUCK YEAH WAHOO
  @Test
  public void testPortugueseSymmetricalDialectDifferences() throws Exception {
    assertTwoWayDialectError("anônimo", "anónimo");
    assertTwoWayDialectError("detecção", "deteção");
    assertTwoWayDialectError("dezesseis", "dezasseis");
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
    assertNoErrors("R$ 3", ltBR, ruleBR);
    assertNoErrors("US$1.000,00", ltBR, ruleBR);
    assertNoErrors("€99,99", ltBR, ruleBR);
    assertNoErrors("6£", ltBR, ruleBR);
    assertNoErrors("30 R$", ltBR, ruleBR);
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

  // TODO: get rid of this test, of course
  @Test
  public void testBrazilPortugueseSpellingMorfologikWeirdness() throws Exception {
    // 'ja' not corrected to 'já' – the issue here is with the .aff to "ja-la", which... I think should be "já-la"?
    // either way, when we tokenise it, it splits that into "ja" and "la"...
    assertSingleError("eu ja fiz isso.", ltBR, ruleBR, new String[]{"já"});
    // corrected to bizarre 'autoconheci emen'
    assertSingleErrorAndPos("- Encontre no autoconheciemen", ltBR, ruleBR,
      new String[]{"autoconhecimento"}, 14, 29);
  }

  @Test
  public void testCustomReplacements() throws Exception {
    // Testing the info file.
    // Here it's less about the error (though of course that's important), and more about the suggestions.
    // We want our custom-defined patterns to prioritise specific types of suggestions.
    assertSingleError("vinheram", ltBR, ruleBR, new String[]{"vieram"});
    assertSingleError("recadações", ltBR, ruleBR, new String[]{"arrecadações"});
    assertSingleError("me dis", ltBR, ruleBR, new String[]{"diz"});
    assertSingleError("ebook", ltBR, ruleBR, new String[]{"e-book"});
    assertSingleError("quizese", ltBR, ruleBR, new String[]{"quisesse"}); // this one's tricky to get working
    assertSingleError("cabesse", ltBR, ruleBR, new String[]{"coubesse"});
    assertSingleError("andância", ltBR, ruleBR, new String[]{"andança"});
    assertSingleError("abto", ltBR, ruleBR, new String[]{"hábito"});
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
