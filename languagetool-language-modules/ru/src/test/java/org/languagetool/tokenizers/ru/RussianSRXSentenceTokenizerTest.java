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

package org.languagetool.tokenizers.ru;

import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Russian;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;

public class RussianSRXSentenceTokenizerTest {

  private final SentenceTokenizer stokenizer = new SRXSentenceTokenizer(new Russian());

  @Test
  public final void testTokenize() {
    // NOTE: sentences here need to end with a space character so they
    // have correct whitespace when appended:
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
