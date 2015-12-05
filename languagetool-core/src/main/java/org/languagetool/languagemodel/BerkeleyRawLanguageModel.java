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
package org.languagetool.languagemodel;

import edu.berkeley.nlp.lm.io.LmReaders;
import edu.berkeley.nlp.lm.map.NgramMapWrapper;
import edu.berkeley.nlp.lm.util.LongRef;
import org.languagetool.Experimental;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Just for testing - at least with the pre-built language models from
 * http://tomato.banatao.berkeley.edu:8080/berkeleylm_binaries/, it doesn't
 * seems possible to get occurrence counts for sentence start symbols, making
 * this not usable (https://github.com/adampauls/berkeleylm/issues/26).
 * @since 3.2
 */
@Experimental
public class BerkeleyRawLanguageModel extends BaseLanguageModel {

  private final NgramMapWrapper<String, LongRef> map;
          
  public BerkeleyRawLanguageModel(File berkeleyLm) {
    if (!berkeleyLm.isFile()) {
      throw new RuntimeException("You need to specify a BerkeleyLM file: " + berkeleyLm);
    }
    File vocabFile = new File(berkeleyLm.getParent(), "vocab_cs.gz");
    if (!vocabFile.exists()) {
      throw new RuntimeException("No vocabulary file 'vocab_cs.gz' found in the BerkeleyLM directory: " + vocabFile);
    }
    map = LmReaders.readNgramMapFromBinary(berkeleyLm.getAbsolutePath(), vocabFile.getAbsolutePath());
    /* For some reason, this crashes with IndexOutOfBoundsException:
    System.out.println("---START");
    Map<List<String>, LongRef> mapForOrder = map.getMapForOrder(2);
    for (Map.Entry<List<String>, LongRef> entry : mapForOrder.entrySet()) {
      System.out.println("E: " + entry.getKey());
    }
    System.out.println("---DONE");*/
  }

  @Override
  public long getCount(List<String> tokens) {
    LongRef count = map.get(tokens);
    long result;
    if (count == null) {
      result = 0;
    } else {
      result = count.asLong();
    }
    //System.out.println(tokens + " -> " + result);
    return result;
  }

  @Override
  public long getCount(String token1) {
    return getCount(Arrays.asList(token1));
  }

  @Override
  public long getTotalTokenCount() {
    return map.getMapForOrder(1).size();
  }

  @Override
  public void close() {}

}
