/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging.ru;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;

import org.languagetool.tagging.BaseTagger;

/**  Part-of-speech tagger.
 * Russian dictionary originally developed by www.aot.ru and licensed under LGPL.
 * See readme.txt for details, the POS tagset is described in tagset.txt
 */
public class RussianTagger extends BaseTagger {

  @Override
  public String getManualAdditionsFileName() {
    return "/ru/added.txt";
  }

  @Override
  public String getManualRemovalsFileName() {
    return "/ru/removed.txt";
  }

  public RussianTagger() {
    super("/ru/russian.dict", new Locale("ru"));
  }

  @Override
  public List<AnalyzedTokenReadings> tag(List<String> sentenceTokens) throws IOException {
    List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    int pos = 0;
    for (String word : sentenceTokens) {
      if (word.length() > 1) {
        word = word.replace("о́", "о");
        word = word.replace("а́", "а");
        word = word.replace("е́", "е");
        word = word.replace("у́", "у");
        word = word.replace("и́", "и");
        word = word.replace("ы́", "ы");
        word = word.replace("э́", "э");
        word = word.replace("ю́", "ю");
        word = word.replace("я́", "я");
        word = word.replace("о̀", "о");
        word = word.replace("а̀", "а");
        word = word.replace("ѐ", "е");
        word = word.replace("у̀", "у");
        word = word.replace("ѝ", "и");
        word = word.replace("ы̀", "ы");
        word = word.replace("э̀", "э");
        word = word.replace("ю̀", "ю");
        word = word.replace("я̀", "я");
        word = word.replace("ʼ", "ъ");
      }
      List<AnalyzedToken> l = getAnalyzedTokens(word);
      tokenReadings.add(new AnalyzedTokenReadings(l, pos));
      pos += word.length();
    }
    return tokenReadings;
  }

}
