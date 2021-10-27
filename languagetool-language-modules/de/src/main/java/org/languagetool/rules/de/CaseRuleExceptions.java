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
package org.languagetool.rules.de;

import gnu.trove.THashSet;
import org.apache.commons.lang3.StringUtils;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.patterns.StringMatcher;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @since 3.0
 */
final class CaseRuleExceptions {

  private static final Set<String> exceptions = loadExceptions(
    "/de/eigennamen_gross.txt",  // Premium
    "/de/case_rule_exceptions.txt"
  );

  private CaseRuleExceptions() {
  }

  public static Set<String> getExceptions() {
    return exceptions;
  }

  public static Set<StringMatcher[]> getExceptionPatterns() {
    THashSet<StringMatcher[]> exceptionPatterns = new THashSet<>(250);
    for (String phrase : exceptions) {
      String[] parts = StringUtils.split(phrase, ' ');
      StringMatcher[] patterns = new StringMatcher[parts.length];
      for (int j = 0; j < parts.length; j++) {
        patterns[j] = StringMatcher.regexp(parts[j]);
      }
      exceptionPatterns.add(patterns);
    }
    exceptionPatterns.trimToSize();
    return Collections.unmodifiableSet(exceptionPatterns);
  }

  private static Set<String> loadExceptions(String... paths) {
    Set<String> result = new HashSet<>();
    for (String path : paths) {
      List<String> lines = JLanguageTool.getDataBroker().getFromResourceDirAsLines(path);
      for (String line : lines) {
        if (line.isEmpty() || line.startsWith("#")) {
          continue;
        }
        if (Character.isWhitespace(line.charAt(0)) || Character.isWhitespace(line.charAt(line.length()-1))) {
          throw new IllegalArgumentException("Invalid line in " + path + ", starts or ends with whitespace: '" + line + "'");
        }
        result.add(line);
      }
    }
    return Collections.unmodifiableSet(result);
  }

}
