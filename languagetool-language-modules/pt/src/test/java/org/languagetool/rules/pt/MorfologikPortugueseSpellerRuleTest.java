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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

  private List<String> getFirstSuggestions(RuleMatch match, int max) {
    return match.getSuggestedReplacements().stream().limit(5).collect(Collectors.toList());
  }

  private void assertErrorLength(String sentence, int length, JLanguageTool lt, MorfologikPortugueseSpellerRule rule,
                                 String[] suggestions) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
    assertEquals(length, matches.length);
    if (matches.length > 0) {
//      System.out.println(matches[0].getSuggestedReplacements());
      List<String> returnedSuggestions = getFirstSuggestions(matches[0], 5);
      assert returnedSuggestions.containsAll(Arrays.asList(suggestions));
    }
  }

  private void assertSingleErrorWithNegativeSuggestion(String sentence, JLanguageTool lt,
                                                       MorfologikPortugueseSpellerRule rule,
                                                       String badSuggestion) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
    assertEquals(1, matches.length);
    if (matches.length > 0) {
      List<String> returnedSuggestions = matches[0].getSuggestedReplacements();
//      System.out.println(returnedSuggestions);
      assertFalse(returnedSuggestions.contains(badSuggestion));
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
                                 MorfologikPortugueseSpellerRule rule, String ...suggestions) throws IOException {
    assertErrorLength(sentence, 1, lt, rule, suggestions);
  }

  private void assertSingleExactError(String sentence, JLanguageTool lt, MorfologikPortugueseSpellerRule rule,
                                      String suggestion, String message, String id) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
    assertEquals(1, matches.length);
    if (matches.length > 0) {
      RuleMatch match = matches[0];
      List<String> returnedSuggestions = match.getSuggestedReplacements();
//      System.out.println(returnedSuggestions);
      assert Objects.equals(returnedSuggestions.get(0), suggestion);
      assert Objects.equals(match.getMessage(), message);
      assert Objects.equals(match.getSpecificRuleId(), id);
    }
  }

  private void assertTwoWayDialectError(String sentenceBR, String sentencePT) throws IOException {
    String brMessage = "Possível erro de ortografia: esta é a grafia utilizada no português europeu.";
    String ptMessage = "Possível erro de ortografia: esta é a grafia utilizada no português brasileiro.";
    assertNoErrors(sentenceBR, ltBR, ruleBR);
    assertSingleExactError(sentencePT, ltBR, ruleBR, sentenceBR, brMessage, "MORFOLOGIK_RULE_PT_BR_DIALECT");
    assertNoErrors(sentencePT, ltPT, rulePT);
    assertSingleExactError(sentenceBR, ltPT, rulePT, sentencePT, ptMessage, "MORFOLOGIK_RULE_PT_PT_DIALECT");
  }

  private void assertTwoWayOrthographicAgreementError(String sentence90, String sentence45) throws IOException {
    assertNoErrors(sentence90, ltPT, rulePT);
    assertSingleError(sentence45, ltPT, rulePT, sentence90);
    assertNoErrors(sentence45, ltMZ, ruleMZ);
    assertSingleError(sentence90, ltMZ, ruleMZ, sentence45);
  }

  @Test
  public void testPortugueseSpellerSanity() throws Exception {
    assertNoErrors("oogaboogatestword", ltBR, ruleBR);
    assertNoErrors("oogaboogatestwordBR", ltBR, ruleBR);
    assertNoErrors("oogaboogatestword", ltPT, rulePT);
    assertNoErrors("oogaboogatestwordPT", ltPT, rulePT);
    assertNoErrors("oogaboogatestwordPT90", ltPT, rulePT);
  }

  @Test
  public void testPortugueseSpellerSpecificIds() throws Exception {
    // Just make sure that the rule ID suffix applies correctly.
    RuleMatch[] matches = ruleBR.match(ltBR.getAnalyzedSentence("Vâmos detetar problèmas."));
    assert matches.length == 3;
    assert Objects.equals(matches[0].getSpecificRuleId(), "MORFOLOGIK_RULE_PT_BR");  // Vâmos
    assert Objects.equals(matches[1].getSpecificRuleId(), "MORFOLOGIK_RULE_PT_BR_DIALECT");  // detetar (pt-PT!)
    assert Objects.equals(matches[2].getSpecificRuleId(), "MORFOLOGIK_RULE_PT_BR");  // problèmas
  }

  public void testPortugueseSpelling(JLanguageTool lt, MorfologikPortugueseSpellerRule rule) throws Exception {
    // inherited from Hunspell tests
    assertNoErrors("A família.", lt, rule);
    assertSingleError("A familia.", lt, rule, new String[]{"família", "famílias", "familiar"});

    assertNoErrors("Covid-19, COVID-19", lt, rule);

    assertSingleError("eu so", lt, rule, new String[]{"sou", "só"});
    assertSingleError("é so", lt, rule, new String[]{"só"});

    assertSingleErrorAndPos("Sr. Qato nos ensina inglês", lt, rule, new String[]{"Fato"}, 4, 8);
    assertSingleErrorAndPos("- Encontre no autoconheciemen", lt, rule,
      new String[]{"autoconhecimento"}, 14, 29);
    assertSingleError("eu ja fiz isso.", lt, rule, new String[]{"já"});
  }

  @Test
  public void testEuropeanPortugueseSpelling() throws Exception {
    testPortugueseSpelling(ltPT, rulePT);
  }

  @Test
  public void testAfricanPortugueseSpelling() throws Exception {
    testPortugueseSpelling(ltMZ, ruleMZ);
  }

  @Test
  public void testBrazilianPortugueseSpelling() throws Exception {
    testPortugueseSpelling(ltBR, ruleBR);
    assertSingleError("ShintaroW.", ltBR, ruleBR, new String[]{});
    assertSingleError("SHINTAROW.", ltBR, ruleBR, new String[]{});
    assertNoErrors("Shintaro Wada", ltBR, ruleBR);
  }

  public void testPortugueseHyphenatedClitics(JLanguageTool lt, MorfologikPortugueseSpellerRule rule) throws Exception {
    assertNoErrors("diz-se", lt, rule);
    assertNoErrors("fá-lo-á", lt, rule);
    assertNoErrors("dir-lhe-ia", lt, rule);
    assertNoErrors("amar-nos-emos", lt, rule);
    assertNoErrors("dê-mo", lt, rule);
    assertNoErrors("fizemo-lo", lt, rule);
    assertNoErrors("compramo-lo", lt, rule);
    assertNoErrors("apercebemo-nos", lt, rule);
    assertNoErrors("referirmo-nos", lt, rule);
    assertNoErrors("amamo-las", lt, rule);
    assertNoErrors("mantínhamo-nos", lt, rule);
    assertNoErrors("qui-lo", lt, rule);
    assertNoErrors("fi-lo", lt, rule);
    assertNoErrors("fê-lo", lt, rule);
    assertNoErrors("trá-las", lt, rule);
    assertNoErrors("pu-las", lt, rule);
  }

  @Test
  public void testEuropeanPortugueseHyphenatedClitics() throws Exception {
    testPortugueseHyphenatedClitics(ltPT, rulePT);
  }

  @Test
  public void testBrazilianPortugueseHyphenatedClitics() throws Exception {
    testPortugueseHyphenatedClitics(ltBR, ruleBR);
  }

  @Test
  public void testPortugueseSpellerDoesNotAcceptVerbFormsWithElidedConsonants() throws Exception {
    // These will need to be accepted until the tokenisation is made to work with pt-BR better.
    // We will, for now, have an XML rule to correct these (id: ELISAO_VERBAL_DESNECESSARIA).
    // Once we rework the tokenisation logic, these will need to be single error assertions!
    assertNoErrors("amávamo", ltBR, ruleBR);
    assertNoErrors("fizemo", ltBR, ruleBR);
    assertNoErrors("compramo", ltBR, ruleBR);
    assertNoErrors("pusemo", ltBR, ruleBR);
    assertNoErrors("fazê", ltBR, ruleBR);
    assertNoErrors("fi", ltBR, ruleBR);  // 'fi-lo'
  }

  @Test
  public void testPortugueseHyphenationRules() throws Exception {
    assertNoErrors("Bolsa-Família", ltBR, ruleBR);
    assertNoErrors("ab-rogava", ltBR, ruleBR);
    // not symmetrical because 'anti' and 'republicanismo' are both valid words...
    // we need the compound rule active to catch this!
    assertNoErrors("antirrepublicanismo", ltPT, rulePT);
    assertSingleError("antirrepublicanismo", ltMZ, ruleMZ, new String[]{"anti-republicanismo"});
    assertNoErrors("anglo-saxônico", ltBR, ruleBR);
    assertNoErrors("paraquedista", ltBR, ruleBR);
    assertSingleError("para-quedista", ltBR, ruleBR, new String[]{"paraquedista"});
    assertNoErrors("sub-bairro", ltBR, ruleBR);
    assertNoErrors("hiper-revista", ltBR, ruleBR);
    assertNoErrors("pseudo-história", ltBR, ruleBR);
    assertNoErrors("semiacústico", ltBR, ruleBR);
    assertNoErrors("húngaro-americano", ltBR, ruleBR);
  }

  // FUCK YEAH WAHOO
  @Test
  public void testPortugueseSymmetricalDialectDifferences() throws Exception {
    // test that a simple lookup works
    assertTwoWayDialectError("anônimo", "anónimo");
    // test that we are able to leverage the synthesiser to expand the lookup (plurals are not on the list)
    assertTwoWayDialectError("caratês", "caratés");
    assertTwoWayDialectError("detectariam", "detetariam");
    // more simple lookups of various phenomena split along dialect lines
    assertTwoWayDialectError("tênis", "ténis");
    assertTwoWayDialectError("ônus", "ónus");
    // I swear I'm not being immature, there was some weirdness with "pêni"/"pênis" in pt-BR ;)
    assertTwoWayDialectError("pênis", "pénis");
    assertTwoWayDialectError("detecção", "deteção");
    assertTwoWayDialectError("dezesseis", "dezasseis");
    assertTwoWayDialectError("bidê", "bidé");
    assertTwoWayDialectError("detectar", "detetar");
    // new words from portal da língua portuguesa
    assertTwoWayDialectError("napoleônia", "napoleónia");
    assertTwoWayDialectError("hiperêmese", "hiperémese");
    // will not work due to tokenisation quirk, bebê-lo, must be fixed
    // assertTwoWayDialectError("bebê", "bebé");
  }

  @Test
  public void testPortugueseAsymmetricalDialectDifferences() throws Exception {
    // 'facto' is always invalid in pt-BR
    assertSingleExactError("facto", ltBR, ruleBR, "fato",
      "Possível erro de ortografia: esta é a grafia utilizada no português europeu.",
      "MORFOLOGIK_RULE_PT_BR_DIALECT");
    // 'fato' is valid in pt-PT, albeit with another meaning
    assertNoErrors("fato", ltPT, rulePT);
  }

  @Test
  public void testPortugueseSpellingAgreementVariation() throws Exception {
    // orthographic reforms
    assertTwoWayOrthographicAgreementError("detetar", "detectar");
    assertTwoWayOrthographicAgreementError("abjeção", "abjecção");
    assertTwoWayOrthographicAgreementError("direção", "direcção");
    assertTwoWayOrthographicAgreementError("diretamente", "directamente");
    assertTwoWayOrthographicAgreementError("afetada", "afectada");
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
    assertNoErrors("lan houses", ltBR, ruleBR);
    assertSingleError("Crohn", ltBR, ruleBR, new String[]{}); // this should prob. be okay tbh
    assertNoErrors("doença de Crohn", ltBR, ruleBR);
    // some of these should come from the global spelling file
    assertNoErrors("Hillary Clinton", ltBR, ruleBR);
    // these used to be in the disambiguator and have been moved to multiwords
    assertNoErrors("está en vogue", ltBR, ruleBR);
    assertNoErrors("startups de Silicon Valley", ltBR, ruleBR);
    assertNoErrors("comme de rigueur", ltBR, ruleBR);
    assertNoErrors("uma T shirt", ltBR, ruleBR); // we may need to have an XML rule for this
    // these are still done by a disambiguator rule
    assertNoErrors("mora na 82nd Street", ltBR, ruleBR);
    assertNoErrors("mora na Fifth Avenue", ltBR, ruleBR);
  }

  @Test
  public void testPortugueseSpellingSpellingTXT() throws Exception {
    assertNoErrors("physalis", ltBR, ruleBR);
    assertNoErrors("jackpot", ltPT, rulePT);
  }

  @Test
  public void testPortugueseSpellingDoesNotSuggestOffensiveWords() throws Exception {
    // some words should not be suggested; this test makes sure they are *not* in the returned suggestions for
    // each given incorrectly spelt word
    assertSingleErrorWithNegativeSuggestion("pwta", ltBR, ruleBR, "puta");
    assertSingleErrorWithNegativeSuggestion("bâbaca", ltBR, ruleBR, "babaca");
    assertSingleErrorWithNegativeSuggestion("redardado", ltBR, ruleBR, "retardado");
    assertSingleErrorWithNegativeSuggestion("cagguei", ltBR, ruleBR, "caguei");
    assertSingleErrorWithNegativeSuggestion("bucetas", ltBR, ruleBR, "bocetas");
    assertSingleErrorWithNegativeSuggestion("mongolóide", ltBR, ruleBR, "mongoloide");
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
    assertNoErrors("US$ 58,0 bilhões", ltBR, ruleBR);
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
  public void testPortugueseSpellerDoesNotCorrectOrdinalSuperscripts() throws Exception {
    assertNoErrors("6º", ltBR, ruleBR);  // superscript 'o'
    assertNoErrors("100°", ltBR, ruleBR);  // degree symbol
    assertNoErrors("21ª", ltBR, ruleBR);
  }

  @Test
  public void testPortugueseSpellerDoesNotCorrectDegreeExpressions() throws Exception {
    assertNoErrors("1,0°", ltBR, ruleBR);
    assertNoErrors("2°c", ltBR, ruleBR);
    assertNoErrors("3°C", ltBR, ruleBR);
    assertNoErrors("4,0ºc", ltBR, ruleBR);
    assertNoErrors("5.0ºc", ltBR, ruleBR);
    assertNoErrors("6,0ºRø", ltBR, ruleBR); // degrees Rømer
    assertNoErrors("7,5ºN", ltBR, ruleBR); // North
    assertNoErrors("−8,0°", ltBR, ruleBR); // negative
  }

  @Test
  public void testPortugueseSpellerDoesNotCorrectCopyrightSymbol() throws Exception {
    assertNoErrors("Copyright©", ltBR, ruleBR);
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
  public void testBrazilPortugueseSpellingCustomReplacements() throws Exception {
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
    assertSingleError("logo nao", ltBR, ruleBR, new String[]{"não"});
    assertSingleError("kitchenette", ltBR, ruleBR, new String[]{"quitinete"});
  }

  @Test
  public void testBrazilPortugueseGema23DFalseNegatives() throws Exception {
    assertSingleError("islam", ltBR, ruleBR, new String[]{"islã"});
    assertSingleError("es", ltBR, ruleBR, new String[]{"é"});
    assertSingleError("fomas", ltBR, ruleBR, new String[]{"formas"});
    assertSingleError("non", ltBR, ruleBR, new String[]{"não"});
//    assertSingleError("Puerto Rico", ltBR, ruleBR, new String[]{"Porto"}); 'Puerto' is a surname; we'll need XML here
    assertSingleError("Suissa", ltBR, ruleBR, new String[]{"Suíça"});
    assertSingleError("actividade", ltBR, ruleBR, new String[]{"atividade"});
  }

  @Test
  public void testPortugueseDiaeresis() throws Exception {
    assertSingleExactError("pingüim", ltBR, ruleBR, "pinguim",
      "No mais recente acordo ortográfico, não se usa mais o trema no português.",
      "MORFOLOGIK_RULE_PT_BR");
  }

  @Test
  public void testEuropeanPortugueseStyle1PLPastTenseCorrectedInBrazilian() throws Exception {
    assertSingleExactError("amámos", ltBR, ruleBR, "amamos",
      "No Brasil, o pretérito perfeito da primeira pessoa do plural escreve-se sem acento.",
      "MORFOLOGIK_RULE_PT_BR_DIALECT");
  }

  @Test
  public void testPortugueseSpellerIgnoresUppercaseAndDigitString() throws Exception {
    // Disambiguator rule!
    assertNoErrors("ABC2000", ltBR, ruleBR);
    assertNoErrors("AI5", ltBR, ruleBR);
    assertNoErrors("IP65", ltBR, ruleBR);
    assertNoErrors("HR2048", ltBR, ruleBR);
  }

  @Test
  public void testPortugueseSpellerIgnoresAmpersandBetweenTwoCapitals() throws Exception {
    // Disambiguator rule!
    assertNoErrors("J&F", ltBR, ruleBR);
    assertNoErrors("A&E", ltBR, ruleBR);
  }

  @Test
  public void testPortugueseSpellerIgnoresParentheticalInflection() throws Exception {
    // Disambiguator rule!
    assertNoErrors("professor(es)", ltBR, ruleBR);
    assertNoErrors("profissional(is)", ltBR, ruleBR);
  }

  @Test
  public void testPortugueseSpellerIgnoresProbableUnitsOfMeasurement() throws Exception {
    // Disambiguator rule; this is a style/typography issue to be taken care of in XML rules
    assertNoErrors("180g", ltBR, ruleBR);
    assertNoErrors("16.2kW", ltBR, ruleBR);
  }

  @Test
  public void testPortugueseSpellerIgnoresNonstandardTimeFormat() throws Exception {
    // Disambiguator rule; this is a style/typography issue to be taken care of in XML rules
    assertNoErrors("31h40min", ltBR, ruleBR);
    assertNoErrors("1h20min3s", ltBR, ruleBR);
    assertNoErrors("13:30h", ltBR, ruleBR);
  }

  @Test
  public void testPortugueseSpellerIgnoresLaughterOnomatopoeia() throws Exception {
    // Disambiguator rule
    assertNoErrors("hahahahaha", ltBR, ruleBR);
    assertNoErrors("heheh", ltBR, ruleBR);
    assertNoErrors("huehuehuehue", ltBR, ruleBR);
    assertNoErrors("Kkkkkkk", ltBR, ruleBR);
  }

  @Test
  public void testPortugueseSpellerRecognisesMonthAbbreviations() throws Exception {
    // Month abbreviations are subject to special rules; as such, they're not handled the same way as other
    // abbreviations. It's prob. better to have XML rules to deal with capitalisation, the absence of full stops, etc.
    assertNoErrors("23/jan", ltBR, ruleBR);
    assertNoErrors("9/Dez/2069", ltBR, ruleBR);  // we need to target the capitalisation here with an XML rule
    assertSingleError("10/feb/2010", ltBR, ruleBR, new String[]{"fev"});
  }

  @Test
  public void testPortugueseSpellerRecognisesRomanNumerals() throws Exception {
    // Disambiguator rule group "ROMAN_NUMBER"
    assertNoErrors("XVIII", ltBR, ruleBR);
    assertNoErrors("xviii", ltBR, ruleBR);
  }

  @Test
  public void testPortugueseSpellerIgnoresIsolatedGreekLetters() throws Exception {
    // Disambiguator rule
    assertNoErrors("ξ", ltBR, ruleBR);
    assertNoErrors("Ω", ltBR, ruleBR);
  }

  @Test
  public void testPortugueseSpellerIgnoresWordsFromIgnoreTXT() throws Exception {
    assertNoErrors("ignorewordoogaboogatest", ltBR, ruleBR);
    // make sure ignored words are *not* suggested
    assertSingleErrorWithNegativeSuggestion("ignorewordoogaboogatext", ltBR, ruleBR, "ignorewordoogaboogatest");
  }

  @Test public void testPortugueseSpellerDoesNotAcceptProhibitedWords() throws Exception {
    assertSingleError("prohibitwordoogaboogatest", ltBR, ruleBR, new String[] {});
  }

  @Test public void testPortugueseSpellerIgnoresNames() throws Exception {
    assertNoErrors("Fulgencio Fuhao", ltBR, ruleBR);
    // making sure the accents are okay
    assertSingleError("Jordao", ltBR, ruleBR, new String[] {"Jordão"});
  }

  @Test public void testPortugueseSpellerMultitokens() throws Exception {
    assertNoErrors("BRIGITTE BARDOT", ltBR, ruleBR);
    assertNoErrors("Brigitte Bardot", ltBR, ruleBR);
    assertNoErrors("MERCEDES-BENZ", ltBR, ruleBR);
    assertNoErrors("Mercedes-Benz", ltBR, ruleBR);
    assertNoErrors("big band", ltBR, ruleBR);
    assertNoErrors("Big band", ltBR, ruleBR);
    assertNoErrors("Big Band", ltBR, ruleBR);
    assertNoErrors("BIG BANDS", ltBR, ruleBR);

    // entry is "rhythm and blues"
    assertNoErrors("rhythm and blues", ltBR, ruleBR);  // same as file
    assertNoErrors("Rhythm and blues", ltBR, ruleBR);  // sentence-initial
    assertNoErrors("Rhythm And Blues", ltBR, ruleBR);  // title-case (naïve)
    assertNoErrors("Rhythm and Blues", ltBR, ruleBR);  // title-case (smart)
    // entry is "stock car"
    assertNoErrors("stock car", ltBR, ruleBR);
    assertNoErrors("Stock Car", ltBR, ruleBR);
    // entry is "Hall of Fame", so titlecase variants are not added
    assertNoErrors("Hall of Fame", ltBR, ruleBR);
    assertSingleError("Hall Of Fame", ltBR, ruleBR, new String[]{});
    assertErrorLength("hall of fame", 2, ltBR, ruleBR, new String[]{});

    assertNoErrors("Rock and Roll", ltBR, ruleBR);
    assertNoErrors("Hall of Fame", ltBR, ruleBR);
    assertNoErrors("Rock and Roll Hall of Fame", ltBR, ruleBR);
    assertSingleError("Rock And Roll Hall Of Fame", ltBR, ruleBR, new String[]{});  // bad titlecasing
    assertNoErrors("Chesapeake Bay retriever", ltBR, ruleBR);
    assertSingleError("Chesapeake Bay Retriever", ltBR, ruleBR, new String[]{});  // an annoying limitation
    assertNoErrors("Pit Bull", ltBR, ruleBR);
    assertNoErrors("Mao Tsé-Tung", ltBR, ruleBR);
    assertNoErrors("Honoris Causa", ltBR, ruleBR);
  }

  @Test public void testPortugueseSpellerEnglishCompounds() throws Exception {
    // disambiguator rule
    assertNoErrors("UntaggedWord Card", ltBR, ruleBR);  // unknown word
    assertNoErrors("Vaca Center", ltBR, ruleBR); // valid uppercase word
    assertNoErrors("de Klerk Center", ltBR, ruleBR);  // any proper noun, regardless of case
    assertSingleError("caramba Center", ltBR, ruleBR, new String[]{"Conter", "Centre"});  // not in context
  }

  @Test public void testPortugueseSpellerAcceptsArbitraryHyphenation() throws Exception {
    assertNoErrors("Xai-Xai", ltBR, ruleBR);
    assertNoErrors("Tsé-Tung", ltBR, ruleBR);
    assertNoErrors("X-Men", ltBR, ruleBR);
    assertNoErrors("t-shirts", ltBR, ruleBR);
    assertNoErrors("além-mar", ltBR, ruleBR);
    assertNoErrors("além-mares", ltBR, ruleBR);
    assertNoErrors("baby-doll", ltBR, ruleBR);
    assertNoErrors("baby-dolls", ltBR, ruleBR);
    assertNoErrors("e-zine", ltBR, ruleBR);
    assertNoErrors("e-zines", ltBR, ruleBR);
    assertNoErrors("CD-ROM", ltBR, ruleBR);
    assertNoErrors("CD-ROMs", ltBR, ruleBR);
    assertSingleError("heavy-metal", ltBR, ruleBR, new String[]{"heavy metal"});
    assertNoErrors("Aix-en-Provence", ltBR, ruleBR);
    assertNoErrors("Agualva-Cacém", ltPT, rulePT);
  }

  @Test public void testPortugueseSpellerAccepts50PercentOff() throws Exception {
    // Tokenising rule; we need to add a rule to add the space ourselves, but at least it doesn't suggest nonsense
    assertNoErrors("50%OFF", ltBR, ruleBR);
    assertSingleError("50%oogabooga", ltBR, ruleBR, new String[]{});
  }

  @Test public void testPortugueseSpellerAcceptsIllegalPrefixation() throws Exception {
    // These are all illegal, to be handled by an XML rule!
    assertNoErrors("semi-consciente", ltBR, ruleBR);
    assertNoErrors("semi-acústicas", ltBR, ruleBR);
    assertNoErrors("semi-frio", ltBR, ruleBR);
    assertNoErrors("sub-taça", ltBR, ruleBR);
    assertNoErrors("sub-pratos", ltBR, ruleBR);
  }

  @Test public void testPortugueseSpellerAcceptsCapitalisationOfAllCompoundElements() throws Exception {
    assertNoErrors("jiu-jitsu.", ltBR, ruleBR);
    assertNoErrors("Jiu-jitsu.", ltBR, ruleBR);
    assertNoErrors("Jiu-Jitsu.", ltBR, ruleBR);
    assertErrorLength("jIu-JItsU", 2, ltBR, ruleBR, new String[]{});
  }

  @Test public void testPortugueseSpellerAcceptsNationalPrefixes() throws Exception {
    // disambiguation rule for productive prefixes (the first element doesn't exist separately)
    // not in speller or tagger
    assertNoErrors("ítalo-congolês", ltBR, ruleBR);
    assertNoErrors("Belgo-Luxemburguesa", ltBR, ruleBR);
    // not in the speller, but in the tagger
    assertNoErrors("franco-prussiana", ltBR, ruleBR);
    assertNoErrors("Franco-prussiana", ltBR, ruleBR);
    assertNoErrors("Franco-Prussiana", ltBR, ruleBR);
    // speller logic (split by hyphen and check elements separately)
    // not a prefix per se, true compounding (the first element exists as an independent lexeme)
    assertNoErrors("húngaro-romeno", ltBR, ruleBR);
  }
}
