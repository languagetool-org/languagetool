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

import org.junit.Before;
import org.junit.Test;
import org.languagetool.tagging.ar.ArabicTagManager;

import static org.junit.Assert.assertEquals;

/**
 * @author Taha Zerrouki
 * @since 5.0
 */
public class ArabicTagManagerTest {

  private ArabicTagManager tagmanager;

  @Before
  public void setUp() {
    tagmanager = new ArabicTagManager();
  }


  @Test
  public void testTagger(){
    assertEquals(tagmanager.setJar("NJ-;M1I-;---","K"),"NJ-;M1I-;-K-");
    assertEquals(tagmanager.setJar("NJ-;M1I-;---","-"),"NJ-;M1I-;---");
    assertEquals(tagmanager.setDefinite("NJ-;M1I-;---","L"),"NJ-;M1I-;--L");
    assertEquals(tagmanager.setDefinite("NJ-;M1I-;--H","L"),"NJ-;M1I-;--H");
    assertEquals(tagmanager.setPronoun("NJ-;M1I-;---","H"),"NJ-;M1I-;--H");
    assertEquals(tagmanager.setConjunction("NJ-;M1I-;---","W"),"NJ-;M1I-;W--");
    assertEquals(tagmanager.setConjunction("V-1;M1I----;---","W"),"V-1;M1I----;W--");
    assertEquals(tagmanager.getConjunctionPrefix("V-1;M1I----;W--"),"و");

  }

}
