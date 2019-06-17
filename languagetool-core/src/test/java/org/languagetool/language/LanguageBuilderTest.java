/* LanguageTool, a natural language style checker 
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.language;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.*;

public class LanguageBuilderTest {

  @Test
  public void testMakeAdditionalLanguage() throws Exception {
    Language language = LanguageBuilder.makeAdditionalLanguage(new File("rules-xy-Fakelanguage.xml"));
    assertEquals("Fakelanguage", language.getName());
    assertEquals("xy", language.getShortCode());
    assertEquals(0, language.getRelevantRules(JLanguageTool.getMessageBundle(), null, null, Collections.emptyList()).size());
    assertTrue(language.isExternal());
  }

  @Test
  public void testIllegalFileName() throws Exception {
    try {
      LanguageBuilder.makeAdditionalLanguage(new File("foo"));
      fail();
    } catch (RuleFilenameException ignored) {}
  }
  
}
