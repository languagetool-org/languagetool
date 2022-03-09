/* LanguageTool, a natural language style checker 
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.language;

import org.jetbrains.annotations.Nullable;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class FastTextTest {

  private static final File MODEL_PATH = new File("/prg/fastText-0.1.0/data/lid.176.bin");
  private static final File BINARY_PATH = new File("/prg/fastText-0.1.0/fasttext");

  @Test
  @Ignore("for interactive use")
  public void testInteractively() throws Exception {
    FastText ft = new FastText(MODEL_PATH, BINARY_PATH);
    List<String> langCodes = Arrays.asList("en", "de", "fr", "es");
    String s = "PROJECT TAGGING CRITERIA";  // {en=0.0688019}
    //String s = "project tagging criteria";  // {en=0.417927, es=0.0365024}
    Map<String, Double> res1 = ft.runFasttext(s, langCodes);
    System.out.println(res1);
  }
  
  @Test
  @Ignore("requires local FastText")
  public void testCaseShouldNotMatter() throws Exception {
    // all-uppercase yields bad results (also see https://github.com/facebookresearch/fastText/issues/1181),
    // so make sure we lowercase input internally
    FastText ft = new FastText(MODEL_PATH, BINARY_PATH);
    List<String> langCodes = Arrays.asList("en", "de", "fr", "es", "ko");
    String s = "project tagging criteria";
    Map<String, Double> res1 = ft.runFasttext(s, langCodes);
    Map<String, Double> res2 = ft.runFasttext(s.toUpperCase(Locale.ROOT), langCodes);
    assertEquals(res1, res2);
  }

  @Test
  @Ignore("for interactive use")
  public void testEverShorterTextWithFastText() throws IOException {
    FastText ft = new FastText(new File("/prg/fastText-0.9.2/lid.176.bin"), new File("/prg/fastText-0.9.2/fasttext"));
    //FastText ft = new FastText(new File("/home/languagetool/fasttext/lid.176.bin"), new File("/prg/fastText/fasttext"));
    //FastText ft = new FastText(new File("/home/languagetool/fasttext/lid.176.bin"), new File("/prg/fastText-0.1.0/fasttext"));
    List<String> langs = Arrays.asList("en", "de", "fr", "es", "nl");
    //List<String> lines = Files.readAllLines(Paths.get("/home/dnaber/lt/lang-detect-test-25K.txt"));
    List<String> lines = Files.readAllLines(Paths.get("/home/dnaber/lt/lang-detect-test-en-25K.txt"));
    String expected = "en";
    System.out.println("Loaded " + lines.size() + " lines");
    long startTime = System.currentTimeMillis();
    int lineCount = 0;
    long thresholdLengthsSum = 0;
    for (String line : lines) {
      lineCount++;
      for (int i = line.length(); i > 0; i--) {
        String shortened = line.substring(0, i);
        Map<String, Double> resMap = ft.runFasttext(shortened, langs);
        String bestLang = getBestLang(resMap);
        //System.out.println(shortened + " -> " + bestLang);
        if (!expected.equals(bestLang)) {
          System.out.println(lineCount + ". " + bestLang + " " + shortened + " @" + shortened.length());
          thresholdLengthsSum += shortened.length();
          break;
        }
      }
    }
    long endTime = System.currentTimeMillis();
    System.out.println("runtime: "+ (endTime-startTime) + "ms");
    float avg = (float)thresholdLengthsSum / lineCount;
    System.out.printf(Locale.ENGLISH, "avg. first detection failure: %.2f%n", avg);
  }

  @Nullable
  private String getBestLang(Map<String, Double> resMap) {
    String bestLang = null;
    double baseValSoFar = 0;
    for (Map.Entry<String, Double> entry : resMap.entrySet()) {
      if (entry.getValue() > baseValSoFar) {
        bestLang = entry.getKey();
        baseValSoFar = entry.getValue();
      }
    }
    return bestLang;
  }

  @Test
  public void testParsing() throws Exception {
    FastText ft = new FastText();
    List<String> l = Arrays.asList("en", "fy", "de", "es", "nl");
    Map<String, Double> res1 = ft.parseBuffer("__label__nl 0.423696 __label__fy 0.207109\n", l);
    assertThat(res1.size(), is(2));
    assertThat(res1.get("nl"), is(0.423696));
    assertThat(res1.get("fy"), is(0.207109));
    Map<String, Double> res2 = ft.parseBuffer("__label__de 0.999985 __label__es 2.02195e-05", l);
    assertThat(res2.size(), is(2));
    assertThat(res2.get("de"), is(0.999985));
    assertThat(res2.get("es"), is(2.02195e-05));
    Map<String, Double> res3 = ft.parseBuffer("__label__en 1", l);
    assertThat(res3.size(), is(1));
    assertThat(res3.get("en"), is(1.0));
    Map<String, Double> res4 = ft.parseBuffer("__label__de 1.00003", l);  // values larger 1 can actually happen
    assertThat(res4.size(), is(1));
    assertThat(res4.get("de"), is(1.00003));
    try {
      ft.parseBuffer("xxx", l);
      fail();
    } catch (RuntimeException expected) {}
    try {
      ft.parseBuffer("xxx foo", l);
      fail();
    } catch (RuntimeException expected) {}
    // this had happened in the past when somehow Fasttext received two texts and thus
    // responds with two lines of output:
    System.out.println("Ignore the following WARN:");
    Map<String, Double> res5 = ft.parseBuffer("__label__de 0.9\n__label__fr 0.1", l);
    assertThat(res5.size(), is(1));
    assertThat(res5.get("de"), is(0.9));
  }

}
