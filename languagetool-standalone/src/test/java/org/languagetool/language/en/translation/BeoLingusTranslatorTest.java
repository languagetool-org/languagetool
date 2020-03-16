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
package org.languagetool.language.en.translation;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.GlobalConfig;
import org.languagetool.rules.en.translation.BeoLingusTranslator;
import org.languagetool.rules.translation.TranslationEntry;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class BeoLingusTranslatorTest {

  private BeoLingusTranslator translator;
  
  @Before
  public void init() throws IOException {
    GlobalConfig globalConfig = new GlobalConfig();
    globalConfig.setBeolingusFile(new File("src/test/resources/beolingus_test.txt"));
    translator = BeoLingusTranslator.getInstance(globalConfig);
  }

  @Test
  @Ignore("for interactive development only")
  public void testForDevelopment() {
    List<TranslationEntry> result1 = translator.translate("Luftpumpen", "de", "en");
    System.out.println(result1);
  }

  @Test
  public void testTranslateInflectedForm() {
    List<TranslationEntry> result1 = translator.translate("Luftpumpen", "de", "en");
    assertThat(result1.size(), is(1));
    assertTrue(result1.get(0).getL2().contains("tyre pumps [Br.]"));
    assertTrue(result1.get(0).getL2().contains("tire pumps [Am.]"));
    assertTrue(result1.get(0).getL2().contains("tyre inflators [Br.]"));
    assertTrue(result1.get(0).getL2().contains("tire inflators [Am.]"));
  }

  @Test
  public void testTranslate() {
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

    //direction not yet activated:
    //List<TranslationEntry> result3 = translator.translate("house", "en", "de");
    //assertThat(result3.size(), is(1));
    //assertTrue(result3.get(0).getL2().contains("Haus {n}"));

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

  @Test
  public void testAmericanBritishVariants() {
    List<TranslationEntry> result1 = translator.translate("Luftpumpe", "de", "en");
    assertThat(result1.size(), is(1));
    assertTrue(result1.get(0).getL2().contains("tyre pump [Br.]"));
    assertTrue(result1.get(0).getL2().contains("tire pump [Am.]"));
    assertTrue(result1.get(0).getL2().contains("tyre inflator [Br.]"));
    assertTrue(result1.get(0).getL2().contains("tire inflator [Am.]"));

    List<TranslationEntry> result2 = translator.translate("Testluftpumpe", "de", "en");
    assertThat(result2.size(), is(1));
    assertTrue(result2.get(0).getL2().contains("tyre pump [Br.]"));
    assertTrue(result2.get(0).getL2().contains("tire pump [Am.]"));

    List<TranslationEntry> result3 = translator.translate("Reifen wechseln", "de", "en");
    assertThat(result3.size(), is(1));
    assertTrue(result3.get(0).getL2().contains("to change the tyres [Br.]"));
    assertTrue(result3.get(0).getL2().contains("to change the tires [Am.]"));
  }

  @Test
  public void testCleanTranslationForReplace() {
    assertThat(translator.cleanTranslationForReplace("", null), CoreMatchers.is(""));
    assertThat(translator.cleanTranslationForReplace("to go", null), CoreMatchers.is("go"));
    assertThat(translator.cleanTranslationForReplace("to go", "need"), CoreMatchers.is("to go"));
    assertThat(translator.cleanTranslationForReplace("to go", "will"), CoreMatchers.is("go"));
    assertThat(translator.cleanTranslationForReplace("to go", "foo"), CoreMatchers.is("go"));
    assertThat(translator.cleanTranslationForReplace("to go", "to"), CoreMatchers.is("go"));
    assertThat(translator.cleanTranslationForReplace("foo (bar) {mus}", null), CoreMatchers.is("foo"));
    assertThat(translator.cleanTranslationForReplace("some thing [Br.], something", null), CoreMatchers.is("some thing , something"));  // not quite clean yet...
  }

  @Test
  public void testGetTranslationSuffix() {
    assertThat(translator.getTranslationSuffix(""), CoreMatchers.is(""));
    assertThat(translator.getTranslationSuffix(" "), CoreMatchers.is(""));
    assertThat(translator.getTranslationSuffix("foo bar"), CoreMatchers.is(""));
    assertThat(translator.getTranslationSuffix("foo bar [Br.]"), CoreMatchers.is("[Br.]"));
    assertThat(translator.getTranslationSuffix("foo bar {ugs} [Br.]"), CoreMatchers.is("{ugs} [Br.]"));
    assertThat(translator.getTranslationSuffix("foo bar {ugs} [Br.] (Blah)"), CoreMatchers.is("{ugs} [Br.] (Blah)"));
    //assertThat(rule.cleanTranslationForAddition("foo (Blah {m})"), is("(Blah {m})"));  // nesting not supported yet
  }

}
