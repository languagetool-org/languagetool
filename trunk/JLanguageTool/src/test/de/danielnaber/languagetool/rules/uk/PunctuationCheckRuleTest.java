package de.danielnaber.languagetool.rules.uk;

import java.io.IOException;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.TestTools;
import de.danielnaber.languagetool.rules.RuleMatch;
import junit.framework.TestCase;

public class PunctuationCheckRuleTest extends TestCase {

	public void testRule() throws IOException {
		PunctuationCheckRule rule = new PunctuationCheckRule(TestTools.getEnglishMessages());

		RuleMatch[] matches;
		JLanguageTool langTool = new JLanguageTool(Language.UKRAINIAN);
		
		// correct sentences:
		matches = rule.match(langTool.getAnalyzedSentence("Đ”Đ˛Ń–, ĐşĐľĐĽĐ¸. ĐžŃ�ŃŚ: Đ´Đ˛Ń–!!!"));
		assertEquals(0, matches.length);

		// correct sentences:
		matches = rule.match(langTool.getAnalyzedSentence("- Đ¦Đµ Đ˛Đ°Ń�Đ° ĐżŃ€ŃŹĐĽĐ° ĐĽĐľĐ˛Đ°?!!"));
		assertEquals(0, matches.length);

		// correct sentences:
		matches = rule.match(langTool.getAnalyzedSentence("Đ”Đ˛Ń–,- ĐşĐľĐĽĐ¸!.."));
		assertEquals(0, matches.length);

		// correct sentences:
		matches = rule.match(langTool.getAnalyzedSentence("Đ”Đ˛Đ°  ĐżŃ€ĐľĐ±Ń–Đ»Đ¸."));	// ĐżĐľĐşĐ¸ Ń‰Đľ Ń–ĐłĐ˝ĐľŃ€Ń�Ń”ĐĽĐľ - Đ˝Đµ Ń†Đ°Ń€Ń�ŃŚĐşĐ° Ń†Đµ Ń�ĐżŃ€Đ°Đ˛Đ° :)
		assertEquals(0, matches.length);

		// incorrect sentences:
		matches = rule.match(langTool.getAnalyzedSentence("Đ”Đ˛Ń– ĐşŃ€Đ°ĐżĐşĐ¸.."));
		assertEquals(1, matches.length);
		assertEquals(1, matches[0].getSuggestedReplacements().size());
		assertEquals(".", matches[0].getSuggestedReplacements().get(0));

		// incorrect sentences:
		matches = rule.match(langTool.getAnalyzedSentence("Đ”Đ˛Ń–,, ĐşĐľĐĽĐ¸."));
		assertEquals(1, matches.length);

		// incorrect sentences:
		matches = rule.match(langTool.getAnalyzedSentence("ĐťĐµ Ń‚Đ°ĐĽ ,ĐşĐľĐĽĐ°."));
		assertEquals(1, matches.length);

		// incorrect sentences:
		matches = rule.match(langTool.getAnalyzedSentence("Đ”Đ˛ĐľĐşŃ€Đ°ĐżĐşĐ°:- Đ· Ń‚Đ¸Ń€Đµ."));
		assertEquals(1, matches.length);
	}
}
