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
package org.languagetool.tagging.fr;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.French;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tokenizers.fr.FrenchWordTokenizer;

import java.io.IOException;

public class FrenchTaggerTest {
  
  private FrenchTagger tagger;
  private WordTokenizer tokenizer;

  @Before
  public void setUp() {
    tagger = FrenchTagger.INSTANCE;;
    tokenizer = new FrenchWordTokenizer();
  }

  @Test
  public void testDictionary() throws IOException {
    TestTools.testDictionary(tagger, new French());
  }

  @Test
  public void testTagger() throws IOException {
    TestTools.myAssert("C'est la vie.",
        "C'/[ce]R dem m s -- est/[est]N m s|est/[être]V etre ind pres 3 s -- la/[la]N m sp|la/[le]D f s|la/[le]R pers obj 3 f s -- vie/[vie]N f s", tokenizer, tagger);
    TestTools.myAssert("Je ne parle pas français.",
        "Je/[je]R pers suj 1 s -- ne/[ne]A -- parle/[parler]V imp pres 2 s|parle/[parler]V ind pres 1 s|parle/[parler]V ind pres 3 s|parle/[parler]V sub pres 1 s|parle/[parler]V sub pres 3 s -- pas/[pas]A|pas/[pas]N m sp -- français/[français]J m sp|français/[français]N m sp", tokenizer, tagger);
    TestTools.myAssert("blablabla","blablabla/[blablabla]N m s", tokenizer, tagger);
    TestTools.myAssert("passagère","passagère/[passager]J f s|passagère/[passager]N f s", tokenizer, tagger);
    TestTools.myAssert("nonexistingword","nonexistingword/[null]null", tokenizer, tagger);  
    
    TestTools.myAssert("auto-mutile","auto-mutile/[auto-mutiler]V imp pres 2 s|auto-mutile/[auto-mutiler]V ind pres 1 s|auto-mutile/[auto-mutiler]V ind pres 3 s|auto-mutile/[auto-mutiler]V sub pres 1 s|auto-mutile/[auto-mutiler]V sub pres 3 s", tokenizer, tagger);
    TestTools.myAssert("auto-mutilées","auto-mutilées/[auto-mutiler]V ppa f p", tokenizer, tagger);
    TestTools.myAssert("micro-plastiques","micro-plastiques/[micro-plastique]J e p|micro-plastiques/[micro-plastique]N e p", tokenizer, tagger);
    TestTools.myAssert("sous-espace","sous-espace/[sous-espace]N m s", tokenizer, tagger);
    TestTools.myAssert("sous-corps","sous-corps/[sous-corps]N m sp", tokenizer, tagger);
    TestTools.myAssert("auto-empoisonnement","auto-empoisonnement/[auto-empoisonnement]N m s", tokenizer, tagger);
    TestTools.myAssert("auto-équilibrage","auto-équilibrage/[auto-équilibrage]N m s", tokenizer, tagger);
    TestTools.myAssert("Grand-Chambre","Grand-Chambre/[grand-chambre]N f s", tokenizer, tagger);
    
    TestTools.myAssert("d’aujourd’hui","d'/[de]D e sp|d'/[de]P -- aujourd'hui/[aujourd'hui]A", tokenizer, tagger);
    TestTools.myAssert("d'aujourd’hui","d'/[de]D e sp|d'/[de]P -- aujourd'hui/[aujourd'hui]A", tokenizer, tagger);
    TestTools.myAssert("d’aujourd'hui","d'/[de]D e sp|d'/[de]P -- aujourd'hui/[aujourd'hui]A", tokenizer, tagger);
    TestTools.myAssert("Fontaine-l’Évêque","Fontaine-l'Évêque/[Fontaine-l'Évêque]Z e sp", tokenizer, tagger);
    TestTools.myAssert("Fontaine-l'Évêque","Fontaine-l'Évêque/[Fontaine-l'Évêque]Z e sp", tokenizer, tagger);
    TestTools.myAssert("entr'ouvrions","entr'ouvrions/[entr'ouvrir]V ind impa 1 p|entr'ouvrions/[entr'ouvrir]V sub pres 1 p", tokenizer, tagger);
    TestTools.myAssert("entr’ouvrions","entr'ouvrions/[entr'ouvrir]V ind impa 1 p|entr'ouvrions/[entr'ouvrir]V sub pres 1 p", tokenizer, tagger);
    TestTools.myAssert("Penses-tu","Penses/[penser]V ind pres 2 s|Penses/[penser]V sub pres 2 s -- -tu/[tu]R pers suj 2 s", tokenizer, tagger);
    TestTools.myAssert("Strauss-Kahn", "Strauss-Kahn/[Strauss-Kahn]Z e sp", tokenizer, tagger);
    TestTools.myAssert("va-t'en", "va/[aller]V imp pres 2 s|va/[aller]V ind pres 3 s -- -t/[te]R pers obj 2 s -- 'en/[en]R pers obj 3 sp", tokenizer, tagger);
        
    TestTools.myAssert("Al-Qaïda","Al-Qaïda/[Al-Qaïda]Z f sp", tokenizer, tagger);
    
    TestTools.myAssert("minitélévision","minitélévision/[minitélévision]N f s", tokenizer, tagger);
    
    TestTools.myAssert("soeur","soeur/[sœur]N f s", tokenizer, tagger);
    TestTools.myAssert("oeils-de-boeuf","oeils-de-boeuf/[œils-de-bœuf]N m p", tokenizer, tagger);
    TestTools.myAssert("Ç'avait","Ç'/[cela]R dem m s -- avait/[avoir]V avoir ind impa 3 s", tokenizer, tagger);
  }

}
