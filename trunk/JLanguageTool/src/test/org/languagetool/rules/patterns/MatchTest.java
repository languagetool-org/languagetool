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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import morfologik.stemming.IStemmer;
import morfologik.stemming.WordData;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.patterns.Match.CaseConversion;
import org.languagetool.rules.patterns.Match.IncludeRange;
import org.languagetool.synthesis.BaseSynthesizer;
import org.languagetool.synthesis.ManualSynthesizer;
import org.languagetool.synthesis.Synthesizer;

/**
 * Test class for {@link Match}.
 * @author Ionuț Păduraru
 */
public class MatchTest extends TestCase {

	/**
	 *  Adapter from {@link ManualSynthesizer} to {@link Synthesizer}. <br/> 
	 *  Note: This could be extracted as a standalone class.
	 */
	public static class ManualSynthesizerAdapter extends BaseSynthesizer implements Synthesizer  {
		private ManualSynthesizer manualSynthesizer;
		public ManualSynthesizerAdapter(ManualSynthesizer manualSynthesizer) {
			super(null, null); // no file
			this.manualSynthesizer = manualSynthesizer;
		}
		@Override
		protected void initSynthesizer() throws IOException {
			synthesizer = new IStemmer() { // null synthesiser 
				@Override
				public List<WordData> lookup(CharSequence word) {
					return new ArrayList<WordData>();
				}
			};
		}
		@Override
		protected void initPossibleTags() throws IOException {
			if (possibleTags == null) {
				possibleTags = new ArrayList<String>(manualSynthesizer.getPossibleTags());
			}
		}
		@Override
		protected void lookup(String lemma, String posTag, List<String> results) {
			super.lookup(lemma, posTag, results);
			// add words that are missing from the romanian_synth.dict file
			final List<String> manualForms = manualSynthesizer.lookup(lemma.toLowerCase(), posTag);
			if (manualForms != null) {
				results.addAll(manualForms); 
			}
		}
	}
	
	//-- helper methods
	
	private Synthesizer getTestSynthesizer() throws UnsupportedEncodingException, IOException {
		 final String data = 
			      "# some test data\n" +
			      "inflectedform11\tlemma1\tPOS1\n" +
			      "inflectedform121\tlemma1\tPOS2\n" +
			      "inflectedform122\tlemma1\tPOS2\n" +
			      "inflectedform2\tlemma2\tPOS1\n"
			      ;
		return new ManualSynthesizerAdapter(new ManualSynthesizer(new ByteArrayInputStream(data.getBytes("UTF-8"))));
	}

	private AnalyzedTokenReadings getAnalyzedTokenReadings(String token, String posTag, String lemma) {
		return new AnalyzedTokenReadings(new AnalyzedToken(token, posTag, lemma), 0);
	}

	private Match getMatch(String posTag, String posTagReplace, CaseConversion caseConversion) throws UnsupportedEncodingException, IOException {
		Match match = new Match(posTag, posTagReplace, true, null, null, caseConversion, false, IncludeRange.NONE);
		match.setSynthesizer(getTestSynthesizer());
		return match;
	}

	//-- test methods
	
		//-- CASE CONVERSION
	
	public void testStartUpper() throws Exception {
		Match match = getMatch("POS1", "POS2", Match.CaseConversion.STARTUPPER);
		match.setToken(getAnalyzedTokenReadings("inflectedform11", "POS1", "Lemma1"));
		assertEquals("[Inflectedform121, Inflectedform122]", Arrays.toString( match.toFinalString()));
	}

	public void testStartLower() throws Exception {
		Match match = getMatch("POS1", "POS2", Match.CaseConversion.STARTLOWER);
		match.setToken(getAnalyzedTokenReadings("InflectedForm11", "POS1", "Lemma1"));
		assertEquals("[inflectedform121, inflectedform122]", Arrays.toString(match.toFinalString()));
	}

	public void testAllUpper() throws Exception {
		Match match = getMatch("POS1", "POS2", Match.CaseConversion.ALLUPPER);
		match.setToken(getAnalyzedTokenReadings("InflectedForm11", "POS1", "Lemma1"));
		assertEquals("[INFLECTEDFORM121, INFLECTEDFORM122]", Arrays.toString(match.toFinalString()));
	}

	public void testAllLower() throws Exception {
		Match match = getMatch("POS1", "POS2", Match.CaseConversion.ALLLOWER);
		match.setToken(getAnalyzedTokenReadings("InflectedForm11", "POS1", "Lemma1"));
		assertEquals("[inflectedform121, inflectedform122]", Arrays.toString(match.toFinalString()));
	}

	public void testPreserveStartUpper() throws Exception {
		Match match = getMatch("POS1", "POS2", Match.CaseConversion.PRESERVE);
		match.setToken(getAnalyzedTokenReadings("InflectedForm11", "POS1", "Lemma1"));
		assertEquals("[Inflectedform121, Inflectedform122]", Arrays.toString(match.toFinalString()));
	}
	
	public void testPreserveStartLower() throws Exception {
		Match match = getMatch("POS1", "POS2", Match.CaseConversion.PRESERVE);
		match.setToken(getAnalyzedTokenReadings("inflectedForm11", "POS1", "Lemma1"));
		assertEquals("[inflectedform121, inflectedform122]", Arrays.toString(match.toFinalString()));
	}
	
	public void testPreserveAllUpper() throws Exception {
		Match match = getMatch("POS1", "POS2", Match.CaseConversion.PRESERVE);
		match.setToken(getAnalyzedTokenReadings("INFLECTEDFORM11", "POS1", "Lemma1"));
		assertEquals("[INFLECTEDFORM121, INFLECTEDFORM122]", Arrays.toString( match.toFinalString()));
	}
	
	public void testPreserveMixed() throws Exception {
		Match match = getMatch("POS1", "POS2", Match.CaseConversion.PRESERVE);
		match.setToken(getAnalyzedTokenReadings("inflecTedForm11", "POS1", "Lemma1"));
		assertEquals("[inflectedform121, inflectedform122]", Arrays.toString(match.toFinalString()));
		
	}

	public void testPreserveNoneUpper() throws Exception {
		Match match = getMatch("POS1", "POS2", Match.CaseConversion.NONE);
		match.setToken(getAnalyzedTokenReadings("INFLECTEDFORM11", "POS1", "Lemma1"));
		assertEquals("[inflectedform121, inflectedform122]", Arrays.toString(match.toFinalString()));
	}
	
	public void testPreserveNoneLower() throws Exception {
		Match match = getMatch("POS1", "POS2", Match.CaseConversion.NONE);
		match.setToken(getAnalyzedTokenReadings("inflectedform11", "POS1", "Lemma1"));
		assertEquals("[inflectedform121, inflectedform122]", Arrays.toString(match.toFinalString()));
	}
	
	public void testPreserveNoneMixed() throws Exception {
		Match match = getMatch("POS1", "POS2", Match.CaseConversion.NONE);
		match.setToken(getAnalyzedTokenReadings("inFLectedFOrm11", "POS1", "Lemma1"));
		assertEquals("[inflectedform121, inflectedform122]", Arrays.toString(match.toFinalString()));
	}

}
