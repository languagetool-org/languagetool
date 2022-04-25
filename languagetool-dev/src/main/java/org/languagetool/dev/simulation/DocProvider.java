/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.simulation;

import java.util.*;

/**
 * Provides random-length documents, with a length distribution that roughly
 * simulates that of the production system.
 */
class DocProvider {

  private static final int MAX_VAL = 20_000;

  private final List<String> docs;

  private Random rnd;

  DocProvider(List<String> docs) {
    this.docs = docs;
    reset();
  }

  void reset() {
    rnd = new Random(120);  // don't change without checking that request size distribution is still realistic
  }

  String getDoc() {
    int len = getWeightedRandomLength();
    synchronized (docs) {
      StringBuilder appended = new StringBuilder();
      int paraSize = 0;
      while (appended.length() < len) {
        if (docs.size() == 0) {
          throw new RuntimeException("Not enough docs left to provide another document");
        }
        String doc = docs.get(0);
        appended.append(doc).append(" ");
        paraSize += doc.length();
        if (paraSize > 250 && appended.toString().endsWith(". ")) {
          appended.append(doc).append("\n\n");
          paraSize = 0;
        }
        docs.remove(0);
      }
      return appended.substring(0, len);
    }
  }

  int getWeightedRandomLength() {
    int max = getRandomMaxLength();
    int min = max == MAX_VAL ? 550 : max - 49;
    // just assume uniform length distribution inside these ranges (not quite true)...
    return min + this.rnd.nextInt(max - min);
  }

  private int getRandomMaxLength() {
    double rnd = this.rnd.nextFloat() * 100;
    // this leads to a distribution roughly as we see it in the production system:
    float fix = 15.6f;
    if (rnd < 32) {
      return 49;
    } else if (rnd < 50 + fix) {
      return 99;
    } else if (rnd < 60 + fix) {
      return 149;
    } else if (rnd < 67 + fix) {
      return 199;
    } else if (rnd < 72 + fix) {
      return 249;
    } else if (rnd < 75 + fix) {
      return 299;
    } else if (rnd < 78 + fix) {
      return 349;
    } else if (rnd < 80 + fix) {
      return 399;
    } else if (rnd < 82 + fix) {
      return 449;
    } else if (rnd < 83 + fix) {
      return 499;
    } else if (rnd < 84 + fix) {
      return 549;
    } else {
      // not quite correct...
      return MAX_VAL;
    }
  }

}
