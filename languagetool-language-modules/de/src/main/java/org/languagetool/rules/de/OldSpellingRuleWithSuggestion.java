/* LanguageTool, a natural language style checker 
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.rules.Rule;

import java.util.Objects;

/**
 * @since 3.8
 */
class OldSpellingRuleWithSuggestion {

  Rule rule;
  String oldSpelling;
  String newSpelling;

  OldSpellingRuleWithSuggestion(Rule rule, String oldSpelling, String newSpelling) {
    this.rule = Objects.requireNonNull(rule);
    this.oldSpelling = Objects.requireNonNull(oldSpelling);
    this.newSpelling = Objects.requireNonNull(newSpelling);
  }

}
