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

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.languagetool.AnalyzedToken;

import static org.languagetool.JLanguageTool.*;
import static org.languagetool.rules.patterns.PatternToken.UNKNOWN_TAG;

public class PatternTokenTest {

  @Test
  public void testSentenceStart() {
    PatternToken patternToken = new PatternToken("", false, false, false);
    patternToken.setPosToken(new PatternToken.PosToken(SENTENCE_START_TAGNAME, false, false));
    Assertions.assertTrue(patternToken.isSentenceStart());
    patternToken.setPosToken(new PatternToken.PosToken(SENTENCE_START_TAGNAME, false, true));
    Assertions.assertFalse(patternToken.isSentenceStart());
    patternToken.setPosToken(new PatternToken.PosToken(SENTENCE_START_TAGNAME, true, false));
    Assertions.assertTrue(patternToken.isSentenceStart());
    patternToken.setPosToken(new PatternToken.PosToken(SENTENCE_START_TAGNAME, true, true));
    Assertions.assertFalse(patternToken.isSentenceStart());

    PatternToken patternToken2 = new PatternToken("bla|blah", false, true, false);
    patternToken2.setPosToken(new PatternToken.PosToken("foo", true, true));
    Assertions.assertFalse(patternToken2.isSentenceStart());
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
    Assertions.assertTrue(patternToken.isMatched(an));
    Assertions.assertFalse(patternToken2.isMatched(an));
    Assertions.assertTrue(patternToken3.isMatched(an));
    Assertions.assertFalse(patternToken4.isMatched(an));
    Assertions.assertTrue(patternToken5.isMatched(an));

    // if the AnalyzedToken is in the set of readings that have
    //non-null tags...
    an.setNoPOSTag(false);
    Assertions.assertFalse(patternToken.isMatched(an));
    Assertions.assertTrue(patternToken2.isMatched(an));
    Assertions.assertFalse(patternToken3.isMatched(an));
    Assertions.assertTrue(patternToken4.isMatched(an));
    Assertions.assertFalse(patternToken5.isMatched(an));

    AnalyzedToken anSentEnd = new AnalyzedToken("schword", SENTENCE_END_TAGNAME, null);
    Assertions.assertTrue(patternToken.isMatched(anSentEnd));
    Assertions.assertFalse(patternToken2.isMatched(anSentEnd));
    Assertions.assertTrue(patternToken3.isMatched(anSentEnd));
    Assertions.assertFalse(patternToken4.isMatched(anSentEnd));
    Assertions.assertTrue(patternToken5.isMatched(anSentEnd));

    PatternToken patternToken6 = new PatternToken("\\p{Ll}+", false, true, false);
    patternToken6.setPosToken(new PatternToken.PosToken(SENTENCE_END_TAGNAME, false, false));
    Assertions.assertTrue(patternToken6.isMatched(anSentEnd));

    PatternToken patternToken7 = new PatternToken("\\p{Ll}+", false, true, false);
    patternToken7.setPosToken(new PatternToken.PosToken(SENTENCE_END_TAGNAME + "|BLABLA", true, false));
    Assertions.assertTrue(patternToken7.isMatched(anSentEnd));

    // if the AnalyzedToken is in the set of readings that have
    //non-null tags...
    anSentEnd.setNoPOSTag(false);
    Assertions.assertFalse(patternToken.isMatched(anSentEnd));
    Assertions.assertTrue(patternToken2.isMatched(anSentEnd));
    Assertions.assertFalse(patternToken3.isMatched(anSentEnd));
    Assertions.assertTrue(patternToken4.isMatched(anSentEnd));
    Assertions.assertFalse(patternToken5.isMatched(anSentEnd));

    AnalyzedToken anParaEnd = new AnalyzedToken("schword", PARAGRAPH_END_TAGNAME, null);
    Assertions.assertTrue(patternToken.isMatched(anParaEnd));
    Assertions.assertFalse(patternToken2.isMatched(anParaEnd));
    Assertions.assertTrue(patternToken3.isMatched(anParaEnd));
    Assertions.assertFalse(patternToken4.isMatched(anParaEnd));
    Assertions.assertTrue(patternToken5.isMatched(anParaEnd));

    // if the AnalyzedToken is in the set of readings that have
    //non-null tags...
    anParaEnd.setNoPOSTag(false);
    Assertions.assertFalse(patternToken.isMatched(anParaEnd));
    Assertions.assertTrue(patternToken2.isMatched(anParaEnd));
    Assertions.assertFalse(patternToken3.isMatched(anParaEnd));
    Assertions.assertTrue(patternToken4.isMatched(anParaEnd));
    Assertions.assertFalse(patternToken5.isMatched(anParaEnd));

    AnalyzedToken anWithPOS = new AnalyzedToken("schword", "POS", null);
    Assertions.assertFalse(patternToken.isMatched(anWithPOS));
    Assertions.assertTrue(patternToken2.isMatched(anWithPOS));
    Assertions.assertFalse(patternToken3.isMatched(anWithPOS));
    Assertions.assertTrue(patternToken4.isMatched(anWithPOS));
    Assertions.assertFalse(patternToken5.isMatched(anWithPOS));
  }

  @Test
  public void testNegation() {

    PatternToken token = new PatternTokenBuilder().tokenRegex("an?|the").negate().build();
    Assertions.assertTrue(token.getNegation());

    token = new PatternTokenBuilder().tokenRegex("an?|the").build();
    Assertions.assertFalse(token.getNegation());

    token = new PatternTokenBuilder().pos("NNS").negate().build();
    Assertions.assertTrue(token.getNegation());

    token = new PatternTokenBuilder().pos("NNS").build();
    Assertions.assertFalse(token.getNegation());

    token = new PatternTokenBuilder().token("text").negate().build();
    Assertions.assertTrue(token.getNegation());

    token = new PatternTokenBuilder().token("text").build();
    Assertions.assertFalse(token.getNegation());

  }

  @Test
  public void testFormHints() {
    PatternToken token = new PatternTokenBuilder().tokenRegex("an?|the|th[eo]se").build();
    Assertions.assertEquals(Sets.newHashSet("a", "an", "the", "these", "those"), token.calcFormHints());

    token = new PatternTokenBuilder().token("foo").build();
    Assertions.assertEquals(Sets.newHashSet("foo"), token.calcFormHints());

    token = new PatternTokenBuilder().tokenRegex("(foo)?.*").build();
    Assertions.assertNull(token.calcFormHints());

    token = new PatternTokenBuilder().csTokenRegex("a|b").build();
    Assertions.assertEquals(Sets.newHashSet("a", "b"), token.calcFormHints());

    token = new PatternTokenBuilder().token("a").min(0).build();
    Assertions.assertNull(token.calcFormHints());

    token = new PatternTokenBuilder().tokenRegex("an|the").build();
    token.setOrGroupElement(new PatternTokenBuilder().tokenRegex("foo|bar").build());
    Assertions.assertEquals(Sets.newHashSet("an", "the", "foo", "bar"), token.calcFormHints());
    token.setOrGroupElement(new PatternTokenBuilder().tokenRegex("foo.*|bar").build());
    Assertions.assertNull(token.calcFormHints());

    token = new PatternTokenBuilder().tokenRegex("an|the").build();
    token.setAndGroupElement(new PatternTokenBuilder().tokenRegex("foo|an").build());
    Assertions.assertEquals(Sets.newHashSet("an"), token.calcFormHints());
  }
}
