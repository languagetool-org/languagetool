/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Pavel Bakhvalov
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
package org.languagetool.rules.spelling.hunspell;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public interface HunspellDictionary extends Closeable {
  /**
   * Spellcheck the word
   * @param word the word to check
   * @return true if the word is spelled correctly
   */
  public boolean spell(String word);

  /**
   * Add word to the run-time dictionary
   * @param word the word to add
   */
  public void add(String word);

  /**
   * Search suggestions for the word
   * @param word the word to get suggestions for
   * @return the list of suggestions
   */
  public List<String> suggest(String word);
}
