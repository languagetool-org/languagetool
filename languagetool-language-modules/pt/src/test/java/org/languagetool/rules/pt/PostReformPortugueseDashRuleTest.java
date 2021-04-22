/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (www.danielnaber.de)
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
package org.languagetool.rules.pt;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.is;

public class PostReformPortugueseDashRuleTest {

  @Test
  public void test() throws IOException {
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("pt-PT"));
    TestTools.disableAllRulesExcept(lt, "PT_POSAO_DASH_RULE");
    assertThat(lt.check("ab-reação").size(), is(0));
    assertThat(lt.check("ab—reação").size(), is(1));
  }
}
