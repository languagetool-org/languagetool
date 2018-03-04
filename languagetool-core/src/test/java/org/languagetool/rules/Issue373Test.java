package org.languagetool.rules;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.language.Demo;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.PatternRuleLoader;
import org.languagetool.rules.patterns.PatternToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class Issue373Test {

    private static final String GRAMMAR_FILE_NAME = "/xx/grammar.xml";
    private static final String ISSUE_TEST_RULE_1 = "ISSUE_373_TEST_01";
    private static final String ISSUE_TEST_RULE_2 = "ISSUE_373_TEST_02";
    private static final String ISSUE_TEST_RULE_3 = "ISSUE_373_TEST_03";
    private static List<AbstractPatternRule> RULES;
    private JLanguageTool langTool;

    @Before
    public void setUp() throws Exception {
        langTool = new JLanguageTool(new Demo());
        PatternRuleLoader prg = new PatternRuleLoader();
        RULES = prg.getRules(JLanguageTool.getDataBroker().getFromRulesDirAsStream(GRAMMAR_FILE_NAME), GRAMMAR_FILE_NAME);
    }

    @Test
    public void testIssueRule1() throws Exception {
        String wrongString = "Xnel vola nén";
        String correctString = "Vo nel la nén";
        testString(wrongString, correctString, ISSUE_TEST_RULE_1);
    }

    @Test
    public void testIssueRule2() throws Exception {
        String wrongString = "Xnel vola nén";
        String correctString = "Vo nel la nén";
        testString(wrongString, correctString, ISSUE_TEST_RULE_2);
    }

    @Test
    public void testIssueRule3() throws Exception {
        String wrongString = "Znel vola nén";
        String correctString = "Vo nel la nén";
        testString(wrongString, correctString, ISSUE_TEST_RULE_3);
    }


    private void testString(String wrongString, String correctString, String ruleName) throws IOException {
        System.out.println(String.format("testing rule: '%s'", ruleName));
        Rule issueRule = getRuleById(ruleName, RULES);

        RuleMatch[] matches = issueRule.match(langTool.getAnalyzedSentence(wrongString));

        int from = matches[0].getFromPos();
        int to = matches[0].getToPos();
        String fixedString = wrongString.substring(0, from) + matches[0].getSuggestedReplacements().get(0) + wrongString.substring(to);

        System.out.println("\twrong string:   " + wrongString);
        System.out.println("\tfixed string:   " + fixedString);
        System.out.println("\tcorrect string: " + correctString);
        try {
            assertEquals(correctString, fixedString);
            System.out.println("SUCCESS");
        }
        catch (AssertionError e){
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
