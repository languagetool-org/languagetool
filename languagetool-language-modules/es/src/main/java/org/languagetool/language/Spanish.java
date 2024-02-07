/* LanguageTool, a natural language style checker
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.language;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.rules.*;
import org.languagetool.rules.es.*;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.rules.spelling.multitoken.MultitokenSpeller;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.es.SpanishSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.es.SpanishHybridDisambiguator;
import org.languagetool.tagging.es.SpanishTagger;
import org.languagetool.tokenizers.*;
import org.languagetool.tokenizers.es.SpanishWordTokenizer;
import org.languagetool.tools.StringTools;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Spanish extends Language implements AutoCloseable {
  
  private LanguageModel languageModel;

  @Override
  public String getName() {
    return "Spanish";
  }

  @Override
  public String getShortCode() {
    return "es";
  }

  @Override
  public String[] getCountries() {
    return new String[]{
            "ES", "", "MX", "GT", "CR", "PA", "DO",
            "VE", "PE", "AR", "EC", "CL", "UY", "PY",
            "BO", "SV", "HN", "NI", "PR", "US", "CU"
    };
  }

  @Override
  public Language getDefaultLanguageVariant() {
    return Languages.getLanguageForShortCode("es");
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return SpanishTagger.INSTANCE;
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new SpanishHybridDisambiguator(getDefaultLanguageVariant());
  }

  @Override
  public Tokenizer createDefaultWordTokenizer() {
    return new SpanishWordTokenizer();
  }

  @Nullable
  @Override
  public Synthesizer createDefaultSynthesizer() {
    return SpanishSynthesizer.INSTANCE;
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }
  
  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {
            new Contributor("Jaume Ortolà")
    };
  }

  @Override
  public SpellingCheckRule createDefaultSpellingRule(ResourceBundle messages) throws IOException {
    return new MorfologikSpanishSpellerRule(messages, this, null, Collections.emptyList());
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Arrays.asList(
            new CommaWhitespaceRule(messages,
                Example.wrong("En su opinión<marker> ,</marker> no era verdad."),
                Example.fixed("En su opinión<marker>,</marker> no era verdad.")),
            new DoublePunctuationRule(messages),
            new SpanishUnpairedBracketsRule(messages),
            new QuestionMarkRule(messages),
            new MorfologikSpanishSpellerRule(messages, this, userConfig, altLanguages),
            new UppercaseSentenceStartRule(messages, this, 
                Example.wrong("Venta al público. <marker>ha</marker> subido mucho."),
                Example.fixed("Venta al público. <marker>Ha</marker> subido mucho.")),
            new SpanishWordRepeatRule(messages, this),
            new MultipleWhitespaceRule(messages, this),
            new SpanishWikipediaRule(messages),
            new SpanishWrongWordInContextRule(messages, this),
            new LongSentenceRule(messages, userConfig, 60),
            new LongParagraphRule(messages, this, userConfig),
            new SimpleReplaceRule(messages, this),
            new SimpleReplaceVerbsRule(messages, this),
            new SpanishWordRepeatBeginningRule(messages, this),
            new CompoundRule(messages, this, userConfig),
            new SpanishRepeatedWordsRule(messages)
    );
  }

  /** @since 3.1 */
  @Override
  public synchronized LanguageModel getLanguageModel(File indexDir) throws IOException {
    languageModel = initLanguageModel(indexDir, languageModel);
    return languageModel;
  }

  /** @since 3.1 */
  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel, UserConfig userConfig) throws IOException {
    return Arrays.asList(
            new SpanishConfusionProbabilityRule(messages, languageModel, this)
    );
  }
  
  /** @since 5.1 */
  public String getOpeningDoubleQuote() {
    return "«";
  }

  /** @since 5.1 */
  public String getClosingDoubleQuote() {
    return "»";
  }
  
  /** @since 5.1 */
  @Override
  public String getOpeningSingleQuote() {
    return "‘";
  }

  /** @since 5.1 */
  @Override
  public String getClosingSingleQuote() {
    return "’";
  }
  
  /** @since 5.1 */
  @Override
  public boolean isAdvancedTypographyEnabled() {
    return true;
  }

  /**
   * Closes the language model, if any. 
   * @since 3.1
   */
  @Override
  public void close() throws Exception {
    if (languageModel != null) {
      languageModel.close();
    }
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }
  
  private final static Map<String, Integer> id2prio = new HashMap<>();
  static {  
    id2prio.put("ES_SIMPLE_REPLACE_MULTIWORDS", 50);
    id2prio.put("LOS_MAPUCHE", 50);
    id2prio.put("TE_TILDE", 50);
    id2prio.put("DE_TILDE", 50); // greater than CONTRACCIONES
    id2prio.put("PLURAL_SEPARADO", 50);
    id2prio.put("PERSONAJES_FAMOSOS", 50);
    id2prio.put("NO_SEPARADO", 40);
    id2prio.put("PARTICIPIO_MS", 40);
    id2prio.put("VERBO_MODAL_INFINITIVO", 40); // greater than DIACRITICS
    id2prio.put("EL_NO_TILDE", 40); // greater than SE_CREO
    id2prio.put("SE_CREO", 35); // greater than DIACRITICS --> or less than DIACRITICS_VERB_N_ADJ ????
    id2prio.put("POR_CIERTO", 30);
    id2prio.put("DEGREE_CHAR", 30); // greater than SPACE_UNITIES
    id2prio.put("LO_LOS", 30);
    id2prio.put("ETCETERA", 30); // greater than other typography rules
    id2prio.put("P_EJ", 30); // greater than other typography rules
    id2prio.put("SE_CREO2", 25); 
    //id2prio.put("ESPACIO_DESPUES_DE_PUNTO", 25); // greater than other typography rules
    id2prio.put("AGREEMENT_ADJ_NOUN_AREA", 30); // greater than AGREEMENT_DET_NOUN
    id2prio.put("PRONOMBRE_SIN_VERBO", 25); // inside CONFUSIONS, but less than other rules ?
    id2prio.put("AGREEMENT_DET_ABREV", 25); // greater than AGREEMENT_DET_NOUN
    id2prio.put("MUCHO_NF", 25); // greater than AGREEMENT_DET_NOUN
    id2prio.put("AGREEMENT_DET_NOUN_EXCEPTIONS", 25); // greater than AGREEMENT_DET_NOUN
    id2prio.put("TYPOGRAPHY", 20); // greater than AGREEMENT_DET_NOUN
    id2prio.put("AGREEMENT_DET_NOUN", 15);
    //id2prio.put("PRONOMBRE_SIN_VERBO", 20);
    id2prio.put("AGREEMENT_DET_ADJ", 10);
    id2prio.put("CONFUSION_ES_SE", 20); //lower than diacrtics rules
    id2prio.put("HALLA_HAYA", 10);
    id2prio.put("VALLA_VAYA", 10);
    id2prio.put("SI_AFIRMACION", 10); // less than DIACRITICS
    id2prio.put("TE_TILDE2", 10); // less than PRONOMBRE_SIN_VERBO
    id2prio.put("SEPARADO", 1);
    id2prio.put("ES_SPLIT_WORDS", -10);
    id2prio.put("U_NO", -10);
    id2prio.put("EL_TILDE", -10);
    id2prio.put("SINGLE_CHARACTER", -15); // less than ES_SPLIT_WORDS
    id2prio.put("TOO_LONG_PARAGRAPH", -15);
    id2prio.put("PREP_VERB", -20);
    id2prio.put("SUBJUNTIVO_FUTURO", -30);
    id2prio.put("SUBJUNTIVO_PASADO", -30);
    id2prio.put("SUBJUNTIVO_PASADO2", -30);
    id2prio.put("AGREEMENT_ADJ_NOUN", -30);
    id2prio.put("AGREEMENT_PARTICIPLE_NOUN", -30);
    id2prio.put("AGREEMENT_POSTPONED_ADJ", -30);
    id2prio.put("MULTI_ADJ", -30);
    id2prio.put("SUBJUNTIVO_INCORRECTO", -40);
    id2prio.put("COMMA_SINO", -40);
    id2prio.put("COMMA_SINO2", -40);
    id2prio.put("VOSEO", -40);
    id2prio.put("REPETITIONS_STYLE", -50);
    id2prio.put("MORFOLOGIK_RULE_ES", -100);
    id2prio.put("PHRASE_REPETITION", -150);
    id2prio.put("SPANISH_WORD_REPEAT_RULE", -150);
    id2prio.put("UPPERCASE_SENTENCE_START", -200);
    id2prio.put("ES_QUESTION_MARK", -250);	  
  }

  @Override
  public Map<String, Integer> getPriorityMap() {
    return id2prio;
  }
  
  @Override
  protected int getPriorityForId(String id) {
    if (id.equals("CONFUSIONS2")) {
      return 50; // greater than CONFUSIONS
    }
    if (id.equals("RARE_WORDS")) {
      return 50;
    }
    if (id.equals("MISSPELLING")) {
      return 40;
    }
    if (id.equals("CONFUSIONS")) {
      return 40;
    }
    if (id.equals("INCORRECT_EXPRESSIONS")) {
      return 40;
    }
    if (id.equals("DIACRITICS")) {
      return 30;
    } 
    if (id.startsWith("ES_SIMPLE_REPLACE_SIMPLE")) {
      return 30;
    }
    if (id.startsWith("ES_COMPOUNDS")) {
      return 50;
    }
    Integer prio = id2prio.get(id);
    if (prio != null) {
      return prio;
    }

    if (id.startsWith("AI_ES_HYDRA_LEO")) { // prefer more specific rules (also speller)
      return -101;
    }
    if (id.startsWith("AI_ES_GGEC")) { // prefer more specific rules (also speller)
      if (id.equals("AI_ES_GGEC_REPLACEMENT_OTHER")) {
        return -300;
      }
      return 0;
      //return -102;
    }
    if (id.startsWith("ES_MULTITOKEN_SPELLING")) {
      return -95;
    }

    //STYLE is -50
    return super.getPriorityForId(id);
  }

  public boolean hasMinMatchesRules() {
    return true;
  }
  
  private static final Pattern ES_CONTRACTIONS = Pattern.compile("\\b([Aa]|[Dd]e) e(l)\\b");
  
  @Override
  public String adaptSuggestion(String replacement) {
    Matcher m = ES_CONTRACTIONS.matcher(replacement);
    String newReplacement = m.replaceAll("$1$2");
    return newReplacement;
  }

  @Override
  public List<String> prepareLineForSpeller(String line) {
    String[] parts = line.split("#");
    if (parts.length == 0) {
      return Arrays.asList(line);
    }
    String[] formTag = parts[0].split("[\t;]");
    if (formTag.length > 1) {
      String tag = formTag[1].trim();
      if (tag.startsWith("N") || tag.equals("_Latin_") || tag.equals("LOC_ADV")) {
        return Arrays.asList(formTag[0].trim());
      } else {
        return Arrays.asList("");
      }
    }
    return Arrays.asList(line);
  }

  public MultitokenSpeller getMultitokenSpeller() {
    return SpanishMultitokenSpeller.INSTANCE;
  }


  private List<String> suggestionsToAvoid = Arrays.asList("aquél", "aquélla", "aquéllas", "aquéllos", "ésa", "ésas",
    "ése", "ésos", "ésta", "éstas", "éste", "éstos", "sólo");
  private Pattern voseoPostagPatern = Pattern.compile("V....V.*");
  @Override
  public List<RuleMatch> mergeSuggestions(List<RuleMatch> ruleMatches, AnnotatedText text, Set<String> enabledRules) {
    List<RuleMatch> results = new ArrayList<>();
    for (RuleMatch ruleMatch : ruleMatches) {
      List<String> suggestions = ruleMatch.getSuggestedReplacements();
      if (suggestions.size()==1 && ruleMatch.getRule().getId().startsWith("AI_ES_GGEC")) {
        String suggestion = suggestions.get(0);
        // avoid obsolete diacritics
        if (suggestionsToAvoid.contains(suggestion.toLowerCase())) {
          continue;
        }
        // avoid lowercase at the sentence start
        if (ruleMatch.getSentence().getText().trim().startsWith(StringTools.uppercaseFirstChar(suggestion))) {
          continue;
        }
        // avoid voseo forms in suggestions
        List<AnalyzedTokenReadings> atr;
        try {
          atr = this.getTagger().tag(Arrays.asList(suggestion));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        if (atr != null && atr.size()>0) {
          if (atr.get(0).matchesPosTagRegex(voseoPostagPatern)) {
            continue;
          }
        }
      }
      results.add(ruleMatch);
    }

    return results;
  }
}
