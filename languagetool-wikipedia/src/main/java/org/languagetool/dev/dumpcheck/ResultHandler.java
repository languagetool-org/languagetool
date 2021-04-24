/* LanguageTool, a natural language style checker 
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.dumpcheck;

import org.languagetool.Language;
import org.languagetool.rules.RuleMatch;

import java.util.List;

/**
 * Abstract class, sub classes handle the results found by LanguageTool, for example
 * by storing them to a database.
 * @since 2.4
 */
abstract class ResultHandler implements AutoCloseable {
  
  protected static final int CONTEXT_SIZE = 50;
  protected static final String MARKER_START = "<err>";
  protected static final String MARKER_END = "</err>";

  protected int sentenceCount = 0;
  protected int errorCount = 0;

  protected abstract void handleResult(Sentence sentence, List<RuleMatch> ruleMatches, Language language);

  private final int maxSentences;
  private final int maxErrors;

  protected ResultHandler(int maxSentences, int maxErrors) {
    this.maxSentences = maxSentences;
    this.maxErrors = maxErrors;
  }

  protected void checkMaxSentences(int i) {
    if (maxSentences > 0 && sentenceCount >= maxSentences) {
      throw new DocumentLimitReachedException(maxSentences);
    }
  }

  protected void checkMaxErrors(int i) {
    if (maxErrors > 0 && errorCount >= maxErrors) {
      throw new ErrorLimitReachedException(maxErrors);
    }
  }

}
