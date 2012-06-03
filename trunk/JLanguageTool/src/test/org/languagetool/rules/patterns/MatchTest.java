/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.rules.patterns;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import junit.framework.TestCase;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.Demo;
import org.languagetool.rules.patterns.Match.CaseConversion;
import org.languagetool.rules.patterns.Match.IncludeRange;
import org.languagetool.synthesis.ManualSynthesizer;
import org.languagetool.synthesis.ManualSynthesizerAdapter;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.ManualTagger;
import org.languagetool.tagging.Tagger;
import org.languagetool.tokenizers.ManualTaggerAdapter;

/**
 * Test class for {@link Match}.
 * @author Ionuț Păduraru
 */
public class MatchTest extends TestCase {

	final static String TEST_DATA = 
			"# some test data\n" +
					"inflectedform11\tlemma1\tPOS1\n" +
					"inflectedform121\tlemma1\tPOS2\n" +
					"inflectedform122\tlemma1\tPOS2\n" +
					"inflectedform123\tlemma1\tPOS3\n" +
					"inflectedform2\tlemma2\tPOS1\n"
					;

	protected JLanguageTool languageTool;
	protected Synthesizer synthesizer;
	protected Tagger tagger;
	
	
	//-- helper methods
	
	private AnalyzedTokenReadings[] getAnalyzedTokenReadings(final String input) throws IOException {
	   return languageTool.getAnalyzedSentence(input).getTokensWithoutWhitespace();
	}
	
	private AnalyzedTokenReadings getAnalyzedTokenReadings(String token, String posTag, String lemma) {
		return new AnalyzedTokenReadings(new AnalyzedToken(token, posTag, lemma), 0);
	}

	private Match getMatch(String posTag, String posTagReplace, CaseConversion caseConversion) throws UnsupportedEncodingException, IOException {
		Match match = new Match(posTag, posTagReplace, true, null, null, caseConversion, false, false, IncludeRange.NONE);
		match.setSynthesizer(synthesizer);
		return match;
	}
	
	private Match getMatch(String posTag, String posTagReplace, boolean spell) throws UnsupportedEncodingException, IOException {
        Match match = new Match(posTag, posTagReplace, true, null, null, CaseConversion.NONE, false, spell, IncludeRange.NONE);        
        return match;
    }
	
	private Match getTextMatch(String regexMatch, String regexpReplace, boolean spell) throws UnsupportedEncodingException, IOException {
        Match match = new Match(null, null, false, regexMatch, regexpReplace, CaseConversion.NONE, false, spell, IncludeRange.NONE);        
        return match;
    }

	private Match getMatch(String posTag, String posTagReplace, IncludeRange includeRange) throws UnsupportedEncodingException, IOException {
		Match match = new Match(posTag, posTagReplace, true, null, null, CaseConversion.NONE, false, false, includeRange);
		match.setSynthesizer(synthesizer);
		return match;
	}

