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
import org.languagetool.language.Arabic;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.SimpleReplaceDataLoader;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.ar.ArabicSynthesizer;
import org.languagetool.tagging.ar.ArabicTagger;

import java.util.*;

/**
 * Filter that maps suggestion from adverb to adjective.
 * Also see https://www.ef.com/wwen/english-resources/english-grammar/forming-adverbs-adjectives/
 * @since 4.9
 */
public class MasdarToVerbFilter extends RuleFilter {

  private final ArabicTagger tagger = new ArabicTagger();
  private static final String FILE_NAME ="/ar/arabic_masdar_verb.txt";
  private final Map<String,List<String>> masdar2verbList = loadFromPath(FILE_NAME);
  private final ArabicSynthesizer synthesizer = new ArabicSynthesizer(new Arabic());

  final List<String> authorizeLemma = new ArrayList() {{
    add("قَامَ");
  }};

  private final Map<String,String> masdar2verb = new HashMap<String, String>() {{
    // tri letters verb:
    put( "عمل", "عَمِلَ");
    put("إعمال","أَعْمَلَ");
    put( "تعميل","عَمَّلَ");
    put( "ضرب","ضَرَبَ");
    put("أكل","أَكَلَ");
//    put("سؤال","سَأَلَ");
// regular ones:
// non tri letters verb
    put("إجابة","أَجَابَ");

    //
    // TODO: add more Masdar verb
    //put("", "");
  }};


  @Nullable
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) {

    //  The pattern is composed from to words
    // قام بالأكل
    // يقوم بالأكل
    // يقومون بالأكل
    // first token: auxialliary  verb Qam
    // second token: Noun as Masdar
    // replace the Masdar by its verb
    // inflect the verb according the auxilaiary verb inflection

    String auxVerb = arguments.get("verb"); // الفعل قام أو ما شابهه
    String masdar = arguments.get("noun");  // masdar

    // filter tokens which have a lemma
    // some cases can have multiple lemmas, but only auxilliry lemma are used
    List<String> auxVerbLemmasAll = tagger.getLemmas(patternTokens[0], "verb");
    List<String> auxVerbLemmas = filterLemmas(auxVerbLemmasAll);

    // get all lemmas of the given masdar
    List<String> masdarLemmas = tagger.getLemmas(patternTokens[1], "masdar");

    // generate multiple verb from masdar lemmas list
    List<String> verbList = new ArrayList<>();

    // if the auxiliary verb has many lemmas, filter authorized lemma only
    // the first token: auxiliary verb
    for(AnalyzedToken auxVerbToken : patternTokens[0]) {
      // if the token has an authorized lemma
      if(auxVerbLemmas.contains(auxVerbToken.getLemma())) {
      // for all masdar lemmas
        for (String lemma : masdarLemmas) {
          // get verb suitable to masdar
          String verb = masdar2verb.get(lemma);

//          if (verb != null) {
//            // if verb, inflect verd according to auxialiary verb inlfection
//            List<String> inflectedverbList = synthesizer.inflectLemmaLike(verb, auxVerbToken);
//            verbList.addAll(inflectedverbList);
//          }
          List<String> verblemmaList = masdar2verbList.get(lemma);
          if (verblemmaList != null) {
            // if verb, inflect verd according to auxialiary verb inlfection
            for(String vrbLem: verblemmaList) {
              List<String> inflectedverbList = synthesizer.inflectLemmaLike(vrbLem, auxVerbToken);
              verbList.addAll(inflectedverbList);
            }
          }
        }

      }
    }
    //
    // only for debug
//    System.out.println("Aux verb: "+auxVerb);
//    System.out.println("Aux verb list: "+auxVerbLemmas.toString());
//    System.out.println("masdar Lemma: "+ masdarLemmas.toString());
//    System.out.println("new verb Lemma: "+ verbList.toString());
//    System.out.println("masdar: "+masdar);
//    System.out.println("tokens: "+ Arrays.deepToString(patternTokens));

    // remove duplicates
    verbList = new ArrayList<>(new HashSet<>(verbList));


    RuleMatch newMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(), match.getMessage(), match.getShortMessage());
    // generate suggestion
    for(String  verb: verbList)
    {
      newMatch.addSuggestedReplacement(verb);
    }
    return newMatch;
//    return match;

//    match.setSuggestedReplacement("Taha");
//    return match;
  }

  List<String> filterLemmas(List<String> lemmas)
  {
    List<String> filtred = new ArrayList<>();

    for(String lem: authorizeLemma) {
      if (lemmas.contains(lem)) {
        filtred.add(lem);
      }
    }
    return filtred;
  }
  protected static Map<String, List<String>> loadFromPath(String path) {
    return new SimpleReplaceDataLoader().loadWords(path);
  }
}
