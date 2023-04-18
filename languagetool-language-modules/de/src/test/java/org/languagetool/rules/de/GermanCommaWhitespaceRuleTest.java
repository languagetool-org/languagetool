/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.Example;
import org.languagetool.tools.Tools;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class GermanCommaWhitespaceRuleTest {

  @Test
  public void testRule() throws IOException {
    GermanCommaWhitespaceRule rule = new GermanCommaWhitespaceRule(TestTools.getEnglishMessages(),
      Example.wrong("Die Partei<marker> ,</marker> die die letzte Wahl gewann."),
      Example.fixed("Die Partei<marker>,</marker> die die letzte Wahl gewann."),
      Tools.getUrl("http://fake"));
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("de"));
    assertThat(rule.match(lt.getAnalyzedSentence("Es gibt 5 Millionen .de-Domains.")).length, is(0));
  }

}