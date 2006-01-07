/*
 * Created on 23.12.2005
 */
package de.danielnaber.languagetool.tagging.xx;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.tagging.Tagger;

/**
 * A trivial tagger that does nothing than assign null
 * tags to words.
 * 
 * @author Daniel Naber
 */
public class DemoTagger implements Tagger {

  public List tag(List sentenceTokens) {
    List l = new ArrayList();
    int pos = 0;
    for (Iterator iter = sentenceTokens.iterator(); iter.hasNext();) {
      String word = (String) iter.next();
      // a real tagger would need to assign a POS tag
      // in the next line instead of null:
      l.add(new AnalyzedToken(word, null, pos));
      pos += word.length();
    }
    return l;
  }

  public Object createNullToken(String token, int startPos) {
    return new AnalyzedToken(token, null, startPos);
  }

}
