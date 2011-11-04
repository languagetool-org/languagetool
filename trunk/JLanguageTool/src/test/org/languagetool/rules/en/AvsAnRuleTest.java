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
package de.danielnaber.languagetool.rules.en;

import java.io.IOException;

import junit.framework.TestCase;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.RuleMatch;

/**
 * @author Daniel Naber
 */
public class AvsAnRuleTest extends TestCase {

  public void testRule() throws IOException {
    AvsAnRule rule = new AvsAnRule(null);
    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(Language.ENGLISH);
    // correct sentences:
    matches = rule.match(langTool.getAnalyzedSentence("This is a test sentence."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("It was an hour ago."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("A university is ..."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("A one-way street ..."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("An hour's work ..."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("Going to an \"industry party\"."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("An 8-year old boy ..."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("An 18-year old boy ..."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("The A-levels are ..."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("An NOP check ..."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("A USA-wide license ..."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("...asked a UN member."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("In an un-united Germany..."));
    assertEquals(0, matches.length);
    
    //fixed false alarms:
    matches = rule.match(langTool.getAnalyzedSentence("Here, a and b are supplementary angles."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("The Qur'an was translated into Polish."));
    assertEquals(0, matches.length);

    // errors:
    matches = rule.match(langTool.getAnalyzedSentence("It was a hour ago."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("It was an sentence that's long."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("It was a uninteresting talk."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("An university"));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("A unintersting ..."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("It was a uninteresting talk with an long sentence."));
    assertEquals(2, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("A hour's work ..."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("Going to a \"industry party\"."));
    assertEquals(1, matches.length);
    // With uppercase letters:
    matches = rule.match(langTool.getAnalyzedSentence("A University"));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("A Europe wide something"));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("then an University sdoj fixme sdoopsd"));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("A 8-year old boy ..."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("A 18-year old boy ..."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("...asked an UN member."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("In a un-united Germany..."));
    assertEquals(1, matches.length);

    //Test on acronyms/initials:
    matches = rule.match(langTool.getAnalyzedSentence("A. R.J. Turgot"));
    assertEquals(0, matches.length);
    
    //mixed case as dictionary-based exception
    matches = rule.match(langTool.getAnalyzedSentence("Anyone for an MSc?"));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("Anyone for a MSc?"));
    assertEquals(1, matches.length);
    //mixed case from general case
    matches = rule.match(langTool.getAnalyzedSentence("Anyone for an XMR-based writer?"));
    assertEquals(0, matches.length);
    
    //Test on apostrophes    
    matches = rule.match(langTool.getAnalyzedSentence("Its name in English is a[1] (), plural A's, As, as, or a's."));
    assertEquals(0, matches.length);    
  }
    
  public void testSuggestions() throws IOException {
    AvsAnRule rule = new AvsAnRule(null);
    assertEquals("a string", rule.suggestAorAn("string"));
    assertEquals("a university", rule.suggestAorAn("university"));
    assertEquals("an hour", rule.suggestAorAn("hour"));
    assertEquals("an all-terrain", rule.suggestAorAn("all-terrain"));    
    assertEquals("a UNESCO", rule.suggestAorAn("UNESCO"));
    
  }
  
  public void testPositions() throws IOException {
    AvsAnRule rule = new AvsAnRule(null);
    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(Language.ENGLISH);
    // no quotes etc.:
    matches = rule.match(langTool.getAnalyzedSentence("a industry standard."));
    assertEquals(0, matches[0].getFromPos());
    assertEquals(1, matches[0].getToPos());
    
    // quotes..
    matches = rule.match(langTool.getAnalyzedSentence("a \"industry standard\"."));
    assertEquals(0, matches[0].getFromPos());
    assertEquals(1, matches[0].getToPos());
    
    matches = rule.match(langTool.getAnalyzedSentence("a - industry standard\"."));
    assertEquals(0, matches[0].getFromPos());
    assertEquals(1, matches[0].getToPos());
    
    matches = rule.match(langTool.getAnalyzedSentence("This is a \"industry standard\"."));
    assertEquals(8, matches[0].getFromPos());
    assertEquals(9, matches[0].getToPos());
    
    matches = rule.match(langTool.getAnalyzedSentence("\"a industry standard\"."));
    assertEquals(1, matches[0].getFromPos());
    assertEquals(2, matches[0].getToPos());
    
    matches = rule.match(langTool.getAnalyzedSentence("\"Many say this is a industry standard\"."));
    assertEquals(18, matches[0].getFromPos());
    assertEquals(19, matches[0].getToPos());
    
    matches = rule.match(langTool.getAnalyzedSentence("Like many \"an desperado\" before him, Bart headed south into Mexico."));
    assertEquals(11, matches[0].getFromPos());
    assertEquals(13, matches[0].getToPos());
    
  }
}
