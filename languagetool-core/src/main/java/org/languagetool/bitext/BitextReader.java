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

package org.languagetool.bitext;

/**
 * Interface for classes that implement reading from bitext files,
 * such as translation memory files, glossary files, aligned text...
 * 
 * @author Marcin Miłkowski
 */
public interface BitextReader extends Iterable<StringPair> {

  /**
   * Get the current line number in the file.
   * @return The current line number.
   */
  int getLineCount();
  
  /**
   * Get the current column number in the file.
   * @return  The current column number.
   */
  int getColumnCount();
  
  /**
   * Get the current target column number in the file.
   * @return  The current target column number.
   */
  int getTargetColumnCount();
  
  
  /**
   * Get the current target sentence position in the file.
   * @return  The current sentence position.
   */
  int getSentencePosition();
  
  /**
   * Get the current line of the bitext input.
   * @return The complete line (including source, if any).
   */
  String getCurrentLine();
  
}
