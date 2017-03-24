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

import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

/**
 * Reader of simple tab-delimited bilingual files.
 * 
 * @author Marcin Miłkowski
 */
public class TabBitextReader implements BitextReader {

  protected BufferedReader in;
  protected StringPair nextPair; 
  protected String nextLine;
  protected int sentencePos;

  private String prevLine;
  private int lineCount = -1;

  /**
   * @param encoding input encoding or {@code null} to use the platform default
   */
  public TabBitextReader(String filename, String encoding) {
    try {     
      if (encoding == null) {
        in = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
      } else {
        in = new BufferedReader(new InputStreamReader(new FileInputStream(filename), encoding));
      }
      nextLine = in.readLine();
      prevLine = "";
      nextPair = tab2StringPair(nextLine);
    } catch (IOException e) { 
      throw new RuntimeException(e); 
    }
  }

  @Nullable
  protected StringPair tab2StringPair(String line) {
    if (line == null) {
      return null;
    }
    String[] fields = line.split("\t");
    if (fields.length < 2) {
      throw new RuntimeException("Unexpected format, expected two tab-separated columns: " + line);
    }
    return new StringPair(fields[0], fields[1]);
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
        StringPair result = nextPair;
        sentencePos = nextPair.getSource().length() + 1;
        if (nextLine != null) {
          prevLine = nextLine;
          nextLine = in.readLine();
          nextPair = tab2StringPair(nextLine);
          lineCount++;
          if (nextLine == null) {
            in.close();
          }
        }
        return result;
      } catch (IOException e) { 
        throw new RuntimeException(e); 
      }
    }

    // The file is read-only.
    @Override
    public void remove() { 
      throw new UnsupportedOperationException(); 
    }
  }

  @Override
  public int getColumnCount() {
    return sentencePos;
  }

  @Override
  public int getTargetColumnCount() {
    return 1;
  }
  
  @Override
  public int getLineCount() {    
    return lineCount;
  }

  @Override
  public int getSentencePosition() {
    return sentencePos;
  }

  @Override
  public String getCurrentLine() {
    return prevLine;
  }

}
