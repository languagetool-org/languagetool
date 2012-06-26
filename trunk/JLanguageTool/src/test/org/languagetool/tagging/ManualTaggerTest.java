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
package org.languagetool.tagging;

import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;

import org.languagetool.JLanguageTool;

/**
 * @author Daniel Naber
 */
public class ManualTaggerTest extends TestCase {

  private static final String MANUAL_DICT_FILENAME = "/de/added.txt";

  public void testManualTagger() throws IOException {
    final ManualTagger tagger = new ManualTagger(JLanguageTool.getDataBroker().getFromResourceDirAsStream(MANUAL_DICT_FILENAME));
    assertNull(tagger.lookup(""));
    assertNull(tagger.lookup("gibtsnicht"));
    
    assertEquals("[Ableitung, SUB:NOM:PLU:FEM, Ableitung, SUB:GEN:PLU:FEM, Ableitung, SUB:DAT:PLU:FEM, Ableitung, SUB:AKK:PLU:FEM]",
            Arrays.toString(tagger.lookup("Ableitungen")));
    // lookup is case sensitive:
    assertNull(tagger.lookup("ableitungen"));
  }
  
}
