/* LanguageTool, a natural language style checker 
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.wordsimilarity;

/**
 * A very simple keyboard distance algorithm. Supports only letters.
 */
public abstract class BaseKeyboardDistance implements KeyboardDistance {

  abstract char[][] getKeys();

  static class Position {
    int row;
    int column;

    Position(int row, int column) {
      this.row = row;
      this.column = column;
    }

    float distanceTo(Position other) {
      return Math.abs(column - other.column) + Math.abs(row - other.row);
    }

    @Override
    public String toString() {
      return "Position{row=" + row + ", column=" + column + '}';
    }
  }

  @Override
  public float getDistance(char c1, char c2) {
    Position p1 = getPosition(c1);
    Position p2 = getPosition(c2);
    return p1.distanceTo(p2);
  }

  private Position getPosition(char searchKey) {
    char searchKeyLowerCase = Character.toLowerCase(searchKey);
    int row = -1;
    int column = -1;
    int rowCount = 0;
    int columnCount;
    for (char[] rowKeys : getKeys()) {
      columnCount = 0;
      for (char c : rowKeys) {
        if (c == searchKeyLowerCase) {
          row = rowCount;
          column = columnCount;
        }
        columnCount++;
      }
      rowCount++;
    }
    if (row == -1 || column == -1) {
      throw new RuntimeException("Could not find '" + searchKey + "' on keyboard - only letters are supported");
    }
    return new Position(row, column);
  }
}
