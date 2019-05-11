/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://danielnaber.de/)
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
package org.languagetool.rules.de;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.RuleMatch;

public class MissingCommaRelativeClauseRuleTest {
  @Test
  public void testMatch() throws Exception {
    JLanguageTool lt = new JLanguageTool(new GermanyGerman());
    
    MissingCommaRelativeClauseRule rule = new MissingCommaRelativeClauseRule(TestTools.getMessages("de"));
    
    assertMatch("Das Auto das am Straßenrand steht parkt im Halteverbot.", 4, 12, rule, lt);
    assertMatch("Das Auto das am Straßenrand steht, parkt im Halteverbot.", 4, 12, rule, lt);
    assertMatch("Das Auto in dem der Mann sitzt, parkt im Halteverbot.", 4, 15, rule, lt);
    assertMatch("Das Auto in dem der Mann sitzt parkt im Halteverbot.", 4, 15, rule, lt);
    assertMatch("Die Frau die vor dem Auto steht hat schwarze Haare.", 4, 12, rule, lt);
    assertMatch("Die Frau die vor dem Auto steht, hat schwarze Haare.", 4, 12, rule, lt);
    assertMatch("Alles was ich habe, ist ein Buch.", 0, 9, rule, lt);
    

    assertNoMatch("Computer machen die Leute dumm.", rule, lt);
    assertNoMatch("Die Unstimmigkeit zwischen den Geschichten der zwei Unfallbeteiligten war groß.", rule, lt);
    assertNoMatch("Ebenso darf keine schwerere Strafe als die zum Zeitpunkt der Begehung der strafbaren Handlung angedrohte Strafe verhängt werden.", rule, lt);
    
    rule = new MissingCommaRelativeClauseRule(TestTools.getMessages("de"), true);
    
    assertMatch("Das Auto, das am Straßenrand steht parkt im Halteverbot.", 29, 40, rule, lt);
    assertMatch("Das Auto, in dem der Mann sitzt parkt im Halteverbot.", 26, 37, rule, lt);
    assertMatch("Die Frau, die vor dem Auto steht hat schwarze Haare.", 27, 36, rule, lt);
    assertMatch("Alles, was ich habe ist ein Buch.", 15, 23, rule, lt);

    assertNoMatch("Ich habe einige Fehler begangen, die ich vermeiden hätte können sollen.", rule, lt);
    assertNoMatch("Wenn du alles, was du meinst nicht zu können, von anderen erledigen lässt, wirst du es niemals selbst lernen.", rule, lt);
    assertNoMatch("Er hat einen Zeitraum durchlebt, in dem seine Gedanken verträumt auf den weiten Feldern der Mysterien umherirrten.", rule, lt);
    
  
  }
  
  protected void assertNoMatch(String input, MissingCommaRelativeClauseRule rule, JLanguageTool lt) throws IOException {
    assertThat(rule.match(lt.getAnalyzedSentence(input)).length, is(0));
  }

  protected void assertMatch(String input, int from, int to, MissingCommaRelativeClauseRule rule, JLanguageTool lt) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    assertThat(matches.length, is(1));
    assertThat(matches[0].getFromPos(), is(from));
    assertThat(matches[0].getToPos(), is(to));
  }

  

}
