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
import org.languagetool.rules.*;
import org.languagetool.rules.patterns.PatternTokenBuilder;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.util.*;

/**
 * Checks that compounds (if in the list) are not written as separate words.
 */
public class CompoundRule extends AbstractCompoundRule {
  
  private static SpellingCheckRule englishSpellerRule;

  // static to make sure this gets loaded only once:
  private static volatile CompoundRuleData compoundData;
  private static final Language AMERICAN_ENGLISH = Languages.getLanguageForShortCode("en-US");
  private static final List<DisambiguationPatternRule> ANTI_PATTERNS = makeAntiPatterns(Arrays.asList(
      Arrays.asList(
        new PatternTokenBuilder().tokenRegex("['’`´‘]").build(),
        new PatternTokenBuilder().token("re").build()
      ),
      Arrays.asList( // We well received your email
        new PatternTokenBuilder().posRegex("SENT_START|CC|PCT").build(),
        new PatternTokenBuilder().tokenRegex("we|you|they|I|s?he|it").build(),
        new PatternTokenBuilder().token("well").build(),
        new PatternTokenBuilder().posRegex("VB.*").build()
      ),
      Arrays.asList(
        new PatternTokenBuilder().tokenRegex("and|&").build(),
        new PatternTokenBuilder().token("co").build()
      ),
      Arrays.asList( // off-key
        new PatternTokenBuilder().token("power").build(),
        new PatternTokenBuilder().token("off").build(),
        new PatternTokenBuilder().token("key").build()
      ),
      Arrays.asList( // see saw seen
        new PatternTokenBuilder().token("see").build(),
        new PatternTokenBuilder().token("saw").build(),
        new PatternTokenBuilder().token("seen").build()
      ),
      Arrays.asList( // moving forward looking for ...
        new PatternTokenBuilder().token("forward").build(),
        new PatternTokenBuilder().token("looking").build(),
        new PatternTokenBuilder().posRegex("IN|TO").build()
      ),
      Arrays.asList( // Go through the store front door
        new PatternTokenBuilder().token("store").build(),
        new PatternTokenBuilder().token("front").build(),
        new PatternTokenBuilder().tokenRegex("doors?").build()
      ),
      Arrays.asList( // It goes from surface to surface
        new PatternTokenBuilder().token("from").build(),
        new PatternTokenBuilder().token("surface").build(),
        new PatternTokenBuilder().token("to").build(),
        new PatternTokenBuilder().token("surface").build()
      ),
      Arrays.asList( // year end
        new PatternTokenBuilder().tokenRegex("senior|junior").build(),
        new PatternTokenBuilder().token("year").build(),
        new PatternTokenBuilder().token("end").build()
      ),
      Arrays.asList( // under investment 
        new PatternTokenBuilder().token("under").build(),
        new PatternTokenBuilder().token("investment").build(),
        new PatternTokenBuilder().token("banking").build()
      ),
      Arrays.asList( // spring clean
        new PatternTokenBuilder().token("spring").build(),
        new PatternTokenBuilder().tokenRegex("cleans?|cleaned|cleaning").build(),
        new PatternTokenBuilder().tokenRegex("up|the|my|our|his|her").build()
      ),
      Arrays.asList( // Serie A team (A-Team)
        new PatternTokenBuilder().tokenRegex("series?").build(),
        new PatternTokenBuilder().tokenRegex("a").build()
      ),
      Arrays.asList( // They had a hard time sharing their ... 
        new PatternTokenBuilder().token("hard").build(),
        new PatternTokenBuilder().token("time").build(),
        new PatternTokenBuilder().pos("VBG").build()
      ),
      Arrays.asList( // the first ever green bond by a municipality
        new PatternTokenBuilder().token("first").build(),
        new PatternTokenBuilder().tokenRegex("ever").build(),
        new PatternTokenBuilder().tokenRegex("green").build()
      )
  ), AMERICAN_ENGLISH);

  public CompoundRule(ResourceBundle messages, Language lang, UserConfig userConfig) throws IOException {    
    super(messages, lang, userConfig,
            "This word is normally spelled with a hyphen.",
            "This word is normally spelled as one.", 
            "This expression is normally spelled as one or with a hyphen.",
            "Compound");
    addExamplePair(Example.wrong("I now have a <marker>part time</marker> job."),
                   Example.fixed("I now have a <marker>part-time</marker> job."));
    setUrl(Tools.getUrl("https://languagetool.org/insights/post/hyphen/"));
    if (englishSpellerRule == null) {
      englishSpellerRule = lang.getDefaultSpellingRule(messages);
    }
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
    return englishSpellerRule.isMisspelled(word);
  }
}
