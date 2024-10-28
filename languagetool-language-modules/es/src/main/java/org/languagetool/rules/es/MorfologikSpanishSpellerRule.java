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
package org.languagetool.rules.es;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.SuggestedReplacement;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;
import org.languagetool.tagging.es.SpanishTagger;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @since 2.8
 */
public class MorfologikSpanishSpellerRule extends MorfologikSpellerRule {

  private static final List<String> REMOVE_FROM_SUGGESTIONS = Arrays.asList("abu", "abue", "abus", "anarco", "anarcos",
    "arbi", "arbis", "arqui", "arquis", "Barna", "bibe", "bibes", "biblio", "biblios", "bolche", "bolches", "cami",
    "camis", "capi", "capis", "celu", "celus", "ceni", "cenis", "cerve", "cerves", "chiqui", "chiquis", "chuche",
    "chuches", "chumi", "chumis", "cintu", "cintus", "comi", "comis", "compu", "compus", "confe", "confes", "confi",
    "confis", "conge", "conges", "copi", "copis", "cosquis", "coti", "cotis", "cíber", "deco", "decos", "deli", "delis",
    "depa", "depas", "díver", "facu", "facus", "festi", "festis", "frigo", "frigos", "fácul", "gili", "gilis", "gine",
    "gineco", "ginecos", "gines", "Graná", "hospi", "hospis", "ilu", "ilus", "impeque", "impeques", "inge", "inges",
    "joputa", "joputas", "jueputa", "jueputas", "lesbi", "lesbis", "lipo", "lipos", "lito", "litos", "mani", "manifa",
    "manifas", "manis", "mari", "maris", "masoca", "masocas", "milqui", "milquis", "munipa", "munipas", "ofi", "ofis",
    "pandi", "pandis", "pasti", "pastis", "pelu", "pelus", "pendeviejo", "pendeviejos", "peni", "penis", "pisci",
    "piscis", "piti", "pitis", "porfaplís", "porfi", "porfiplís", "porfis", "porsi", "porsiaca", "porsiacas", "porsis",
    "prefe", "prefes", "prince", "princes", "pringui", "pringuis", "prosti", "prostis", "prota", "protas", "prote",
    "protes", "psico", "psicos", "psiqui", "psiquis", "publi", "publis", "puti", "putis", "quillo", "quillos", "refri",
    "refris", "regu", "regus", "repe", "repes", "resi", "resis", "ridi", "ridis", "rotu", "rotus", "sado", "sados",
    "soco", "socos", "sufi", "sufis", "suje", "sujes", "tatu", "tatus", "torti", "tortis", "tranqui", "tranquis",
    "trici", "tricis", "ulti", "ultis", "urba", "urbas", "vice", "vices", "vitro", "vitros", "ñero", "ñeros");
  private static final List<String> PREFIX_WITH_WHITESPACE = Arrays.asList("ultra", "eco", "tele", "anti", "auto", "ex",
    "extra", "macro", "mega", "meta", "micro", "multi", "mono", "mini", "post", "retro", "semi", "super", "hiper",
    "trans", "re", "g", "l", "m");
  private static final List<String> PRONOMBRE_INICIAL = Arrays.asList("me", "te", "se", "nos", "os", "lo", "le", "la", "los",
    "las");
  private static final Pattern CAMEL_CASE = Pattern.compile("^(.\\p{Ll}+)(\\p{Lu}.+)$", Pattern.UNICODE_CASE);
  private static final List<String> PARTICULA_FINAL = Arrays.asList("que", "cual");
  private static final List<String> SPLIT_DIGITS_AT_END = Arrays.asList("en", "de", "del", "al", "a", "y", "o", "con");
  private static final Pattern VERB_INDSUBJ = Pattern.compile("V.[SI].*");
  private static final SpanishTagger tagger = SpanishTagger.INSTANCE;

  public MorfologikSpanishSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig,
      List<Language> altLanguages) throws IOException {
    super(messages, language, userConfig, altLanguages);
    this.setIgnoreTaggedWords();
  }

  @Override
  public String getFileName() {
    return "/es/es-ES.dict";
  }

  @Override
  public final String getId() {
    return "MORFOLOGIK_RULE_ES";
  }

  @Override
  // Use this rule in LO/OO extension despite being a spelling rule
  public boolean useInOffice() {
    return true;
  }

  @Override
  public List<String> getAdditionalSpellingFileNames() {
    return Arrays.asList("/es/"+ SpellingCheckRule.CUSTOM_SPELLING_FILE, SpellingCheckRule.GLOBAL_SPELLING_FILE,
      "/es/multiwords.txt");
  }

  @Override
  protected List<SuggestedReplacement> orderSuggestions(List<SuggestedReplacement> suggestions, String word) {
    List<SuggestedReplacement> newSuggestions = new ArrayList<>();
    String wordWithouDiacriticsString = StringTools.removeDiacritics(word);
    for (int i = 0; i < suggestions.size(); i++) {
      String replacement = suggestions.get(i).getReplacement().toLowerCase();
      if (REMOVE_FROM_SUGGESTIONS.contains(replacement)) {
        continue;
      }
      String parts[] = replacement.split(" ");
      // Don't change first suggestions if they match word without diacritics
      int posNewSugg = 0;
      while (newSuggestions.size() > posNewSugg
        && StringTools.removeDiacritics(newSuggestions.get(posNewSugg).getReplacement())
        .equalsIgnoreCase(wordWithouDiacriticsString)) {
        posNewSugg++;
      }
      if (parts.length == 2) {
        // remove wrong split prefixes
        if (parts[1].equals("s")) {
          continue;
        }
        if (PREFIX_WITH_WHITESPACE.contains(parts[0])) {
          continue;
        }
        // move some split words to first place
        if (parts[1].length() > 1 && PRONOMBRE_INICIAL.contains(parts[0].toLowerCase())) {
          String newSuggestion = parts[1];
          List<AnalyzedTokenReadings> atkn = tagger.tag(Arrays.asList(newSuggestion));
          if (atkn.get(0).matchesPosTagRegex(VERB_INDSUBJ)) {
            newSuggestions.add(posNewSugg, suggestions.get(i));
            continue;
          }
        }

        // move some split words to first place
        if (PARTICULA_FINAL.contains(parts[1])) {
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
    String[] parts = StringTools.splitCamelCase(word);
    if (parts.length > 1) {
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
      if (tagger.tag(Arrays.asList(parts[0])).get(0).isTagged()
        && (parts[0].length() > 2 || SPLIT_DIGITS_AT_END.contains(parts[0].toLowerCase()))) {
        return Collections.singletonList(String.join(" ",parts));
      }
    }
    return Collections.emptyList();
  }

  // Do not tokenize new words from spelling.txt...
  // Multi-token words should be in multiwords.txt
  protected boolean tokenizeNewWords() {
    return false;
  }

}

