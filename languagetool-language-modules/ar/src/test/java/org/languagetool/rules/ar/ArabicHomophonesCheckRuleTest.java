/*
 * LanguageTool, a natural language style checker
 * Copyright (C) 2020 Sohaib Afifi, Taha Zerrouki
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
package org.languagetool.rules.ar;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Arabic;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Taha Zerrouki
 * @since 5.0
 */
public class ArabicHomophonesCheckRuleTest {

  private ArabicHomophonesRule rule;
  private JLanguageTool langTool;

  @Before
  public void setUp() {
    rule = new ArabicHomophonesRule(TestTools.getEnglishMessages());
    langTool = new JLanguageTool(new Arabic());
  }

  @Test
  public void testRule() throws IOException {
    // incorrect sentences:
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("ضن")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("حاضر")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("حض")).length);

    // FIXME : AbstractSimpleReplaceRule2 doesn't support lemma checking
    // can find and replace words after stemming
    // the word الحاضر doesn't exist, but its lemma حاضر exists
    // assertEquals(1, rule.match(langTool.getAnalyzedSentence("الحاضر")).length);

  }

}
