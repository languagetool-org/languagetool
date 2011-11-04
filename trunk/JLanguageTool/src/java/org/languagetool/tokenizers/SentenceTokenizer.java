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
package de.danielnaber.languagetool.tokenizers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tokenizes text into sentences by looking for typical end-of-sentence markers,
 * but considering exceptions (e.g. abbreviations).
 *
 * @author Daniel Naber
 */
public class SentenceTokenizer implements Tokenizer {

  // end of sentence marker:
  protected static final String EOS = "\0";
  //private final static String EOS = "#"; // for testing only
  protected static final String P = "[\\.!?…]"; // PUNCTUATION
  protected static final String AP = "(?:'|«|\"||\\)|\\]|\\})?"; // AFTER PUNCTUATION
  protected static final String PAP = P + AP;
  protected static final String PARENS = "[\\(\\)\\[\\]]"; // parentheses

  // Check out the private methods for comments and examples about these
  // regular expressions:

  private Pattern paragraph;
  private static final Pattern paragraphByTwoLineBreaks = Pattern.compile("([\\n\\r]\\s*[\\n\\r])");
  private static final Pattern paragraphByLineBreak = Pattern.compile("([\\n\\r])");

  // add unbreakable field, for example footnote, if it's at the end of the sentence
  private static final Pattern punctWhitespace = Pattern.compile("(" + PAP + "(\u0002)?\\s)");
  // \p{Lu} = uppercase, with obeying Unicode (\p{Upper} is just US-ASCII!):
  private static final Pattern punctUpperLower = Pattern.compile("(" + PAP
      + ")([\\p{Lu}][^\\p{Lu}.])");
  private static final Pattern letterPunct = Pattern.compile("(\\s[\\wüöäÜÖÄß]" + P + ")");
  private static final Pattern abbrev1 = Pattern.compile("([^-\\wüöäÜÖÄß][\\wüöäÜÖÄß]" + PAP + "\\s)" + EOS);
  private static final Pattern abbrev2 = Pattern.compile("([^-\\wüöäÜÖÄß][\\wüöäÜÖÄß]" + P + ")" + EOS);
  private static final Pattern abbrev3 = Pattern.compile("(\\s[\\wüöäÜÖÄß]\\.\\s+)" + EOS);
  private static final Pattern abbrev4 = Pattern.compile("(\\.\\.\\. )" + EOS + "([\\p{Ll}])");
  private static final Pattern abbrev5 = Pattern.compile("(['\"]" + P + "['\"]\\s+)" + EOS);
  private static final Pattern abbrev6 = Pattern.compile("([\"']\\s*)" + EOS + "(\\s*[\\p{Ll}])");
  private static final Pattern abbrev7 = Pattern.compile("(\\s" + PAP + "\\s)" + EOS);
  // z.b. 3.10. (im Datum):
  private static final Pattern abbrev8 = Pattern.compile("(\\d{1,2}\\.\\d{1,2}\\.\\s+)" + EOS);
  private static final Pattern repair1 = Pattern.compile("('[\\wüöäÜÖÄß]" + P + ")(\\s)");
  private static final Pattern repair2 = Pattern.compile("(\\sno\\.)(\\s+)(?!\\d)");
  private static final Pattern repair3 = Pattern.compile("([ap]\\.m\\.\\s+)([\\p{Lu}])");

  private static final Pattern repair10 = Pattern.compile("([\\(\\[])([!?]+)([\\]\\)]) " + EOS);
  private static final Pattern repair11 = Pattern.compile("([!?]+)([\\)\\]]) " + EOS);
  private static final Pattern repair12 = Pattern.compile("(" + PARENS + ") " + EOS);

  // some abbreviations:
  private static final String[] ABBREV_LIST = {
      // English -- but these work globally for all languages:
      "Mr", "Mrs", "No", "pp", "St", "no",
      "Sr", "Jr", "Bros", "etc", "vs", "esp", "Fig", "fig", "Jan", "Feb", "Mar", "Apr", "Jun", "Jul",
      "Aug", "Sep", "Sept", "Oct", "Okt", "Nov", "Dec", "Ph.D", "PhD",
      "al",  // in "et al."
      "cf", "Inc", "Ms", "Gen", "Sen", "Prof", "Corp", "Co"
  };

  private final Set<Pattern> abbreviationPatterns = new HashSet<Pattern>();

  /**
   * Month names like "Dezember" that should not be considered a sentence
   * boundary in string like "13. Dezember". May also contain other
   * words that indicate there's no sentence boundary when preceded
   * by a number and a dot.
   */
  protected String[] monthNames;

  /**
   * Create a sentence tokenizer that uses the built-in abbreviations.
   */
  public SentenceTokenizer() {
    this(new String[]{});
  }

  /**
   * Create a sentence tokenizer with the given list of abbreviations,
   * additionally to the built-in ones.
   */
  public SentenceTokenizer(final String[] abbrevList) {
    final List<String> allAbbreviations = new ArrayList<String>();
    allAbbreviations.addAll(Arrays.asList(abbrevList));
    allAbbreviations.addAll(Arrays.asList(ABBREV_LIST));
    for (String element : allAbbreviations) {
      final Pattern pattern = Pattern.compile("(\\b" + element + PAP + "\\s)" + EOS);
      abbreviationPatterns.add(pattern);
    }
    setSingleLineBreaksMarksParagraph(false);
  }

