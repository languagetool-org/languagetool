/* LanguageTool, a natural language style checker 
 * Copyright (C) 2008 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.rules;

import java.io.IOException;

import junit.framework.TestCase;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.TestTools;

public class UnpairedQuotesBracketsRuleTest extends TestCase {

  public void testRule() throws IOException {
    UnpairedQuotesBracketsRule rule = new UnpairedQuotesBracketsRule(TestTools.getEnglishMessages(), Language.ENGLISH);
    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(Language.ENGLISH);
    // correct sentences:
    matches = rule.match(langTool.getAnalyzedSentence("(This is a test sentence)."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("This is a word 'test'."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("This is the joint presidents' declaration."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("The screen is 20\" wide."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("This is a [test] sentence..."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("The plight of Tamil refugees caused a surge of support from most of the Tamil political parties.[90]"));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("(([20] [20] [20]))"));
    assertEquals(0, matches.length);
    // numerical bullets
    matches = rule.match(langTool.getAnalyzedSentence("We discussed this in Chapter 1)."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("We discussed this in section 1a)."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("We discussed this in section iv)."));
    assertEquals(0, matches.length);
    // incorrect sentences:
    matches = rule.match(langTool.getAnalyzedSentence("(This is a test sentence."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("(This is a test” sentence."));
    assertEquals(2, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("This is a {test sentence."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("This [is (a test} sentence."));
    assertEquals(3, matches.length);
  }
  
  public void testRuleGerman() throws IOException {
    UnpairedQuotesBracketsRule rule = new UnpairedQuotesBracketsRule(TestTools.getEnglishMessages(), Language.GERMAN);
    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(Language.GERMAN);
    // correct sentences:
    matches = rule.match(langTool.getAnalyzedSentence("(Das sind die Sätze, die die testen sollen)."));
    assertEquals(0, matches.length);
    // incorrect sentences:
    matches = rule.match(langTool.getAnalyzedSentence("Die „Sätze zum testen."));
    assertEquals(1, matches.length);
  }
  
  public void testRulePolish() throws IOException {
    UnpairedQuotesBracketsRule rule = new UnpairedQuotesBracketsRule(TestTools.getEnglishMessages(), Language.POLISH);
    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(Language.POLISH);
    // correct sentences:
    matches = rule.match(langTool.getAnalyzedSentence("(To jest zdanie do testowania)."));
    assertEquals(0, matches.length);
    // correct sentences:
    matches = rule.match(langTool.getAnalyzedSentence("Piosenka ta trafiła na wiele list \"Best of...\", włączając w to te, które zostały utworzone przez magazyn Rolling Stone."));
    assertEquals(0, matches.length);
    // incorrect sentences:
    matches = rule.match(langTool.getAnalyzedSentence("W tym zdaniu jest niesparowany „cudzysłów."));
    assertEquals(1, matches.length);
  }
  
  public void testRuleSpanish() throws IOException {
    UnpairedQuotesBracketsRule rule = new UnpairedQuotesBracketsRule(TestTools.getEnglishMessages(), Language.SPANISH);
    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(Language.SPANISH);
    // correct sentences:
    matches = rule.match(langTool.getAnalyzedSentence("Soy un hombre (muy honrado)."));
    assertEquals(0, matches.length);
    // incorrect sentences:
    matches = rule.match(langTool.getAnalyzedSentence("De dónde vas?"));
    assertEquals(1, matches.length);    
    matches = rule.match(langTool.getAnalyzedSentence("¡Atención"));
    assertEquals(1, matches.length);
  }

  public void testRuleFrench() throws IOException {
    UnpairedQuotesBracketsRule rule = new UnpairedQuotesBracketsRule(TestTools.getEnglishMessages(), Language.FRENCH);
    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(Language.FRENCH);
    // correct sentences:
    matches = rule.match(langTool.getAnalyzedSentence("(Qu'est ce que c'est ?)"));
    assertEquals(0, matches.length);
    // incorrect sentences:
    matches = rule.match(langTool.getAnalyzedSentence("(Qu'est ce que c'est ?"));
    assertEquals(1, matches.length);        
  }  
  
  public void testRuleDutch() throws IOException {
    UnpairedQuotesBracketsRule rule = new UnpairedQuotesBracketsRule(TestTools.getEnglishMessages(), Language.DUTCH);
    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(Language.DUTCH);
    // correct sentences:
    matches = rule.match(langTool.getAnalyzedSentence("Het centrale probleem van het werk is de ‘dichterlijke kuischheid’."));
    assertEquals(0, matches.length);
    // incorrect sentences:
    matches = rule.match(langTool.getAnalyzedSentence("Het centrale probleem van het werk is de ‘dichterlijke kuischheid."));
    assertEquals(1, matches.length);        
  }
  
}
