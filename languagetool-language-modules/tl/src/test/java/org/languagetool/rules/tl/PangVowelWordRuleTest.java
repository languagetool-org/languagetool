package org.languagetool.rules.tl;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Tagalog;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PangVowelWordRuleTest {

  @Test
  public void testPangVowelWordRule() throws IOException {
    Tagalog language = new Tagalog();
    PangVowelWordRule rule = new PangVowelWordRule(TestTools.getMessages("tl"), language);
    JLanguageTool lt = new JLanguageTool(language);

    RuleMatch[] pangUloMatches = rule.match(lt.getAnalyzedSentence("pang ulo"));
    assertEquals(1, pangUloMatches.length);
    assertEquals("pang-ulo", pangUloMatches[0].getSuggestedReplacements().get(0));
    assertTrue(pangUloMatches[0].getSuggestedReplacements().contains("pangulo"));

    RuleMatch[] pangAkitMatches = rule.match(lt.getAnalyzedSentence("pang akit"));
    assertEquals(1, pangAkitMatches.length);
    assertEquals("pang-akit", pangAkitMatches[0].getSuggestedReplacements().get(0));

    assertEquals(0, rule.match(lt.getAnalyzedSentence("pangalan")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("pangulo")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("pang bata")).length);
  }
}
