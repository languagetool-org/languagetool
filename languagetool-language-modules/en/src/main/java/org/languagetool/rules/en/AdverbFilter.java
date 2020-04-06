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
package org.languagetool.rules.en;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.AbstractDateCheckFilter;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Filter that maps suggestion from adverb to adjective.
 * Also see https://www.ef.com/wwen/english-resources/english-grammar/forming-adverbs-adjectives/
 * @since 4.9
 */
public class AdverbFilter extends RuleFilter {

  private Map<String,String> adverb2Adj = new HashMap<String, String>() {{
    // irregular ones:
    put("well", "good");
    put("fast", "fast");
    put("hard", "hard");
    put("late", "late");
    put("early", "early");
    put("daily", "daily");
    put("straight", "straight");
    // regular ones:
    put("simply", "simple");
    put("cheaply", "cheap");
    put("quickly", "quick");
    put("slowly", "slow");
    put("easily", "easy");
    put("angrily", "angry");
    put("happily", "happy");
    put("luckily", "lucky");
    put("probably", "probable");
    put("terribly", "terrible");
    put("gently", "gentle");
    put("basically", "basic");
    put("tragically", "tragic");
    put("economically", "economic");
    put("greatly", "great");
    put("highly", "high");
    put("generally", "general");
    put("differently", "different");
    put("rightly", "right");
    put("largely", "large");
    put("really", "real");
    put("philosophically", "philosophical");
    put("directly", "direct");
    put("clearly", "clear");
    put("merely", "mere");
    put("exactly", "exact");
    put("recently", "recent");
    put("rapidly", "rapid");
    put("suddenly", "sudden");
    put("extremely", "extreme");
    put("properly", "proper");
    put("politically", "political");
    put("probably", "probable");
    put("self-consciously", "self-conscious");
    put("successfully", "successful");
    put("unusually", "unusual");
    put("obviously", "obvious");
    put("currently", "current");
    put("residentially", "residential");
    put("fully", "full");
    put("accidentally", "accidental");
    put("medicinally", "medicinal");
    put("automatically", "automatic");
    put("completely", "complete");
    put("chronologically", "chronological");
    put("accurately", "accurate");
    put("necessarily", "necessary");
    put("temporarily", "temporary");
    put("significant", "significantly");
    put("hastily", "hasty");
    put("immediately", "immediate");
    put("rarely", "rare");
    put("totally", "total");
    put("literally", "literal");
    put("gently", "gentle");
    put("finally", "final");
    put("increasingly", "increasing");
    put("decreasingly", "decreasing");
    put("considerably", "considerable");
    put("effectively", "effective");
    put("suddenly", "sudden");
    put("fairly", "fair");
    put("briefly", "brief");
    put("slightly", "slight");
    put("merely", "mere");
    put("exceedingly", "exceeding");
    put("physically", "physical");
    put("enthusiastically", "enthusiastic");
    put("incredibly", "incredible");
    put("permanently", "permanent");
    put("entirely", "entire");
    put("surely", "sure");
    put("positively", "positive");
    put("negatively", "negative");
    put("outwards", "outward");
    put("devastatingly", "devastating");
    put("entirely", "entire");
    put("relatively", "relative");
    put("absolutely", "absolute");
    put("socially", "social");
    put("consciously", "conscious");
    put("industriously", "industrious");
    put("solely", "sole");
    put("asynchronously", "asynchronous");
    put("fortunately", "fortunate");
    put("unfortunately", "unfortunate");
    put("ideally", "ideal");
    put("privately", "private");
    put("reasonably", "reasonable");
    put("forcibly", "forcible");

    // TODO: add more or maybe use https://github.com/simplenlg/simplenlg?
    //put("", "");
  }};

  @Nullable
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) {
    String adverb = arguments.get("adverb");
    String noun = arguments.get("noun");
    String adjective = adverb2Adj.get(adverb);
    if (adjective != null && !adjective.equals(adverb)) {
      // we can't simply cut off "ly" because of cases like "simply" -> "simple" etc.
      match.setSuggestedReplacement(adjective + " " + noun);
    }
    return match;
  }
}
