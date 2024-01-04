/* LanguageTool, a natural language style checker 
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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

import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class OldSpellingRuleTest {

  private static final Language german = Languages.getLanguageForShortCode("de");
  private static final OldSpellingRule rule = new OldSpellingRule(JLanguageTool.getMessageBundle());
  private static final JLanguageTool lt = new JLanguageTool(german);

  @Test
  public void test() throws IOException {
    AnalyzedSentence sentence1 = lt.getAnalyzedSentence("Ein Kuß");
    assertThat(rule.match(sentence1).length, is(1));
    assertThat(rule.match(sentence1)[0].getSuggestedReplacements().toString(), is("[Kuss]"));

    AnalyzedSentence sentence2 = lt.getAnalyzedSentence("Das Corpus delicti");
    assertThat(rule.match(sentence2).length, is(1));
    assertThat(rule.match(sentence2)[0].getSuggestedReplacements().toString(), is("[Corpus Delicti]"));

    // inflected forms should work, too (as long as the inflection database still contains the old variants):
    AnalyzedSentence sentence3 = lt.getAnalyzedSentence("In Rußlands Weiten");
    assertThat(rule.match(sentence3).length, is(1));
    assertThat(rule.match(sentence3)[0].getSuggestedReplacements().toString(), is("[Russlands]"));

    AnalyzedSentence sentence4 = lt.getAnalyzedSentence("Hot pants");
    assertThat(rule.match(sentence4).length, is(1));
    assertThat(rule.match(sentence4)[0].getSuggestedReplacements().toString(), is("[Hotpants]"));

    AnalyzedSentence sentence5 = lt.getAnalyzedSentence("Ich muß los");
    assertThat(rule.match(sentence5).length, is(1));
    assertThat(rule.match(sentence5)[0].getSuggestedReplacements().toString(), is("[muss]"));

    AnalyzedSentence sentence6 = lt.getAnalyzedSentence("schwarzweißmalen");
    assertThat(rule.match(sentence6).length, is(1));
    assertThat(rule.match(sentence6)[0].getSuggestedReplacements().toString(), is("[schwarzweiß malen, schwarz-weiß malen]"));

    assertThat(rule.match(lt.getAnalyzedSentence("geschneuzt"))[0].getSuggestedReplacements().toString(), is("[geschnäuzt]"));
    assertThat(rule.match(lt.getAnalyzedSentence("naß machen"))[0].getSuggestedReplacements().toString(), is("[nassmachen]"));
    assertThat(rule.match(lt.getAnalyzedSentence("Midlife-crisis"))[0].getSuggestedReplacements().toString(), is("[Midlife-Crisis, Midlifecrisis]"));
    assertThat(rule.match(lt.getAnalyzedSentence("Schluß"))[0].getSuggestedReplacements().toString(), is("[Schluss]"));
    assertThat(rule.match(lt.getAnalyzedSentence("schluß")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Schloß"))[0].getSuggestedReplacements().toString(), is("[Schloss]"));
    assertThat(rule.match(lt.getAnalyzedSentence("radfahren"))[0].getSuggestedReplacements().toString(), is("[Rad fahren]"));
    assertThat(rule.match(lt.getAnalyzedSentence("Photo"))[0].getSuggestedReplacements().toString(), is("[Foto]"));

    assertNoMatch("In Russland");
    assertNoMatch("In Russlands Weiten");
    assertNoMatch("Schlüsse");
    assertNoMatch("Schloß Holte");
    assertNoMatch("in Schloß Holte");
    assertNoMatch("Schloß Holte ist");
    assertNoMatch("Asse");
    assertNoMatch("Photons");  // not "Photo" substring match
    assertNoMatch("Photon");
    assertNoMatch("Des Photons");
    assertNoMatch("Photons ");
    assertNoMatch("Hallo Herr Naß");
    assertNoMatch("Hallo Hr. Naß");
    assertNoMatch("Hallo Frau Naß");
    assertNoMatch("Hallo Fr. Naß");
    assertNoMatch("Fr. Naß");
    assertNoMatch("Dr. Naß");
    assertNoMatch("Prof. Naß");
    assertNoMatch("Bell Telephone");
    assertNoMatch("Telephone Company");
    assertMatch("Naß ist das Wasser");
    assertMatch("Läßt du das bitte");
    assertNoMatch("Das mögliche Bestehenbleiben");
    assertNoMatch("Das mögliche Bloßstrampeln verhindern.");
    assertMatch("Bloßstrampeln konnte er sich nicht.");
  }

  private void assertMatch(String input) throws IOException {
    RuleMatch[] match = rule.match(lt.getAnalyzedSentence(input));
    assertThat(match.length, is(1));
  }

  private void assertNoMatch(String input) throws IOException {
    RuleMatch[] match = rule.match(lt.getAnalyzedSentence(input));
    if (match.length > 0) {
      fail("Unexpected match for '" + input + "': " + match[0]);
    }
  }

}
