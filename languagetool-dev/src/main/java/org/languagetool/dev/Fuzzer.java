/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;

import java.io.IOException;
import java.util.Random;

/**
 * A very simple fuzzer to see if certain random input causes long processing times.
 */
public class Fuzzer {

  private final static String chars = "0,.-";

  private void run() throws IOException {
    Random rnd = new Random(231);
    for (Language language : Languages.get()) {
      JLanguageTool lt = new JLanguageTool(language);
      String text = fuzz(rnd);
      long t1 = System.currentTimeMillis();
      System.out.println(language.getShortCode() + " with text length of " + text.length() + "...");
      System.out.println(">> " + text);
      lt.check(text);
      long t2 = System.currentTimeMillis();
      System.out.println(language.getShortCode() + ": " + (t2-t1) + "ms");
    }
  }

  private String fuzz(Random rnd) {
    String[] charsList = chars.split("");
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < charsList.length; i++) {
      int randomPos = rnd.nextInt(charsList.length);
      int repeat = rnd.nextInt(1000);
      String s = StringUtils.repeat(charsList[randomPos], repeat);
      sb.append(s);
    }
    return sb.toString();
  }

  public static void main(String[] args) throws IOException {
    new Fuzzer().run();
  }

}
