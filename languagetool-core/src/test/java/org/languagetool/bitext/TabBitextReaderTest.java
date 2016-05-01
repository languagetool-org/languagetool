/* LanguageTool, a natural language style checker 
 * Copyright (C) 2010 Marcin Miłkowski (http://www.languagetool.org)
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

package org.languagetool.bitext;

import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TabBitextReaderTest {

  @Test
  public void testReader() throws Exception {
    // Create a simple plain text file.
    File input = File.createTempFile("input", "txt");  
    input.deleteOnExit();

    // Populate the file with data.
    PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(input), "UTF-8"));
    writer.println("This is not actual.\tTo nie jest aktualne.");
    writer.println("Test\tTest");
    writer.println("ab\tVery strange data indeed, much longer than input");
    writer.close();  

    TabBitextReader reader = new TabBitextReader(input.getAbsolutePath(), "UTF-8");
    int i = 1;
    for (StringPair srcAndTrg : reader) {
      assertTrue(srcAndTrg.getSource() != null);
      assertTrue(srcAndTrg.getTarget() != null);
      if (i == 1) {
        assertEquals("This is not actual.", srcAndTrg.getSource());
      } else if (i == 2) {
        assertEquals("Test", srcAndTrg.getSource());
      } else if (i == 3) {
        assertEquals("Very strange data indeed, much longer than input",
            srcAndTrg.getTarget());
      }
      i++;
    }
  }
}
