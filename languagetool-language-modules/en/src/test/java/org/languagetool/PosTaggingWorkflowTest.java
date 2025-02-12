/**
 * SWE 261P Software Testing Project
 * By Kenny Chen, Haitong Yan, Jiacheng Zhuo
 */

package org.languagetool;

import org.junit.Test;
import org.languagetool.language.AmericanEnglish;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PosTaggingWorkflowTest {
  // List<AnalyzedTokenReadings> aTokens = language.getTagger().tag(tokens);
  // initialize language tool
  private final Language language = new AmericanEnglish();

  @Test
  public void testTagging01() throws IOException {
    // empty space should get a "null" pos tag
    String sentence = " ";
    List<String> tokens = language.getWordTokenizer().tokenize(sentence);
    List<AnalyzedTokenReadings> aTokens = language.getTagger().tag(tokens);
    assertEquals(" /null", aTokens.get(0).getAnalyzedToken(0).toString());
  }

  @Test
  public void testTagging02() throws IOException {
    String sentence = "dog";
    List<String> tokens = language.getWordTokenizer().tokenize(sentence);
    List<AnalyzedTokenReadings> aTokens = language.getTagger().tag(tokens);
    AnalyzedTokenReadings token = aTokens.get(0);

    boolean isSuccessful = false;
    for (AnalyzedToken analyzedToken : token.getReadings()) {
      if("NN".equals(analyzedToken.getPOSTag())) { // should be at least one referring to noun
        isSuccessful = true;
        break;
      }
    }
    assertTrue(isSuccessful);
  }

  @Test
  public void testTagging03() throws IOException {
    String sentence = "fight";
    List<String> tokens = language.getWordTokenizer().tokenize(sentence);
    List<AnalyzedTokenReadings> aTokens = language.getTagger().tag(tokens);
    AnalyzedTokenReadings token = aTokens.get(0);

    boolean isSuccessful = false;
    for (AnalyzedToken analyzedToken : token.getReadings()) {
      if("VB".equals(analyzedToken.getPOSTag())) { // should be at least one referring to verb
        isSuccessful = true;
        break;
      }
    }
    assertTrue(isSuccessful);
  }

  @Test
  public void testTagging04() throws IOException {
    String sentence = "is";
    List<String> tokens = language.getWordTokenizer().tokenize(sentence);
    List<AnalyzedTokenReadings> aTokens = language.getTagger().tag(tokens);
    AnalyzedTokenReadings token = aTokens.get(0);

    boolean isSuccessful = false;
    for (AnalyzedToken analyzedToken : token.getReadings()) {
      if("VBZ".equals(analyzedToken.getPOSTag())) { // should be at least one referring to Third-Person Singular verb
        isSuccessful = true;
        break;
      }
    }
    assertTrue(isSuccessful);
  }

  @Test
  public void testTagging05() throws IOException {
    String sentence = "good";
    List<String> tokens = language.getWordTokenizer().tokenize(sentence);
    List<AnalyzedTokenReadings> aTokens = language.getTagger().tag(tokens);
    AnalyzedTokenReadings token = aTokens.get(0);

    boolean isSuccessful = false;
    for (AnalyzedToken analyzedToken : token.getReadings()) {
      if("JJ".equals(analyzedToken.getPOSTag())) { // should be at least one referring to adjective
        isSuccessful = true;
        break;
      }
    }
    assertTrue(isSuccessful);
  }

  @Test
  public void testTagging06() throws IOException {
    String sentence = "France";
    List<String> tokens = language.getWordTokenizer().tokenize(sentence);
    List<AnalyzedTokenReadings> aTokens = language.getTagger().tag(tokens);
    AnalyzedTokenReadings token = aTokens.get(0);

    boolean isSuccessful = false;
    for (AnalyzedToken analyzedToken : token.getReadings()) {
      if("NNP".equals(analyzedToken.getPOSTag())) { // should be considered a proper noun
        isSuccessful = true;
        break;
      }
    }
    assertTrue(isSuccessful);
  }

  @Test
  public void testTagging07() throws IOException {
    String sentence = "between";
    List<String> tokens = language.getWordTokenizer().tokenize(sentence);
    List<AnalyzedTokenReadings> aTokens = language.getTagger().tag(tokens);
    AnalyzedTokenReadings token = aTokens.get(0);

    boolean isSuccessful = false;
    for (AnalyzedToken analyzedToken : token.getReadings()) {
      System.out.println(analyzedToken.getPOSTag());
      if("IN".equals(analyzedToken.getPOSTag())) { // should be considered a preposition
        isSuccessful = true;
        break;
      }
    }
    assertTrue(isSuccessful);
  }
}
