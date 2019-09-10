/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (www.danielnaber.de)
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
package org.languagetool;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.ConfusionPair;
import org.languagetool.rules.ConfusionSetLoader;
import org.languagetool.rules.ConfusionString;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Provide short (~ up to 30 characters) descriptions for words.
 * Used to display as an additional hint when there are several suggestions.
 * @since 4.5
 */
public class ShortDescriptionProvider {

  private final Map<String, String> wordToDesc;

  private static final LoadingCache<String, Map<String, String>> cache = CacheBuilder.newBuilder()
      //.maximumSize(0)
      .expireAfterAccess(30, TimeUnit.MINUTES)
      .build(new CacheLoader<String, Map<String, String>>() {
        @Override
        public Map<String, String> load(@NotNull String langCode) {
          Map<String, String> map = new HashMap<>();
          String path = "/" + langCode + "/confusion_sets.txt";
          ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
          if (dataBroker.resourceExists(path)) {
            loadConfusionSet(map, path, dataBroker);
          }
          return map;
        }
      });

  private static void loadConfusionSet(Map<String, String> map, String path, ResourceDataBroker dataBroker) {
    ConfusionSetLoader loader = new ConfusionSetLoader();
    try (InputStream confusionSetStream = dataBroker.getFromResourceDirAsStream(path)) {
      Map<String, List<ConfusionPair>> confusionSet = loader.loadConfusionPairs(confusionSetStream);
      for (List<ConfusionPair> confPairs : confusionSet.values()) {
        for (ConfusionPair confPair : confPairs) {
          List<ConfusionString> set = confPair.getTerms();
          for (ConfusionString confString : set) {
            if (confString.getDescription() != null) {
              map.put(confString.getString(), confString.getDescription());
              //System.out.println("#" + confString.getString() + " -> " + confString.getDescription());
            }
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  public ShortDescriptionProvider(Language lang) {
    wordToDesc = cache.getUnchecked(lang.getShortCode());
  }

  @Nullable
  public String getShortDescription(String word) {
    return wordToDesc.get(word);
  }
  
}
