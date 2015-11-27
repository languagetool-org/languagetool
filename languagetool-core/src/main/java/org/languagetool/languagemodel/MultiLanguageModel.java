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

import java.util.List;

/**
 * Combines the results of several {@link LanguageModel}s.
 * @since 3.2
 */
public class MultiLanguageModel implements LanguageModel {

  private final List<LanguageModel> lms;

  public MultiLanguageModel(List<LanguageModel> lms) {
    if (lms.size() == 0) {
      throw new IllegalArgumentException("List of language models is empty");
    }
    this.lms = lms;
  }

  @Override
  public long getCount(String token) {
    return lms.stream().mapToLong(lm -> lm.getCount(token)).sum();
  }

  @Override
  public long getCount(List<String> tokens) {
    return lms.stream().mapToLong(lm -> lm.getCount(tokens)).sum();
  }

  @Override
  public long getCount(String token1, String token2) {
    return lms.stream().mapToLong(lm -> lm.getCount(token1, token2)).sum();
  }

  @Override
  public long getCount(String token1, String token2, String token3) {
    return lms.stream().mapToLong(lm -> lm.getCount(token1, token2, token3)).sum();
  }

  @Override
  public long getTotalTokenCount() {
    return lms.stream().mapToLong(LanguageModel::getTotalTokenCount).sum();
  }

  @Override
  public void close() {
    lms.stream().forEach(LanguageModel::close);
  }

}
