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
package de.danielnaber.languagetool.tagging.ru;

import java.io.IOException;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.WordData;

import junit.framework.TestCase;
import de.danielnaber.languagetool.TestTools;
import de.danielnaber.languagetool.tokenizers.WordTokenizer;

public class RussianTaggerTest extends TestCase {
    
  private RussianTagger tagger;
  private WordTokenizer tokenizer;
      
  public void setUp() {
    tagger = new RussianTagger();
    tokenizer = new WordTokenizer();
  }

  public void testDictionary() throws IOException {
    final Dictionary dictionary = Dictionary.read(
        this.getClass().getResource(tagger.getFileName()));
    final DictionaryLookup dl = new DictionaryLookup(dictionary);
    for (WordData wd : dl) {
      if (wd.getTag() == null || wd.getTag().length() == 0) {
        System.err.println("**** Warning: the word " + wd.getWord() + "/" + wd.getStem() +" lacks a POS tag in the dictionary.");
      }
    }    
  }
  
  public void testTagger() throws IOException {
    TestTools.myAssert("Все счастливые семьи похожи друг на друга,  каждая  несчастливая  семья несчастлива по-своему.",
        "Все/[весь]PADJ:PL:Nom|Все/[весь]PADJ:PL:V|Все/[все]ADV|Все/[все]PNN:PL:Nom|Все/[все]PNN:PL:V|Все/[все]PNN:Sin:Nom|Все/[все]PNN:Sin:V -- счастливые/[счастливый]ADJ:PL:Nom|счастливые/[счастливый]ADJ:PL:V -- семьи/[семья]NN:Fem:PL:Nom|семьи/[семья]NN:Fem:PL:V|семьи/[семья]NN:Fem:Sin:R -- похожи/[похожий]ADJ_Short:PL -- друг/[друг]NN:Masc:Sin:Nom -- на/[на]PREP -- друга/[друг]NN:Masc:Sin:R|друга/[друг]NN:Masc:Sin:V -- каждая/[каждый]PADJ:Fem:Nom -- несчастливая/[несчастливый]ADJ:Fem:Nom -- семья/[семья]NN:Fem:Sin:Nom -- несчастлива/[несчастливый]ADJ_Short:Fem -- по-своему/[по-своему]ADV", tokenizer, tagger);        
    TestTools.myAssert("Все смешалось в доме Облонских.",
        "Все/[весь]PADJ:PL:Nom|Все/[весь]PADJ:PL:V|Все/[все]ADV|Все/[все]PNN:PL:Nom|Все/[все]PNN:PL:V|Все/[все]PNN:Sin:Nom|Все/[все]PNN:Sin:V -- смешалось/[смешаться]VB:Past:Neut -- в/[в]PREP -- доме/[дом]NN:Masc:Sin:P -- Облонских/[null]null", tokenizer, tagger);        
  }

}
