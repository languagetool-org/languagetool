/*
 * LanguageTool, a natural language style checker
 * Copyright (c) 2022.  Stefan Viol (https://stevio.de)
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

package org.languagetool.rules;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.GermanyGerman;
import org.languagetool.language.identifier.LanguageIdentifierService;
import org.languagetool.rules.de.GermanSpellerRule;
import org.languagetool.rules.en.MorfologikAmericanSpellerRule;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class MultiLanguageTextTest {

  private static final GermanyGerman GERMAN_DE = (GermanyGerman) Languages.getLanguageForShortCode("de-DE");
  private static final AmericanEnglish ENGLISH_US = (AmericanEnglish) Languages.getLanguageForShortCode("en-US");
  
  private static MorfologikAmericanSpellerRule morfologikAmericanSpellerRule;
  private static GermanSpellerRule germanSpellerRule;
  @BeforeClass
  public static void setup() throws IOException {
    germanSpellerRule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    morfologikAmericanSpellerRule = new MorfologikAmericanSpellerRule(TestTools.getMessages("en"), ENGLISH_US);
  }
  
  @Test
  @Ignore //TODO: need rework: works only with preferred languages in userConfig
  public void testEnglishInGermanDetected() throws IOException {
    JLanguageTool lt = new JLanguageTool(GERMAN_DE);
    RuleMatch[] matches1 = germanSpellerRule.match(lt.getAnalyzedSentence("He is a very cool guy from Poland."));
    boolean match1Found = false;
    for (RuleMatch match : matches1) {
      if (match.getErrorLimitLang() != null && match.getErrorLimitLang().equals("en")) {
        match1Found = true;
        break;
      }
    }
    assertTrue("It was expected to find a match.", match1Found);

    RuleMatch[] matches2 = germanSpellerRule.match(lt.getAnalyzedSentence("How are you?"));
    boolean match2Found = false;
    for (RuleMatch match : matches2) {
      if (match.getErrorLimitLang() != null && match.getErrorLimitLang().equals("en")) {
        match2Found = true;
        break;
      }
    }
    assertTrue("It was expected to find a match.", match2Found);

    RuleMatch[] matches3 = germanSpellerRule.match(lt.getAnalyzedSentence("CONFIDENTIALITY NOTICE:"));
    boolean match3Found = false;
    for (RuleMatch match : matches3) {
      if (match.getErrorLimitLang() != null && match.getErrorLimitLang().equals("en")) {
        match3Found = true;
        break;
      }
    }
    assertTrue("It was expected to find a match.", match3Found);
  }

  @Test
  @Ignore //TODO: need rework: works only with preferred languages in userConfig 
  public void testWithLanguageIdentifier() throws IOException {
    LanguageIdentifierService.INSTANCE.getDefaultLanguageIdentifier(1000, new File("/home/stefan/Dokumente/languagetool/data/model_ml50_new.zip"), new File("/home/stefan/Dokumente/languagetool/data/fasttext/fasttext"), new File("/home/stefan/Dokumente/languagetool/data/fasttext/lid.176.bin"));
    JLanguageTool lt = new JLanguageTool(GERMAN_DE);

    RuleMatch[] matchesFr = germanSpellerRule.match(lt.getAnalyzedSentence("Wikipédia est un projet d’encyclopédie collective en ligne, universelle, multilingue et fonctionnant sur le principe du wiki."));
    RuleMatch lastMatchFr = matchesFr[matchesFr.length - 1];
    assertEquals(lastMatchFr.getErrorLimitLang(), "fr");

    RuleMatch[] matchesEs = germanSpellerRule.match(lt.getAnalyzedSentence("Anderson creció junto a su familia primero en el sur de Estados Unidos, luego se establecieron por un tiempo en Missuri y finalmente en Kansas hasta que se emancipó en 1862, manteniéndose mediante el robo y venta de caballos."));
    RuleMatch lastMatchEs = matchesEs[matchesEs.length - 1];
    assertEquals(lastMatchEs.getErrorLimitLang(), "es");

    RuleMatch[] matchesNl = germanSpellerRule.match(lt.getAnalyzedSentence("Wikipedia is een online encyclopedie die ernaar streeft informatie te bieden in alle erkende talen ter wereld, die vrij herbruikbaar, objectief en verifieerbaar is."));
    RuleMatch lastMatchNl = matchesNl[matchesNl.length - 1];
    assertEquals(lastMatchNl.getErrorLimitLang(), "nl");
    
    LanguageIdentifierService.INSTANCE.clearLanguageIdentifier("both"); //clear for next test
  }
  
  @Test
  @Ignore("Moved from MorfologikAmericanSpellerRuleTest and run with langIdentifier")
  // case: signature is (mostly) English, user starts typing in German -> first, EN is detected for whole text
  public void testMultilingualSignatureCase() throws IOException {
    LanguageIdentifierService.INSTANCE.getDefaultLanguageIdentifier(1000, new File("/home/stefan/Dokumente/languagetool/data/model_ml50_new.zip"), new File("/home/stefan/Dokumente/languagetool/data/fasttext/fasttext"), new File("/home/stefan/Dokumente/languagetool/data/fasttext/lid.176.bin"));
    JLanguageTool lt = new JLanguageTool(ENGLISH_US);
    String sig = "-- " +
                 "Department of Electrical and Electronic Engineering\n" +
                 "Office XY, Sackville Street Building, The University of Manchester, Manchester\n";
    assertZZ("Hallo Herr Müller, wie geht\n\n" + sig, lt);  // "Herr" and "Müller" are accepted by EN speller
    assertZZ("Hallo Frau Müller, wie\n\n" + sig, lt);  // "Frau" and "Müller" are accepted by EN speller
    assertZZ("Hallo Frau Sauer, wie\n\n" + sig, lt);
    LanguageIdentifierService.INSTANCE.clearLanguageIdentifier("both"); //clear for next test
  }
  
  private void assertZZ(String input, JLanguageTool lt) throws IOException {
    List<AnalyzedSentence> analyzedSentences = lt.analyzeText(input);
    assertThat(analyzedSentences.size(), is(2));
    assertThat(morfologikAmericanSpellerRule.match(analyzedSentences.get(0))[0].getErrorLimitLang(), is("de"));
    assertNull(morfologikAmericanSpellerRule.match(analyzedSentences.get(1))[0].getErrorLimitLang());
  }
}
