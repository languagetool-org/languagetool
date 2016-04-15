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
package org.languagetool.rules.bitext;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.languagetool.bitext.StringPair;

/**
 * A text, typically a pair of sentences that contains an error.
 * @since 1.0.1
 */
public class IncorrectBitextExample {

  private final StringPair example;
  private final List<String> corrections;

  public IncorrectBitextExample(StringPair example) {
    this(example, Collections.<String>emptyList());
  }

  /**
   * @since 2.9
   */
  public IncorrectBitextExample(StringPair example, List<String> corrections) {
    this.example = Objects.requireNonNull(example);
    this.corrections = Collections.unmodifiableList(corrections);
  }

  /**
   * Return the example that contains the error.
   */
  public StringPair getExample() {
    return example;
  }

  /**
   * Return the possible corrections.
   */
  public List<String> getCorrections() {
    return corrections;
  }
  
  @Override
  public String toString() {
    return example.getSource() + "/ " + example.getTarget() + " " + corrections;
  }

}
