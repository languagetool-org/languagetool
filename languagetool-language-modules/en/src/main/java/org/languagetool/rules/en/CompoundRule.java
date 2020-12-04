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
import org.languagetool.rules.*;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.patterns.PatternTokenBuilder;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;

import java.io.IOException;
import java.util.*;

/**
 * Checks that compounds (if in the list) are not written as separate words.
 */
public class CompoundRule extends AbstractCompoundRule {

  // static to make sure this gets loaded only once:
  private static volatile CompoundRuleData compoundData;
  private static final Language AMERICAN_ENGLISH = Languages.getLanguageForShortCode("en-US");
  private static final List<DisambiguationPatternRule> ANTI_PATTERNS = makeAntiPatterns(Arrays.asList(
      Arrays.asList(
        new PatternTokenBuilder().tokenRegex("['’`´‘]").build(),
        new PatternTokenBuilder().token("re").build()
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
      )
  ), AMERICAN_ENGLISH);

  public CompoundRule(ResourceBundle messages) throws IOException {    
    super(messages,
            "This word is normally spelled with a hyphen.",
            "This word is normally spelled as one.", 
            "This expression is normally spelled as one or with a hyphen.",
            "Hyphenation problem");
    addExamplePair(Example.wrong("I now have a <marker>part time</marker> job."),
                   Example.fixed("I now have a <marker>part-time</marker> job."));
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
  protected CompoundRuleData getCompoundRuleData() {
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
}
