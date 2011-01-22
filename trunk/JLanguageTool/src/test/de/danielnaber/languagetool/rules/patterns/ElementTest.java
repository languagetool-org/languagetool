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

package de.danielnaber.languagetool.rules.patterns;

import de.danielnaber.languagetool.JLanguageTool;
import junit.framework.TestCase;

public class ElementTest extends TestCase {

  public void testSentenceStart() {
    Element element = new Element("", false, false, false);
    element.setPosElement(JLanguageTool.SENTENCE_START_TAGNAME, false, false);
    assertTrue(element.isSentStart());
    element.setPosElement(JLanguageTool.SENTENCE_START_TAGNAME, false, true);
    assertFalse(element.isSentStart());
    element.setPosElement(JLanguageTool.SENTENCE_START_TAGNAME, true, false);
    assertTrue(element.isSentStart());
    element.setPosElement(JLanguageTool.SENTENCE_START_TAGNAME, true, true);
    assertFalse(element.isSentStart());

    //this should be false:
    element = new Element("bla|blah", false, true, false);
    element.setPosElement("foo", true, true);
    assertFalse(element.isSentStart());
  }
}
