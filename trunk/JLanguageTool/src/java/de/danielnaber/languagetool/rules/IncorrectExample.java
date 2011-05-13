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
package de.danielnaber.languagetool.rules;

import java.util.Arrays;
import java.util.List;

/**
 * A text, typically a sentence, that contains an error.
 * 
 * @since 0.9.2
 * @author Daniel Naber
 */
public class IncorrectExample {

  private String example;
  private List<String> corrections;

  public IncorrectExample(final String example) {
    this.example = example;
  }

  public IncorrectExample(final String example, final String[] corrections) {
    this(example);
    this.corrections = Arrays.asList(corrections);
  }
  
  /**
   * Return the example that contains the error.
   */
  public String getExample() {
    return example;
  }

  /**
   * Return the possible corrections. May be null.
   */
  public List<String> getCorrections() {
    return corrections;
  }
  
  @Override
  public String toString() {
    return example + " " + corrections;
  }

}
