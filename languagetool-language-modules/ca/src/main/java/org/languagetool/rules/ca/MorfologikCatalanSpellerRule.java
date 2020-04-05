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

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class MorfologikCatalanSpellerRule extends MorfologikSpellerRule {

  private String dictFilename;
  private static final String SPELLING_FILE = "/ca/spelling.txt";
  
  private static final Pattern PARTICULA_INICIAL = Pattern.compile("^(els?|als?|pels?|dels?|de|per|uns?|una|unes|la|les|[tms]eus?) (..+)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern PREFIX_AMB_ESPAI = Pattern.compile("^(des|avant|auto|ex|extra|macro|mega|meta|micro|multi|mono|mini|post|retro|semi|super|trans) (..+)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  
  private static final Pattern APOSTROF_INICI_VERBS = Pattern.compile("^([lnmts])(h?[aeiouàéèíòóú].*)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern APOSTROF_INICI_NOM_SING = Pattern.compile("^([ld])(h?[aeiouàéèíòóú].+)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern APOSTROF_INICI_NOM_PLURAL = Pattern.compile("^(d)(h?[aeiouàéèíòóú].+)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern APOSTROF_FINAL = Pattern.compile("^(.+[aei])(l|ls|m|n|ns|s|t)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern GUIONET_FINAL = Pattern.compile("^([\\p{L}·]+)[’']?(hi|ho|la|les|li|lo|los|me|ne|nos|se|te|vos)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern VERB_INDSUBJ = Pattern.compile("V.[SI].*");
  private static final Pattern NOM_SING = Pattern.compile("V.[NG].*|V.P..S..|N..[SN].*|A...[SN].|PX..S...|DD..S.");
  private static final Pattern NOM_PLURAL = Pattern.compile("V.P..P..|N..[PN].*|A...[PN].|PX..P...|DD..P.");
  private static final Pattern INICI_NOM = Pattern.compile("^(a|en)(.+)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern NOM = Pattern.compile("V.P.*|N.*|A.*|PX.*|DD.*");
  private static final Pattern VERB_INFGERIMP = Pattern.compile("V.[NGM].*");
  private CatalanTagger tagger;

  public MorfologikCatalanSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig, List<Language> altLanguages) throws IOException {
    super(messages, language, userConfig, altLanguages);
    this.setIgnoreTaggedWords();
    tagger = new CatalanTagger(language);
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
    //move some run-on-words suggestions to the top
    List<SuggestedReplacement> newSuggestions = new ArrayList<>();
    for (SuggestedReplacement suggestion : suggestions) {
      //remove wrong split prefixes
      if (!PREFIX_AMB_ESPAI.matcher(suggestion.getReplacement()).matches()) {
        if (PARTICULA_INICIAL.matcher(suggestion.getReplacement()).matches()) {
          newSuggestions.add(0, suggestion);
        } else {
          newSuggestions.add(suggestion);
        }
      }
    }
    return newSuggestions;
  }
  @Override
  protected List<SuggestedReplacement> getAdditionalTopSuggestions(List<SuggestedReplacement> suggestions, String word) throws IOException {
    List<String> suggestionsList = suggestions.stream()
      .map(SuggestedReplacement::getReplacement).collect(Collectors.toList());
    return SuggestedReplacement.convert(getAdditionalTopSuggestionsString(suggestionsList, word));
  }

  private List<String> getAdditionalTopSuggestionsString(List<String> suggestions, String word) throws IOException {
    //TODO Try other combinations. Ex. daconseguirlos, 
    //TODO Including errors (Hunspell can do it). Ex. sescontaminarla > descontaminar-la
    /*if (word.length() < 5) {
      return Collections.emptyList();
    }*/
    String suggestion = "";
    suggestion = findSuggestion(suggestion, word, APOSTROF_INICI_VERBS, VERB_INDSUBJ, 2, "'");
    suggestion = findSuggestion(suggestion, word, APOSTROF_INICI_NOM_SING, NOM_SING, 2, "'");
    suggestion = findSuggestion(suggestion, word, APOSTROF_INICI_NOM_PLURAL, NOM_PLURAL, 2, "'");
    suggestion = findSuggestion(suggestion, word, INICI_NOM, NOM, 2, " ");
    if (!word.endsWith("as") && !word.endsWith("et")) { 
      suggestion = findSuggestion(suggestion, word, APOSTROF_FINAL, VERB_INFGERIMP, 1, "'");
    }
    suggestion = findSuggestion(suggestion, word, GUIONET_FINAL, VERB_INFGERIMP, 1, "-");
    if (!suggestion.isEmpty()) {
      return Collections.singletonList(suggestion);
    } 
    return Collections.emptyList();
  }
  
  private String findSuggestion(String suggestion, String word,
      Pattern wordPattern, Pattern postagPattern, int suggestionPosition,
      String separator) throws IOException {
    if (!suggestion.isEmpty()) {
      return suggestion;
    }
    Matcher matcher = wordPattern.matcher(word);
    if (matcher.matches()) {
      String newSuggestion = matcher.group(suggestionPosition);
      if (matchPostagRegexp(tagger.tag(Arrays.asList(newSuggestion)).get(0), postagPattern)) {
        return matcher.group(1) + separator + matcher.group(2);
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
