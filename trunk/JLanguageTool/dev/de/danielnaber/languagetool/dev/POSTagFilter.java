/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.dev;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tagging.de.AnalyzedGermanTokenReadings;

/**
 * Filter that puts the words of a text, the base form, and word's POS tags at the 
 * same index position.
 * 
 * @author Daniel Naber
 */
class POSTagFilter extends TokenFilter {

  private static final String BASEFORM_PREFIX = "B_";
  private static final String TEXTFORM_PREFIX = "T_";
  
  private Stack<Token> stack = new Stack<Token>();
  private Tagger tagger = null;
  
  public POSTagFilter(TokenStream in, Tagger tagger) {
    super(in);
    this.tagger = tagger;
  }

  public final org.apache.lucene.analysis.Token next() throws java.io.IOException {

    if (stack.size() > 0) {
      //System.err.println("*"+stack.peek());
      return stack.pop();
    } else {
      Token t = input.next();
      if (t == null)
        return null;
      List<String> wordList = new ArrayList<String>();
      wordList.add(t.termText());
      List<AnalyzedTokenReadings> atr = tagger.tag(wordList);
      for (Iterator iter = atr.iterator(); iter.hasNext();) {
        AnalyzedGermanTokenReadings atrs = (AnalyzedGermanTokenReadings) iter.next();
        List<AnalyzedToken> ats = atrs.getReadings();
        for (Iterator iterator = ats.iterator(); iterator.hasNext();) {
          AnalyzedToken at = (AnalyzedToken) iterator.next();
          if (at.getPOSTag() != null) {
            //System.err.println(">>>>>"+at.getPOSTag());
            Token posToken = new Token(at.getPOSTag(), t.startOffset(), t.endOffset());
            posToken.setPositionIncrement(0);
            stack.push(posToken);
          }
          Set<String> indexLemmas = new HashSet<String>();
          if (at.getLemma() != null) {
            String lemma = at.getLemma().toLowerCase();
            if (!lemma.equalsIgnoreCase(t.termText()) && !indexLemmas.contains(lemma)) {
              Token posToken = new Token(BASEFORM_PREFIX + lemma, t.startOffset(), t.endOffset());
              posToken.setPositionIncrement(0);
              stack.push(posToken);
              indexLemmas.add(lemma);
            }
          }
        }
      }
      return new Token(TEXTFORM_PREFIX + t.termText().toLowerCase(), t.startOffset(), t.endOffset());
    }
  }
  
}
