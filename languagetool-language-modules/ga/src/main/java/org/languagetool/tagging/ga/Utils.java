/*
 * Copyright 2019 Jim O'Regan <jaoregan@tcd.ie>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.languagetool.tagging.ga;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {
    private static class FormGuess {
      String prefix;
      String prefixReplacement;
      String suffix;
      String suffixReplacement;
      String restrictToTags;
      String appendTags;
      FormGuess(String prefix,
                String prefixReplacement,
                String suffix,
                String suffixReplacement,
                String restrictToTags,
                String appendTags) {
        this.prefix = prefix;
        this.prefixReplacement = prefixReplacement;
        this.suffix = suffix;
        this.suffixReplacement = suffixReplacement;
        this.restrictToTags = restrictToTags;
        this.appendTags = appendTags;
      }
    }
    private static final List<FormGuess> guesses = Arrays.asList(
      new FormGuess("", "", "éaracht", "éireacht", ".*Noun.*", ":MorphError"),
      new FormGuess("", "", "éarachta", "éireachta", ".*Noun.*", ":MorphError"),
      new FormGuess("ts", "s", "", "", "(?:C[UMC]:)?Noun:Masc:Com:Sg:DefArt", ":MorphError"),
      new FormGuess("t-s", "s", "", "", "(?:C[UMC]:)?Noun:Masc:Com:Sg:DefArt", ":MorphError"),
      new FormGuess("tS", "S", "", "", "(?:C[UMC]:)?Noun:Masc:Com:Sg:DefArt", ":MorphError"),
      new FormGuess("Ts", "S", "", "", "(?:C[UMC]:)?Noun:Masc:Com:Sg:DefArt", ":MorphError"),
      new FormGuess("TS", "S", "", "", "(?:C[UMC]:)?Noun:Masc:Com:Sg:DefArt", ":MorphError"),
      new FormGuess("t-S", "S", "", "", "(?:C[UMC]:)?Noun:Masc:Com:Sg:DefArt", ":MorphError"),
      new FormGuess("T-s", "S", "", "", "(?:C[UMC]:)?Noun:Masc:Com:Sg:DefArt", ":MorphError"),
      new FormGuess("T-S", "S", "", "", "(?:C[UMC]:)?Noun:Masc:Com:Sg:DefArt", ":MorphError")
    );

  public class Mutation {
    String word;
    List<String> tags;
    public Mutation() {
      tags = new ArrayList<String>();
    }
  }
  public static String fixMutationCase(String in) {
    String orig = in;
    int from = 1;
    char first = in.charAt(0);
    if(in.length()> 2) {
      char ch1 = in.charAt(1);
      switch(in.charAt(0)) {
        case 'N':
          if(in.length() > 2 && in.charAt(1) == '-') {
            ch1 = in.charAt(2);
          }
          if(ch1 == 'G' || ch1 == 'D' || isUpperVowel(ch1)) {
            return unEclipseChar(in, 'n', Character.toLowerCase(ch1));
          } else {
            return null;
          }
        case 'G':
        case 'g':
          return unEclipseChar(in, 'g', 'c');
        case 'M':
        case 'm':
          return unEclipseChar(in, 'm', 'b');
      }
    }
    return in;
  }

  public static String unLenite(String in) {
    if(in.length() < 2) {
      return null;
    }
    if(in.charAt(1) == 'h' || in.charAt(1) == 'H') {
      return in.charAt(0) + in.substring(2);
    }
    return null;
  }

  private static String unEclipseChar(String in, char first, char second) {
    int from = 2;
    char upperFirst = Character.toUpperCase(first);
    char upperSecond = Character.toUpperCase(second);
    char retSecond = (in.charAt(0) == upperFirst) ? upperSecond : second;
    // bail out if there's nothing to do
    if(in.length() < 2) {
      return null;
    }
    if(in.charAt(0) != first) {
      return null;
    }
    if(in.charAt(0) == first && (in.charAt(1) == second || in.charAt(1) == upperSecond)) {
      return null;
    }

    char ch1 = in.charAt(1);
    if(in.length() > 3 && in.charAt(1) == '-') {
      from++;
      ch1 = in.charAt(2);
    }
    if(ch1 == second || ch1 == upperSecond) {
      return first + retSecond + in.substring(from);
    } else {
      return null;
    }
  }

  public static boolean isUpperVowel(char c) {
    switch(c) {
      case 'A':
      case 'E':
      case 'I':
      case 'O':
      case 'U':
      case '\u00c1':
      case '\u00c9':
      case '\u00cd':
      case '\u00d3':
      case '\u00da':
        return true;
      default:
        return false;
    }
  }

  public static boolean isLowerVowel(char c) {
    switch(c) {
      case 'a':
      case 'e':
      case 'i':
      case 'o':
      case 'u':
      case '\u00e1':
      case '\u00e9':
      case '\u00ed':
      case '\u00f3':
      case '\u00fa':
        return true;
      default:
        return false;
    }
  }

  public static String toLowerCaseIrish(String s) {
    if(s.length() > 1 && (s.charAt(0) == 'n' || s.charAt(0) == 't') && isUpperVowel(s.charAt(1))) {
      return s.substring(0,1) + "-" + s.substring(1).toLowerCase();
    } else {
      return s.toLowerCase();
    }
  }
}
