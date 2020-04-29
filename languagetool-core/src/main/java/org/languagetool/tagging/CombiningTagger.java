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
package org.languagetool.tagging;

import java.util.ArrayList;
import java.util.List;

/**
 * Tags a word using two taggers, combining their results.
 * @since 2.8
 */
public class CombiningTagger implements WordTagger {

  private final WordTagger tagger1;
  private final WordTagger tagger2;
  private final WordTagger removalTagger;
  private final boolean overwriteWithSecondTagger;

  public CombiningTagger(WordTagger tagger1, WordTagger tagger2, boolean overwriteWithSecondTagger) {
    this(tagger1, tagger2, null, overwriteWithSecondTagger);
  }

  /**
   * @param tagger1 typically the tagger that takes its data from the binary file
   * @param tagger2 typically the tagger that takes its data from the plain text file {@code added.txt}
   * @param removalTagger the tagger that removes readings which takes its data from the plain text file {@code removed.txt}, or {@code null}
   * @param overwriteWithSecondTagger if set to {@code true}, only the second tagger's result will be
   *                                  used if both first and second tagger can tag that word
   * @since 3.2
   */
  public CombiningTagger(WordTagger tagger1, WordTagger tagger2, WordTagger removalTagger, boolean overwriteWithSecondTagger) {
    this.tagger1 = tagger1;
    this.tagger2 = tagger2;
    this.removalTagger = removalTagger;
    this.overwriteWithSecondTagger = overwriteWithSecondTagger;
  }

  @Override
  public List<TaggedWord> tag(String word) {
    List<TaggedWord> result = new ArrayList<>();
    result.addAll(tagger2.tag(word));
    if (!(overwriteWithSecondTagger && result.size() > 0)) {
      result.addAll(tagger1.tag(word));
    }
    if (removalTagger != null) {
      List<TaggedWord> removalTags = removalTagger.tag(word);
      result.removeAll(removalTags);
    }
    return result;
  }
  
}
