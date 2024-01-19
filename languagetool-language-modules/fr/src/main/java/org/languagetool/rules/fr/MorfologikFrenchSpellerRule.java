/* LanguageTool, a natural language style checker 
 * Copyright (C) 2020 Jaume Ortolà (http://www.languagetool.org)
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

import org.languagetool.*;
import org.languagetool.rules.SuggestedReplacement;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;
import org.languagetool.tagging.fr.FrenchTagger;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.regex.Pattern.*;

public final class MorfologikFrenchSpellerRule extends MorfologikSpellerRule {

  private static final String SPELLING_FILE = "/fr/hunspell/spelling.txt";

  private static final int flags = CASE_INSENSITIVE | UNICODE_CASE;
  private static final List<String> TOKEN_AT_START = Arrays.asList("non", "en", "a", "le", "la", "les", "pour", "de",
    "du", "des", "un", "une", "mon", "ma", "mes", "ton", "ta", "tes", "son", "sa", "ses", "leur", "leurs", "ce", "cet");
  private static final List<String> PREFIX_WITH_WHITESPACE = Arrays.asList("agro", "anti", "archi", "auto", "aéro",
    "cardio", "co", "cyber", "demi", "ex", "extra", "géo", "hospitalo", "hydro", "hyper", "hypo", "infra", "inter",
    "macro", "mega", "meta", "mi", "micro", "mini", "mono", "multi", "musculo", "méga", "méta", "néo", "omni", "pan",
    "para", "pluri", "poly", "post", "prim", "pro", "proto", "pré", "pseudo", "psycho", "péri", "re", "retro", "ré",
    "semi", "simili", "socio", "super", "supra", "sus", "trans", "tri", "télé", "ultra", "uni", "vice", "éco");
  //grand, haut, nord, sud, sous, sur l|d|s|t
  private static final List<String> exceptionsEgrave = Arrays.asList(new String[]{"burkinabè", "koinè", "épistémè"});
  private static final Pattern APOSTROF_INICI_VERBS = compile("^([lnts])(h?[aeiouàéèíòóú].*[^è])$", flags);
  private static final Pattern APOSTROF_INICI_VERBS_M = compile("^(m)(h?[aeiouàéèíòóú].*[^è])$", flags);
  private static final Pattern APOSTROF_INICI_VERBS_C = compile("^(c)([eiéèê].*)$", flags);
  private static final Pattern APOSTROF_INICI_NOM_SING = compile("^([ld])(h?[aeiouàéèíòóú]...+)$", flags);
  private static final Pattern APOSTROF_INICI_NOM_PLURAL = compile("^(d)(h?[aeiouàéèíòóú].+)$", flags);
  private static final Pattern APOSTROF_INICI_VERBS_INF = compile("^([lntsmd]|nous|vous)(h?[aeiouàéèíòóú].*[^è])$", flags);
  //je, tu, il, elle, ce, on, nous, vous, ils
  private static final Pattern HYPHEN_ON = compile("^([\\p{L}]+[^aeiou])[’']?(il|elle|ce|on)$", flags);
  private static final Pattern HYPHEN_JE = compile("^([\\p{L}]+[^e])[’']?(je)$", flags);
  private static final Pattern HYPHEN_TU = compile("^([\\p{L}]+)[’']?(tu)$", flags);
  private static final Pattern HYPHEN_NOUS = compile("^([\\p{L}]+)[’']?(nous)$", flags);
  private static final Pattern HYPHEN_VOUS = compile("^([\\p{L}]+)[’']?(vous)$", flags);
  private static final Pattern HYPHEN_ILS = compile("^([\\p{L}]+)[’']?(ils|elles)$", flags);
  private static final List<String> SPLIT_DIGITS_AT_END = Arrays.asList("et", "ou", "de", "en", "à", "aux", "des");
  private static final Pattern IMPERATIVE_HYPHEN = compile(
      "^([\\p{L}]+)[’']?(moi|toi|le|la|lui|nous|vous|les|leur|y|en)$", flags); //|vs|y

  //private static final Pattern MOVE_TO_SECOND_POS = Pattern.compile("^(.+'[nt])$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern VERB_INDSUBJ = compile("V .*(ind|sub).*");
  private static final Pattern VERB_IMP = compile("V.* imp .*");
  private static final Pattern VERB_INF = compile("V.* inf");
  private static final Pattern VERB_INDSUBJ_M = compile("V .* [123] s|V .* [23] p");
  private static final Pattern VERB_INDSUBJ_C = compile("V .* 3 s");
  private static final Pattern NOM_SING = compile("[NJZ] .* (s|sp)|V .inf|V .*ppa.* s");
  private static final Pattern NOM_PLURAL = compile("[NJZ] .* (p|sp)|V .*ppa.* p");
  //private static final Pattern VERB_INFGERIMP = Pattern.compile("V.[NGM].*");
  //private static final Pattern VERB_INF = Pattern.compile("V.N.*");
  private static final Pattern ANY_TAG = compile("[NAZJPD].*");
  
  private static final Pattern VERB_1S = compile("V .*(ind).* 1 s");
  private static final Pattern VERB_2S = compile("V .*(ind).* 2 s");
  private static final Pattern VERB_3S = compile("V .*(ind).* 3 s");
  private static final Pattern VERB_1P = compile("V .*(ind).* 1 p");
  private static final Pattern VERB_2P = compile("V .*(ind).* 2 p");
  private static final Pattern VERB_3P = compile("V .*(ind).* 3 p");
  private static final String DICT_FILE = "/fr/french.dict";
  private static final Pattern HYPHEN_OR_QUOTE = compile("['-]");

  public MorfologikFrenchSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig,
      List<Language> altLanguages) throws IOException {
    super(messages, language, userConfig, altLanguages);
    setIgnoreTaggedWords();
  }

  @Override
  public String getFileName() {
    return DICT_FILE;
  }

  @Override
  public String getSpellingFileName() {
    return SPELLING_FILE;
  }

  @Override
  public String getId() {
    return "FR_SPELLING_RULE"; // same name just for diffs
  }

  @Override
  // Use this rule in LO/OO extension despite being a spelling rule
  public boolean useInOffice() {
    return true;
  }

  @Override
  protected List<SuggestedReplacement> orderSuggestions(List<SuggestedReplacement> suggestions, String word) {
    // move some run-on-words suggestions to the top
    List<SuggestedReplacement> newSuggestions = new ArrayList<>();
    // for (SuggestedReplacement suggestion : suggestions) {
    String wordWithouDiacriticsString = StringTools.removeDiacritics(word);
    for (int i = 0; i < suggestions.size(); i++) {

      String[] parts = suggestions.get(i).getReplacement().toLowerCase().split(" ");

      // remove wrong split prefixes
      if (parts.length == 2 && PREFIX_WITH_WHITESPACE.contains(parts[0])) {
        continue;
      }
      if (parts[0].length() == 1 && !parts[0].equals("a") && !parts[0].equals("à") && !parts[0].equals("y")) {
        continue;
      }
      // remove: informè V ind pres 1 s
      if (suggestions.get(i).getReplacement().toLowerCase().endsWith("è")
        && !exceptionsEgrave.contains(suggestions.get(i).getReplacement().toLowerCase())) {
        continue;
      }

      // Don't change first suggestions if they match word without diacritics
      int posNewSugg = 0;
      while (newSuggestions.size() > posNewSugg
          && StringTools.removeDiacritics(newSuggestions.get(posNewSugg).getReplacement())
              .equalsIgnoreCase(wordWithouDiacriticsString)) {
        posNewSugg++;
      }
      // move some split words to first place
      if (parts.length == 2 && TOKEN_AT_START.contains(parts[0]) && parts[1].length() > 1) {
        newSuggestions.add(posNewSugg, suggestions.get(i));
        continue;
      }

      String suggWithoutDiacritics = StringTools.removeDiacritics(suggestions.get(i).getReplacement());
      if (wordWithouDiacriticsString.equalsIgnoreCase(suggWithoutDiacritics)) {
        newSuggestions.add(posNewSugg, suggestions.get(i));
        continue;
      }

      // move words with apostrophe or hyphen to second position
      String cleanSuggestion = HYPHEN_OR_QUOTE.matcher(suggestions.get(i).getReplacement()).replaceAll("");
      if (i > 1 && suggestions.size() > 2 && cleanSuggestion.equalsIgnoreCase(word)) {
        if (posNewSugg == 0) {
          posNewSugg = 1;
        }
        newSuggestions.add(posNewSugg, suggestions.get(i));
        continue;
      }
      newSuggestions.add(suggestions.get(i));
    }
    return newSuggestions;
  }

  @Override
  protected List<SuggestedReplacement> getAdditionalTopSuggestions(List<SuggestedReplacement> suggestions, String word)
      throws IOException {
    List<String> suggestionsList = suggestions.stream().map(SuggestedReplacement::getReplacement)
        .collect(Collectors.toList());
    return SuggestedReplacement.convert(getAdditionalTopSuggestionsString(suggestionsList, word));
  }

  private List<String> getAdditionalTopSuggestionsString(List<String> suggestions, String word) {
    if (word.equals("voulai")) {
      return Arrays.asList("voulais", "voulait");
    } else if (word.equalsIgnoreCase("mm2")) {
      return Arrays.asList("mm²");
    } else if (word.equalsIgnoreCase("cm2")) {
      return Arrays.asList("cm²");
    } else if (word.equalsIgnoreCase("dm2")) {
      return Arrays.asList("dm²");
    } else if (word.equalsIgnoreCase("m2")) {
      return Arrays.asList("m²");
    } else if (word.equalsIgnoreCase("km2")) {
      return Arrays.asList("km²");
    } else if (word.equalsIgnoreCase("mm3")) {
      return Arrays.asList("mm³");
    } else if (word.equalsIgnoreCase("cm3")) {
      return Arrays.asList("cm³");
    } else if (word.equalsIgnoreCase("dm3")) {
      return Arrays.asList("dm³");
    } else if (word.equalsIgnoreCase("m3")) {
      return Arrays.asList("m³");
    } else if (word.equalsIgnoreCase("km3")) {
      return Arrays.asList("km³");
    }
    /*
     * if (word.length() < 5) { return Collections.emptyList(); }
     */
    String[] parts = StringTools.splitCamelCase(word);
    if (parts.length > 1 && parts[0].length() > 1) {
      boolean isNotMisspelled = true;
      for(String part: parts) {
        isNotMisspelled &= !speller1.isMisspelled(part);
      }
      if (isNotMisspelled) {
        return Collections.singletonList(String.join(" ",parts));
      }
    }
    parts = StringTools.splitDigitsAtEnd(word);
    if (parts.length > 1) {
      if (FrenchTagger.INSTANCE.tag(Arrays.asList(parts[0])).get(0).isTagged()
        && (parts[0].length() > 2 || SPLIT_DIGITS_AT_END.contains(parts[0].toLowerCase()))) {
        return Collections.singletonList(String.join(" ",parts));
      }
    }
    List<String> newSuggestions = new ArrayList<>();
    newSuggestions.addAll(findSuggestion(word, APOSTROF_INICI_VERBS, VERB_INDSUBJ, 2, "'", true));
    newSuggestions.addAll(findSuggestion(word, APOSTROF_INICI_VERBS_M, VERB_INDSUBJ_M, 2, "'", true));
    newSuggestions.addAll(findSuggestion(word, APOSTROF_INICI_VERBS_C, VERB_INDSUBJ_C, 2, "'", true));
    newSuggestions.addAll(findSuggestion(word, APOSTROF_INICI_VERBS_INF, VERB_INF, 2, "'", true));
    newSuggestions.addAll(findSuggestion(word, APOSTROF_INICI_NOM_SING, NOM_SING, 2, "'", true));
    newSuggestions.addAll(findSuggestion(word, APOSTROF_INICI_NOM_PLURAL, NOM_PLURAL, 2, "'", true));
    //newSuggestions.addAll(findSuggestion(word, APOSTROF_FINAL, VERB_INFGERIMP, 1, "'", true);
    //newSuggestions.addAll(findSuggestion(word, APOSTROF_FINAL_S, VERB_INF, 1, "'", true);
    newSuggestions.addAll(findSuggestion(word, IMPERATIVE_HYPHEN, VERB_IMP, 1, "-", true));
    //newSuggestions.addAll(findSuggestion(word, GUIONET_FINAL, VERB_INDSUBJ, 1, "-", true);
    
    newSuggestions.addAll(findSuggestion(word, HYPHEN_JE, VERB_1S, 1, "-", true));
    newSuggestions.addAll(findSuggestion(word, HYPHEN_TU, VERB_2S, 1, "-", true));
    newSuggestions.addAll(findSuggestion(word, HYPHEN_ON, VERB_3S, 1, "-", true));
    newSuggestions.addAll(findSuggestion(word, HYPHEN_NOUS, VERB_1P, 1, "-", true));
    newSuggestions.addAll(findSuggestion(word, HYPHEN_VOUS, VERB_2P, 1, "-", true));
    newSuggestions.addAll(findSuggestion(word, HYPHEN_ILS, VERB_3P, 1, "-", true));

    if (!newSuggestions.isEmpty()) {
      return newSuggestions;
    }
    return Collections.emptyList();
  }

  private List<String> findSuggestion(String word, Pattern wordPattern, Pattern postagPattern,
      int suggestionPosition, String separator, boolean recursive) {
    List<String> newSuggestions = new ArrayList<>();
    Matcher matcher = wordPattern.matcher(word);
    if (matcher.matches()) {
      String newSuggestion = matcher.group(suggestionPosition);
      AnalyzedTokenReadings newatr = FrenchTagger.INSTANCE.tag(Arrays.asList(newSuggestion)).get(0);
      if (matchPostagRegexp(newatr, postagPattern)) {
        newSuggestions.add(matcher.group(1) + separator + matcher.group(2));
        return newSuggestions;
      }
      if (recursive) {
        List<String> moreSugg = this.speller1.getSuggestions(newSuggestion);
        if (moreSugg.size() > 0) {
          for (int i = 0; i < moreSugg.size(); i++) {
            String newWord;
            if (suggestionPosition == 1) {
              newWord = moreSugg.get(i) + matcher.group(2); //.toLowerCase()
            } else {
              newWord = matcher.group(1) + moreSugg.get(i);
            }
            List<String> newSugg = findSuggestion(newWord, wordPattern, postagPattern, suggestionPosition, separator, false);
            if (!newSugg.isEmpty()) {
              newSuggestions.addAll(newSugg);
            }
            if (i > 5) {
              break;
            }
          }
        }
      }
    }
    if (!newSuggestions.isEmpty()) {
      return newSuggestions;
    }
    return Collections.emptyList();
  }

  /**
   * Match POS tag with regular expression
   */
  private boolean matchPostagRegexp(AnalyzedTokenReadings aToken, Pattern pattern) {
    for (AnalyzedToken analyzedToken : aToken) {
      String posTag = analyzedToken.getPOSTag();
      if (posTag == null) {
        posTag = "UNKNOWN";
      }
      Matcher m = pattern.matcher(posTag);
      if (m.matches()) {
        return true;
      }
    }
    return false;
  }

}
