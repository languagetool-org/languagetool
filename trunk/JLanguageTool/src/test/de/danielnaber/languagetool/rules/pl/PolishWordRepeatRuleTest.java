package de.danielnaber.languagetool.rules.pl;

import java.io.IOException;

import junit.framework.TestCase;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.RuleMatch;

public class PolishWordRepeatRuleTest extends TestCase {

	/*
	 * Test method for 'de.danielnaber.languagetool.rules.pl.PolishWordRepeatRule.match(AnalyzedSentence)'
	 */
	public void testRule() throws IOException {
	    PolishWordRepeatRule rule = new PolishWordRepeatRule();
	    RuleMatch[] matches;
	    JLanguageTool langTool = new JLanguageTool(Language.POLISH);
	    //correct
	    matches = rule.match(langTool.getAnalyzedSentence("To jest zdanie próbne."));
	    assertEquals(0, matches.length);
	    //repeated prepositions, don't count'em
	    matches = rule.match(langTool.getAnalyzedSentence("Na dyskotece tańczył jeszcze, choć był na bani."));
	    assertEquals(0, matches.length);
	    //incorrect
	    matches = rule.match(langTool.getAnalyzedSentence("Był on bowiem pięknym strzelcem bowiem."));
	    assertEquals(1, matches.length);
	    matches = rule.match(langTool.getAnalyzedSentence("Mówiła długo, żeby tylko mówić długo."));
	    assertEquals(2, matches.length);
	}

}
