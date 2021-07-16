/* LanguageTool, a natural language style checker 
 * Copyright (C) 2008 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A text, typically a sentence, that contains an error.
 * @since 0.9.2
 */
public final class IncorrectExample extends ExampleSentence {
  
  private final Object corrections;

  public IncorrectExample(String example) {
    this(example, Collections.emptyList());
  }

  /**
   * @since 2.9
   */
  public IncorrectExample(String example, List<String> corrections) {
    super(example);
    this.corrections = corrections.isEmpty() ? null :
                       corrections.size() == 1 ? corrections.get(0) :
                       corrections.toArray(new String[0]);
  }

  /**
   * Return the possible corrections.
   */
  @NotNull
  public List<String> getCorrections() {
    return corrections == null ? Collections.emptyList() :
           corrections instanceof String ? Collections.singletonList((String) corrections) :
           Collections.unmodifiableList(Arrays.asList((String[])corrections));
  }
  
  @Override
  public String toString() {
    return example + " " + getCorrections();
  }

}
