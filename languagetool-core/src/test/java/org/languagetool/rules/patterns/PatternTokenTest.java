/* LanguageTool, a natural language style checker 
 * Copyright (C) 2009 Daniel Naber (http://www.danielnaber.de)
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

package org.languagetool.rules.patterns;

import org.junit.Test;
import org.languagetool.AnalyzedToken;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.languagetool.JLanguageTool.*;
import static org.languagetool.rules.patterns.PatternToken.UNKNOWN_TAG;

public class PatternTokenTest {

  @Test
  public void testSentenceStart() {
    PatternToken patternToken = new PatternToken("", false, false, false);
    patternToken.setPosToken(new PatternToken.PosToken(SENTENCE_START_TAGNAME, false, false));
    assertTrue(patternToken.isSentenceStart());
    patternToken.setPosToken(new PatternToken.PosToken(SENTENCE_START_TAGNAME, false, true));
    assertFalse(patternToken.isSentenceStart());
    patternToken.setPosToken(new PatternToken.PosToken(SENTENCE_START_TAGNAME, true, false));
    assertTrue(patternToken.isSentenceStart());
    patternToken.setPosToken(new PatternToken.PosToken(SENTENCE_START_TAGNAME, true, true));
    assertFalse(patternToken.isSentenceStart());

    PatternToken patternToken2 = new PatternToken("bla|blah", false, true, false);
    patternToken2.setPosToken(new PatternToken.PosToken("foo", true, true));
    assertFalse(patternToken2.isSentenceStart());
  }

  @Test
  public void testUnknownTag() {
    PatternToken patternToken = new PatternToken("", false, false, false);
    patternToken.setPosToken(new PatternToken.PosToken(UNKNOWN_TAG, false, false));
    
    PatternToken patternToken2 = new PatternToken("", false, false, false);
    patternToken2.setPosToken(new PatternToken.PosToken(UNKNOWN_TAG, false, true));

    PatternToken patternToken3 = new PatternToken("", false, false, false);
    patternToken3.setPosToken(new PatternToken.PosToken(UNKNOWN_TAG + "|VBG", true, false));
    
    PatternToken patternToken4 = new PatternToken("", false, false, false);
    patternToken4.setPosToken(new PatternToken.PosToken(UNKNOWN_TAG + "|VBG", true, true));
    
    PatternToken patternToken5 = new PatternToken("\\p{Ll}+", false, true, false);
    patternToken5.setPosToken(new PatternToken.PosToken(UNKNOWN_TAG, false, false));
    
    AnalyzedToken an = new AnalyzedToken("schword", null, null);
    assertTrue(patternToken.isMatched(an));
    assertFalse(patternToken2.isMatched(an));
    assertTrue(patternToken3.isMatched(an));
    assertFalse(patternToken4.isMatched(an));
    assertTrue(patternToken5.isMatched(an));
    
    // if the AnalyzedToken is in the set of readings that have
    //non-null tags...
    an.setNoPOSTag(false);
    assertFalse(patternToken.isMatched(an));
    assertTrue(patternToken2.isMatched(an));
    assertFalse(patternToken3.isMatched(an));
    assertTrue(patternToken4.isMatched(an));
    assertFalse(patternToken5.isMatched(an));
    
    AnalyzedToken anSentEnd = new AnalyzedToken("schword", SENTENCE_END_TAGNAME, null);
    assertTrue(patternToken.isMatched(anSentEnd));
    assertFalse(patternToken2.isMatched(anSentEnd));
    assertTrue(patternToken3.isMatched(anSentEnd));
    assertFalse(patternToken4.isMatched(anSentEnd));
    assertTrue(patternToken5.isMatched(anSentEnd));
    
    PatternToken patternToken6 = new PatternToken("\\p{Ll}+", false, true, false);
    patternToken6.setPosToken(new PatternToken.PosToken(SENTENCE_END_TAGNAME, false, false));
    assertTrue(patternToken6.isMatched(anSentEnd));
    
    PatternToken patternToken7 = new PatternToken("\\p{Ll}+", false, true, false);
    patternToken7.setPosToken(new PatternToken.PosToken(SENTENCE_END_TAGNAME + "|BLABLA", true, false));
    assertTrue(patternToken7.isMatched(anSentEnd));
    
    // if the AnalyzedToken is in the set of readings that have
    //non-null tags...
    anSentEnd.setNoPOSTag(false);
    assertFalse(patternToken.isMatched(anSentEnd));
    assertTrue(patternToken2.isMatched(anSentEnd));
    assertFalse(patternToken3.isMatched(anSentEnd));
    assertTrue(patternToken4.isMatched(anSentEnd));
    assertFalse(patternToken5.isMatched(anSentEnd));
    
    AnalyzedToken anParaEnd = new AnalyzedToken("schword", PARAGRAPH_END_TAGNAME, null);
    assertTrue(patternToken.isMatched(anParaEnd));
    assertFalse(patternToken2.isMatched(anParaEnd));
    assertTrue(patternToken3.isMatched(anParaEnd));
    assertFalse(patternToken4.isMatched(anParaEnd));
    assertTrue(patternToken5.isMatched(anParaEnd));
    
    // if the AnalyzedToken is in the set of readings that have
    //non-null tags...
    anParaEnd.setNoPOSTag(false);
    assertFalse(patternToken.isMatched(anParaEnd));
    assertTrue(patternToken2.isMatched(anParaEnd));
    assertFalse(patternToken3.isMatched(anParaEnd));
    assertTrue(patternToken4.isMatched(anParaEnd));
    assertFalse(patternToken5.isMatched(anParaEnd));
    
    AnalyzedToken anWithPOS = new AnalyzedToken("schword", "POS", null);
    assertFalse(patternToken.isMatched(anWithPOS));
    assertTrue(patternToken2.isMatched(anWithPOS));
    assertFalse(patternToken3.isMatched(anWithPOS));
    assertTrue(patternToken4.isMatched(anWithPOS));
    assertFalse(patternToken5.isMatched(anWithPOS));
  }
  
}
