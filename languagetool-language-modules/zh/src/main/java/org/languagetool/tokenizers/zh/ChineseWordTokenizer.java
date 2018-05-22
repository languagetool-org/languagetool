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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hankcs.hanlp.model.perceptron.PerceptronLexicalAnalyzer;
import com.hankcs.hanlp.seg.common.Term;

import org.languagetool.tokenizers.Tokenizer;


public class ChineseWordTokenizer implements Tokenizer {

  private PerceptronLexicalAnalyzer seg;

  private void init() {
    if (seg == null) {
      try {
        seg = new PerceptronLexicalAnalyzer();
        seg.enableCustomDictionary(true);
        seg.enableNameRecognize(true);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public List<String> tokenize(String text) {
    init();
    List<Term> termList = seg.seg(text);
    List<String> result = new ArrayList<>();
    for (Term term: termList) {
      result.add(term.toString());
    }
    return result;
  }
}
