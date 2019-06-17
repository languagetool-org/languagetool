/* LanguageTool, a natural language style checker 
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.ngrams;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Experimental;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tools.StringTools;

import java.util.*;

/**
 * A token as tokenized in the Google ngram index.
 * @since 3.2
 */
class GoogleToken {

  final String token;
  final int startPos;
  final int endPos;
  final Set<AnalyzedToken> posTags;

  GoogleToken(String token, int startPos, int endPos) {
    this(token, startPos, endPos, Collections.emptySet());
  }

  GoogleToken(String token, int startPos, int endPos, Set<AnalyzedToken> posTags) {
    this.token = "’".equals(token) ? "'" : token;  // Google seems to have indexed the apostrophe always like this
    this.startPos = startPos;
    this.endPos = endPos;
    this.posTags = posTags;
  }

  Set<AnalyzedToken> getPosTags() {
    return posTags;
  }

  boolean isWhitespace() {
    return StringTools.isWhitespace(token);
  }

  @Override
  public String toString() {
    return token;
  }

  // Tokenization in google ngram corpus is different from LT tokenization (e.g. {@code you ' re} -> {@code you 're}),
  // so we use getTokenizer() and simple ignore the LT tokens.
  static List<GoogleToken> getGoogleTokens(String sentence, boolean addStartToken, Tokenizer wordTokenizer) {
    List<GoogleToken> result = new ArrayList<>();
    if (addStartToken) {
      result.add(new GoogleToken(LanguageModel.GOOGLE_SENTENCE_START, 0, 0));
    }
    List<String> tokens = wordTokenizer.tokenize(sentence);
    int startPos = 0;
    for (String token : tokens) {
      if (!StringTools.isWhitespace(token)) {
        result.add(new GoogleToken(token, startPos, startPos+token.length()));
      }
      startPos += token.length();
    }
    return result;
  }

  // Tokenization in google ngram corpus is different from LT tokenization (e.g. {@code you ' re} -> {@code you 're}),
  // so we use getTokenizer() and simple ignore the LT tokens. Also adds POS tags from original sentence if trivially possible.
  static List<GoogleToken> getGoogleTokens(AnalyzedSentence sentence, boolean addStartToken, Tokenizer wordTokenizer) {
    List<GoogleToken> result = new ArrayList<>();
    if (addStartToken) {
      result.add(new GoogleToken(LanguageModel.GOOGLE_SENTENCE_START, 0, 0));
    }
    List<String> tokens = wordTokenizer.tokenize(sentence.getText());
    int startPos = 0;
    for (String token : tokens) {
      if (!StringTools.isWhitespace(token)) {
        int endPos = startPos + token.length();
        Set<AnalyzedToken> pos = findOriginalAnalyzedTokens(sentence, startPos, endPos);
        GoogleToken gToken = new GoogleToken(token, startPos, endPos, pos);
        result.add(gToken);
      }
      startPos += token.length();
    }
    return result;
  }

  private static Set<AnalyzedToken> findOriginalAnalyzedTokens(AnalyzedSentence sentence, int startPos, int endPos) {
    Set<AnalyzedToken> result = new HashSet<>();
    for (AnalyzedTokenReadings tokens : sentence.getTokensWithoutWhitespace()) {
      if (tokens.getStartPos() == startPos && tokens.getEndPos() == endPos) {
        for (AnalyzedToken analyzedToken : tokens.getReadings()) {
          result.add(analyzedToken);
        }
      }
    }
    return result;
  }

}
