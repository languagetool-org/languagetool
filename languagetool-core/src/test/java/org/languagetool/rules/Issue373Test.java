package org.languagetool.rules;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Demo;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.rules.patterns.PatternRuleLoader;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;


@Ignore
public class Issue373Test {

  private static final String GRAMMAR_FILE_NAME = "/xx/grammar.xml";
  private static final String ISSUE_373_TEST_SUGGESTION_INSIDE = "ISSUE_373_TEST_SUGGESTION_INSIDE";
  private static final String ISSUE_373_TEST_SUGGESTION_OUTSIDE = "ISSUE_373_TEST_SUGGESTION_OUTSIDE";
  private static final String ISSUE_373_TEST_NO_ISSUE_1 = "ISSUE_373_TEST_NO_ISSUE_1";
  private static final String ISSUE_373_TEST_NO_ISSUE_2 = "ISSUE_373_TEST_NO_ISSUE_2";
  private static List<AbstractPatternRule> RULES;
  private JLanguageTool langTool;

  @Before
  public void setUp() throws Exception {
    langTool = new JLanguageTool(new Demo());
    PatternRuleLoader prg = new PatternRuleLoader();
    RULES = prg.getRules(JLanguageTool.getDataBroker().getFromRulesDirAsStream(GRAMMAR_FILE_NAME), GRAMMAR_FILE_NAME);
  }

  @Test
  public void testIssueSuggestionInside() throws Exception {
    String wrongString = "Xnel vola nén Xnel vola nén";
    String correctString = "Vo nel la nén";
    testString(wrongString, correctString, ISSUE_373_TEST_SUGGESTION_INSIDE);
  }

  @Test
  public void testIssueSuggestionOutside() throws Exception {
    String wrongString = "Xnel vola nén";
    String correctString = "Vo nel la nén";
    testString(wrongString, correctString, ISSUE_373_TEST_SUGGESTION_OUTSIDE);
  }

  @Test
  public void testNoIssue1() throws Exception {
    String wrongString = "Znel vola nén";
    String correctString = "Vo nel la nén";
    testString(wrongString, correctString, ISSUE_373_TEST_NO_ISSUE_1);
  }

  @Test
  public void testNoIssue2() throws Exception {
    String wrongString = "Znel vola nén";
    String correctString = "Vo nel la nén";
    testString(wrongString, correctString, ISSUE_373_TEST_NO_ISSUE_2);
  }


  @Test
  public void testDeWalkaround() throws Exception {
    String wrongString = "Das habe ich von vorne herein geahnt.";
    String correctString = "Das habe ich von vorneherein geahnt.";
    testString(wrongString, correctString, "DE_WALKAROUND");
  }

  @Test
  public void test62() throws Exception {
    String wrongString = "Du solltest die 62-Bit-Version installieren";
    String correctString = "Du solltest die 32-Bit-Version installieren";
    testString(wrongString, correctString, "BIT_62");
  }


  @Test
  public void testIssue1() throws Exception {
    String wrongString = "She lived several years in south America.";
    String correctString = "She lived several years in South America.";
    testString(wrongString, correctString, "ISSUE_373_TEST_ISSUE_1");
  }

  private void testString(String wrongString, String correctString, String ruleId) throws IOException {
    System.out.println(String.format("testing rule: '%s'", ruleId));
    Rule issueRule = getRuleById(ruleId, RULES);

    AnalyzedSentence analyzedSentence = langTool.getAnalyzedSentence(wrongString);
    RuleMatch[] matches = issueRule.match(analyzedSentence);

    int from = matches[0].getFromPos();
    int to = matches[0].getToPos();
    String fixedString = wrongString.substring(0, from) + matches[0].getSuggestedReplacements().get(0) + wrongString.substring(to);

    System.out.println("\twrong string:   " + wrongString);
    System.out.println("\tfixed string:   " + fixedString);
    System.out.println("\tcorrect string: " + correctString);
    try {
      assertEquals(correctString, fixedString);
      System.out.println("SUCCESS");
    } catch (AssertionError e) {
      System.out.println("FAILURE");
      throw new AssertionError(e);
    }
  }


  private Rule getRuleById(String id, List<AbstractPatternRule> rules) {
    for (Rule rule : rules) {
      if (rule.getId().equals(id)) {
        return rule;
      }
    }
    throw new RuntimeException("No rule found for id '" + id + "'");
  }

}
