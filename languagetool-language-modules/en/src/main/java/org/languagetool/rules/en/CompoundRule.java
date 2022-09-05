/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.UserConfig;
import org.languagetool.rules.AbstractCompoundRule;
import org.languagetool.rules.CompoundRuleData;
import org.languagetool.rules.Example;
import org.languagetool.rules.patterns.PatternTokenBuilder;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import static org.languagetool.rules.patterns.PatternRuleBuilderHelper.token;
import static org.languagetool.rules.patterns.PatternRuleBuilderHelper.tokenRegex;

/**
 * Checks that compounds (if in the list) are not written as separate words.
 */
public class CompoundRule extends AbstractCompoundRule {
  
  // static to make sure this gets loaded only once:
  private static volatile CompoundRuleData compoundData;
  private static final Language AMERICAN_ENGLISH = Languages.getLanguageForShortCode("en-US");
  private static final List<DisambiguationPatternRule> ANTI_PATTERNS = makeAntiPatterns(Arrays.asList(
      Arrays.asList(
        tokenRegex("['’`´‘]"),
        token("re")
      ),
      Arrays.asList( // We well received your email
        new PatternTokenBuilder().posRegex("SENT_START|CC|PCT").build(),
        tokenRegex("we|you|they|I|s?he|it"),
        token("well"),
        new PatternTokenBuilder().posRegex("VB.*").build()
      ),
      Arrays.asList(
        tokenRegex("and|&"),
        token("co")
      ),
      Arrays.asList( // off-key
        token("power"),
        token("off"),
        token("key")
      ),
      Arrays.asList( // see saw seen
        token("see"),
        token("saw"),
        token("seen")
      ),
      Arrays.asList( // moving forward looking for ...
        token("forward"),
        token("looking"),
        new PatternTokenBuilder().posRegex("IN|TO").build()
      ),
      Arrays.asList( // Go through the store front door
        token("store"),
        token("front"),
        tokenRegex("doors?")
      ),
      Arrays.asList( // It goes from surface to surface
        token("from"),
        token("surface"),
        token("to"),
        token("surface")
      ),
      Arrays.asList( // year end
        tokenRegex("senior|junior"),
        token("year"),
        token("end")
      ),
      Arrays.asList( // under investment 
        token("under"),
        token("investment"),
        token("banking")
      ),
      Arrays.asList( // spring clean
        token("spring"),
        tokenRegex("cleans?|cleaned|cleaning"),
        tokenRegex("up|the|my|our|his|her")
      ),
      Arrays.asList( // Serie A team (A-Team)
        tokenRegex("series?"),
        tokenRegex("a")
      ),
      Arrays.asList( // They had a hard time sharing their ... 
        token("hard"),
        token("time"),
        new PatternTokenBuilder().pos("VBG").build()
      ),
      Arrays.asList( // the first ever green bond by a municipality
        token("first"),
        tokenRegex("ever"),
        tokenRegex("green")
      ),
      Arrays.asList( // inter-state.com
        tokenRegex(".+"),
        token("."),
        tokenRegex("(com|io|de|nl|co|net|org|es)")
      )
  ), AMERICAN_ENGLISH);
  private final Language english;

  public CompoundRule(ResourceBundle messages, Language english, UserConfig userConfig) throws IOException {
    super(messages, english, userConfig,
            "This word is normally spelled with a hyphen.",
            "This word is normally spelled as one.", 
            "This expression is normally spelled as one or with a hyphen.",
            "Compound");
    this.english = english;
    addExamplePair(Example.wrong("I now have a <marker>part time</marker> job."),
                   Example.fixed("I now have a <marker>part-time</marker> job."));
    setUrl(Tools.getUrl("https://languagetool.org/insights/post/hyphen/"));
  }

  @Override
  public String getId() {
    return "EN_COMPOUNDS";
  }

  @Override
  public String getDescription() {
    return "Hyphenated words, e.g., 'case-sensitive' instead of 'case sensitive'";
  }

  @Override
  public CompoundRuleData getCompoundRuleData() {
    CompoundRuleData data = compoundData;
    if (data == null) {
      synchronized (CompoundRule.class) {
        data = compoundData;
        if (data == null) {
          compoundData = data = new CompoundRuleData("/en/compounds.txt");
        }
      }
    }

    return data;
  }

  @Override
  public List<DisambiguationPatternRule> getAntiPatterns() {
    return ANTI_PATTERNS;
  }
  
  @Override
  public boolean isMisspelled(String word) throws IOException {
    //return !EnglishTagger.INSTANCE.tag(Arrays.asList(word)).get(0).isTagged();
    return Objects.requireNonNull(english.getDefaultSpellingRule()).isMisspelled(word);
  }
}
