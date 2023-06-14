/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Jaume Ortolà
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
package org.languagetool.tools;

import java.util.List;
import java.util.ArrayList;

public class PseudoMatch {
  private final List<String> replacements;
  private final int fromPos;
  private final int toPos;

  PseudoMatch(String replacement, int fromPos, int toPos) {
    this.replacements = new ArrayList<>();
    this.replacements.add(replacement);
    this.fromPos = fromPos;
    this.toPos = toPos;
  }

  public List<String> getReplacements() {
    return this.replacements;
  }

  public int getFromPos() {
    return this.fromPos;
  }

  public int getToPos() {
    return this.toPos;
  }

  public String toString() {
    return String.valueOf(this.fromPos) + "-" + String.valueOf(this.toPos) + "-" + this.replacements.toString();

  }
}