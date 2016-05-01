/* LanguageTool, a natural language style checker 
 * Copyright (C) 2009 Marcin Miłkowski (http://www.languagetool.org)
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
package org.languagetool.tools;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.English;
import org.languagetool.language.Polish;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.bitext.BitextRule;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ToolsTest {

  private PrintStream stdout;
  private PrintStream stderr;

  @Before
  public void setUp() throws Exception {
    this.stdout = System.out;
    this.stderr = System.err;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));
    System.setErr(new PrintStream(err));
  }

  @After
  public void tearDown() throws Exception {
    System.setOut(this.stdout);
    System.setErr(this.stderr);
  }

  @Test
  public void testBitextCheck() throws IOException, ParserConfigurationException, SAXException {
    English english = new English();
    JLanguageTool srcTool = new JLanguageTool(english);
    Polish polish = new Polish();
    JLanguageTool trgTool = new JLanguageTool(polish);

    List<BitextRule> rules = Tools.getBitextRules(english, polish);
    
    int matches1 = Tools.checkBitext(
        "This is a perfectly good sentence.",
        "To jest całkowicie prawidłowe zdanie.", srcTool, trgTool, rules).size();
    assertEquals(0, matches1);

    List<RuleMatch> matches = Tools.checkBitext(
            "This is not actual.",
            "To nie jest aktualne.",
            srcTool, trgTool, rules);
    assertEquals(1, matches.size());
    assertTrue(matches.get(0).getRule().getId().equals("ACTUAL"));
  }
}
