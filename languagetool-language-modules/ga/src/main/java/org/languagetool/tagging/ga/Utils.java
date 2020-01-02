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

  private static final String MATHEMATICAL_BOLD_CAPITALS =
    "\uDC00\uDC01\uDC02\uDC03\uDC04\uDC05\uDC06\uDC07\uDC08\uDC09\uDC0A\uDC0B\uDC0C"
  + "\uDC0D\uDC0E\uDC0F\uDC10\uDC11\uDC12\uDC13\uDC14\uDC15\uDC16\uDC17\uDC18\uDC19";
  private static final String MATHEMATICAL_BOLD_LOWERS =
    "\uDC1A\uDC1B\uDC1C\uDC1D\uDC1E\uDC1F\uDC20\uDC21\uDC22\uDC23\uDC24\uDC25\uDC26"
  + "\uDC27\uDC28\uDC29\uDC2A\uDC2B\uDC2C\uDC2D\uDC2E\uDC2F\uDC30\uDC31\uDC32\uDC33";
  private static final String MATHEMATICAL_ITALIC_CAPITALS =
    "\uDC34\uDC35\uDC36\uDC37\uDC38\uDC39\uDC3A\uDC3B\uDC3C\uDC3D\uDC3E\uDC3F\uDC40"
  + "\uDC41\uDC42\uDC43\uDC44\uDC45\uDC46\uDC47\uDC48\uDC49\uDC4A\uDC4B\uDC4C\uDC4D";
  private static final String MATHEMATICAL_ITALIC_LOWERS =
    "\uDC4E\uDC4F\uDC50\uDC51\uDC52\uDC53\uDC54\uDC55\uDC56\uDC57\uDC58\uDC59\uDC5A"
  + "\uDC5B\uDC5C\uDC5D\uDC5E\uDC5F\uDC60\uDC61\uDC62\uDC63\uDC64\uDC65\uDC66\uDC67";
  private static final String MATHEMATICAL_BOLD_ITALIC_CAPITALS =
    "\uDC68\uDC69\uDC6A\uDC6B\uDC6C\uDC6D\uDC6E\uDC6F\uDC70\uDC71\uDC72\uDC73\uDC74"
  + "\uDC75\uDC76\uDC77\uDC78\uDC79\uDC7A\uDC7B\uDC7C\uDC7D\uDC7E\uDC7F\uDC80\uDC81";
  private static final String MATHEMATICAL_BOLD_ITALIC_LOWERS =
    "\uDC82\uDC83\uDC84\uDC85\uDC86\uDC87\uDC88\uDC89\uDC8A\uDC8B\uDC8C\uDC8D\uDC8E"
  + "\uDC8F\uDC90\uDC91\uDC92\uDC93\uDC94\uDC95\uDC96\uDC97\uDC98\uDC99\uDC9A\uDC9B";
  private static final String MATHEMATICAL_SCRIPT_CAPITALS =
    "\uDC9C\uDC9D\uDC9E\uDC9F\uDCA0\uDCA1\uDCA2\uDCA3\uDCA4\uDCA5\uDCA6\uDCA7\uDCA8"
  + "\uDCA9\uDCAA\uDCAB\uDCAC\uDCAD\uDCAE\uDCAF\uDCB0\uDCB1\uDCB2\uDCB3\uDCB4\uDCB5";
  private static final String MATHEMATICAL_SCRIPT_LOWERS =
    "\uDCB6\uDCB7\uDCB8\uDCB9\uDCBA\uDCBB\uDCBC\uDCBD\uDCBE\uDCBF\uDCC0\uDCC1\uDCC2"
  + "\uDCC3\uDCC4\uDCC5\uDCC6\uDCC7\uDCC8\uDCC9\uDCCA\uDCCB\uDCCC\uDCCD\uDCCE\uDCCF";
  private static final int MATHEMATICAL_BOLD_CAPITAL_A = Character.getNumericValue('\uDC00');
  private static final int MATHEMATICAL_BOLD_CAPITAL_Z = Character.getNumericValue('\uDC19');
  private static final int MATHEMATICAL_BOLD_LOWER_A = Character.getNumericValue('\uDC1A');
  private static final int MATHEMATICAL_BOLD_LOWER_Z = Character.getNumericValue('\uDC33');
  private static final int MATHEMATICAL_ITALIC_CAPITAL_A = Character.getNumericValue('\uDC34');
  private static final int MATHEMATICAL_ITALIC_CAPITAL_Z = Character.getNumericValue('\uDC4D');
  private static final int MATHEMATICAL_ITALIC_LOWER_A = Character.getNumericValue('\uDC4E');
  private static final int MATHEMATICAL_ITALIC_LOWER_Z = Character.getNumericValue('\uDC67');
  private static final int MATHEMATICAL_BOLD_ITALIC_CAPITAL_A = Character.getNumericValue('\uDC68');
  private static final int MATHEMATICAL_BOLD_ITALIC_CAPITAL_Z = Character.getNumericValue('\uDC81');
  private static final int MATHEMATICAL_BOLD_ITALIC_LOWER_A = Character.getNumericValue('\uDC82');
  private static final int MATHEMATICAL_BOLD_ITALIC_LOWER_Z = Character.getNumericValue('\uDC9B');
  private static final int MATHEMATICAL_SCRIPT_CAPITAL_A = Character.getNumericValue('\uDC9C');
  private static final int MATHEMATICAL_SCRIPT_CAPITAL_Z = Character.getNumericValue('\uDCB5');
  private static final int MATHEMATICAL_SCRIPT_LOWER_A = Character.getNumericValue('\uDCB6');
  private static final int MATHEMATICAL_SCRIPT_LOWER_Z = Character.getNumericValue('\uDCCF');
  private static final String ASCII_CAPITALS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String ASCII_LOWERS = "abcdefghijklmnopqrstuvwxyz";

  private static char getMathsChar(char c) {
    int numeric = Character.getNumericValue(c);
    if(numeric < MATHEMATICAL_BOLD_CAPITAL_A) {
      return c;
    } else {
      System.err.println("BASR");
      String inmap = MATHEMATICAL_BOLD_CAPITALS;
      String outmap = ASCII_CAPITALS;
      if(numeric >= MATHEMATICAL_BOLD_CAPITAL_A && numeric <= MATHEMATICAL_BOLD_CAPITAL_Z) {
        System.err.println("Upper " + numeric);
        inmap = MATHEMATICAL_BOLD_CAPITALS;
        outmap = ASCII_CAPITALS;
      } else if(numeric >= MATHEMATICAL_BOLD_LOWER_A && numeric <= MATHEMATICAL_BOLD_LOWER_Z) {
        System.err.println("Lower");
        inmap = MATHEMATICAL_BOLD_LOWERS;
        outmap = ASCII_LOWERS;
      } else if(numeric >= MATHEMATICAL_ITALIC_CAPITAL_A && numeric <= MATHEMATICAL_ITALIC_CAPITAL_Z) {
        inmap = MATHEMATICAL_ITALIC_CAPITALS;
        outmap = ASCII_CAPITALS;
      } else if(numeric >= MATHEMATICAL_ITALIC_LOWER_A && numeric <= MATHEMATICAL_ITALIC_LOWER_Z) {
        inmap = MATHEMATICAL_ITALIC_LOWERS;
        outmap = ASCII_LOWERS;
      } else if(numeric >= MATHEMATICAL_BOLD_ITALIC_CAPITAL_A && numeric <= MATHEMATICAL_BOLD_ITALIC_CAPITAL_Z) {
        inmap = MATHEMATICAL_BOLD_ITALIC_CAPITALS;
        outmap = ASCII_CAPITALS;
      } else if(numeric >= MATHEMATICAL_BOLD_ITALIC_LOWER_A && numeric <= MATHEMATICAL_BOLD_ITALIC_LOWER_Z) {
        inmap = MATHEMATICAL_BOLD_ITALIC_LOWERS;
        outmap = ASCII_LOWERS;
      } else if(numeric >= MATHEMATICAL_SCRIPT_CAPITAL_A && numeric <= MATHEMATICAL_SCRIPT_CAPITAL_Z) {
        inmap = MATHEMATICAL_SCRIPT_CAPITALS;
        outmap = ASCII_CAPITALS;
      } else if(numeric >= MATHEMATICAL_SCRIPT_LOWER_A && numeric <= MATHEMATICAL_SCRIPT_LOWER_Z) {
        inmap = MATHEMATICAL_SCRIPT_LOWERS;
        outmap = ASCII_LOWERS;
      }
      int pos = inmap.indexOf(c);
      if(pos < 0) {
        throw new RuntimeException("Error reading character " + c + " (" + numeric + ")");
      }
      return outmap.charAt(pos);
    }
  }

  public static String simplifyMathematical(String s) {
    String out = "";
    for(int i = 0; i < s.length(); i++) {
      if(s.charAt(i) == '\uD835') {
        int j = i + 1;
        if(j < s.length() && Character.getNumericValue(s.charAt(j)) >= MATHEMATICAL_BOLD_CAPITAL_A) {
          char mapped = getMathsChar(s.charAt(j));
          if(mapped == s.charAt(j)) {
            out += s.charAt(i);
            out += s.charAt(j);
          } else {
            out += mapped;
          }
        }
      }
    }
    return out;
  }
}
