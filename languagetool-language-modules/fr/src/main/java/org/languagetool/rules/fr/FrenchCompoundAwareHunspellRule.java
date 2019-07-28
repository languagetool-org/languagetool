/* LanguageTool, a natural language style checker 
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.fr;

import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.Example;
import org.languagetool.rules.spelling.hunspell.CompoundAwareHunspellRule;
import org.languagetool.rules.spelling.morfologik.MorfologikMultiSpeller;
import org.languagetool.tokenizers.CompoundWordTokenizer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A French spell checker that uses hunspell for checking but Morfologik for suggestions (for performance reasons).
 * @since 4.0
 */
public class FrenchCompoundAwareHunspellRule extends CompoundAwareHunspellRule {
  
  public FrenchCompoundAwareHunspellRule(ResourceBundle messages, Language language, UserConfig userConfig, List<Language> altLanguages) {
    super(messages, language, new NonSplittingTokenizer(), getSpeller(language, userConfig), userConfig, altLanguages);
    addExamplePair(Example.wrong("Le <marker>chein</marker> noir"),
                   Example.fixed("Le <marker>chien</marker> noir"));
  }

  @Override
  public String getId() {
    return "FR_SPELLING_RULE";
  }

  @Override
  protected void filterForLanguage(List<String> suggestions) {
  }

  @Nullable
  private static MorfologikMultiSpeller getSpeller(Language language, UserConfig userConfig) {
    if (!language.getShortCode().equals(Locale.FRENCH.getLanguage())) {
      throw new RuntimeException("Language is not a variant of French: " + language);
    }
    try {
      String morfoFile = "/fr/hunspell/fr_" + language.getCountries()[0] + ".dict";
      if (JLanguageTool.getDataBroker().resourceExists(morfoFile)) {
        // spell data will not exist in LibreOffice/OpenOffice context
        String path = "/fr/hunspell/spelling.txt";
        try (InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(path);
             BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
          return new MorfologikMultiSpeller(morfoFile, br, path, null, null, userConfig != null ? userConfig.getAcceptedWords(): Collections.emptyList(), 2);
        }
      } else {
        return null;
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not set up morfologik spell checker", e);
    }
  }

  static class NonSplittingTokenizer implements CompoundWordTokenizer {
    @Override
    public List<String> tokenize(String text) {
      return Collections.singletonList(text);
    }
  }

  @Override
  protected List<String> getAdditionalTopSuggestions(List<String> suggestions, String word) throws IOException {
    switch (word) {
      case "Jai": return Collections.singletonList("J'ai");
      case "jai": return Collections.singletonList("j'ai");
      default: return Collections.emptyList();
    }
  }
}
