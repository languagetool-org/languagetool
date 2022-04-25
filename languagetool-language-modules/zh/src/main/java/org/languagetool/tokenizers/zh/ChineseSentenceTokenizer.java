/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tokenizers.zh;

import java.util.ArrayList;
import java.util.List;

import com.hankcs.hanlp.utility.SentencesUtil;
import org.languagetool.tokenizers.SentenceTokenizer;

public class ChineseSentenceTokenizer implements SentenceTokenizer {

  @Override
  public List<String> tokenize(String text) {
    List<String> result = new ArrayList<>();
    StringBuilder whitespace = new StringBuilder();
    StringBuilder nonWhitespace = new StringBuilder();
    // SentencesUtil.toSentenceList() ignores whitespace, so we add it ourselves so match positions are not messed up:
    for (int i = 0; i < text.length(); i++) {
      if (Character.isWhitespace(text.charAt(i))) {
        if (nonWhitespace.length() > 0) {
          result.addAll(SentencesUtil.toSentenceList(nonWhitespace.toString()));
          nonWhitespace.setLength(0);
        }
        whitespace.append(text.charAt(i));  // keep collecting whitespaces
      } else {
        if (whitespace.length() > 0) {
          result.add(whitespace.toString());
          whitespace.setLength(0);
        }
        nonWhitespace.append(text.charAt(i));
      }
    }
    if (whitespace.length() > 0) {
      result.add(whitespace.toString());
    }
    if (nonWhitespace.length() > 0) {
      result.addAll(SentencesUtil.toSentenceList(nonWhitespace.toString()));
    }
    return result;
  }

  /** Note: does have no effect for Chinese */
  @Override
  public void setSingleLineBreaksMarksParagraph(boolean lineBreakParagraphs) {
  }

  /** Note: will always return {@code false} */
  @Override
  public boolean singleLineBreaksMarksPara() {
    return false;
  }
}
