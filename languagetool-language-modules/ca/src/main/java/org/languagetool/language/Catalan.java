/* LanguageTool, a natural language style checker 
 * Copyright (C) 2009 Daniel Naber (http://www.danielnaber.de)
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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.UserConfig;
import org.languagetool.rules.*;
import org.languagetool.rules.ca.AccentuationCheckRule;
import org.languagetool.rules.ca.CatalanUnpairedBracketsRule;
import org.languagetool.rules.ca.CatalanUnpairedExclamationMarksRule;
import org.languagetool.rules.ca.CatalanUnpairedQuestionMarksRule;
import org.languagetool.rules.ca.CatalanWordRepeatRule;
import org.languagetool.rules.ca.CatalanWrongWordInContextDiacriticsRule;
import org.languagetool.rules.ca.CatalanWrongWordInContextRule;
import org.languagetool.rules.ca.ComplexAdjectiveConcordanceRule;
import org.languagetool.rules.ca.MorfologikCatalanSpellerRule;
import org.languagetool.rules.ca.ReflexiveVerbsRule;
import org.languagetool.rules.ca.ReplaceOperationNamesRule;
import org.languagetool.rules.ca.SimpleReplaceRule;
import org.languagetool.rules.ca.SimpleReplaceBalearicRule;
import org.languagetool.rules.ca.SimpleReplaceDNVRule;
import org.languagetool.rules.ca.SimpleReplaceDiacriticsIEC;
import org.languagetool.rules.ca.SimpleReplaceDiacriticsTraditional;
import org.languagetool.rules.ca.SimpleReplaceVerbsRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.ca.CatalanSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.ca.CatalanTagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.ca.CatalanHybridDisambiguator;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.ca.CatalanWordTokenizer;

public class Catalan extends Language {

  private static final Language DEFAULT_CATALAN = new Catalan();
  
  private Tagger tagger;
  private SentenceTokenizer sentenceTokenizer;
  private Tokenizer wordTokenizer;
  private Synthesizer synthesizer;
  private Disambiguator disambiguator;

  @Override
  public String getName() {
    return "Catalan";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"ES"}; // "AD", "FR", "IT"
  }
  
  @Override
  public String getShortCode() {
    return "ca";
  }

  @Override
  public Language getDefaultLanguageVariant() {
    return DEFAULT_CATALAN;
  }
  
  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] { new Contributor("Ricard Roca"), new Contributor("Jaume Ortolà") };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Arrays.asList(
            new CommaWhitespaceRule(messages, 
            		Example.wrong("A parer seu<marker> ,</marker> no era veritat."),
            		Example.fixed("A parer seu<marker>,</marker> no era veritat.")),
            new DoublePunctuationRule(messages),
            new CatalanUnpairedBracketsRule(messages, this),
            new UppercaseSentenceStartRule(messages, this,
            		Example.wrong("Preus de venda al públic. <marker>han</marker> pujat molt."),
            		Example.fixed("Preus de venda al públic. <marker>Han</marker> pujat molt.")),
            new MultipleWhitespaceRule(messages, this),
            new LongSentenceRule(messages, userConfig),
            // specific to Catalan:
            new CatalanWordRepeatRule(messages, this),
            new MorfologikCatalanSpellerRule(messages, this, userConfig, altLanguages),
            new CatalanUnpairedQuestionMarksRule(messages, this),
            new CatalanUnpairedExclamationMarksRule(messages, this),
            new AccentuationCheckRule(messages),
            new ComplexAdjectiveConcordanceRule(messages),
            new CatalanWrongWordInContextRule(messages),
            new CatalanWrongWordInContextDiacriticsRule(messages),
            new ReflexiveVerbsRule(messages),
            new SimpleReplaceVerbsRule(messages, this),
            new SimpleReplaceBalearicRule(messages),
            new SimpleReplaceRule(messages),
            new ReplaceOperationNamesRule(messages, this),
            new SimpleReplaceDNVRule(messages, this), // can be removed here after updating dictionaries
            new SimpleReplaceDiacriticsIEC(messages),
            new SimpleReplaceDiacriticsTraditional(messages)
    );
  }

  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new CatalanTagger(this);
    }
    return tagger;
  }

  @Override
  public Synthesizer getSynthesizer() {
    if (synthesizer == null) {
      synthesizer = new CatalanSynthesizer();
    }
    return synthesizer;
  }

  @Override
  public SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new SRXSentenceTokenizer(this);
    }
    return sentenceTokenizer;
  }
  
  @Override
  public Disambiguator getDisambiguator() {
    if (disambiguator == null) {
      disambiguator = new CatalanHybridDisambiguator();
    }
    return disambiguator;
  }  
  
  @Override
  public Tokenizer getWordTokenizer() {
    if (wordTokenizer == null) {
      wordTokenizer = new CatalanWordTokenizer();
    }
    return wordTokenizer;
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  @Override
  public int getPriorityForId(String id) {
    switch (id) {
      case "CA_SIMPLE_REPLACE_BALEARIC": return 100;
      case "CONCORDANCES_CASOS_PARTICULARS": return 30;
      case "CONFUSIONS_ACCENT": return 20;
      case "DIACRITICS": return 20;
      case "ACCENTUATION_CHECK": return 10;
      case "CONCORDANCES_DET_NOM": return 5;
      case "REGIONAL_VERBS": return -10;
      case "FALTA_ELEMENT_ENTRE_VERBS": return -10;
      case "FALTA_COMA_FRASE_CONDICIONAL": return -20;
      case "SUBSTANTIUS_JUNTS": return -25;
      case "MUNDAR": return -50;
      case "MORFOLOGIK_RULE_CA_ES": return -100;
      case "NOMBRES_ROMANS": return -400;
      case "UPPERCASE_SENTENCE_START": return -500;
    }
    return super.getPriorityForId(id);
  }
}
