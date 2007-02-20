package de.danielnaber.languagetool.rules.uk;

import java.io.IOException;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.TestTools;
import de.danielnaber.languagetool.rules.RuleMatch;
import junit.framework.TestCase;

public class SimpleReplaceRuleTest extends TestCase {

	public void testRule() throws IOException {
		SimpleReplaceRule rule = new SimpleReplaceRule(TestTools.getEnglishMessages());

		RuleMatch[] matches;
		JLanguageTool langTool = new JLanguageTool(Language.UKRAINIAN);
		
		// correct sentences:
		matches = rule.match(langTool.getAnalyzedSentence("Đ¦Ń– Ń€ŃŹĐ´ĐşĐ¸ ĐżĐľĐ˛Đ¸Đ˝Đ˝Ń– Đ·Đ±Ń–ĐłĐ°Ń‚Đ¸Ń�ŃŹ."));
		assertEquals(0, matches.length);

		// incorrect sentences:
		matches = rule.match(langTool.getAnalyzedSentence("Đ¦Ń– Ń€ŃŹĐ´ĐşĐ¸ ĐżĐľĐ˛Đ¸Đ˝Đ˝Ń– Ń�ĐżŃ–Đ˛ĐżĐ°Đ´Đ°Ń‚Đ¸."));
		assertEquals(1, matches.length);
		assertEquals(1, matches[0].getSuggestedReplacements().size());
		assertEquals("Đ·Đ±Ń–ĐłĐ°Ń‚Đ¸Ń�ŃŹ", matches[0].getSuggestedReplacements().get(0));
	}
}
