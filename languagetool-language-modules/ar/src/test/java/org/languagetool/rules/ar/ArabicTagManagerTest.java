/*
 * LanguageTool, a natural language style checker
 * Copyright (C) 2020 Sohaib Afifi, Taha Zerrouki
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
package org.languagetool.rules.ar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.languagetool.tagging.ar.ArabicTagManager;

/**
 * @author Taha Zerrouki
 * @since 5.0
 */
public class ArabicTagManagerTest {

  private ArabicTagManager tagManager;

  @BeforeEach
  public void setUp() {
    tagManager = new ArabicTagManager();
  }

  @Test
  public void testTagger(){
    Assertions.assertEquals(tagManager.setJar("NJ-;M1I-;---","K"), "NJ-;M1I-;-K-");
    Assertions.assertEquals(tagManager.setJar("NJ-;M1I-;---","-"), "NJ-;M1I-;---");
    Assertions.assertEquals(tagManager.setDefinite("NJ-;M1I-;---","L"), "NJ-;M1I-;--L");
    Assertions.assertEquals(tagManager.setDefinite("NJ-;M1I-;--H","L"), "NJ-;M1I-;--H");
    Assertions.assertEquals(tagManager.setPronoun("NJ-;M1I-;---","H"), "NJ-;M1I-;--H");
    Assertions.assertEquals(tagManager.setConjunction("NJ-;M1I-;---","W"), "NJ-;M1I-;W--");
    Assertions.assertEquals(tagManager.setConjunction("V-1;M1I----;---","W"), "V-1;M1I----;W--");
    Assertions.assertEquals(tagManager.getConjunctionPrefix("V-1;M1I----;W--"), "و");
  }

}
