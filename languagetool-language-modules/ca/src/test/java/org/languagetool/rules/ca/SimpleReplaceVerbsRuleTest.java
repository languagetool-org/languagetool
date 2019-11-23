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

package org.languagetool.rules.ca;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Catalan;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Jaume Ortolà
 */
public class SimpleReplaceVerbsRuleTest {

  private SimpleReplaceVerbsRule rule;
  private JLanguageTool langTool;

  @Before
  public void setUp() throws Exception {
    rule = new SimpleReplaceVerbsRule(TestTools.getMessages("ca"), new Catalan());
    langTool = new JLanguageTool(new Catalan());
  }

  @Test
  public void testRule() throws IOException {

    // correct sentences:

    // incorrect sentences:
    RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("abarca"));
    assertEquals(1, matches.length);
    assertEquals("abraça", matches[0].getSuggestedReplacements().get(0));
    assertEquals("abasta", matches[0].getSuggestedReplacements().get(1));
    assertEquals("agafa", matches[0].getSuggestedReplacements().get(2));
    assertEquals("estreny", matches[0].getSuggestedReplacements().get(3));
    assertEquals("comprèn", matches[0].getSuggestedReplacements().get(4));
    assertEquals("comprén", matches[0].getSuggestedReplacements().get(5));
    assertEquals("inclou", matches[0].getSuggestedReplacements().get(6));
    
    matches = rule.match(langTool.getAnalyzedSentence("abarcaven"));
    assertEquals(1, matches.length);
    assertEquals("abraçaven", matches[0].getSuggestedReplacements().get(0));
    assertEquals("abastaven", matches[0].getSuggestedReplacements().get(1));
    assertEquals("agafaven", matches[0].getSuggestedReplacements().get(2));
    assertEquals("estrenyien", matches[0].getSuggestedReplacements().get(3));
    assertEquals("comprenien", matches[0].getSuggestedReplacements().get(4));
    assertEquals("incloïen", matches[0].getSuggestedReplacements().get(5));
    
    matches = rule.match(langTool.getAnalyzedSentence("abarquéssim"));
    assertEquals(1, matches.length);
    assertEquals("abracéssim", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(langTool.getAnalyzedSentence("antojà"));
    assertEquals(1, matches.length);
    assertEquals("passà pel cap", matches[0].getSuggestedReplacements().get(0));
    assertEquals("passà pel magí", matches[0].getSuggestedReplacements().get(1));
    assertEquals("antullà", matches[0].getSuggestedReplacements().get(2));
    
    matches = rule.match(langTool.getAnalyzedSentence("alardeaven"));
    assertEquals(1, matches.length);
    assertEquals("feien gala", matches[0].getSuggestedReplacements().get(0));
    assertEquals("feien ostentació", matches[0].getSuggestedReplacements().get(1));
    
    matches = rule.match(langTool.getAnalyzedSentence("alardejo"));
    assertEquals(1, matches.length);
    assertEquals("faig gala", matches[0].getSuggestedReplacements().get(0));
    assertEquals("faig ostentació", matches[0].getSuggestedReplacements().get(1));  
    
    matches = rule.match(langTool.getAnalyzedSentence("aclares"));
    assertEquals(1, matches.length);
    assertEquals("aclareixes", matches[0].getSuggestedReplacements().get(0));
    assertEquals("aclarisques", matches[0].getSuggestedReplacements().get(1));  
    assertEquals("aclaresques", matches[0].getSuggestedReplacements().get(2));
    
    matches = rule.match(langTool.getAnalyzedSentence("atossigues"));
    assertEquals(1, matches.length);
    assertEquals("acuites", matches[0].getSuggestedReplacements().get(0));
    assertEquals("apresses", matches[0].getSuggestedReplacements().get(1));
    //assertEquals("dones pressa", matches[0].getSuggestedReplacements().get(2));
    assertEquals("dónes pressa", matches[0].getSuggestedReplacements().get(2));
    assertEquals("atabuixes", matches[0].getSuggestedReplacements().get(3));
    assertEquals("aclapares", matches[0].getSuggestedReplacements().get(4));
    assertEquals("afeixugues", matches[0].getSuggestedReplacements().get(5));
    assertEquals("mareges", matches[0].getSuggestedReplacements().get(6));
    assertEquals("afanyes", matches[0].getSuggestedReplacements().get(7));
    
    matches = rule.match(langTool.getAnalyzedSentence("agobiem"));
    assertEquals(1, matches.length);
    assertEquals("aclaparem", matches[0].getSuggestedReplacements().get(0));
    assertEquals("atabalem", matches[0].getSuggestedReplacements().get(1));  
    assertEquals("angoixem", matches[0].getSuggestedReplacements().get(2));
    assertEquals("estressem", matches[0].getSuggestedReplacements().get(3));
    assertEquals("(estar) molt a sobre", matches[0].getSuggestedReplacements().get(4));
    assertEquals("(cansar) molt", matches[0].getSuggestedReplacements().get(5));
    assertEquals("(ser) molt pesat", matches[0].getSuggestedReplacements().get(6));
    
    matches = rule.match(langTool.getAnalyzedSentence("agobiïs"));
    assertEquals(1, matches.length);
    assertEquals("aclaparis", matches[0].getSuggestedReplacements().get(0));
    assertEquals("atabalis", matches[0].getSuggestedReplacements().get(1));  
    assertEquals("angoixis", matches[0].getSuggestedReplacements().get(2));
    assertEquals("estressis", matches[0].getSuggestedReplacements().get(3));
    assertEquals("(estar) molt a sobre", matches[0].getSuggestedReplacements().get(4));
    assertEquals("(cansar) molt", matches[0].getSuggestedReplacements().get(5));
    assertEquals("(ser) molt pesat", matches[0].getSuggestedReplacements().get(6));
  }

}
