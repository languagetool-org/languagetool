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
package org.languagetool;

import org.junit.Test;
import org.languagetool.language.Dutch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class JLanguageToolTest {

  @Test
  public void testDutch() throws IOException {
    final JLanguageTool tool = new JLanguageTool(new Dutch());
    /* this is in the way, since I am using rules to experiment with possible disambiguation pattern, to check if they are good enough
    assertEquals(0, tool.check("Een test, die geen fouten mag geven.").size());
    */
    //assertEquals(1, tool.check("Dit is fout.!").size());
    //test uppercasing rule:
    /*  
    matches = tool.check("De Afdeling Beheer kan het");
    assertEquals(1, matches.size());   
    assertEquals("Als Afdeling geen deel uitmaakt van de naam, dan is juist:<suggestion>afdeling</suggestion>", matches.get(0).getMessage());
     */
    // Dutch rule has no effect with English error but they are spelling mistakes:
    assertEquals(3, tool.check("I can give you more a detailed description.").size());
  }
  
}
