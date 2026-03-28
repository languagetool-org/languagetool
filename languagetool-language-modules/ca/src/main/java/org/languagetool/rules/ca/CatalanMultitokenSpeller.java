/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Jaume Ortolà
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
package org.languagetool.rules.ca;

import org.languagetool.Languages;
import org.languagetool.rules.spelling.morfologik.MorfologikSpeller;
import org.languagetool.rules.spelling.morfologik.WeightedSuggestion;
import org.languagetool.rules.spelling.multitoken.MultitokenSpeller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CatalanMultitokenSpeller extends MultitokenSpeller {

  public static final CatalanMultitokenSpeller INSTANCE = new CatalanMultitokenSpeller();
  private static MorfologikSpeller speller;

  protected CatalanMultitokenSpeller() {
    super(Languages.getLanguageForShortCode("ca-ES"),
      Arrays.asList("/ca/multiwords.txt", "/spelling_global.txt", "/ca/hyphenated_words.txt"));
    this.speller = CatalanMorfologikMultitokenSpeller.getSpeller();
  }

  @Override
  protected List<WeightedSuggestion> getAdditionalSuggestions(String originalWord) throws IOException {
    if (speller != null) {
      // the weights from the dict are different!
      return speller.getSuggestions(originalWord);
    }
    return new ArrayList<>();
  }

}
