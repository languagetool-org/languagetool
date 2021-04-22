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
 * A filter that indexes the tokens with POS tags.
 * 
 * @author Tao Lin
 */
public final class LanguageToolFilter extends TokenFilter {

  static final String POS_PREFIX = "_POS_";
  static final String LEMMA_PREFIX = "_LEMMA_";

  private final JLanguageTool languageTool;
  private final boolean toLowerCase;
  private final Stack<String> posStack;
  private final CharTermAttribute termAtt;
  private final OffsetAttribute offsetAtt;
  private final PositionIncrementAttribute posIncrAtt;
  private final TypeAttribute typeAtt;
  private final StringBuilder collectedInput = new StringBuilder();

  private AttributeSource.State current;
  private Iterator<AnalyzedTokenReadings> tokenIter;

  LanguageToolFilter(TokenStream input, JLanguageTool languageTool, boolean toLowerCase) {
    super(input);
    this.languageTool = languageTool;
    this.toLowerCase = toLowerCase;
    posStack = new Stack<>();
    termAtt = addAttribute(CharTermAttribute.class);
    offsetAtt = addAttribute(OffsetAttribute.class);
    posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    typeAtt = addAttribute(TypeAttribute.class);
  }

  @Override
  public boolean incrementToken() throws IOException {

    if (posStack.size() > 0) {
      String pop = posStack.pop();
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
        String sentenceStr = termAtt.toString();
        collectedInput.append(sentenceStr);
        if (sentenceStr.length() >= 255) {
          // Long sentences get split, so keep collecting data to avoid errors
          // later. See https://github.com/languagetool-org/languagetool/issues/364
          return true;
        } else {
          sentenceStr = collectedInput.toString();
          collectedInput.setLength(0);
        }
        AnalyzedSentence sentence = languageTool.getAnalyzedSentence(sentenceStr);
        List<AnalyzedTokenReadings> tokenBuffer = Arrays.asList(sentence.getTokens());
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
    AnalyzedTokenReadings tr = tokenIter.next();

    // add POS tag for sentence start.
    if (tr.isSentenceStart()) {
      // TODO: would be needed so negated tokens can match on something (see testNegatedMatchAtSentenceStart())
      // but breaks other cases:
      //termAtt.append("SENT_START");
      typeAtt.setType("pos");
      String posTag = tr.getAnalyzedToken(0).getPOSTag();
      String lemma = tr.getAnalyzedToken(0).getLemma();
      if (toLowerCase) {
        termAtt.append(POS_PREFIX.toLowerCase()).append(posTag.toLowerCase());
        if (lemma != null) {
          termAtt.append(LEMMA_PREFIX.toLowerCase()).append(lemma.toLowerCase());
        }
      } else {
        termAtt.append(POS_PREFIX).append(posTag);
        if (lemma != null) {
          termAtt.append(LEMMA_PREFIX).append(lemma);
        }
      }
      return true;
    }

    // by pass the white spaces.
    if (tr.isWhitespace()) {
      return this.incrementToken();
    }

    offsetAtt.setOffset(tr.getStartPos(), tr.getEndPos());

    for (AnalyzedToken token : tr) {
      if (token.getPOSTag() != null) {
        if (toLowerCase) {
          posStack.push(POS_PREFIX.toLowerCase() + token.getPOSTag().toLowerCase());
        } else {
          posStack.push(POS_PREFIX + token.getPOSTag());
        }
      }
      if (token.getLemma() != null) {
        if (toLowerCase) {
          posStack.push(LEMMA_PREFIX.toLowerCase() + token.getLemma().toLowerCase());
        } else {
          // chances are good this is the same for all loop iterations, store it anyway...
          posStack.push(LEMMA_PREFIX + token.getLemma());
        }
      }
    }

    current = captureState();
    if (toLowerCase) {
      termAtt.append(tr.getAnalyzedToken(0).getToken().toLowerCase());
    } else {
      termAtt.append(tr.getAnalyzedToken(0).getToken());
    }

    return true;

  }
}
