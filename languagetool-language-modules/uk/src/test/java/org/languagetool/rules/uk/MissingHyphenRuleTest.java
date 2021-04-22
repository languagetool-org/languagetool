/* LanguageTool, a natural language style checker 
 * Copyright (C) 2018 Daniel Naber (http://www.danielnaber.de)
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

package org.languagetool.rules.uk;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tagging.uk.UkrainianTagger;


public class MissingHyphenRuleTest {
  private final JLanguageTool lt = new JLanguageTool(new Ukrainian());

  @Test
  public void testRule() throws IOException {
    MissingHyphenRule rule = new MissingHyphenRule(TestTools.getEnglishMessages(), new UkrainianTagger().getWordTagger());

    RuleMatch[] matches;

    // incorrect sentences:
    matches = rule.match(lt.getAnalyzedSentence("Поїхали у штаб квартиру."));
    assertEquals(1, matches.length);
    assertEquals(Arrays.asList("штаб-квартиру"), matches[0].getSuggestedReplacements());

    matches = rule.match(lt.getAnalyzedSentence("Роблю «тайм аут»"));
    assertEquals(1, matches.length);
    assertEquals(Arrays.asList("тайм-аут"), matches[0].getSuggestedReplacements());

    matches = rule.match(lt.getAnalyzedSentence("Такий компакт диск."));
    assertEquals(1, matches.length);
    assertEquals(Arrays.asList("компакт-диск"), matches[0].getSuggestedReplacements());

    // spelling 2019 

    matches = rule.match(lt.getAnalyzedSentence("Такий міні автомобіль."));
    assertEquals(1, matches.length);
    assertEquals(Arrays.asList("мініавтомобіль"), matches[0].getSuggestedReplacements());

    matches = rule.match(lt.getAnalyzedSentence("Арт проект вийшов провальним."));
    assertEquals(1, matches.length);
    assertEquals(Arrays.asList("Артпроект"), matches[0].getSuggestedReplacements());

    // TODO: hard - two errors, should be "ексвіцепрезидент"
    matches = rule.match(lt.getAnalyzedSentence("екс віце-президент"));
    assertEquals(1, matches.length);
    assertEquals(Arrays.asList("ексвіце-президент"), matches[0].getSuggestedReplacements());

    matches = rule.match(lt.getAnalyzedSentence("всі медіа півострова."));
    assertEquals(0, matches.length);

    
    // correct sentences:
    matches = rule.match(lt.getAnalyzedSentence("Тут все гаразд."));
    assertEquals(0, matches.length);

    matches = rule.match(lt.getAnalyzedSentence("Такий блок схемі не потрібен."));
    assertEquals(0, matches.length);

    matches = rule.match(lt.getAnalyzedSentence("конгрес Трансваалю."));
    assertEquals(0, matches.length);

    matches = rule.match(lt.getAnalyzedSentence("конгрес профспілок."));
    assertEquals(0, matches.length);

    matches = rule.match(lt.getAnalyzedSentence("рентген п'яти."));
    assertEquals(0, matches.length);

  }
}
