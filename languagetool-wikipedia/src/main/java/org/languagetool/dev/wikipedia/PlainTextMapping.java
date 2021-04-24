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

import java.util.Map;

/**
 * The result of a text extraction: plain text plus a mapping from plain text
 * positions to corresponding positions in the original markup.
 */
public class PlainTextMapping {

  private final String plainText;
  private final Map<Integer,Location> mapping;

  public PlainTextMapping(String plainText, Map<Integer, Location> mapping) {
    this.plainText = plainText;
    this.mapping = mapping;
  }

  public String getPlainText() {
    return plainText;
  }

  public Map<Integer, Location> getMapping() {
    return mapping;
  }

  /**
   * @param plainTextPosition not zero-based - smallest value is 1!
   */
  public Location getOriginalTextPositionFor(int plainTextPosition) {
    if (plainTextPosition < 1) {
      throw new RuntimeException("plainTextPosition must be > 0 - its value starts at 1");
    }
    Location origPosition = mapping.get(plainTextPosition);
    if (origPosition != null) {
      //System.out.println("mapping " + plainTextPosition + " to " + origPosition + " [direct]");
      return origPosition;
    }
    int minDiff = Integer.MAX_VALUE;
    Location bestMatch = null;
    //Integer bestMaybeClosePosition = null;
    // algorithm: find the closest lower position
    for (Map.Entry<Integer, Location> entry : mapping.entrySet()) {
      int maybeClosePosition = entry.getKey();
      if (plainTextPosition > maybeClosePosition) {
        int diff = plainTextPosition - maybeClosePosition;
        if (diff >= 0 && diff < minDiff) {
          bestMatch = entry.getValue();
          //bestMaybeClosePosition = maybeClosePosition;
          minDiff = diff;
        }
      }
    }
    if (bestMatch == null) {
      throw new RuntimeException("Could not map " + plainTextPosition + " to original position. Mapping: " + mapping);
    }
    // we assume that when we have found the closest match there's a one-to-one mapping
    // in this region, thus we can add 'minDiff' to get the exact position:
    //System.out.println("mapping " + plainTextPosition + " to line " + bestMatch.line + ", column " +
    //        bestMatch.column + "+" +  minDiff + ", bestMatch was: " + bestMaybeClosePosition +"=>"+ bestMatch);
    return new Location(bestMatch.file, bestMatch.line, bestMatch.column + minDiff);
  }

}
