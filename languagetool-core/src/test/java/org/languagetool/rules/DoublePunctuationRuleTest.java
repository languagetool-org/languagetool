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
package org.languagetool.rules;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Daniel Naber
 */
public class DoublePunctuationRuleTest {

  @Test
  public void testRule() throws IOException {
    DoublePunctuationRule rule = new DoublePunctuationRule(TestTools.getEnglishMessages());
    JLanguageTool lt = new JLanguageTool(TestTools.getDemoLanguage());
    
    // correct sentences:
    assertEquals(0, rule.match(lt.getAnalyzedSentence("This is a test sentence...")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Это тестовое предложение?..")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Это тестовое предложение!.. ")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("This is a test sentence... More stuff....")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("This is a test sentence..... More stuff....")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("This, is, a test sentence.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("The path is ../data/vtest.avi")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("The path is ..\\data\\vtest.avi")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Something … … ..")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Something … … ... …")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Something … … .... …")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Something … … .. …")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Something ……..")).length);

    // errors:
    RuleMatch[] matches1 = rule.match(lt.getAnalyzedSentence("This,, is a test sentence."));
    assertEquals(1, matches1.length);
    assertEquals(4, matches1[0].getFromPos());
    assertEquals(6, matches1[0].getToPos());
    RuleMatch[] matches2 = rule.match(lt.getAnalyzedSentence("This is a test sentence.. Another sentence"));
    assertEquals(1, matches2.length);
    assertEquals(23, matches2[0].getFromPos());
    assertEquals(25, matches2[0].getToPos());
  }
  
}
