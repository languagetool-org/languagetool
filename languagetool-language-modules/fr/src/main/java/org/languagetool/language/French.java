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
import org.languagetool.rules.fr.*;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.rules.spelling.multitoken.MultitokenSpeller;
import org.languagetool.synthesis.FrenchSynthesizer;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.fr.FrenchHybridDisambiguator;
import org.languagetool.tagging.fr.FrenchTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.fr.FrenchWordTokenizer;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;


public class French extends LanguageWithModel {
  private static final String BEFORE_APOS = "([cjnmtsldCJNMTSLD]|qu|jusqu|lorsqu|puisqu|quoiqu|Qu|Jusqu|Lorsqu|Puisqu|Quoiqu|QU|JUSQU|LORSQU|PUISQU|QUOIQU)";
  private static final Pattern BEFORE_APOS_PATTERN_1 = compile("(\\b" + BEFORE_APOS + ")'");
  private static final Pattern BEFORE_APOS_PATTERN_2 = compile("(\\b" + BEFORE_APOS + ")’\"");
  private static final Pattern BEFORE_APOS_PATTERN_3 = compile("(\\b" + BEFORE_APOS + ")’'");

  private static final Pattern TYPOGRAPHY_PATTERN_1 = compile("\u00a0;");
  private static final Pattern TYPOGRAPHY_PATTERN_2 = compile("\u00a0!");
  private static final Pattern TYPOGRAPHY_PATTERN_3 = compile("\u00a0\\?");
  private static final Pattern TYPOGRAPHY_PATTERN_4 = compile(";");
  private static final Pattern TYPOGRAPHY_PATTERN_5 = compile("!");
  private static final Pattern TYPOGRAPHY_PATTERN_6 = compile("\\?");
  private static final Pattern TYPOGRAPHY_PATTERN_7 = compile(":");
  private static final Pattern TYPOGRAPHY_PATTERN_8 = compile("»");
  private static final Pattern TYPOGRAPHY_PATTERN_9 = compile("«");
  private static final Pattern TYPOGRAPHY_PATTERN_10 = compile("\u00a0\u00a0");
  private static final Pattern TYPOGRAPHY_PATTERN_11 = compile("\u202f\u202f");
  private static final Pattern TYPOGRAPHY_PATTERN_12 = compile("  ");
  private static final Pattern TYPOGRAPHY_PATTERN_13 = compile("\u00a0 ");
  private static final Pattern TYPOGRAPHY_PATTERN_14 = compile(" \u00a0");
  private static final Pattern TYPOGRAPHY_PATTERN_15 = compile(" \u202f");
  private static final Pattern TYPOGRAPHY_PATTERN_16 = compile("\u202f ");

  private static final String FRENCH_SHORT_CODE = "fr";

  private static volatile Throwable instantiationTrace;

  /**
   * @deprecated don't use this method besides the inheritance or core code. Languages are not supposed to be
   * instantiated multiple times. They may contain heavy data which may waste the memory.
   * Use {@link #getInstance()} instead.
   */
  @Deprecated
  public French() {
    Throwable trace = instantiationTrace;
    if (trace != null) {
      throw new RuntimeException("Language was already instantiated, see the cause stacktrace below.", trace);
    }
    instantiationTrace = new Throwable();
  }

  /**
   * This is a fake constructor overload for the subclasses. Public constructors can only be used by the LT itself.
   */
  protected French(boolean fakeValue) {
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }

  @Override
  public String getName() {
    return "French";
  }

  @Override
  public String getShortCode() {
    return FRENCH_SHORT_CODE;
  }

  @Override
  public String[] getCountries() {
    return new String[]{"FR", "", "LU", "MC", "CM",  "CI", "HT", "ML", "SN", "CD", "MA", "RE"};
    //  "BE", "CH", "CA",
  }

