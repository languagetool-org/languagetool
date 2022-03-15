/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool;

import org.junit.jupiter.api.Test;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class AnnotatedTextBuilderTest {

  private final JLanguageTool ltGB =  new JLanguageTool(Languages.getLanguageForShortCode("en-GB"));

  @Test
  public void test() throws IOException {
    AnnotatedTextBuilder builder = new AnnotatedTextBuilder()
      .addText("This is a caf")
      .addMarkup("&eacute;", "é");
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("en-US"));
    List<RuleMatch> matches = lt.check(builder.build());
    assertThat(matches.size(), is(0));
  }

  /*
   * Test case for https://github.com/languagetool-org/languagetool/issues/2247
   */
  @Test
  public void testWithEmptyFakeContent() throws IOException {
    AnnotatedTextBuilder builder = new AnnotatedTextBuilder()
      .addText("And ths is ")
      .addMarkup("_", "");
    List<RuleMatch> matches = ltGB.check(builder.build());
    assertThat(matches.size(), is(1));
  }

}
