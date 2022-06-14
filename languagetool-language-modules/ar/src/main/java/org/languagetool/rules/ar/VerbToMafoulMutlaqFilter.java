/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.ar;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.Arabic;
import org.languagetool.rules.AbstractSimpleReplaceRule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.rules.SimpleReplaceDataLoader;
import org.languagetool.tagging.ar.ArabicTagger;
import org.languagetool.synthesis.ar.ArabicSynthesizer;
import org.languagetool.tools.StringTools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

// constants
//import static org.languagetool.tools.ArabicConstants.FATHATAN;
import static org.languagetool.tools.ArabicConstants.TEH_MARBUTA;
//import static org.languagetool.tools.ArabicConstants.ALEF;
/**
 * Filter that maps suggestion from adverb to adjective.
 * Also see https://www.ef.com/wwen/english-resources/english-grammar/forming-adverbs-adjectives/
 * @since 4.9
 */
public class VerbToMafoulMutlaqFilter extends RuleFilter {

  private final ArabicTagger tagger = new ArabicTagger();
  private static final String FILE_NAME ="/ar/arabic_verb_masdar.txt";
  private final Map<String,List<String>> verb2masdarList = loadFromPath(FILE_NAME);
  private final ArabicSynthesizer synthesizer = new ArabicSynthesizer(new Arabic());


  private final Map<String,String> verb2masdar = new HashMap<String, String>() {{
    // tri letters verb:
    put("عَمِلَ", "عمل");
    put("أَعْمَلَ", "إعمال");
    put("عَمَّلَ", "تعميل");
//    put("ضَرَبَ", "ضرب");
    put("أَكَلَ", "أكل");
    put("سَأَلَ", "سؤال");
    // regular ones:
    // non tri letters verb
    put("أَجَابَ", "إجابة");

    //
    // TODO: add more Masdar verb
    //put("", "");
  }};

/*
  @Nullable
//  @Override
//  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) {
  public RuleMatch acceptRuleMatch2(RuleMatch match, Map<String, String> arguments, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) {
//    match.setSuggestedReplacement("Taha");
//    return match;

    String verb = arguments.get("verb");
    List<String> verbLemmas = tagger.getLemmas(patternTokens[0], "verb");
    String adj = arguments.get("adj");
    // generate multiple masdar from verb lemmas list
    List<String> inflectedMasdarList = new ArrayList<>();

    for(String lemma: verbLemmas) {
      String msdr = verb2masdar.get(lemma);
      if (msdr != null) {
        String inflectedMasdar = synthesizer.inflectMafoulMutlq(msdr);
        inflectedMasdarList.add(inflectedMasdar);
      }
      System.out.println("Actual masdar lists:"  +inflectedMasdarList.toString());

    }


    // only for debug
//    System.out.println("verb: "+verb);
//    System.out.println("verb Lemma: "+ verbLemmas.toString());
//    System.out.println("masdar Lemma: "+ masdarList.toString());
//    System.out.println("adj: "+adj);
//    System.out.println("masdar: "+masdar);
//    System.out.println("tokens: "+ Arrays.deepToString(patternTokens));

    String inflectedAdj = synthesizer.inflectAdjectiveTanwinNasb(adj);
    for( String  msdr: inflectedMasdarList)
    {
      match.addSuggestedReplacement(verb +" "+msdr + " " + inflectedAdj);
    }
    return match;
  }
*/


  @Nullable
//  @Override
//  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) {
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) {
//    match.setSuggestedReplacement("Taha");
//    return match;

    String verb = arguments.get("verb");
    List<String> verbLemmas = tagger.getLemmas(patternTokens[0], "verb");
    String adj = arguments.get("adj");
//    String masdar = verb2masdar.get(verb);
    // generate multiple masdar from verb lemmas list */

    List<String> inflectedMasdarList = new ArrayList<>();
    List<String> inflectedAdjList = new ArrayList<>();
    String inflectedAdjMasculine = synthesizer.inflectAdjectiveTanwinNasb(adj, false);
    String inflectedAdjfeminin = synthesizer.inflectAdjectiveTanwinNasb(adj, true);
    for(String lemma: verbLemmas)
    {
      // get sugegsted masdars lemmas
      List<String> msdrLemmaList = verb2masdarList.get(lemma);
      if(msdrLemmaList!=null) {
//        System.out.println("lemma:" + lemma +" masders: "+ msdrLemmaList.toString());

      for(String msdr:  msdrLemmaList) {
//        String msdr2 = msdrLemmaList.get(0);
        if (msdr != null) {
          String inflectedMasdar = synthesizer.inflectMafoulMutlq(msdr);
          inflectedMasdarList.add(inflectedMasdar);
          String inflectedAdj = (msdr.endsWith(Character.toString(TEH_MARBUTA))) ? inflectedAdjfeminin: inflectedAdjMasculine;
          inflectedAdjList.add(inflectedAdj);
        }
      }
      }
//      System.out.println("New masdar lists:" +inflectedMasdarList.toString());
    }

//    for( String  msdr: inflectedMasdarList)
//    {
//      System.out.println(verb +" "+msdr + " " + inflectedAdj);
//    }
    RuleMatch newMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(), match.getMessage(), match.getShortMessage());
    int i = 0;
    List<String> suggestionPhrases = new ArrayList<>();
    for( String  msdr: inflectedMasdarList)
    {
      String sugPhrase = verb +" "+msdr + " " + inflectedAdjList.get(i);
      // Avoid redendency
      if(!suggestionPhrases.contains(sugPhrase)) {
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
