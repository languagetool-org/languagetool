/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Marcin Milkowski (http://www.languagetool.org)
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
package org.languagetool.rules.spelling;

import java.io.IOException;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;
import org.languagetool.Language;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

/**
 * An abstract rule for spellchecking rules.
 *
 * If you want to have spellchecker which is not based on hunspell,
 * you should simply create a subclass of this class. 
 *
 * @author Marcin Miłkowski
 */
public abstract class SpellingCheckRule extends Rule {

  protected final Language language;

  @Override
  public abstract String getId();

  public SpellingCheckRule(final ResourceBundle messages, final Language language) {
    super(messages);
    this.language = language;
  }

  @Override
  public abstract String getDescription();

  @Override
  public abstract RuleMatch[] match(AnalyzedSentence text) throws IOException;

  @Override
  public boolean isSpellingRule() {
    return true;
  }

  @Override
  public void reset() {
  }

}
