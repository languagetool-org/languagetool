/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.en.translation;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.GlobalConfig;
import org.languagetool.rules.translation.TranslationEntry;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class BeoLingusTranslatorTest {

  private BeoLingusTranslator translator;
  
  @Before
  public void init() throws IOException {
    GlobalConfig globalConfig = new GlobalConfig();
    globalConfig.setBeolingusFile(new File("src/test/resources/beolingus_test.txt"));
    translator = BeoLingusTranslator.getInstance(globalConfig);
  }
  
  @Test
  public void testTranslate() throws IOException {
    List<TranslationEntry> result1 = translator.translate("Haus", "de", "en");
    assertThat(result1.size(), is(3));
    assertTrue(result1.get(0).getL2().contains("house"));
    assertTrue(result1.get(1).getL2().contains("home"));
    assertTrue(result1.get(2).getL2().contains("volta bracket (sheet music)"));

    List<TranslationEntry> result2 = translator.translate("haus", "de", "en");
    assertThat(result2.size(), is(3));
    assertTrue(result2.get(0).getL2().contains("house"));
    assertTrue(result2.get(1).getL2().contains("home"));
    assertTrue(result2.get(2).getL2().contains("volta bracket (sheet music)"));

    List<TranslationEntry> result3 = translator.translate("house", "en", "de");
    assertThat(result3.size(), is(1));
    assertTrue(result3.get(0).getL2().contains("Haus {n}"));

    List<TranslationEntry> result4 = translator.translate("suchwort", "de", "en");
    //for (TranslationEntry s : result4) { System.out.println("  " + s.getL1() + " <> " + String.join(" -- " , s.getL2())); }
    assertThat(result4.size(), is(1));
    assertTrue(result4.get(0).getL2().contains("search word {one; to}"));
    assertTrue(result4.get(0).getL2().contains("another item"));
  }
  
  @Test 
  public void testSplit() {
    assertThat(translator.split("foo").toString(), is("[foo]"));
    assertThat(translator.split("foo { bar } foo").toString(), is("[foo { bar } foo]"));
    assertThat(translator.split("foo { bar }; foo").toString(), is("[foo { bar }, foo]"));
    assertThat(translator.split("foo; bar; foo").toString(), is("[foo, bar, foo]"));
    assertThat(translator.split("foo; bar { blah }; foo").toString(), is("[foo, bar { blah }, foo]"));
    assertThat(translator.split("foo; bar { blah; blubb }; foo").toString(), is("[foo, bar { blah; blubb }, foo]"));
    assertThat(translator.split("foo; bar { blah; blubb; three four }; foo").toString(), is("[foo, bar { blah; blubb; three four }, foo]"));
  }
  
}
