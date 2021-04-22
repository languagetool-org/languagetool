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

import org.languagetool.tokenizers.Tokenizer;

import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.BasicTokenizer;
import cn.com.cjf.CJFBeanFactory;
import cn.com.cjf.ChineseJF;

public class ChineseWordTokenizer implements Tokenizer {

  private ChineseJF chinesdJF;

  private void init() {
    if (chinesdJF == null) {
      chinesdJF = CJFBeanFactory.getChineseJF();
    }
  }

  @Override
  public List<String> tokenize(String text) {
    init();
    try {
      List<Term> termList = BasicTokenizer.segment(text);
      List<String> words = new ArrayList<>(termList.size());
      for (Term term : termList) {
        words.add(term.toString());
      }
      return words;
    } catch (Exception e) {
      return new ArrayList<>(0);
    }
  }
}
