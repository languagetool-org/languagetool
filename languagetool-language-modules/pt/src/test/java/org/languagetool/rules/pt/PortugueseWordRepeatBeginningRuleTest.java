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
package org.languagetool.rules.pt;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Portuguese;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @since 3.6
 * localized by @author Tiago F. Santos from the german version
 */
public class PortugueseWordRepeatBeginningRuleTest {

  @Test
  public void testRule() throws IOException {
    JLanguageTool langTool = new JLanguageTool(new Portuguese());
    // correct sentences:
    assertEquals(0, langTool.check("Este exemplo está correto. Este exemplo também está.").size());
    assertEquals(0, langTool.check("2011: Setembro já passou. 2011: Outubro também já passou. 2011: Novembro já se foi.").size());
    assertEquals(0, langTool.check("Certo, isto está bem. Este exemplo está correto. Certo que este também.").size()); // 1 error from NO_VERB
    // errors:
    assertEquals(1, langTool.check("Este exemplo está correto. Este segundo também. Este terceiro exemplo não.").size()); //1 error from NO_VERB
    assertEquals(1, langTool.check("Então, este está correto. Então, este está errado, por causa da repetição.").size());
    // this used to cause false alarms because reset() was not implemented
    assertEquals(0, langTool.check("Então, este deve ser considerado uma nova frase.").size());
  }

}
