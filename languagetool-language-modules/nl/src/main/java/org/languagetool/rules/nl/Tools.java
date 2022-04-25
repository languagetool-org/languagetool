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
package org.languagetool.rules.nl;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;

class Tools {

  private Tools() {
  }

  static String glueParts(String[] str) {
    return glueParts(Arrays.asList(str));
  }

  @SuppressWarnings("StringConcatenationInLoop")
  static String glueParts(List<String> s) {
    String spelledWords = "(abc|adv|aed|apk|b2b|bh|bhv|bso|btw|bv|cao|cd|cfk|ckv|cv|dc|dj|dtp|dvd|fte|gft|ggo|ggz|gm|gmo|gps|gsm|hbo|" +
      "hd|hiv|hr|hrm|hst|ic|ivf|kmo|lcd|lp|lpg|lsd|mbo|mdf|mkb|mms|msn|mt|ngo|nv|ob|ov|ozb|p2p|pc|pcb|pdf|pk|pps|" +
      "pr|pvc|roc|rvs|sms|tbc|tbs|tl|tv|uv|vbo|vj|vmbo|vsbo|vwo|wc|wo|xtc|zzp)";
    String compound = s.get(0);
    for (int i = 1; i < s.size(); i++) {
      String word2 = s.get(i);
      char lastChar = compound.charAt(compound.length() - 1);
      char firstChar = word2.charAt(0);
      String connection = lastChar + String.valueOf(firstChar);
      if (StringUtils.containsAny(connection, "aa", "ae", "ai", "ao", "au", "ee", "ei", "eu","ée", "éi", "éu", "ie", "ii", "oe", "oi", "oo", "ou", "ui", "uu", "ij")) {
        compound = compound + '-' + word2;
      } else if (isUpperCase(firstChar) && isLowerCase(lastChar)) {
        compound = compound + '-' + word2;
      } else if (isUpperCase(lastChar) && isLowerCase(firstChar)) {
        compound = compound + '-' + word2;
      } else if (compound.matches(".*[0-9]$")) {
        compound = compound + '-' + word2;
      } else if (word2.matches("^[0-9].*")) {
        compound = compound + '-' + word2;
      } else if (compound.matches("(^|.+-)?" + spelledWords) || word2.matches(spelledWords + "(-.+|$)?")) {
        compound = compound + '-' + word2;
      } else if (compound.matches(".+-[a-z]$") || word2.matches("^[a-z]-.+")) {
        compound = compound + '-' + word2;
      } else {
        compound = compound + word2;
      }
    }
    return compound;
  }

}
