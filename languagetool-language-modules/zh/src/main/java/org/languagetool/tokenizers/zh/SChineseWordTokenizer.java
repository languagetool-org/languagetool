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


import com.hankcs.hanlp.model.perceptron.PerceptronLexicalAnalyzer;
import com.hankcs.hanlp.seg.common.Term;

import org.languagetool.tokenizers.Tokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A word tokenizer of Simple Chinese
 * It can also be used for TC, but the result
 * is not as good as for SC.
 */
public class SChineseWordTokenizer implements Tokenizer {

  private PerceptronLexicalAnalyzer seg;

  public SChineseWordTokenizer() {

    try {
      seg = new PerceptronLexicalAnalyzer();
      seg.enableCustomDictionary(true);
      seg.enableNameRecognize(true);
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }

  @Override
  public List<String> tokenize(String text) {
    List<Term> termList = seg.seg(text);
    List<String> termStringList = new ArrayList<>();
    for (Term term: termList) {
      termStringList.add(term.toString());
    }
    return termStringList;
  }
}
