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
    public String suffix;
    public String suffixReplacement;
    public String restrictToTags;
    public String appendTags;
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
    new SuffixGuess("éarachta", "éireachta", ".*Noun.*", ":MorphError"),
    new SuffixGuess("eamhail", "iúil", ".*Noun.*|.*Adj.*", ":MorphError"),
    new SuffixGuess("eamhuil", "iúil", ".*Noun.*|.*Adj.*", ":MorphError"),
    new SuffixGuess("eamhla", "iúla", ".*Noun.*|.*Adj.*", ":MorphError"),
    new SuffixGuess("amhail", "úil", ".*Noun.*|.*Adj.*", ":MorphError"),
    new SuffixGuess("amhuil", "úil", ".*Noun.*|.*Adj.*", ":MorphError")
  );

  public static Retaggable fixSuffix(String in) {
    for(SuffixGuess guess : guesses) {
      if(in.endsWith(guess.suffix)) {
        String base = in.substring(0, in.length() - guess.suffix.length());
        return new Retaggable(base + guess.suffixReplacement, guess.restrictToTags, guess.appendTags);
      }
    }
    return new Retaggable(in, "", "");
  }

  public static List<Retaggable> morphWord(String in) {
    List<Retaggable> out = new ArrayList<>();
    // First, mutations
    Retaggable mut = demutate(in);
    if(mut.getAppendTag().equals(":Len:MorphError") || mut.getAppendTag().equals(":Ecl:MorphError") || mut.getAppendTag().equals(":EclLen")) {
      out.add(mut);
      out.add(new Retaggable(mut.getWord(), mut.getRestrictToPos(), ":DefArt:MorphError"));
    } else if(!"".equals(mut.getAppendTag())) {
      out.add(mut);
    }
    // Second, suffixes
    Retaggable sfx = fixSuffix(mut.getWord());
    if(!"".equals(sfx.getAppendTag())) {
      sfx.setAppendTag(mut.getAppendTag());
      out.add(sfx);
    }
    // TODO: prefix corrections
    // TODO: other alterations
    return out;
  }

  public static Retaggable demutate(String in) {
    String out;
    if((out = unLeniteDefiniteS(in)) != null) {
      return new Retaggable(out, "(?:C[UMC]:)?Noun:.*:DefArt", ":MorphError");
    }
    if((out = unLenite(in)) != null) {
      return new Retaggable(out, "", ":Len:MorphError");
    }
    if((out = unEclipse(in)) != null) {
      String out2 = unLenite(out);
      if(out2 == null) {
        return new Retaggable(out, "", ":Ecl:MorphError");
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

  public static boolean isVowel(char c) {
    return isLowerVowel(c) || isUpperVowel(c);
  }

  public static boolean isUpperLenitable(char c) {
    switch(c) {
      case 'B':
      case 'C':
      case 'D':
      case 'F':
      case 'G':
      case 'M':
      case 'P':
      case 'S':
      case 'T':
        return true;
      default:
        return false;
    }
  }

  public static boolean isLowerLenitable(char c) {
    switch(c) {
      case 'b':
      case 'c':
      case 'd':
      case 'f':
      case 'g':
      case 'm':
      case 'p':
      case 's':
      case 't':
        return true;
      default:
        return false;
    }
  }

  public static boolean isSLenitable(char c) {
    switch(c) {
      case 'l':
      case 'n':
      case 'r':
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

  public static String lenite(String in) {
    if(in.length() < 2) {
      return in;
    }
    String outh = (Character.isUpperCase(in.charAt(0)) && Character.isUpperCase(1)) ? "H" : "h";
    if(isLowerLenitable(in.charAt(0)) || isUpperLenitable(in.charAt(0))) {
      if(in.charAt(0) == 'S' || in.charAt(0) == 's') {
        if(isSLenitable(Character.toLowerCase(in.charAt(1)))) {
          return Character.toString(in.charAt(0)) + outh + in.substring(1);
        } else {
          return in;
        }
      } else {
        return Character.toString(in.charAt(0)) + outh + in.substring(1);
      }
    } else {
      return in;
    }
  }

  public static String eclipse(String in) {
    if(in == null || in.equals("")) {
      return in;
    }
    if(isUpperVowel(in.charAt(0))) {
      return "n" + in;
    }
    if(isLowerVowel(in.charAt(0))) {
      return "n-" + in;
    }

    switch(in.toLowerCase().charAt(0)) {
      case 'b':
        return "m" + in;
      case 'c':
        return "g" + in;
      case 'd':
      case 'g':
        return "n" + in;
      case 'f':
        return "bh" + in;
      case 'p':
        return "b" + in;
      case 't':
        return "d" + in;
      default:
        return in;
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
