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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ictclas4j.segment.SegTag;
import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.tokenizers.Tokenizer;

import cn.com.cjf.CJFBeanFactory;
import cn.com.cjf.ChineseJF;

public class ChineseWordTokenizer implements Tokenizer {

  private SegTag seg;
  private ChineseJF chinesdJF;

  private void init() {
    if (chinesdJF == null) {
      chinesdJF = CJFBeanFactory.getChineseJF();
    }
    if (seg == null) {
      ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
      try (InputStream coreDictIn = dataBroker.getFromResourceDirAsStream("/zh/coreDict.dct");
           InputStream bigramDictIn = dataBroker.getFromResourceDirAsStream("/zh/BigramDict.dct");
           InputStream personTaggerDctIn = dataBroker.getFromResourceDirAsStream("/zh/nr.dct");
           InputStream personTaggerCtxIn = dataBroker.getFromResourceDirAsStream("/zh/nr.ctx");
           InputStream transPersonTaggerDctIn = dataBroker.getFromResourceDirAsStream("/zh/tr.dct");
           InputStream transPersonTaggerCtxIn = dataBroker.getFromResourceDirAsStream("/zh/tr.ctx");
           InputStream placeTaggerDctIn = dataBroker.getFromResourceDirAsStream("/zh/ns.dct");
           InputStream placeTaggerCtxIn = dataBroker.getFromResourceDirAsStream("/zh/ns.ctx");
           InputStream lexTaggerCtxIn = dataBroker.getFromResourceDirAsStream("/zh/lexical.ctx")) {
        seg = new SegTag(1, coreDictIn, bigramDictIn, personTaggerDctIn, personTaggerCtxIn,
                transPersonTaggerDctIn, transPersonTaggerCtxIn, placeTaggerDctIn, placeTaggerCtxIn,
                lexTaggerCtxIn);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public List<String> tokenize(String text) {
    init();
    String result;
    try {
      result = seg.split(chinesdJF.chineseFan2Jan(text)).getFinalResult();
      // a hacky workaround for http://sourceforge.net/p/languagetool/bugs/186/ and
      // http://code.google.com/p/ictclas4j/issues/detail?id=14 which otherwise causes a StringIndexOutOfBoundsException
      // TODO: fix the original cause
      result = result.replace("始##始年/t", "年/t");
    } catch (Exception e) {
      // Occasionally, the Chinese tokenization/segment component throws NullPointerException or
      // ArrayIndexOutOfBoundsException, due to some internal bugs of ictclas4j. The reasons of the
      // bugs and how to resolve them are unknown now. In this case, we can just bypass the sentence
      // and return a empty List.
      return new ArrayList<>();
    }
    String[] list = result.split(" ");
    return Arrays.asList(list);
  }
}
