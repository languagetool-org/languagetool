package de.danielnaber.languagetool.tokenizers.fr;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import de.danielnaber.languagetool.tokenizers.Tokenizer;

/**
 * Tokenizes a sentence into words. Punctuation and whitespace gets its own token.
 * 
 * @author Marcin Miłkowski
 */
public class FrenchWordTokenizer implements Tokenizer {

  public FrenchWordTokenizer() {
  }
  
  public List<String> tokenize(final String text) {
    List<String> l = new ArrayList<String>();
    //French quotation marks
    StringTokenizer st = new StringTokenizer(text, " \u00a0,.;()!?:\"'„”«»\\/\n", true);
    while (st.hasMoreElements()) {
      l.add(st.nextToken());
    }
    return l;
  }
  
}
