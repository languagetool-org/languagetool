package de.danielnaber.languagetool.tokenizers.pl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;

/**
 * Tokenizes Polish text into sentences by looking for typical end-of-sentence markers,
 * but considering exceptions (e.g. abbreviations).
 * 
 * @author Marcin Milkowski
*/

public class PolishSentenceTokenizer extends SentenceTokenizer {

  
  /** End of sentence marker.
   */
  private static final String EOS = "\0";
  // private final static String EOS = "#"; // for testing only
  
  /** Punctuation.
   * 
   */
  private static final String P = "[\\.!?…]"; 
  
  /** After punctuation.
  * 
  */
  private static final String AP = "(?:'|«|\"|”|\\)|\\]|\\})?"; 
  private static final String PAP = P + AP;

  // Check out the private methods for comments and examples about these
  // regular expressions:

  private Pattern paragraph = null;
  private static final Pattern paragraphByTwoLineBreaks = Pattern.compile("(\\n\\s*\\n[\\t]*)");
  private static final Pattern paragraphByLineBreak = Pattern.compile("(\\n[\\t]*)");
  
  // add unbreakable field, for example footnote, if it's at the end of the sentence
  private static final Pattern punctWhitespace = Pattern.compile("(" + PAP + "(\u0002)?\\s)");
  // \p{Lu} = uppercase, with obeying Unicode (\p{Upper} is just US-ASCII!):
  private static final Pattern punctUpperLower = Pattern.compile("(" + PAP
      + ")([\\p{Lu}][^\\p{Lu}.])");
  private static final Pattern letterPunct = Pattern.compile("(\\s[\\p{L}]" + P + ")");
  private static final Pattern abbrev1 = Pattern.compile("([^-\\p{L}\\p{S}”][\\p{L}]" + PAP + "\\s)" + EOS);
  private static final Pattern abbrev2 = Pattern.compile("([^-\\p{L}\\p{S}][\\p{L}]" + P + ")" + EOS);
  //** Lookahead regexp excludes some possible abbrevs here
  private static final Pattern abbrev3 = Pattern.compile("(\\s(?![rwn])[\\p{L}]\\.\\s+)" + EOS +"(\\p{Ll}\\p{Ll}|\\p{Lu}[\\p{Punct}\\p{Lu}])");
  private static final Pattern abbrev4 = Pattern.compile("(\\.\\.\\. )" + EOS + "([\\p{Ll}])");
  private static final Pattern abbrev5 = Pattern.compile("(['\"]" + P + "['\"]\\s+)" + EOS);
  private static final Pattern abbrev6 = Pattern.compile("([\"”']\\s*)" + EOS + "(\\s*[\\p{Ll}])");
  private static final Pattern abbrev7 = Pattern.compile("(\\s" + PAP + "\\s)" + EOS);
  // z.b. 3.10. (im Datum):
  private static final Pattern abbrev8 = Pattern.compile("(\\d{1,2}\\.\\d{1,2}\\.\\s+)" + EOS);  
  
  private static final Pattern ELLIPSIS = Pattern.compile("([\\[]*(\\.\\.\\.|…)[\\]]*\\s)" + EOS + "(\\p{Ll})");
  
  /** Polish abbreviations as a single regexp. **/
  private static final String ABBREVLIST = "adw|afr|akad|al|am|amer|arch|art|artyst|astr|austr|" +
        "bałt|bdb|bł|bm|br|bryt|centr|ces|chem|chiń|chir|c.k|c.o|cyg|cyw|cyt|" +
        "czes|czw|cd|czyt|ćw|ćwicz|" +
        "daw|dcn|dekl|demokr|det|diec|dł|dn|dop|dost|dosł|h.c|ds|dst|duszp|dypl|egz|ekol|ekon|" +
        "elektr|em|etc|ew|fab|farm|fot|fr|gat|gastr|geogr|geol|gimn|głęb|gm|godz|górn|gosp|gr|gram|" +
        "hist|hiszp|hr|hot|id|in|im|iron|jn|kard|kat|katol|k.k|kk|klas|kol|k.p.a|kpc|k.p.c|kpt|kr|k.r|" +
        "krak|k.r.o|kryt|kult|laic|łac|niem|woj|np|pol|m.in|itd|itp|pt|cdn|jw|" +
        "nb|rys|tj|tzw|tzn|zob|ang|ul|pl|al|k|n|ok|tys|ww|ur|zm|żyd|żarg|żyw|wył|" +
        "up|tow|o|zn|zew|zewn|zdr|zazw|zast|zaw|zał|zal|zam|zak|zakł|zagr|zach|"+
        "adw|lek|mec|doc|dyr|inż|mgr|dr|red|prof|hab|ks|gen|por|przyp";
  
