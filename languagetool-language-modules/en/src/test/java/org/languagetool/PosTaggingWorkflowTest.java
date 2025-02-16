/**
 * SWE 261P Software Testing Project By Kenny Chen, Haitong Yan, Jiacheng Zhuo
 */

package org.languagetool;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.languagetool.language.AmericanEnglish;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PosTaggingWorkflowTest {
  private static Language language;

  // initialize LanguageTool
  @BeforeClass
  public static void setUp() throws Exception {
    language = AmericanEnglish.getInstance();
  }

  @Test
  public void testNullTag() throws IOException {
    // empty space should get a "null" pos tag
    String sentence = " ";
    List<String> tokens = language.getWordTokenizer().tokenize(sentence);
    List<AnalyzedTokenReadings> aTokens = language.getTagger().tag(tokens);
    assertEquals(" /null", aTokens.get(0).getAnalyzedToken(0).toString());
  }

  @Test
  public void testNounTag() throws IOException {
    String sentence = "dog";
    List<String> tokens = language.getWordTokenizer().tokenize(sentence);
    List<AnalyzedTokenReadings> aTokens = language.getTagger().tag(tokens);
    AnalyzedTokenReadings token = aTokens.get(0);

    boolean isSuccessful = false;
    for (AnalyzedToken analyzedToken : token.getReadings()) {
      if ("NN".equals(analyzedToken.getPOSTag())) { // should be at least one referring to noun
        isSuccessful = true;
        break;
      }
    }
    assertTrue(isSuccessful);
  }

  @Test
  public void testVerbTag() throws IOException {
    String sentence = "fight";
    List<String> tokens = language.getWordTokenizer().tokenize(sentence);
    List<AnalyzedTokenReadings> aTokens = language.getTagger().tag(tokens);
    AnalyzedTokenReadings token = aTokens.get(0);

    boolean isSuccessful = false;
    for (AnalyzedToken analyzedToken : token.getReadings()) {
      if ("VB".equals(analyzedToken.getPOSTag())) { // should be at least one referring to verb
        isSuccessful = true;
        break;
      }
    }
    assertTrue(isSuccessful);
  }

  @Test
  public void testSingularVerbTag() throws IOException {
    String sentence = "is";
    List<String> tokens = language.getWordTokenizer().tokenize(sentence);
    List<AnalyzedTokenReadings> aTokens = language.getTagger().tag(tokens);
    AnalyzedTokenReadings token = aTokens.get(0);

    boolean isSuccessful = false;
    for (AnalyzedToken analyzedToken : token.getReadings()) {
      if ("VBZ".equals(analyzedToken.getPOSTag())) { // should be at least one referring to
                                                     // Third-Person Singular verb
        isSuccessful = true;
        break;
      }
    }
    assertTrue(isSuccessful);
  }

  @Test
  public void testAdjectiveTag() throws IOException {
    String sentence = "good";
    List<String> tokens = language.getWordTokenizer().tokenize(sentence);
    List<AnalyzedTokenReadings> aTokens = language.getTagger().tag(tokens);
    AnalyzedTokenReadings token = aTokens.get(0);

    boolean isSuccessful = false;
    for (AnalyzedToken analyzedToken : token.getReadings()) {
      if ("JJ".equals(analyzedToken.getPOSTag())) { // should be at least one referring to adjective
        isSuccessful = true;
        break;
      }
    }
    assertTrue(isSuccessful);
  }

  @Test
  public void testProperNounTag() throws IOException {
    String sentence = "France";
    List<String> tokens = language.getWordTokenizer().tokenize(sentence);
    List<AnalyzedTokenReadings> aTokens = language.getTagger().tag(tokens);
    AnalyzedTokenReadings token = aTokens.get(0);

    boolean isSuccessful = false;
    for (AnalyzedToken analyzedToken : token.getReadings()) {
      if ("NNP".equals(analyzedToken.getPOSTag())) { // should be considered a proper noun
        isSuccessful = true;
        break;
      }
    }
    assertTrue(isSuccessful);
  }

  @Test
  public void testPrepositionTag() throws IOException {
    String sentence = "between";
    List<String> tokens = language.getWordTokenizer().tokenize(sentence);
    List<AnalyzedTokenReadings> aTokens = language.getTagger().tag(tokens);
    AnalyzedTokenReadings token = aTokens.get(0);

    boolean isSuccessful = false;
    for (AnalyzedToken analyzedToken : token.getReadings()) {
      if ("IN".equals(analyzedToken.getPOSTag())) { // should be considered a preposition
        isSuccessful = true;
        break;
      }
    }
    assertTrue(isSuccessful);
  }
}
