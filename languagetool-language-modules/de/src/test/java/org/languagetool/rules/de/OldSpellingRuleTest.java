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

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class OldSpellingRuleTest {
  
  @Test
  public void test() throws IOException {
    Language german = Languages.getLanguageForShortCode("de");
    OldSpellingRule rule = new OldSpellingRule(JLanguageTool.getMessageBundle());
    JLanguageTool lt = new JLanguageTool(german);

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

    assertThat(rule.match(lt.getAnalyzedSentence("In Russland")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("In Russlands Weiten")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Schlüsse")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("in Schloß Holte")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Asse")).length, is(0));
  }

}
