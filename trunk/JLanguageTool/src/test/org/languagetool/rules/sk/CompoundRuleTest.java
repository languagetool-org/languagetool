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
package de.danielnaber.languagetool.rules.sk;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.CompoundRuleTestAbs;

import java.io.IOException;

/**
 * @author Daniel Naber
 */
public class CompoundRuleTest extends CompoundRuleTestAbs {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    langTool = new JLanguageTool(Language.SWEDISH);
    rule = new CompoundRule(null);
  }
  
  public void testRule() throws IOException {
    // correct:
    check(0, "česko-slovenský");
    check(0, "rakúsko-uhorský");
    // incorrect:
    check(1, "bim bam", new String[]{"bim-bam"});
  }
  
}
