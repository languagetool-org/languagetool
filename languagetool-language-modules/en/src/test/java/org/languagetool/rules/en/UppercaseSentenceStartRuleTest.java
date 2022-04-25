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
package org.languagetool.rules.en;

import org.junit.Test;
import org.languagetool.*;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class UppercaseSentenceStartRuleTest {

  @Test
  public void testRule() throws IOException {
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("en"));
    assertEquals(0, lt.check("In Nov. next year.").size());
    assertEquals(0, lt.check("www.languagetool.org is a website.").size());
    assertEquals(0, lt.check("Languagetool.org is a website.").size());
    assertEquals(0, lt.check("1. an item in a list\n2. another item.").size());
    assertEquals(0, lt.check("This is a sentence. microRNA is the start of another sentence.").size());
    assertEquals(0, lt.check("This is a sentence. mRNA is the start of another sentence.").size());
    assertEquals(0, lt.check("This is a sentence. iDeal is the start of another sentence.").size());
    assertEquals(1, lt.check("languagetool.org is a website.").size());
    assertEquals(1, lt.check("a sentence.").size());
    assertEquals(1, lt.check("a sentence!").size());
    lt.disableRule("EN_CASE_AFTER_SALUTATION");
    assertEquals(0, lt.check("Hi Mr. Miller,\n\n\u00A0\n\nhow are you?").size());  // special case for paste from e.g. Outlook
  }

}