	//-- setup

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		tagger = new ManualTaggerAdapter(new ManualTagger(new ByteArrayInputStream(TEST_DATA.getBytes("UTF-8"))));
		synthesizer = new ManualSynthesizerAdapter(new ManualSynthesizer(new ByteArrayInputStream(TEST_DATA.getBytes("UTF-8"))));
		languageTool = new JLanguageTool(new Demo() {
			@Override
			public String getName() {
				return "TEST";
			}
			@Override
			public Synthesizer getSynthesizer() {
				return MatchTest.this.synthesizer;
			}
			@Override
			public Tagger getTagger() {
				return MatchTest.this.tagger;
			}
		});
	}
	
	//-- test methods
	
		//-- CASE CONVERSION
	
	public void testStartUpper() throws Exception {
		Match match = getMatch("POS1", "POS2", Match.CaseConversion.STARTUPPER);
		match.setToken(getAnalyzedTokenReadings("inflectedform11", "POS1", "Lemma1"));
		assertEquals("[Inflectedform121, Inflectedform122]", Arrays.toString( match.toFinalString(null)));
	}

	public void testStartLower() throws Exception {
		Match match = getMatch("POS1", "POS2", Match.CaseConversion.STARTLOWER);
		match.setToken(getAnalyzedTokenReadings("InflectedForm11", "POS1", "Lemma1"));
		assertEquals("[inflectedform121, inflectedform122]", Arrays.toString(match.toFinalString(null)));
	}

	public void testAllUpper() throws Exception {
		Match match = getMatch("POS1", "POS2", Match.CaseConversion.ALLUPPER);
		match.setToken(getAnalyzedTokenReadings("InflectedForm11", "POS1", "Lemma1"));
		assertEquals("[INFLECTEDFORM121, INFLECTEDFORM122]", Arrays.toString(match.toFinalString(null)));
	}

	public void testAllLower() throws Exception {
		Match match = getMatch("POS1", "POS2", Match.CaseConversion.ALLLOWER);
		match.setToken(getAnalyzedTokenReadings("InflectedForm11", "POS1", "Lemma1"));
		assertEquals("[inflectedform121, inflectedform122]", Arrays.toString(match.toFinalString(null)));
	}

	public void testPreserveStartUpper() throws Exception {
		Match match = getMatch("POS1", "POS2", Match.CaseConversion.PRESERVE);
		match.setToken(getAnalyzedTokenReadings("InflectedForm11", "POS1", "Lemma1"));
		assertEquals("[Inflectedform121, Inflectedform122]", Arrays.toString(match.toFinalString(null)));
	}
	
	public void testStaticLemmaPreserveStartLower() throws Exception {
		Match match = getMatch("POS2", "POS1", Match.CaseConversion.PRESERVE);
		match.setLemmaString("lemma2");
		match.setToken(getAnalyzedTokenReadings("inflectedform121", "POS2", "Lemma1"));
		assertEquals("[inflectedform2]", Arrays.toString(match.toFinalString(null)));
	}
	public void testStaticLemmaPreserveStartUpper() throws Exception {
		Match match = getMatch("POS2", "POS1", Match.CaseConversion.PRESERVE);
		match.setLemmaString("lemma2");
		match.setToken(getAnalyzedTokenReadings("InflectedForm121", "POS2", "Lemma1"));
		assertEquals("[Inflectedform2]", Arrays.toString(match.toFinalString(null)));
	}
	public void testStaticLemmaPreserveAllUpper() throws Exception {
		Match match = getMatch("POS2", "POS1", Match.CaseConversion.PRESERVE);
		match.setLemmaString("lemma2");
		match.setToken(getAnalyzedTokenReadings("INFLECTEDFORM121", "POS2", "Lemma1"));
		assertEquals("[INFLECTEDFORM2]", Arrays.toString(match.toFinalString(null)));
	}
	public void testStaticLemmaPreserveMixed() throws Exception {
		Match match = getMatch("POS2", "POS1", Match.CaseConversion.PRESERVE);
		match.setLemmaString("lemma2");
		match.setToken(getAnalyzedTokenReadings("infleCtedForm121", "POS2", "Lemma1"));
		assertEquals("[inflectedform2]", Arrays.toString(match.toFinalString(null)));
	}
	
	public void testPreserveStartLower() throws Exception {
		Match match = getMatch("POS1", "POS2", Match.CaseConversion.PRESERVE);
		match.setToken(getAnalyzedTokenReadings("inflectedForm11", "POS1", "Lemma1"));
		assertEquals("[inflectedform121, inflectedform122]", Arrays.toString(match.toFinalString(null)));
	}
	
	public void testPreserveAllUpper() throws Exception {
		Match match = getMatch("POS1", "POS2", Match.CaseConversion.PRESERVE);
		match.setToken(getAnalyzedTokenReadings("INFLECTEDFORM11", "POS1", "Lemma1"));
		assertEquals("[INFLECTEDFORM121, INFLECTEDFORM122]", Arrays.toString(match.toFinalString(null)));
	}
	
	public void testPreserveMixed() throws Exception {
		Match match = getMatch("POS1", "POS2", Match.CaseConversion.PRESERVE);
		match.setToken(getAnalyzedTokenReadings("inflecTedForm11", "POS1", "Lemma1"));
		assertEquals("[inflectedform121, inflectedform122]", Arrays.toString(match.toFinalString(null)));
		
	}

	public void testPreserveNoneUpper() throws Exception {
		Match match = getMatch("POS1", "POS2", Match.CaseConversion.NONE);
		match.setToken(getAnalyzedTokenReadings("INFLECTEDFORM11", "POS1", "Lemma1"));
		assertEquals("[inflectedform121, inflectedform122]", Arrays.toString(match.toFinalString(null)));
	}
	
	public void testPreserveNoneLower() throws Exception {
		Match match = getMatch("POS1", "POS2", Match.CaseConversion.NONE);
		match.setToken(getAnalyzedTokenReadings("inflectedform11", "POS1", "Lemma1"));
		assertEquals("[inflectedform121, inflectedform122]", Arrays.toString(match.toFinalString(null)));
	}
	
	public void testPreserveNoneMixed() throws Exception {
		Match match = getMatch("POS1", "POS2", Match.CaseConversion.NONE);
		match.setToken(getAnalyzedTokenReadings("inFLectedFOrm11", "POS1", "Lemma1"));
		assertEquals("[inflectedform121, inflectedform122]", Arrays.toString(match.toFinalString(null)));
	}

		//-- INCLUDE RANGE 
	
	public void testSimpleIncludeFollowing() throws Exception {
		Match match = getMatch(null, null, Match.IncludeRange.FOLLOWING);
		match.setToken(getAnalyzedTokenReadings("inflectedform11 inflectedform2 inflectedform122 inflectedform122"), 1, 3);
		assertEquals("[inflectedform2 inflectedform122]", Arrays.toString(match.toFinalString(null)));
	}

	public void testPOSIncludeFollowing() throws Exception {
		// POS is ignored when using IncludeRange.Following
		Match match = getMatch("POS2", "POS33", Match.IncludeRange.FOLLOWING); 
		match.setToken(getAnalyzedTokenReadings("inflectedform11 inflectedform2 inflectedform122 inflectedform122"), 1, 3);
		assertEquals("[inflectedform2 inflectedform122]", Arrays.toString(match.toFinalString(null)));
	}
	
	public void testIncludeAll() throws Exception {
		Match match = getMatch(null, null, Match.IncludeRange.ALL);
		match.setToken(getAnalyzedTokenReadings("inflectedform11 inflectedform2 inflectedform122 inflectedform122"), 1, 3);
		assertEquals("[inflectedform11inflectedform2 inflectedform122]", Arrays.toString(match.toFinalString(null)));
		// the first two tokens come together, it is a known issue
	}

	public void testPOSIncludeAll() throws Exception {
		Match match = getMatch("POS1", "POS3", Match.IncludeRange.ALL); 
		match.setToken(getAnalyzedTokenReadings("inflectedform11 inflectedform2 inflectedform122 inflectedform122"), 1, 3);
		assertEquals("[inflectedform123inflectedform2 inflectedform122]", Arrays.toString(match.toFinalString(null)));
		// Note that in this case the first token has the requested POS (POS3 replaces POS1)
		// the first two tokens come together, it is a known issue. 
	}
	
	// TODO ad tests for using Match.IncludeRange with {@link Match#staticLemma}
	
	public void testSpeller() throws Exception {
	    //tests with synthesizer
        Match match = getMatch("POS1", "POS2", true);
        match.setSynthesizer(Language.POLISH.getSynthesizer());
        match.setToken(getAnalyzedTokenReadings("inflectedform11", "POS1", "Lemma1"));
        //getting empty strings, which is what we want
        assertEquals("[]", Arrays.toString( match.toFinalString(Language.POLISH)));
        
        // contrast with a speller = false!
        match = getMatch("POS1", "POS2", false);
        match.setSynthesizer(Language.POLISH.getSynthesizer());
        match.setToken(getAnalyzedTokenReadings("inflectedform11", "POS1", "Lemma1"));        
        assertEquals("[(inflectedform11)]", Arrays.toString( match.toFinalString(Language.POLISH)));
        
        //and now a real word - we should get something
        match = getMatch("subst:sg:acc.nom:m3", "subst:sg:gen:m3", true);
        match.setSynthesizer(Language.POLISH.getSynthesizer());
        match.setToken(getAnalyzedTokenReadings("AON", "subst:sg:acc.nom:m3", "AON"));
        assertEquals("[AON-u]", Arrays.toString( match.toFinalString(Language.POLISH)));
        
        //and now pure text changes        
        match = getTextMatch("^(.*)$", "$0-u", true);
        match.setSynthesizer(Language.POLISH.getSynthesizer());
        match.setLemmaString("AON");
        assertEquals("[AON-u]", Arrays.toString( match.toFinalString(Language.POLISH)));
        match.setLemmaString("batalion");
        //should be empty
        assertEquals("[]", Arrays.toString( match.toFinalString(Language.POLISH)));
        match.setLemmaString("ASEAN");
        //and this one not
        assertEquals("[ASEAN-u]", Arrays.toString( match.toFinalString(Language.POLISH)));
    }
}