  /**
   * @param lineBreakParagraphs if <code>true</code>, single lines breaks are assumed to end a paragraph,
   *  with <code>false</code>, only two ore more consecutive line breaks end a paragraph
   */
  public void setSingleLineBreaksMarksParagraph(final boolean lineBreakParagraphs) {
    if (lineBreakParagraphs) {
      paragraph = paragraphByLineBreak;
    } else {
      paragraph = paragraphByTwoLineBreaks;
    }
  }

  public boolean singleLineBreaksMarksPara() {
    return paragraph == paragraphByLineBreak;
  }

  /**
   * Tokenize the given string to sentences.
   */
  @Override
  public List<String> tokenize(String s) {
    s = firstSentenceSplitting(s);
    s = removeFalseEndOfSentence(s);
    s = splitUnsplitStuff(s);
    final StringTokenizer stringTokenizer =
      new StringTokenizer(s, EOS);
    final List<String> l = new ArrayList<String>();
    while (stringTokenizer.hasMoreTokens()) {
      final String sentence = stringTokenizer.nextToken();
      l.add(sentence);
    }
    return l;
  }

  /**
   * Add a special break character at all places with typical sentence delimiters.
   */
  private String firstSentenceSplitting(String s) {
    // Double new-line means a new sentence:
    s = paragraph.matcher(s).replaceAll("$1" + EOS);
    // Punctuation followed by whitespace means a new sentence:
    s = punctWhitespace.matcher(s).replaceAll("$1" + EOS);
    // New (compared to the perl module): Punctuation followed by uppercase followed
    // by non-uppercase character (except dot) means a new sentence:
    s = punctUpperLower.matcher(s).replaceAll("$1" + EOS + "$2");
    // Break also when single letter comes before punctuation:
    s = letterPunct.matcher(s).replaceAll("$1" + EOS);
    return s;
  }

  /**
   * Repair some positions that don't require a split, i.e. remove the special break character at
   * those positions.
   */
  protected String removeFalseEndOfSentence(String s) {
    // Don't split at e.g. "U. S. A.":
    s = abbrev1.matcher(s).replaceAll("$1");
    // Don't split at e.g. "U.S.A.":
    s = abbrev2.matcher(s).replaceAll("$1");
    // Don't split after a white-space followed by a single letter followed
    // by a dot followed by another whitespace.
    // e.g. " p. "
    s = abbrev3.matcher(s).replaceAll("$1");
    // Don't split at "bla bla... yada yada" (TODO: use \.\.\.\s+ instead?)
    s = abbrev4.matcher(s).replaceAll("$1$2");
    // Don't split [.?!] when the're quoted:
    s = abbrev5.matcher(s).replaceAll("$1");

    // Don't split at abbreviations:
    for (final Pattern abbrevPattern : abbreviationPatterns) {
      final Matcher matcher = abbrevPattern.matcher(s);
      s = matcher.replaceAll("$1");
    }
    // Don't break after quote unless there's a capital letter:
    // e.g.: "That's right!" he said.
    s = abbrev6.matcher(s).replaceAll("$1$2");

    // fixme? not sure where this should occur, leaving it commented out:
    // don't break: text . . some more text.
    // text=~s/(\s\.\s)$EOS(\s*)/$1$2/sg;

    // e.g. "Das ist . so." -> assume one sentence
    s = abbrev7.matcher(s).replaceAll("$1");

    // e.g. "Das ist . so." -> assume one sentence
    s = abbrev8.matcher(s).replaceAll("$1");

    // extension by dnaber --commented out, doesn't help:
    // text = re.compile("(:\s+)%s(\s*[%s])" % (self.EOS, string.lowercase),
    // re.DOTALL).sub("\\1\\2", text)

    // "13. Dezember" etc. -> keine Satzgrenze:
    if (monthNames != null) {
      for (String element : monthNames) {
        s = s.replaceAll("(\\d+\\.) " + EOS + "(" + element + ")", "$1 $2");
      }
    }

    // z.B. "Das hier ist ein(!) Satz."
    s = repair10.matcher(s).replaceAll("$1$2$3 ");

    // z.B. "Das hier ist (genau!) ein Satz."
    s = repair11.matcher(s).replaceAll("$1$2 ");

    // z.B. "bla (...) blubb" -> kein Satzende
    s = repair12.matcher(s).replaceAll("$1 ");

    return s;
  }

  /**
   * Treat some more special cases that make up a sentence boundary. Insert the special break
   * character at these positions.
   */
  private String splitUnsplitStuff(String s) {
    // e.g. "x5. bla..." -- not sure, leaving commented out:
    // text = re.compile("(\D\d+)(%s)(\s+)" % self.P, re.DOTALL).sub("\\1\\2%s\\3" % self.EOS, text)
    // Not sure about this one, leaving out four now:
    // text = re.compile("(%s\s)(\s*\()" % self.PAP, re.DOTALL).sub("\\1%s\\2" % self.EOS, text)
    // Split e.g.: He won't. #Really.
    s = repair1.matcher(s).replaceAll("$1" + EOS + "$2");
    // Split e.g.: He won't say no. Not really.
    s = repair2.matcher(s).replaceAll("$1" + EOS + "$2");
    // Split at "a.m." or "p.m." followed by a capital letter.
    s = repair3.matcher(s).replaceAll("$1" + EOS + "$2");
    return s;
  }

  /*public static void main(final String[] args) {
    final SentenceTokenizer st = new GermanSentenceTokenizer();
    st.tokenize("Er sagte (...) und");
  }*/

}
