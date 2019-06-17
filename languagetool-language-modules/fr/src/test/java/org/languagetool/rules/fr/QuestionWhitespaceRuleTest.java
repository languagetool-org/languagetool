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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;
import org.languagetool.language.French;
import org.languagetool.rules.RuleMatch;

/**
 * @author Marcin Miłkowski
 */
public class QuestionWhitespaceRuleTest {

    @Test
    public final void testRule() throws IOException {
      Language french = new French();
      QuestionWhitespaceRule rule = new QuestionWhitespaceRule(TestTools.getEnglishMessages(), french);
      RuleMatch[] matches;
      JLanguageTool langTool = new JLanguageTool(french);
      
      // correct sentences:
      assertEquals(0, rule.match(langTool.getAnalyzedSentence("C'est vrai !")).length);
      assertEquals(0, rule.match(langTool.getAnalyzedSentence("Qu'est ce que c'est ?")).length);
      assertEquals(0, rule.match(langTool.getAnalyzedSentence("L'enjeu de ce livre est donc triple : philosophique")).length);
      assertEquals(0, rule.match(langTool.getAnalyzedSentence("Bonjour :)")).length);
      
      // errors:
      matches = rule.match(langTool.getAnalyzedSentence("C'est vrai!"));
      assertEquals(1, matches.length);
      matches = rule.match(langTool.getAnalyzedSentence("Qu'est ce que c'est?"));
      assertEquals(1, matches.length);
      matches = rule.match(langTool.getAnalyzedSentence("L'enjeu de ce livre est donc triple: philosophique;"));
      assertEquals(2, matches.length);
      matches = rule.match(langTool.getAnalyzedSentence("L'enjeu de ce livre est donc triple: philosophique ;"));
      assertEquals(1, matches.length);
      // check match positions:
      assertEquals(29, matches[0].getFromPos());
      assertEquals(36, matches[0].getToPos());
      //guillemets
      matches = rule.match(langTool.getAnalyzedSentence("Le guillemet ouvrant est suivi d'un espace insécable : «mais le lieu [...] et le guillemet fermant est précédé d'un espace insécable : [...] littérature»."));
      assertEquals(1, matches.length);
    }
    
}