  @Override
  public Language getDefaultLanguageVariant() {
    return Languages.getLanguageForShortCode(FRENCH_SHORT_CODE);
  }
  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return FrenchTagger.INSTANCE;
  }

  @Nullable
  @Override
  public Synthesizer createDefaultSynthesizer() {
    return FrenchSynthesizer.INSTANCE;
  }
  
  @Override
  public Tokenizer createDefaultWordTokenizer() {
    return new FrenchWordTokenizer();
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new FrenchHybridDisambiguator();
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {
        Contributors.DOMINIQUE_PELLE
    };
  }


  @Override
  public SpellingCheckRule createDefaultSpellingRule(ResourceBundle messages) throws IOException {
    return new MorfologikFrenchSpellerRule(messages, this, null, Collections.emptyList());
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Arrays.asList(
            new CommaWhitespaceRule(messages, false),
            new DoublePunctuationRule(messages),
            new GenericUnpairedBracketsRule(messages,
                    Arrays.asList("[", "(", "{" /*"«", "‘"*/),
                    Arrays.asList("]", ")", "}"
                         /*"»", French dialog can contain multiple sentences. */
                         /*"’" used in "d’arm" and many other words */)),
            userConfig == null && altLanguages.isEmpty() ? getDefaultSpellingRule() : new MorfologikFrenchSpellerRule(messages, this, userConfig, altLanguages),
            new UppercaseSentenceStartRule(messages, this),
            new MultipleWhitespaceRule(messages, this),
            new SentenceWhitespaceRule(messages),
            new LongSentenceRule(messages, userConfig, 40),
            new LongParagraphRule(messages, this, userConfig),
            // specific to French:
            new CompoundRule(messages, this, userConfig),
            new QuestionWhitespaceStrictRule(messages, this),
            new QuestionWhitespaceRule(messages, this),
            new SimpleReplaceRule(messages, this),
            new FrenchRepeatedWordsRule(messages)
    );
  }

  @Override
  public List<Rule> getRelevantRulesGlobalConfig(ResourceBundle messages, GlobalConfig globalConfig, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    List<Rule> rules = new ArrayList<>();
    if (globalConfig != null && globalConfig.getGrammalecteServer() != null) {
      rules.add(new GrammalecteRule(messages, globalConfig));
    }
    return rules;
  }

  /** @since 3.1 */
  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel, UserConfig userConfig) throws IOException {
    return Collections.singletonList(
      new FrenchConfusionProbabilityRule(messages, languageModel, this)
    );
  }

  /** @since 5.1 */
  @Override
  public String getOpeningDoubleQuote() {
    return "«";
  }

  /** @since 5.1 */
  @Override
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
  
  @Override
  public String toAdvancedTypography (String input) {
    String output = super.toAdvancedTypography(input);
  
    // special cases: apostrophe + quotation marks
    output = BEFORE_APOS_PATTERN_1.matcher(output).replaceAll("$1’");
    output = BEFORE_APOS_PATTERN_2.matcher(output).replaceAll("$1’" + getOpeningDoubleQuote());
    output = BEFORE_APOS_PATTERN_3.matcher(output).replaceAll("$1’" + getOpeningSingleQuote());

    // non-breaking (thin) space 
    // according to https://fr.wikipedia.org/wiki/Espace_ins%C3%A9cable#En_France
    output = TYPOGRAPHY_PATTERN_1.matcher(output).replaceAll("\u202f;");
    output = TYPOGRAPHY_PATTERN_2.matcher(output).replaceAll("\u202f!");
    output = TYPOGRAPHY_PATTERN_3.matcher(output).replaceAll("\u202f?");
    output = TYPOGRAPHY_PATTERN_4.matcher(output).replaceAll("\u202f;");
    output = TYPOGRAPHY_PATTERN_5.matcher(output).replaceAll("\u202f!");
    output = TYPOGRAPHY_PATTERN_6.matcher(output).replaceAll("\u202f?");

    output = TYPOGRAPHY_PATTERN_7.matcher(output).replaceAll("\u00a0:");
    output = TYPOGRAPHY_PATTERN_8.matcher(output).replaceAll("\u00a0»");
    output = TYPOGRAPHY_PATTERN_9.matcher(output).replaceAll("«\u00a0");
    
    //remove duplicate spaces
    output = TYPOGRAPHY_PATTERN_10.matcher(output).replaceAll("\u00a0");
    output = TYPOGRAPHY_PATTERN_11.matcher(output).replaceAll("\u202f");
    output = TYPOGRAPHY_PATTERN_12.matcher(output).replaceAll(" ");
    output = TYPOGRAPHY_PATTERN_13.matcher(output).replaceAll("\u00a0");
    output = TYPOGRAPHY_PATTERN_14.matcher(output).replaceAll("\u00a0");
    output = TYPOGRAPHY_PATTERN_15.matcher(output).replaceAll("\u202f");
    output = TYPOGRAPHY_PATTERN_16.matcher(output).replaceAll("\u202f");

    return output;
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  private final static Map<String, Integer> id2prio = new HashMap<>();
  static {
    id2prio.put("AGREEMENT_EXCEPTIONS", 100); // greater than D_N
    id2prio.put("EXPRESSIONS_VU", 100); // greater than A_ACCENT_A
    id2prio.put("SA_CA_SE", 100); // greater than D_N
    id2prio.put("SIL_VOUS_PLAIT", 100); // greater than ACCORD_R_PERS_VERBE
    id2prio.put("QUASI_NOM", 100); // greater than D_N
    id2prio.put("MA", 100); // greater than D_J
    id2prio.put("SON_SONT", 100); // greater than D_J
    id2prio.put("JE_TES", 100); // greater than D_J
    id2prio.put("A_INFINITIF", 100);
    id2prio.put("ON_ONT", 100); // greater than PRONSUJ_NONVERBE
    id2prio.put("LEURS_LEUR", 100); // greater than N_V
    id2prio.put("DU_DU", 100); // greater than DU_LE
    id2prio.put("ACCORD_CHAQUE", 100); // greater than ACCORD_NOMBRE
    id2prio.put("J_N2", 100); // greater than J_N
    id2prio.put("CEST_A_DIRE", 100); // greater than A_A_ACCENT
    id2prio.put("FAIRE_VPPA", 100); // greater than A_ACCENT_A
    id2prio.put("D_N_E_OU_E", 100); // greater than D_N
    id2prio.put("GENS_ACCORD", 100); // greater than AGREEMENT_POSTPONED_ADJ
    id2prio.put("VIRGULE_EXPRESSIONS_FIGEES", 100); // greater than agreement rules
    id2prio.put("TRAIT_UNION", 100); // greater than other rules for trait d'union
    id2prio.put("PLURIEL_AL2", 100); // greater than other rules for pluriel al
    id2prio.put("FR_SPLIT_WORDS_HYPHEN", 100); // greater than MOTS_INCOMP
    id2prio.put("PAS_DE_TRAIT_UNION", 50); //  // greater than agreement rules
    id2prio.put("SOCIOCULTUREL", 40); // greater than TIRET
    id2prio.put("A_VERBE_INFINITIF", 20); // greater than PRONSUJ_NONVERBE
    id2prio.put("DE_OU_DES", 20); // greater than PAS_ADJ
    id2prio.put("EMPLOI_EMPLOIE", 20); // greater than MOTS_INCOMP
    id2prio.put("VOIR_VOIRE", 20); // greater than PLACE_DE_LA_VIRGULE
    id2prio.put("D_VPPA", 20); //greater than D_J
    id2prio.put("EST_CE_QUE", 20); // greater than TRAIT_UNION_INVERSION
    id2prio.put("CONFUSION_PARLEZ_PARLER", 10); // greater than N_V
    id2prio.put("AGREEMENT_TOUT_LE", 10); // compare to TOUT_LES
    id2prio.put("ESPACE_UNITES", 10); // needs to have higher priority than spell checker
    id2prio.put("BYTES", 10); // needs to be higher than spell checker for 10MB style matches
    id2prio.put("Y_A", 10); // needs to be higher than spell checker for style suggestion
    id2prio.put("COTE", 10); // needs to be higher than D_N
    id2prio.put("PEUTETRE", 10); // needs to be higher than AUX_ETRE_VCONJ
    id2prio.put("A_A_ACCENT", 10); // triggers false alarms for IL_FAUT_INF if there is no a/à correction
    id2prio.put("A_ACCENT_A", 10); // greater than PRONSUJ_NONVERBE
    id2prio.put("A_A_ACCENT2", 10); // greater than ACCORD_SUJET_VERBE
    id2prio.put("A_ACCENT", 10); // greater than ACCORD_SUJET_VERBE
    id2prio.put("JE_M_APPEL", 10);  // override NON_V
    id2prio.put("ACCORD_R_PERS_VERBE", 10);  // match before POSER_UNE_QUESTION
    id2prio.put("JE_SUI", 10);  // needs higher priority than spell checker
    //id2prio.put("D_N", 10); // needs to have higher priority than agreement postponed adj | Commented out because many other rules should be higher: CAT_REGIONALISMES, CAT_TYPOGRAPHIE, CAT_GRAMMAIRE...
    //id2prio.put("ACCORD_COULEUR", 1); // needs to have higher priority than agreement postponed adj
    id2prio.put("R_VAVOIR_VINF", 10); // needs higher priority than A_INFINITIF
    id2prio.put("AN_EN", 10); // needs higher priority than AN_ANNEE
    id2prio.put("APOS_M", 10); // needs higher priority than APOS_ESPACE
    id2prio.put("ACCORD_PLURIEL_ORDINAUX", 10); // needs higher priority than D_J
    id2prio.put("SUJET_AUXILIAIRE", 10); // needs higher priority than JE_VERBE; TU_VERBE; IL_VERBE; ILS_VERBE; ON_VERBE;
    id2prio.put("ADJ_ADJ_SENT_END", 10); // needs higher priority than ACCORD_COULEUR
    id2prio.put("OU_PAS", 10); // needs higher priority than VERBE_OBJ
    id2prio.put("PLACE_DE_LA_VIRGULE", 10); // needs higher priority than C_EST_QUOI
    id2prio.put("SE_CE", -10); // needs higher priority than ELISION
    id2prio.put("PAS_DE_SOUCIS", 10); // needs higher priority than PAS_DE_PB_SOUCIS (premium)
    //id2prio.put("PRONSUJ_NONVERBE", 10); // needs higher priority than AUXILIAIRE_MANQUANT
    //id2prio.put("AUXILIAIRE_MANQUANT", 5); // needs higher priority than ACCORD_NOM_VERBE
    id2prio.put("J_N", -10); // needs lesser priority than D_J
    id2prio.put("TE_NV", -20); // less than SE_CE, SE_SA and SE_SES
    id2prio.put("TE_NV2", -10); // less than SE_CE, SE_SA and SE_SES
    id2prio.put("INTERROGATIVE_DIRECTE", -10); // less than OU
    id2prio.put("V_J_A_R", -10); // less than grammar rules
    id2prio.put("TRES_TRES_ADJ", -10); // less than grammar rules
    id2prio.put("IMP_PRON", -10); // less than D_N
    id2prio.put("TOO_LONG_PARAGRAPH", -15);
    id2prio.put("PREP_VERBECONJUGUE", -20);
    id2prio.put("LA_LA2", -20); // less than LA_LA
    id2prio.put("FRENCH_WORD_REPEAT_RULE", -20); // less than TRES_TRES_ADJ
    id2prio.put("PAS_DE_VERBE_APRES_POSSESSIF_DEMONSTRATIF", -20);
    id2prio.put("VIRGULE_VERBE", -20); // less than grammar rules
    id2prio.put("VERBES_FAMILIERS", -25);  // less than PREP_VERBECONJUGUE + PAS_DE_VERBE_APRES_POSSESSIF_DEMONSTRATIF
    id2prio.put("VERB_PRONOUN", -50); // greater than FR_SPELLING_RULE; less than ACCORD_V_QUESTION
    id2prio.put("IL_VERBE", -50); // greater than FR_SPELLING_RULE
    id2prio.put("A_LE", -50); // less than A_ACCENT
    id2prio.put("ILS_VERBE", -50); // greater than FR_SPELLING_RULE
    id2prio.put("AGREEMENT_POSTPONED_ADJ", -50);
    id2prio.put("MULTI_ADJ", -50);
    id2prio.put("PARENTHESES", -50);// less than grammar rules
    id2prio.put("REP_ESSENTIEL", -50); // lesser than grammar rules
    id2prio.put("CONFUSION_AL_LA", -50); // lesser than AUX_AVOIR_VCONJ
    id2prio.put("LE_COVID", -60); // lower than COVID_19_GRAPHIE
    id2prio.put("FR_SPELLING_RULE", -100);
    id2prio.put("VIRG_INF", -100);// lesser than CONFUSION_E_ER
    id2prio.put("ET_SENT_START", -151); // lower than grammalecte rules
    id2prio.put("MAIS_SENT_START", -151); // lower than grammalecte rules
    id2prio.put("EN_CE_QUI_CONCERNE", -152);  // less than MAIS_SENT_START + ET_SENT_START
    id2prio.put("EN_MEME_TEMPS", -152);  // less than MAIS_SENT_START + ET_SENT_START
    id2prio.put("ET_AUSSI", -152);  // less than CONFUSION_EST_ET + ET_SENT_START
    id2prio.put("MAIS_AUSSI", -152);  // less than MAIS_SENT_START
    id2prio.put("ELISION", -200); // should be lower in priority than spell checker
    id2prio.put("POINT", -200); // should be lower in priority than spell checker
    id2prio.put("REPETITIONS_STYLE", -250);  // repetition style rules, usually with prefix REP_
    id2prio.put("POINTS_SUSPENSIONS_SPACE", -250);  // should be lower in priority than ADJ_ADJ_SENT_END
    id2prio.put("UPPERCASE_SENTENCE_START", -300);
    id2prio.put("FRENCH_WHITESPACE_STRICT", -350); // picky; if on, it should overwrite FRENCH_WHITESPACE
    id2prio.put("TOUT_MAJUSCULES", -400);
    id2prio.put("VIRG_NON_TROUVEE", -400);
    id2prio.put("POINTS_2", -400);
    id2prio.put("MOTS_INCOMP", -400); // greater than PRONSUJ_NONVERBE and DUPLICATE_DETERMINER
    id2prio.put("FRENCH_WHITESPACE", -400); // lesser than UPPERCASE_SENTENCE_START and FR_SPELLING_RULE
    id2prio.put("MOT_TRAIT_MOT", -400); // lesser than UPPERCASE_SENTENCE_START and FR_SPELLING_RULE
    id2prio.put("FRENCH_WORD_REPEAT_BEGINNING_RULE", -350); // less than REPETITIONS_STYLE
  }

  @Override
  public Map<String, Integer> getPriorityMap() {
    return id2prio;
  }

  @Override
  protected int getPriorityForId(String id) {
    Integer prio = id2prio.get(id);
    if (prio != null) {
      return prio;
    }
    if (id.startsWith("FR_COMPOUNDS")) {
      return 500;
    }
    if (id.equals("CAT_TYPOGRAPHIE")) {
      return 20; // greater than PRONSUJ_NONVERBE or agreement rules
    }
    if (id.equals("CAT_TOURS_CRITIQUES")) {
      return 20;
    }
    if (id.equals("CAT_HOMONYMES_PARONYMES")) {
      return 20;
    }
    if (id.equals("SON")) {
      return -5; // less than ETRE_VPPA_OU_ADJ
    }
    if (id.startsWith("CAR")) {
      return -50; // lesser than grammar rules
    }
    if (id.equals("CONFUSION_RULE_PREMIUM")) {
      return -50; // lesser than PRONSUJ_NONVERBE
    }
    if (id.startsWith("FR_MULTITOKEN_SPELLING_")) {
      return -90;
    }
    if (id.startsWith("FR_SIMPLE_REPLACE")) {
      return 150;
    }
    if (id.startsWith("grammalecte_")) {
      return -150;
    }

    if (id.startsWith("AI_FR_HYDRA_LEO")) { // prefer more specific rules (also speller)
      return -101;
    }

    if (id.startsWith("AI_FR_GGEC_REPLACEMENT_ORTHOGRAPHY")) { // prefer more specific rules (also speller)
      return -101;
    }
    return super.getPriorityForId(id);
  }
  
  public boolean hasMinMatchesRules() {
    return true;
  }


  @Override
  public List<RuleMatch> filterRuleMatches(List<RuleMatch> ruleMatches, AnnotatedText text, Set<String> enabledRules) {
    List<RuleMatch> resultMatches = new ArrayList<>();
    RuleMatch previousMatch = null;
    for (int i = 0; i < ruleMatches.size(); i++) {
      RuleMatch currentMatch = adjustFrenchRuleMatch(ruleMatches.get(i), enabledRules);
      List<String> suggestions = currentMatch.getSuggestedReplacements();
      // ignore adding punctuation at the sentence end
      if (suggestions.size()==1 && currentMatch.getRule().getId().startsWith("AI_FR_GGEC")) {
        String suggestion = suggestions.get(0);
        if (currentMatch.getRule().getId().equals("AI_FR_GGEC_MISSING_PUNCTUATION_PERIOD") && suggestion.endsWith(".")) {
          if (currentMatch.getSentence().getText().replaceAll("\\s+$", "").endsWith(suggestion.substring(0, suggestion.length() - 1))) {
            continue;
          }
        }
      }
      if (previousMatch != null && previousMatch.getRule().getId().startsWith("AI_FR_GGEC") &&
              currentMatch.getRule().getId().startsWith("AI_FR_GGEC")) {
        if (previousMatch.getToPos() > currentMatch.getFromPos()) {
          continue;  // Skip overlapping matches
        }
        // Check if matches are adjacent and share the same 'picky' status
        if ((previousMatch.getToPos() == currentMatch.getFromPos() || previousMatch.getToPos() + 1 == currentMatch.getFromPos()) &&
                (previousMatch.getRule().getTags().contains(Tag.picky) == currentMatch.getRule().getTags().contains(Tag.picky))) {
          // Merge if they have the same ITSIssueType
          if (previousMatch.getRule().getLocQualityIssueType() == currentMatch.getRule().getLocQualityIssueType()) {
            RuleMatch mergedMatch = new RuleMatch(mergeMatches(previousMatch, currentMatch));
            previousMatch = mergedMatch;
            continue;
          }
          // If matches have different ITSIssueTypes but neither is a style match, merge them
          if (previousMatch.getRule().getLocQualityIssueType() != currentMatch.getRule().getLocQualityIssueType() &&
                  previousMatch.getRule().getLocQualityIssueType() != ITSIssueType.Style && currentMatch.getRule().getLocQualityIssueType() != ITSIssueType.Style) {
            RuleMatch mergedMatch = new RuleMatch(mergeMatches(previousMatch, currentMatch));
            previousMatch = mergedMatch;
            continue;
          }
        }
        // If no merge happened, add the previous match to results
        resultMatches.add(previousMatch);
        previousMatch = currentMatch;  // Move to next match
      } else {
        // Ensure current match becomes previous if no merging criteria are met
        if (previousMatch != null) {
          resultMatches.add(previousMatch);
        }
        previousMatch = currentMatch;
      }
    }
    // Add the last processed match if it exists and hasn't been added yet
    if (previousMatch != null) {
      resultMatches.add(previousMatch);
    }
    return resultMatches;
  }

  private RuleMatch mergeMatches(RuleMatch match1, RuleMatch match2) {
    // Calculate separator based on position
    String separator = "";
    if (match1.getToPos() + 1 == match2.getFromPos()) {
      separator = " ";
    }
    // Merge original error strings and suggested replacements
    String newErrorStr = match1.getOriginalErrorStr() + separator + match2.getOriginalErrorStr();
    String newReplacement = match1.getSuggestedReplacements().get(0) + separator + match2.getSuggestedReplacements().get(0);

    // Create a new merged RuleMatch object
    RuleMatch mergedMatch = new RuleMatch(match1.getRule(), match1.getSentence(), match1.getFromPos(), match2.getToPos(),
            "Il pourrait y avoir un problème ici.", "Erreur potentielle");
    mergedMatch.setOriginalErrorStr(newErrorStr);
    mergedMatch.setSuggestedReplacement(newReplacement);

    // Set ID based on issue type
    String newId = "AI_FR_MERGED_MATCH";
    if (match1.getRule().getLocQualityIssueType().equals(ITSIssueType.Style) &&
            match2.getRule().getLocQualityIssueType().equals(ITSIssueType.Style)) {
      newId += "_STYLE";
    }
    if (match1.getRule().getTags().contains(Tag.picky) &&
            match2.getRule().getTags().contains(Tag.picky)) {
      newId += "_PICKY";
    }
    mergedMatch.setSpecificRuleId(newId);

    // Set ITSIssueType to Grammar unless both are Style
    if (!match1.getRule().getLocQualityIssueType().equals(match2.getRule().getLocQualityIssueType())) {
      mergedMatch.getRule().setLocQualityIssueType(ITSIssueType.Grammar);
    } else if (match1.getRule().getLocQualityIssueType() == ITSIssueType.Style) {
      mergedMatch.getRule().setLocQualityIssueType(ITSIssueType.Style);
    }
    return mergedMatch;
  }

  private final List<String> spellerExceptions = Collections.singletonList("Ho Chi Minh");

  @Override
  public List<String> prepareLineForSpeller(String line) {
    String[] parts = line.split("#");
    if (parts.length == 0) {
      return Collections.singletonList(line);
    }
    String[] formTag = parts[0].split("[\t;]");
    String form = formTag[0].trim();
    if (spellerExceptions.contains(form)) {
      return Collections.singletonList("");
    }
    if (formTag.length > 1) {
      String tag = formTag[1].trim();
      if (tag.startsWith("Z") || tag.startsWith("N") || tag.equals("A") ) {
        return Collections.singletonList(form);
      } else {
        return Collections.singletonList("");
      }
    }
    return Collections.singletonList(line);
  }

  public MultitokenSpeller getMultitokenSpeller() {
    return FrenchMultitokenSpeller.INSTANCE;
  }

  public RuleMatch adjustFrenchRuleMatch(RuleMatch rm, Set<String> enabledRules) {
    rm.setOriginalErrorStr();
    String errorStr = rm.getOriginalErrorStr();
    List<String> suggestions = rm.getSuggestedReplacements();
    if (suggestions.size() == 1 && rm.getRule().getId().startsWith("AI_FR_GGEC")) {
      String suggestion = suggestions.get(0);
      // the suggestion only changes the casing
      if (suggestion.equalsIgnoreCase(errorStr)) {
        rm.setMessage("Un usage différent des majuscules et des minuscules est recommandé.");
        rm.setShortMessage("Majuscules et minuscules");
        rm.getRule().setLocQualityIssueType(ITSIssueType.Typographical);
        rm.getRule().setCategory(Categories.CASING.getCategory(ResourceBundleTools.getMessageBundle(this)));
        rm.setSpecificRuleId(rm.getRule().getId().replace("ORTHOGRAPHY", "CASING"));
        //ruleMatch.getRule().setTags(Arrays.asList(Tag.picky));
      }
    }
    if (suggestions.size() == 1 &&  rm.getRule().getId().startsWith("AI_FR_GGEC")
      && rm.getRule().getId().contains("MISSING_PRONOUN_LAPOSTROPHE")) {
      if (errorStr.equals("on") && suggestions.get(0).equals("l'on") && rm.getSentence().getText().toLowerCase().contains("si on")) {
        rm.setSpecificRuleId("AI_FR_GGEC_SI_LON");
        rm.getRule().setTags(Collections.singletonList(Tag.picky));
      }
    }
    if (rm.getRule().getId().startsWith("AI_FR_GGEC") && rm.getRule().getId().contains("REPLACEMENT_PUNCTUATION_QUOTE"
    )) {
      rm.setSpecificRuleId("AI_FR_GGEC_QUOTES");
      rm.getRule().setTags(Collections.singletonList(Tag.picky));
      rm.getRule().setLocQualityIssueType(ITSIssueType.Typographical);
    }
    // if the typographical apostrophe rule is enabled, use the typographical apostrophe in suggestons
    if (enabledRules != null && enabledRules.contains("APOS_TYP")) {
      List<String> newReplacements = new ArrayList<>();
      for (String s : rm.getSuggestedReplacements()) {
        if (s.length() > 1) {
          s = s.replace('\'', '’');
        }
        newReplacements.add(s);
      }
      rm.setSuggestedReplacements(newReplacements);
    }
    return rm;
  }

  public static @NotNull French getInstance() {
    Language language = Objects.requireNonNull(Languages.getLanguageForShortCode(FRENCH_SHORT_CODE));
    if (language instanceof French french) { // cannot use French here as in premium FRENCH_SHORT_CODE returns FrenchPremium
      return french;
    }
    throw new RuntimeException("French(Premium) language expected, got " + language);
  }
}