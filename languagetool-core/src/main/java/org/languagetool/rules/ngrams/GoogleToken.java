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

import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tools.StringTools;

import java.util.ArrayList;
import java.util.List;

/**
 * A token as tokenized in the Google ngram index.
 * @since 3.2
 */
class GoogleToken {

  String token;
  int startPos;
  int endPos;

  GoogleToken(String token, int startPos, int endPos) {
    this.token = token;
    this.startPos = startPos;
    this.endPos = endPos;
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

}
