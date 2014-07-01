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
package org.languagetool.dev.errorcorpus;

import org.languagetool.markup.AnnotatedText;
import org.languagetool.rules.RuleMatch;

import java.util.List;

/**
 * @since 2.7
 */
public class ErrorSentence {

  private final String markupText;
  private final AnnotatedText annotatedText;
  private final List<Error> errors;

  ErrorSentence(String markupText, AnnotatedText annotatedText, List<Error> errors) {
    this.markupText = markupText;
    this.annotatedText = annotatedText;
    this.errors = errors;
  }

  public boolean hasErrorCoveredByMatchAndGoodFirstSuggestion(RuleMatch match) {
    if (hasErrorCoveredByMatch(match)) {
      List<String> suggestion = match.getSuggestedReplacements();
      if (suggestion.size() > 0) {
        String firstSuggestion = suggestion.get(0);
        for (Error error : errors) {
          if (error.getCorrection().equals(firstSuggestion)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public boolean hasErrorCoveredByMatch(RuleMatch match) {
    for (Error error : errors) {
      if (match.getFromPos() <= error.getStartPos() && match.getToPos() >= error.getEndPos()) {
        return true;
      }
    }
    return false;
  }

  public String getMarkupText() {
    return markupText;
  }

  public AnnotatedText getAnnotatedText() {
    return annotatedText;
  }

  public List<Error> getErrors() {
    return errors;
  }
}
