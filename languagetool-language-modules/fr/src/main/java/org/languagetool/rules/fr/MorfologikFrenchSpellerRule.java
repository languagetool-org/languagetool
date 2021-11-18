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

public final class MorfologikFrenchSpellerRule extends MorfologikSpellerRule {

  private static final String SPELLING_FILE = "/fr/hunspell/spelling.txt";

  private static final Pattern PARTICULA_INICIAL = Pattern.compile(
      "^(non|en|a|le|la|les|pour|de|du|des|un|une|mon|ma|mes|ton|ta|tes|son|sa|ses|leur|leurs|ce|cet) (..+)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern PREFIX_AMB_ESPAI = Pattern.compile(
      "^(agro|anti|archi|auto|aéro|cardio|co|cyber|demi|ex|extra|géo|hospitalo|hydro|hyper|hypo|infra|inter|macro|mega|meta|mi|micro|mini|mono|multi|musculo|méga|méta|néo|omni|pan|para|pluri|poly|post|prim|pro|proto|pré|pseudo|psycho|péri|re|retro|ré|semi|simili|socio|super|supra|sus|trans|tri|télé|ultra|uni|vice|éco|[^ayà]) (..+)$",
      //grand, haut, nord, sud, sous, sur l|d|s|t
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

  private static final Pattern APOSTROF_INICI_VERBS = Pattern.compile("^([lnts])(h?[aeiouàéèíòóú].*[^è])$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern APOSTROF_INICI_VERBS_M = Pattern.compile("^(m)(h?[aeiouàéèíòóú].*[^è])$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern APOSTROF_INICI_VERBS_C = Pattern.compile("^(c)([eiéèê].*)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern APOSTROF_INICI_NOM_SING = Pattern.compile("^([ld])(h?[aeiouàéèíòóú]...+)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern APOSTROF_INICI_NOM_PLURAL = Pattern.compile("^(d)(h?[aeiouàéèíòóú].+)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern APOSTROF_INICI_VERBS_INF = Pattern.compile("^([lntsmd]|nous|vous)(h?[aeiouàéèíòóú].*[^è])$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  //je, tu, il, elle, ce, on, nous, vous, ils
  private static final Pattern HYPHEN_ON = Pattern.compile("^([\\p{L}]+[^aeiou])[’']?(il|elle|ce|on)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern HYPHEN_JE = Pattern.compile("^([\\p{L}]+[^e])[’']?(je)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern HYPHEN_TU = Pattern.compile("^([\\p{L}]+)[’']?(tu)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern HYPHEN_NOUS = Pattern.compile("^([\\p{L}]+)[’']?(nous)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern HYPHEN_VOUS = Pattern.compile("^([\\p{L}]+)[’']?(vous)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern HYPHEN_ILS = Pattern.compile("^([\\p{L}]+)[’']?(ils|elles)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern SPLIT_SUGGESTIONS = Pattern.compile("^(..+\\p{L}|et|ou|de|en|à|aux|des)(\\d+)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  
  private static final Pattern IMPERATIVE_HYPHEN = Pattern.compile(
      "^([\\p{L}]+)[’']?(moi|toi|le|la|lui|nous|vous|les|leur|y|en)$", //|vs|y
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  
  //private static final Pattern MOVE_TO_SECOND_POS = Pattern.compile("^(.+'[nt])$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern VERB_INDSUBJ = Pattern.compile("V .*(ind|sub).*");
  private static final Pattern VERB_IMP = Pattern.compile("V.* imp .*");
  private static final Pattern VERB_INF = Pattern.compile("V.* inf");
  private static final Pattern VERB_INDSUBJ_M = Pattern.compile("V .* [123] s|V .* [23] p");
  private static final Pattern VERB_INDSUBJ_C = Pattern.compile("V .* 3 s");
  private static final Pattern NOM_SING = Pattern.compile("[NJZ] .* (s|sp)|V .inf|V .*ppa.* s");
  private static final Pattern NOM_PLURAL = Pattern.compile("[NJZ] .* (p|sp)|V .*ppa.* p");
  //private static final Pattern VERB_INFGERIMP = Pattern.compile("V.[NGM].*");
  //private static final Pattern VERB_INF = Pattern.compile("V.N.*");
  private static final Pattern ANY_TAG = Pattern.compile("[NAZJPD].*");
  
  private static final Pattern VERB_1S = Pattern.compile("V .*(ind).* 1 s");
  private static final Pattern VERB_2S = Pattern.compile("V .*(ind).* 2 s");
  private static final Pattern VERB_3S = Pattern.compile("V .*(ind).* 3 s");
  private static final Pattern VERB_1P = Pattern.compile("V .*(ind).* 1 p");
  private static final Pattern VERB_2P = Pattern.compile("V .*(ind).* 2 p");
  private static final Pattern VERB_3P = Pattern.compile("V .*(ind).* 3 p");

  private final String dictFilename;

  public MorfologikFrenchSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig,
      List<Language> altLanguages) throws IOException {
    super(messages, language, userConfig, altLanguages);
    this.setIgnoreTaggedWords();
    dictFilename = "/fr/french.dict";
  }

  @Override
  public String getFileName() {
    return dictFilename;
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

      // remove wrong split prefixes
      if (PREFIX_AMB_ESPAI.matcher(suggestions.get(i).getReplacement()).matches()) {
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
      Matcher matcher = PARTICULA_INICIAL.matcher(suggestions.get(i).getReplacement());
      if (matcher.matches()) {
        newSuggestions.add(posNewSugg, suggestions.get(i));
        continue;
      }

      String suggWithoutDiacritics = StringTools.removeDiacritics(suggestions.get(i).getReplacement());
      if (wordWithouDiacriticsString.equalsIgnoreCase(suggWithoutDiacritics)) {
        newSuggestions.add(posNewSugg, suggestions.get(i));
        continue;
      }

      // move words with apostrophe or hyphen to second position
      String cleanSuggestion = suggestions.get(i).getReplacement().replaceAll("'", "").replaceAll("-", "");
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

  private List<String> getAdditionalTopSuggestionsString(List<String> suggestions, String word) throws IOException {
    if (word.equals("voulai")) {
      return Arrays.asList("voulais", "voulait");
    } else if (word.toLowerCase().equals("mm2")) {
      return Arrays.asList("mm²");
    } else if (word.toLowerCase().equals("cm2")) {
      return Arrays.asList("cm²");
    } else if (word.toLowerCase().equals("dm2")) {
      return Arrays.asList("dm²");
    } else if (word.toLowerCase().equals("m2")) {
      return Arrays.asList("m²");
    } else if (word.toLowerCase().equals("km2")) {
      return Arrays.asList("km²");
    } else if (word.toLowerCase().equals("mm3")) {
      return Arrays.asList("mm³");
    } else if (word.toLowerCase().equals("cm3")) {
      return Arrays.asList("cm³");
    } else if (word.toLowerCase().equals("dm3")) {
      return Arrays.asList("dm³");
    } else if (word.toLowerCase().equals("m3")) {
      return Arrays.asList("m³");
    } else if (word.toLowerCase().equals("km3")) {
      return Arrays.asList("km³");
    }
    /*
     * if (word.length() < 5) { return Collections.emptyList(); }
     */
    List<String> newSuggestions = new ArrayList<>();
    newSuggestions.addAll(findSuggestion(word, SPLIT_SUGGESTIONS, ANY_TAG, 1, " ", true));
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
