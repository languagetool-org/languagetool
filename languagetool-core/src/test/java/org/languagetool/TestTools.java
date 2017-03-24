/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.WordData;

import org.languagetool.language.Demo;
import org.languagetool.rules.Rule;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;

import static org.junit.Assert.assertEquals;

public final class TestTools {

  private static final Language DEMO_LANGUAGE = new Demo();
  
  private TestTools() {
  }

  public static Language getDemoLanguage() {
    return DEMO_LANGUAGE;
  }

  public static Set<Language> getLanguagesExcept(String[] langCodes) {
    Set<Language> languages = new HashSet<>();
    languages.addAll(Languages.getWithDemoLanguage());
    if (langCodes != null) {
      for (String langCode : langCodes) {
        Language lang = Languages.getLanguageForShortCode(langCode);
        languages.remove(lang);
      }
    }
    return languages;
  }

  public static ResourceBundle getEnglishMessages() {
    return getMessages("en");
  }

  /**
   * Gets the resource bundle for the specified language.
   * @param languageCode lowercase two-letter ISO-639 code.
   * @return the resource bundle for the specified language.
   */
  public static ResourceBundle getMessages(String languageCode) {
    if (languageCode.length() > 3) {
      throw new RuntimeException("Use a character code (ISO-639 code), not a full language name: " + languageCode);
    }
    ResourceBundle messages = ResourceBundle.getBundle(
            JLanguageTool.MESSAGE_BUNDLE, new Locale(languageCode));
    return messages;
  }

  public static void testSplit(String[] sentences, SentenceTokenizer sTokenizer) {
    StringBuilder inputString = new StringBuilder();
    List<String> input = new ArrayList<>();
    Collections.addAll(input, sentences);
    for (String s : input) {
      inputString.append(s);
    }
    assertEquals(input, sTokenizer.tokenize(inputString.toString()));
  }

  public static void myAssert(String input, String expected,
      Tokenizer tokenizer, Tagger tagger) throws IOException {
    List<String> tokens = tokenizer.tokenize(input);
    List<String> noWhitespaceTokens = getNoWhitespaceTokens(tokens);
    List<AnalyzedTokenReadings> output = tagger.tag(noWhitespaceTokens);
    StringBuilder outputStr = new StringBuilder();
    for (Iterator<AnalyzedTokenReadings> iter = output.iterator(); iter.hasNext();) {
      AnalyzedTokenReadings tokenReadings = iter.next();
      List<String> readings = getAsStrings(tokenReadings);
      outputStr.append(String.join("|", readings));
      if (iter.hasNext()) {
        outputStr.append(" -- ");
      }
    }
    assertEquals(expected, outputStr.toString());
  }

  public static void myAssert(String input, String expected,
      Tokenizer tokenizer, SentenceTokenizer sentenceTokenizer,
      Tagger tagger, Disambiguator disambiguator)
      throws IOException {
    StringBuilder outputStr = new StringBuilder();
    List<String> sentences = sentenceTokenizer.tokenize(input);
    for (String sentence : sentences) {
      List<String> tokens = tokenizer.tokenize(sentence);
      List<String> noWhitespaceTokens = getNoWhitespaceTokens(tokens);
      List<AnalyzedTokenReadings> aTokens = tagger.tag(noWhitespaceTokens);
      AnalyzedTokenReadings[] tokenArray = new AnalyzedTokenReadings[tokens.size() + 1];
      AnalyzedToken[] startTokenArray = new AnalyzedToken[1];
      int toArrayCount = 0;
      AnalyzedToken sentenceStartToken = new AnalyzedToken("", JLanguageTool.SENTENCE_START_TAGNAME, null);
      startTokenArray[0] = sentenceStartToken;
      tokenArray[toArrayCount++] = new AnalyzedTokenReadings(startTokenArray, 0);
      int startPos = 0;
      int noWhitespaceCount = 0;
      for (String tokenStr : tokens) {
        AnalyzedTokenReadings posTag;
        if (isWord(tokenStr)) {
          posTag = aTokens.get(noWhitespaceCount);
          posTag.setStartPos(startPos);
          noWhitespaceCount++;
        } else {
          posTag = tagger.createNullToken(tokenStr, startPos);
        }
        tokenArray[toArrayCount++] = posTag;
        startPos += tokenStr.length();
      }

      AnalyzedSentence finalSentence = new AnalyzedSentence(tokenArray);
      finalSentence = disambiguator.disambiguate(finalSentence);

      AnalyzedTokenReadings[] output = finalSentence.getTokens();

      for (int i = 0; i < output.length; i++) {
        AnalyzedTokenReadings tokenReadings = output[i];
        List<String> readings = getAsStrings(tokenReadings);
        outputStr.append(String.join("|", readings));
        if (i < output.length - 1) {
          outputStr.append(' ');
        }
      }
    }
    assertEquals(expected, outputStr.toString());
  }

  public static boolean isWord(String token) {
    for (int i = 0; i < token.length(); i++) {
      char c = token.charAt(i);
      if (Character.isLetter(c) || Character.isDigit(c)) {
        return true;
      }
    }
    return false;
  }

  public static void testDictionary(BaseTagger tagger, Language language) throws IOException {
    Dictionary dictionary = Dictionary.read(JLanguageTool.getDataBroker().getFromResourceDirAsUrl(tagger.getDictionaryPath()));
    DictionaryLookup lookup = new DictionaryLookup(dictionary);
    for (WordData wordData : lookup) {
      if (wordData.getTag() == null || wordData.getTag().length() == 0) {
        System.err.println("**** Warning: " + language + ": the word " + wordData.getWord() + "/" + wordData.getStem() + " lacks a POS tag in the dictionary.");
      }
    }
  }

  private static List<String> getAsStrings(AnalyzedTokenReadings tokenReadings) {
    List<String> readings = new ArrayList<>();
    for (AnalyzedToken analyzedToken : tokenReadings) {
      readings.add(getAsString(analyzedToken));
    }
    // force some order on the result just for the test case - order may vary
    // from one version of the lexicon to the next:
    Collections.sort(readings);
    return readings;
  }

  private static String getAsString(AnalyzedToken analyzedToken) {
    return analyzedToken.getToken() + "/[" + analyzedToken.getLemma() + ']' + analyzedToken.getPOSTag();
  }

  private static List<String> getNoWhitespaceTokens(List<String> tokens) {
    List<String> noWhitespaceTokens = new ArrayList<>();
    // whitespace confuses tagger, so give it the tokens but no whitespace tokens:
    for (String token : tokens) {
      if (isWord(token)) {
        noWhitespaceTokens.add(token);
      }
    }
    return noWhitespaceTokens;
  }

  public static void disableAllRulesExcept(JLanguageTool lt, String ruleIdToActivate) {
    for (Rule rule : lt.getAllRules()) {
      lt.disableRule(rule.getId());
    }
    lt.enableRule(ruleIdToActivate);
  }

}
