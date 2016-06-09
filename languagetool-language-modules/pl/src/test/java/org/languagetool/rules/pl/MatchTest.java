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
package org.languagetool.rules.pl;

import org.junit.Test;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.language.Polish;
import org.languagetool.rules.patterns.Match;
import org.languagetool.rules.patterns.Match.CaseConversion;
import org.languagetool.rules.patterns.Match.IncludeRange;
import org.languagetool.rules.patterns.MatchState;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class MatchTest {

  private AnalyzedTokenReadings getAnalyzedTokenReadings(String token, String posTag, String lemma) {
    return new AnalyzedTokenReadings(new AnalyzedToken(token, posTag, lemma), 0);
  }

  private Match getMatch(String posTag, String posTagReplace, boolean spell) {
    final Match match = new Match(posTag, posTagReplace, true, null, null, CaseConversion.NONE, false, spell, IncludeRange.NONE);
    return match;
  }

  private Match getTextMatch(String regexMatch, String regexpReplace, boolean spell) {
    final Match match = new Match(null, null, false, regexMatch, regexpReplace, CaseConversion.NONE, false, spell, IncludeRange.NONE);
    return match;
  }

  @Test
  public void testSpeller() throws Exception {
    //tests with synthesizer
    Match match = getMatch("POS1", "POS2", true);
    final Polish polish = new Polish();
    MatchState matchState = new MatchState(match, polish.getSynthesizer());
    matchState.setToken(getAnalyzedTokenReadings("inflectedform11", "POS1", "Lemma1"));
    //getting empty strings, which is what we want
    assertEquals("[]", Arrays.toString(matchState.toFinalString(polish)));

    // contrast with a speller = false!
    match = getMatch("POS1", "POS2", false);
    matchState = new MatchState(match, polish.getSynthesizer());
    matchState.setToken(getAnalyzedTokenReadings("inflectedform11", "POS1", "Lemma1"));
    assertEquals("[(inflectedform11)]", Arrays.toString(matchState.toFinalString(polish)));

    //and now a real word - we should get something
    match = getMatch("subst:sg:acc.nom:m3", "subst:sg:gen:m3", true);
    matchState = new MatchState(match, polish.getSynthesizer());
    matchState.setToken(getAnalyzedTokenReadings("AON", "subst:sg:acc.nom:m3", "AON"));
    assertEquals("[AON-u]", Arrays.toString(matchState.toFinalString(polish)));

    //and now pure text changes        
    match = getTextMatch("^(.*)$", "$0-u", true);
    match.setLemmaString("AON");
    matchState = new MatchState(match, polish.getSynthesizer());
    assertEquals("[AON-u]", Arrays.toString(matchState.toFinalString(polish)));
    match.setLemmaString("batalion");
    //should be empty
    matchState = new MatchState(match, polish.getSynthesizer());
    assertEquals("[]", Arrays.toString(matchState.toFinalString(polish)));
    match.setLemmaString("ASEAN");
    //and this one not
    matchState = new MatchState(match, polish.getSynthesizer());
    assertEquals("[ASEAN-u]", Arrays.toString(matchState.toFinalString(polish)));
  }
}
