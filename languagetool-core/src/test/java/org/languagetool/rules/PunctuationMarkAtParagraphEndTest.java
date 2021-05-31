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
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Fred Kruse
 */
public class PunctuationMarkAtParagraphEndTest {

  @Test
  public void testRule() throws IOException {
    JLanguageTool lt = new JLanguageTool(TestTools.getDemoLanguage());
    setUpRule(lt);

    assertEquals(0, lt.check("This is a test sentence.").size());
    assertEquals(0, lt.check("This is a test headline").size());
    assertEquals(0, lt.check("This is a test sentence. This is a link: http://example.com").size());  // no error because of colon
    assertEquals(1, lt.check("This is a test sentence. It can be found at http://example.com/foobar").size());
    assertEquals(1, lt.check("This is a test sentence. And this is a second test sentence").size());
    assertEquals(1, lt.check("\"This is a test sentence. And this is a second test sentence").size());
    assertEquals(0, lt.check("This is a test sentence. And this is a second test sentence.").size());
    assertEquals(0, lt.check("B. v. – Beschluss vom").size());
    assertEquals(1, 
        lt.check("This is a test sentence.\nAnd this is a second test sentence. Here is a dot missing").size());
    assertEquals(0, 
        lt.check("This is a test sentence.\nAnd this is a second test sentence. Here is a dot missing.").size());
    assertEquals(0, 
        lt.check("This is a sentence. Another one: https://languagetool.org/foo\n\nAnother sentence\n").size());
  }

  private void setUpRule(JLanguageTool lt) {
    for (Rule rule : lt.getAllRules()) {
      lt.disableRule(rule.getId());
    }
    PunctuationMarkAtParagraphEnd rule = new PunctuationMarkAtParagraphEnd(TestTools.getEnglishMessages(), TestTools.getDemoLanguage());
    rule.setTags(Collections.emptyList());
    lt.addRule(rule);
  }

}
