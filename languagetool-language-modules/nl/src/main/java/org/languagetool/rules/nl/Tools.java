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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;
import static java.util.Arrays.asList;
import static java.util.regex.Pattern.*;

class Tools {

  private final static String spelledWords = "abc|adv|aed|apk|b2b|bh|bhv|bso|btw|bv|cao|cd|cfk|ckv|cv|dc|dj|dtp|dvd|fte|gft|ggo|ggz|gm|gmo|gps|gsm|hbo|" +
    "hd|hiv|hr|hrm|hst|ic|ivf|kmo|lcd|lp|lpg|lsd|mbo|mdf|mkb|mms|msn|mt|ngo|nv|ob|ov|ozb|p2p|pc|pcb|pdf|pk|pps|" +
    "pr|pvc|roc|rvs|sms|tbc|tbs|tl|tv|uv|vbo|vj|vmbo|vsbo|vwo|wc|wo|xtc|zzp";
  private final static Set<String> spelledWordsSet = new HashSet<>(asList(spelledWords.split("\\|")));
  private static final Pattern ENDS_IN_DIGIT = compile(".*[0-9]$");
  private static final Pattern STARTS_WITH_DIGIT = compile("^[0-9].*");
  private static final Pattern ENDS_IN_HYPHEN_AND_CHAR = compile(".+-[a-z]$");
  private static final Pattern STARTS_WITH_CHAR_AND_HYPHEN = compile("^[a-z]-.+");
  private static final Pattern HYPHEN_CHARS = compile("(^|.+-)?(" + spelledWords + ")");
  private static final Pattern CHARS_HYPHEN = compile("(" + spelledWords + ")(-.+|$)?");

  private Tools() {
  }

  static String glueParts(String[] str) {
    return glueParts(asList(str));
  }

  static String glueParts(List<String> s) {
    StringBuilder compound = new StringBuilder(s.get(0));
    for (int i = 1; i < s.size(); i++) {
      String word2 = s.get(i);
      if (compound.length() > 2 || spelledWordsSet.contains(compound.toString())) {
        char lastChar = compound.charAt(compound.length() - 1);
        char firstChar = word2.charAt(0);
        String connection = lastChar + String.valueOf(firstChar);
        if (StringUtils.containsAny(connection, "aa", "ae", "ai", "ao", "au", "ee", "ei", "eu", "ée", "éi", "éu", "ie", "ii", "oe", "oi", "oo", "ou", "ui", "uu", "ij") ||
            isUpperCase(firstChar) && isLowerCase(lastChar) ||
            isUpperCase(lastChar) && isLowerCase(firstChar) ||
            isUpperCase(lastChar) && isUpperCase(firstChar) ||
            ENDS_IN_DIGIT.matcher(compound).matches() ||
            STARTS_WITH_DIGIT.matcher(word2).matches() ||
            HYPHEN_CHARS.matcher(compound).matches() ||
            CHARS_HYPHEN.matcher(word2).matches() ||
            ENDS_IN_HYPHEN_AND_CHAR.matcher(compound).matches() ||
            STARTS_WITH_CHAR_AND_HYPHEN.matcher(word2).matches()) {
          compound.append('-').append(word2);
        } else {
          compound.append(word2);
        }
      } else {
        compound.append(word2);
      }
    }
    return compound.toString();
  }

}
