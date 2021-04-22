/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.markup.AnnotatedText;

import java.util.List;

/**
 * Filter rule matches.
 *
 * @since 1.8
 */
public interface RuleMatchFilter {

  /**
   * @deprecated use and implement {@code filter(List<RuleMatch> ruleMatches, AnnotatedText text)}
   */
  @Deprecated
  default List<RuleMatch> filter(List<RuleMatch> ruleMatches) {
    throw new RuntimeException("Method not implemented");
  }

  /**
   * @since 4.7
   * post-processing of rule matches
   * @param ruleMatches matches to transform/filter
   * @param text corresponding text
   * @return transformed matches
   */
  default List<RuleMatch> filter(List<RuleMatch> ruleMatches, AnnotatedText text) {
    return filter(ruleMatches);
  }

}
