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

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.rules.Example;
import org.languagetool.rules.SuggestedReplacement;
import org.languagetool.rules.spelling.hunspell.CompoundAwareHunspellRule;
import org.languagetool.rules.spelling.morfologik.MorfologikMultiSpeller;
import org.languagetool.tagging.Tagger;
import org.languagetool.tokenizers.CompoundWordTokenizer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A French spell checker that uses hunspell for checking but Morfologik for suggestions (for performance reasons).
 * @since 4.0
 */
public class FrenchCompoundAwareHunspellRule extends CompoundAwareHunspellRule {

  private final static Tagger tagger = Languages.getLanguageForShortCode("fr").getTagger();
  private final static Pattern vocalPattern = Pattern.compile("[dDLl]['’′][Hh]?[aàâäeéèêëiîïoöôuyAÀÂÄEÉÈÊËIÎÏOÖÔUY].*");

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
      String morfoFile = "/fr/hunspell/fr_" + language.getCountries()[0] + JLanguageTool.DICTIONARY_FILENAME_EXTENSION;
      if (JLanguageTool.getDataBroker().resourceExists(morfoFile)) {
        // spell data will not exist in LibreOffice/OpenOffice context
        String path = "/fr/hunspell/spelling.txt";
        try (InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(path);
             BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
          return new MorfologikMultiSpeller(morfoFile, br, Collections.singletonList(path), null, null, userConfig != null ? userConfig.getAcceptedWords(): Collections.emptyList(), 2);
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
  protected List<SuggestedReplacement> getAdditionalTopSuggestions(List<SuggestedReplacement> suggestions, String word) throws IOException {
    String s = null;
    switch (word) {
      case "Jai": s = "J'ai"; break;
      case "jai": s = "j'ai"; break;
      case "Etais-tu": s = "Étais-tu"; break;
      case "etais-tu": s = "étais-tu"; break;
      case "Etes-vous": s = "Êtes-vous"; break;
      case "etes-vous": s = "êtes-vous"; break;
      case "Etiez-vous": s = "Êtiez-vous"; break;
      case "etiez-vous": s = "êtiez-vous"; break;
      case "Etait-ce": s = "Était-ce"; break;
      case "etait-ce": s = "était-ce"; break;
      case "Etait-il": s = "Était-il"; break;
      case "etait-il": s = "était-il"; break;
      case "Depeche-toi": s = "Dépêche-toi"; break;
      case "depeche-toi": s = "dépêche-toi"; break;
      case "preferes-tu": s = "préfères-tu"; break;
      case "Preferes-tu": s = "Préfères-tu"; break;
      case "la-bas": s = "là-bas"; break;
      case "la-dedans": s = "là-dedans"; break;
      case "la-dessus": s = "là-dessus"; break;
      /* a more generic solution could be like this, but which of the suggestions for the first part can be re-prepended?
      Pattern p = Pattern.compile("([a-zA-Z]+)-(tu|vous|ce|il|toi)");
      Matcher matcher = p.matcher(word);
      if (matcher.matches()) {
        System.out.println("-->" + matcher.group(1));
        System.out.println("-->" + getSuggestions(matcher.group(1)));
      }
      */
    }
    if (s == null) {
      return SuggestedReplacement.convert(Collections.emptyList());
    } else {
      return SuggestedReplacement.convert(Collections.singletonList(s));
    }
  }

  @Override
  protected boolean ignoreWord(List<String> words, int idx) throws IOException {
    boolean ignore = super.ignoreWord(words, idx);
    boolean ignoreUncapitalizedWord = !ignore && idx == 0 && super.ignoreWord(StringUtils.uncapitalize(words.get(0)));
    return ignore || ignoreUncapitalizedWord;
  }

  @Override
  protected boolean ignoreWord(String word) throws IOException {
    boolean ignore = super.ignoreWord(word);
    if (ignore) {
      return true;
    }
    if (word.length() > 3) {
      Matcher matcher = vocalPattern.matcher(word);   // e.g. "d'Harvard"
      if (matcher.matches()) {
        List<AnalyzedTokenReadings> readings = tagger.tag(Collections.singletonList(word.substring(2)));
        //System.out.println(word + " => " + readings  + " for '" + word.substring(2) + "'");
        return readings.stream().anyMatch(k -> k.hasPosTagStartingWith("N ") || k.hasPosTagStartingWith("Z "));
      }
    }
    return false;
  }

}
