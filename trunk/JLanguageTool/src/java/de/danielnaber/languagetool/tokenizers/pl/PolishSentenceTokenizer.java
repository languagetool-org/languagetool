package de.danielnaber.languagetool.tokenizers.pl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import de.danielnaber.languagetool.tokenizers.*;

/**
 * Tokenizes Polish text into sentences by looking for typical end-of-sentence markers,
 * but considering exceptions (e.g. abbreviations).
 * 
 * @author Marcin Milkowski
*/

public class PolishSentenceTokenizer extends SentenceTokenizer {

  // end of sentence marker:
  private final static String EOS = "\0";
  // private final static String EOS = "#"; // for testing only
  private final static String P = "[\\.!?…]"; // PUNCTUATION
  private final static String AP = "(?:'|«|\"|”|\\)|\\]|\\})?"; // AFTER PUNCTUATION
  private final static String PAP = P + AP;

  // Check out the private methods for comments and examples about these
  // regular expressions:

  private Pattern paragraph = null;
  private static final Pattern paragraphByTwoLineBreaks = Pattern.compile("(\\n\\s*\\n)");
  private static final Pattern paragraphByLineBreak = Pattern.compile("(\\n)");
  
  private static final Pattern punctWhitespace = Pattern.compile("(" + PAP + "\\s)");
  // \p{Lu} = uppercase, with obeying Unicode (\p{Upper} is just US-ASCII!):
  private static final Pattern punctUpperLower = Pattern.compile("(" + PAP
      + ")([\\p{Lu}][^\\p{Lu}.])");
  private static final Pattern letterPunct = Pattern.compile("(\\s[\\wąćęłńóśźżĄĆĘŁŃÓŚŹŻ]" + P + ")");
  private static final Pattern abbrev1 = Pattern.compile("([^-\\wąćęłńóśźżĄĆĘŁŃÓŚŹŻ][\\wąćęłńóśźżĄĆĘŁŃÓŚŹŻ]" + PAP + "\\s)" + EOS);
  private static final Pattern abbrev2 = Pattern.compile("([^-\\wąćęłńóśźżĄĆĘŁŃÓŚŹŻ][\\wąćęłńóśźżĄĆĘŁŃÓŚŹŻ]" + P + ")" + EOS);
  private static final Pattern abbrev3 = Pattern.compile("(\\s[\\wąćęłńóśźżĄĆĘŁŃÓŚŹŻ]\\.\\s+)" + EOS);
  private static final Pattern abbrev4 = Pattern.compile("(\\.\\.\\. )" + EOS + "([\\p{Ll}])");
  private static final Pattern abbrev5 = Pattern.compile("(['\"]" + P + "['\"]\\s+)" + EOS);
  private static final Pattern abbrev6 = Pattern.compile("([\"']\\s*)" + EOS + "(\\s*[\\p{Ll}])");
  private static final Pattern abbrev7 = Pattern.compile("(\\s" + PAP + "\\s)" + EOS);
  // z.b. 3.10. (im Datum):
  private static final Pattern abbrev8 = Pattern.compile("(\\d{1,2}\\.\\d{1,2}\\.\\s+)" + EOS);
  private static final Pattern repair1 = Pattern.compile("('[\\wąćęłńóśźżĄĆĘŁŃÓŚŹŻ]" + P + ")(\\s)");
  private static final Pattern repair2 = Pattern.compile("(\\sno\\.)(\\s+)(?!\\d)");
  
  // Polish:
  private static final String[] ABBREV_LIST = {
      //Polish:
      "adw", "afr", "akad", "am", "amer", "arch", "art", "artyst",
      "astr", "austr", "bałt", "bdb", "bł", "bm", "br", "bryt", 
      "centr", "ces", "chem", "chiń", "chir", "c.k","c.o", "cyg",
      "cyw", "czes", "czw", "cd", "czyt", "ćw", "ćwicz", "daw",
      "dcn", "dekl", "demokr", "det", "diec", "dł", "dn", "doc",
      "dop", "dost", "dosł", "h.c", "ds", "dst", "duszp", "dypl",
      "egz", "ekol", "ekon", "elektr", "em", "etc", "ew", "fab",
      "farm", "fot", "fr", "gat", "gastr", "geogr", "geol", "gimn",
      "głęb", "gm", "godz", "górn", "gosp", "gr", "gram", "hist", "hiszp",
      "hr", "hot", "id", "in", "im", "iron", "jn", "kard", "kat",
      "katol", "k.k", "kk", "klas", "kol", "k.p.a", "kpc", "k.p.c",
      "kpt", "kr", "k.r", "krak", "k.r.o", "kryt", "kult", "laic",
      "łac", "np", "p.n.e", "m.in", "itd", "itp", "pt","cdn", "dyr","hab",
      "inż","jw", "lek","n.e","nb","rys", "tj", "tzw", "tzn", "zob" , "ang",
      "ul", "pl", "al", "prof", "gen", "k", "n", "ks", "ok", "tys", "r", "proc",
      "ww", "ur", "zm"
  };

  private static Set<String> abbreviations = new HashSet<String>();
  private StringTokenizer stringTokenizer = null;

  /**
   * Create a sentence tokenizer.
   */
  public PolishSentenceTokenizer() {
    for (int i = 0; i < ABBREV_LIST.length; i++) {
      abbreviations.add(ABBREV_LIST[i]);
    }
    setSingleLineBreaksMarksParagraph(false);
  }

  /**
   * @param lineBreakParagraphs if <code>true</code>, single lines breaks are assumed to end a paragraph,
   *  with <code>false</code>, only two ore more consecutive line breaks end a paragraph
   */
  public void setSingleLineBreaksMarksParagraph(boolean lineBreakParagraphs) {
    if (lineBreakParagraphs)
      paragraph = paragraphByLineBreak;
    else
      paragraph = paragraphByTwoLineBreaks;
  }

  public List<String> tokenize(String s) {
    s = firstSentenceSplitting(s);
    s = removeFalseEndOfSentence(s);
    s = splitUnsplitStuff(s);
    stringTokenizer = new StringTokenizer(s, EOS);
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
  private String removeFalseEndOfSentence(String s) {
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
    for (String abbrev : abbreviations) {
      Pattern pattern = Pattern.compile("(?u)(\\b" + abbrev + PAP + "\\s)" + EOS);
      s = pattern.matcher(s).replaceAll("$1");
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
