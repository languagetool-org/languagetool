/* LanguageTool, a natural language style checker 
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.languagemodel;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Like {@link LuceneSingleIndexLanguageModel}, but can merge the results of
 * lookups in several independent indexes to one result.
 * @since 2.7
 */
public class LuceneLanguageModel extends BaseLanguageModel {

  private final List<LuceneSingleIndexLanguageModel> lms = new ArrayList<>();

  public static void validateDirectory(File topIndexDir) {
    File[] subDirs = getSubDirectoriesOrNull(topIndexDir);
    if (subDirs == null || subDirs.length == 0) {
      LuceneSingleIndexLanguageModel.validateDirectory(topIndexDir);
    }
  }

  @Nullable
  private static File[] getSubDirectoriesOrNull(File topIndexDir) {
    return topIndexDir.listFiles((file, name) -> name.matches("index-\\d+"));
  }

  /**
   * @param topIndexDir a directory which contains either:
   *                    1) sub directories called {@code 1grams}, {@code 2grams}, {@code 3grams},
   *                    which are Lucene indexes with ngram occurrences as created by
   *                    {@code org.languagetool.dev.FrequencyIndexCreator}
   *                    or 2) sub directories {@code index-1}, {@code index-2} etc that contain
   *                    the sub directories described under 1)
   */
  public LuceneLanguageModel(File topIndexDir)  {
    File[] subDirs = getSubDirectoriesOrNull(topIndexDir);
    if (subDirs != null && subDirs.length > 0) {
      System.out.println("Running in multi-index mode with " + subDirs.length + " indexes: " + topIndexDir);
      for (File subDir : subDirs) {
        lms.add(new LuceneSingleIndexLanguageModel(subDir));
      }
    } else {
      lms.add(new LuceneSingleIndexLanguageModel(topIndexDir));
    }
  }

  @Override
  public long getCount(List<String> tokens) {
    return lms.stream().mapToLong(lm -> lm.getCount(tokens)).sum();
  }

  @Override
  public long getCount(String token) {
    return getCount(Arrays.asList(token));
  }

  @Override
  public long getTotalTokenCount() {
    return lms.stream().mapToLong(lm -> lm.getTotalTokenCount()).sum();
  }

  @Override
  public void close() {
    lms.forEach(lm -> lm.close());
  }

  @Override
  public String toString() {
    return lms.toString();
  }

}
