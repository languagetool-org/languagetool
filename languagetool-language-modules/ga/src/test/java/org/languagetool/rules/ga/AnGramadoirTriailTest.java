/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.ga;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Irish;
import org.languagetool.rules.RuleMatch;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AnGramadoirTriailTest {
  private JLanguageTool langTool;
  private List<TriailError> errors;
  final String TRIAIL_XML = "org/languagetool/resource/ga/triail.xml";
  ClassLoader cl;
  AnGramadoirTriailData data;
  InputStream in;
  BufferedReader br;

  @Before
  public void setUp() throws IOException {
    langTool = new JLanguageTool(new Irish());
    cl = this.getClass().getClassLoader();
    in = cl.getResourceAsStream(TRIAIL_XML);

    data = new AnGramadoirTriailData(in);
    errors = data.getErrors();
  }
  @Test
  public void testTriailData() throws IOException {
    in = cl.getResourceAsStream(TRIAIL_XML);
    assert(in != null);
    byte[] buf = new byte[5];
    assertEquals(5, in.read(buf, 0, 5));
    assertEquals("<?xml", new String(buf, StandardCharsets.UTF_8));

    assert(data != null);
    assert(errors != null);
  }

  @Test
  public void testCheckTrial() throws IOException {
    int numerrors = 0;
    int nummatches = 0;
    for(TriailError te : errors) {
      numerrors++;
      System.err.println(te.getContext());
      List<RuleMatch> matches = langTool.check(te.getContext());
      for(RuleMatch match : matches) {
        if(match.getFromPos() == te.getContextOffset() && match.getToPos() == te.getErrorLength()) {
          nummatches++;
        }
      }
    }
    //assertEquals(numerrors, nummatches);
  }
}
