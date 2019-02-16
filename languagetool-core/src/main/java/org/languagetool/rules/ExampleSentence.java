/* LanguageTool, a natural language style checker 
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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

import java.util.Objects;

/**
 * @since 3.5
 */
public abstract class ExampleSentence {

  protected final String example;

  public ExampleSentence(String example) {
    this.example = Objects.requireNonNull(example);
    int markerStart= example.indexOf("<marker>");
    int markerEnd = example.indexOf("</marker>");
    if (markerStart != -1 && markerEnd == -1) {
      throw new IllegalArgumentException("Example contains <marker> but lacks </marker>:" + example);
    }
    if (markerStart == -1 && markerEnd != -1) {
      throw new IllegalArgumentException("Example contains </marker> but lacks <marker>:" + example);
    }
    if (markerStart > markerEnd) {
      throw new IllegalArgumentException("Example <marker> comes before </marker>:" + example);
    }
  }

  /**
   * Return the example, typically one sentence.
   */
  public String getExample() {
    return example;
  }

  @Override
  public String toString() {
    return example;
  }

}
