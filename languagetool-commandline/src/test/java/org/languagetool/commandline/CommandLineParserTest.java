/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.commandline;

import org.junit.Test;
import org.languagetool.JLanguageTool;

import static org.junit.Assert.*;

public class CommandLineParserTest {

  @Test
  public void testUsage() throws Exception {
    CommandLineParser parser = new CommandLineParser();
    try {
      parser.parseOptions(new String[]{});
      fail();
    } catch (WrongParameterNumberException ignored) {}

    CommandLineOptions commandLineOptions = parser.parseOptions(new String[]{"--help"});
    assertTrue(commandLineOptions.isPrintUsage());
  }

  @Test
  public void testErrors() throws Exception {
    CommandLineParser parser = new CommandLineParser();
    try {
      parser.parseOptions(new String[]{"--apply", "--taggeronly"});
      fail();
    } catch (IllegalArgumentException ignored) {}

    try {
      parser.parseOptions(new String[]{"--level"});
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Missing argument to --level command line option.", e.getMessage());
    }
  }

  @Test
  public void testSimple() throws Exception {
    CommandLineParser parser = new CommandLineParser();
    CommandLineOptions options;

    options = parser.parseOptions(new String[]{"filename.txt"});
    assertNull(options.getLanguage());
    assertEquals("filename.txt", options.getFilename());
    assertFalse(options.isVerbose());

    options = parser.parseOptions(new String[]{"--language", "xx", "filename.txt"});
    assertEquals("xx", options.getLanguage().getShortCode());
    assertEquals("filename.txt", options.getFilename());
    assertFalse(options.isVerbose());

    options = parser.parseOptions(new String[]{"-l", "xx", "filename.txt"});
    assertEquals("xx", options.getLanguage().getShortCode());
    assertEquals("filename.txt", options.getFilename());
    assertFalse(options.isVerbose());

    options = parser.parseOptions(new String[]{"-v", "-l", "xx", "filename.txt"});
    assertEquals("xx", options.getLanguage().getShortCode());
    assertEquals("filename.txt", options.getFilename());
    assertTrue(options.isVerbose());

    options = parser.parseOptions(new String[]{"--version"});
    assertTrue(options.isPrintVersion());

    options = parser.parseOptions(new String[]{"--list"});
    assertTrue(options.isPrintLanguages());
  }

  @Test
  public void testLongOptionList() throws Exception {
    CommandLineParser parser = new CommandLineParser();
    CommandLineOptions options = parser.parseOptions(new String[]{
            "--language", "xx",
            "--mothertongue", "xx",
            "--encoding", "utf-8",
            "--disable", "RULE_ONE,RULE_TWO",
            "--enable", "RULE_THREE",
            "--enablecategories", "CAT_ONE",
            "--disablecategories", "CAT_TWO",
            "--line-by-line",
            "--enable-temp-off",
            "--clean-overlapping",
            "--level", "PICKY",
            "filename.txt"
    });

    assertEquals("xx", options.getLanguage().getShortCode());
    assertEquals("xx", options.getMotherTongue().getShortCode());
    assertEquals("utf-8", options.getEncoding());
    assertEquals("filename.txt", options.getFilename());
    assertEquals(JLanguageTool.Level.PICKY, options.getLevel());
    assertEquals(2, options.getDisabledRules().size());
    assertEquals(1, options.getEnabledRules().size());
    assertEquals(1, options.getEnabledCategories().size());
    assertEquals(1, options.getDisabledCategories().size());
    assertTrue(options.isLineByLine());
    assertTrue(options.isEnableTempOff());
    assertTrue(options.isCleanOverlapping());
  }

}
