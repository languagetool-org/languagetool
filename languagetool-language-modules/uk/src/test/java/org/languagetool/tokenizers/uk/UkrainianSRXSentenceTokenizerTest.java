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

package org.languagetool.tokenizers.uk;

import junit.framework.TestCase;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.tokenizers.SRXSentenceTokenizer;

/*
 * Ukrainian SRX Sentence Tokenizer Test
 */
public class UkrainianSRXSentenceTokenizerTest extends TestCase {

  private final SRXSentenceTokenizer stokenizer = new SRXSentenceTokenizer(new Ukrainian());

  public final void testTokenize() {
	testSplit("Це просте речення.");
	testSplit("Вони приїхали в Париж. ", "Але там їм геть не сподобалося.");
    testSplit("Панк-рок — напрям у рок-музиці, що виник у середині 1970-х рр. у США і Великобританії.");
    testSplit("Разом із втечами, вже у XV ст. почастішали збройні виступи селян.");
    testSplit("На початок 1994 р. державний борг України становив 4,8 млрд. дол.");
//    TODO:
//    testSplit("На початок 1994 р. державний борг України становив 4,8 млрд. ", "Досить значна сума.");
    testSplit("Київ, вул. Сагайдачного, буд. 43, кв. 4.");
    testSplit("Наша зустріч з А. Марчуком відбулася в грудні минулого року.");
    testSplit("Наша зустріч з А.Марчуком відбулася в грудні минулого року.");
  }

  private void testSplit(final String... sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }

}
