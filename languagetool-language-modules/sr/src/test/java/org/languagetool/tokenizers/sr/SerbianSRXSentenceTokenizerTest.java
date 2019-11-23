/* LanguageTool, a natural language style checker 
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tokenizers.sr;

import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Serbian;
import org.languagetool.tokenizers.SRXSentenceTokenizer;

/**
 * @author Zoltán Csala
 */
public class SerbianSRXSentenceTokenizerTest {

  private final SRXSentenceTokenizer stokenizer = new SRXSentenceTokenizer(new Serbian());

  @Test
  public void testTokenize() {
    // NOTE: sentences here need to end with a space character so they
    // have correct whitespace when appended:
    testSplit("Ово је једна реченица. ");
    testSplit("Ово је једна реченица. ", "И још једна.");
    testSplit("Једна реченица! ", "Још једна.");
    //testSplit("Ein Satz... ", "Noch einer.");
    testSplit("На адреси http://www.gov.rs станује српска влада.");
    testSplit("Писмо је стигло 3.10. пре подне.");
    testSplit("Писмо је стигло 31.1. пре подне.");
    testSplit("Писмо је стигло 3.10.2000 поподне.");
    testSplit("Србија је под Турцима била од 14. до 19. века.");

    // Testing (non-)segmentation after Roman numerals
    testSplit("Петар I, познат и као Петар Ослободилац.");
    testSplit("Петар II, познат и као Петар Изгнаник.");
    testSplit("Петар III, принц наследник.");
    testSplit("Александар V Обреновић.");

    // Testing (non-)segmentation in dates and times
    testSplit("Данас је 13.12.2004.");
    testSplit("Данас је 13. децембар.");
    testSplit("Видећемо се 29. фебруара.");
    testSplit("Јесен стиже 23.09. поподне.");
    testSplit("Жена стиже тачно у 17:00 кући.");

    testSplit("Ренесанса је почела у 13. веку и бла бла трућ.");
    testSplit("Све је почело у 13. или 14. веку и бла бла трућ.");
    testSplit("Трајало је све од 13. до 14. века и бла бла трућ.");

    testSplit("Ово је једна(!) реченица.");
    testSplit("Чуо сам једну(!!) реченицу.");
    testSplit("Ово је једна(?) реченица.");
    testSplit("Чујем ли само једну(???) реченицу.");

    testSplit("„Ћуко је креп'о“, рече он");

    // Testing segmentation after Serbian keywords
    //testSplit("Поштовани господине тј. госпођо.");
  }

  private void testSplit(String... sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }
  
}
