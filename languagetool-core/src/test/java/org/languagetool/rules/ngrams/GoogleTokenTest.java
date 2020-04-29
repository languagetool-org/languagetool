/* LanguageTool, a natural language style checker 
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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

import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Demo;
import org.languagetool.tagging.Tagger;
import org.languagetool.tokenizers.WordTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class GoogleTokenTest {
  
  @Test
  public void testTokenization() {
    List<GoogleToken> tokens = GoogleToken.getGoogleTokens("This, isn't a test.", false, new MyWordTokenizer());
    assertThat(tokens.get(0).token, is("This"));
    assertThat(tokens.get(0).posTags.toString(), is("[]"));
    assertThat(tokens.get(1).token, is(","));
    assertThat(tokens.get(2).token, is("isn"));
    assertThat(tokens.get(3).token, is("'t"));
    assertThat(tokens.get(4).token, is("a"));
    assertThat(tokens.get(5).token, is("test"));
    assertThat(tokens.get(6).token, is("."));
  }

  @Test
  public void testTokenizationWithPosTag() throws IOException {
    JLanguageTool lt = new JLanguageTool(new PosTaggingDemo());
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("This, isn't a test.");
    List<GoogleToken> tokens = GoogleToken.getGoogleTokens(analyzedSentence, false, new MyWordTokenizer());
    assertThat(tokens.get(0).token, is("This"));
    assertThat(tokens.get(0).posTags.toString(), is("[This/DT]"));
    assertThat(tokens.get(1).token, is(","));
    assertThat(tokens.get(1).posTags.toString(), is("[,/null]"));
    assertThat(tokens.get(2).token, is("isn"));
    assertThat(tokens.get(3).token, is("'t"));
    assertThat(tokens.get(4).token, is("a"));
    assertThat(tokens.get(5).token, is("test"));
    assertThat(tokens.get(5).posTags.toString(), is("[test/NN]"));
    assertThat(tokens.get(6).token, is("."));
  }

  class PosTaggingDemo extends Demo {
    @Override
    public Tagger getTagger() {
      return new Tagger() {
        @Override
        public List<AnalyzedTokenReadings> tag(List<String> sentenceTokens) {
          List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
          int pos = 0;
          for (String word : sentenceTokens) {
            List<AnalyzedToken> l = new ArrayList<>();
            switch (word) {
              case "This": l.add(new AnalyzedToken(word, "DT", word)); break;
              case "is": l.add(new AnalyzedToken(word, "VBZ", word)); break;
              case "test": l.add(new AnalyzedToken(word, "NN", word)); break;
              default: l.add(new AnalyzedToken(word, null, word)); break;
            }
            tokenReadings.add(new AnalyzedTokenReadings(l, pos));
            pos += word.length();
          }
          return tokenReadings;
        }
        @Override
        public AnalyzedTokenReadings createNullToken(String token, int startPos) {
          return null;
        }
        @Override
        public AnalyzedToken createToken(String token, String posTag) {
          return null;
        }
      };
    }
  }

  private class MyWordTokenizer extends WordTokenizer {
    @Override
    public List<String> tokenize(String text) {
      List<String> tokens = super.tokenize(text);
      String prev = null;
      Stack<String> l = new Stack<>();
      for (String token : tokens) {
        if ("'".equals(prev)) {
          if (token.equals("t")) {
            l.pop();
            l.push("'t");
          } else {
            l.push(token);
          }
        } else {
          l.push(token);
        }
        prev = token;
      }
      return l;
    }
  }

}