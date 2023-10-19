/*
 * LanguageTool, a natural language style checker
 * Copyright (c) 2023.  Stefan Viol (https://stevio.de)
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  USA
 */

package org.languagetool.language.multiLanguage;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.*;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.GermanyGerman;
import org.languagetool.language.identifier.LanguageIdentifierService;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.Rule;
import org.languagetool.rules.de.GermanSpellerRule;
import org.languagetool.rules.en.MorfologikAmericanSpellerRule;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class MultiLanguageTest {

  private static final String fastTextBinary = "/home/stefan/Dokumente/languagetool/data/fasttext/fasttext";
  private static final String fastTextModel = "/home/stefan/Dokumente/languagetool/data/fasttext/lid.176.bin";
  private static final String ngramData = "/home/stefan/Dokumente/languagetool/data/model_ml50_new.zip";
  private static final GermanyGerman GERMAN_DE = (GermanyGerman) Languages.getLanguageForShortCode("de-DE");
  private static final AmericanEnglish ENGLISH_US = (AmericanEnglish) Languages.getLanguageForShortCode("en-US");
  private static final UserConfig userConfig = new UserConfig(Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(), 0, 0L, null, 0L, null, false, null, null, false, Arrays.asList("en", "de", "fr", "es", "pt", "nl"));
  private static JLanguageTool germanJLanguageTool;
  private static JLanguageTool germanJLanguageToolWPL;
  private static JLanguageTool englishJLanguageTool;


  private static MorfologikAmericanSpellerRule morfologikAmericanSpellerRule;
  private static GermanSpellerRule germanSpellerRule;

  @BeforeClass
  public static void setup() throws IOException {
    LanguageIdentifierService.INSTANCE.getDefaultLanguageIdentifier(0, new File(ngramData), new File(fastTextBinary), new File(fastTextModel));
    germanSpellerRule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    morfologikAmericanSpellerRule = new MorfologikAmericanSpellerRule(TestTools.getMessages("en"), ENGLISH_US);
    germanJLanguageTool = new JLanguageTool(GERMAN_DE, null, userConfig);
    germanJLanguageTool.disableRules(germanJLanguageTool.getAllRules().stream().map(Rule::getId).collect(Collectors.toList()));
    germanJLanguageTool.enableRule("GERMAN_SPELLER_RULE");
    germanJLanguageToolWPL = new JLanguageTool(GERMAN_DE);
    germanJLanguageToolWPL.disableRules(germanJLanguageTool.getAllRules().stream().map(Rule::getId).collect(Collectors.toList()));
    germanJLanguageToolWPL.enableRule("GERMAN_SPELLER_RULE");
    englishJLanguageTool = new JLanguageTool(ENGLISH_US, null, userConfig);
  }

  @Test
  @Ignore("Only run with full LanguageIdentifierService (fasttext and ngrams")
  public void multiLangHunspellRuleTest() throws IOException {
    String text = "Hallo Herr Müller, wie geht\n\n" + // 0 - 27
      "Das ist jetzt deutsch und wird auch die Hauptsprache sein.\n" + // 29 - 87
      "This Text is in english but the other one was in german.\n" + // 88 - 144
      "Hier wieder etwas deutsch.\n" + // 145 - 171
      "\n" +
      "La page que vous cherchez est introuvable\n" + // 173 - 214
      "\n" +
      "-- " +
      "Department of Electrical and Electronic Engineering\n" +
      "Office XY, Sackville Street Building, The University of Manchester, Manchester\n\n" + // 216 - 349
      "Wikipédia est un projet d’encyclopédie collective en ligne, universelle, multilingue et fonctionnant sur le principe du wiki." + // 351 -476
      "Anderson creció junto a su familia primero en el sur de Estados Unidos, luego se establecieron por un tiempo en Missuri y finalmente en Kansas hasta que se emancipó en 1862, manteniéndose mediante el robo y venta de caballos." + // 476 - 701
      "Wikipedia is een online encyclopedie die ernaar streeft informatie te bieden in alle erkende talen ter wereld, die vrij herbruikbaar, objectief en verifieerbaar is." + // 701 - 865
      "Ein schöner Satz." + // 865 - 882
      "But this is English." + // 882 - 902
      "Tom, could we meet next Monday\n\n" + // 902 - 932
      "Este novo website tem como objetivo fornecer toda a informação aos seus utilizadores, da forma mais clara e atualizada possível." + // 934 - 1062
      "La tour Eiffel est un symbole emblématique de la France.\n" + // 1062 - 1118
      "Die schöne Küste Spaniens ist ein beliebtes Reiseziel im Sommer.\n" + // 1119 - 1183
      "Las playas de España son famosas por su belleza natural.\n" + // 1184 - 1240
      "As praias de Portugal são conhecidas pela sua areia dourada.\n" + // 1241 - 1301
      "The Eiffel Tower is an iconic symbol of France.\n" + // 1302 - 1349
      "De prachtige kust van Spanje is een populaire bestemming in de zomer.\n" + // 1350 - 1419
      "De stranden van Nederland zijn ideaal voor lange wandelingen." + // 1420 - 1481
      "Die Seine schlängelt sich durch die belebten Straßen von Paris.\n" + // 1481 - 1544
      "La cuisine française est renommée pour sa délicieuse variété de fromages.\n" + // 1545 - 1618
      "Barcelona es famosa por su arquitectura modernista, incluyendo la Sagrada Familia.\n" + // 1619 - 1701
      "A cidade do Rio de Janeiro é conhecida por suas praias deslumbrantes.\n" + // 1702 - 1771
      "London is famous for its iconic red double-decker buses and black cabs.\n" + // 1772 - 1843
      "Amsterdam staat bekend om zijn schilderachtige grachten en fietsvriendelijke infrastructuur." + // 1844 - 1936
      "Die deutsche Autobahn ist weltweit berühmt für ihre Geschwindigkeitsbegrenzungen.\n" + // 1936 - 2017
      "La cuisine française est réputée pour sa délicatesse et sa diversité de saveurs.\n" + // 2018 - 2098
      "El tango argentino es una danza apasionada que refleja la cultura del país.\n" + // 2099 - 2174
      "A culinária portuguesa é conhecida por pratos como o bacalhau à Gomes de Sá.\n" + // 2175 - 2251
      "The Big Ben clock tower in London is an iconic symbol of the city.\n" + // 2252 - 2318
      "Les canaux d'Amsterdam offrent des balades pittoresques en bateau.\n" + // 2319 - 2385
      "El flamenco es un género musical y de baile tradicional en España.\n" + // 2386 - 2452
      "Os campos de tulipas na Holanda criam paisagens coloridas na primavera.\n" + // 2453 - 2524
      "La paella española es famosa por su mezcla de sabores y su colorido.\n" + // 2525 - 2593
      "Der Schwarzwald in Deutschland ist berühmt für seine dichten Wälder und Seen."; // 2594 - 2671
    AnnotatedText aText = new AnnotatedTextBuilder().addText(text).build();

    CheckResults checkResults = germanJLanguageTool.check2(aText, true, JLanguageTool.ParagraphHandling.NORMAL, null, JLanguageTool.Mode.ALL_BUT_TEXTLEVEL_ONLY, JLanguageTool.Level.DEFAULT, Collections.emptySet(), null);
    //run 2nd LT for benchmark (first check is always very slow)
    germanJLanguageToolWPL.check2(aText, true, JLanguageTool.ParagraphHandling.NORMAL, null, JLanguageTool.Mode.ALL_BUT_TEXTLEVEL_ONLY, JLanguageTool.Level.DEFAULT, Collections.emptySet(), null);


    List<ExtendedSentenceRange> extendedSentenceRanges = checkResults.getExtendedSentenceRanges();
    System.out.println(extendedSentenceRanges);
    assertNotNull(extendedSentenceRanges);
    assertFalse(extendedSentenceRanges.isEmpty());
    assertEquals(36, extendedSentenceRanges.size());

    testRangeAndLanguage(0, 27, "de", extendedSentenceRanges.get(0));
    testRangeAndLanguage(29, 87, "de", extendedSentenceRanges.get(1));
    testRangeAndLanguage(88, 144, "en", extendedSentenceRanges.get(2));
    testRangeAndLanguage(145, 171, "de", extendedSentenceRanges.get(3));
    testRangeAndLanguage(173, 214, "fr", extendedSentenceRanges.get(4));
    testRangeAndLanguage(216, 349, "de", extendedSentenceRanges.get(5)); //will not detect as non-German sentence
    testRangeAndLanguage(351, 476, "fr", extendedSentenceRanges.get(6));
    testRangeAndLanguage(476, 701, "es", extendedSentenceRanges.get(7));
    testRangeAndLanguage(701, 865, "nl", extendedSentenceRanges.get(8));
    testRangeAndLanguage(865, 882, "de", extendedSentenceRanges.get(9));
    testRangeAndLanguage(882, 902, "en", extendedSentenceRanges.get(10));
    testRangeAndLanguage(902, 932, "en", extendedSentenceRanges.get(11));
    testRangeAndLanguage(934, 1062, "pt", extendedSentenceRanges.get(12));
    benchmarkMultiLang();

  }

  @Test
  @Ignore("Only run with full LanguageIdentifierService (fasttext and ngrams")
  public void multiLangMorfologikRuleTest() {
//    String
  }

  private void testRangeAndLanguage(int expectedStart, int expectedEnd, String lang, ExtendedSentenceRange sentence) {
    assertEquals(expectedStart, sentence.getFromPos());
    assertEquals(expectedEnd, sentence.getToPos());
    assertEquals(lang, getLanguageWithHighestConfidenceRate(sentence.getLanguageConfidenceRates()));
  }

  private String getLanguageWithHighestConfidenceRate(Map<String, Float> languages) {
    final float[] top = {-1f};
    final String[] lang = {""};
    languages.forEach((l, r) -> {
      if (r > top[0]) {
        top[0] = r;
        lang[0] = l;
      }
    });
    return lang[0];
  }

  private void benchmarkMultiLang() throws IOException {
    String text = "Die romantische Stadt Paris ist für ihre Eiffelturm und köstliche Küche berühmt.\n" +
      "La romántica ciudad de París es famosa por su Torre Eiffel y su deliciosa cocina.\n" +
      "The romantic city of Paris is famous for its Eiffel Tower and delicious cuisine.\n" +
      "L'Espagne attire avec de magnifiques plages sur la côte méditerranéenne.\n" +
      "A Espanha atrai com belas praias na costa mediterrânea.\n" +
      "Spain entices with beautiful beaches on the Mediterranean coast.\n" +
      "Die Niederlande sind für ihre Tulpenfelder und charmanten Windmühlen bekannt.\n" +
      "Les Pays-Bas sont connus pour leurs champs de tulipes et leurs charmants moulins à vent.\n" +
      "The Netherlands is known for its tulip fields and charming windmills.\n" +
      "Nederland staat bekend om zijn tulpenvelden en charmante windmolens." +
      "Berlin ist die Hauptstadt Deutschlands und hat eine faszinierende Geschichte.\n" +
      "La gastronomie française est réputée pour ses délicieuses pâtisseries.\n" +
      "Madrid es conocida por su animada vida nocturna y deliciosa comida tapas.\n" +
      "O carnaval do Brasil é uma das festas mais animadas do mundo.\n" +
      "The British countryside is famous for its rolling hills and quaint villages.\n" +
      "Brugge is beroemd om zijn goed bewaarde middeleeuwse architectuur en kanalen." +
      "München ist berühmt für sein Oktoberfest, das größte Bierfest der Welt.\n" +
      "La Tour Eiffel scintille de mille lumières lors de la nuit à Paris.\n" +
      "En Barcelona, la playa y la arquitectura gótica son impresionantes.\n" +
      "O futebol é paixão nacional no Brasil, com Pelé sendo uma lenda do esporte.\n" +
      "London's museums, such as the British Museum and the National Gallery, are world-renowned.\n" +
      "Les canaux d'Amsterdam sont bordés de maisons étroites à pignons.\n" +
      "El tango argentino es conocido por su pasión y elegancia en el baile.\n" +
      "Die Alpen erstrecken sich über mehrere europäische Länder und bieten großartige Skimöglichkeiten.\n" +
      "La paella es un plato tradicional español que combina arroz, mariscos y azafrán.\n" +
      "Os moinhos de vento na Holanda são um marco icônico da paisagem rural.";
    AnnotatedText aText = new AnnotatedTextBuilder().addText(text).build();
    long startCheckWithAdditionalDetection = System.currentTimeMillis();
    CheckResults withMulti = germanJLanguageTool.check2(aText, true, JLanguageTool.ParagraphHandling.NORMAL, null, JLanguageTool.Mode.ALL_BUT_TEXTLEVEL_ONLY, JLanguageTool.Level.DEFAULT, Collections.emptySet(), null);
    long endCheckWithAdditionalDetection = System.currentTimeMillis();
    System.out.println("Check time with multi language: " + (endCheckWithAdditionalDetection - startCheckWithAdditionalDetection));
    long startCheckWithoutAdditionalDetection = System.currentTimeMillis();
    CheckResults withoutMulti = germanJLanguageToolWPL.check2(aText, true, JLanguageTool.ParagraphHandling.NORMAL, null, JLanguageTool.Mode.ALL_BUT_TEXTLEVEL_ONLY, JLanguageTool.Level.DEFAULT, Collections.emptySet(), null);
    long endCheckWithoutAdditionalDetection = System.currentTimeMillis();
    System.out.println("Check time without multi language: " + (endCheckWithoutAdditionalDetection - startCheckWithoutAdditionalDetection));

    Set<String> detectedLanguages = withMulti.getExtendedSentenceRanges().stream().map(extendedSentenceRange -> getLanguageWithHighestConfidenceRate(extendedSentenceRange.getLanguageConfidenceRates())).collect(Collectors.toSet());
    assertEquals(6, detectedLanguages.size());

    withoutMulti.getExtendedSentenceRanges().forEach(extendedSentenceRange -> {
      assertEquals("de", getLanguageWithHighestConfidenceRate(extendedSentenceRange.getLanguageConfidenceRates()));
    });
  }
}
