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
import java.util.Map;
import java.util.Set;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;

public class ConfusionSetLoaderTest {
  
  @Test
  public void testLoadWithStrictLimits() throws IOException {
    try (InputStream inputStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream("/yy/confusion_sets.txt")) {
      ConfusionSetLoader loader = new ConfusionSetLoader();
      Map<String, ConfusionSet> map = loader.loadConfusionSet(inputStream);
      assertTrue(map.size() == 2);

      assertThat(map.get("there").getFactor(), is(10));
      assertThat(map.get("their").getFactor(), is(10));

      Set<ConfusionString> there = map.get("there").getSet();
      assertTrue(getAsString(there).contains("there - example 1"));
      assertTrue(getAsString(there).contains("their - example 2"));

      Set<ConfusionString> their = map.get("their").getSet();
      assertTrue(getAsString(their).contains("there - example 1"));
      assertTrue(getAsString(their).contains("their - example 2"));
      assertFalse(getAsString(their).contains("comment"));
    }
  }

  private String getAsString(Set<ConfusionString> their) {
    StringBuilder sb = new StringBuilder();
    for (ConfusionString confusionString : their) {
      sb.append(confusionString.getString()).append(" - ");
      sb.append(confusionString.getDescription());
      sb.append(" ");
    }
    return sb.toString();
  }

}
