package org.languagetool.rules.en;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.RuleMatch;

import static org.junit.Assert.*;

public class SubjectVerbAgreementRuleTest {

  private final JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("en-US"));

  @Test
  public void testCorrectSentence() throws IOException {
    List<RuleMatch> matches1 = lt.check("She writes a letter.");
    assertEquals(0, matches1.size());    // No error, this is a correct sentence
    List<RuleMatch> matches2 = lt.check("He eats an apple.");
    assertEquals(0, matches2.size());    // No error, this is a correct sentence
  }

  @Test
  public void testSingularSubjectWithPluralVerb() throws IOException {
    List<RuleMatch> matches1 = lt.check("She write a letter.");
    assertEquals(1, matches1.size());    // One error, singular subject is mismatched with a plural verb
    List<RuleMatch> matches2 = lt.check("He eat an apple.");
    assertEquals(1, matches2.size());    // One error, singular subject is mismatched with a plural verb
  }

  @Test
  public void testPluralSubjectWithSingularVerb() throws IOException {
    List<RuleMatch> matches1 = lt.check("They writes a letter.");
    assertEquals(1, matches1.size());    // One error, plural subject is mismatched with a singular verb
    List<RuleMatch> matches2 = lt.check("They eats an apple.");
    assertEquals(1, matches2.size());    // One error, plural subject is mismatched with a singular verb
  }

  @Test
  public void testComplexSentenceStructure() throws IOException {
    List<RuleMatch> matches1 = lt.check("The boy, along with his friends, write a letter.");
    assertEquals(1, matches1.size());    // One error, singular subject is mismatched with a plural verb
    List<RuleMatch> matches2 = lt.check("The dog, along with its owner, are going for a walk.");
    assertEquals(1, matches2.size());    // One error, singular subject is mismatched with a plural verb
  }

  @Test
  public void testNoneAsSubject() throws IOException {
    List<RuleMatch> matches = lt.check("Three people were on the panel. None of them was women.");
    assertEquals(1, matches.size());    // One error, plural subject is mismatched with a singular verb
  }

}
