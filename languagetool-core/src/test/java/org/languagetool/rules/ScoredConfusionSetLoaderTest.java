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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("QuestionableName")
public class ScoredConfusionSetLoaderTest {
  
  @Test
  public void testLoadWithStrictLimits() throws IOException {
    try (InputStream inputStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream("/yy/neuralnetwork_confusion_sets.txt")) {
      List<ScoredConfusionSet> list = ScoredConfusionSetLoader.loadConfusionSet(inputStream);
      assertThat(list.size(), is(6));

      assertThat(list.get(0).getConfusionWords().size(), is(2));

      assertThat(list.get(0).getScore(), is(5.0f));
      assertThat(list.get(1).getScore(), is(1.1f));
      assertThat(list.get(2).getScore(), is(0.5f));
      assertThat(list.get(3).getScore(), is(0.8f));
      assertThat(list.get(4).getScore(), is(1.2f));
      assertThat(list.get(5).getScore(), is(1.0f));

      assertThat(list.get(5).getConfusionTokens().get(0), is("im"));

      List<ConfusionString> their = list.get(0).getConfusionWords();
      assertTrue(getAsString(their).contains("there - example 1"));
      assertTrue(getAsString(their).contains("their - example 2"));
      assertFalse(getAsString(their).contains("comment"));
    }
  }

  private String getAsString(List<ConfusionString> their) {
    StringBuilder sb = new StringBuilder();
    for (ConfusionString confusionString : their) {
      sb.append(confusionString.getString()).append(" - ");
      sb.append(confusionString.getDescription());
      sb.append(" ");
    }
    return sb.toString();
  }

}
