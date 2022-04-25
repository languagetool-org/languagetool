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
package org.languagetool.dev.dumpcheck;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.Language;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Alternately returns sentences from different sentence sources.
 * @since 2.4
 */
public class MixingSentenceSource extends SentenceSource {

  private final List<SentenceSource> sources;
  private final Map<String, Integer> sourceDistribution = new HashMap<>();
  
  private int count;

  public static MixingSentenceSource create(List<String> dumpFileNames, Language language) throws IOException {
    return create(dumpFileNames, language, null);
  }

  public static MixingSentenceSource create(List<String> dumpFileNames, Language language, Pattern filter) throws IOException {
    List<SentenceSource> sources = new ArrayList<>();
    for (String dumpFileName : dumpFileNames) {
      File file = new File(dumpFileName);
      if (file.getName().endsWith(".xml")) {
        sources.add(new WikipediaSentenceSource(new FileInputStream(dumpFileName), language, filter));
      } else if (file.getName().startsWith("tatoeba-")) {
        sources.add(new TatoebaSentenceSource(new FileInputStream(dumpFileName), language, filter));
      } else if (file.getName().endsWith(".txt")) {
        sources.add(new PlainTextSentenceSource(new FileInputStream(dumpFileName), language, filter));
      } else if (file.getName().endsWith(".xz")) {
        sources.add(new CommonCrawlSentenceSource(new FileInputStream(dumpFileName), language, filter));
      } else {
        throw new RuntimeException("Could not find a source handler for " + dumpFileName +
                " - Wikipedia files must be named '*.xml', Tatoeba files must be named 'tatoeba-*', CommonCrawl files '*.xz', plain text files '*.txt'");
      }
    }
    return new MixingSentenceSource(sources, language);
  }

  private MixingSentenceSource(List<SentenceSource> sources, Language language) {
    super(language);
    this.sources = sources;
  }

  Map<String, Integer> getSourceDistribution() {
    return sourceDistribution;
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
      if (sources.isEmpty()) {
        throw new NoSuchElementException();
      }
      count++;
      sentenceSource = sources.get(count % sources.size());
    }
    count++;
    Sentence next = sentenceSource.next();
    updateDistributionMap(next);
    return next;
  }

  private void updateDistributionMap(Sentence next) {
    Integer prevCount = sourceDistribution.get(next.getSource());
    if (prevCount != null) {
      sourceDistribution.put(next.getSource(), prevCount + 1);
    } else {
      sourceDistribution.put(next.getSource(), 1);
    }
  }

  @Override
  public String getSource() {
    return StringUtils.join(sources, ", ");
  }

  int getIgnoredCount() {
    int sum = 0;
    for (SentenceSource source : sources) {
      sum += source.getIgnoredCount();
    }
    return sum;
  }

}
