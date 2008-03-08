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
import java.util.List;

import junit.framework.TestCase;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.rules.patterns.PatternRule;

/**
 * @author Daniel Naber
 */
public class JLanguageToolTest extends TestCase {

  // used on http://www.languagetool.org/usage/
  /*
  public void testDemo() throws IOException {
    JLanguageTool langTool = new JLanguageTool(Language.ENGLISH);
    langTool.activateDefaultPatternRules();
    List<RuleMatch> matches = langTool.check("A sentence " + 
        "with a error in the Hitchhiker's Guide tot he Galaxy");
    for (RuleMatch match : matches) {
      System.out.println("Potential error at line " +
          match.getEndLine() + ", column " +
          match.getColumn() + ": " + match.getMessage());
      System.out.println("Suggested correction: " +
          match.getSuggestedReplacements());
    }
  }
  */
  
  
  public void testEnglish() throws IOException {
    final JLanguageTool tool = new JLanguageTool(Language.ENGLISH);
    List<RuleMatch> matches = tool.check("A test that should not give errors.");
    assertEquals(0, matches.size());
    matches = tool.check("A test test that should give errors.");
    assertEquals(1, matches.size());
    matches = tool.check("I can give you more a detailed description.");
    assertEquals(0, matches.size());
    assertEquals(6, tool.getAllRules().size());
    final List<PatternRule> rules = tool.loadPatternRules("/rules/en/grammar.xml");
    for (PatternRule patternRule : rules) {
      tool.addRule(patternRule);
    }
    assertTrue(tool.getAllRules().size() > 3);
    matches = tool.check("I can give you more a detailed description.");
    assertEquals(1, matches.size());
    tool.disableRule("MORE_A_JJ");
    matches = tool.check("I can give you more a detailed description.");
    assertEquals(0, matches.size());
    tool.disableCategory("Possible Typo");
    matches = tool.check("I've go to go.");
    assertEquals(0, matches.size());
  }
  
  public void testGerman() throws IOException {
    final JLanguageTool tool = new JLanguageTool(Language.GERMAN);
    List<RuleMatch> matches = tool.check("Ein Test, der keine Fehler geben sollte.");
    assertEquals(0, matches.size());
    matches = tool.check("Ein Test Test, der Fehler geben sollte.");
    assertEquals(1, matches.size());
    final List<PatternRule> rules = tool.loadPatternRules("/rules/de/grammar.xml");
    for (PatternRule patternRule : rules) {
      tool.addRule(patternRule);
    }
    // German rule has no effect with English error:
    matches = tool.check("I can give you more a detailed description");
    assertEquals(0, matches.size());
  }

  public void testDutch() throws IOException {
    final JLanguageTool tool = new JLanguageTool(Language.DUTCH);
    List<RuleMatch> matches = tool.check("Een test, die geen fouten mag geven.");
    assertEquals(0, matches.size());
    matches = tool.check("Een test test, die een fout moet geven.");
    assertEquals(1, matches.size());
    final List<PatternRule> rules = tool.loadPatternRules("/rules/nl/grammar.xml");
    for (PatternRule patternRule : rules) {
      tool.addRule(patternRule);
    }
    // Dutch rule has no effect with English error:
    matches = tool.check("I can give you more a detailed description");
    assertEquals(0, matches.size());
  }
  
  public void testPolish() throws IOException {
    final JLanguageTool tool = new JLanguageTool(Language.POLISH);
    List<RuleMatch> matches = tool.check("To jest całkowicie prawidłowe zdanie.");
    assertEquals(0, matches.size());
    matches = tool.check("To jest jest problem.");
    assertEquals(1, matches.size());
    //this rule is by default off
    matches = tool.check("Był on bowiem pięknym strzelcem bowiem.");
    assertEquals(0, matches.size());
    tool.enableDefaultOffRule("PL_WORD_REPEAT");
    matches = tool.check("Był on bowiem pięknym strzelcem bowiem.");
    assertEquals(1, matches.size());
    final List<PatternRule> rules = tool.loadPatternRules("/rules/pl/grammar.xml");
    for (final PatternRule rule : rules) {
      tool.addRule(rule);
    }
    matches = tool.check("Premier drapie się w ucho co i rusz.");
    assertEquals(1, matches.size());
    // Polish rule has no effect with English error:
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
