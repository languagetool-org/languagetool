/* LanguageTool, a natural language style checker 
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.ngrams;

import org.languagetool.languagemodel.LuceneSingleIndexLanguageModel;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeLanguageModel extends LuceneSingleIndexLanguageModel {

  static Map<String,Integer> map = new HashMap<>();

  public FakeLanguageModel(Map<String,Integer> map) {
    super(3);
    FakeLanguageModel.map = map;
  }
  
  public FakeLanguageModel() {
    super(3);
    // for "Their are new ideas to explore":
    map.put("There are", 10);
    map.put("There are new", 5);
    map.put("Their are", 2);
    map.put("Their are new", 1);
    // for "Why is there car broken again?"
    map.put("Why is", 50);
    map.put("Why is there", 5);
    map.put("Why is their", 5);
    map.put("their car", 11);
    map.put("their car broken", 2);
    // for um/im (German):
    map.put("_START_ Um", 10);
    map.put("_START_ Im", 1);
    map.put("_START_ Um dabei", 20);
    map.put("_START_ Im dabei", 0);
  }

  @Override
  public void doValidateDirectory(File topIndexDir) {
  }

  @Override
  public long getCount(List<String> tokens) {
    Integer count = map.get(String.join(" ", tokens));
    return count == null ? 0 : count;
  }

  @Override
  public long getCount(String token1) {
    return getCount(Arrays.asList(token1));
  }

  @Override
  public long getTotalTokenCount() {
    int sum = 0;
    for (int val : map.values()) {
      sum += val;
    }
    return sum;
  }

  @Override
  public void close() {}

}
