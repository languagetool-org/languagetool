/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2020 Fabian Richter
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

package org.languagetool.rules.es;

import org.junit.Test;
import org.languagetool.Language;
import org.languagetool.rules.RemoteRuleFilterTest;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class SpanishRemoteRuleFilterTest extends RemoteRuleFilterTest {

  @Test
  public void testRules() throws IOException {
    runGrammarRulesFromXmlTest();
  }

  @Override
  protected List<String> getGrammarFileNames(Language lang) {
    // no variant support included for now
    if (lang.isVariant()) {
      return Collections.emptyList();
    } else {
      return super.getGrammarFileNames(lang);
    }
  }
}
