/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.wikipedia;

import xtc.tree.Location;

class LocationHelper {

  private LocationHelper() {
  }

  /**
   * Get an absolute position (character-based) for a line/column-based location.
   */
  static int absolutePositionFor(Location location, String text) {
    int line = 1;
    int col = 1;
    int pos = 0;
    boolean ignoreMode = false;
    for (int i = 0; i < text.length(); i++) {
      if (line == location.line && col == location.column) {
        return pos;
      }
      char prevCh = i > 0 ? text.charAt(i - 1) : '-';
      char ch = text.charAt(i);
      if (ignoreMode) {
        //
        if (ch == '}' && prevCh == '}') {
          // ignore templates
          ignoreMode = false;
        }
      } else if (ch == '{' && prevCh == '{') {
        ignoreMode = true;
      } else if (ch == '\n') {
        line++;
        col = 1;
      } else {
        col++;
      }
      pos++;
    }
    if (line == location.line && col == location.column) {
      return pos;
    }
    throw new RuntimeException("Could not find location " + location + " in text: '" + text + "'. Max line/col was: " + line + "/" + col);
  }

}
