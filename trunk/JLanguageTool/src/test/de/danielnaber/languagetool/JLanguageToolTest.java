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
package de.danielnaber.languagetool;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.danielnaber.languagetool.rules.Rule;

import junit.framework.TestCase;

/**
 * @author Daniel Naber
 */
public class JLanguageToolTest extends TestCase {

  public void testEnglish() throws IOException, ParserConfigurationException, SAXException {
    JLanguageTool tool = new JLanguageTool(Language.ENGLISH);
    List matches = tool.check("A test that should not give errors.");
    assertEquals(0, matches.size());
    matches = tool.check("A test test that should give errors.");
    assertEquals(1, matches.size());
    matches = tool.check("I can give you more a detailed description.");
    assertEquals(0, matches.size());
    assertEquals(5, tool.getAllRules().size());
    List rules = tool.loadPatternRules("rules/en/grammar.xml");
    for (Iterator iter = rules.iterator(); iter.hasNext();) {
      Rule rule = (Rule) iter.next();
      tool.addRule(rule);
    }
    assertTrue(tool.getAllRules().size() > 3);
    matches = tool.check("I can give you more a detailed description.");
    assertEquals(1, matches.size());
    tool.disableRule("MORE_A_JJ");
    matches = tool.check("I can give you more a detailed description.");
    assertEquals(0, matches.size());
  }
  
  public void testGerman() throws IOException, ParserConfigurationException, SAXException {
    JLanguageTool tool = new JLanguageTool(Language.GERMAN);
    List matches = tool.check("Ein Test, der keine Fehler geben sollte.");
    assertEquals(0, matches.size());
    matches = tool.check("Ein Test Test, der Fehler geben sollte.");
    assertEquals(1, matches.size());
    List rules = tool.loadPatternRules("rules/de/grammar.xml");
    for (Iterator iter = rules.iterator(); iter.hasNext();) {
      Rule rule = (Rule) iter.next();
      tool.addRule(rule);
    }
    // German rule has no effect with English error:
    matches = tool.check("I can give you more a detailed description");
    assertEquals(0, matches.size());
  }
  
  public void testCountLines() {
    assertEquals(0, JLanguageTool.countLineBreaks(""));
    assertEquals(1, JLanguageTool.countLineBreaks("Hallo,\nnächste Zeile"));
    assertEquals(2, JLanguageTool.countLineBreaks("\nZweite\nDritte"));
    assertEquals(4, JLanguageTool.countLineBreaks("\nZweite\nDritte\n\n"));
  }

}
