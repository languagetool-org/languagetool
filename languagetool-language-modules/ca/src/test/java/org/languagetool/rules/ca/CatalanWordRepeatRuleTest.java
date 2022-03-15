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
import org.junit.jupiter.api.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Catalan;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

public class CatalanWordRepeatRuleTest {

  /*
   * Test method for 'org.languagetool.rules.ca.CatalanWordRepeatRule.match(AnalyzedSentence)'
   */
  @Test
  public void testRule() throws IOException {
    final CatalanWordRepeatRule rule = new CatalanWordRepeatRule(TestTools.getMessages("ca"), new Catalan());
    RuleMatch[] matches;
    JLanguageTool lt = new JLanguageTool(new Catalan());
    //correct
    matches = rule.match(lt.getAnalyzedSentence("Sempre pensa en en Joan."));
    Assertions.assertEquals(0, matches.length);
    matches = rule.match(lt.getAnalyzedSentence("Els els portaré aviat."));
    Assertions.assertEquals(0, matches.length);
    matches = rule.match(lt.getAnalyzedSentence("Maximilià I i Maria de Borgonya"));
    Assertions.assertEquals(0, matches.length);
    matches = rule.match(lt.getAnalyzedSentence("De la A a la z"));
    Assertions.assertEquals(0, matches.length);
    matches = rule.match(lt.getAnalyzedSentence("Entre I i II."));
    Assertions.assertEquals(0, matches.length);
    matches = rule.match(lt.getAnalyzedSentence("fills de Sigebert I i Brunegilda"));
    Assertions.assertEquals(0, matches.length);
    matches = rule.match(lt.getAnalyzedSentence("del segle I i del segle II"));
    Assertions.assertEquals(0, matches.length);
    matches = rule.match(lt.getAnalyzedSentence("entre el capítol I i el II"));
    Assertions.assertEquals(0, matches.length);
    matches = rule.match(lt.getAnalyzedSentence("cada una una casa"));
    Assertions.assertEquals(0, matches.length);
    matches = rule.match(lt.getAnalyzedSentence("cada un un llibre"));
    Assertions.assertEquals(0, matches.length);
    matches = rule.match(lt.getAnalyzedSentence("Si no no es gaudeix."));
    Assertions.assertEquals(0, matches.length);
    matches = rule.match(lt.getAnalyzedSentence("HUCHA-GANGA.ES es presenta."));
    Assertions.assertEquals(0, matches.length);
        
    //incorrect
    matches = rule.match(lt.getAnalyzedSentence("Tots els els homes són iguals."));
    Assertions.assertEquals(1, matches.length);
    matches = rule.match(lt.getAnalyzedSentence("Maximilià i i Maria de Borgonya"));
    Assertions.assertEquals(1, matches.length);
  }

}
