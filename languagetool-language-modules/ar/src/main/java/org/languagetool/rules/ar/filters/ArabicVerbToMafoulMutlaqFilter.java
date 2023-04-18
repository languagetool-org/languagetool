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
import org.languagetool.language.Arabic;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.SimpleReplaceDataLoader;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.ar.ArabicSynthesizer;
import org.languagetool.tagging.ar.ArabicTagger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.languagetool.tools.ArabicConstants.TEH_MARBUTA;
//import static org.languagetool.tools.ArabicConstants.ALEF;

/**
 * Filter that maps suggestion from adverb to adjective.
 * Also see https://www.ef.com/wwen/english-resources/english-grammar/forming-adverbs-adjectives/
 *
 * @since 4.9
 */
public class ArabicVerbToMafoulMutlaqFilter extends RuleFilter {

  public ArabicVerbToMafoulMutlaqFilter() {
    this.verb2masdarList = loadFromPath(FILE_NAME);
  }

  private final ArabicTagger tagger = new ArabicTagger();
  private static final String FILE_NAME = "/ar/arabic_verb_masdar.txt";
  private Map<String, List<String>> verb2masdarList;
  private final ArabicSynthesizer synthesizer = new ArabicSynthesizer(new Arabic());


  private final Map<String, String> verb2masdar = new HashMap<String, String>() {{
    // tri letters verb:
    put("عَمِلَ", "عمل");
    put("أَعْمَلَ", "إعمال");
    put("عَمَّلَ", "تعميل");
    put("أَكَلَ", "أكل");
    put("سَأَلَ", "سؤال");
    // regular ones:
    // non tri letters verb
    put("أَجَابَ", "إجابة");
  }};

  @Nullable
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) {
    String verb = arguments.get("verb");
    List<String> verbLemmas = tagger.getLemmas(patternTokens[0], "verb");
    String adj = arguments.get("adj");

    // generate multiple masdar from verb lemmas list */

    List<String> inflectedMasdarList = new ArrayList<>();
    List<String> inflectedAdjList = new ArrayList<>();
    String inflectedAdjMasculine = synthesizer.inflectAdjectiveTanwinNasb(adj, false);
    String inflectedAdjfeminin = synthesizer.inflectAdjectiveTanwinNasb(adj, true);
    for (String lemma : verbLemmas) {
      // get sugegsted masdars lemmas
      List<String> msdrLemmaList = verb2masdarList.get(lemma);
      if (msdrLemmaList != null) {

        for (String msdr : msdrLemmaList) {
          if (msdr != null) {
            String inflectedMasdar = synthesizer.inflectMafoulMutlq(msdr);
            inflectedMasdarList.add(inflectedMasdar);
            String inflectedAdj = (msdr.endsWith(Character.toString(TEH_MARBUTA))) ? inflectedAdjfeminin : inflectedAdjMasculine;
            inflectedAdjList.add(inflectedAdj);
          }
        }
      }
    }
    RuleMatch newMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(), match.getMessage(), match.getShortMessage());
    int i = 0;
    List<String> suggestionPhrases = new ArrayList<>();
    for (String msdr : inflectedMasdarList) {
      String sugPhrase = verb + " " + msdr + " " + inflectedAdjList.get(i);
      // Avoid redundancy
      if (!suggestionPhrases.contains(sugPhrase)) {
        newMatch.addSuggestedReplacement(sugPhrase);
        suggestionPhrases.add(sugPhrase);
      }
      i++;
    }
    return newMatch;
  }


  protected static Map<String, List<String>> loadFromPath(String path) {
    return new SimpleReplaceDataLoader().loadWords(path);
  }
}
