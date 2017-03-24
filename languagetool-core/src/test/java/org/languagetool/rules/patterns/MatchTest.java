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

import org.junit.Before;
import org.junit.Test;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Demo;
import org.languagetool.rules.patterns.Match.CaseConversion;
import org.languagetool.rules.patterns.Match.IncludeRange;
import org.languagetool.synthesis.ManualSynthesizer;
import org.languagetool.synthesis.ManualSynthesizerAdapter;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.ManualTagger;
import org.languagetool.tagging.Tagger;
import org.languagetool.tokenizers.ManualTaggerAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author Ionuț Păduraru
 */
public class MatchTest {

  private static final String TEST_DATA =
          "# some test data\n" +
          "inflectedform11\tlemma1\tPOS1\n" +
          "inflectedform121\tlemma1\tPOS2\n" +
          "inflectedform122\tlemma1\tPOS2\n" +
          "inflectedform123\tlemma1\tPOS3\n" +
          "inflectedform2\tlemma2\tPOS1\n";

  private JLanguageTool languageTool;
  private Synthesizer synthesizer;
  private Tagger tagger;

  //-- helper methods

  private AnalyzedTokenReadings[] getAnalyzedTokenReadings(String input) throws IOException {
    return languageTool.getAnalyzedSentence(input).getTokensWithoutWhitespace();
  }

  private AnalyzedTokenReadings getAnalyzedTokenReadings(String token, String posTag, String lemma) {
    return new AnalyzedTokenReadings(new AnalyzedToken(token, posTag, lemma), 0);
  }

  private Match getMatch(String posTag, String posTagReplace, CaseConversion caseConversion) {
    return new Match(posTag, posTagReplace, true, null, null, caseConversion, false, false, IncludeRange.NONE);
  }

  private Match getMatch(String posTag, String posTagReplace, IncludeRange includeRange) {
    return  new Match(posTag, posTagReplace, true, null, null, CaseConversion.NONE, false, false, includeRange);
  }

  //-- setup

