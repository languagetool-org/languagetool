package de.danielnaber.languagetool.rules.uk;

import java.io.IOException;

import junit.framework.TestCase;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.TestTools;
import de.danielnaber.languagetool.rules.RuleMatch;


public class SimpleReplaceRuleTest extends TestCase {

	public void testRule() throws IOException {
		SimpleReplaceRule rule = new SimpleReplaceRule(TestTools.getEnglishMessages());

		RuleMatch[] matches;
		JLanguageTool langTool = new JLanguageTool(Language.UKRAINIAN);
		
		// correct sentences:
		matches = rule.match(langTool.getAnalyzedSentence("Ці рядки повинні збігатися."));
		assertEquals(0, matches.length);

		// incorrect sentences:
		matches = rule.match(langTool.getAnalyzedSentence("Ці рядки повинні співпадати."));
		assertEquals(1, matches.length);
		assertEquals(1, matches[0].getSuggestedReplacements().size());
		assertEquals("збігатися", matches[0].getSuggestedReplacements().get(0));
	}
}
