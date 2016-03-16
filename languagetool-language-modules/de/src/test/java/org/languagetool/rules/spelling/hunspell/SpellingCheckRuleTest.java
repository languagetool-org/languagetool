/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.spelling.hunspell;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.de.GermanSpellerRule;
import org.languagetool.rules.spelling.SpellingCheckRule;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SpellingCheckRuleTest {

  @Test
  public void testIgnoreSuggestionsWithHunspell() throws IOException {
    JLanguageTool lt = new JLanguageTool(new GermanyGerman());

    assertThat(lt.check("Das ist ein einPseudoWortFürLanguageToolTests").size(), is(0));   // no error, as this word is in ignore.txt

    List<RuleMatch> matches = lt.check("Das ist ein Tibbfehla");
    assertThat(matches.size(), is(1));
    assertThat(matches.get(0).getRule().getId(), is(GermanSpellerRule.RULE_ID));
  }

  @Test
  public void testIgnorePhrases() throws IOException {
    JLanguageTool lt = new JLanguageTool(new GermanyGerman());
    assertThat(lt.check("Ein Test mit Auriensis Fantasiewortus").size(), is(2));
    for (Rule rule : lt.getAllActiveRules()) {
      if (rule instanceof SpellingCheckRule) {
        ((SpellingCheckRule) rule).acceptPhrases(Arrays.asList("Auriensis Fantasiewortus", "fudeldu laberwort"));
      } else {
        lt.disableRule(rule.getId());
      }
    }
    assertThat(lt.check("Ein Test mit Auriensis Fantasiewortus").size(), is(0));
    assertThat(lt.check("Ein Test mit Auriensis und Fantasiewortus").size(), is(2));  // the words on their own are not ignored
    assertThat(lt.check("fudeldu laberwort").size(), is(0));
    assertThat(lt.check("Fudeldu laberwort").size(), is(0));  // Uppercase at sentence start is okay
    assertThat(lt.check("Fudeldu Laberwort").size(), is(2));  // Different case somewhere other than at sentence start is not okay
  }

}
