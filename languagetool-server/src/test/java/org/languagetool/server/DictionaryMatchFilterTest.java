/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2018 Fabian Richter
 *  *
 *  * This library is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU Lesser General Public
 *  * License as published by the Free Software Foundation; either
 *  * version 2.1 of the License, or (at your option) any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this library; if not, write to the Free Software
 *  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  * USA
 *
 */

package org.languagetool.server;

import org.jetbrains.annotations.NotNull;
import org.languagetool.Languages;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DictionaryMatchFilterTest extends UserDictTest {
  private static final String TEST_SENTENCE = "I wonder if this errors are easy to fix.";

  @NotNull
  @Override
  protected HTTPServerConfig getServerConfig() {
    HTTPServerConfig config = super.getServerConfig();
    config.setPipelineCaching(true);
    config.setPipelineExpireTime(300);
    return config;
  }

  @Override
  protected void run() throws Exception {
    // order of checks + parameters is relevant because of enabled ResultCache
    assertTrue(matched(null, null, false));
    assertTrue(matched(null, null, true));
    assertTrue(matched(USERNAME1, API_KEY1, true));
    assertTrue(matched(USERNAME1, API_KEY1, false));
    assertTrue(matched(USERNAME2, API_KEY2, false));
    assertTrue(matched(USERNAME2, API_KEY2, true));

    addWord("this", USERNAME1, API_KEY1);
    assertTrue(matched(USERNAME1, API_KEY1, false));
    assertFalse(matched(USERNAME1, API_KEY1, true));
    assertTrue(matched(USERNAME2, API_KEY2, true));
    assertTrue(matched(USERNAME2, API_KEY2, false));

    addWord("this", USERNAME2, API_KEY2);
    assertFalse(matched(USERNAME2, API_KEY2, true));
    assertTrue(matched(USERNAME2, API_KEY2, false));
  }

  private boolean matched(String user, String key, boolean filterDictionaryMatches) throws IOException {
    String option = "&filterDictionaryMatches=" + filterDictionaryMatches;
    String json = check(Languages.getLanguageForShortCode("en"), TEST_SENTENCE, user, key, option);
    return json.contains("THIS_NNS");
  }
}
