/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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

/**
 * Helper class to create error examples. For internal use by LanguageTool only.
 * @since 2.5
 */
public final class Example {

  private Example() {
  }

  /**
   * Create an example text (usually just one sentence) with an error - the error must be marked with {@code <marker>...</marker>}.
   * @throws IllegalArgumentException if the {@code <marker>...</marker>} is missing
   * @since 2.5
   */
  public static IncorrectExample wrong(String example) {
    requireMarkup(example);
    return new IncorrectExample(example);
  }

  /**
   * Create an example text (usually just one sentence) without an error - the fixed error (compared to the text created
   * with {@link #wrong(String)}) can be marked with {@code <marker>...</marker>}.
   * @since 2.5, return type modified in 3.5
   */
  public static CorrectExample fixed(String example) {
    return new CorrectExample(example);
  }

  private static void requireMarkup(String example) {
    if (!example.contains("<marker>") || !example.contains("</marker>")) {
      throw new IllegalArgumentException("Example text must contain '<marker>...</marker>': " + example);
    }
  }

}
