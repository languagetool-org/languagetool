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

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.AbstractCompoundRuleTest;

public class CompoundRuleTest extends AbstractCompoundRuleTest {

  @Before
  public void setUp() throws Exception {
    lt = new JLanguageTool(Languages.getLanguageForShortCode("en-US"));
    rule = new CompoundRule(TestTools.getEnglishMessages(), Languages.getLanguageForShortCode("en-US"), null);
    //testAllCompounds();
  }

  @Test
  public void testRule() throws IOException {    
    
    // correct sentences:
    check(0, "The software supports case-sensitive search.");
    check(0, "He is one-year-old.");
    check(0, "If they're educated people, they will know.");
    check(0, "Tiffany & Co chairman has to say something important");
    check(0, "Another one bites the dust");
    check(0, "We well received your email"); 
    check(0, "air-to-air");
    check(0, "non-party");
    check(0, "age-old");
    check(0, "able-bodied");
    check(0, "non-scientific");
    check(0, "This is the first ever green bond by a municipality.");
    check(0, "Semi Automatic"); // desired?
    check(0, "Night Mare"); // desired?
    
    // incorrect sentences:
    check(1, "case sensitive", "case-sensitive");
    check(1, "Young criminals must be re educated.");
    check(1, "And an other one bites the dust");
    check(1, "An other one bites the dust");
        
    check(1, "good-bye", "goodbye"); //?
    check(1, "back-fire", "backfire"); //+
    check(1, "back fire", "backfire");
    check(1, "air-to air", "air-to-air");
    check(1, "air to air", "air-to-air");
    check(1, "air to-air", "air-to-air");
    check(1, "air to -air", "air-to-air");
    check(1, "age old", "age-old");
    check(1, "able bodied", "able-bodied");
    check(1, "non scientific", "non-scientific", "nonscientific");
    check(1, "night-mare", "nightmare");
    check(1, "Night-mare", "Nightmare");
    check(1, "Night mare", "Nightmare");
    check(1, "semi automatic", "semi-automatic", "semiautomatic");
    check(1, "Semi automatic", "Semi-automatic", "Semiautomatic");
    check(1, "Dev-Ops", "DevOps");
    check(1, "Dev Ops", "DevOps");
    check(1, "Night-mare", "Nightmare");
    check(1, "Play Station", "PlayStation");
    check(1, "Play-Station", "PlayStation");
    
  }
 
}
