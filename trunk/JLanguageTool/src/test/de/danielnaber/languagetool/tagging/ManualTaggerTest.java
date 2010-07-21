/* LanguageTool, a natural language style checker 
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.tagging;

import java.io.IOException;
import java.util.Arrays;

import de.danielnaber.languagetool.JLanguageTool;

import junit.framework.TestCase;

/**
 * @author Daniel Naber
 */
public class ManualTaggerTest extends TestCase {

  private static final String MANUAL_DICT_FILENAME = "/de/added.txt";

  public void testManualTagger() throws IOException {
    ManualTagger mt = new ManualTagger(JLanguageTool.getDataBroker().getFromResourceDirAsStream(MANUAL_DICT_FILENAME));
    assertNull(mt.lookup(""));
    assertNull(mt.lookup("gibtsnicht"));
    
    assertEquals("[Trotz, SUB:NOM:SIN:MAS]", Arrays.toString(mt.lookup("Trotz")));
    // lookup is case sensitive:
    assertNull(mt.lookup("trotz"));

    assertEquals("[Interesse, SUB:NOM:PLU:NEU, Interesse, SUB:AKK:PLU:NEU, Interesse, SUB:GEN:PLU:NEU]",
        Arrays.toString(mt.lookup("Interessen")));
  }
  
}
