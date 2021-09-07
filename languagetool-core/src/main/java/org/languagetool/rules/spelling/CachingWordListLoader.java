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
package org.languagetool.rules.spelling;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.languagetool.JLanguageTool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Helper to load text files from classpath.
 * @since 3.3, public since 3.5
 */
public class CachingWordListLoader {

  // Speed up the server use case, where rules get initialized for every call.
  private static final LoadingCache<String, List<String>> cache = CacheBuilder.newBuilder()
      .expireAfterWrite(10, TimeUnit.MINUTES)
      .build(new CacheLoader<String, List<String>>() {
        @Override
        public List<String> load(@NotNull String fileInClassPath) throws IOException {
          List<String> result = new ArrayList<>();
          if (!JLanguageTool.getDataBroker().resourceExists(fileInClassPath)) {
            return result;
          }
          List<String> lines = JLanguageTool.getDataBroker().getFromResourceDirAsLines(fileInClassPath);
          for (String line : lines) {
            if (line.isEmpty() || line.startsWith("#")) {
              continue;
            }
            result.add(StringUtils.substringBefore(line.trim(), "#").trim());
          }
          return Collections.unmodifiableList(result);
        }
      });

  public List<String> loadWords(String filePath) {
    return cache.getUnchecked(filePath);
  }
}
