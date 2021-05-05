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
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.TestTools;

import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class GermanWordRepeatRuleTest {
  
  @Test
  public void testRule() throws IOException {
    Language german = Languages.getLanguageForShortCode("de");
    GermanWordRepeatRule rule = new GermanWordRepeatRule(TestTools.getEnglishMessages(), german);
    JLanguageTool lt = new JLanguageTool(german);
    assertThat(rule.match(lt.getAnalyzedSentence("Das ist gut so.")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Das ist ist gut so.")).length, is(1));
    assertThat(rule.match(lt.getAnalyzedSentence("Der der Mann")).length, is(1));
    assertThat(rule.match(lt.getAnalyzedSentence("Warum fragen Sie sie nicht selbst?")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Er will nur sein Leben leben.")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Wie bei Honda, die die Bezahlung erhöht haben.")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Dann warfen sie sie weg.")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Dann konnte sie sie sehen.")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Er muss sein Essen essen.")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Wahrscheinlich ist das das Problem.")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Dann wäre das das erste Wirtschaftsmagazin mit mehr als 10.000 Lesern.")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Für mich war das das Härteste.")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Ich weiß, wer wer ist!")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Als ich das das erste Mal gehört habe …")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Hat sie sie")).length, is(1));  // used to crash, issue #1010
    assertThat(rule.match(lt.getAnalyzedSentence("Hat hat")).length, is(1));
    assertThat(rule.match(lt.getAnalyzedSentence("hat hat")).length, is(1));
  }

}