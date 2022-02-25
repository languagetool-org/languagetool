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
package org.languagetool.rules.en;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.languagemodel.BaseLanguageModel;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.Example;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.SuggestedReplacement;
import org.languagetool.rules.en.translation.BeoLingusTranslator;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;
import org.languagetool.rules.translation.Translator;
import org.languagetool.tagging.ner.NERService;
import org.languagetool.tools.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.languagetool.rules.SuggestedReplacement.topMatch;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public abstract class AbstractEnglishSpellerRule extends MorfologikSpellerRule {

  private static final Logger logger = LoggerFactory.getLogger(AbstractEnglishSpellerRule.class);
  //private static final EnglishSynthesizer synthesizer = (EnglishSynthesizer) Languages.getLanguageForShortCode("en").getSynthesizer();

  private final static Set<String> lcDoNotSuggestWords = new HashSet<>(Arrays.asList(
    // words with 'NOSUGGEST' in en_US.dic:
    "asshole",
    "assholes",
    "bullshit",
    "bullshitted",
    "bullshitter",
    "bullshitters",
    "bullshitting",
    "chickenshit",
    "chickenshits",
    "cocksucker",
    "cocksuckers",
    "coon",
    "coon ass",
    "coon asses",
    "cunt",
    "double check",
    "hard coded",
    "hands free",
    "faggot",
    "faggots",
    "fuck",
    "fucker",
    "fuckhead",
    "fuckheads",
    "horseshit",
    "kraut",
    "krauts",
    "motherfucker",
    "motherfuckers",
    "motherfucking",
    "nigga",
    "niggas",
    "niggaz",
    "negro",
    "nigger",
    "nigger lip",
    "nigger lips",
    "niggers",
    "shit",
    "shits",
    "shitfaced",
    "shithead",
    "shitheads",
    "shitload",
    "shitted",
    "shitting",
    "shitty",
    "wop",
    "wops",
    // extension:
    "niggard", "niggardly"
  ));
  
  private final BeoLingusTranslator translator;

  private static NERService nerPipe = null;
  
    
  
  private static final int maxPatterns = 9;
  private static final Pattern[] wordPatterns = new Pattern[maxPatterns];
  private static final String[] blogLinks = new String[maxPatterns];
  static  {
    wordPatterns[0] = Pattern.compile(".*[yi][zs]e(s|d)?|.*[yi][zs]ings?|.*i[zs]ations?", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    blogLinks[0] = "https://languagetool.org/insights/post/ise-ize/#the-distinctions-between-%E2%80%9C-ise%E2%80%9D-%E2%80%9C-ize%E2%80%9D-and-%E2%80%9C-yse%E2%80%9D-%E2%80%9C-yze%E2%80%9D";

    wordPatterns[1] = Pattern.compile(".*(defen[cs]e|offen[sc]e|preten[sc]e).*", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    blogLinks[1] = "https://languagetool.org/insights/post/ise-ize/#the-distinctions-between-%E2%80%9C-ise%E2%80%9D-%E2%80%9C-ize%E2%80%9D-and-%E2%80%9C-yse%E2%80%9D-%E2%80%9C-yze%E2%80%9D";

    wordPatterns[2] = Pattern.compile(".*og|.*ogue", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    blogLinks[2] = "https://languagetool.org/insights/post/ise-ize/#another-difference-because-of-foreign-words-%E2%80%9C-og%E2%80%9D-vs-%E2%80%9C-ogue%E2%80%9D";
    
    wordPatterns[3] = Pattern.compile(".*(or|our).*", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    blogLinks[3] = "https://languagetool.org/insights/post/our-or/#colour-or-color-%E2%80%94-colourise-or-colorize";

    wordPatterns[4] = Pattern.compile(".*e?able|.*dge?ments?|aging|ageing|ax|axe|.*grame?s?|neuron|neurone|neurons|neurones", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    blogLinks[4] = "https://languagetool.org/insights/post/our-or/#likeable-vs-likable-judgement-vs-judgment-oestrogen-vs-estrogen";
    
    wordPatterns[5] = Pattern.compile(".*(centre|center).*|.*(re|er)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    blogLinks[5] = "https://languagetool.org/insights/post/re-vs-er/#the-difference-of-%E2%80%9C-reer%E2%80%9D-at-the-center-of-attention";
   
    wordPatterns[6] = Pattern.compile("canceled|cancelled|canceling|cancelling|chili|chilli|chilies|chillies|chilis|chillis|counselor|counsellor|counselors|counsellors|defueled|defuelled|defueling|defuelling|defuelings|defuellings|dialed|dialled|dialer|dialler|dialers|diallers|dialing|dialling|dialog|dialogue|dialogize|dialogise|dialogized|dialogised|dialogizes|dialogises|dialogizing|dialogising|dialogs|dialogues|dialyzable|dialysable|dialyze|dialyse|dialyzed|dialysed|dialyzes|dialyses|dialyzing|dialysing|enroll|enrol|enrolled|enroled|enrolling|enroling|enrollment|enrolment|enrollments|enrolments|enrolls|enrols|fueled|fuelled|fueling|fuelling|fulfill|fulfil|fulfillment|fulfilment|fulfills|fulfils|installment|instalment|installments|instalments|jewelry|jewellery|labeled|labelled|labeling|labelling|marvelous|marvellous|medalist|medallist|medalists|medallists|modeled|modelled|modeling|modelling|noise-canceling|noise-cancelling|refueled|refuelled|refueling|refuelling|relabeled|relabelled|relabeling|relabelling|remodeled|remodelled|remodeling|remodelling|signalization|signalisation|signalize|signalise|signalized|signalised|signalizes|signalises|signalizing|signalising|skillful|skilful|skillfully|skilfully|tranquilize|tranquillize|tranquilized|tranquillized|tranquilizes|tranquillizes|traveled|travelled|traveler|traveller|travelers|travellers|traveling|travelling|uncanceled|uncancelled|uncanceling|uncancelling|unlabeled|unlabelled|wooly|woolly", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    blogLinks[6] = "https://languagetool.org/insights/post/re-vs-er/#british-english-prefers-doubling-consonants-doesn%E2%80%99t-it";
    
    wordPatterns[7] = Pattern.compile("airfoil|aerofoil|airfoils|aerofoils|airplane|aeroplane|airplanes|aeroplanes|aluminum|aluminium|artifact|artefact|artifacts|artefacts|backdraft|backdraught|cozy|cosy|", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    blogLinks[7] = "https://languagetool.org/insights/post/re-vs-er/#more-radical-differences-between-british-and-american-english-spellings";
    
    wordPatterns[8] = Pattern.compile("amenorrhea|amenorrhoea|anesthesia|anaesthesia|anesthesias|anaesthesias|anesthetic|anaesthetic|anesthetically|anaesthetically|anesthetics|anaesthetics|anesthetist|anaesthetist|anesthetists|anaesthetists|anesthetization|anaesthetisation|anesthetizations|anaesthetisations|anesthetize|anaesthetise|anesthetized|anaesthetised|anesthetizes|anaesthetises|anesthetizing|anaesthetising|archeological|archaeological|archeologically|archaeologically|archeologies|archaeologies|archeology|archaeology|cesium|caesium|diarrhea|diarrhoea|diarrheal|diarrhoeal|dyslipidemia|dyslipidaemia|dyslipidemias|dyslipidaemias|edematous|oedematous|encyclopedia|encyclopaedia|encyclopedias|encyclopaedias|eon|aeon|eons|aeons|esophagi|oesophagi|esophagus|oesophagus|esophaguses|oesophaguses|esthetic|aesthetic|esthetical|aesthetical|esthetically|aesthetically|esthetician|aesthetician|estheticians|aestheticians|estrogen|oestrogen|estrus|oestrus|etiologies|aetiologies|etiology|aetiology|feces|faeces|fetal|foetal|fetus|foetus|fetuses|foetuses|gastroesophageal|gastro-oesophageal|glycemic|glycaemic|gynecomastia|gynaecomastia|hematemesis|haematemesis|hematoma|haematoma|hematomas|haematomas|hematopoietic|haematopoietic|hematuria|haematuria|hematurias|haematurias|hemolytic|haemolytic|hemophilia|haemophilia|hemorrhage|haemorrhage|hemorrhages|haemorrhages|hemostasis|haemostasis|homeopathies|homoeopathies|homeopathy|homoeopathy|hyperemia|hyperaemia|hyperemic|hyperaemic|hypnopedia|hypnopaedia|hypnopedic|hypnopaedic|hypocalcaemia|hypocalcaemia|hypokalaemic|hypokalemic|kinesthesia|kinaesthesia|kinesthesis|kinaesthesis|kinesthetic|kinaesthetic|kinesthetically|kinaesthetically|maneuver|manoeuvre|maneuvers|manoeuvres|orthopedic|orthopaedic|orthopedics|orthopaedics|paleoecology|palaeoecology|paleogeographical|palaeogeographical|paleogeographically|palaeogeographically|paleogeography|palaeogeography|paresthesia|paraesthesia|pediatric|paediatric|pediatrically|paediatrically|pediatrician|paediatrician|pediatricians|paediatricians|pedomorphic|paedomorphic|pedophile|paedophile|pedophiles|paedophiles|polycythemia|polycythaemia|pretorium|praetorium|pyorrhea|pyorrhoea|septicemia|septicaemia|synesthesia|synaesthesia|synesthete|synaesthete|synesthetes|synaesthetes|tracheoesophageal|tracheo-oesophageal", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    blogLinks[8] = "https://languagetool.org/insights/post/our-or/#likeable-vs-likable-judgement-vs-judgment-oestrogen-vs-estrogen";
  }
  
  public AbstractEnglishSpellerRule(ResourceBundle messages, Language language) throws IOException {
    this(messages, language, null, Collections.emptyList());
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    RuleMatch[] matches = super.match(sentence);
    if (languageModel != null && languageModel instanceof BaseLanguageModel && nerPipe != null) {
      String sentenceText = sentence.getText();
      try {
        //long startTime = System.currentTimeMillis();
        if (sentenceText.length() <= 250) {
          List<NERService.Span> namedEntities = nerPipe.runNER(sentenceText);
          //System.out.println("namedEntities: " + namedEntities + ", matches before filter: " + matches.length);
          List<RuleMatch> filtered = filter(matches, sentenceText, namedEntities, languageModel);
          //System.out.println("matches after filter: " + filtered.size());
          matches = filtered.toArray(RuleMatch.EMPTY_ARRAY);
        }
      } catch (Exception e) {
        logger.warn("Could not run NER test on '" + sentenceText + "', will assume there are no named entities", e);
      }
    }
    
    // add custom URLs
    for (RuleMatch match : matches) {
      String misspelledWord = (String) match.getSentence().getText().subSequence(match.getFromPos(), match.getToPos());
      if (isValidInOtherVariant(misspelledWord) != null) {
        for (int i = 0; i < maxPatterns; i++) {
          Matcher m = wordPatterns[i].matcher(misspelledWord);
          if (m.matches()) {
            match.setUrl(new URL(blogLinks[i]));
            break;
          }
        }  
      }
      
    }
    
    return matches;
  }

  private List<RuleMatch> filter(RuleMatch[] matches, String sentenceText, List<NERService.Span> namedEntities, LanguageModel languageModel) {
    BaseLanguageModel lm = (BaseLanguageModel) languageModel;
    Set<RuleMatch> toFilter = new HashSet<>();
    for (NERService.Span neSpan : namedEntities) {
      for (RuleMatch match : matches) {
        //System.out.println(neSpan.getStart() + " <= " + match.getFromPos() + " && " + neSpan.getEnd() + " >= " +  match.getToPos());
        if (neSpan.getStart() <= match.getFromPos() && neSpan.getEnd() >= match.getToPos()) {
          String covered = sentenceText.substring(match.getFromPos(), match.getToPos());
          if (!StringTools.startsWithUppercase(covered)) {
            continue;
          }
          List<String> infos = new ArrayList<>();
          long textCount = lm.getCount(covered);
          //System.out.println(textCount + " for " + covered);
          infos.add(covered + "/" + textCount);
          String mostCommonRepl = null;
          long mostCommonReplCount = textCount;
          int i = 0;
          int nonZeroReplacements = 0;
          int lookupFailures = 0;
          int translations = 0;
          for (SuggestedReplacement repl : match.getSuggestedReplacementObjects()) {
            if (repl.getType() == SuggestedReplacement.SuggestionType.Translation) {
              translations++;
            }
            List<String> replList = Arrays.asList(repl.getReplacement().split(" "));
            if (replList.size() <= 3) {  // hard-coding 3grams is not good, but a base LM doesn't know about ngrams...
              long replCount = lm.getCount(replList);
              if (replCount > 0) {
                nonZeroReplacements++;
              }
              if (replCount > mostCommonReplCount) {
                mostCommonRepl = repl.getReplacement();
                mostCommonReplCount = replCount;
              }
              infos.add(repl + "/" + replCount);
            } else {
              lookupFailures++;
            }
            if (i++ >= 4) {
              break;
            }
          }
          //System.out.println("mostCommonRepl: "+  mostCommonRepl);
          if (translations == 0 && nonZeroReplacements == 0 && lookupFailures == 0) {  // e.g. "Fastow", which only offers "Fa stow" and "Fast ow"
            //System.out.println("Would skip, as no replacement was found with > 0 occurrences: " + covered + " " + match.getSuggestedReplacements());
            toFilter.add(match);
          } else if (translations == 0 && mostCommonRepl != null) {
            Integer dist = new LevenshteinDistance().apply(mostCommonRepl, covered);
            String msg = "Could skip: " + mostCommonRepl + " FOR " + covered + ", dist: " + dist;
            if (dist <= 2) {
              //System.out.println(msg + "\n -> Would not skip, common repl: " + mostCommonRepl + ": " + infos);
            } else {
              //System.out.println(msg + "\n -> Would skip, " + infos);
              toFilter.add(match);
            }
          }
        }
      }
    }
    List<RuleMatch> filteredMatches = new ArrayList<>();
    for (RuleMatch match : matches) {
      if (!toFilter.contains(match)) {
        filteredMatches.add(match);
      }
    }
    return filteredMatches;
  }

  /**
   * @since 4.4
   */
  public AbstractEnglishSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig, List<Language> altLanguages) throws IOException {
    this(messages, language, null, userConfig, altLanguages, null, null);
  }

  @Override
  protected List<SuggestedReplacement> filterNoSuggestWords(List<SuggestedReplacement> l) {
    return l.stream().filter(k -> !lcDoNotSuggestWords.contains(k.getReplacement().toLowerCase())).collect(Collectors.toList());
  }

  protected static Map<String,String> loadWordlist(String path, int column) {
    if (column != 0 && column != 1) {
      throw new IllegalArgumentException("Only column 0 and 1 are supported: " + column);
    }
    Map<String,String> words = new HashMap<>();
    List<String> lines = JLanguageTool.getDataBroker().getFromResourceDirAsLines(path);
    for (String line : lines) {
      line = line.trim();
      if (line.isEmpty() ||  line.startsWith("#")) {
        continue;
      }
      String[] parts = line.split(";");
      if (parts.length != 2) {
        throw new RuntimeException("Unexpected format in " + path + ": " + line + " - expected two parts delimited by ';'");
      }
      words.put(parts[column].toLowerCase(), parts[column == 1 ? 0 : 1]);
    }
    return words;
  }


  /**
   * @since 4.5
   * optional: language model for better suggestions
   */
  public AbstractEnglishSpellerRule(ResourceBundle messages, Language language, GlobalConfig globalConfig, UserConfig userConfig,
                                    List<Language> altLanguages, LanguageModel languageModel, Language motherTongue) throws IOException {
    super(messages, language, globalConfig, userConfig, altLanguages, languageModel, motherTongue);
    super.ignoreWordsWithLength = 1;
    setCheckCompound(true);
    addExamplePair(Example.wrong("This <marker>sentenc</marker> contains a spelling mistake."),
                   Example.fixed("This <marker>sentence</marker> contains a spelling mistake."));
    String languageSpecificIgnoreFile = getSpellingFileName().replace(".txt", "_" + language.getShortCodeWithCountryAndVariant() + ".txt");
    for (String ignoreWord : wordListLoader.loadWords(languageSpecificIgnoreFile)) {
      addIgnoreWords(ignoreWord);
    }
    translator = BeoLingusTranslator.getInstance(globalConfig);
    topSuggestions = getTopSuggestions();
    topSuggestionsIgnoreCase = getTopSuggestionsIgnoreCase();
    if (nerPipe == null && globalConfig != null && globalConfig.getNerUrl() != null) {
      nerPipe = new NERService(globalConfig.getNerUrl());
    }
  }

  @Override
  protected List<SuggestedReplacement> filterSuggestions(List<SuggestedReplacement> suggestions) {
    List<SuggestedReplacement> result = super.filterSuggestions(suggestions);
    List<SuggestedReplacement> clean = new ArrayList<>();
    for (SuggestedReplacement suggestion : result) {
      if (!suggestion.getReplacement().matches(".* (b|c|d|e|f|g|h|j|k|l|m|n|o|p|q|r|s|t|v|w|y|z|ll|ve)")) {  // e.g. 'timezones' suggests 'timezone s'
        clean.add(suggestion);
      }
    }
    return clean;
  }
  
  @Override
  protected List<RuleMatch> getRuleMatches(String word, int startPos, AnalyzedSentence sentence, List<RuleMatch> ruleMatchesSoFar, int idx, AnalyzedTokenReadings[] tokens) throws IOException {
    List<RuleMatch> ruleMatches = super.getRuleMatches(word, startPos, sentence, ruleMatchesSoFar, idx, tokens);
    if (ruleMatches.size() > 0) {
      // so 'word' is misspelled:
      IrregularForms forms = getIrregularFormsOrNull(word);
      if (forms != null) {
        String message = "Possible spelling mistake. Did you mean <suggestion>" + forms.forms.get(0) +
                "</suggestion>, the " + forms.formName + " form of the " + forms.posName +
                " '" + forms.baseform + "'?";
        addFormsToFirstMatch(message, sentence, ruleMatches, forms.forms);
      } else {
        VariantInfo variantInfo = isValidInOtherVariant(word);
        if (variantInfo != null) {
          String message = "Possible spelling mistake. '" + word + "' is " + variantInfo.getVariantName() + ".";
          String suggestion = StringTools.startsWithUppercase(word) ?
              StringTools.uppercaseFirstChar(variantInfo.otherVariant()) : variantInfo.otherVariant();
          replaceFormsOfFirstMatch(message, sentence, ruleMatches, suggestion);
        }
      }
    }
    // filter "re ..." (#2562):
    return ruleMatches.stream().map(m -> {
      RuleMatch copy = new RuleMatch(m);
      copy.setLazySuggestedReplacements(() -> cleanSuggestions(m));
      return copy;
    }).collect(Collectors.toList());
  }

  private static List<SuggestedReplacement> cleanSuggestions(RuleMatch ruleMatch) {
    return ruleMatch.getSuggestedReplacementObjects().stream()
      .filter(k -> !k.getReplacement().startsWith("re ") &&
                   !k.getReplacement().startsWith("en ") &&
                   !k.getReplacement().toLowerCase().startsWith("co ") &&
                   !k.getReplacement().toLowerCase().startsWith("de ") &&
                   !k.getReplacement().toLowerCase().startsWith("ex ") &&
                   !k.getReplacement().toLowerCase().startsWith("es ") &&
                   !k.getReplacement().toLowerCase().startsWith("mid ") &&
                   !k.getReplacement().toLowerCase().startsWith("non ") &&
                   !k.getReplacement().toLowerCase().startsWith("bio ") &&
                   !k.getReplacement().toLowerCase().startsWith("bi ") &&
                   !k.getReplacement().toLowerCase().startsWith("con ") &&
                   !k.getReplacement().startsWith("ins ") && // instable (ins table)
                   !k.getReplacement().toLowerCase().startsWith("pre ") &&
                   !k.getReplacement().toLowerCase().startsWith("inter ") &&
                   !k.getReplacement().toLowerCase().startsWith("multi ") &&
                   !k.getReplacement().toLowerCase().startsWith("retro ") &&
                   !k.getReplacement().toLowerCase().startsWith("extra ") &&
                   !k.getReplacement().toLowerCase().startsWith("meta ") &&
                   !k.getReplacement().toLowerCase().startsWith("uni ") &&
                   !k.getReplacement().toLowerCase().startsWith("anti ") &&
                   !k.getReplacement().toLowerCase().startsWith("necro ") &&
                   !k.getReplacement().toLowerCase().startsWith("photo ") &&
                   !k.getReplacement().toLowerCase().startsWith("post ") &&
                   !k.getReplacement().toLowerCase().startsWith("sub ") &&
                   !k.getReplacement().toLowerCase().startsWith("auto ") &&
                   !k.getReplacement().startsWith("sh ") &&
                   !k.getReplacement().startsWith("li ") &&
                   !k.getReplacement().startsWith("ha ") &&
                   !k.getReplacement().startsWith("st ") &&
                   !k.getReplacement().toLowerCase().startsWith("dis ") &&
                   !k.getReplacement().toLowerCase().startsWith("mono ") &&
                   !k.getReplacement().toLowerCase().startsWith("trans ") &&
                   !k.getReplacement().toLowerCase().startsWith("neuro ") &&
                   !k.getReplacement().toLowerCase().startsWith("ultra ") &&
                   !k.getReplacement().toLowerCase().startsWith("mini ") &&
                   !k.getReplacement().toLowerCase().startsWith("hyper ") &&
                   !k.getReplacement().toLowerCase().startsWith("micro ") &&
                   !k.getReplacement().toLowerCase().startsWith("counter ") &&
                   !k.getReplacement().toLowerCase().startsWith("cyber ") &&
                   !k.getReplacement().toLowerCase().startsWith("hydro ") &&
                   !k.getReplacement().toLowerCase().startsWith("ergo ") &&
                   !k.getReplacement().toLowerCase().startsWith("fore ") &&
                   !k.getReplacement().toLowerCase().startsWith("geo ") &&
                   !k.getReplacement().toLowerCase().startsWith("pro ") &&
                   !k.getReplacement().toLowerCase().startsWith("pseudo ") &&
                   !k.getReplacement().toLowerCase().startsWith("psycho ") &&
                   !k.getReplacement().toLowerCase().startsWith("nano ") &&
                   !k.getReplacement().toLowerCase().startsWith("ans ") &&
                   !k.getReplacement().toLowerCase().startsWith("semi ") &&
                   !k.getReplacement().toLowerCase().startsWith("infra ") &&
                   !k.getReplacement().toLowerCase().startsWith("hypo ") &&
                   !k.getReplacement().toLowerCase().startsWith("lo ") &&
                   !k.getReplacement().toLowerCase().startsWith("ed ") &&
                   !k.getReplacement().toLowerCase().startsWith("ac ") &&
                   !k.getReplacement().toLowerCase().startsWith("al ") &&
                   !k.getReplacement().toLowerCase().startsWith("ea ") &&
                   !k.getReplacement().toLowerCase().startsWith("ge ") &&
                   !k.getReplacement().toLowerCase().startsWith("mu ") &&
                   !k.getReplacement().toLowerCase().startsWith("ma ") &&
                   !k.getReplacement().toLowerCase().startsWith("la ") &&
                   !k.getReplacement().toLowerCase().startsWith("bis ") &&
                   !k.getReplacement().toLowerCase().startsWith("tar ") &&
                   !k.getReplacement().toLowerCase().startsWith("f ") &&
                   !k.getReplacement().toLowerCase().startsWith("k ") &&
                   !k.getReplacement().toLowerCase().startsWith("e ") &&
                   !k.getReplacement().toLowerCase().startsWith("c ") &&
                   !k.getReplacement().toLowerCase().startsWith("p ") &&
                   !k.getReplacement().toLowerCase().startsWith("v ") &&
                   !k.getReplacement().toLowerCase().startsWith("s ") &&
                   !k.getReplacement().toLowerCase().startsWith("h ") &&
                   !k.getReplacement().toLowerCase().startsWith("r ") &&
                   !k.getReplacement().toLowerCase().startsWith("s ") &&
                   !k.getReplacement().toLowerCase().startsWith("t ") &&
                   !k.getReplacement().toLowerCase().startsWith("um ") &&
                   !k.getReplacement().toLowerCase().startsWith("oft ") &&
                   !k.getReplacement().endsWith(" i") &&
                   !k.getReplacement().endsWith(" able") &&
                   !k.getReplacement().endsWith(" less") && // (e.g. permissionless)
                   !k.getReplacement().endsWith(" sly") && // unnecessary suggestion (e.g. for continuesly)
                   !k.getReplacement().endsWith(" OO") && // unnecessary suggestion (e.g. for "HELLOOO")
                   !k.getReplacement().endsWith(" HHH") && // unnecessary suggestion (e.g. for "OHHHH")
                   !k.getReplacement().endsWith(" ally") && // adverbs ending in "ally" that LT doesn't know (yet)
                   !k.getReplacement().endsWith(" ize") && // "advertize"
                   !k.getReplacement().endsWith(" sh") &&
                   !k.getReplacement().endsWith(" st") &&
                   !k.getReplacement().endsWith(" ward") &&
                   !k.getReplacement().endsWith(" ability") && // interruptability
                   !k.getReplacement().endsWith(" ware") && // drinkware
                   !k.getReplacement().endsWith(" logy") && // volcanology
                   !k.getReplacement().endsWith(" ting") && // someting
                   !k.getReplacement().endsWith(" ion") && // presention
                   !k.getReplacement().endsWith(" ions") && // sealions
                   !k.getReplacement().endsWith(" cal") &&
                   !k.getReplacement().endsWith(" ted") && // "bursted"
                   !k.getReplacement().endsWith(" sphere") &&
                   !k.getReplacement().endsWith(" ell") &&
                   !k.getReplacement().endsWith(" con") &&
                   !k.getReplacement().endsWith(" sis") &&
                   !k.getReplacement().endsWith(" like") && // "ribbonlike"
                   !k.getReplacement().endsWith(" full") && // do not suggest "use full" for "useful"
                   !k.getReplacement().endsWith(" en") && // "Antwerpen" suggests "Antwerp en"
                   !k.getReplacement().endsWith(" ne") &&
                   !k.getReplacement().endsWith(" ed") &&
                   !k.getReplacement().endsWith(" al") &&
                   !k.getReplacement().endsWith(" ans") &&
                   !k.getReplacement().endsWith(" mans") &&
                   !k.getReplacement().endsWith(" ti") &&
                   !k.getReplacement().endsWith(" de") &&
                   !k.getReplacement().endsWith(" ea") &&
                   !k.getReplacement().endsWith(" ge") &&
                   !k.getReplacement().endsWith(" tar") &&
                   !k.getReplacement().endsWith(" re") &&
                   !k.getReplacement().endsWith(" e") &&
                   !k.getReplacement().endsWith(" c") &&
                   !k.getReplacement().endsWith(" v") &&
                   !k.getReplacement().endsWith(" h") &&
                   !k.getReplacement().endsWith(" s") &&
                   !k.getReplacement().endsWith(" r") &&
                   !k.getReplacement().endsWith(" um") &&
                   !k.getReplacement().endsWith(" er") &&
                   !k.getReplacement().endsWith(" es") &&
                   !k.getReplacement().endsWith(" ex") &&
                   !k.getReplacement().endsWith(" na") &&
                   !k.getReplacement().endsWith(" gs") &&
                   !k.getReplacement().endsWith(" don") &&
                   !k.getReplacement().endsWith(" dons") &&
                   !k.getReplacement().endsWith(" la") &&
                   !k.getReplacement().endsWith(" ism") &&
                   !k.getReplacement().endsWith(" ma"))
      .collect(Collectors.toList());
  }

  /**
   * @since 4.5
   */
  @Nullable
  protected VariantInfo isValidInOtherVariant(String word) {
    return null;
  }

  private void addFormsToFirstMatch(String message, AnalyzedSentence sentence, List<RuleMatch> ruleMatches, List<String> forms) {
    // recreating match, might overwrite information by SuggestionsRanker;
    // this has precedence
    RuleMatch oldMatch = ruleMatches.get(0);
    RuleMatch newMatch = new RuleMatch(this, sentence, oldMatch.getFromPos(), oldMatch.getToPos(), message);
    newMatch.setLazySuggestedReplacements(() -> new ArrayList<>(Sets.newLinkedHashSet(Iterables.concat(
      Iterables.transform(forms, SuggestedReplacement::new),
      oldMatch.getSuggestedReplacementObjects()
    ))));
    ruleMatches.set(0, newMatch);
  }

  private void replaceFormsOfFirstMatch(String message, AnalyzedSentence sentence, List<RuleMatch> ruleMatches, String suggestion) {
    // recreating match, might overwrite information by SuggestionsRanker;
    // this has precedence
    RuleMatch oldMatch = ruleMatches.get(0);
    RuleMatch newMatch = new RuleMatch(this, sentence, oldMatch.getFromPos(), oldMatch.getToPos(), message);
    SuggestedReplacement sugg = new SuggestedReplacement(suggestion);
    sugg.setShortDescription(language.getName());
    newMatch.setSuggestedReplacementObjects(Collections.singletonList(sugg));
    ruleMatches.set(0, newMatch);
  }

  @SuppressWarnings({"ReuseOfLocalVariable", "ControlFlowStatementWithoutBraces"})
  @Nullable
  private IrregularForms getIrregularFormsOrNull(String word) {
    IrregularForms irregularFormsOrNull = getIrregularFormsOrNull(word, "ed", Arrays.asList("ed"), "VBD", "verb", "past tense");
    if (irregularFormsOrNull != null) return irregularFormsOrNull;
    irregularFormsOrNull = getIrregularFormsOrNull(word, "ed", Arrays.asList("d" /* e.g. awaked */), "VBD", "verb", "past tense");
    if (irregularFormsOrNull != null) return irregularFormsOrNull;
    irregularFormsOrNull = getIrregularFormsOrNull(word, "s", Arrays.asList("s"), "NNS", "noun", "plural");
    if (irregularFormsOrNull != null) return irregularFormsOrNull;
    irregularFormsOrNull = getIrregularFormsOrNull(word, "es", Arrays.asList("es"/* e.g. 'analysises' */), "NNS", "noun", "plural");
    if (irregularFormsOrNull != null) return irregularFormsOrNull;
    irregularFormsOrNull = getIrregularFormsOrNull(word, "er", Arrays.asList("er"/* e.g. 'farer' */), "JJR", "adjective", "comparative");
    if (irregularFormsOrNull != null) return irregularFormsOrNull;
    irregularFormsOrNull = getIrregularFormsOrNull(word, "est", Arrays.asList("est"/* e.g. 'farest' */), "JJS", "adjective", "superlative");
    return irregularFormsOrNull;
  }

  @Nullable
  private IrregularForms getIrregularFormsOrNull(String word, String wordSuffix, List<String> suffixes, String posTag, String posName, String formName) {
    try {
      for (String suffix : suffixes) {
        if (word.endsWith(wordSuffix)) {
          String baseForm = word.substring(0, word.length() - suffix.length());
          String[] forms = Objects.requireNonNull(language.getSynthesizer()).synthesize(new AnalyzedToken(word, null, baseForm), posTag);
          List<String> result = new ArrayList<>();
          for (String form : forms) {
            if (!speller1.isMisspelled(form)) {
              // only accept suggestions that the spellchecker will accept
              result.add(form);
            }
          }
          // the internal dict might contain forms that the spell checker doesn't accept (e.g. 'criterions'),
          // but we trust the spell checker in this case:
          result.remove(word);
          result.remove("badder");  // non-standard usage
          result.remove("baddest");  // non-standard usage
          result.remove("spake");  // can be removed after dict update
          if (result.size() > 0) {
            return new IrregularForms(baseForm, posName, formName, result);
          }
        }
      }
      return null;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected final Map<String, List<String>> topSuggestions;
  protected final Map<String, List<String>> topSuggestionsIgnoreCase;

  protected static Map<String, List<String>> getTopSuggestionsIgnoreCase() {
    Map<String, List<String>> s = new HashMap<>();
    s.put("json", Arrays.asList("Jason"));
    s.put("bmps", Arrays.asList("BMPs"));
    s.put("ppts", Arrays.asList("PPTs"));

    s.put("italia", Arrays.asList("Italy"));
    s.put("copenhague", Arrays.asList("Copenhagen"));
    s.put("applepay", Arrays.asList("Apple Pay"));
    s.put("&&", Arrays.asList("&"));
    s.put("wensday", Arrays.asList("Wednesday"));
    s.put("interweb", Arrays.asList("internet"));
    s.put("interwebs", Arrays.asList("internet"));
    s.put("srilanka", Arrays.asList("Sri Lanka"));
    s.put("pcs", Arrays.asList("PCs"));

    s.put("oconnor", Arrays.asList("O'Connor"));
    s.put("oconor", Arrays.asList("O'Conor"));
    s.put("obrien", Arrays.asList("O'Brien"));
    s.put("odonnell", Arrays.asList("O'Donnell"));
    s.put("oneill", Arrays.asList("O'Neill"));
    s.put("oneil", Arrays.asList("O'Neil"));
    s.put("oconnell", Arrays.asList("O'Connell"));
    s.put("todo", Arrays.asList("To-do", "To do"));
    s.put("todos", Arrays.asList("To-dos"));
    s.put("elearning", Arrays.asList("e-learning"));
    s.put("g-mail", Arrays.asList("Gmail"));
    s.put("playstation", Arrays.asList("PlayStation"));
    s.put("iam", Arrays.asList("I am", "I'm"));
    s.put("kpop", Arrays.asList("K-pop"));
    s.put("trumpian", Arrays.asList("Trumpist"));
    s.put("trumpians", Arrays.asList("Trumpists"));
    s.put("UberEats", Arrays.asList("Uber Eats"));
    s.put("KWH", Arrays.asList("kWh"));
    s.put("MWH", Arrays.asList("MWh"));
    s.put("xray", Arrays.asList("X-ray"));
    s.put("xrays", Arrays.asList("X-rays"));
    return s;
  }

  protected static Map<String, List<String>> getTopSuggestions() {
    Map<String, List<String>> s = new HashMap<>();
    s.put("retd", Arrays.asList("retd.", "retired"));
    s.put("Retd", Arrays.asList("Retd.", "Retired"));
    s.put("intransparent", Arrays.asList("non-transparent", "lacking transparency"));
    s.put("repetion", Arrays.asList("repetition"));
    s.put("Repetion", Arrays.asList("Repetition"));
    s.put("repetions", Arrays.asList("repetitions"));
    s.put("Repetions", Arrays.asList("Repetitions"));
    s.put("rom", Arrays.asList("room"));
    s.put("abt", Arrays.asList("about"));
    s.put("becuz", Arrays.asList("because"));
    s.put("becus", Arrays.asList("because"));
    s.put("lullabys", Arrays.asList("lullabies"));
    s.put("Lullabys", Arrays.asList("Lullabies"));
    s.put("forceably", Arrays.asList("forcibly"));
    s.put("Forceably", Arrays.asList("Forcibly"));
    s.put("accidently", Arrays.asList("accidentally"));
    s.put("Accidently", Arrays.asList("Accidentally"));
    s.put("aer", Arrays.asList("are", "air"));
    s.put("Aer", Arrays.asList("Are", "Air"));
    s.put("downie", Arrays.asList("downy"));
    s.put("Downie", Arrays.asList("Downy"));
    s.put("happing", Arrays.asList("happening"));
    s.put("Happing", Arrays.asList("Happening"));
    s.put("se", Arrays.asList("see"));
    s.put("maam", Arrays.asList("ma'am", "madam"));
    s.put("Maam", Arrays.asList("Ma'am", "Madam"));
    s.put("mam", Arrays.asList("ma'am"));
    s.put("Mam", Arrays.asList("Ma'am"));
    s.put("evidents", Arrays.asList("evidence"));
    s.put("Evidents", Arrays.asList("Evidence"));
    s.put("greatfull", Arrays.asList("grateful"));
    s.put("Greatfull", Arrays.asList("Grateful"));
    s.put("verticle", Arrays.asList("vertical"));
    s.put("Verticle", Arrays.asList("Vertical"));
    s.put("definaly", Arrays.asList("definitely"));
    s.put("Definaly", Arrays.asList("Definitely"));
    s.put("definally", Arrays.asList("definitely"));
    s.put("Definally", Arrays.asList("Definitely"));
    s.put("handable", Arrays.asList("handleable"));
    s.put("Handable", Arrays.asList("Handleable"));
    s.put("Tuffed", Arrays.asList("Toughed"));
    s.put("tuffed", Arrays.asList("toughed"));
    s.put("Tuffest", Arrays.asList("Toughest"));
    s.put("tuffest", Arrays.asList("toughest"));
    s.put("Tuffer", Arrays.asList("Tougher"));
    s.put("tuffer", Arrays.asList("tougher"));
    s.put("Fundrace", Arrays.asList("Fundraise"));
    s.put("fundrace", Arrays.asList("fundraise"));
    s.put("Fundraces", Arrays.asList("Fundraises"));
    s.put("fundraces", Arrays.asList("fundraises"));
    s.put("Fundracing", Arrays.asList("Fundraising"));
    s.put("fundracing", Arrays.asList("fundraising"));
    s.put("Fundraced", Arrays.asList("Fundraised"));
    s.put("fundraced", Arrays.asList("fundraised"));
    s.put("hollyday", Arrays.asList("holiday"));
    s.put("Hollyday", Arrays.asList("Holiday"));
    s.put("hollydays", Arrays.asList("holidays"));
    s.put("Hollydays", Arrays.asList("Holidays"));
    s.put("overnite", Arrays.asList("overnight"));
    s.put("Overnite", Arrays.asList("Overnight"));
    s.put("tonite", Arrays.asList("tonight"));
    s.put("Tonite", Arrays.asList("Tonight"));
    s.put("nite", Arrays.asList("night"));
    s.put("Nite", Arrays.asList("Night"));
    s.put("nites", Arrays.asList("nights"));
    s.put("Nites", Arrays.asList("Nights"));
    s.put("upto", Arrays.asList("up to", "unto"));
    s.put("Upto", Arrays.asList("Up to", "Unto"));
    s.put("prolly", Arrays.asList("probably"));
    s.put("corse", Arrays.asList("course"));
    s.put("util", Arrays.asList("utility"));
    s.put("Util", Arrays.asList("Utility"));
    s.put("prorata", Arrays.asList("pro rata"));
    s.put("pro-rata", Arrays.asList("pro rata"));
    s.put("Prorata", Arrays.asList("Pro rata"));
    s.put("Pro-rata", Arrays.asList("Pro rata"));
    s.put("wast", Arrays.asList("was", "waste", "waist", "wasn't"));
    s.put("Wast", Arrays.asList("Was", "Waste", "Waist", "Wasn't"));
    s.put("drag&drop", Arrays.asList("drag & drop"));
    s.put("Drag&drop", Arrays.asList("Drag & drop"));
    s.put("Drag&Drop", Arrays.asList("Drag & Drop"));
    s.put("copy&paste", Arrays.asList("copy & paste"));
    s.put("Copy&paste", Arrays.asList("Copy & paste"));
    s.put("Copy&Paste", Arrays.asList("Copy & Paste"));
    s.put("hiphop", Arrays.asList("hip-hop"));
    s.put("Hiphop", Arrays.asList("Hip-hop", "Hip-Hop"));
    s.put("ap", Arrays.asList("app", "up", "AP"));
    s.put("Ap", Arrays.asList("App", "Up", "AP"));
    s.put("aps", Arrays.asList("apps"));
    s.put("Aps", Arrays.asList("Apps"));
    s.put("hehe", Arrays.asList("he-he"));
    s.put("Hehe", Arrays.asList("He-he"));
    s.put("defacto", Arrays.asList("de facto"));
    s.put("Defacto", Arrays.asList("De facto"));
    s.put("differently-abled", Arrays.asList("differently abled"));
    s.put("Differently-abled", Arrays.asList("Differently abled"));
    s.put("data-uri", Arrays.asList("data URI"));
    s.put("Ppl", Arrays.asList("People"));
    s.put("Si", Arrays.asList("Is"));
    s.put("si", Arrays.asList("is"));
    s.put("pcs", Arrays.asList("PCs"));
    s.put("constits", Arrays.asList("consists"));
    s.put("ect", Arrays.asList("etc"));
    s.put("fastly", Arrays.asList("fast"));
    s.put("carrie", Arrays.asList("carry"));
    s.put("bare-bone", Arrays.asList("bare-bones", "bare-boned"));
    s.put("Bare-bone", Arrays.asList("Bare-bones", "Bare-boned"));
    s.put("mae", Arrays.asList("make", "MAE", "may", "May", "man"));
    s.put("transportion", Arrays.asList("transportation"));
    s.put("Transportion", Arrays.asList("Transportation"));
    s.put("presention", Arrays.asList("presentation"));
    s.put("Presention", Arrays.asList("Presentation"));
    s.put("presension", Arrays.asList("presentation"));
    s.put("Presension", Arrays.asList("Presentation"));
    s.put("realtime", Arrays.asList("real-time"));
    s.put("Realtime", Arrays.asList("Real-time"));
    s.put("morphium", Arrays.asList("morphine"));
    s.put("Morphium", Arrays.asList("Morphine"));
    s.put("morphiums", Arrays.asList("morphines"));
    s.put("Morphiums", Arrays.asList("Morphines"));
    s.put("approvement", Arrays.asList("approval"));
    s.put("Approvement", Arrays.asList("Approval"));
    s.put("approvements", Arrays.asList("approvals"));
    s.put("Approvements", Arrays.asList("Approvals"));
    s.put("ciggarets", Arrays.asList("cigarettes"));
    s.put("Ciggarets", Arrays.asList("Cigarettes"));
    s.put("pro-bono", Arrays.asList("pro bono"));
    s.put("Pro-bono", Arrays.asList("Pro bono"));
    s.put("probono", Arrays.asList("pro bono"));
    s.put("Probono", Arrays.asList("Pro bono"));
    s.put("electronical", Arrays.asList("electronic", "electronically"));
    s.put("Electronical", Arrays.asList("Electronic", "Electronically"));
    s.put("unpolite", Arrays.asList("impolite"));
    s.put("armys", Arrays.asList("armies"));
    s.put("Armys", Arrays.asList("Armies"));
    s.put("tarrif", Arrays.asList("tariff"));
    s.put("Tarrif", Arrays.asList("Tariff"));
    s.put("tarrifs", Arrays.asList("tariffs"));
    s.put("Tarrifs", Arrays.asList("Tariffs"));
    s.put("techy", Arrays.asList("techie"));
    s.put("Techy", Arrays.asList("Techie"));
    s.put("techys", Arrays.asList("techies"));
    s.put("Techys", Arrays.asList("Techies"));
    s.put("non-techy", Arrays.asList("non-techie"));
    s.put("pharmasuitable", Arrays.asList("pharmaceutical"));
    s.put("storie", Arrays.asList("story", "store", "stories"));
    s.put("Storie", Arrays.asList("Story", "Store", "Stories"));
    s.put("ensuite", Arrays.asList("en suite"));
    s.put("Ensuite", Arrays.asList("En suite"));
    s.put("insite", Arrays.asList("insight"));
    s.put("insites", Arrays.asList("insights"));
    s.put("Insite", Arrays.asList("Insight"));
    s.put("Insites", Arrays.asList("Insights"));
    s.put("thouraly", Arrays.asList("thoroughly"));
    s.put("heiaracky", Arrays.asList("hierarchy"));
    s.put("on-prem", Arrays.asList("on-premise"));
    s.put("sin-off", Arrays.asList("sign-off"));
    s.put("sin-offs", Arrays.asList("sign-offs"));
    s.put("Sin-off", Arrays.asList("Sign-off"));
    s.put("Sin-offs", Arrays.asList("Sign-offs"));
    s.put("Alot", Arrays.asList("A lot"));
    s.put("alot", Arrays.asList("a lot"));
    s.put("DDOS", Arrays.asList("DDoS"));
    s.put("disencouraged", Arrays.asList("discouraged"));
    s.put("Disencouraged", Arrays.asList("Discouraged"));
    s.put("async", Arrays.asList("asynchronous", "asynchronously"));
    s.put("Async", Arrays.asList("Asynchronous", "Asynchronously"));
    s.put("endevours", Arrays.asList("endeavours"));
    s.put("endevors", Arrays.asList("endeavors"));
    s.put("endevour", Arrays.asList("endeavour"));
    s.put("endevor", Arrays.asList("endeavor"));
    s.put("industrys", Arrays.asList("industries", "industry's", "industry"));
    s.put("Industrys", Arrays.asList("Industries", "Industry's", "Industry"));
    s.put("partys", Arrays.asList("parties", "party's", "party"));
    s.put("Partys", Arrays.asList("Parties", "Party's", "Party"));
    s.put("countrys", Arrays.asList("countries", "country's", "country"));
    s.put("Countrys", Arrays.asList("Countries", "Country's", "Country"));
    s.put("bodys", Arrays.asList("bodies", "body's", "body"));
    s.put("Bodys", Arrays.asList("Bodies", "Body's", "Body"));
    s.put("ladys", Arrays.asList("ladies", "lady's", "lady"));
    s.put("Ladys", Arrays.asList("Ladies", "Lady's", "Lady"));
    s.put("citys", Arrays.asList("cities", "city's", "city"));
    s.put("Citys", Arrays.asList("Cities", "City's", "City"));
    s.put("batterys", Arrays.asList("batteries", "battery's", "battery"));
    s.put("Batterys", Arrays.asList("Batteries", "Battery's", "Battery"));
    s.put("technologys", Arrays.asList("technologies", "technology's", "technology"));
    s.put("Technologys", Arrays.asList("Technologies", "Technology's", "Technology"));
    s.put("childrens", Arrays.asList("children's", "children"));
    s.put("Childrens", Arrays.asList("Children's", "Children"));
    s.put("countys", Arrays.asList("counties", "county's", "county"));
    s.put("Countys", Arrays.asList("Counties", "County's", "County"));
    s.put("familys", Arrays.asList("families", "family's", "family"));
    s.put("Familys", Arrays.asList("Families", "Family's", "Family"));
    s.put("dieing", Arrays.asList("dying"));
    s.put("Dieing", Arrays.asList("Dying"));
    s.put("dem", Arrays.asList("them"));
    s.put("Dem", Arrays.asList("Them"));
    s.put("infact", Arrays.asList("in fact"));
    s.put("Infact", Arrays.asList("In fact"));
    s.put("ad-hoc", Arrays.asList("ad hoc"));
    s.put("adhoc", Arrays.asList("ad hoc"));
    s.put("Ad-hoc", Arrays.asList("Ad hoc"));
    s.put("Adhoc", Arrays.asList("Ad hoc"));
    s.put("ad-on", Arrays.asList("add-on"));
    s.put("add-o", Arrays.asList("add-on"));
    s.put("acc", Arrays.asList("account", "accusative"));
    s.put("Acc", Arrays.asList("Account", "Accusative"));
    s.put("ºC", Arrays.asList("°C"));
    s.put("jus", Arrays.asList("just", "juice"));
    s.put("Jus", Arrays.asList("Just", "Juice"));
    s.put("sayed", Arrays.asList("said"));
    s.put("sess", Arrays.asList("says", "session", "cess"));
    s.put("Addon", Arrays.asList("Add-on"));
    s.put("Addons", Arrays.asList("Add-ons"));
    s.put("ios", Arrays.asList("iOS"));
    s.put("yrs", Arrays.asList("years"));
    s.put("standup", Arrays.asList("stand-up"));
    s.put("standups", Arrays.asList("stand-ups"));
    s.put("Standup", Arrays.asList("Stand-up"));
    s.put("Standups", Arrays.asList("Stand-ups"));
    s.put("Playdough", Arrays.asList("Play-Doh"));
    s.put("playdough", Arrays.asList("Play-Doh"));
    s.put("biggy", Arrays.asList("biggie"));
    s.put("lieing", Arrays.asList("lying"));
    s.put("preffered", Arrays.asList("preferred"));
    s.put("preffering", Arrays.asList("preferring"));
    s.put("reffered", Arrays.asList("referred"));
    s.put("reffering", Arrays.asList("referring"));
    s.put("passthrough", Arrays.asList("pass-through"));
    s.put("&&", Arrays.asList("&"));
    s.put("cmon", Arrays.asList("c'mon"));
    s.put("Cmon", Arrays.asList("C'mon"));
    s.put("da", Arrays.asList("the"));
    s.put("Da", Arrays.asList("The"));
    s.put("Vue", Arrays.asList("Vue.JS"));
    s.put("errornous", Arrays.asList("erroneous"));
    s.put("brang", Arrays.asList("brought"));
    s.put("brung", Arrays.asList("brought"));
    s.put("thru", Arrays.asList("through"));
    s.put("pitty", Arrays.asList("pity"));
    s.put("barbwire", Arrays.asList("barbed wire"));
    s.put("barbwires", Arrays.asList("barbed wires"));
    s.put("monkie", Arrays.asList("monkey"));
    s.put("Monkie", Arrays.asList("Monkey"));
    s.put("monkies", Arrays.asList("monkeys"));
    s.put("Monkies", Arrays.asList("Monkeys"));
    s.put("Daddys", Arrays.asList("Daddy's", "Daddies"));
    s.put("Mommys", Arrays.asList("Mommy's", "Mommies"));
    s.put("daddys", Arrays.asList("daddy's", "daddies"));
    s.put("mommys", Arrays.asList("mommy's", "mommies"));
    // the replacement pairs would prefer "speak"
    s.put("speach", Arrays.asList("speech"));
    s.put("icecreem", Arrays.asList("ice cream"));
    // in en-gb it's 'maths'
    s.put("math", Arrays.asList("maths"));
    s.put("fora", Arrays.asList("for a"));
    s.put("fomr", Arrays.asList("form", "from"));
    s.put("lotsa", Arrays.asList("lots of"));
    s.put("tryna", Arrays.asList("trying to"));
    s.put("coulda", Arrays.asList("could have"));
    s.put("shoulda", Arrays.asList("should have"));
    s.put("woulda", Arrays.asList("would have"));
    s.put("tellem", Arrays.asList("tell them"));
    s.put("Tellem", Arrays.asList("Tell them"));
    s.put("Webex", Arrays.asList("WebEx"));
    s.put("didint", Arrays.asList("didn't"));
    s.put("Didint", Arrays.asList("Didn't"));
    s.put("wasint", Arrays.asList("wasn't"));
    s.put("hasint", Arrays.asList("hasn't"));
    s.put("doesint", Arrays.asList("doesn't"));
    s.put("ist", Arrays.asList("is"));
    s.put("Boing", Arrays.asList("Boeing"));
    s.put("te", Arrays.asList("the"));
    s.put("todays", Arrays.asList("today's"));
    s.put("Todays", Arrays.asList("Today's"));
    s.put("todo", Arrays.asList("to-do", "to do"));
    s.put("todos", Arrays.asList("to-dos", "to do"));
    s.put("heres", Arrays.asList("here's"));
    s.put("Heres", Arrays.asList("Here's"));
    s.put("aways", Arrays.asList("always"));
    s.put("McDonalds", Arrays.asList("McDonald's"));
    s.put("ux", Arrays.asList("UX"));
    s.put("ive", Arrays.asList("I've"));
    s.put("infos", Arrays.asList("informations"));
    s.put("Infos", Arrays.asList("Informations"));
    s.put("prios", Arrays.asList("priorities"));
    s.put("Prio", Arrays.asList("Priority"));
    s.put("prio", Arrays.asList("priority"));
    s.put("Ecommerce", Arrays.asList("E-Commerce"));
    s.put("ezine", Arrays.asList("e-zine"));
    s.put("Ezine", Arrays.asList("E-zine"));
    s.put("ezines", Arrays.asList("e-zines"));
    s.put("Ezines", Arrays.asList("E-zines"));
    s.put("ebook", Arrays.asList("e-book"));
    s.put("ebooks", Arrays.asList("e-books"));
    s.put("eBook", Arrays.asList("e-book"));
    s.put("eBooks", Arrays.asList("e-books"));
    s.put("Ebook", Arrays.asList("E-Book"));
    s.put("Ebooks", Arrays.asList("E-Books"));
    s.put("Esport", Arrays.asList("E-Sport"));
    s.put("Esports", Arrays.asList("E-Sports"));
    s.put("R&B", Arrays.asList("R & B", "R 'n' B"));
    s.put("ie", Arrays.asList("i.e."));
    s.put("eg", Arrays.asList("e.g."));
    s.put("ppl", Arrays.asList("people"));
    s.put("kiddin", Arrays.asList("kidding"));
    s.put("doin", Arrays.asList("doing"));
    s.put("nothin", Arrays.asList("nothing"));
    s.put("SPOC", Arrays.asList("SpOC"));
    s.put("Thx", Arrays.asList("Thanks"));
    s.put("thx", Arrays.asList("thanks"));
    s.put("ty", Arrays.asList("thank you", "thanks"));
    s.put("Sry", Arrays.asList("Sorry"));
    s.put("sry", Arrays.asList("sorry"));
    s.put("im", Arrays.asList("I'm"));
    s.put("spoilt", Arrays.asList("spoiled"));
    s.put("Lil", Arrays.asList("Little"));
    s.put("lil", Arrays.asList("little"));
    s.put("gmail", Arrays.asList("Gmail"));
    s.put("Sucka", Arrays.asList("Sucker"));
    s.put("sucka", Arrays.asList("sucker"));
    s.put("whaddya", Arrays.asList("what are you", "what do you"));
    s.put("Whaddya", Arrays.asList("What are you", "What do you"));
    s.put("sinc", Arrays.asList("sync"));
    s.put("sweety", Arrays.asList("sweetie"));
    s.put("sweetys", Arrays.asList("sweeties"));
    s.put("sowwy", Arrays.asList("sorry"));
    s.put("Sowwy", Arrays.asList("Sorry"));
    s.put("grandmum", Arrays.asList("grandma", "grandmother"));
    s.put("grandmom", Arrays.asList("grandma", "grandmother"));
    s.put("Grandmum", Arrays.asList("Grandma", "Grandmother"));
    s.put("Grandmom", Arrays.asList("Grandma", "Grandmother"));
    s.put("Hongkong", Arrays.asList("Hong Kong"));
    s.put("enlighting", Arrays.asList("enlightening"));
    s.put("Enlighting", Arrays.asList("Enlightening"));
    // For non-US English
    s.put("center", Arrays.asList("centre"));
    s.put("ur", Arrays.asList("your", "you are"));
    s.put("Ur", Arrays.asList("Your", "You are"));
    s.put("ure", Arrays.asList("your", "you are"));
    s.put("Ure", Arrays.asList("Your", "You are"));
    s.put("mins", Arrays.asList("minutes", "min"));
    s.put("geo", Arrays.asList("geography"));
    s.put("Geo", Arrays.asList("Geography"));
    s.put("addon", Arrays.asList("add-on"));
    s.put("addons", Arrays.asList("add-ons"));
    s.put("afterparty", Arrays.asList("after-party"));
    s.put("Afterparty", Arrays.asList("After-party"));
    s.put("wellbeing", Arrays.asList("well-being"));
    s.put("cuz", Arrays.asList("because"));
    s.put("Cuz", Arrays.asList("Because"));
    s.put("coz", Arrays.asList("because"));
    s.put("Coz", Arrays.asList("Because"));
    s.put("pls", Arrays.asList("please"));
    s.put("Pls", Arrays.asList("Please"));
    s.put("plz", Arrays.asList("please"));
    s.put("Plz", Arrays.asList("Please"));
    // AtD irregular plurals - START
    s.put("addendums", Arrays.asList("addenda"));
    s.put("algas", Arrays.asList("algae"));
    s.put("alumnas", Arrays.asList("alumnae"));
    s.put("alumnuses", Arrays.asList("alumni"));
    s.put("analysises", Arrays.asList("analyses"));
    s.put("appendixs", Arrays.asList("appendices"));
    s.put("axises", Arrays.asList("axes"));
    s.put("bacilluses", Arrays.asList("bacilli"));
    s.put("bacteriums", Arrays.asList("bacteria"));
    s.put("basises", Arrays.asList("bases"));
    s.put("beaus", Arrays.asList("beaux"));
    s.put("bisons", Arrays.asList("bison"));
    s.put("buffalos", Arrays.asList("buffaloes"));
    s.put("calfs", Arrays.asList("calves"));
    s.put("Childs", Arrays.asList("Children", "Child's"));
    s.put("childs", Arrays.asList("children", "child's"));
    s.put("crisises", Arrays.asList("crises"));
    s.put("criterions", Arrays.asList("criteria"));
    s.put("curriculums", Arrays.asList("curricula"));
    s.put("datums", Arrays.asList("data"));
    s.put("deers", Arrays.asList("deer"));
    s.put("diagnosises", Arrays.asList("diagnoses"));
    s.put("echos", Arrays.asList("echoes"));
    s.put("elfs", Arrays.asList("elves"));
    s.put("ellipsises", Arrays.asList("ellipses"));
    s.put("embargos", Arrays.asList("embargoes"));
    s.put("erratums", Arrays.asList("errata"));
    s.put("firemans", Arrays.asList("firemen"));
    s.put("fishs", Arrays.asList("fishes", "fish"));
    s.put("genuses", Arrays.asList("genera"));
    s.put("gooses", Arrays.asList("geese"));
    s.put("halfs", Arrays.asList("halves"));
    s.put("heros", Arrays.asList("heroes"));
    s.put("indexs", Arrays.asList("indices", "indexes"));
    s.put("lifes", Arrays.asList("lives"));
    s.put("mans", Arrays.asList("men", "man's"));
    s.put("mens", Arrays.asList("men", "men's"));
    s.put("matrixs", Arrays.asList("matrices"));
    s.put("meanses", Arrays.asList("means"));
    s.put("mediums", Arrays.asList("media"));
    s.put("memorandums", Arrays.asList("memoranda"));
    s.put("mooses", Arrays.asList("moose"));
    s.put("mosquitos", Arrays.asList("mosquitoes"));
    s.put("moskitos", Arrays.asList("mosquitoes"));
    s.put("moskito", Arrays.asList("mosquito"));
    s.put("neurosises", Arrays.asList("neuroses"));
    s.put("nucleuses", Arrays.asList("nuclei"));
    s.put("oasises", Arrays.asList("oases"));
    s.put("ovums", Arrays.asList("ova"));
    s.put("oxs", Arrays.asList("oxen"));
    s.put("oxes", Arrays.asList("oxen"));
    s.put("paralysises", Arrays.asList("paralyses"));
    s.put("potatos", Arrays.asList("potatoes"));
    s.put("radiuses", Arrays.asList("radii"));
    s.put("selfs", Arrays.asList("selves"));
    s.put("serieses", Arrays.asList("series"));
    s.put("sheeps", Arrays.asList("sheep"));
    s.put("shelfs", Arrays.asList("shelves"));
    s.put("scissorses", Arrays.asList("scissors"));
    s.put("specieses", Arrays.asList("species"));
    s.put("stimuluses", Arrays.asList("stimuli"));
    s.put("stratums", Arrays.asList("strata"));
    s.put("tableaus", Arrays.asList("tableaux"));
    s.put("thats", Arrays.asList("those"));
    s.put("thesises", Arrays.asList("theses"));
    s.put("thiefs", Arrays.asList("thieves"));
    s.put("thises", Arrays.asList("these"));
    s.put("tomatos", Arrays.asList("tomatoes"));
    s.put("tooths", Arrays.asList("teeth"));
    s.put("torpedos", Arrays.asList("torpedoes"));
    s.put("vertebras", Arrays.asList("vertebrae"));
    s.put("vetos", Arrays.asList("vetoes"));
    s.put("vitas", Arrays.asList("vitae"));
    s.put("watchs", Arrays.asList("watches"));
    s.put("wifes", Arrays.asList("wives", "wife's"));
    s.put("womans", Arrays.asList("women", "woman's"));
    s.put("Womans", Arrays.asList("Women", "Woman's"));
    s.put("womens", Arrays.asList("women's"));
    s.put("Womens", Arrays.asList("Women's"));
    s.put("deauthorized", Arrays.asList("unauthorized"));
    // AtD irregular plurals - END
    // "tippy-top" is an often used word by Donald Trump
    s.put("tippy-top", Arrays.asList("tip-top", "top most"));
    s.put("tippytop", Arrays.asList("tip-top", "top most"));
    s.put("imma", Arrays.asList("I'm going to", "I'm a"));
    s.put("Imma", Arrays.asList("I'm going to", "I'm a"));
    s.put("dontcha", Arrays.asList("don't you"));
    s.put("tobe", Arrays.asList("to be"));
    s.put("Gi", Arrays.asList("Hi"));
    s.put("Ji", Arrays.asList("Hi"));
    s.put("Dontcha", Arrays.asList("don't you"));
    s.put("greatfruit", Arrays.asList("grapefruit", "great fruit"));
    s.put("Insta", Arrays.asList("Instagram"));
    s.put("IO", Arrays.asList("I/O"));
    s.put("wierd", Arrays.asList("weird"));
    s.put("Wierd", Arrays.asList("Weird"));
    s.put("HipHop", Arrays.asList("Hip-Hop"));
    s.put("gove", Arrays.asList("love", "give", "gave", "move"));
    s.put("birdseye", Arrays.asList("bird's-eye"));
    s.put("Birdseye", Arrays.asList("Bird's-eye"));
    s.put("al", Arrays.asList("all", "Al"));
    s.put("publically", Arrays.asList("publicly"));
    s.put("fo", Arrays.asList("for"));
    s.put("shawty", Arrays.asList("shorty"));
    s.put("Shawty", Arrays.asList("Shorty"));
    s.put("savy", Arrays.asList("savvy"));
    s.put("Savy", Arrays.asList("Savvy"));
    s.put("automization", Arrays.asList("automatization"));
    s.put("automisation", Arrays.asList("automatisation"));
    s.put("Automization", Arrays.asList("Automatization"));
    s.put("Automisation", Arrays.asList("Automatisation"));
    s.put("aswell", Arrays.asList("as well"));
    s.put("Continuesly", Arrays.asList("Continuously"));
    s.put("continuesly", Arrays.asList("continuously"));
    s.put("humain", Arrays.asList("humane", "human"));
    s.put("protene", Arrays.asList("protein"));
    s.put("throught", Arrays.asList("thought", "through", "throat"));
    s.put("specifity", Arrays.asList("specificity"));
    s.put("specicity", Arrays.asList("specificity"));
    s.put("Specifity", Arrays.asList("Specificity"));
    s.put("Specicity", Arrays.asList("Specificity"));
    s.put("specifities", Arrays.asList("specificities"));
    s.put("specicities", Arrays.asList("specificities"));
    s.put("Specifities", Arrays.asList("Specificities"));
    s.put("Specicities", Arrays.asList("Specificities"));
    s.put("Neonazi", Arrays.asList("Neo-Nazi"));
    s.put("Neonazis", Arrays.asList("Neo-Nazis"));
    s.put("neonazi", Arrays.asList("neo-Nazi"));
    s.put("neonazis", Arrays.asList("neo-Nazis"));
    s.put("fiveteen", Arrays.asList("fifteen"));
    s.put("Fiveteen", Arrays.asList("Fifteen"));
    s.put("critism", Arrays.asList("criticism"));
    s.put("Critism", Arrays.asList("Criticism"));
    s.put("Hobbie", Arrays.asList("Hobby"));
    s.put("hobbie", Arrays.asList("hobby"));
    s.put("Hobbys", Arrays.asList("Hobbies"));
    s.put("hobbys", Arrays.asList("hobbies"));
    s.put("Copie", Arrays.asList("Copy"));
    s.put("Copys", Arrays.asList("Copies"));
    s.put("copie", Arrays.asList("copy"));
    s.put("copys", Arrays.asList("copies"));
    s.put("rideshare", Arrays.asList("ride-share"));
    s.put("Rideshare", Arrays.asList("Ride-share"));
    s.put("Rideshares", Arrays.asList("Ride-shares"));
    s.put("bonafide", Arrays.asList("bona fide"));
    s.put("Bonafide", Arrays.asList("Bona fide"));
    s.put("dropoff", Arrays.asList("drop-off"));
    s.put("Dropoff", Arrays.asList("Drop-off"));
    s.put("reportings", Arrays.asList("reports", "reporting"));
    s.put("Reportings", Arrays.asList("Reports", "Reporting"));
    s.put("luv", Arrays.asList("love"));
    s.put("luvs", Arrays.asList("loves"));
    s.put("Luv", Arrays.asList("Love"));
    s.put("Luvs", Arrays.asList("Loves"));
    s.put("islam", Arrays.asList("Islam"));
    s.put("wud", Arrays.asList("what", "mud", "bud"));
    s.put("Wud", Arrays.asList("What", "Mud", "Bud"));
    s.put("fablet", Arrays.asList("phablet", "tablet"));
    s.put("Fablet", Arrays.asList("Phablet", "Tablet"));
    s.put("companys", Arrays.asList("companies", "company's", "company"));
    s.put("Companys", Arrays.asList("Companies", "Company's", "Company"));
    s.put("unencode", Arrays.asList("decode"));
    s.put("unencodes", Arrays.asList("decodes"));
    s.put("unencoded", Arrays.asList("decoded"));
    s.put("unencoding", Arrays.asList("decoding"));
    s.put("cheq", Arrays.asList("check"));
    s.put("southwest", Arrays.asList("south-west"));
    s.put("southeast", Arrays.asList("south-east"));
    s.put("northwest", Arrays.asList("north-west"));
    s.put("northeast", Arrays.asList("north-east"));
    s.put("Marylin", Arrays.asList("Marilyn"));
    s.put("blest", Arrays.asList("blessed"));
    s.put("yeld", Arrays.asList("yelled"));
    s.put("os", Arrays.asList("OS", "is", "so"));
    s.put("abel", Arrays.asList("able"));
    s.put("param", Arrays.asList("parameter"));
    s.put("params", Arrays.asList("parameters"));
    s.put("Param", Arrays.asList("Parameter"));
    s.put("Params", Arrays.asList("Parameters"));
    s.put("amature", Arrays.asList("amateur"));
    s.put("egoic", Arrays.asList("egoistic"));
    s.put("tarpit", Arrays.asList("tar pit"));
    s.put("tarpits", Arrays.asList("tar pits"));
    s.put("Tarpit", Arrays.asList("Tar pit"));
    s.put("Tarpits", Arrays.asList("Tar pits"));

    return s;
  }

  /**
   * @since 2.7
   */
  @Override
  protected List<SuggestedReplacement> getAdditionalTopSuggestions(List<SuggestedReplacement> suggestions, String word) throws IOException {
    /*if (word.length() < 20 && word.matches("[a-zA-Z-]+.?")) {
      List<String> prefixes = Arrays.asList("inter", "pre");
      for (String prefix : prefixes) {
        if (word.startsWith(prefix)) {
          String lastPart = word.substring(prefix.length());
          if (!isMisspelled(lastPart)) {
            // as these are only single words and both the first part and the last part are spelled correctly
            // (but the combination is not), it's okay to log the words from a privacy perspective:
            logger.info("UNKNOWN-EN: " + word);
          }
        }
      }
    }*/
    List<String> curatedSuggestions = new ArrayList<>();
    curatedSuggestions.addAll(topSuggestions.getOrDefault(word, Collections.emptyList()));
    curatedSuggestions.addAll(topSuggestionsIgnoreCase.getOrDefault(word.toLowerCase(), Collections.emptyList()));
    if (!curatedSuggestions.isEmpty()) {
      return SuggestedReplacement.convert(curatedSuggestions);
    } else if (word.endsWith("ys")) {
      String suggestion = word.replaceFirst("ys$", "ies");
      if (!speller1.isMisspelled(suggestion)) {
        return SuggestedReplacement.convert(Arrays.asList(suggestion));
      }
    }
    return super.getAdditionalTopSuggestions(suggestions, word);
  }

  @Override
  protected Translator getTranslator(GlobalConfig globalConfig) {
    return translator;
  }

  // Do not tokenize new words from spelling.txt...
  // Multi-token words should be in multiwords.txt
  protected boolean tokenizeNewWords() {
    return false;
  }

  @Override
  protected List<SuggestedReplacement> getOnlySuggestions(String word) {
    // NOTE: only add words here that would otherwise have more than one suggestion
    // and have apply to all variants of English (en-US, en-GB, ...):
    if (word.matches("[Cc]emetary")) return topMatch(word.replaceFirst("emetary", "emetery"));
    if (word.matches("[Cc]emetaries")) return topMatch(word.replaceFirst("emetaries", "emeteries"));
    if (word.matches("[Bb]asicly")) return topMatch(word.replaceFirst("asicly", "asically"));
    if (word.matches("[Bb]eleives?")) return topMatch(word.replaceFirst("eleive", "elieve"));
    if (word.matches("[Bb]elives?")) return topMatch(word.replaceFirst("elive", "elieve"));
    if (word.matches("[Bb]izzare")) return topMatch(word.replaceFirst("izzare", "izarre"));
    if (word.matches("[Cc]ompletly")) return topMatch(word.replaceFirst("ompletly", "ompletely"));
    if (word.matches("[Dd]issapears?")) return topMatch(word.replaceFirst("issapear", "isappear"));
    if (word.matches("[Ff]arenheit")) return topMatch(word.replaceFirst("arenheit", "ahrenheit"));
    if (word.matches("[Ff]reinds?")) return topMatch(word.replaceFirst("reind", "riend"));
    if (word.matches("[Ii]ncidently")) return topMatch(word.replaceFirst("ncidently", "ncidentally"));
    if (word.matches("[Ii]nterupts?")) return topMatch(word.replaceFirst("nterupt", "nterrupt"));
    if (word.matches("[Ll]ollypops?")) return topMatch(word.replaceFirst("ollypop", "ollipop"));
    if (word.matches("[Oo]cassions?")) return topMatch(word.replaceFirst("cassion", "ccasion"));
    if (word.matches("[Oo]ccurances?")) return topMatch(word.replaceFirst("ccurance", "ccurrence"));
    if (word.matches("[Pp]ersistant")) return topMatch(word.replaceFirst("ersistant", "ersistent"));
    if (word.matches("[Pp]eices?")) return topMatch(word.replaceFirst("eice", "iece"));
    if (word.matches("[Ss]eiges?")) return topMatch(word.replaceFirst("eige", "iege"));
    if (word.matches("[Ss]upercedes?")) return topMatch(word.replaceFirst("upercede", "upersede"));
    if (word.matches("[Tt]hreshholds?")) return topMatch(word.replaceFirst("hreshhold", "hreshold"));
    if (word.matches("[Tt]ommorrows?")) return topMatch(word.replaceFirst("ommorrow", "omorrow"));
    if (word.matches("[Tt]ounges?")) return topMatch(word.replaceFirst("ounge", "ongue"));
    if (word.matches("[Ww]ierd")) return topMatch(word.replaceFirst("ierd", "eird"));
    if (word.matches("[Jj]ist")) {
      List<SuggestedReplacement> l = new ArrayList<>();
      l.add(new SuggestedReplacement("just"));
      l.add(new SuggestedReplacement("gist"));
      return l;
    }
    return Collections.emptyList();
  }

  private static class IrregularForms {
    final String baseform;
    final String posName;
    final String formName;
    final List<String> forms;
    private IrregularForms(String baseform, String posName, String formName, List<String> forms) {
      this.baseform = baseform;
      this.posName = posName;
      this.formName = formName;
      this.forms = forms;
    }
  }

}
