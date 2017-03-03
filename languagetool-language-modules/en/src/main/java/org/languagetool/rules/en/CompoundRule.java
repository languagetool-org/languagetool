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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.AbstractCompoundRule;
import org.languagetool.rules.CompoundRuleData;
import org.languagetool.rules.Example;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.patterns.PatternTokenBuilder;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;

/**
 * Checks that compounds (if in the list) are not written as separate words.
 */
public class CompoundRule extends AbstractCompoundRule {

  // static to make sure this gets loaded only once:
  private static final CompoundRuleData compoundData = new CompoundRuleData("/en/compounds.txt");
  private static final Language AMERICAN_ENGLISH = new AmericanEnglish();
  private static List<DisambiguationPatternRule> antiDisambiguationPatterns = null;
  private static final List<List<PatternToken>> ANTI_PATTERNS = Arrays.asList(
      Arrays.asList(
          new PatternTokenBuilder().tokenRegex("['´]").build(),
          new PatternTokenBuilder().token("re").build()
        )
      );

  public CompoundRule(ResourceBundle messages) throws IOException {    
    super(messages,
            "This word is normally spelled with hyphen.", 
            "This word is normally spelled as one.", 
            "This expression is normally spelled as one or with hyphen.",
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
    return compoundData;
  }

  @Override
  public List<DisambiguationPatternRule> getAntiPatterns() {
    if (antiDisambiguationPatterns == null) {
      antiDisambiguationPatterns = makeAntiPatterns(ANTI_PATTERNS, AMERICAN_ENGLISH);
    }
    return antiDisambiguationPatterns;
  }
}
