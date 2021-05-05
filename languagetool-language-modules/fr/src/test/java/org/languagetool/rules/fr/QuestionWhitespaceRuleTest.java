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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Marcin Miłkowski
 */
public class QuestionWhitespaceRuleTest {

    @Test
    public final void testRule() throws IOException {
      Language french = new French();
      QuestionWhitespaceRule rule = new QuestionWhitespaceRule(TestTools.getEnglishMessages(), french);
      JLanguageTool lt = new JLanguageTool(french);
      
      // correct sentences:
      assertEquals(0, rule.match(lt.getAnalyzedSentence("C'est vrai !")).length);
      assertEquals(0, rule.match(lt.getAnalyzedSentence("Qu'est ce que c'est ?")).length);
      assertEquals(0, rule.match(lt.getAnalyzedSentence("L'enjeu de ce livre est donc triple : philosophique")).length);
      assertEquals(0, rule.match(lt.getAnalyzedSentence("Bonjour :)")).length);
      assertEquals(0, rule.match(lt.getAnalyzedSentence("5/08/2019 23:30")).length);
      assertEquals(0, rule.match(lt.getAnalyzedSentence("C'est vrai !!")).length);
      assertEquals(0, rule.match(lt.getAnalyzedSentence("C'est vrai ??")).length);
      assertEquals(0, rule.match(lt.getAnalyzedSentence("☀️9:00")).length);
      assertEquals(0, rule.match(lt.getAnalyzedSentence("00:80:41:ae:fd:7e")).length);

      TestTools.disableAllRulesExcept(lt, "FRENCH_WHITESPACE");
      assertEquals(0, lt.check("« Je suis Chris… »").size());
      assertEquals(0, lt.check("« Je suis Chris ! »").size());
      assertEquals(0, lt.check("Je suis.[!]").size());
      assertEquals(0, lt.check("Je suis[?]").size());
      assertEquals(0, lt.check("Je suis(?)").size());
      
      assertEquals(0, lt.check("1;2;3").size());
      assertEquals(0, lt.check("asd@dsa.fr;test@foo.com;").size());
      
      // errors:
      assertEquals(rule.match(lt.getAnalyzedSentence("C'est vrai!")).length, 1);
      assertEquals(rule.match(lt.getAnalyzedSentence("Qu'est ce que c'est?")).length, 1);
      assertEquals(rule.match(lt.getAnalyzedSentence("L'enjeu de ce livre est donc triple: philosophique;")).length, 2);
      
      RuleMatch[] matches1 = rule.match(lt.getAnalyzedSentence("L'enjeu de ce livre est donc triple: philosophique ;"));
      assertEquals(1, matches1.length);
      assertEquals(matches1[0].getFromPos(), 29);
      assertEquals(matches1[0].getToPos(), 36);
      assertEquals(matches1[0].getSuggestedReplacements().toString(), "[triple\u00a0:]");
      
      //guillemets:
      
      RuleMatch[] matches2 = rule.match(lt.getAnalyzedSentence("LanguageTool offre une «vérification» orthographique."));
      assertEquals(matches2.length, 1);
      assertEquals(matches2[0].getFromPos(), 23);
      assertEquals(matches2[0].getToPos(), 37);
      assertEquals(matches2[0].getSuggestedReplacements().toString(), "[«\u00a0vérification\u00a0»]");
      
      RuleMatch[] matches3 = rule.match(lt.getAnalyzedSentence("Le guillemet ouvrant est suivi d'un espace insécable : «mais le lieu [...] et le guillemet fermant est précédé d'un espace insécable : [...] littérature»."));
      assertEquals(matches3.length, 2);
      assertEquals(matches3[0].getSuggestedReplacements().toString(), "[«\u00a0mais]");
      assertEquals(matches3[0].getFromPos(), 55);
      assertEquals(matches3[0].getToPos(), 60);
      assertEquals(matches3[1].getSuggestedReplacements().toString(), "[littérature\u00a0»]");
      assertEquals(matches3[1].getFromPos(), 141);
      assertEquals(matches3[1].getToPos(), 153);
      
      
    }
    
}

