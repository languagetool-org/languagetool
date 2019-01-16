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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.AustrianGerman;

public class AustrianGermanSpellerRuleTest {

  private static final AustrianGerman DE_AT = new AustrianGerman();

  @Test
  public void testGetSuggestionsFromSpellingTxt() throws Exception {
    AustrianGermanSpellerRule rule = new AustrianGermanSpellerRule(TestTools.getEnglishMessages(), DE_AT, null, null);
    JLanguageTool lt = new JLanguageTool(DE_AT);
    assertThat(rule.match(lt.getAnalyzedSentence("Shopbewertung")).length, is(0));  // from spelling.txt
    assertThat(rule.match(lt.getAnalyzedSentence("Wahlzuckerl")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("Wahlzuckerls")).length, is(0));
    assertThat(rule.match(lt.getAnalyzedSentence("aifhdlidflifs")).length, is(1));
  }

}
