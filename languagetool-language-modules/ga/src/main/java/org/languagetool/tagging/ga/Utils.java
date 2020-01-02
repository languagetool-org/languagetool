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
            in.length() > 3 && in.charAt(1) == '-') {
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
   * Attempts to unlenite a string (See {@link #lenite(String)})
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

  /**
   * Removes lenition from a word beginning with 's', following
   * the definite article; as an exception to conventional
   * lenition, this is a 't' prefix.
   * The standard representation is a lowercase 't', regardless
   * of the case of the word; this function additionally checks
   * for incorrect (e.g., capital 'T') and prestandard (e.g.,
   * hyphenated 't-') versions.
   * @param in The written form
   * @return The form with lenition removed
   */
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

  /**
   * The (non-definite) eclipsed form of 's', 'sh',
   * is pronounced like 'h' in English; words beginning
   * with 's' can only have lenition applied if the
   * following letter would be easily pronounced after
   * this sound: this function checks if the that second
   * letter is one of them
   * @param c The second letter of a word beginning with 's'
   * @return true if the word can be lenited
   */
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

  /**
   * lenites a word
   * ("Lenition" in Irish grammar is an initial mutation,
   * historically related to phonetic lenition; its
   * written representation is an 'h' after the initial
   * consonant).
   * In this context, to "lenite" is to apply lenition)
   * @param in word form to be lenited
   * @return lenited form, or unmodified string if it
   * cannot be lenited
   */
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

  /**
   * eclipses a word
   * ("Eclipsis" in Irish grammar is an initial mutation,
   * represented as a prefix to the word that replaces
   * the pronunciation of the letter for consonants, i.e.,
   * 'f' is eclipsed as 'bh' - 'focal' becomes 'bhfocal' -
   * but only 'bh' (not 'f') is pronounced; or, with vowels,
   * an initial 'n' is added (hyphenated before a lowercase
   * word, lowercased but not hyphenated before an uppercase
   * or titlecase word).
   * In this context, to "eclipse" is to apply eclipsis)
   * @param in word form to be eclipsed
   * @return eclipsed form, or unmodified string if it
   * cannot be eclipsed
   */
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

  /**
   * Case folding in Irish is non-trivial: initial mutations that
   * prefix the word are always written in lowercase; 'n' and 't'
   * are written with a hyphen before a lowercase vowel.
   * Converting to uppercase is impossible without a dictionary:
   * unlike 'n' and 't' (and unlike Scots Gaelic), 'h' is not
   * written hyphenated as 'h' was not traditionally a 'letter', per
   * se, but was used to indicate phonetic changes: in modern Irish,
   * there are enough words that begin with 'h' that converting to
   * uppercase is impossible without a dictionary.
   * @param s the word to lowercase
   * @return lowercased word
   */
  public static String toLowerCaseIrish(String s) {
    if(s.length() > 1 && (s.charAt(0) == 'n' || s.charAt(0) == 't') && isUpperVowel(s.charAt(1))) {
      return s.substring(0,1) + "-" + s.substring(1).toLowerCase();
    } else {
      return s.toLowerCase();
    }
  }

  private static boolean isUpperPonc(char c) {
    switch(c) {
      case 'Ḃ':
      case 'Ċ':
      case 'Ḋ':
      case 'Ḟ':
      case 'Ġ':
      case 'Ṁ':
      case 'Ṗ':
      case 'Ṡ':
      case 'Ṫ':
        return true;
      default:
        return false;
    }
  }

  private static boolean isLowerPonc(char c) {
    switch(c) {
      case 'ḃ':
      case 'ċ':
      case 'ḋ':
      case 'ḟ':
      case 'ġ':
      case 'ṁ':
      case 'ṗ':
      case 'ṡ':
      case 'ṫ':
        return true;
      default:
        return false;
    }
  }

  /**
   * Check if the character is dotted ('ponc' in Irish)
   * @param c the character to check
   * @return true if the character is dotted, false otherwise
   */
  public static boolean isPonc(char c) {
    return isUpperPonc(c) || isLowerPonc(c);
  }

  public static boolean containsPonc(String s) {
    for(char c : s.toCharArray()) {
      if(isPonc(c)) {
        return true;
      }
    }
    return false;
  }

  private static char unPonc(char c) {
    switch(c) {
      case 'Ḃ':
        return 'B';
      case 'Ċ':
        return 'C';
      case 'Ḋ':
        return 'D';
      case 'Ḟ':
        return 'F';
      case 'Ġ':
        return 'G';
      case 'Ṁ':
        return 'M';
      case 'Ṗ':
        return 'P';
      case 'Ṡ':
        return 'S';
      case 'Ṫ':
        return 'T';
      case 'ḃ':
        return 'b';
      case 'ċ':
        return 'c';
      case 'ḋ':
        return 'd';
      case 'ḟ':
        return 'f';
      case 'ġ':
        return 'g';
      case 'ṁ':
        return 'm';
      case 'ṗ':
        return 'p';
      case 'ṡ':
        return 's';
      case 'ṫ':
        return 't';
      default:
        return c;
    }
  }

  /**
   * Converts pre-standard lenition to modern
   * (converts dotted (= ponc) letters to the equivalent
   * undotted, followed by 'h'
   * @param s string to convert
   * @return converted string
   */
  public static String unPonc(String s) {
    StringBuilder sb = new StringBuilder();
    for(int i = 0; i < s.length(); i++) {
      if(!isPonc(s.charAt(i))) {
        sb.append(unPonc(s.charAt(i)));
      } else {
        if(isLowerPonc(s.charAt(i))) {
          sb.append(unPonc(s.charAt(i)));
          sb.append('h');
        } else {
          if(i < s.length() - 1 && Character.isUpperCase(s.charAt(i + 1))) {
            sb.append(unPonc(s.charAt(i)));
            sb.append('H');
          } else if(i == s.length() - 1 && i > 0 && Character.isUpperCase(s.charAt(i - 1))) {
            sb.append(unPonc(s.charAt(i)));
            sb.append('H');
          } else {
            sb.append(unPonc(s.charAt(i)));
            sb.append('h');
          }
        }
      }
    }

    return sb.toString();
  }
}
