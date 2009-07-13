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
package de.danielnaber.languagetool.rules.es;

import java.io.IOException;

import junit.framework.TestCase;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.RuleMatch;

/**
 * @author Susana Sotelo Docio
 *
 * based on English tests
 */
public class ElwithFemRuleTest extends TestCase {

  public void testRule() throws IOException {
    ElwithFemRule rule = new ElwithFemRule(null);
    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(Language.SPANISH);
    // correct sentences:
    matches = rule.match(langTool.getAnalyzedSentence("El alma inmortal."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("Tomaré un agua."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("Usa mejor el hacha."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("Lo escondí bajo el haya."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("La foto del \"aura\" se la debo a él."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("Encontraron un ánfora ..."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("Ningún acta ..."));
    assertEquals(0, matches.length);
    // errors:
    matches = rule.match(langTool.getAnalyzedSentence("La alma inmortal."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("Tomaré una agua."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("Usa mejor la hacha."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("Lo escondí bajo la haya."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("La foto de la \"aura\" se la debo a él."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("Ninguna acta ..."));
    assertEquals(1, matches.length);
    // With uppercase letters:
    matches = rule.match(langTool.getAnalyzedSentence("En La Haya se vive muy bien."));
    assertEquals(0, matches.length);
    // With accented chars
    //matches = rule.match(langTool.getAnalyzedSentence("Encontraron una ánfora ..."));
    //assertEquals(1, matches.length);
  }
}
