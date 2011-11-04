/* LanguageTool, a natural language style checker 
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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

/*
 * CzechSentenceTokenizer.java
 *
 * Created on 25.1.2007, 11:45
 */

package de.danielnaber.languagetool.tokenizers.cs;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;

/**
 *
 * @author Jozef Licko
 */
public class CzechSentenceTokenizer extends SentenceTokenizer {

  // End of sentence marker.
  private static final String EOS = "\0";

  // private final static String EOS = "#"; // for testing only

  // Punctuation.
  private static final String P = "[\\.!?…]";

  // After punctuation.
  private static final String AP = "(?:'|«|\"|”|\\)|\\]|\\})?";

  private static final String PAP = P + AP;

  // Check out the private methods for comments and examples about these
  // regular expressions:

  private static final Pattern paragraphByTwoLineBreaks = Pattern.compile("(\\n\\s*\\n)");

  private static final Pattern paragraphByLineBreak = Pattern.compile("(\\n)");

  // add unbreakable field, for example footnote, if it's at the end of the sentence
  private static final Pattern punctWhitespace = Pattern.compile("(" + PAP + "(\u0002)?\\s)");

  // \p{Lu} = uppercase, with obeying Unicode (\p{Upper} is just US-ASCII!):
  private static final Pattern punctUpperLower = Pattern.compile("(" + PAP
      + ")([\\p{Lu}][^\\p{Lu}.])");

  private static final Pattern letterPunct = Pattern
      .compile("(\\s[\\wáčďéěíňóřšťúůýžÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ]" + P + ")");

  private static final Pattern abbrev1 = Pattern
      .compile("([^-\\wáčďéěíňóřšťúůýžÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ][\\wáčďéěíňóřšťúůýžÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ]" + PAP
          + "\\s)" + EOS);

  private static final Pattern abbrev2 = Pattern
      .compile("([^-\\wáčďéěíňóřšťúůýžÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ][\\wáčďéěíňóřšťúůýžÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ]" + P
          + ")" + EOS);

  private static final Pattern abbrev3 = Pattern
      .compile("(\\s[\\wáčďéěíňóřšťúůýžÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ]\\.\\s+)" + EOS);

  private static final Pattern abbrev4 = Pattern.compile("(\\.\\.\\. )" + EOS + "([\\p{Ll}])");
  private static final Pattern abbrev5 = Pattern.compile("(['\"]" + P + "['\"]\\s+)" + EOS);
  private static final Pattern abbrev6 = Pattern.compile("([\"']\\s*)" + EOS + "(\\s*[\\p{Ll}])");
  private static final Pattern abbrev7 = Pattern.compile("(\\s" + PAP + "\\s)" + EOS);
  // z.b. 3.10. (im Datum):
  private static final Pattern abbrev8 = Pattern.compile("(\\d{1,2}\\.\\d{1,2}\\.\\s+)" + EOS);
  private static final Pattern repair1 = Pattern.compile("('[\\wáčďéěíňóřšťúůýžÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ]"
      + P + ")(\\s)");
  private static final Pattern repair2 = Pattern.compile("(\\sno\\.)(\\s+)(?!\\d)");

  //  Czech abbreviations (ver. 0.2)

  // various titles
  private static final String TITLES = "Bc|BcA|Ing|Ing.arch|MUDr|MVDr|MgA|Mgr|JUDr|PhDr|" +
      "RNDr|PharmDr|ThLic|ThDr|Ph.D|Th.D|prof|doc|CSc|DrSc|dr. h. c|PaedDr|Dr|PhMr|DiS";

  // as a single regexp:
  private static final String ABBREVIATIONS = "abt|ad|a.i|aj|angl|anon|apod|atd|atp|aut|bd|biogr|" +
      "b.m|b.p|b.r|cca|cit|cizojaz|c.k|col|čes|čín|čj|ed|facs|fasc|fol|fot|franc|h.c|hist|hl|" +
      "hrsg|ibid|il|ind|inv.č|jap|jhdt|jv|koed|kol|korej|kl|krit|lat|lit|m.a|maď|mj|mp|násl|" +
      "např|nepubl|něm|no|nr|n.s|okr|odd|odp|obr|opr|orig|phil|pl|pokrač|pol|port|pozn|př.kr|" +
      "př.n.l|přel|přeprac|příl|pseud|pt|red|repr|resp|revid|rkp|roč|roz|rozš|samost|sect|" +
      "sest|seš|sign|sl|srv|stol|sv|šk|šk.ro|špan|tab|t.č|tis|tj|tř|tzv|univ|uspoř|vol|" +
      "vl.jm|vs|vyd|vyobr|zal|zejm|zkr|zprac|zvl|n.p"
      + "|" + TITLES;

  private Pattern paragraph;

  /**
   * Create a sentence tokenizer.
   */
  public CzechSentenceTokenizer() {
    setSingleLineBreaksMarksParagraph(false);
  }

  /**
   * @param lineBreakParagraphs if <code>true</code>, single lines breaks are assumed to end a paragraph,
   *  with <code>false</code>, only two ore more consecutive line breaks end a paragraph
   */
  @Override
  public final void setSingleLineBreaksMarksParagraph(final boolean lineBreakParagraphs) {
    if (lineBreakParagraphs)
      paragraph = paragraphByLineBreak;
    else
      paragraph = paragraphByTwoLineBreaks;
  }

  @Override
  public final List<String> tokenize(String s) {
    s = firstSentenceSplitting(s);
    s = removeFalseEndOfSentence(s);
    s = splitUnsplitStuff(s);
    final StringTokenizer stringTokenizer = 
      new StringTokenizer(s, EOS);
    List<String> l = new ArrayList<String>();
    while (stringTokenizer.hasMoreTokens()) {
      String sentence = stringTokenizer.nextToken();
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
  @Override
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

    // Don't split at abbreviations, treat them case insensitive
    //TODO: don't split at some abbreviations followed by uppercase
    //E.g., "Wojna rozpoczęła się w 1918 r. To była krwawa jatka"
    //should be split at "r."... But
    //"Ks. Jankowski jest analfabetą" shouldn't be split...
    //this requires a special list of abbrevs used before names etc.

    //removing the loop and using only one regexp - this is definitely much, much faster
    Pattern pattern = Pattern.compile("(?u)(\\b(" + ABBREVIATIONS + ")" + PAP + "\\s)" + EOS);
    s = pattern.matcher(s).replaceAll("$1");

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

    s = s.replaceAll("(\\d+\\.) " + EOS + "([\\p{L}&&[^\\p{Lu}]]+)", "$1 $2");

    // z.B. "Das hier ist ein(!) Satz."
    s = s.replaceAll("\\(([!?]+)\\) " + EOS, "($1) ");
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
    return s;
  }

}
