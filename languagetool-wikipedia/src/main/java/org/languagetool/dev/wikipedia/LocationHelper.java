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

public class LocationHelper {

  private LocationHelper() {
  }

  /**
   * Get an absolute position (character-based) for a line/column-based location.
   */
  public static int absolutePositionFor(Location location, String text) {
    int line = 1;
    int col = 1;
    int pos = 0;
    int ignoreLevel = 0;
    boolean inReference = false;
    StringBuilder relevantLine = new StringBuilder();
    for (int i = 0; i < text.length(); i++) {
      char ch = text.charAt(i);
      if (line == location.line) {
        relevantLine.append(ch);
      }
      //System.out.println(line  + "/" + col + ", ignoreLevel: " + ignoreLevel);
      if (line == location.line && col == location.column) {
        return pos;
      }
      char prevCh = i > 0 ? text.charAt(i - 1) : '-';
      if (isReferenceStart(text, i)) {
        ignoreLevel++;
        inReference = true;
      } else if (inReference && (isFullReferenceEndTag(text, i) || isShortReferenceEndTag(text, i))) {
        ignoreLevel--;
        inReference = false;
        if (isShortReferenceEndTag(text, i)) {
          // this makes SuggestionReplacerTest.testReference2() work, not sure why
          col++;
        }
      } else if (isHtmlCommentStart(text, i)) {
        ignoreLevel++;
      } else if (isHtmlCommentEnd(text, i)) {
        ignoreLevel--;
      } else if (ch == '}' && prevCh == '}') {
        if (ignoreLevel > 0) {
          ignoreLevel--;
        }
      } else if (ch == '{' && prevCh == '{') {
        // ignore templates
        ignoreLevel++;
      } else if (ch == '\n' && ignoreLevel == 0) {
        line++;
        col = 1;
      } else if (ignoreLevel == 0) {
        col++;
      }
      pos++;
    }
    if (line == location.line && col == location.column) {
      return pos;
    }
    throw new RuntimeException("Could not find location " + location + " in text. " +
            "Max line/col was: " + line + "/" + col + ", Content of relevant line (" + location.line + "): '"
            + relevantLine + "' (" + relevantLine.length() + " chars)");
  }

  private static boolean isReferenceStart(String text, int i) {
    return i < text.length() - 4 && text.substring(i, i + 4).equals("<ref");
  }

  private static boolean isFullReferenceEndTag(String text, int i) {
    return i < text.length() - 6 && text.substring(i, i + 6).equals("</ref>");
  }

  private static boolean isShortReferenceEndTag(String text, int i) {
    return i < text.length() - 2 && text.substring(i, i + 2).equals("/>");
  }

  private static boolean isHtmlCommentStart(String text, int i) {
    return i < text.length() - 4 && text.substring(i, i + 4).equals("<!--");
  }

  private static boolean isHtmlCommentEnd(String text, int i) {
    return i < text.length() - 3 && text.substring(i, i + 3).equals("-->");
  }

}
