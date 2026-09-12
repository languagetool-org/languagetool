package org.languagetool.rules.tl;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Tagalog;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ReduplicationRuleTest {

  @Test
  public void testReduplicationRule() throws IOException {
    Tagalog language = new Tagalog();
    ReduplicationRule rule = new ReduplicationRule(TestTools.getMessages("tl"), language);
    JLanguageTool lt = new JLanguageTool(language);

    RuleMatch[] arawarawMatches = rule.match(lt.getAnalyzedSentence("arawaraw"));
    assertEquals(1, arawarawMatches.length);
    assertEquals("araw-araw", arawarawMatches[0].getSuggestedReplacements().get(0));

    assertEquals(0, rule.match(lt.getAnalyzedSentence("lolo")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("mama")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("papa")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("araw-araw")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("nana")).length);
  }
}