  @Before
  public void setUp() throws Exception {
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

  @Test
  public void testStartUpper() throws Exception {
    Match match = getMatch("POS1", "POS2", Match.CaseConversion.STARTUPPER);
    MatchState state = match.createState(synthesizer, getAnalyzedTokenReadings("inflectedform11", "POS1", "Lemma1"));
    assertEquals("[Inflectedform121, Inflectedform122]", Arrays.toString(state.toFinalString(null)));
  }

  @Test
  public void testStartLower() throws Exception {
    Match match = getMatch("POS1", "POS2", Match.CaseConversion.STARTLOWER);
    MatchState state = match.createState(synthesizer, getAnalyzedTokenReadings("InflectedForm11", "POS1", "Lemma1"));
    assertEquals("[inflectedform121, inflectedform122]", Arrays.toString(state.toFinalString(null)));
  }

  @Test
  public void testAllUpper() throws Exception {
    Match match = getMatch("POS1", "POS2", Match.CaseConversion.ALLUPPER);
    MatchState state = match.createState(synthesizer, getAnalyzedTokenReadings("InflectedForm11", "POS1", "Lemma1"));
    assertEquals("[INFLECTEDFORM121, INFLECTEDFORM122]", Arrays.toString(state.toFinalString(null)));
  }

  @Test
  public void testAllLower() throws Exception {
    Match match = getMatch("POS1", "POS2", Match.CaseConversion.ALLLOWER);
    MatchState state = match.createState(synthesizer, getAnalyzedTokenReadings("InflectedForm11", "POS1", "Lemma1"));
    assertEquals("[inflectedform121, inflectedform122]", Arrays.toString(state.toFinalString(null)));
  }

  @Test
  public void testPreserveStartUpper() throws Exception {
    Match match = getMatch("POS1", "POS2", Match.CaseConversion.PRESERVE);
    MatchState state = match.createState(synthesizer, getAnalyzedTokenReadings("InflectedForm11", "POS1", "Lemma1"));
    assertEquals("[Inflectedform121, Inflectedform122]", Arrays.toString(state.toFinalString(null)));
  }

  @Test
  public void testStaticLemmaPreserveStartLower() throws Exception {
    Match match = getMatch("POS2", "POS1", Match.CaseConversion.PRESERVE);
    match.setLemmaString("lemma2");
    MatchState state = match.createState(synthesizer, getAnalyzedTokenReadings("inflectedform121", "POS2", "Lemma1"));
    assertEquals("[inflectedform2]", Arrays.toString(state.toFinalString(null)));
  }

  @Test
  public void testStaticLemmaPreserveStartUpper() throws Exception {
    Match match = getMatch("POS2", "POS1", Match.CaseConversion.PRESERVE);
    match.setLemmaString("lemma2");
    MatchState state = match.createState(synthesizer, getAnalyzedTokenReadings("InflectedForm121", "POS2", "Lemma1"));
    assertEquals("[Inflectedform2]", Arrays.toString(state.toFinalString(null)));
  }

  @Test
  public void testStaticLemmaPreserveAllUpper() throws Exception {
    Match match = getMatch("POS2", "POS1", Match.CaseConversion.PRESERVE);
    match.setLemmaString("lemma2");
    MatchState state = match.createState(synthesizer, getAnalyzedTokenReadings("INFLECTEDFORM121", "POS2", "Lemma1"));
    assertEquals("[INFLECTEDFORM2]", Arrays.toString(state.toFinalString(null)));
  }

  @Test
  public void testStaticLemmaPreserveMixed() throws Exception {
    Match match = getMatch("POS2", "POS1", Match.CaseConversion.PRESERVE);
    match.setLemmaString("lemma2");
    MatchState state = match.createState(synthesizer, getAnalyzedTokenReadings("infleCtedForm121", "POS2", "Lemma1"));
    assertEquals("[inflectedform2]", Arrays.toString(state.toFinalString(null)));
  }

  @Test
  public void testPreserveStartLower() throws Exception {
    Match match = getMatch("POS1", "POS2", Match.CaseConversion.PRESERVE);
    MatchState state = match.createState(synthesizer, getAnalyzedTokenReadings("inflectedForm11", "POS1", "Lemma1"));
    assertEquals("[inflectedform121, inflectedform122]", Arrays.toString(state.toFinalString(null)));
  }

  @Test
  public void testPreserveAllUpper() throws Exception {
    Match match = getMatch("POS1", "POS2", Match.CaseConversion.PRESERVE);
    MatchState state = match.createState(synthesizer, getAnalyzedTokenReadings("INFLECTEDFORM11", "POS1", "Lemma1"));
    assertEquals("[INFLECTEDFORM121, INFLECTEDFORM122]", Arrays.toString(state.toFinalString(null)));
  }

  @Test
  public void testPreserveMixed() throws Exception {
    Match match = getMatch("POS1", "POS2", Match.CaseConversion.PRESERVE);
    MatchState state = match.createState(synthesizer, getAnalyzedTokenReadings("inflecTedForm11", "POS1", "Lemma1"));
    assertEquals("[inflectedform121, inflectedform122]", Arrays.toString(state.toFinalString(null)));
  }

  @Test
  public void testPreserveNoneUpper() throws Exception {
    Match match = getMatch("POS1", "POS2", Match.CaseConversion.NONE);
    MatchState state = match.createState(synthesizer, getAnalyzedTokenReadings("INFLECTEDFORM11", "POS1", "Lemma1"));
    assertEquals("[inflectedform121, inflectedform122]", Arrays.toString(state.toFinalString(null)));
  }

  @Test
  public void testPreserveNoneLower() throws Exception {
    Match match = getMatch("POS1", "POS2", Match.CaseConversion.NONE);
    MatchState state = match.createState(synthesizer, getAnalyzedTokenReadings("inflectedform11", "POS1", "Lemma1"));
    assertEquals("[inflectedform121, inflectedform122]", Arrays.toString(state.toFinalString(null)));
  }

  @Test
  public void testPreserveNoneMixed() throws Exception {
    Match match = getMatch("POS1", "POS2", Match.CaseConversion.NONE);
    MatchState state = match.createState(synthesizer, getAnalyzedTokenReadings("inFLectedFOrm11", "POS1", "Lemma1"));
    assertEquals("[inflectedform121, inflectedform122]", Arrays.toString(state.toFinalString(null)));
  }

  //-- INCLUDE RANGE 

  @Test
  public void testSimpleIncludeFollowing() throws Exception {
    Match match = getMatch(null, null, Match.IncludeRange.FOLLOWING);
    MatchState state = match.createState(synthesizer, getAnalyzedTokenReadings("inflectedform11 inflectedform2 inflectedform122 inflectedform122"), 1, 3);
    assertEquals("[inflectedform2 inflectedform122]", Arrays.toString(state.toFinalString(null)));
  }

  @Test
  public void testPOSIncludeFollowing() throws Exception {
    // POS is ignored when using IncludeRange.Following
    Match match = getMatch("POS2", "POS33", Match.IncludeRange.FOLLOWING);
    MatchState state = match.createState(synthesizer, getAnalyzedTokenReadings("inflectedform11 inflectedform2 inflectedform122 inflectedform122"), 1, 3);
    assertEquals("[inflectedform2 inflectedform122]", Arrays.toString(state.toFinalString(null)));
  }

  @Test
  public void testIncludeAll() throws Exception {
    Match match = getMatch(null, null, Match.IncludeRange.ALL);
    MatchState state = match.createState(synthesizer, getAnalyzedTokenReadings("inflectedform11 inflectedform2 inflectedform122 inflectedform122"), 1, 3);
    assertEquals("[inflectedform11 inflectedform2 inflectedform122]", Arrays.toString(state.toFinalString(null)));
  }

  @Test
  public void testPOSIncludeAll() throws Exception {
    Match match = getMatch("POS1", "POS3", Match.IncludeRange.ALL);
    MatchState state = match.createState(synthesizer, getAnalyzedTokenReadings("inflectedform11 inflectedform2 inflectedform122 inflectedform122"), 1, 3);
    assertEquals("[inflectedform123 inflectedform2 inflectedform122]", Arrays.toString(state.toFinalString(null)));
    // Note that in this case the first token has the requested POS (POS3 replaces POS1)
  }

  // TODO add tests for using Match.IncludeRange with {@link Match#staticLemma}
}
