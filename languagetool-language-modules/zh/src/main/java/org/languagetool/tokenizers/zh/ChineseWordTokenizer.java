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

import java.util.List;
import java.util.stream.Collectors;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import org.languagetool.tokenizers.Tokenizer;

import com.hankcs.hanlp.seg.common.Term;

public class ChineseWordTokenizer implements Tokenizer {

  private static final Segment SEGMENT = HanLP.newSegment()
    .enableAllNamedEntityRecognize(false)
    .enableCustomDictionary(false)
    .enablePartOfSpeechTagging(true);

  @Override
  public List<String> tokenize(String text) {
    List<Term> termList = SEGMENT.seg(text);

    return termList.stream().map(Term::toString).collect(Collectors.toList());
  }
}
