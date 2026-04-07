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
import org.apache.commons.lang3.StringUtils;
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

import static java.util.regex.Pattern.*;
import static org.languagetool.rules.SuggestedReplacement.topMatch;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public abstract class AbstractEnglishSpellerRule extends MorfologikSpellerRule {

  @Override
  public List<String> getAdditionalSpellingFileNames() {
    // NOTE: also add to GermanSpellerRule.getSpeller() when adding items here:
    return Arrays.asList(language.getShortCode() + CUSTOM_SPELLING_FILE, GLOBAL_SPELLING_FILE,
      "/en/multiwords.txt");
  }

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
    "germane", // confused with German
    "double check",
    "flat screen", // flatscreen
    "full time", // should be 'full-time'
    "part time", // should be 'part-time'
    "java script",
    "off topic",
    "hard coding",
    "hard coded",
    "fine tune",
    "fine tuned",
    "fine tuning",
    "kick started", // kickstarted
    "kick starting", // kickstarting
    "kicks tarting", // kickstarting
    "kicks tarted", // kickstarted
    "with holdings",
    "hard coded",
    "hands free",
    "screens hare", // suggestion to screenshare
    "screens hares", // suggestion to screenshare
    "screens hared", // suggestion to screenshare
    "screens haring", // suggestion to screenshare
    "in flight",
    "in car",
    "client side",
    "server side",
    "worry some", // suggestion for worrysome
    "skillet", // wrong suggestion for skillset
    "skillets", // wrong suggestion for skillsets
    "code named",
    "code naming",
    "in house",
    "back office",
    "faggot",
    "faggots",
    "fuckable",
    "fuck",
    "fucker",
    "fuckhead",
    "fuckheads",
    "horseshit",
    "kraut",
    "krauts",
    "blackie",
    "blackies",
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
    "skillet",
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
  private static final Pattern PROFILERATION = compile("[Pp]rofileration");
  private static final Pattern CEMETARY = compile("[Cc]emetary");
  private static final Pattern CEMETARIES = compile("[Cc]emetaries");
  private static final Pattern BASICLY = compile("[Bb]asicly");
  private static final Pattern BELEIVES = compile("[Bb]eleives?");
  private static final Pattern BELIVES = compile("[Bb]elives?");
  private static final Pattern BIZZARE = compile("[Bb]izzare");
  private static final Pattern COMPLETLY = compile("[Cc]ompletly");
  private static final Pattern DISSAPEARS = compile("[Dd]issapears?");
  private static final Pattern FARENHEIT = compile("[Ff]arenheit");
  private static final Pattern FREINDS = compile("[Ff]reinds?");
  private static final Pattern INCIDENTLY = compile("[Ii]ncidently");
  private static final Pattern INTERUPTS = compile("[Ii]nterupts?");
  private static final Pattern LOLLYPOPS = compile("[Ll]ollypops?");
  private static final Pattern OCASSIONS = compile("[Oo]cassions?");
  private static final Pattern OCCURANCES = compile("[Oo]ccurances?");
  private static final Pattern PERSISTANT = compile("[Pp]ersistant");
  private static final Pattern PEICES = compile("[Pp]eices?");
  private static final Pattern SEIGES = compile("[Ss]eiges?");
  private static final Pattern SUPERCEDES = compile("[Ss]upercedes?");
  private static final Pattern THRESHHOLDS = compile("[Tt]hreshholds?");
  private static final Pattern TOMMORROWS = compile("[Tt]ommorrows?");
  private static final Pattern TOUTES = compile("[Tt]ounges?");
  private static final Pattern WIERD = compile("[Ww]ierd");
  private static final Pattern SARGENT = compile("[Ss]argent");
  private static final Pattern SWIMMED = compile("swimmed");
  private static final Pattern MISSPELT = compile("misspelt");
  private static final Pattern JIST = compile("[Jj]ist");
  private static final Pattern ADHOC = compile("[Ad]hoc");
  private static final Pattern DEACTIVE = compile("[De]eactive");
  private static final Pattern HONGKONG = compile("[hH]on[kg]kong");
  private static final Pattern AFAIK = compile("afaik");
  private static final Pattern JANUARY = compile("january");
  private static final Pattern ADMITTINGLY = compile("[Aa]dmittingly");
  private static final Pattern APRIL = compile("april");
  private static final Pattern SEPTEMBER = compile("september");
  private static final Pattern OCTOBER = compile("october");
  private static final Pattern NOVEMBER = compile("november");
  private static final Pattern DECEMBER = compile("december");
  private static final Pattern ENGLISH = compile("english");
  private static final Pattern SPANISH = compile("spanish");
  private static final Pattern GITLAB = compile("[Gg]itlab");
  private static final Pattern BONAFIDE = compile("[Bb]onafide");
  private static final Pattern WHEREEVER = compile("[Ww]hereever");
  private static final Pattern UNINSPIRATIONAL = compile("[Uu]ninspirational");
  private static final Pattern WHATSAPP = compile("[Ww]hatsapp");
  private static final Pattern JETLAGGED = compile("jetlagged");
  private static final Pattern MACBOOK = compile("[Mm]acbooks?");
  private static final Pattern LIKELYHOOD = compile("[Ll]ikelyhood");
  private static final Pattern FORSEEABLE = compile("[Ff]orseeable");
  private static final Pattern UNFORSEEABLE = compile("[Uu]nforseeable");
  private static final Pattern FORSEEABLY = compile("[Ff]orseeably");
  private static final Pattern UNFORSEEABLY = compile("[Uu]nforseeably");
  private static final Pattern UNECESSARY = compile("[Uu]necessary");
  private static final Pattern HUBSPOT = compile("[Hh]ubspot");
  private static final Pattern URL = compile("[Uu]rl");
  private static final Pattern TV = compile("tv");
  private static final Pattern HTTP = compile("[Hh]ttp");
  private static final Pattern HTTPS = compile("[Hh]ttps");
  private static final Pattern EUROPEAN = compile("european");
  private static final Pattern EUROPEANS = compile("europeans");
  private static final Pattern FYI = compile("[Ff]yi");
  private static final Pattern MICROSOFT = compile("microsoft");
  private static final Pattern DEVOPS = compile("[Dd]evops");
  private static final Pattern ALLRIGHT = compile("[Aa]llright");
  private static final Pattern INTRANSPARENT = compile("intransparent(ly)?");
  private static final Pattern ADDON = compile("[Aa]ddons?");
  private static final Pattern WDYT = compile("[Ww]dyt");
  private static final Pattern UNCOMPLIANT = compile("[UuIi]ncompliant");
  private static final Pattern UX = compile("ux");
  private static final Pattern LANGUAGETOOL = compile("[Ll]anguagetool");
  private static final Pattern UNDETERMINISTIC = compile("undeterministic");
  private static final Pattern QUILLBOT_POS = compile("QuillBots");
  private static final Pattern QUILLBOT1 = compile("[Qq]uill?bot");
  private static final Pattern QUILLBOT1_POS = compile("[Qq]uill?bots");
  private static final Pattern QUILLBOT2 = compile("QuilBot");
  private static final Pattern QUILLBOT2_POS = compile("QuilBots");

  private final BeoLingusTranslator translator;

  private static NERService nerPipe = null;
  
  private static final int maxPatterns = 9;
  private static final Pattern[] wordPatterns = new Pattern[maxPatterns];
  private static final String[] blogLinks = new String[maxPatterns];
  private static final Pattern CONTAINS_TOKEN = compile(".* (b|c|d|e|f|g|h|j|k|l|m|n|o|p|q|r|s|t|v|w|y|z|ll|ve)");
  static  {
    wordPatterns[0] = compile(".*[yi][zs]e([sd])?|.*[yi][zs]ings?|.*i[zs]ations?", CASE_INSENSITIVE | UNICODE_CASE);
    blogLinks[0] = "https://quillbot.com/blog/category/uk-vs-us/";

    wordPatterns[1] = compile(".*(defen[cs]e|offen[sc]e|preten[sc]e).*", CASE_INSENSITIVE | UNICODE_CASE);
    blogLinks[1] = "https://quillbot.com/blog/category/uk-vs-us/";

    wordPatterns[2] = compile(".*og|.*ogue", CASE_INSENSITIVE | UNICODE_CASE);
    blogLinks[2] = "https://quillbot.com/blog/category/uk-vs-us/";
    
    wordPatterns[3] = compile(".*(or|our).*", CASE_INSENSITIVE | UNICODE_CASE);
    blogLinks[3] = "https://languagetool.org/insights/post/our-or/#colour-or-color%E2%80%94colourise-or-colorize";

    wordPatterns[4] = compile(".*e?able|.*dge?ments?|aging|ageing|ax|axe|.*grame?s?|neuron|neurone|neurons|neurones", CASE_INSENSITIVE | UNICODE_CASE);
    blogLinks[4] = "https://languagetool.org/insights/post/our-or/#likeable-vs-likable-judgement-vs-judgment-oestrogen-vs-estrogen";
    
    wordPatterns[5] = compile(".*(centre|center).*|.*(re|er)", CASE_INSENSITIVE | UNICODE_CASE);
    blogLinks[5] = "https://languagetool.org/insights/post/re-vs-er/#the-difference-of-%E2%80%9C-reer%E2%80%9D-at-the-center-of-attention";
   
    wordPatterns[6] = compile("canceled|cancelled|canceling|cancelling|chili|chilli|chilies|chillies|chilis|chillis|counselor|counsellor|counselors|counsellors|defueled|defuelled|defueling|defuelling|defuelings|defuellings|dialed|dialled|dialer|dialler|dialers|diallers|dialing|dialling|dialog|dialogue|dialogize|dialogise|dialogized|dialogised|dialogizes|dialogises|dialogizing|dialogising|dialogs|dialogues|dialyzable|dialysable|dialyze|dialyse|dialyzed|dialysed|dialyzes|dialyses|dialyzing|dialysing|enroll|enrol|enrolled|enroled|enrolling|enroling|enrollment|enrolment|enrollments|enrolments|enrolls|enrols|fueled|fuelled|fueling|fuelling|fulfill|fulfil|fulfillment|fulfilment|fulfills|fulfils|installment|instalment|installments|instalments|jewelry|jewellery|labeled|labelled|labeling|labelling|marvelous|marvellous|medalist|medallist|medalists|medallists|modeled|modelled|modeling|modelling|noise-canceling|noise-cancelling|refueled|refuelled|refueling|refuelling|relabeled|relabelled|relabeling|relabelling|remodeled|remodelled|remodeling|remodelling|signalization|signalisation|signalize|signalise|signalized|signalised|signalizes|signalises|signalizing|signalising|skillful|skilful|skillfully|skilfully|tranquilize|tranquillize|tranquilized|tranquillized|tranquilizes|tranquillizes|traveled|travelled|traveler|traveller|travelers|travellers|traveling|travelling|uncanceled|uncancelled|uncanceling|uncancelling|unlabeled|unlabelled|wooly|woolly", CASE_INSENSITIVE | UNICODE_CASE);
    blogLinks[6] = "https://languagetool.org/insights/post/re-vs-er/#british-english-prefers-doubling-consonants-doesn%E2%80%99t-it";
    
    wordPatterns[7] = compile("airfoil|aerofoil|airfoils|aerofoils|airplane|aeroplane|airplanes|aeroplanes|aluminum|aluminium|artifact|artefact|artifacts|artefacts|backdraft|backdraught|cozy|cosy|", CASE_INSENSITIVE | UNICODE_CASE);
    blogLinks[7] = "https://languagetool.org/insights/post/re-vs-er/#more-radical-differences-between-british-and-american-english-spellings";
    
    wordPatterns[8] = compile("amenorrhea|amenorrhoea|anesthesia|anaesthesia|anesthesias|anaesthesias|anesthetic|anaesthetic|anesthetically|anaesthetically|anesthetics|anaesthetics|anesthetist|anaesthetist|anesthetists|anaesthetists|anesthetization|anaesthetisation|anesthetizations|anaesthetisations|anesthetize|anaesthetise|anesthetized|anaesthetised|anesthetizes|anaesthetises|anesthetizing|anaesthetising|archeological|archaeological|archeologically|archaeologically|archeologies|archaeologies|archeology|archaeology|cesium|caesium|diarrhea|diarrhoea|diarrheal|diarrhoeal|dyslipidemia|dyslipidaemia|dyslipidemias|dyslipidaemias|edematous|oedematous|encyclopedia|encyclopaedia|encyclopedias|encyclopaedias|eon|aeon|eons|aeons|esophagi|oesophagi|esophagus|oesophagus|esophaguses|oesophaguses|esthetic|aesthetic|esthetical|aesthetical|esthetically|aesthetically|esthetician|aesthetician|estheticians|aestheticians|estrogen|oestrogen|estrus|oestrus|etiologies|aetiologies|etiology|aetiology|feces|faeces|fetal|foetal|fetus|foetus|fetuses|foetuses|gastroesophageal|gastro-oesophageal|glycemic|glycaemic|gynecomastia|gynaecomastia|hematemesis|haematemesis|hematoma|haematoma|hematomas|haematomas|hematopoietic|haematopoietic|hematuria|haematuria|hematurias|haematurias|hemolytic|haemolytic|hemophilia|haemophilia|hemorrhage|haemorrhage|hemorrhages|haemorrhages|hemostasis|haemostasis|homeopathies|homoeopathies|homeopathy|homoeopathy|hyperemia|hyperaemia|hyperemic|hyperaemic|hypnopedia|hypnopaedia|hypnopedic|hypnopaedic|hypocalcaemia|hypocalcaemia|hypokalaemic|hypokalemic|kinesthesia|kinaesthesia|kinesthesis|kinaesthesis|kinesthetic|kinaesthetic|kinesthetically|kinaesthetically|maneuver|manoeuvre|maneuvers|manoeuvres|orthopedic|orthopaedic|orthopedics|orthopaedics|paleoecology|palaeoecology|paleogeographical|palaeogeographical|paleogeographically|palaeogeographically|paleogeography|palaeogeography|paresthesia|paraesthesia|pediatric|paediatric|pediatrically|paediatrically|pediatrician|paediatrician|pediatricians|paediatricians|pedomorphic|paedomorphic|pedophile|paedophile|pedophiles|paedophiles|polycythemia|polycythaemia|pretorium|praetorium|pyorrhea|pyorrhoea|septicemia|septicaemia|synesthesia|synaesthesia|synesthete|synaesthete|synesthetes|synaesthetes|tracheoesophageal|tracheo-oesophageal", CASE_INSENSITIVE | UNICODE_CASE);
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
          //List<String> infos = new ArrayList<>();
          long textCount = lm.getCount(covered);
          //System.out.println(textCount + " for " + covered);
          //infos.add(covered + "/" + textCount);
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
              //infos.add(repl + "/" + replCount);
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
      if (!CONTAINS_TOKEN.matcher(suggestion.getReplacement()).matches())   // e.g. 'timezones' suggests 'timezone s'
        clean.add(suggestion);
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
      .filter(k -> {
        String rep = k.getReplacement();
        if (!rep.contains(" ")) {
          return true;
        }
        String repLc = rep.toLowerCase();
        return
          !repLc.startsWith("re ") &&
          !repLc.startsWith("en ") &&
          !repLc.startsWith("co ") &&
          !repLc.startsWith("cl ") &&
          !repLc.startsWith("de ") &&
          !repLc.startsWith("ex ") &&
          !repLc.startsWith("es ") &&
          !repLc.startsWith("ab ") &&
          !repLc.startsWith("ty ") &&
          !repLc.startsWith("mid ") &&
          !repLc.startsWith("non ") &&
          !repLc.startsWith("bio ") &&
          !repLc.startsWith("bi ") &&
          !repLc.startsWith("op ") &&
          !repLc.startsWith("con ") &&
          !repLc.startsWith("pre ") &&
          !repLc.startsWith("mis ") &&
          !repLc.startsWith("socio ") &&
          !repLc.startsWith("proto ") &&
          !repLc.startsWith("neo ") &&
          !repLc.startsWith("geo ") &&
          !repLc.startsWith("inter ") &&
          !repLc.startsWith("multi ") &&
          !repLc.startsWith("retro ") &&
          !repLc.startsWith("extra ") &&
          !repLc.startsWith("mega ") &&
          !repLc.startsWith("meta ") &&
          !repLc.startsWith("poly ") &&
          !repLc.startsWith("para ") &&
          !repLc.startsWith("uni ") &&
          !repLc.startsWith("anti ") &&
          !repLc.startsWith("necro ") &&
          !repLc.startsWith("photo ") &&
          !repLc.startsWith("post ") &&
          !repLc.startsWith("sub ") &&
          !repLc.startsWith("auto ") &&
          !repLc.startsWith("pl ") &&
          !repLc.startsWith("ht ") &&
          !repLc.startsWith("dis ") &&
          !repLc.startsWith("est ") &&
          !repLc.startsWith("mono ") &&
          !repLc.startsWith("trans ") &&
          !repLc.startsWith("neuro ") &&
          !repLc.startsWith("hetero ") &&
          !repLc.startsWith("ultra ") &&
          !repLc.startsWith("mini ") &&
          !repLc.startsWith("hyper ") &&
          !repLc.startsWith("micro ") &&
          !repLc.startsWith("counter ") &&
          !repLc.startsWith("over ") &&
          !repLc.startsWith("overs ") &&
          !repLc.startsWith("overt ") &&
          !repLc.startsWith("under ") &&
          !repLc.startsWith("cyber ") &&
          !repLc.startsWith("hydro ") &&
          !repLc.startsWith("ergo ") &&
          !repLc.startsWith("fore ") &&
          !repLc.startsWith("pro ") &&
          !repLc.startsWith("pseudo ") &&
          !repLc.startsWith("psycho ") &&
          !repLc.startsWith("mi ") &&
          !repLc.startsWith("nano ") &&
          !repLc.startsWith("ans ") &&
          !repLc.startsWith("semi ") &&
          !repLc.startsWith("infra ") &&
          !repLc.startsWith("hypo ") &&
          !repLc.startsWith("syn ") &&
          !repLc.startsWith("adv ") &&
          !repLc.startsWith("com ") &&
          !repLc.startsWith("res ") &&
          !repLc.startsWith("resp ") &&
          !repLc.startsWith("lo ") &&
          !repLc.startsWith("ed ") &&
          !repLc.startsWith("ac ") &&
          !repLc.startsWith("al ") &&
          !repLc.startsWith("ea ") &&
          !repLc.startsWith("ge ") &&
          !repLc.startsWith("mu ") &&
          !repLc.startsWith("ma ") &&
          !repLc.startsWith("la ") &&
          !repLc.startsWith("bis ") &&
          !repLc.startsWith("ger ") &&
          !repLc.startsWith("inf ") &&
          !repLc.startsWith("tar ") &&
          !repLc.startsWith("f ") &&
          !repLc.startsWith("k ") &&
          !repLc.startsWith("l ") &&
          !repLc.startsWith("b ") &&
          !repLc.startsWith("e ") &&
          !repLc.startsWith("c ") &&
          !repLc.startsWith("d ") &&
          !repLc.startsWith("p ") &&
          !repLc.startsWith("v ") &&
          !repLc.startsWith("h ") &&
          !repLc.startsWith("r ") &&
          !repLc.startsWith("s ") &&
          !repLc.startsWith("t ") &&
          !repLc.startsWith("u ") &&
          !repLc.startsWith("w ") &&
          !repLc.startsWith("um ") &&
          !repLc.startsWith("oft ") &&
          !rep.startsWith("i ") &&
          !rep.startsWith("sh ") &&
          !rep.startsWith("li ") &&
          !rep.startsWith("ha ") &&
          !rep.startsWith("st ") &&
          !rep.startsWith("ins ") && // instable (ins table)
          !rep.endsWith(" i") &&
          !rep.endsWith(" ING") &&
          !rep.endsWith(" able") &&
          !rep.endsWith(" om") &&
          !rep.endsWith(" ox") &&
          !rep.endsWith(" ht") &&
          !rep.endsWith(" wide") && // (e.g. storewide)
          !rep.endsWith(" less") && // (e.g. permissionless)
          !rep.endsWith(" sly") && // unnecessary suggestion (e.g. for continuesly)
          !rep.endsWith(" OO") && // unnecessary suggestion (e.g. for "HELLOOO")
          !rep.endsWith(" HHH") && // unnecessary suggestion (e.g. for "OHHHH")
          !rep.endsWith(" ally") && // adverbs ending in "ally" that LT doesn't know (yet)
          !rep.endsWith(" ize") && // "advertize"
          !rep.endsWith(" sh") &&
          !rep.endsWith(" st") &&
          !rep.endsWith(" est") &&
          !rep.endsWith(" em") &&
          !rep.endsWith(" ward") &&
          !rep.endsWith(" ability") && // interruptability
          !rep.endsWith(" ware") && // drinkware
          !rep.endsWith(" logy") && // volcanology
          !rep.endsWith(" ting") && // someting
          !rep.endsWith(" ion") && // presention
          !rep.endsWith(" ions") && // sealions
          !rep.endsWith(" cal") &&
          !rep.endsWith(" ted") && // "bursted"
          !rep.endsWith(" sphere") &&
          !rep.endsWith(" ell") &&
          !rep.endsWith(" co") &&
          !rep.endsWith(" con") &&
          !rep.endsWith(" com") &&
          !rep.endsWith(" sis") &&
          !rep.endsWith(" like") && // "ribbonlike"
          !rep.endsWith(" full") && // do not suggest "use full" for "useful"
          !rep.endsWith(" en") && // "Antwerpen" suggests "Antwerp en"
          !rep.endsWith(" ne") &&
          !rep.endsWith(" ed") &&
          !rep.endsWith(" al") &&
          !rep.endsWith(" ans") &&
          !rep.endsWith(" mans") &&
          !rep.endsWith(" ti") &&
          !rep.endsWith(" de") &&
          !rep.endsWith(" ea") &&
          !rep.endsWith(" ge") &&
          !rep.endsWith(" ab") &&
          !rep.endsWith(" rs") &&
          !rep.endsWith(" mi") &&
          !rep.endsWith(" tar") &&
          !rep.endsWith(" adv") &&
          !rep.endsWith(" re") &&
          !rep.endsWith(" e") &&
          !rep.endsWith(" c") &&
          !rep.endsWith(" v") &&
          !rep.endsWith(" h") &&
          !rep.endsWith(" s") &&
          !rep.endsWith(" r") &&
          !rep.endsWith(" l") &&
          !rep.endsWith(" u") &&
          !rep.endsWith(" um") &&
          !rep.endsWith(" er") &&
          !rep.endsWith(" es") &&
          !rep.endsWith(" ex") &&
          !rep.endsWith(" na") &&
          !rep.endsWith(" ifs") &&
          !rep.endsWith(" gs") &&
          !rep.endsWith(" don") &&
          !rep.endsWith(" dons") &&
          !rep.endsWith(" la") &&
          !rep.endsWith(" ism") &&
          !rep.endsWith(" ma");
      })
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
    newMatch.setType(oldMatch.getType());
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
    newMatch.setType(oldMatch.getType());
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
    s.put("defenate", Arrays.asList("definite"));
    s.put("defenately", Arrays.asList("definitely"));
    s.put("trumpian", Arrays.asList("Trumpist"));
    s.put("trumpians", Arrays.asList("Trumpists"));
    s.put("UberEats", Arrays.asList("Uber Eats"));
    s.put("KWH", Arrays.asList("kWh"));
    s.put("MWH", Arrays.asList("MWh"));
    s.put("xray", Arrays.asList("X-ray"));
    s.put("xrays", Arrays.asList("X-rays"));
    s.put("soo", Arrays.asList("so", "too", "son"));
    return s;
  }

  protected static Map<String, List<String>> getTopSuggestions() {
    Map<String, List<String>> s = new HashMap<>();
    s.put("Ths", Arrays.asList("This", "The"));
    s.put("prev", Arrays.asList("previous"));
    s.put("kilotonne", Arrays.asList("kiloton"));
    s.put("kilotonnes", Arrays.asList("kilotons"));
    s.put("litehouse", Arrays.asList("lighthouse"));
    s.put("Litehouse", Arrays.asList("Lighthouse"));
    s.put("whitout", Arrays.asList("without", "white out"));
    s.put("Whitout", Arrays.asList("Without", "White out"));
    s.put("compability", Arrays.asList("compatibility"));
    s.put("Compability", Arrays.asList("Compatibility"));
    s.put("enroute", Arrays.asList("en route"));
    s.put("teasered", Arrays.asList("teased"));
    s.put("teasering", Arrays.asList("teasing"));
    s.put("stealed", Arrays.asList("stole", "stolen"));
    s.put("stealt", Arrays.asList("stole", "stolen"));
    s.put("ignight", Arrays.asList("ignite"));
    s.put("Ignight", Arrays.asList("Ignite"));
    s.put("ignights", Arrays.asList("ignites"));
    s.put("Ignights", Arrays.asList("Ignites"));
    s.put("ignighted", Arrays.asList("ignited"));
    s.put("Ignighted", Arrays.asList("Ignited"));
    s.put("ignighting", Arrays.asList("igniting"));
    s.put("Ignighting", Arrays.asList("Igniting"));
    s.put("transcripted", Arrays.asList("transcribed"));
    s.put("transcripting", Arrays.asList("transcribing"));
    s.put("incase", Arrays.asList("in case"));
    s.put("Incase", Arrays.asList("In case"));
    s.put("admittably", Arrays.asList("admittedly"));
    s.put("Admittably", Arrays.asList("Admittedly"));
    s.put("fam", Arrays.asList("family", "fame"));
    s.put("awnser", Arrays.asList("answer"));
    s.put("Awnser", Arrays.asList("Answer"));
    s.put("awnsers", Arrays.asList("answers"));
    s.put("Awnsers", Arrays.asList("Answers"));
    s.put("megatonne", Arrays.asList("megaton"));
    s.put("Megatonne", Arrays.asList("Megaton"));
    s.put("megatonnes", Arrays.asList("megatons"));
    s.put("Megatonnes", Arrays.asList("Megatons"));
    s.put("retd", Arrays.asList("retd.", "retired"));
    s.put("Retd", Arrays.asList("Retd.", "Retired"));
    s.put("intransparent", Arrays.asList("non-transparent", "lacking transparency"));
    s.put("repetion", Arrays.asList("repetition"));
    s.put("Repetion", Arrays.asList("Repetition"));
    s.put("repetions", Arrays.asList("repetitions"));
    s.put("Repetions", Arrays.asList("Repetitions"));
    s.put("rom", Arrays.asList("room"));
    s.put("th", Arrays.asList("the"));
    s.put("transman", Arrays.asList("trans man"));
    s.put("Transman", Arrays.asList("Trans man"));
    s.put("transmen", Arrays.asList("trans men"));
    s.put("Transmen", Arrays.asList("Trans men"));
    s.put("transwoman", Arrays.asList("trans woman"));
    s.put("Transwoman", Arrays.asList("Trans woman"));
    s.put("transwomen", Arrays.asList("trans women"));
    s.put("Transwomen", Arrays.asList("Trans women"));
    s.put("litterly", Arrays.asList("literally"));
    s.put("Litterly", Arrays.asList("Literally"));
    s.put("abt", Arrays.asList("about"));
    s.put("ley", Arrays.asList("let"));
    s.put("Ley", Arrays.asList("Let"));
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
    s.put("sal-ammoniac", Arrays.asList("sal ammoniac"));
    s.put("mumbo-jumbo", Arrays.asList("mumbo jumbo"));
    s.put("Mumbo-jumbo", Arrays.asList("Mumbo jumbo"));
    s.put("Mumbo-Jumbo", Arrays.asList("Mumbo Jumbo"));
    s.put("Tuffed", Arrays.asList("Toughed"));
    s.put("tuffed", Arrays.asList("toughed"));
    s.put("biosim", Arrays.asList("biosimilar"));
    s.put("biosims", Arrays.asList("biosimilars"));
    s.put("Tuffest", Arrays.asList("Toughest"));
    s.put("tuffest", Arrays.asList("toughest"));
    s.put("Tuffer", Arrays.asList("Tougher"));
    s.put("tuffer", Arrays.asList("tougher"));
    s.put("devast", Arrays.asList("devastate"));
    s.put("devasts", Arrays.asList("devastates"));
    s.put("devasted", Arrays.asList("devastated"));
    s.put("devasting", Arrays.asList("devastating"));
    s.put("Fundrace", Arrays.asList("Fundraise"));
    s.put("fundrace", Arrays.asList("fundraise"));
    s.put("Fundraces", Arrays.asList("Fundraises"));
    s.put("fundraces", Arrays.asList("fundraises"));
    s.put("Fundracing", Arrays.asList("Fundraising"));
    s.put("fundracing", Arrays.asList("fundraising"));
    s.put("Fundraced", Arrays.asList("Fundraised"));
    s.put("fundraced", Arrays.asList("fundraised"));
    s.put("withing", Arrays.asList("within"));
    s.put("Withing", Arrays.asList("Within"));
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
    s.put("politeful", Arrays.asList("polite"));
    s.put("Politeful", Arrays.asList("Polite"));
    s.put("defacto", Arrays.asList("de facto"));
    s.put("Defacto", Arrays.asList("De facto"));
    s.put("rethoric", Arrays.asList("rhetoric"));
    s.put("Rethoric", Arrays.asList("Rhetoric"));
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
    s.put("Thak", Arrays.asList("Thank"));
    s.put("thak", Arrays.asList("thank"));
    s.put("dieing", Arrays.asList("dying"));
    s.put("Dieing", Arrays.asList("Dying"));
    s.put("Supposably", Arrays.asList("Supposedly"));
    s.put("supposably", Arrays.asList("supposedly"));
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
    s.put("dat", Arrays.asList("that", "day"));
    s.put("Dat", Arrays.asList("That", "Day"));
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
    s.put("shouldent", Arrays.asList("shouldn't"));
    s.put("couldent", Arrays.asList("couldn't"));
    s.put("grandmum", Arrays.asList("grandma", "grandmother"));
    s.put("grandmom", Arrays.asList("grandma", "grandmother"));
    s.put("Grandmum", Arrays.asList("Grandma", "Grandmother"));
    s.put("Grandmom", Arrays.asList("Grandma", "Grandmother"));
    s.put("enlighting", Arrays.asList("enlightening"));
    s.put("Enlighting", Arrays.asList("Enlightening"));
    // For non-US English
    s.put("center", Arrays.asList("centre"));
    s.put("ur", Arrays.asList("your", "you are"));
    s.put("Ur", Arrays.asList("Your", "You are"));
    s.put("ure", Arrays.asList("your", "you are"));
    s.put("Ure", Arrays.asList("Your", "You are"));
    s.put("mins", Arrays.asList("minutes", "min"));
    s.put("geo", Arrays.asList("geography", "geographic"));
    s.put("Geo", Arrays.asList("Geography", "Geographic"));
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
    s.put("ir", Arrays.asList("it"));
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
    s.put("unconform", Arrays.asList("nonconform"));
    s.put("Unconform", Arrays.asList("Nonconform"));
    s.put("rideshare", Arrays.asList("ride-share"));
    s.put("Rideshare", Arrays.asList("Ride-share"));
    s.put("Rideshares", Arrays.asList("Ride-shares"));
    s.put("dropoff", Arrays.asList("drop-off"));
    s.put("Dropoff", Arrays.asList("Drop-off"));
    s.put("reportings", Arrays.asList("reports", "reporting"));
    s.put("Reportings", Arrays.asList("Reports", "Reporting"));
    s.put("luv", Arrays.asList("love"));
    s.put("luvs", Arrays.asList("loves"));
    s.put("Luv", Arrays.asList("Love"));
    s.put("Luvs", Arrays.asList("Loves"));
    s.put("islam", Arrays.asList("Islam"));
    s.put("andit", Arrays.asList("and it"));
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
    s.put("amatures", Arrays.asList("amateurs"));
    s.put("egoic", Arrays.asList("egoistic"));
    s.put("tarpit", Arrays.asList("tar pit"));
    s.put("tarpits", Arrays.asList("tar pits"));
    s.put("Tarpit", Arrays.asList("Tar pit"));
    s.put("Tarpits", Arrays.asList("Tar pits"));
    s.put("wheater", Arrays.asList("weather"));
    s.put("Wheater", Arrays.asList("Weather"));
    s.put("defi", Arrays.asList("defibrillator", "DeFi"));
    s.put("Defi", Arrays.asList("Defibrillator", "DeFi"));
    s.put("topup", Arrays.asList("top-up"));
    s.put("topups", Arrays.asList("top-ups"));
    s.put("nacked", Arrays.asList("backed", "naked"));
    s.put("no-profit", Arrays.asList("non-profit"));
    s.put("No-profit", Arrays.asList("Non-profit"));
    s.put("wrose", Arrays.asList("worse"));
    s.put("Wrose", Arrays.asList("Worse"));
    s.put("reak", Arrays.asList("wreak"));
    s.put("Reak", Arrays.asList("Wreak"));
    s.put("reaks", Arrays.asList("wreaks"));
    s.put("Reaks", Arrays.asList("Wreaks"));
    s.put("reaked", Arrays.asList("wreaked"));
    s.put("Reaked", Arrays.asList("Wreaked"));
    s.put("reaking", Arrays.asList("wreaking"));
    s.put("Reaking", Arrays.asList("Wreaking"));
    s.put("hight", Arrays.asList("height"));
    s.put("Hight", Arrays.asList("Height"));
    s.put("fulltime", Arrays.asList("full-time"));
    s.put("Fulltime", Arrays.asList("Full-time"));
    s.put("slimiar", Arrays.asList("similar"));
    s.put("Slimiar", Arrays.asList("Similar"));

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

  // trivial approach that assumes only one part of the hyphenated word has a typo...
  @Override
  protected void addHyphenSuggestions(String[] parts, List<SuggestedReplacement> topSuggestions) throws IOException {
    int i = 0;
    for (String part : parts) {
      if (isMisspelled(part)) {
        List<String> partSuggestions = speller1.getSuggestions(part);
        if (partSuggestions.size() == 0) {
          partSuggestions = speller2.getSuggestions(part);
        }
        if (partSuggestions.size() > 0) {
          String suggestion = getHyphenatedWordSuggestion(parts, i, partSuggestions.get(0));
          topSuggestions.add(new SuggestedReplacement(suggestion));
        }
      }
      i++;
    }
  }

  private String getHyphenatedWordSuggestion(String[] parts, int currentPos, String currentPostSuggestion) {
    List<String> newParts = new ArrayList<>();
    for (int j = 0; j < parts.length; j++) {
      if (currentPos == j) {
        newParts.add(currentPostSuggestion);
      } else {
        newParts.add(parts[j]);
      }
    }
    return String.join("-", newParts);
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
    if (PROFILERATION.matcher(word).matches())
      return topMatch(StringUtils.replaceOnce(word, "rofileration", "roliferation"), "rapid expansion");
    if (CEMETARY.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "emetary", "emetery"));
    if (CEMETARIES.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "emetaries", "emeteries"));
    if (BASICLY.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "asicly", "asically"));
    if (BELEIVES.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "eleive", "elieve"));
    if (BELIVES.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "elive", "elieve"));
    if (BIZZARE.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "izzare", "izarre"));
    if (COMPLETLY.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "ompletly", "ompletely"));
    if (DISSAPEARS.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "issapear", "isappear"));
    if (FARENHEIT.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "arenheit", "ahrenheit"));
    if (FREINDS.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "reind", "riend"));
    if (INCIDENTLY.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "ncidently", "ncidentally"));
    if (INTERUPTS.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "nterupt", "nterrupt"));
    if (LOLLYPOPS.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "ollypop", "ollipop"));
    if (OCASSIONS.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "cassion", "ccasion"));
    if (OCCURANCES.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "ccurance", "ccurrence"));
    if (PERSISTANT.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "ersistant", "ersistent"));
    if (PEICES.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "eice", "iece"));
    if (SEIGES.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "eige", "iege"));
    if (SUPERCEDES.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "upercede", "upersede"));
    if (THRESHHOLDS.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "hreshhold", "hreshold"));
    if (TOMMORROWS.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "ommorrow", "omorrow"));
    if (TOUTES.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "ounge", "ongue"));
    if (WIERD.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "ierd", "eird"));
    if (SARGENT.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "argent", "ergeant"));
    if (SWIMMED.matcher(word).matches()) return topMatch("swam");
    if (MISSPELT.matcher(word).matches()) return topMatch("misspelled");
    if (ADHOC.matcher(word).matches()) return topMatch("ad hoc");
    if (DEACTIVE.matcher(word).matches()) return topMatch("inactive");
    if (HUBSPOT.matcher(word).matches()) return topMatch("HubSpot");
    if (URL.matcher(word).matches()) return topMatch("URL");
    if (HTTP.matcher(word).matches()) return topMatch("HTTP");
    if (HTTPS.matcher(word).matches()) return topMatch("HTTPS");
    if (FYI.matcher(word).matches()) return topMatch("FYI");
    if (EUROPEAN.matcher(word).matches()) return topMatch("European");
    if (EUROPEANS.matcher(word).matches()) return topMatch("Europeans");
    if (DEVOPS.matcher(word).matches()) return topMatch("DevOps");
    if (MICROSOFT.matcher(word).matches()) return topMatch("Microsoft");
    if (LANGUAGETOOL.matcher(word).matches()) return topMatch("LanguageTool");
    if (HONGKONG.matcher(word).matches()) return topMatch("Hong Kong");
    if (OCTOBER.matcher(word).matches()) return topMatch("October");
    if (SEPTEMBER.matcher(word).matches()) return topMatch("September");
    if (DECEMBER.matcher(word).matches()) return topMatch("December");
    if (NOVEMBER.matcher(word).matches()) return topMatch("November");
    if (APRIL.matcher(word).matches()) return topMatch("April");
    if (AFAIK.matcher(word).matches()) return topMatch("AFAIK");
    if (JANUARY.matcher(word).matches()) return topMatch("January");
    if (ADMITTINGLY.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "dmittingly", "dmittedly"));
    if (ENGLISH.matcher(word).matches()) return topMatch("English");
    if (SPANISH.matcher(word).matches()) return topMatch("Spanish");
    if (UNDETERMINISTIC.matcher(word).matches()) return topMatch("nondeterministic");
    if (WDYT.matcher(word).matches()) return topMatch("WDYT");
    if (INTRANSPARENT.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "in", "un"));
    if (UNCOMPLIANT.matcher(word).matches()) return topMatch("non-compliant");
    if (UX.matcher(word).matches()) return topMatch("UX");
    if (GITLAB.matcher(word).matches()) return topMatch("GitLab");
    if (BONAFIDE.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "onafide", "ona fide"));
    if (ALLRIGHT.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "llright", "lright"));
    if (ADDON.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "ddon", "dd-on"));
    if (WHEREEVER.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "hereever", "herever"));
    if (WHATSAPP.matcher(word).matches()) return topMatch("WhatsApp");
    if (UNINSPIRATIONAL.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "ninspirational", "ninspiring"));
    if (JETLAGGED.matcher(word).matches()) return topMatch("jet-lagged");
    if (MACBOOK.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "acbook", "acBook"));
    if (LIKELYHOOD.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "ikelyhood", "ikelihood"));
    if (UNECESSARY.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "necessary", "nnecessary"));
    if (FORSEEABLE.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "orseeable", "oreseeable"));
    if (UNFORSEEABLE.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "orseeable", "oreseeable"));
    if (FORSEEABLY.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "orseeably", "oreseeably"));
    if (UNFORSEEABLY.matcher(word).matches()) return topMatch(StringUtils.replaceOnce(word, "orseeably", "oreseeably"));
    if (QUILLBOT1.matcher(word).matches() || QUILLBOT2.matcher(word).matches()) return topMatch("QuillBot");
    if (QUILLBOT_POS.matcher(word).matches() || QUILLBOT1_POS.matcher(word).matches() ||
      QUILLBOT2_POS.matcher(word).matches()) {
      List<SuggestedReplacement> l = new ArrayList<>();
      l.add(new SuggestedReplacement("QuillBot's"));
      l.add(new SuggestedReplacement("QuillBot"));
      return l;
    }
    if (TV.matcher(word).matches()) {
      List<SuggestedReplacement> l = new ArrayList<>();
      l.add(new SuggestedReplacement("TV"));
      l.add(new SuggestedReplacement("to"));
      return l;
    }
    if (JIST.matcher(word).matches()) {
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
