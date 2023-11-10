/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
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
package org.languagetool.rules.pt;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;


import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class MorfologikPortugueseSpellerRuleTest {
  private final MorfologikPortugueseSpellerRule ruleBR = getSpellerRule("BR");
  private final JLanguageTool ltBR = getLT("BR");
  private final MorfologikPortugueseSpellerRule rulePT = getSpellerRule("PT");
  private final JLanguageTool ltPT = getLT("PT");
  // This one is used to test the pre-90 agreement spellings
  private final MorfologikPortugueseSpellerRule ruleMZ = getSpellerRule("MZ");
  private final JLanguageTool ltMZ = getLT("MZ");

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

  private void assertSingleErrorWithNegativeSuggestion(String sentence, JLanguageTool lt,
                                                       MorfologikPortugueseSpellerRule rule,
                                                       String badSuggestion) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
    // TODO: just debugging, must delete later!
    if (matches.length > 0) {
      System.out.println(matches[0].getSuggestedReplacements());
    }
    assertEquals(1, matches.length);
    if (matches.length > 0) {
      assertFalse(matches[0].getSuggestedReplacements().contains(badSuggestion));
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
    assertSingleError(sentencePT, ltBR, ruleBR, new String[]{sentenceBR});
    assertNoErrors(sentencePT, ltPT, rulePT);
    assertSingleError(sentenceBR, ltPT, rulePT, new String[]{sentencePT});
  }

  private void assertTwoWayOrthographicAgreementError(String sentence90, String sentence45) throws IOException {
    assertNoErrors(sentence90, ltPT, rulePT);
    assertSingleError(sentence45, ltPT, rulePT, new String[]{sentence90});
    assertNoErrors(sentence45, ltMZ, ruleMZ);
    assertSingleError(sentence90, ltMZ, ruleMZ, new String[]{sentence45});
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
    assertTwoWayDialectError("tênis", "ténis");
    assertTwoWayDialectError("ônus", "ónus");
    // I swear I'm not being immature, there was some weirdness with "pêni"/"pênis" in pt-BR ;)
    assertTwoWayDialectError("pênis", "pénis");
    assertTwoWayDialectError("detecção", "deteção");
    assertTwoWayDialectError("dezesseis", "dezasseis");
    // new words from portal da língua portuguesa
    assertTwoWayDialectError("napoleônia", "napoleónia");
    assertTwoWayDialectError("hiperêmese", "hiperémese");
    // orthographic reforms
    assertTwoWayOrthographicAgreementError("detetar", "detectar");
    // not working yet
    assertTwoWayDialectError("detectar", "detetar");
    // will not work due to tokenisation quirk, bebê-lo, must be fixed
    // assertTwoWayDialectError("bebê", "bebé");
  }

  @Test
  public void testPortugueseSpellingDiminutives() throws Exception {
    assertNoErrors("franguito", ltBR, ruleBR);
    assertNoErrors("irmãozinho", ltBR, ruleBR);
    assertNoErrors("retratozinho", ltBR, ruleBR);
    assertNoErrors("notebookzinho", ltBR, ruleBR);
    assertNoErrors("finaizitos", ltBR, ruleBR);
    assertNoErrors("cafezito", ltBR, ruleBR);
    assertNoErrors("chorõezitos", ltBR, ruleBR);
    assertNoErrors("assadito", ltBR, ruleBR);
  }

  @Test
  public void testPortugueseSpellingProductiveAdverbs() throws Exception {
    assertNoErrors("enciclopedicamente", ltBR, ruleBR);
    assertNoErrors("nefastamente", ltBR, ruleBR);
    assertNoErrors("funereamente", ltBR, ruleBR);
  }

  @Test
  public void testPortugueseSpellingValidAbbreviations() throws Exception {
    // need to understand how to test segment.srx here!
    assertSingleError("primit", ltBR, ruleBR, new String[]{"primit."});
    assertSingleError("Islam,", ltBR, ruleBR, new String[]{"Islam."});
    assertNoErrors("xerogr.", ltBR, ruleBR);
    assertNoErrors("Baixei a vers. 7.0.0", ltBR, ruleBR);
    assertSingleError("Sem terminol exata, nunca vamos saber.", ltBR, ruleBR, new String[]{"terminol."});
  }

  @Test
  public void testPortugueseSpellingMultiwords() throws Exception {
    assertSingleError("volant", ltBR, ruleBR, new String[]{});
    assertNoErrors("verba volant, scripta remnant", ltBR, ruleBR);
    assertSingleError("Raspberry", ltBR, ruleBR, new String[]{});
    assertNoErrors("Raspberry Pi", ltBR, ruleBR);
  }

  @Test
  public void testPortugueseSpellingDoesNotSuggestOffensiveWords() throws Exception {
    // some words should not be suggested; this test makes sure they are *not* in the returned suggestions for
    // each given incorrectly spelt word
    assertSingleErrorWithNegativeSuggestion("pwta", ltBR, ruleBR, "puta");
    assertSingleErrorWithNegativeSuggestion("bâbaca", ltBR, ruleBR, "babaca");
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
