/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules;

import java.util.Arrays;
import java.util.Locale;

/**
 * Some constants for Localization Quality Issue Type from the
 * Internationalization Tag Set (ITS) Version 2.0. Note that this class is
 * internal to LanguageTool, it is public only for technical reasons.
 * 
 * @see <a href="http://www.w3.org/International/multilingualweb/lt/drafts/its20/its20.html#lqissue-typevalues">ITS 2.0</a>
 * @since 2.5
 */
public enum ITSIssueType {

  Terminology, Mistranslation, Omission, Untranslated, Addition, Duplication, Inconsistency, Grammar,
  Legal, Register, LocaleSpecificContent("locale-specific-content"), LocaleViolation("locale-violation"),
  Style, Characters, Misspelling, Typographical, Formatting, InconsistentEntities("inconsistent-entities"),
  Numbers, Markup, PatternProblem("pattern-problem"), Whitespace, Internationalization, Length, 
  NonConformance("non-conformance"), Uncategorized, Other;

  public static ITSIssueType getIssueType(String name) {
    for (ITSIssueType issueType : values()) {
      if (issueType.toString().equals(name)) {
        return issueType;
      }
    }
    throw new IllegalArgumentException("No IssueType found for name '" + name + "'. Valid values: " + Arrays.toString(values()));
  }

  private final String name;

  ITSIssueType() {
    this.name = name();
  }

  ITSIssueType(String name) {
    this.name = name;
  }

  /**
   * Use this to get the name as it is used in the ITS 2.0 standard
   * (namely lowercase and with hyphens, not camel case)
   */
  @Override
  public String toString() {
    return name.toLowerCase(Locale.ENGLISH);
  }

}
