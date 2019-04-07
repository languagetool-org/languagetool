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
package org.languagetool.rules;

import org.junit.Test;
import org.languagetool.JLanguageTool;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("QuestionableName")
public class ConfusionSetLoaderTest {
  
  @Test
  public void testLoadWithStrictLimits() throws IOException {
    try (InputStream inputStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream("/yy/confusion_sets.txt")) {
      ConfusionSetLoader loader = new ConfusionSetLoader();
      Map<String, List<ConfusionPair>> map = loader.loadConfusionPairs(inputStream);
      assertThat(map.size(), is(10));

      assertThat(map.get("there").size(), is(1));
      assertThat(map.get("there").get(0).getFactor(), is(10L));

      assertThat(map.get("their").size(), is(1));
      assertThat(map.get("their").get(0).getFactor(), is(10L));
      
      assertThat(map.get("foo").size(), is(2));
      assertThat(map.get("foo").get(0).getFactor(), is(5L));
      assertThat(map.get("foo").get(1).getFactor(), is(8L));

      assertThat(map.get("goo").size(), is(2));
      assertThat(map.get("goo").get(0).getFactor(), is(11L));
      assertThat(map.get("goo").get(1).getFactor(), is(12L));
      assertThat(map.get("lol").size(), is(1));
      assertThat(map.get("something").size(), is(1));

      assertThat(map.get("bar").size(), is(1));
      assertThat(map.get("bar").get(0).getFactor(), is(5L));

      List<ConfusionString> there = map.get("there").get(0).getTerms();
      assertTrue(getAsString(there).contains("there - example 1"));
      assertTrue(getAsString(there).contains("their - example 2"));

      List<ConfusionString> their = map.get("their").get(0).getTerms();
      assertTrue(getAsString(their).contains("there - example 1"));
      assertTrue(getAsString(their).contains("their - example 2"));
      assertFalse(getAsString(their).contains("comment"));

      List<ConfusionString> foo = map.get("foo").get(0).getTerms();
      assertTrue(getAsString(foo).contains("foo"));
      List<ConfusionString> bar = map.get("foo").get(0).getTerms();
      assertTrue(getAsString(bar).contains("bar"));
      List<ConfusionString> baz = map.get("foo").get(1).getTerms();
      assertTrue(getAsString(baz).contains("baz"));
    }
  }

  private String getAsString(List<ConfusionString> confStrings) {
    StringBuilder sb = new StringBuilder();
    for (ConfusionString confusionString : confStrings) {
      sb.append(confusionString.getString()).append(" - ");
      sb.append(confusionString.getDescription());
      sb.append(" ");
    }
    return sb.toString();
  }

}
