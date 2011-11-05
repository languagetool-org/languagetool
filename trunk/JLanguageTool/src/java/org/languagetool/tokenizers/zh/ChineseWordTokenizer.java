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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.ictclas4j.segment.SegTag;

import cn.com.cjf.CJFBeanFactory;
import cn.com.cjf.ChineseJF;
import org.languagetool.JLanguageTool;
import org.languagetool.tokenizers.Tokenizer;

public class ChineseWordTokenizer implements Tokenizer {

  private SegTag seg;

  private ChineseJF chinesdJF;

  private void init() {
    if (chinesdJF == null) {
      chinesdJF = CJFBeanFactory.getChineseJF();
    }
    if (seg == null) {
      InputStream coreDictIn = JLanguageTool.getDataBroker().getFromResourceDirAsStream(
          "/zh/coreDict.dct");
      InputStream bigramDictIn = JLanguageTool.getDataBroker().getFromResourceDirAsStream(
          "/zh/BigramDict.dct");
      InputStream personTaggerDctIn = JLanguageTool.getDataBroker().getFromResourceDirAsStream(
          "/zh/nr.dct");
      InputStream personTaggerCtxIn = JLanguageTool.getDataBroker().getFromResourceDirAsStream(
          "/zh/nr.ctx");
      InputStream transPersonTaggerDctIn = JLanguageTool.getDataBroker()
          .getFromResourceDirAsStream("/zh/tr.dct");
      InputStream transPersonTaggerCtxIn = JLanguageTool.getDataBroker()
          .getFromResourceDirAsStream("/zh/tr.ctx");
      InputStream placeTaggerDctIn = JLanguageTool.getDataBroker().getFromResourceDirAsStream(
          "/zh/ns.dct");
      InputStream placeTaggerCtxIn = JLanguageTool.getDataBroker().getFromResourceDirAsStream(
          "/zh/ns.ctx");
      InputStream lexTaggerCtxIn = JLanguageTool.getDataBroker().getFromResourceDirAsStream(
          "/zh/lexical.ctx");
      seg = new SegTag(1, coreDictIn, bigramDictIn, personTaggerDctIn, personTaggerCtxIn,
          transPersonTaggerDctIn, transPersonTaggerCtxIn, placeTaggerDctIn, placeTaggerCtxIn,
          lexTaggerCtxIn);
    }
  }

  @Override
  public List<String> tokenize(String text) {
    init();
    final ArrayList<String> ret = new ArrayList<String>();
    String result;
    try {
      result = seg.split(chinesdJF.chineseFan2Jan(text)).getFinalResult();
    } catch (Exception e) {
      // Occasionally, the Chinese tokenization/segment component throws NullPointerException or
      // ArrayIndexOutOfBoundsException, due to some internal bugs of ictclas4j. The reasons of the
      // bugs and how to resolve them are unknown now. In this case, we can just bypass the sentence
      // and return a empty List.
      return ret;
    }
    final String[] list = result.split(" ");

    for (int i = 0; i < list.length; i++) {
      ret.add(list[i]);
    }
    return ret;
  }
}
