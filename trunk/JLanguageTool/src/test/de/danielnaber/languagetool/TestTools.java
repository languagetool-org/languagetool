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
package de.danielnaber.languagetool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import junit.framework.TestCase;

import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;

/**
 * @author Daniel Naber
 */
public final class TestTools {
  
  private TestTools() {}

  public static ResourceBundle getEnglishMessages() {
    ResourceBundle messages = ResourceBundle.getBundle("de.danielnaber.languagetool.MessagesBundle",
        new Locale("en"));
    return messages;
  }

  public static void testSplit(String[] sentences, SentenceTokenizer stokenizer) {
    StringBuilder inputString = new StringBuilder();
    List<String> input = new ArrayList<String>();
    for (int i = 0; i < sentences.length; i++) {
      input.add(sentences[i]);
    }
    for (Iterator iter = input.iterator(); iter.hasNext();) {
      String s = (String) iter.next();
      inputString.append(s);
    }
    TestCase.assertEquals(input, stokenizer.tokenize(inputString.toString()));
  }
  
}
