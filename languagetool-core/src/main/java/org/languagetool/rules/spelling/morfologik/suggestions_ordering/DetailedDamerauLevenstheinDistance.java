/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2018 Fabian Richter
 *  *
 *  * This library is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU Lesser General Public
 *  * License as published by the Free Software Foundation; either
 *  * version 2.1 of the License, or (at your option) any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this library; if not, write to the Free Software
 *  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  * USA
 *
 */

package org.languagetool.rules.spelling.morfologik.suggestions_ordering;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

public final class DetailedDamerauLevenstheinDistance {
  private DetailedDamerauLevenstheinDistance() {
  }

  public abstract static class EditOperation implements UnaryOperator<String> {
    protected final Random random;

    public EditOperation() {
      this.random = new Random();
    }

    public EditOperation(long seed) {
      this.random = new Random(seed);
    }
  }

  public static class Delete extends EditOperation {

    @Override
    public String apply(String s) {
      if (s.length() <= 1) {
        return null;
      }

      int i = random.nextInt(s.length());
      if (i == 0) {
        return s.substring(1);
      } else if (i == s.length() - 1) {
        return s.substring(0, i);
      } else {
        return s.substring(0, i) + s.substring(i + 1);
      }
    }
  }

  public static class Transpose extends EditOperation {
    @Override
    public String apply(String s) {
      if (s.length() <= 1) {
        return null;
      }
      int i = random.nextInt(s.length() - 1);
      String transposed = "" + s.charAt(i + 1) + s.charAt(i);

      if (i == 0) {
        return transposed + s.substring(2);
      } else if (i == s.length() - 1) {
        return s.substring(0, i) + transposed;
      } else {
        return s.substring(0, i) + transposed + s.substring(i + 2);
      }
    }
  }
  private static char randomChar(Random random) {
    return (char) ((int) 'a' + random.nextInt(26));
  }

  public static class Insert extends EditOperation {

    @Override
    public String apply(String s) {
      int i = random.nextInt(s.length() + 1);
      char c = randomChar(random);

      if (i == 0) {
        return "" + c + s;
      } else if (i == s.length()) {
        return s + c;
      } else {
        return s.substring(0, i) + c + s.substring(i);
      }
    }
  }

  public static class Replace extends EditOperation {

    @Override
    public String apply(String s) {
      if (s.length() == 0) {
        return null;
      }
      int i = random.nextInt(s.length());
      char c = randomChar(random);

      if (i == 0) {
        return "" + c + s.substring(1);
      } else if (i == s.length() - 1) {
        return s.substring(0, i) + c;
      } else {
        return s.substring(0, i) + c + s.substring(i + 1);
      }
    }
  }

  public static final List<EditOperation> editOperations = Arrays.asList(
    new Insert(), new Replace(), new Transpose(), new Delete());
  private static final Random random = new Random();

  public static final EditOperation randomEdit() {
    return editOperations.get(random.nextInt(editOperations.size()));
  }

  public static class Distance {
    public final int inserts;
    public final int deletes;
    public final int replaces;
    public final int transposes;

    public Distance() {
      this(0, 0, 0, 0);
    }

    public Distance(int inserts, int deletes, int replaces, int transposes) {
      this.inserts = inserts;
      this.deletes = deletes;
      this.replaces = replaces;
      this.transposes = transposes;
    }

    public Distance insert() {
      return new Distance(inserts + 1, deletes, replaces, transposes);
    }

    public Distance delete() {
      return new Distance(inserts, deletes + 1, replaces, transposes);
    }

    public Distance replace() {
      return new Distance(inserts, deletes, replaces + 1, transposes);
    }

    public Distance transpose() {
      return new Distance(inserts, deletes, replaces, transposes + 1);
    }

    public Distance track(EditOperation operation) {
      if (operation instanceof Insert) {
        return insert();
      } else if (operation instanceof Delete) {
        return delete();
      } else if (operation instanceof Replace) {
        return replace();
      } else if (operation instanceof Transpose) {
        return transpose();
      } else {
        throw new IllegalArgumentException("Unknown operation: " + operation);
      }
    }

