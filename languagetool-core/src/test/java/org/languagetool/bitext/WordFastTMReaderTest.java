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

public class WordFastTMReaderTest {

  @Test
  public void testReader() throws Exception {
    // Create a simple WordFast text memory.
    File input = File.createTempFile("input", ".txt");  
    input.deleteOnExit();

    // Populate the file with data.
    try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(input), "UTF-8"))) {
      writer.println("%20100801~111517\t%UserID,AHLJat,AHLJat\t%TU=00008580\t%EN-US\t%Wordfast TM v.546/00\t%PL-PL\t%\t.");
      writer.println("20100727~145333\tAHLJat\t2\tEN-US\tObjection:\tPL-PL\tZarzut: ");
      writer.println("20100727~051350\tAHLJat\t2\tEN-US\tWhy not?&tA;\tPL-PL\tDlaczego nie?&tA; ");
    }

    WordFastTMReader reader = new WordFastTMReader(input.getAbsolutePath(), "UTF-8");
    int i = 1;
    for (StringPair srcAndTrg : reader) {
      assertTrue(srcAndTrg.getSource() != null);
      assertTrue(srcAndTrg.getTarget() != null);
      if (i == 1) {
        assertEquals("Objection:", srcAndTrg.getSource());
      } else if (i == 2) {
        assertEquals("Why not?&tA;", srcAndTrg.getSource());
      }
      i++;
    }
  }
}
