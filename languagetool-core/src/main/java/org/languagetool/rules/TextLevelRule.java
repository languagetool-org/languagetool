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

import org.languagetool.AnalyzedSentence;
import org.languagetool.markup.AnnotatedText;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

/**
 * A rule that considers the complete text, not just one sentence after
 * the other. Useful for rules that check coherency over sentence boundaries etc.
 * @since 2.7
 */
public abstract class TextLevelRule extends Rule {

  /**
   * @since 3.9
   */
  public RuleMatch[] match(List<AnalyzedSentence> sentences, AnnotatedText annotatedText) throws IOException {
    return match(sentences);
  }

  /**
   * @deprecated use {@link #match(List, AnnotatedText)} instead
   */
  public abstract RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException;

  /**
   * @since 3.7
   */
  public TextLevelRule() {
    super();
  }

  /**
   * Called by rules that require a translation of their messages.
   */
  public TextLevelRule(ResourceBundle messages) {
    super(messages);
  }

  @Override
  public int estimateContextForSureMatch() {
    return -1;
  }
  
  @Override
  public final RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    throw new RuntimeException("Not implemented for a text-level rule");
  }
  
  /**
   * Gives back the minimum number of paragraphs to check to give back a correct result.
   * Only used by LO office extension.
   * <ul>
   * <li>n == -1  --&gt; need to check full text (use only if really needed / bad performance)
   *              examples: AbstractWordCoherencyRule, GenericUnpairedBracketsRule, ...
   * <li>n == 0   --&gt; need only to check the current paragraph
   *              examples: MultipleWhitespaceRule, LongParagraphRule, ...
   * <li>n &gt;= 1   --&gt; need only to check n paragraphs around the current paragraph
   *              examples: ParagraphRepeatBeginningRule (n == 1), WordRepeatBeginningRule (n == 2), ...
   * </ul>
   */
  public abstract int minToCheckParagraph();

}
