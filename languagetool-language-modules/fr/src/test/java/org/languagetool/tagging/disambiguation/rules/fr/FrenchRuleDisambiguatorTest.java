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

package org.languagetool.tagging.disambiguation.rules.fr;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.French;
import org.languagetool.tagging.disambiguation.fr.FrenchHybridDisambiguator;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.tagging.disambiguation.xx.DemoDisambiguator;
import org.languagetool.tagging.fr.FrenchTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tokenizers.fr.FrenchWordTokenizer;

import java.io.IOException;

public class FrenchRuleDisambiguatorTest {

  private FrenchTagger tagger;
  private WordTokenizer tokenizer;
  private SentenceTokenizer sentenceTokenizer;
  private FrenchHybridDisambiguator disambiguator;
  private DemoDisambiguator disamb2;

  @Before
  public void setUp() throws IOException {
    tagger = FrenchTagger.INSTANCE;
    tokenizer = new FrenchWordTokenizer();
    French language = new French();
    sentenceTokenizer = new SRXSentenceTokenizer(language);
    disambiguator = new FrenchHybridDisambiguator();
    disamb2 = new DemoDisambiguator();    
  }

  @Test
  public void testChunker() throws IOException {
    TestTools.myAssert("Il a enfin publié son livre.",
        "/[null]SENT_START Il/[il]R pers suj 3 m s  /[null]null a/[avoir]V avoir ind pres 3 s  /[null]null enfin/[enfin]A  /[null]null publié/[publier]V ppa m s  /[null]null son/[son]D e s  /[null]null livre/[livre]N e s ./[null]null", 
        tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools.myAssert("Je danse toutes les semaines au club.",
        "/[null]SENT_START Je/[je]R pers suj 1 s  /[null]null danse/[danser]V ind pres 1 s|danse/[danser]V sub pres 1 s  /[null]null toutes/[tout]D f p  /[null]null les/[le]D e p|les/[les]_GN_FP  /[null]null semaines/[semaine]N f p|semaines/[semaines]_GN_FP  /[null]null au/[à+le]P+D m s  /[null]null club/[club]N m s ./[null]null",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools.myAssert("Je danse toutes les semaines au club.",
        "/[null]SENT_START Je/[je]R pers suj 1 s  /[null]null danse/[danse]N f s|danse/[danser]V imp pres 2 s|danse/[danser]V ind pres 1 s|danse/[danser]V ind pres 3 s|danse/[danser]V sub pres 1 s|danse/[danser]V sub pres 3 s  /[null]null toutes/[tout]A|toutes/[tout]D f p|toutes/[tout]R f p  /[null]null les/[le]D e p|les/[les]R pers obj 3 e p  /[null]null semaines/[semaine]N f p  /[null]null au/[à+le]P+D m s  /[null]null club/[club]N m s ./[null]null", 
        tokenizer, sentenceTokenizer, tagger, disamb2);
    TestTools.myAssert("Quand j'étais petit, je jouais au football.",
        "/[null]SENT_START Quand/[quand]A inte|Quand/[quand]C sub  /[null]null j'/[je]R pers suj 1 s étais/[être]V etre ind impa 1 s  /[null]null petit/[petit]J m s ,/[null]null  /[null]null je/[je]R pers suj 1 s  /[null]null jouais/[jouer]V ind impa 1 s  /[null]null au/[à+le]P+D m s  /[null]null football/[football]N m s ./[null]null", 
        tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools.myAssert("Quand j'étais petit, je jouais au football.",
        "/[null]SENT_START Quand/[quand]A inte|Quand/[quand]C sub  /[null]null j'/[je]R pers suj 1 s étais/[étai]N m p|étais/[être]V etre ind impa 1 s|étais/[être]V etre ind impa 2 s  /[null]null petit/[petit]J m s|petit/[petit]N m s ,/[null]null  /[null]null je/[je]R pers suj 1 s  /[null]null jouais/[jouer]V ind impa 1 s|jouais/[jouer]V ind impa 2 s  /[null]null au/[à+le]P+D m s  /[null]null football/[football]N m s ./[null]null", 
        tokenizer, sentenceTokenizer, tagger, disamb2);
    TestTools.myAssert("Je suis petite.",
        "/[null]SENT_START Je/[je]R pers suj 1 s  /[null]null suis/[être]V etre ind pres 1 s  /[null]null petite/[petit]J f s ./[null]null", 
        tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools.myAssert("Je suis petite.",
        "/[null]SENT_START Je/[je]R pers suj 1 s  /[null]null suis/[suivre]V imp pres 2 s|suis/[suivre]V ind pres 1 s|suis/[suivre]V ind pres 2 s|suis/[être]V etre ind pres 1 s  /[null]null petite/[petit]J f s|petite/[petit]N f s ./[null]null", 
        tokenizer, sentenceTokenizer, tagger, disamb2);
    
    TestTools.myAssert("sous-corps.",
        "/[null]SENT_START sous-corps/[sous-corps]N m sp ./[null]null", 
        tokenizer, sentenceTokenizer, tagger, disamb2);
    
    // multiwords: d'abord (locution adverbiale)
    TestTools.myAssert("Je suis d'abord allé au laboratoire.",
        "/[null]SENT_START Je/[je]R pers suj 1 s  /[null]null suis/[être]V etre ind pres 1 s  /[null]null d'/[d'abord]A abord/[d'abord]A  /[null]null allé/[aller]V ppa m s  /[null]null au/[à+le]P+D m s  /[null]null laboratoire/[laboratoire]N m s ./[null]null", 
        tokenizer, sentenceTokenizer, tagger, disambiguator);
  }
  
}