  /** Abbreviations which can occur at the end of sentence. **/
  private static final String ENDABBREVLIST 
    = "proc|r|itd|itp|cdn|jw|n.e|w|nn|n"
    // needed for SKROTY_BEZ_KROPKI rule
    + "dl|ml|dag|ha|cm|dm|m|zł|gr|kg|mln|mld|min|npl|pkt|pg|tg|cos|cosec|sec|sin|rkm|wg"; 
  
  private static final Pattern ABREVLIST_PATTERN 
    = Pattern.compile("(?iu)(\\b(" + ABBREVLIST + ")" + PAP + "\\s)" + EOS);
  
  private static final Pattern ENDABREVLIST_PATTERN 
    = Pattern.compile("(?iu)(\\b(" + ENDABBREVLIST + ")"+ PAP + "\\s)" + EOS +"(\\p{Ll})");
  
  /**
   * Create a sentence tokenizer.
   */
  public PolishSentenceTokenizer() {
    setSingleLineBreaksMarksParagraph(false);
  }

  /**
   * @param lineBreakParagraphs if <code>true</code>, single lines breaks are assumed to end a paragraph,
   *  with <code>false</code>, only two ore more consecutive line breaks end a paragraph
   */
  @Override
  public final void setSingleLineBreaksMarksParagraph(final boolean lineBreakParagraphs) {
    if (lineBreakParagraphs) {
      paragraph = paragraphByLineBreak;
    } else {
      paragraph = paragraphByTwoLineBreaks;
    }
  }

  @Override
  public final List<String> tokenize(String s) {
    s = firstSentenceSplitting(s);
    s = removeFalseEndOfSentence(s);
    //s = splitUnsplitStuff(s);
    final StringTokenizer stringTokenizer = new StringTokenizer(s, EOS);
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
  private String removeFalseEndOfSentence(String s) {
    // Don't split at e.g. "U. S. A.":
    s = abbrev1.matcher(s).replaceAll("$1");
    // Don't split at e.g. "U.S.A.":    
    s = abbrev2.matcher(s).replaceAll("$1");
    // Don't split after a white-space followed by a single letter followed
    // by a dot followed by another whitespace.
    // e.g. " p. "
    s = abbrev3.matcher(s).replaceAll("$1$2");
    // Don't split at "bla bla... yada yada" 
    s = abbrev4.matcher(s).replaceAll("$1$2");
    // Don't split [.?!] when the're quoted:
    s = abbrev5.matcher(s).replaceAll("$1");

    // Don't split at abbreviations, treat them case insensitive
    s = ABREVLIST_PATTERN.matcher(s).replaceAll("$1");

    //a special list of abbrevs used at the end of sentence
    s = ENDABREVLIST_PATTERN.matcher(s).replaceAll("$1$3");
    
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
   
    s = ELLIPSIS.matcher(s).replaceAll("$1$3");
    
    s = s.replaceAll("(\\d+\\.) " + EOS + "([\\p{L}&&[^\\p{Lu}]]+)", "$1 $2");

    // np. "Uczeń napisał: "Szfecja (sic!) jest wielkim krajem".
      s = s.replaceAll("\\(((sic)?[!?]+)\\) " + EOS, "($1) ");
    return s;
  } 

}
