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

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.patterns.RuleFilter;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filters rule matches so that only matches are kept where a <em>part of the
 * token</em> has a given POS tag. Expects these arguments:
 * <ul>
 *   <li>{@code no}: an integer of the matching 'token' position to be considered. Starts with 1.</li>
 *   <li>{@code regexp}: the regular expression to specify the part of the token to be considered.
 *       For example, <tt>(?:in|un)(.*)</tt> will consider the part of the token that comes after 'in' or 'un'.
 *       Note that always the first group is considered, so if you need more parenthesis you need to use
 *       non-capturing groups <tt>(?:...)</tt>, as in the example.</li>
 *   <li>{@code postag_regexp}: a regular expression to match the POS tag of the part of the word, e.g. <tt>VB.?</tt>
 *       to match any verb in English.</li>
 *   <li>{@code negate_postag}: if value is yes, then the regexp is negated (not negated if not specified).</li>
 *   <li>{@code two_groups_regexp}: if value is yes, then the regexp must contain 2 groups (if not specified - 1 groups).</li>
 * </ul>
 * @since 2.8
 */
public abstract class PartialPosTagFilter extends RuleFilter {

  @Nullable
  protected abstract List<AnalyzedTokenReadings> tag(String token);

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> args, AnalyzedTokenReadings[] patternTokens) {
    if (!(args.containsKey("no") && args.containsKey("regexp") && args.containsKey("postag_regexp"))) {
      throw new RuntimeException("Set 'no', 'regexp' and 'postag_regexp' for filter " + PartialPosTagFilter.class.getSimpleName());
    }
    int tokenPos = Integer.parseInt(args.get("no"));
    Pattern pattern = Pattern.compile(args.get("regexp"));
    String requiredTagRegexp = args.get("postag_regexp");
    boolean negatePos = args.containsKey("negate_pos");
    boolean two_groups_regexp = args.containsKey("two_groups_regexp");
    String token = patternTokens[tokenPos - 1].getToken();
    Matcher matcher = pattern.matcher(token);
    if ((matcher.groupCount() != 1) && !(two_groups_regexp)) {
      throw new RuntimeException("Got " + matcher.groupCount() + " groups for regex '" + pattern.pattern() + "', expected 1");
    }
    if ((matcher.groupCount() != 2) && (two_groups_regexp)) {
      throw new RuntimeException("Got " + matcher.groupCount() + " groups for regex '" + pattern.pattern() + "', expected 2");
    }
    if (matcher.matches()) {
      String partialToken = matcher.group(1);
      if (matcher.groupCount() == 2) {
        partialToken += matcher.group(2);
      } 
      List<AnalyzedTokenReadings> tags = tag(partialToken);
      if (tags != null && partialTagHasRequiredTag(tags, requiredTagRegexp, negatePos)) {
        return match;
      }
      return null;
    }
    return null;
  }

  private boolean partialTagHasRequiredTag(List<AnalyzedTokenReadings> tags, String requiredTagRegexp, boolean negatePos) {
    // Without negate_pos=yes: return true if any postag matches the regexp.
    // With negate_pos=yes:    return true if there are postag and none them matches the regexp.
    int postagCount = 0;
    for (AnalyzedTokenReadings tag : tags) {
      for (AnalyzedToken analyzedToken : tag.getReadings()) {
        if (analyzedToken.getPOSTag() != null) {
          if (negatePos) {
            postagCount++;
            if (analyzedToken.getPOSTag().matches(requiredTagRegexp)) {
              return false;
            }
          } else {
            if (analyzedToken.getPOSTag().matches(requiredTagRegexp)) {
              return true;
            }
          }
        }
      }
    }
    return postagCount == 0 ? false : negatePos;
  }
}
