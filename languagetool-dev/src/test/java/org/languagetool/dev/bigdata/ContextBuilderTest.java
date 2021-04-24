/* LanguageTool, a natural language style checker 
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.bigdata;

import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.language.English;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class ContextBuilderTest {

  private final ContextBuilder cb = new ContextBuilder();
  private final JLanguageTool lt = new JLanguageTool(new English());

  @Test
  public void testGetContext() throws IOException {
    check("And this is a test.",  3/*'is'*/, 1, "[this, is, a]");
    check("And this is a test.",  3/*'is'*/, 2, "[And, this, is, a, test]");
    check("And this is a test.",  3/*'is'*/, 3, "[_START_, And, this, is, a, test, .]");
    check("And this is a test.",  3/*'is'*/, 4, "[_START_, And, this, is, a, test, ., _END_]");

    check("This",  1, 0, "[This]");
    check("This",  1, 1, "[_START_, This, _END_]");
    check("This",  1, 2, "[_START_, This, _END_]");
  }

  private void check(String input, int pos, int contextSize, String expected) throws IOException {
    AnalyzedSentence sentence = lt.getAnalyzedSentence(input);
    List<String> context = cb.getContext(sentence.getTokensWithoutWhitespace(), pos, contextSize);
    assertThat(context.toString(), is(expected));
  }

}