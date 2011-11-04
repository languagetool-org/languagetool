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

package de.danielnaber.languagetool.bitext;

import java.io.IOException;
import java.util.Iterator;

/**
 * Reader of WordFast Translation Memory text files.
 * They are simple tab-delimited text files.
 * 
 * @author Marcin Miłkowski
 */
public class WordFastTMReader extends TabBitextReader {

  public WordFastTMReader(final String filename, final String encoding) throws IOException {
    super(filename, encoding);
    //skip the header (first line)
    if (nextLine != null) {
      nextLine = in.readLine();
      nextPair =  tab2StringPair(nextLine);
    }
  }
  
  @Override
  public final StringPair tab2StringPair(final String line) {
    if (line == null) {
      return null;
    }
    final String[] fields = line.split("\t");
    sentencePos = fields[4].length() + 1;
    return new StringPair(fields[4], fields[6]);
  }
  
  @Override
  public Iterator<StringPair> iterator() {
    return new TabReader();
  }

  class TabReader implements Iterator<StringPair> {

    @Override
    public boolean hasNext() { 
      return nextLine != null;
    }

    @Override
    public StringPair next() {
      try {
        final StringPair result = nextPair;

        if (nextLine != null) {
          nextLine = in.readLine();
          nextPair = tab2StringPair(nextLine);
          if (nextLine == null) {
            in.close();
          }
        }
        return result;
      } catch(IOException e) { 
        throw new IllegalArgumentException(e); 
      }
    }

    // The file is read-only.
    @Override
    public void remove() { 
      throw new UnsupportedOperationException(); 
    }
  }
  
}


