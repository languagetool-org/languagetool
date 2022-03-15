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
package org.languagetool.rules.fr;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;
import org.languagetool.language.French;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

/**
 * @author Marcin Miłkowski
 */
public class QuestionWhitespaceStrictRuleTest {

    @Test
    public final void testRule() throws IOException {
      Language french = new French();
      QuestionWhitespaceStrictRule rule = new QuestionWhitespaceStrictRule(TestTools.getEnglishMessages(), french);
      RuleMatch[] matches;
      JLanguageTool lt = new JLanguageTool(french);
      
      // correct sentences:
      Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("C'est vrai !")).length);
      Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Qu'est ce que c'est ?")).length);
      Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("L'enjeu de ce livre est donc triple : philosophique")).length);
      Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Bonjour :)")).length);
      
      // errors:
      matches = rule.match(lt.getAnalyzedSentence("C'est vrai!"));
      Assertions.assertEquals(1, matches.length);
      matches = rule.match(lt.getAnalyzedSentence("C'est vrai !"));
      Assertions.assertEquals(1, matches.length);
      matches = rule.match(lt.getAnalyzedSentence("Qu'est ce que c'est ?"));
      Assertions.assertEquals(1, matches.length);
      matches = rule.match(lt.getAnalyzedSentence("Qu'est ce que c'est?"));
      Assertions.assertEquals(1, matches.length);
      matches = rule.match(lt.getAnalyzedSentence("L'enjeu de ce livre est donc triple: philosophique;"));
      Assertions.assertEquals(2, matches.length);
      Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("Bonjour : )")).length);
      matches = rule.match(lt.getAnalyzedSentence("L'enjeu de ce livre est donc triple: philosophique ;"));
      Assertions.assertEquals(2, matches.length);
      // check match positions:
      Assertions.assertEquals(2, matches.length);
      Assertions.assertEquals(29, matches[0].getFromPos());
      Assertions.assertEquals(36, matches[0].getToPos());
      Assertions.assertEquals("[triple\u00a0:]", matches[0].getSuggestedReplacements().toString());
      Assertions.assertEquals(50, matches[1].getFromPos());
      Assertions.assertEquals(52, matches[1].getToPos());
      Assertions.assertEquals("[\u202f;]", matches[1].getSuggestedReplacements().toString());
      //guillemets
      matches = rule.match(lt.getAnalyzedSentence("Le guillemet ouvrant est suivi d'un espace insécable : « mais le lieu [...] et le guillemet fermant est précédé d'un espace insécable : [...] littérature »."));
      Assertions.assertEquals(2, matches.length);
      matches = rule.match(lt.getAnalyzedSentence("Le guillemet ouvrant est suivi d'un espace insécable : «mais le lieu [...] et le guillemet fermant est précédé d'un espace insécable : [...] littérature»."));
      Assertions.assertEquals(2, matches.length);
    }
    
}

