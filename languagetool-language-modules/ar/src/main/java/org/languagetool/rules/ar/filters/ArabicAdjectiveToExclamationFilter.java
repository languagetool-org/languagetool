/* LanguageTool, a natural language style checker
 * Copyright (C) 2022 Sohaib Afifi, Taha Zerrouki
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
package org.languagetool.rules.ar.filters;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.SimpleReplaceDataLoader;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.tagging.ar.ArabicTagger;
import org.languagetool.tools.ArabicWordMaps;

import java.util.*;

/**
 * Filter that maps suggestion from adverb to adjective.
 *
 * @since 5.8
 */
public class ArabicAdjectiveToExclamationFilter extends RuleFilter {

  public ArabicAdjectiveToExclamationFilter() {
    this.adj2compList = loadFromPath(FILE_NAME);
  }

  private final ArabicTagger tagger = new ArabicTagger();
  private static final String FILE_NAME = "/ar/arabic_adjective_exclamation.txt";
  private final Map<String, List<String>> adj2compList;

  private final Map<String, String> adj2comp = new HashMap<String, String>() {{
    // tri letters verb:
    put("رشيد", "أرشد");
    put("طويل", "أطول");
    put("بديع", "أبدع");
  }};


  @Nullable
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) {

    //  This rule return only the comparative according to given adjective
    String adj = arguments.get("adj"); // extract adjective
    String noun = arguments.get("noun"); // the second argument
    int adjTokenIndex;
    try {
      adjTokenIndex = Integer.parseInt(arguments.get("adj_pos")) - 1;
    } catch (NumberFormatException e) {
      throw new RuntimeException("Error parsing adj_pos from : " + arguments.get("adj_pos"), e);
    }

    // filter tokens which have a lemma of adjective

    // some cases can have multiple lemmas, but only adjective lemma are used
    List<String> adjLemmas = tagger.getLemmas(patternTokens[adjTokenIndex], "adj");

    // get comparative from Adj/comp list
    List<String> compList = new ArrayList<>();

    for (String adjLemma : adjLemmas) {
      // get comparative suitable to adjective
      List<String> comparativeList = adj2compList.get(adjLemma);
      if (comparativeList != null) {
        compList.addAll(comparativeList);
      }
    }

    // remove duplicates
    compList = new ArrayList<>(new HashSet<>(compList));
    RuleMatch newMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(), match.getMessage(), match.getShortMessage());
    // generate suggestion
    List<String> suggestionList = prepareSuggestions(compList, noun);
    for (String sug : suggestionList) {
      newMatch.addSuggestedReplacement(sug);
    }
    return newMatch;
  }

  /* prepare suggestion for a list of comparative */
  protected static List<String> prepareSuggestions(List<String> compList, String noun) {
    List<String> sugList = new ArrayList<>();
    for (String comp : compList) {
      sugList.addAll(prepareSuggestions(comp, noun));
    }
    return sugList;
  }

  protected static List<String> prepareSuggestions(String comp, String noun) {
    /*
    الحالات:
    الاسم ليس ضميرا


    ال   كم الولد جميل==> ما أجمل الولد
    أجمل بالولد

    حالة الضمير

    كم هو جميل==> ما أجمله
      أجمل به

    حالة الضفة غير الثلاثية
    اسم:
    كم الطالب شديد الاستيعاب
    ما أشد استيعاب الطالب
    أشدد باستيعابه

    ضمير
    كم هو شديد الاستيعاب
    ما أشد استيعابه
    أشد باستيعابه
     */

    List<String> sugList = new ArrayList<>();
    StringBuilder suggestion = new StringBuilder();
    suggestion.append(comp);
    if (noun == null || noun.isEmpty()) {
    } else if (isPronoun(noun)) {
      // no space adding
      suggestion.append(ArabicWordMaps.getAttachedPronoun(noun));
    } else {
      //if comparative is of second form don't add a space
      if (!comp.endsWith(" ب")) {
        suggestion.append(" ");
      }
      suggestion.append(noun);
    }

    // add suggestions
    sugList.add(suggestion.toString());
    return sugList;
  }

  /* test if the word is an isolated pronoun */
  private static boolean isPronoun(String word) {
    if (word == null) {
      return false;
    }
    return word.equals("هو")
      || word.equals("هي")
      || word.equals("هم")
      || word.equals("هما")
      || word.equals("أنا");
  }

  /* get corresponding attached to unattached pronoun */
  private static String getAttachedPronoun(String word) {
    if (word == null) {
      return "";
    }
    Map<String, String> isolatedToAttachedPronoun = new HashMap<>();
    isolatedToAttachedPronoun.put("هو", "ه");
    isolatedToAttachedPronoun.put("هي", "ها");
    isolatedToAttachedPronoun.put("هم", "هم");
    isolatedToAttachedPronoun.put("هن", "هن");
    isolatedToAttachedPronoun.put("نحن", "نا");
    return isolatedToAttachedPronoun.getOrDefault(word, "");
  }

  protected static Map<String, List<String>> loadFromPath(String path) {
    return new SimpleReplaceDataLoader().loadWords(path);
  }

  public static String getDataFilePath() {
    return FILE_NAME;
  }
}
