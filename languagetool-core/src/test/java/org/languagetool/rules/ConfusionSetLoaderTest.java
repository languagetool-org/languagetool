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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Demo;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SuppressWarnings("QuestionableName")
public class ConfusionSetLoaderTest {
  
  @Test
  public void testLoadWithStrictLimits() throws IOException {
    try (InputStream inputStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream("/yy/confusion_sets.txt")) {
      ConfusionSetLoader loader = new ConfusionSetLoader(new Demo());
      Map<String, List<ConfusionPair>> map = loader.loadConfusionPairs(inputStream);
      assertThat(map.size(), is(15));

      assertThat(map.get("there").size(), is(2));
      assertThat(map.get("there").get(0).getFactor(), is(10L));

      assertThat(map.get("their").size(), is(2));
      assertThat(map.get("their").get(0).getFactor(), is(10L));
      
      assertThat(map.get("foo").size(), is(4));
      assertThat(map.get("foo").get(0).getFactor(), is(5L));
      assertThat(map.get("foo").get(1).getFactor(), is(5L));
      assertThat(map.get("foo").get(2).getFactor(), is(8L));
      assertThat(map.get("foo").get(3).getFactor(), is(8L));

      assertThat(map.get("goo").size(), is(4));
      assertThat(map.get("goo").get(0).getFactor(), is(11L));
      assertThat(map.get("goo").get(1).getFactor(), is(11L));
      assertThat(map.get("goo").get(2).getFactor(), is(12L));
      assertThat(map.get("goo").get(3).getFactor(), is(12L));
      assertThat(map.get("lol").size(), is(2));
      assertThat(map.get("something").size(), is(2));

      assertThat(map.get("bar").size(), is(2));
      assertThat(map.get("bar").get(0).getFactor(), is(5L));
      assertThat(map.get("bar").get(0).getTerms().toString(), is("[bar, foo]"));
      assertThat(map.get("bar").get(1).getFactor(), is(5L));
      assertThat(map.get("bar").get(1).getTerms().toString(), is("[foo, bar]"));

      assertThat(map.get("one").size(), is(1));
      assertThat(map.get("one").get(0).getFactor(), is(13L));
      assertThat(map.get("one").get(0).getTerms().toString(), is("[one, two]"));
      assertThat(map.get("one").get(0).getTerm1().toString(), is("one"));
      assertThat(map.get("one").get(0).getTerm2().toString(), is("two"));

      assertThat(map.get("three").size(), is(1));
      assertThat(map.get("three").get(0).getFactor(), is(14L));
      assertThat(map.get("three").get(0).getTerms().toString(), is("[three, four]"));
      assertThat(map.get("three").get(0).getTerm1().toString(), is("three"));
      assertThat(map.get("three").get(0).getTerm2().toString(), is("four"));
      assertThat(map.get("four").size(), is(2));
      assertThat(map.get("four").get(0).getFactor(), is(14L));
      assertThat(map.get("four").get(0).getTerms().toString(), is("[three, four]"));
      assertThat(map.get("four").get(0).getTerm1().toString(), is("three"));
      assertThat(map.get("four").get(0).getTerm2().toString(), is("four"));
      assertThat(map.get("four").get(1).getFactor(), is(15L));
      assertThat(map.get("four").get(1).getTerms().toString(), is("[four, five]"));
      assertThat(map.get("four").get(1).getTerm1().toString(), is("four"));
      assertThat(map.get("four").get(1).getTerm2().toString(), is("five"));

      List<ConfusionString> there = map.get("there").get(0).getTerms();
      Assertions.assertTrue(getAsString(there).contains("there - example 1"));
      Assertions.assertTrue(getAsString(there).contains("their - example 2"));

      List<ConfusionString> their = map.get("their").get(0).getTerms();
      Assertions.assertTrue(getAsString(their).contains("there - example 1"));
      Assertions.assertTrue(getAsString(their).contains("their - example 2"));
      Assertions.assertFalse(getAsString(their).contains("comment"));

      assertThat(map.get("foo").toString(), is("[bar -> foo, foo -> bar, baz -> foo, foo -> baz]"));
    }
  }

  private String getAsString(List<ConfusionString> confStrings) {
    StringBuilder sb = new StringBuilder();
    for (ConfusionString confusionString : confStrings) {
      sb.append(confusionString.getString()).append(" - ");
      sb.append(confusionString.getDescription());
      sb.append(' ');
    }
    return sb.toString();
  }

}
