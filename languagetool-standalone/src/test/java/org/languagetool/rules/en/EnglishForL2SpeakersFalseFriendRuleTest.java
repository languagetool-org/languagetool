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

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.languagetool.*;
import org.languagetool.broker.ResourceDataBroker;
import org.languagetool.rules.ConfusionPair;
import org.languagetool.rules.ConfusionSetLoader;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.ngrams.FakeLanguageModel;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.rules.patterns.FalseFriendRuleLoader;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.tools.StringTools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.languagetool.JLanguageTool.FALSE_FRIEND_FILE;

public class EnglishForL2SpeakersFalseFriendRuleTest {

  @Test
  @Disabled("had problems with running it locally (false-friends.xml not found)")
  public void testMessageDetailData() throws IOException {
    List<String> langs = Arrays.asList("nl", "de", "fr", "es");
    //List<String> langs = Arrays.asList("es");
    Language en = Languages.getLanguageForShortCode("en");
    ShortDescriptionProvider descProvider = new ShortDescriptionProvider();
    for (String lang : langs) {
      Map<String, List<ConfusionPair>> ngramData = getFalseFriendNgramData(en, lang);
      Set<String> falseFriendsDetailData = getFalseFriendsDetailData(en, lang);
      for (String s : ngramData.keySet()) {
        String desc = descProvider.getShortDescription(s, en);
        boolean entryInXml = falseFriendsDetailData.contains(s) || falseFriendsDetailData.contains(StringTools.uppercaseFirstChar(s));
        if (desc == null && !entryInXml)  {
          System.out.println("[" + lang + "] WARNING: no entry for '" + s + "' found in en/word_definitions.txt or false-friends.xml, " +
                  "user will get a less useful message for false friends");
        }
      }
    }
  }

  private Map<String, List<ConfusionPair>> getFalseFriendNgramData(Language en, String lang) throws IOException {
    String path = "/en/confusion_sets_l2_" + lang + ".txt";
    ConfusionSetLoader confusionSetLoader = new ConfusionSetLoader(en);
    ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
    try (InputStream confusionSetStream = dataBroker.getFromResourceDirAsStream(path)) {
      return confusionSetLoader.loadConfusionPairs(confusionSetStream);
    }
  }

  private Set<String> getFalseFriendsDetailData(Language en, String l1Code) throws IOException {
    Language l1 = Languages.getLanguageForShortCode(l1Code);
    FalseFriendRuleLoader ruleLoader = new FalseFriendRuleLoader(l1);
    String ffFilename = JLanguageTool.getDataBroker().getRulesDir() + "/" + FALSE_FRIEND_FILE;
    List<AbstractPatternRule> rules = ruleLoader.getRules(new File(ffFilename), en, l1);
    Set<String> patternsWithDetails = new HashSet<>();
    for (AbstractPatternRule rule : rules) {
      for (PatternToken patternToken : rule.getPatternTokens()) {
        String[] parts = patternToken.getString().split("\\|");
        Collections.addAll(patternsWithDetails, parts);
      }
    }
    return patternsWithDetails;
  }

  @Test
  public void testRule() throws IOException {
    Language en = Languages.getLanguageForShortCode("en");
    Language fr = Languages.getLanguageForShortCode("fr");
    Map<String, Integer> map = new HashMap<>();
    // first test:
    map.put("will achieve", 1);
    map.put("will complete", 10);
    map.put("achieve her", 1);
    map.put("complete her", 5);
    map.put("achieve her task", 1);
    map.put("complete her task", 10);
    // second test:
    map.put("was completed", 100);
    map.put("was completed .", 100);
    map.put("form was completed", 100);
    FakeLanguageModel lm = new FakeLanguageModel(map);
    Rule rule = new EnglishForFrenchFalseFriendRule(TestTools.getEnglishMessages(), lm, fr, en);
    JLanguageTool lt = new JLanguageTool(en, fr);

    RuleMatch[] matches1 = rule.match(lt.getAnalyzedSentence("She will achieve her task."));
    MatcherAssert.assertThat(matches1.length, is(1));
    Assertions.assertTrue(matches1[0].getMessage().contains("\"achieve\" (English) means \"réaliser\" (French)"));

    RuleMatch[] matches2 = rule.match(lt.getAnalyzedSentence("The code only worked if the form was achieved."));
    MatcherAssert.assertThat(matches2.length, is(1));
    Assertions.assertTrue(matches2[0].getMessage().contains("\"achieve\" (English) means \"réaliser\" (French)"));
  }

}
