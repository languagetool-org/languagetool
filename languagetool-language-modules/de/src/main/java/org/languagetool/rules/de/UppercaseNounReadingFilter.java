/* LanguageTool, a natural language style checker
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.tagging.de.GermanTagger;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Accepts rule matches when the uppercased word of the {@code token} parameter
 * has a noun reading, e.g. German "stand" (past of "stehen") would be turned into
 * "Stand" and the rule match would be accepted, as it's a noun.
 * @since 3.3
 */
public class UppercaseNounReadingFilter extends RuleFilter {

  @Nullable
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) {
    String token = arguments.get("token");
    if (token == null) {
      throw new RuntimeException("Set 'token' for filter " + UppercaseNounReadingFilter.class.getName() + " in rule " + match.getRule().getId());
    }
    try {
      String uppercase = StringTools.uppercaseFirstChar(token);
      List<AnalyzedTokenReadings> tags = GermanTagger.INSTANCE.tag(Collections.singletonList(uppercase));
      boolean hasNounReading = false;
      for (AnalyzedTokenReadings tag : tags) {
        if (tag.hasPartialPosTag("SUB:") && !tag.hasPartialPosTag("ADJ")) {
          hasNounReading = true;
          break;
        }
      }
      if (hasNounReading) {
        return match;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return null;
  }
}
