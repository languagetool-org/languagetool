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
  private static class SuffixGuess {
    String suffix;
    String suffixReplacement;
    String restrictToTags;
    String appendTags;
    SuffixGuess(String suffix,
                String suffixReplacement,
                String restrictToTags,
                String appendTags) {
      this.suffix = suffix;
      this.suffixReplacement = suffixReplacement;
      this.restrictToTags = restrictToTags;
      this.appendTags = appendTags;
    }
  }
  private static final List<SuffixGuess> guesses = Arrays.asList(
    new SuffixGuess("éaracht", "éireacht", ".*Noun.*", ":MorphError"),
    new SuffixGuess("éarachta", "éireachta", ".*Noun.*", ":MorphError")
    //new FormGuess("T-S", "S", "", "", )
  );

  public static Retaggable demutate(String in) {
    String out;
    if((out = unLeniteDefiniteS(in)) != null) {
      return new Retaggable(out, "(?:C[UMC]:)?Noun:.*:DefArt", ":MorphError");
    }
    if((out = unLenite(in)) != null) {
      return new Retaggable(out, "", ":Len");
    }
    if((out = unEclipse(in)) != null) {
      String out2 = unLenite(out);
      if(out2 == null) {
        return new Retaggable(out, "", ":Ecl");
      } else {
        return new Retaggable(out2, "", ":EclLen");
      }
    }
    return new Retaggable(in, "", "");
  }

  public static String unEclipse(String in) {
    if(in.length() > 2) {
      char ch1 = in.charAt(1);
      switch(in.charAt(0)) {
        case 'N':
        case 'n':
          if(in.length() > 3 && in.charAt(1) == '-') {
            ch1 = in.charAt(2);
          }
          if(ch1 == 'G' || ch1 == 'D' || isUpperVowel(ch1) || ch1 == 'g' || ch1 == 'd' || isLowerVowel(ch1)) {
            return unEclipseChar(in, 'n', Character.toLowerCase(ch1));
          } else {
            return null;
          }
        case 'B':
        case 'b':
          if((ch1 == 'p' || ch1 == 'P') ||
            in.length() > 3 && in.charAt(1) == '-' && (ch1 == 'p' || ch1 == 'P')) {
            return unEclipseChar(in, 'b', 'p');
          } else {
            return unEclipseF(in);
          }
        case 'D':
        case 'd':
          return unEclipseChar(in, 'd', 't');
        case 'G':
        case 'g':
          return unEclipseChar(in, 'g', 'c');
        case 'M':
        case 'm':
          return unEclipseChar(in, 'm', 'b');
      }
    }
    return null;
  }

  /**
   * Attempts to unlenite a string
   * Deliberately does not check if first character is one
   * that ought to be lenited (this can be checked in XML rules)
   * @param in
   * @return
   */
  public static String unLenite(String in) {
    if(in.length() < 2) {
      return null;
    }
    if(in.charAt(1) == 'h' || in.charAt(1) == 'H') {
      return in.charAt(0) + in.substring(2);
    }
    return null;
  }

  public static String unLeniteDefiniteS(String in) {
    String[] uppers = {"Ts", "T-s", "TS", "T-S", "t-S", "tS"};
    String[] lowers = {"ts", "t-s"};
    for(String start : uppers) {
      if(in.length() < start.length()) {
        continue;
      }
      if(in.startsWith(start)) {
        return "S" + in.substring(start.length());
      }
    }
    for(String start : lowers) {
      if(in.length() < start.length()) {
        continue;
      }
      if(in.startsWith(start)) {
        return "s" + in.substring(start.length());
      }
    }
    return null;
  }
  public static String unEclipseF(String in) {
    String[] uppers = {"Bhf", "bhF", "Bf", "bhF", "bF", "Bh-f", "bh-F", "B-f", "bh-F", "b-F"};
    String[] lowers = {"bhf", "bh-f", "bf", "b-f"};
    for(String start : uppers) {
      if(in.length() < start.length()) {
        continue;
      }
      if(in.startsWith(start)) {
        return "F" + in.substring(start.length());
      }
    }
    for(String start : lowers) {
      if(in.length() < start.length()) {
        continue;
      }
      if(in.startsWith(start)) {
        return "f" + in.substring(start.length());
      }
    }
    return null;
  }

  /**
   * Helper to uneclipse single-letter consonant eclipsis (i.e., not bhfear or
   *  n-éin), handling miscapitalised eclipsed words: Gcarr -> Carr, etc.
   * @param in string to uneclipse
   * @param first first (eclipsis) character
   * @param second second character; first character of the word proper
   * @return String with uneclipsed word or null if no match
   */
  public static String unEclipseChar(String in, char first, char second) {
    int from = 2;
    char upperFirst = Character.toUpperCase(first);
    char upperSecond = Character.toUpperCase(second);
    char retSecond = (in.charAt(0) == upperFirst) ? upperSecond : second;
    // bail out if there's nothing to do
    if(in.length() < 2) {
      return null;
    }
    // no match
    if(in.charAt(0) != first && in.charAt(0) != upperFirst) {
      return null;
    }
    // properly eclipsed
    if(in.charAt(0) == first && (in.charAt(1) == second || in.charAt(1) == upperSecond)) {
      return in.substring(1);
    }

    char ch1 = in.charAt(1);
    if(in.length() > 3 && in.charAt(1) == '-') {
      from++;
      ch1 = in.charAt(2);
    }
    if(ch1 == second || ch1 == upperSecond) {
      return Character.toString(retSecond)+ in.substring(from);
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
