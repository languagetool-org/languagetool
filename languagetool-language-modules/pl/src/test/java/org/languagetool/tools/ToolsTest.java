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

import junit.framework.TestCase;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Polish;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class ToolsTest extends TestCase {

  private ByteArrayOutputStream out;
  private PrintStream stdout;
  private PrintStream stderr;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    this.stdout = System.out;
    this.stderr = System.err;
    this.out = new ByteArrayOutputStream();
    final ByteArrayOutputStream err = new ByteArrayOutputStream();      
    System.setOut(new PrintStream(this.out));
    System.setErr(new PrintStream(err));
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    System.setOut(this.stdout);
    System.setErr(this.stderr);
  }

  public void testCheck() throws IOException, ParserConfigurationException, SAXException {
    final JLanguageTool tool = new JLanguageTool(new Polish());
    tool.activateDefaultPatternRules();
    tool.activateDefaultFalseFriendRules();
 
    int matches = Tools.checkText("To jest całkowicie prawidłowe zdanie.", tool);
    String output = new String(this.out.toByteArray());
    assertEquals(0, output.indexOf("Time:"));
    assertEquals(0, matches);

    matches = Tools.checkText("To jest problem problem.", tool);
    output = new String(this.out.toByteArray());
    assertTrue(output.contains("Rule ID: WORD_REPEAT_RULE"));
    assertEquals(1, matches);
  }

  public void testCorrect() throws IOException, ParserConfigurationException, SAXException {
    JLanguageTool tool = new JLanguageTool(new Polish());
    tool.activateDefaultPatternRules();
    tool.activateDefaultFalseFriendRules();

    String correct = Tools.correctText("To jest całkowicie prawidłowe zdanie.", tool);
    assertEquals("To jest całkowicie prawidłowe zdanie.", correct);
    correct = Tools.correctText("To jest jest problem.", tool);
    assertEquals("To jest problem.", correct);

    // more sentences, need to apply more suggestions > 1 in subsequent sentences
    correct = Tools.correctText("To jest jest problem. Ale to już już nie jest problem.", tool);
    assertEquals("To jest problem. Ale to już nie jest problem.", correct);
    correct = Tools.correctText("To jest jest problem. Ale to już już nie jest problem. Tak sie nie robi. W tym zdaniu brakuje przecinka bo go zapomniałem.", tool);
    assertEquals("To jest problem. Ale to już nie jest problem. Tak się nie robi. W tym zdaniu brakuje przecinka, bo go zapomniałem.", correct);
  }
  
}
