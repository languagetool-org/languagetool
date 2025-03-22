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
package org.languagetool.tokenizers.crh;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.CrimeanTatar;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;

public class CrimeanTatarSRXSentenceTokenizerTest {

  private static final CrimeanTatar LANGUAGE = new CrimeanTatar();
  
  // accept \n as paragraph:
  private final SentenceTokenizer stokenizer = new SRXSentenceTokenizer(LANGUAGE);
  // accept only \n\n as paragraph:
//  private final SentenceTokenizer stokenizer2 = new SRXSentenceTokenizer(LANGUAGE);

  @Before
  public void setUp() {
    stokenizer.setSingleLineBreaksMarksParagraph(true);
//    stokenizer2.setSingleLineBreaksMarksParagraph(false);
  }

  // NOTE: sentences here need to end with a space character so they
  // have correct whitespace when appended:
  @Test
  public void testTokenize() {

    testSplit("Yapraqlar töküldi. ");
    testSplit("Yapraqlar töküldi. ", "Otlar-ölenler sarardı, soldılar.");
    //TODO:
//    testSplit("– Afu etiñiz, ocam! – dedim men, – Selâmet mıtlaqa ketmek kerekmi?");
  }

  private void testSplit(String... sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }

}
