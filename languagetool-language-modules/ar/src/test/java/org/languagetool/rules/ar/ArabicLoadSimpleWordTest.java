/*
 * LanguageTool, a natural language style checker
 * Copyright (C) 2019 Sohaib Afifi, Taha Zerrouki
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
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.SimpleReplaceDataLoader;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Taha Zerrouki
 * @since 5.0
 */
public class ArabicLoadSimpleWordTest {

  private final JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("ar"));
  private static final String FILE_NAME = "/ar/arabic_masdar_verb.txt";

  @Before
  public void before() {
    TestTools.disableAllRulesExcept(lt, ArabicWordinessRule.AR_WORDINESS_REPLACE);
  }

  @Test
  public void testRule() throws IOException {
    assertNotNull(loadFromPath());
  }

  private void assertError(String s) throws IOException {
    ArabicWordinessRule rule = new ArabicWordinessRule(TestTools.getEnglishMessages());
    assertEquals(1, rule.match(lt.getAnalyzedSentence(s)).length);
  }

  protected static Map<String, List<String>> loadFromPath() {
    Map<String, List<String>> list = new SimpleReplaceDataLoader().loadWords(ArabicLoadSimpleWordTest.FILE_NAME);
    return list;
  }
}
