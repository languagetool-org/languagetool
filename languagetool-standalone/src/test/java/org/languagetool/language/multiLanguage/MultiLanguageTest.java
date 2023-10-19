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
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.de.GermanSpellerRule;
import org.languagetool.rules.en.MorfologikAmericanSpellerRule;
import org.languagetool.rules.patterns.PatternRule;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
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
  private static JLanguageTool englishJLanguageTool;

  private static MorfologikAmericanSpellerRule morfologikAmericanSpellerRule;
  private static GermanSpellerRule germanSpellerRule;

  private static List<String> ENGLISH_SENTENCES = Arrays.asList(
    "He is a very cool guy from Poland.",
    "How are you?",
    "But this is English.",
    "This is so cool.",
    "How are you my friend?",
    "Not sure if it's really",
    "And I’m an English text!");

  private static List<String> GERMAN_SENTENCES = Arrays.asList(
    "Und er sagte, this is a good test."
  );

  @BeforeClass
  public static void setup() throws IOException {
    LanguageIdentifierService.INSTANCE.getDefaultLanguageIdentifier(0, new File(ngramData), new File(fastTextBinary), new File(fastTextModel));
    germanSpellerRule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    morfologikAmericanSpellerRule = new MorfologikAmericanSpellerRule(TestTools.getMessages("en"), ENGLISH_US);
    germanJLanguageTool = new JLanguageTool(GERMAN_DE, null, userConfig);
    germanJLanguageTool.disableRules(germanJLanguageTool.getAllRules().stream().map(Rule::getId).collect(Collectors.toList()));
    germanJLanguageTool.enableRule("GERMAN_SPELLER_RULE");
    englishJLanguageTool = new JLanguageTool(ENGLISH_US, null, userConfig);
  }

  @Test
  @Ignore("Only run with full LanguageIdentifierService (fasttext and ngrams")
  public void MultiLangHunspellRuleTest() throws IOException {
    String text = "Hallo Herr Müller, wie geht\n\n" + // 0 - 27 de-DE
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
      "Tom, could we meet next Monday\n\n"; // 902 - 932
    AnnotatedText aText = new AnnotatedTextBuilder().addText(text).build();
    CheckResults checkResults = germanJLanguageTool.check2(aText, true, JLanguageTool.ParagraphHandling.NORMAL, null, JLanguageTool.Mode.ALL_BUT_TEXTLEVEL_ONLY, JLanguageTool.Level.DEFAULT, Collections.emptySet(), null);
    List<ExtendedSentenceRange> extendedSentenceRanges = checkResults.getExtendedSentenceRanges();
    System.out.println(extendedSentenceRanges);
    assertNotNull(extendedSentenceRanges);
    assertFalse(extendedSentenceRanges.isEmpty());
    assertEquals(12, extendedSentenceRanges.size());

    ExtendedSentenceRange sentence1 = extendedSentenceRanges.get(0);
    assertEquals(0, sentence1.getFromPos());
    assertEquals(27, sentence1.getToPos());


    extendedSentenceRanges.forEach(range -> {
      System.out.println("Satz: " + text.substring(range.getFromPos(), range.getToPos()));
    });
  }

  @Test
  @Ignore("Only run with full LanguageIdentifierService (fasttext and ngrams")
  public void MultiLangMorfologikRuleTest() {
//    String
  }


  @Test
  @Ignore("Only run with full LanguageIdentifierService")
  public void testWithPreferredLanguagesDeAndEn() throws IOException {
    List<String> preferredLanguages = Arrays.asList("en", "de");
    UserConfig userConfig = new UserConfig(Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(), 0, 0L, null, 0L, null, false, null, null, false, preferredLanguages);
    GermanSpellerRule germanSpellerRule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE, userConfig, null);
    JLanguageTool lt = new JLanguageTool(GERMAN_DE);

    //test short sentences
    int matchCounter = 0;
    for (String sentence : ENGLISH_SENTENCES) {
      RuleMatch[] matches = germanSpellerRule.match(lt.getAnalyzedSentence(sentence));
      for (RuleMatch match : matches) {
        //only matches in one of the preferred languages are accepted
//        if (match.getErrorLimitLang() != null && match.getErrorLimitLang().equals("en")) {
//          matchCounter++;
//          break;
//        }
      }
    }
    assertEquals("Not all foreign sentences detected", ENGLISH_SENTENCES.size(), matchCounter);
    matchCounter = 0;
    for (String sentence : GERMAN_SENTENCES) {
      RuleMatch[] matches = germanSpellerRule.match(lt.getAnalyzedSentence(sentence));
      for (RuleMatch match : matches) {
        //only matches in one of the preferred languages are accepted
//        if (match.getErrorLimitLang() != null && match.getErrorLimitLang().equals("de")) {
//          matchCounter++;
//          break;
//        }
      }
    }
    assertEquals("False positive detected languages", 0, matchCounter);
  }
}
