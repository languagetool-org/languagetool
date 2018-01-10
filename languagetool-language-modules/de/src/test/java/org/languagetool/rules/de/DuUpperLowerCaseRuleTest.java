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
package org.languagetool.rules.de;

import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.German;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class DuUpperLowerCaseRuleTest {

  private final DuUpperLowerCaseRule rule = new DuUpperLowerCaseRule(TestTools.getEnglishMessages());
  private final JLanguageTool lt = new JLanguageTool(new German());

  @Test
  public void testRule() throws IOException {
    assertErrors("Du bist noch jung.", 0);
    assertErrors("Du bist noch jung, du bist noch fit.", 0);
    assertErrors("Aber du bist noch jung, du bist noch fit.", 0);
    assertErrors("Aber du bist noch jung, dir ist das egal.", 0);

    assertErrors("Aber Du bist noch jung, du bist noch fit.", 1);
    assertErrors("Aber Du bist noch jung, dir ist das egal.", 1);
    assertErrors("Aber Du bist noch jung. Und dir ist das egal.", 1);
    
    assertErrors("Aber du bist noch jung. Und Du bist noch fit.", 1);
    assertErrors("Aber du bist noch jung, Dir ist das egal.", 1);
    assertErrors("Aber du bist noch jung. Und Dir ist das egal.", 1);
  }

  private void assertErrors(String input, int expectedMatches) throws IOException {
    AnalyzedSentence sentence = lt.getAnalyzedSentence(input);
    RuleMatch[] matches = rule.match(Collections.singletonList(sentence));
    assertThat("Expected " + expectedMatches + ", got " + matches.length + ": " + sentence.getText() + " -> " + Arrays.toString(matches),
               matches.length, is(expectedMatches));
  }

}