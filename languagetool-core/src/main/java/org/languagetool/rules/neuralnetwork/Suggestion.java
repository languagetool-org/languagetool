/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Markus Brenneis
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
package org.languagetool.rules.neuralnetwork;

public class Suggestion {

  private final String suggestion;

  private final boolean unsure;

  Suggestion(String suggestion, boolean unsure) {
    this.suggestion = suggestion;
    this.unsure = unsure;
  }

  @Override
  public String toString() {
    if (unsure) {
      return suggestion + "*";
    } else {
      return suggestion;
    }
  }

  boolean matches(String string) {
    return suggestion.equals(string);
  }

  public boolean isUnsure() {
    return unsure;
  }
}
