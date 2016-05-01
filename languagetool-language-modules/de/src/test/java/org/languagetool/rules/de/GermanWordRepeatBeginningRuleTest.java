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
package org.languagetool.rules.de;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.German;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Markus Brenneis
 */
public class GermanWordRepeatBeginningRuleTest {

  @Test
  public void testRule() throws IOException {
    JLanguageTool langTool = new JLanguageTool(new German());
    // correct sentences:
    assertEquals(0, langTool.check("Er ist nett. Er heißt Max.").size());
    assertEquals(0, langTool.check("Außerdem kommt er. Ferner kommt sie. Außerdem kommt es.").size());
    assertEquals(0, langTool.check("2011: Dieses passiert. 2011: Jenes passiert. 2011: Nicht passiert").size());
    // errors:
    assertEquals(1, langTool.check("Er ist nett. Er heißt Max. Er ist 11.").size());
    assertEquals(1, langTool.check("Außerdem kommt er. Außerdem kommt sie.").size());
    // this used to cause false alarms because reset() was not implemented
    assertEquals(0, langTool.check("Außerdem ist das ein neuer Text.").size());
  }

}
