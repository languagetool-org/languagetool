/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.wikipedia;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Alternately returns sentences from different sentence sources.
 * @since 2.4
 */
class MixingSentenceSource extends SentenceSource {

  private final List<SentenceSource> sources;
  
  private int count;

  MixingSentenceSource(List<SentenceSource> sources) {
    this.sources = sources;
  }

  @Override
  public boolean hasNext() {
    for (SentenceSource source : sources) {
      if (source.hasNext()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Sentence next() {
    SentenceSource sentenceSource = sources.get(count % sources.size());
    while (!sentenceSource.hasNext()) {
      sources.remove(sentenceSource);
      if (sources.size() == 0) {
        throw new NoSuchElementException();
      }
      count++;
      sentenceSource = sources.get(count % sources.size());
    }
    count++;
    return sentenceSource.next();
  }

  @Override
  public String getSource() {
    return "mixing:" + StringUtils.join(sources, ", ");
  }
}
