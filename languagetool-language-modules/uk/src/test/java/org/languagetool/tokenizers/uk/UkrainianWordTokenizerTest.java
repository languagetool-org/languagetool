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

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public class UkrainianWordTokenizerTest extends TestCase {
  private final UkrainianWordTokenizer w = new UkrainianWordTokenizer();

  public void testTokenize() {
    List<String> testList = w.tokenize("Вони прийшли додому.");
    assertEquals(Arrays.asList("Вони", " ", "прийшли", " ", "додому", "."), testList);

    testList = w.tokenize("Вони прийшли пʼятими зів’ялими.");
    assertEquals(Arrays.asList("Вони", " ", "прийшли", " ", "п'ятими", " ", "зів'ялими", "."), testList);

//    testList = w.tokenize("Вони\u0301 при\u00ADйшли пʼя\u0301тими зів’я\u00ADлими.");
//    assertEquals(Arrays.asList("Вони", " ", "прийшли", " ", "п'ятими", " ", "зів'ялими", "."), testList);

    testList = w.tokenize("Засідав І.Єрмолюк.");
    assertEquals(Arrays.asList("Засідав", " ", "І", ".", "Єрмолюк", "."), testList);

    testList = w.tokenize("Засідав І.П.Єрмолюк.");
    assertEquals(Arrays.asList("Засідав", " ", "І", ".", "П", ".", "Єрмолюк", "."), testList);

    testList = w.tokenize("І.\u00A0Єрмолюк.");
    assertEquals(Arrays.asList("І", ".", "\u00A0", "Єрмолюк", "."), testList);

    testList = w.tokenize("300 грн. на балансі");
    assertEquals(Arrays.asList("300", " ", "грн.", " ", "на", " ", "балансі"), testList);

    testList = w.tokenize("надійшло 2,2 мільйона");
    assertEquals(Arrays.asList("надійшло", " ", "2,2", " ", "мільйона"), testList);

    testList = w.tokenize("надійшло 84,46 мільйона");
    assertEquals(Arrays.asList("надійшло", " ", "84,46", " ", "мільйона"), testList);

//    testList = w.tokenize("надійшло 2 000 тон");
//    assertEquals(Arrays.asList("надійшло", " ", "2 000", " ", "тон"), testList);

    testList = w.tokenize("сталося 14.07.2001 вночі");
    assertEquals(Arrays.asList("сталося", " ", "14.07.2001", " ", "вночі"), testList);

    testList = w.tokenize("вчора о 7.30 ранку");
    assertEquals(Arrays.asList("вчора", " ", "о", " ", "7.30", " ", "ранку"), testList);

    testList = w.tokenize("я українець(сміється");
    assertEquals(Arrays.asList("я", " ", "українець", "(", "сміється"), testList);
        
    testList = w.tokenize("ОУН(б) та КП(б)У");
    assertEquals(Arrays.asList("ОУН(б)", " ", "та", " ", "КП(б)У"), testList);

    testList = w.tokenize("Негода є... заступником");
    assertEquals(Arrays.asList("Негода", " ", "є", "...", " ", "заступником"), testList);

    testList = w.tokenize("140 тис. працівників");
    assertEquals(Arrays.asList("140", " ", "тис.", " ", "працівників"), testList);

    testList = w.tokenize("проф. Артюхов");
    assertEquals(Arrays.asList("проф.", " ", "Артюхов"), testList);
  }

}
