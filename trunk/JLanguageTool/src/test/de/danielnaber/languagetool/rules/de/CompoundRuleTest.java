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
package de.danielnaber.languagetool.rules.de;

import java.io.IOException;

import junit.framework.TestCase;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;

/**
 * @author Daniel Naber
 */
public class CompoundRuleTest extends TestCase {

  private JLanguageTool langTool;
  private CompoundRule rule;
  
  public void testRule() throws IOException {
    langTool = new JLanguageTool(Language.GERMAN);
    rule = new CompoundRule(null);
    // correct sentences:
    check(0, "Eine tolle CD-ROM.");
    check(0, "Ein toller CD-ROM-Test.");
    check(0, "Systemadministrator");
    check(0, "System-Administrator");
    check(0, "Eine Million Dollar");
    check(0, "Das System des Administrators");
    check(0, "Nur im Stand-by-Betrieb");
    // incorrect sentences:
    check(1, "System Administrator");
    check(1, "Der System Administrator");
    check(1, "Der dumme System Administrator");
    check(2, "Der dumme System Administrator legt die CD ROM ein");
    check(1, "Nur im Stand by Betrieb");
    // TODO: properly support compounds with more than 2 parts:
    //assertEquals(1, rule.match(langTool.getAnalyzedSentence("Ein echter Start Ziel Sieg")).length);
  }

  private void check(int expectedErrors, String text) throws IOException {
    assertEquals(expectedErrors, rule.match(langTool.getAnalyzedSentence(text)).length);
  }
}
