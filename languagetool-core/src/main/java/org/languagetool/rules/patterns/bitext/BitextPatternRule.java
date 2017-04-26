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

package org.languagetool.rules.patterns.bitext;

import java.io.IOException;

import org.languagetool.AnalyzedSentence;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.bitext.BitextRule;
import org.languagetool.rules.patterns.AbstractPatternRule;

/**
 * A bitext pattern rule class. A BitextPatternRule describes a language error and 
 * can test whether a given pre-analyzed pair of source and target text 
 * contains that error using the {@link Rule#match} method. It uses the syntax
 * of XML files similar to normal PatternRules.
 * 
 * @author Marcin Miłkowski
 */
public class BitextPatternRule extends BitextRule {

  private final AbstractPatternRule srcRule;
  private final AbstractPatternRule trgRule;
  
  BitextPatternRule(AbstractPatternRule src, AbstractPatternRule trg) {    
    srcRule = src;
    trgRule = trg;
  }
  
  public AbstractPatternRule getSrcRule() {
    return srcRule;        
  }
  
  public AbstractPatternRule getTrgRule() {
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
   * Use {@link #match(org.languagetool.AnalyzedSentence, org.languagetool.AnalyzedSentence)} instead.
   */
  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    return new RuleMatch[0];
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sourceSentence,
      AnalyzedSentence targetSentence) throws IOException {
    if (srcRule.match(sourceSentence).length > 0)  {
      return trgRule.match(targetSentence);
    }
    return new RuleMatch[0];
  }

}
