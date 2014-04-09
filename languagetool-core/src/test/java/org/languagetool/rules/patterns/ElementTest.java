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

import junit.framework.TestCase;
import org.languagetool.AnalyzedToken;
import org.languagetool.JLanguageTool;

public class ElementTest extends TestCase {

  public void testSentenceStart() {
    final Element element = new Element("", false, false, false);
    element.setPosElement(JLanguageTool.SENTENCE_START_TAGNAME, false, false);
    assertTrue(element.isSentenceStart());
    element.setPosElement(JLanguageTool.SENTENCE_START_TAGNAME, false, true);
    assertFalse(element.isSentenceStart());
    element.setPosElement(JLanguageTool.SENTENCE_START_TAGNAME, true, false);
    assertTrue(element.isSentenceStart());
    element.setPosElement(JLanguageTool.SENTENCE_START_TAGNAME, true, true);
    assertFalse(element.isSentenceStart());

    //this should be false:
    final Element element2 = new Element("bla|blah", false, true, false);
    element2.setPosElement("foo", true, true);
    assertFalse(element2.isSentenceStart());
  }
  
  public void testUnknownTag() {
    final Element element = new Element("", false, false, false);
    element.setPosElement(Element.UNKNOWN_TAG, false, false);
    
    final Element element2 = new Element("", false, false, false);
    element2.setPosElement(Element.UNKNOWN_TAG, false, true);

    final Element element3 = new Element("", false, false, false);
    element3.setPosElement(Element.UNKNOWN_TAG+"|VBG", true, false);
    
    final Element element4 = new Element("", false, false, false);
    element4.setPosElement(Element.UNKNOWN_TAG+"|VBG", true, true);
    
    final Element element5 = new Element("\\p{Ll}+", false, true, false);
    element5.setPosElement(Element.UNKNOWN_TAG, false, false);        
    
    final AnalyzedToken an = new AnalyzedToken("schword", null, null);
    assertTrue(element.isMatched(an));
    assertFalse(element2.isMatched(an));
    assertTrue(element3.isMatched(an));
    assertFalse(element4.isMatched(an));
    assertTrue(element5.isMatched(an));
    
    // if the AnalyzedToken is in the set of readings that have
    //non-null tags...
    an.setNoPOSTag(false);
    assertFalse(element.isMatched(an));
    assertTrue(element2.isMatched(an));
    assertFalse(element3.isMatched(an));
    assertTrue(element4.isMatched(an));
    assertFalse(element5.isMatched(an));
    
    final AnalyzedToken anSentEnd = new AnalyzedToken("schword", JLanguageTool.SENTENCE_END_TAGNAME, null);
    assertTrue(element.isMatched(anSentEnd));
    assertFalse(element2.isMatched(anSentEnd));
    assertTrue(element3.isMatched(anSentEnd));
    assertFalse(element4.isMatched(anSentEnd));
    assertTrue(element5.isMatched(anSentEnd));
    
    final Element element6 = new Element("\\p{Ll}+", false, true, false);
    element6.setPosElement(JLanguageTool.SENTENCE_END_TAGNAME, false, false);
    assertTrue(element6.isMatched(anSentEnd));
    
    final Element element7 = new Element("\\p{Ll}+", false, true, false);
    element7.setPosElement(JLanguageTool.SENTENCE_END_TAGNAME+"|BLABLA", true, false);
    assertTrue(element7.isMatched(anSentEnd));
    
    // if the AnalyzedToken is in the set of readings that have
    //non-null tags...
    anSentEnd.setNoPOSTag(false);
    assertFalse(element.isMatched(anSentEnd));
    assertTrue(element2.isMatched(anSentEnd));
    assertFalse(element3.isMatched(anSentEnd));
    assertTrue(element4.isMatched(anSentEnd));
    assertFalse(element5.isMatched(anSentEnd));
    
    final AnalyzedToken anParaEnd = new AnalyzedToken("schword", JLanguageTool.PARAGRAPH_END_TAGNAME, null);
    assertTrue(element.isMatched(anParaEnd));
    assertFalse(element2.isMatched(anParaEnd));
    assertTrue(element3.isMatched(anParaEnd));
    assertFalse(element4.isMatched(anParaEnd));
    assertTrue(element5.isMatched(anParaEnd));
    
    // if the AnalyzedToken is in the set of readings that have
    //non-null tags...
    anParaEnd.setNoPOSTag(false);
    assertFalse(element.isMatched(anParaEnd));
    assertTrue(element2.isMatched(anParaEnd));
    assertFalse(element3.isMatched(anParaEnd));
    assertTrue(element4.isMatched(anParaEnd));
    assertFalse(element5.isMatched(anParaEnd));
    
    final AnalyzedToken anWithPOS = new AnalyzedToken("schword", "POS", null);
    assertFalse(element.isMatched(anWithPOS));
    assertTrue(element2.isMatched(anWithPOS));
    assertFalse(element3.isMatched(anWithPOS));
    assertTrue(element4.isMatched(anWithPOS)); 
    assertFalse(element5.isMatched(anWithPOS));
  }
  
}
