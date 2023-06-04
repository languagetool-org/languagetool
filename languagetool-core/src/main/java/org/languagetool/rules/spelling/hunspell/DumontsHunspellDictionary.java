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

import dumonts.hunspell.Hunspell;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class DumontsHunspellDictionary implements HunspellDictionary {
  private final Hunspell hunspell;
  private boolean closed = false;

  public DumontsHunspellDictionary(Path dictionary, Path affix) {
    try {
      hunspell = new Hunspell(dictionary,affix);
    } catch (UnsatisfiedLinkError e) {
      throw new RuntimeException("Could not create hunspell instance. Please note that LanguageTool supports only 64-bit platforms " +
          "(Linux, Windows, Mac) and that it requires a 64-bit JVM (Java).", e);
    }
  }

  @Override
  public boolean spell(String word) {
    return hunspell.spell(word);
  }

  @Override
  public void add(String word) {
    hunspell.add(word);
  }

  @Override
  public List<String> suggest(String word) {
    return Arrays.asList(hunspell.suggest(word));
  }

  @Override
  public boolean isClosed() {
    return closed;
  }

  @Override
  public void close() throws IOException {
    closed = true;
    hunspell.close();
  }
}
