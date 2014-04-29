/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.WordData;
import org.languagetool.JLanguageTool;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public abstract class PosTagResolverTestBase {

  protected abstract boolean ignoreKnownProblems(WordData wd);
  
  protected void testDictionary(String path, PosTagResolver resolver) throws IOException {
    Dictionary dictionary = Dictionary.read(
            JLanguageTool.getDataBroker().getFromResourceDirAsUrl(path));
    DictionaryLookup dl = new DictionaryLookup(dictionary);
    for (WordData wd : dl) {
      String posTag = wd.getTag().toString();
      if (ignoreKnownProblems(wd)) {
        System.err.println("Ignoring: " + posTag + " for word '" + wd.getWord() + "'");
        continue;
      }
      try {
        resolver.resolvePOSTag(posTag);
      } catch (Exception e) {
        //System.err.println("Could not resolve '" + posTag + "' for word '" + wd.getWord() + "': " + e.getMessage());
        throw new RuntimeException("Could not resolve '" + posTag + "' for word '" + wd.getWord() + "'", e);
      }
    }
  }

  protected void assertTag(String input, String expected, PosTagResolver resolver) {
    List<TokenPoS> pos = resolver.resolvePOSTag(input);
    String[] parts = expected.split(", ");
    assertThat(pos.size(), is(1));
    for (String part : parts) {
      String[] keyVal = part.split("=");
      String expectedKey = keyVal[0];
      String expectedVal = keyVal[1];
      ValueSet values = pos.get(0).getValues(expectedKey);
      if (values == null) {
        throw new RuntimeException("No value found for expected key '" + expectedKey + "'");
      }
      List<String> sortedValues = new ArrayList<>(values.getValues());
      Collections.sort(sortedValues, new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
          return o1.compareTo(o2);
        }
      });
      assertThat("Failure for input '" + input + "', key '" + expectedKey + "'", StringTools.listToString(sortedValues, "|"), is(expectedVal));
    }
  }

}
