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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Catalan;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

/**
 * @author Jaume Ortolà
 */
public class SimpleReplaceVerbsRuleTest {

  private SimpleReplaceVerbsRule rule;
  private JLanguageTool lt;

  @BeforeEach
  public void setUp() throws Exception {
    rule = new SimpleReplaceVerbsRule(TestTools.getMessages("ca"), new Catalan());
    lt = new JLanguageTool(new Catalan());
  }

  @Test
  public void testRule() throws IOException {

    // incorrect sentences:
    RuleMatch[] matches;
    
    matches = rule.match(lt.getAnalyzedSentence("permanegué"));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("restà", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("estigué", matches[0].getSuggestedReplacements().get(1));
    Assertions.assertEquals("quedà", matches[0].getSuggestedReplacements().get(2));
    Assertions.assertEquals("romangué", matches[0].getSuggestedReplacements().get(3));
    
    matches = rule.match(lt.getAnalyzedSentence("permanesqué"));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("restà", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("estigué", matches[0].getSuggestedReplacements().get(1));
    Assertions.assertEquals("quedà", matches[0].getSuggestedReplacements().get(2));
    Assertions.assertEquals("romangué", matches[0].getSuggestedReplacements().get(3));
    
    matches = rule.match(lt.getAnalyzedSentence("permanéixer"));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("restar", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("estar", matches[0].getSuggestedReplacements().get(1));
    Assertions.assertEquals("quedar", matches[0].getSuggestedReplacements().get(2));
    Assertions.assertEquals("romandre", matches[0].getSuggestedReplacements().get(3));
    
    matches = rule.match(lt.getAnalyzedSentence("pringava"));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("enllardava", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("empastifava", matches[0].getSuggestedReplacements().get(1));
    Assertions.assertEquals("llepava", matches[0].getSuggestedReplacements().get(2));
    Assertions.assertEquals("cagava", matches[0].getSuggestedReplacements().get(3));
    Assertions.assertEquals("(haver begut oli)", matches[0].getSuggestedReplacements().get(4));
    Assertions.assertEquals("(tocar el rebre)", matches[0].getSuggestedReplacements().get(5));
    Assertions.assertEquals("(fotre's)", matches[0].getSuggestedReplacements().get(6));
    Assertions.assertEquals("(fer-se fotre)", matches[0].getSuggestedReplacements().get(7));
    
    matches = rule.match(lt.getAnalyzedSentence("abarca"));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("abraça", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("abasta", matches[0].getSuggestedReplacements().get(1));
    Assertions.assertEquals("comprèn", matches[0].getSuggestedReplacements().get(2));
    Assertions.assertEquals("comprén", matches[0].getSuggestedReplacements().get(3));
    Assertions.assertEquals("inclou", matches[0].getSuggestedReplacements().get(4));
    
    matches = rule.match(lt.getAnalyzedSentence("abarcaven"));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("abraçaven", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("abastaven", matches[0].getSuggestedReplacements().get(1));
    Assertions.assertEquals("comprenien", matches[0].getSuggestedReplacements().get(2));
    Assertions.assertEquals("incloïen", matches[0].getSuggestedReplacements().get(3));
    
    matches = rule.match(lt.getAnalyzedSentence("abarquéssim"));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("abracéssim", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(lt.getAnalyzedSentence("antojà"));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("passà pel cap", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("passà pel magí", matches[0].getSuggestedReplacements().get(1));
    Assertions.assertEquals("antullà", matches[0].getSuggestedReplacements().get(2));
    
    matches = rule.match(lt.getAnalyzedSentence("alardeaven"));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("feien gala", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("feien ostentació", matches[0].getSuggestedReplacements().get(1));
    
    matches = rule.match(lt.getAnalyzedSentence("alardejo"));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("faig gala", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("faig ostentació", matches[0].getSuggestedReplacements().get(1));  
    
    matches = rule.match(lt.getAnalyzedSentence("aclares"));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("aclareixes", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("aclarisques", matches[0].getSuggestedReplacements().get(1));  
    Assertions.assertEquals("aclaresques", matches[0].getSuggestedReplacements().get(2));
    
    matches = rule.match(lt.getAnalyzedSentence("atossigues"));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("acuites", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("apresses", matches[0].getSuggestedReplacements().get(1));
    Assertions.assertEquals("dones pressa", matches[0].getSuggestedReplacements().get(2));
    Assertions.assertEquals("dónes pressa", matches[0].getSuggestedReplacements().get(3));
    Assertions.assertEquals("atabuixes", matches[0].getSuggestedReplacements().get(4));
    Assertions.assertEquals("aclapares", matches[0].getSuggestedReplacements().get(5));
    Assertions.assertEquals("afeixugues", matches[0].getSuggestedReplacements().get(6));
    Assertions.assertEquals("mareges", matches[0].getSuggestedReplacements().get(7));
    Assertions.assertEquals("afanyes", matches[0].getSuggestedReplacements().get(8));
    
    matches = rule.match(lt.getAnalyzedSentence("agobiem"));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("aclaparem", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("atabalem", matches[0].getSuggestedReplacements().get(1));  
    Assertions.assertEquals("angoixem", matches[0].getSuggestedReplacements().get(2));
    Assertions.assertEquals("estressem", matches[0].getSuggestedReplacements().get(3));
    Assertions.assertEquals("(estar) molt a sobre", matches[0].getSuggestedReplacements().get(4));
    Assertions.assertEquals("(cansar) molt", matches[0].getSuggestedReplacements().get(5));
    Assertions.assertEquals("(ser) molt pesat", matches[0].getSuggestedReplacements().get(6));
    
    matches = rule.match(lt.getAnalyzedSentence("agobiïs"));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("aclaparis", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("atabalis", matches[0].getSuggestedReplacements().get(1));  
    Assertions.assertEquals("angoixis", matches[0].getSuggestedReplacements().get(2));
    Assertions.assertEquals("estressis", matches[0].getSuggestedReplacements().get(3));
    Assertions.assertEquals("(estar) molt a sobre", matches[0].getSuggestedReplacements().get(4));
    Assertions.assertEquals("(cansar) molt", matches[0].getSuggestedReplacements().get(5));
    Assertions.assertEquals("(ser) molt pesat", matches[0].getSuggestedReplacements().get(6));
    
    matches = rule.match(lt.getAnalyzedSentence("desabasteix"));
    Assertions.assertEquals("desproveeix", matches[0].getSuggestedReplacements().get(0));
    matches = rule.match(lt.getAnalyzedSentence("desabastíssim"));
    Assertions.assertEquals("desproveíssim", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(lt.getAnalyzedSentence("desabasta"));
    Assertions.assertEquals("desproveeix", matches[0].getSuggestedReplacements().get(0)); 
    
    matches = rule.match(lt.getAnalyzedSentence("sobresegueix"));
    Assertions.assertEquals("sobreseu", matches[0].getSuggestedReplacements().get(0));
    matches = rule.match(lt.getAnalyzedSentence("sobreseguir"));
    Assertions.assertEquals("sobreseure", matches[0].getSuggestedReplacements().get(0));

  }

}
