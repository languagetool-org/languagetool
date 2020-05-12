/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2018 Fabian Richter
 *  *
 *  * This library is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU Lesser General Public
 *  * License as published by the Free Software Foundation; either
 *  * version 2.1 of the License, or (at your option) any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this library; if not, write to the Free Software
 *  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  * USA
 *
 */

package org.languagetool.rules;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.languagetool.*;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DictionaryMatchFilterTest {

  static class ForbiddenWordsRule extends Rule {
    private final Set<String> words;

    public ForbiddenWordsRule(Set<String> words) {
      this.words = words;
    }

    @Override
    public String getId() {
      return "DictionaryMatchFilterTestRule";
    }

    @Override
    public String getDescription() {
      return "Test";
    }

    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
      List<RuleMatch> matches = new LinkedList<>();
      for (AnalyzedTokenReadings token : sentence.getTokensWithoutWhitespace()) {
        String word = token.getToken();
        if (words.contains(word)) {
          matches.add(new RuleMatch(this, sentence, token.getStartPos(), token.getEndPos(), "Forbidden word: " + word));
        }
      }
      return matches.toArray(new RuleMatch[0]);
    }
  }

  private JLanguageTool getLT(Set<String> forbidden, Set<String> dictionary, boolean addFilter) {
    UserConfig config = new UserConfig(new ArrayList<>(dictionary));
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("en-US"), null, config);
    Rule testRule = new ForbiddenWordsRule(forbidden);
    lt.addRule(testRule);
    lt.enableRule(testRule.getId());
    if (addFilter) {
      lt.addMatchFilter(new DictionaryMatchFilter(config));
    }
    return lt;
  }

  private boolean isForbiddenWordMatch(String word, RuleMatch match) {
    return match.getMessage().equals("Forbidden word: " + word);
  }
  private boolean isSpellingMatch(RuleMatch match) {
    return match.getMessage().contains("Possible spelling mistake found");
  }

  @Test
  public void matchesWithoutFilter() throws IOException {
    JLanguageTool lt = getLT(Sets.newHashSet("foo", "bar", "foobar"), Collections.emptySet(), false);
    List<RuleMatch> matches = lt.check("This is foo. Very bar of you! Even foobar, one might say.");
    assertEquals(3, matches.size());
    assertTrue(isForbiddenWordMatch("foo", matches.get(0)));
    assertTrue(isForbiddenWordMatch("bar", matches.get(1)));
    assertTrue(isForbiddenWordMatch("foobar", matches.get(2)));
  }


  @Test
  public void spellingRuleMatches() throws IOException {
    JLanguageTool lt = getLT(Collections.emptySet(), Collections.emptySet(), false);
    assertTrue(isSpellingMatch(lt.check("This is a mistak").get(0)));
    JLanguageTool lt2 = getLT(Collections.emptySet(), Sets.newHashSet("mistak"), true);
    assertEquals(lt2.check("This is a mistak.").size(), 0);
    assertTrue(isSpellingMatch(lt2.check("This is another mistke.").get(0)));
  }

  @Test
  public void filter() throws IOException {
    JLanguageTool lt = getLT(Sets.newHashSet("foo", "bar"), Sets.newHashSet("foo"), true);
    List<RuleMatch> matches = lt.check("This is foo. This is bar.");
    assertEquals(matches.size(), 1);
    assertTrue(isForbiddenWordMatch("bar", matches.get(0)));
  }

}
