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

package org.languagetool.tokenizers;

import junit.framework.TestCase;
import org.languagetool.Language;
import org.languagetool.TestTools;

/*
 * Russian SRX Sentence Tokenizer Test
 * $Id: RussianSRXSentenceTokenizerTest.java,v 1.1 2010-02-07 14:22:38 yakovru Exp $
 */
public class RussianSRXSentenceTokenizerTest extends TestCase {

  // accept \n as paragraph:
  private final SentenceTokenizer stokenizer = new SRXSentenceTokenizer(Language.RUSSIAN);
  // accept only \n\n as paragraph:
  private final SentenceTokenizer stokenizer2 = new SRXSentenceTokenizer(Language.RUSSIAN);
  
  
  public final void setUp() {
    stokenizer.setSingleLineBreaksMarksParagraph(true);  
    stokenizer2.setSingleLineBreaksMarksParagraph(false);  
  }

  public final void testTokenize() {
    // NOTE: sentences here need to end with a space character so they
    // have correct whitespace when appended:
    testSplit("Dies ist ein Satz.");
    testSplit("Dies ist ein Satz. ", "Noch einer.");
    testSplit("Ein Satz! ", "Noch einer.");
    testSplit("Ein Satz... ", "Noch einer.");
    testSplit("Unter http://www.test.de gibt es eine Website.");
    testSplit("Das Schreiben ist auf den 3.10. datiert.");
    testSplit("Das Schreiben ist auf den 31.1. datiert.");
    testSplit("Das Schreiben ist auf den 3.10.2000 datiert.");

    testSplit("Heute ist der 13.12.2004.");
    testSplit("Es geht am 24.09. los.");
    testSplit("Das in Punkt 3.9.1 genannte Verhalten.");

    testSplit("Das ist,, also ob es bla.");
    testSplit("Das ist es.. ", "So geht es weiter.");

    testSplit("Das hier ist ein(!) Satz.");
    testSplit("Das hier ist ein(!!) Satz.");
    testSplit("Das hier ist ein(?) Satz.");
    testSplit("Das hier ist ein(???) Satz.");
    testSplit("Das hier ist ein(???) Satz.");

    // TODO: derzeit unterscheiden wir nicht, ob nach dem Doppelpunkt ein
    // ganzer Satz kommt oder nicht:
    testSplit("Das war es: gar nichts.");
    testSplit("Das war es: Dies ist ein neuer Satz.");

    // incomplete sentences, need to work for on-thy-fly checking of texts:
    testSplit("Here's a");
    testSplit("Here's a sentence. ", "And here's one that's not comp");

    // Tests taken from LanguageTool's SentenceSplitterTest.py:
    testSplit("This is a sentence. ");
    testSplit("This is a sentence. ", "And this is another one.");
    testSplit("This is a sentence.", "Isn't it?", "Yes, it is.");
    testSplit("Don't split strings like U.S.A. either.");
    testSplit("Don't split strings like U. S. A. either.");
    testSplit("Don't split... ", "Well you know. ", "Here comes more text.");
    testSplit("Don't split... well you know. ", "Here comes more text.");
    testSplit("The \".\" should not be a delimiter in quotes.");
    testSplit("\"Here he comes!\" she said.");
    testSplit("\"Here he comes!\", she said.");
    testSplit("\"Here he comes.\" ", "But this is another sentence.");
    testSplit("\"Here he comes!\". ", "That's what he said.");
    testSplit("The sentence ends here. ", "(Another sentence.)");
    // known to fail:
    // testSplit(new String[]{"He won't. ", "Really."});
    testSplit("He won't go. ", "Really.");
    testSplit("He won't say no.", "Not really.");
    testSplit("He won't say No.", "Not really.");
    testSplit("This is it: a test.");
    // one/two returns = paragraph = new sentence:
    TestTools.testSplit(new String[] { "He won't\n\n", "Really." }, stokenizer2);
    TestTools.testSplit(new String[] { "He won't\n", "Really." }, stokenizer);
    TestTools.testSplit(new String[] { "He won't\n\n", "Really." }, stokenizer2);
    TestTools.testSplit(new String[] { "He won't\nReally." }, stokenizer2);
    // Missing space after sentence end:
    testSplit("James is from the Ireland!", "He lives in Spain now.");
    // From the Russian abbreviation list:
    testSplit("Отток капитала из России составил 7 млрд. долларов, сообщил министр финансов Алексей Кудрин.");
    testSplit("Журнал издаётся с 1967 г., пользуется большой популярностью в мире.");
    testSplit("С 2007 г. периодичность выхода газеты – 120 раз в год.");
    testSplit("Редакция журнала находится в здании по адресу: г. Москва, 110000, улица Мира, д. 1.");
    testSplit("Все эти вопросы заставляют нас искать ответы в нашей истории 60-80-х гг. прошлого столетия.");
    testSplit("Более 300 тыс. документов и справочников.");
    testSplit("Скидки до 50000 руб. на автомобили.");
    testSplit("Изготовление визиток любыми тиражами (от 20 шт. до 10 тысяч) в минимальные сроки (от 20 минут).");
  }

  private void testSplit(final String... sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }

}
