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
 * </ul>
 * @since 2.8
 */
public abstract class PartialPosTagFilter implements RuleFilter {

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
    String token = patternTokens[tokenPos - 1].getToken();
    Matcher matcher = pattern.matcher(token);
    if (matcher.matches()) {
      String partialToken = matcher.group(1);
      List<AnalyzedTokenReadings> tags = tag(partialToken);
      if (tags != null && partialTagHasRequiredTag(tags, requiredTagRegexp)) {
        return match;
      }
      return null;
    }
    return null;
  }

  private boolean partialTagHasRequiredTag(List<AnalyzedTokenReadings> tags, String requiredTagRegexp) {
    for (AnalyzedTokenReadings tag : tags) {
      for (AnalyzedToken analyzedToken : tag.getReadings()) {
        boolean tagFound = analyzedToken.getPOSTag() != null && analyzedToken.getPOSTag().matches(requiredTagRegexp);
        if (tagFound) {
          return true;
        }
      }
    }
    return false;
  }

}
