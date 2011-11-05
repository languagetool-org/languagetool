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
package org.languagetool.dev.index;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;

/**
 * A filter that index the tokens with POS tags.
 * 
 * @author Tao Lin
 */
public class LanguageToolFilter extends TokenFilter {

  private final JLanguageTool languageTool;

  private Iterator<AnalyzedTokenReadings> tokenIter;

  // stack for POS
  private final Stack<String> posStack;

  private final CharTermAttribute termAtt;

  private final OffsetAttribute offsetAtt;

  private final PositionIncrementAttribute posIncrAtt;

  private final TypeAttribute typeAtt;

  private AttributeSource.State current;

  public static final String POS_PREFIX = "_POS_";

  protected LanguageToolFilter(TokenStream input, JLanguageTool languageTool) {
    super(input);
    this.languageTool = languageTool;
    posStack = new Stack<String>();
    termAtt = addAttribute(CharTermAttribute.class);
    offsetAtt = addAttribute(OffsetAttribute.class);
    posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    typeAtt = addAttribute(TypeAttribute.class);
  }

  @Override
  public boolean incrementToken() throws IOException {

    if (posStack.size() > 0) {
      final String pop = posStack.pop();
      restoreState(current);
      termAtt.append(pop);
      posIncrAtt.setPositionIncrement(0);
      typeAtt.setType("pos");
      return true;
    }

    if (tokenIter == null || !tokenIter.hasNext()) {
      // there are no remaining tokens from the current sentence... are there more sentences?
      if (input.incrementToken()) {
        // a new sentence is available: process it.
        final AnalyzedSentence sentence = languageTool.getAnalyzedSentence(termAtt.toString());

        final List<AnalyzedTokenReadings> tokenBuffer = Arrays.asList(sentence.getTokens());
        tokenIter = tokenBuffer.iterator();
        /*
         * it should not be possible to have a sentence with 0 words, check just in case. returning
         * EOS isn't the best either, but it's the behavior of the original code.
         */
        if (!tokenIter.hasNext()) {
          return false;
        }
      } else {
        return false; // no more sentences, end of stream!
      }
    }

    // It must clear attributes, as it is creating new tokens.
    clearAttributes();
    final AnalyzedTokenReadings tr = tokenIter.next();
    AnalyzedToken at = tr.getAnalyzedToken(0);

    // add POS tag for sentence start.
    if (tr.isSentStart()) {
      typeAtt.setType("pos");
      termAtt.append(POS_PREFIX + tr.getAnalyzedToken(0).getPOSTag());
      return true;
    }

    // by pass the white spaces.
    if (tr.isWhitespace()) {
      return this.incrementToken();
    }

    offsetAtt.setOffset(tr.getStartPos(), tr.getStartPos() + at.getToken().length());

    for (int i = 0; i < tr.getReadingsLength(); i++) {
      at = tr.getAnalyzedToken(i);
      if (at.getPOSTag() != null) {
        posStack.push(POS_PREFIX + at.getPOSTag());
      }
    }

    current = captureState();
    termAtt.append(tr.getAnalyzedToken(0).getToken());

    return true;

  }
}