    public int value() {
      return inserts + deletes + replaces + transposes;
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) { return true; }
      if (o == null) { return false; }
      if (o.getClass() != getClass()) { return false; }
      Distance other = (Distance) o;
      return new EqualsBuilder()
        .append(inserts, other.inserts)
        .append(deletes, other.deletes)
        .append(replaces, other.replaces)
        .append(transposes, other.transposes)
        .build();
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
        .append("value", value())
        .append("inserts", inserts)
        .append("deletes", deletes)
        .append("replaces", replaces)
        .append("transposes", transposes)
        .build();
    }
  }

  /**
   * Compute the distance between strings: the minimum number of operations
   * needed to transform one string into the other (insertion, deletion,
   * substitution of a single character, or a transposition of two adjacent
   * characters).
   *
   * Code adapted from https://github.com/tdebatty/java-string-similarity/blob/3406d2cfd853ca385090d144eed117f636ebd304/src/main/java/info/debatty/java/stringsimilarity/Damerau.java, available under MIT License
   *
   *  The MIT License
   *
   *  Copyright 2015 Thibault Debatty.
   *
   *  Permission is hereby granted, free of charge, to any person obtaining a copy
   *  of this software and associated documentation files (the "Software"), to deal
   *  in the Software without restriction, including without limitation the rights
   *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
   *  copies of the Software, and to permit persons to whom the Software is
   *  furnished to do so, subject to the following conditions:
   *
   *  The above copyright notice and this permission notice shall be included in
   *  all copies or substantial portions of the Software.
   *
   *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
   *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
   *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
   *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
   *  THE SOFTWARE.
   *
   *
   * @param s1 The first string to compare.
   * @param s2 The second string to compare.
   * @return The computed distance.
   * @throws NullPointerException if s1 or s2 is null.
   */
  public static Distance compare(final String s1, final String s2) {

    if (s1 == null) {
      throw new NullPointerException("s1 must not be null");
    }

    if (s2 == null) {
      throw new NullPointerException("s2 must not be null");
    }

    if (s1.equals(s2)) {
      return new Distance();
    }

    // INFinite distance is the max possible distance
    int inf = s1.length() + s2.length();

    // Create and initialize the character array indices
    HashMap<Character, Integer> da = new HashMap<Character, Integer>();

    for (int d = 0; d < s1.length(); d++) {
      da.put(s1.charAt(d), 0);
    }

    for (int d = 0; d < s2.length(); d++) {
      da.put(s2.charAt(d), 0);
    }

    // Create the distance matrix H[0 .. s1.length+1][0 .. s2.length+1]
    Distance[][] h = new Distance[s1.length() + 2][s2.length() + 2];

    // fill with empty values
    for (int i = 0; i < s1.length() + 2; i ++) {
      for (int j = 0; j < s2.length() + 2; j++) {
        h[i][j] = new Distance();
      }
    }

    // initialize the left and top edges of H
    // TODO understand initialization
    for (int i = 0; i <= s1.length(); i++) {
      h[i + 1][0] = new Distance(inf, 0, 0, 0);
      h[i + 1][1] = new Distance(i, 0, 0, 0);
    }

    for (int j = 0; j <= s2.length(); j++) {
      h[0][j + 1] = new Distance(inf, 0, 0, 0);
      h[1][j + 1] = new Distance(j, 0, 0, 0);
    }

    // fill in the distance matrix H
    // look at each character in s1
    for (int i = 1; i <= s1.length(); i++) {
      int db = 0;

      // look at each character in b
      for (int j = 1; j <= s2.length(); j++) {
        int i1 = da.get(s2.charAt(j - 1));
        int j1 = db;

        int cost = 1;
        if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
          cost = 0;
          db = j;
        }

        int substitution = h[i][j].value() + cost;
        int insertion = h[i + 1][j].value() + 1;
        int deletion = h[i][j + 1].value() + 1;
        int transpose = h[i1][j1].value() + (i - i1 - 1) + 1 + (j - j1 - 1);
        int min = IntStream.of(substitution, insertion, deletion, transpose).min().getAsInt();

        // TODO: think about order, multiple cases might be true, which operations should be preferred?
        if (min == substitution) {
          if (cost == 1) {
            h[i + 1][j + 1] = h[i][j].replace();
          } else {
            h[i + 1][j + 1] = h[i][j];
          }
        } else if (min == insertion) {
          h[i + 1][j + 1] = h[i + 1][j].insert();
        } else if (min == deletion) {
          h[i + 1][j + 1] = h[i][j + 1].delete();
        } else if (min == transpose) {
          int transposeCost = (i - i1 - 1) + 1 + (j - j1 - 1);
          Distance value = h[i1][j1];
          for (int k = 0; k < transposeCost; k++) {
            value = value.transpose();
          }
          h[i + 1][j + 1] = value;
        }

      }

      da.put(s1.charAt(i - 1), i);
    }

    return h[s1.length() + 1][s2.length() + 1];
  }
}
