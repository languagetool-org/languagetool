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
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.UserConfig;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.GermanyGerman;
import org.languagetool.language.identifier.LanguageIdentifier;
import org.languagetool.language.identifier.LanguageIdentifierService;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.de.GermanSpellerRule;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MultiLanguageTest {

  private final static String fastTextBinary = "/home/stefan/Dokumente/languagetool/data/fasttext/fasttext";
  private final static String fastTextModel = "/home/stefan/Dokumente/languagetool/data/fasttext/lid.176.bin";
  private final static String ngramData = "/home/stefan/Dokumente/languagetool/data/model_ml50_new.zip";

  private static final GermanyGerman GERMAN_DE = (GermanyGerman) Languages.getLanguageForShortCode("de-DE");
  private static final AmericanEnglish ENGLISH_US = (AmericanEnglish) Languages.getLanguageForShortCode("en-US");

  private static List<String> ENGLISH_SENTENCES = Arrays.asList(
          "He is a very cool guy from Poland.",
          "How are you?",
          "But this is English.",
          "This is so cool.",
          "How are you my friend?",
          "Not sure if it's really",
          //"Nokia Takes Its Peers To Task.", TODO: could not detect this sentence for now
          "And I’m an English text!");

  private static List<String> GERMAN_SENTENCES = Arrays.asList(
          "Und er sagte, this is a good test."
  );

  @BeforeClass
  public static void setup() {
    LanguageIdentifierService.INSTANCE.getDefaultLanguageIdentifier(0, new File(ngramData), new File(fastTextBinary), new File(fastTextModel));
  }

  @Test
  @Ignore("Only run with full LanguageIdentifierService")
  public void testWithPreferredLanguagesDeAndEn() throws IOException {
    List<String> preferredLanguages = Arrays.asList("en","de");
    UserConfig userConfig = new UserConfig(Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(), 0, 0L, null, 0L, null, false, null, null, false, preferredLanguages);
    GermanSpellerRule germanSpellerRule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE, userConfig, null);
    JLanguageTool lt = new JLanguageTool(GERMAN_DE);
    
    //test short sentences
    int matchCounter = 0;
    for (String sentence : ENGLISH_SENTENCES) {
      RuleMatch[] matches = germanSpellerRule.match(lt.getAnalyzedSentence(sentence));
      for (RuleMatch match : matches) {
        //only matches in one of the preferred languages are accepted
        if (match.getErrorLimitLang() != null && match.getErrorLimitLang().equals("en")) {
          matchCounter++;
          break;
        }
      }
    }
    assertEquals("Not all foreign sentences detected", ENGLISH_SENTENCES.size(), matchCounter);
    matchCounter = 0;
    for (String sentence : GERMAN_SENTENCES) {
      RuleMatch[] matches = germanSpellerRule.match(lt.getAnalyzedSentence(sentence));
      for (RuleMatch match : matches) {
        //only matches in one of the preferred languages are accepted
        if (match.getErrorLimitLang() != null && match.getErrorLimitLang().equals("de")) {
          matchCounter++;
          break;
        }
      }
    }
    assertEquals("False positive detected languages", 0, matchCounter);
  }
}
