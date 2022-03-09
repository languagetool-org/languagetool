/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
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

import org.languagetool.*;
import org.languagetool.rules.SuggestedReplacement;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;
import org.languagetool.tagging.ca.CatalanTagger;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class MorfologikCatalanSpellerRule extends MorfologikSpellerRule {

  private String dictFilename;
  private static final String SPELLING_FILE = "/ca/spelling.txt";

  private static final Pattern PARTICULA_INICIAL = Pattern.compile(
      "^(no|en|a|els?|als?|pels?|dels?|de|per|uns?|una|unes|la|les|[tms]eus?) (..+)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern PREFIX_AMB_ESPAI = Pattern.compile(
      "^(tele|anti|re|des|avant|auto|ex|extra|macro|mega|meta|micro|multi|mono|mini|post|retro|semi|super|trans|pro|g) (..+)|.+ s$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

  private static final Pattern APOSTROF_INICI_VERBS = Pattern.compile("^([lnts])[90]?(h?[aeiouàéèíòóú].*)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern APOSTROF_INICI_VERBS_M = Pattern.compile("^(m)[90]?(h?[aeiouàéèíòóú].*)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern APOSTROF_INICI_NOM_SING = Pattern.compile("^([ld])[90]?(h?[aeiouàéèíòóú]...+)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern APOSTROF_INICI_NOM_PLURAL = Pattern.compile("^(d)[90]?(h?[aeiouàéèíòóú].+)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern APOSTROF_FINAL = Pattern.compile("^(...+[aei])[90]?(l|ls|m|ns|n|t)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern APOSTROF_FINAL_S = Pattern.compile("^(.+e)[90]?(s)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern GUIONET_FINAL = Pattern.compile(
      "^([\\p{L}·]+)[’']?(hi|ho|la|les|li|lo|los|me|ne|nos|se|te|vos)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern SPLIT_SUGGESTIONS = Pattern.compile("^(..+\\p{L}|en|de|del|al|dels|als|a|i|o|amb)(\\d+)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern MOVE_TO_SECOND_POS = Pattern.compile("^(.+'[nt])$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern VERB_INDSUBJ = Pattern.compile("V.[SI].*");
  private static final Pattern VERB_INDSUBJ_M = Pattern.compile("V.[SI].[123]S.*|V.[SI].[23]P.*");
  private static final Pattern NOM_SING = Pattern.compile("V.[NG].*|V.P..S..|N..[SN].*|A...[SN].|PX..S...|DD..S.");
  private static final Pattern NOM_PLURAL = Pattern.compile("V.P..P..|N..[PN].*|A...[PN].|PX..P...|DD..P.");
  private static final Pattern VERB_INFGERIMP = Pattern.compile("V.[NGM].*");
  private static final Pattern VERB_INF = Pattern.compile("V.N.*");
  private static final Pattern ANY_TAG = Pattern.compile("[NVACPDRS].*");
  private CatalanTagger tagger;

  public MorfologikCatalanSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig,
      List<Language> altLanguages) throws IOException {
    super(messages, language, userConfig, altLanguages);
    this.setIgnoreTaggedWords();
    if (language.getVariant() != null) {
      tagger = CatalanTagger.INSTANCE_VAL;
    } else {
      tagger = CatalanTagger.INSTANCE_CAT;
    }
    dictFilename = "/ca/" + language.getShortCodeWithCountryAndVariant() + JLanguageTool.DICTIONARY_FILENAME_EXTENSION;
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
    return "MORFOLOGIK_RULE_CA_ES";
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
        String newSuggestion = matcher.group(2);
        List<AnalyzedTokenReadings> atkn = tagger.tag(Arrays.asList(newSuggestion));
        boolean isBalear = atkn.get(0).hasPosTag("VMIP1S0B") && !atkn.get(0).hasPosTagStartingWith("N");
        if (!isBalear) {
          newSuggestions.add(posNewSugg, suggestions.get(i));
          continue;
        }
      }
      
      String suggWithoutDiacritics = StringTools.removeDiacritics(suggestions.get(i).getReplacement());
      if (word.equalsIgnoreCase(suggWithoutDiacritics)) {
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

      // move "queda'n" to second position
      if (i == 1) {
        Matcher m = MOVE_TO_SECOND_POS.matcher(suggestions.get(0).getReplacement());
        if (m.matches()) {
          newSuggestions.add(0, suggestions.get(i));
          continue;
        }
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
    /*
     * if (word.length() < 5) { return Collections.emptyList(); }
     */
    String suggestion = "";
    suggestion = findSuggestion(suggestion, word, APOSTROF_INICI_VERBS, VERB_INDSUBJ, 2, "'", true);
    suggestion = findSuggestion(suggestion, word, APOSTROF_INICI_VERBS_M, VERB_INDSUBJ_M, 2, "'", true);
    suggestion = findSuggestion(suggestion, word, APOSTROF_INICI_NOM_SING, NOM_SING, 2, "'", true);
    suggestion = findSuggestion(suggestion, word, APOSTROF_INICI_NOM_PLURAL, NOM_PLURAL, 2, "'", true);
    suggestion = findSuggestion(suggestion, word, APOSTROF_FINAL, VERB_INFGERIMP, 1, "'", true);
    suggestion = findSuggestion(suggestion, word, APOSTROF_FINAL_S, VERB_INF, 1, "'", true);
    suggestion = findSuggestion(suggestion, word, GUIONET_FINAL, VERB_INFGERIMP, 1, "-", true);
    suggestion = findSuggestion(suggestion, word, SPLIT_SUGGESTIONS, ANY_TAG, 1, " ", true);
    if (!suggestion.isEmpty()) {
      return Collections.singletonList(suggestion);
    }
    return Collections.emptyList();
  }

  private String findSuggestion(String suggestion, String word, Pattern wordPattern, Pattern postagPattern,
      int suggestionPosition, String separator, boolean recursive) throws IOException {
    if (!suggestion.isEmpty()) {
      return suggestion;
    }
    Matcher matcher = wordPattern.matcher(word);
    if (matcher.matches()) {
      String newSuggestion = matcher.group(suggestionPosition);
      AnalyzedTokenReadings newatr = tagger.tag(Arrays.asList(newSuggestion)).get(0);
      if ((!newatr.hasPosTag("VMIP1S0B") || newSuggestion.equals("fer")) && matchPostagRegexp(newatr, postagPattern)) {
        return matcher.group(1) + separator + matcher.group(2);
      }
      if (recursive) {
        List<String> moresugg = this.speller1.getSuggestions(newSuggestion);
        if (moresugg.size() > 0) {
          String newWord;
          if (suggestionPosition == 1) {
            newWord = moresugg.get(0) + matcher.group(2); //.toLowerCase()
          } else {
            newWord = matcher.group(1) + moresugg.get(0).toLowerCase();
          }
          return findSuggestion(suggestion, newWord, wordPattern, postagPattern, suggestionPosition, separator, false);
        }
      }
    }
    return "";
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
      final Matcher m = pattern.matcher(posTag);
      if (m.matches()) {
        return true;
      }
    }
    return false;
  }

}
