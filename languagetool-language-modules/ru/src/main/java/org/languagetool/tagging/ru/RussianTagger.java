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

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.tagging.BaseTagger;

import java.io.IOException;
import java.util.*;



/**  Part-of-speech tagger.
 * Russian dictionary originally developed by www.aot.ru and licensed under LGPL.
 * See readme.txt for details, the POS tagset is described in tagset.txt
 */
public class RussianTagger extends BaseTagger {
  public static final RussianTagger INSTANCE = new RussianTagger();

  private RussianTagger() {
    super("/ru/russian.dict", new Locale("ru"));
  }

  @Override
    public List<AnalyzedTokenReadings> tag(List<String> sentenceTokens) throws IOException {
        List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
        int pos = 0;
        String word_ie;
        for (String word : sentenceTokens) {
            boolean maymissingyo = false;
            if (word.length() > 1) {
	      if  ( !(word.contains("ё")) && !(word.contains("Ё")) && (word.contains("е") || word.contains("Е")) && !(word.contains("е́")) && !(word.contains("о́")) &&
	      !(word.contains("а́")) && !(word.contains("у́")) && !(word.contains("и́")) && !(word.contains("ю́")) && !(word.contains("ы́")) && !(word.contains("э́")) && !(word.contains("я́"))  ) {
                maymissingyo = true;
            };
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
                word_ie=word.toLowerCase();
                word_ie=word_ie.replace("е","ё");

            List<AnalyzedToken> l = getAnalyzedTokens(word);

            AnalyzedTokenReadings atr = new AnalyzedTokenReadings(l, pos);
            
            
            if (maymissingyo) { 
            if (getWordTagger().tag(word_ie).isEmpty()) {
            maymissingyo= false;
            }
            }
                
            
            
            
            if (maymissingyo) { 
                List<ChunkTag> listChunkTags = new ArrayList<>();
                listChunkTags.add(new ChunkTag("MayMissingYO"));
                atr.setChunkTags(listChunkTags);
            }

            tokenReadings.add(atr);
            pos += word.length();

        }
        return tokenReadings;
    }

}
