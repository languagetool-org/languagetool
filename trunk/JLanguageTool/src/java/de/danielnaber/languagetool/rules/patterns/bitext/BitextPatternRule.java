/* LanguageTool, a natural language style checker 
 * Copyright (C) 2010 Marcin Miłkowski (www.languagetool.org)
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

package de.danielnaber.languagetool.rules.patterns.bitext;

import java.io.IOException;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.rules.bitext.BitextRule;
import de.danielnaber.languagetool.rules.patterns.PatternRule;

/**
 * A bitext pattern rule class. A BitextPatternRule describes a language error and 
 * can test whether a given pre-analyzed pair of source and target text 
 * contains that error using the {@link Rule#match} method. It uses the syntax
 * of XML files similar to normal PatternRules.
 * 
 * @author Marcin Miłkowski
 */
public class BitextPatternRule extends BitextRule {

  private final PatternRule srcRule;
  private final PatternRule trgRule;
  
  BitextPatternRule(final PatternRule src, final PatternRule trg) {    
    srcRule = src;
    trgRule = trg;
  }
  
  public PatternRule getSrcRule() {
    return srcRule;        
  }
  
  public PatternRule getTrgRule() {
    return trgRule;
  }
  
  @Override
  public String getDescription() {
    return srcRule.getDescription();
  }

  @Override
  public String getMessage() {
    return trgRule.getMessage();
  }
  
  @Override
  public String getId() {
    return srcRule.getId();
  }

  /**
   * This method always returns an empty array.
   */
  @Override
  public RuleMatch[] match(AnalyzedSentence text) throws IOException {
    return new RuleMatch[0];
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sourceText,
      AnalyzedSentence targetText) throws IOException {
    if (srcRule.match(sourceText).length > 0)  {    
      return trgRule.match(targetText);
    }
    return new RuleMatch[0];
  }

  @Override
  public void reset() {
    // TODO Auto-generated method stub

  }

}
