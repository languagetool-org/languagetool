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
package de.danielnaber.languagetool.language;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.patterns.Unifier;
import de.danielnaber.languagetool.synthesis.Synthesizer;
import de.danielnaber.languagetool.synthesis.es.SpanishSynthesizer;
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tagging.disambiguation.Disambiguator;
import de.danielnaber.languagetool.tagging.disambiguation.rules.es.SpanishRuleDisambiguator;
import de.danielnaber.languagetool.tagging.es.SpanishTagger;
import de.danielnaber.languagetool.tokenizers.SRXSentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;

public class Spanish extends Language {
	
	private final SentenceTokenizer sentenceTokenizer = new SRXSentenceTokenizer("es");
	private final Synthesizer synthesizer = new SpanishSynthesizer();
  
  private static final String[] COUNTRIES = {
    "ES", "", "MX", "GT", "CR", "PA", "DO",
    "VE", "PE", "AR", "EC", "CL", "UY", "PY",
    "BO", "SV", "HN", "NI", "PR", "US", "CU"
  };

  private final Tagger tagger = new SpanishTagger();
  private final Disambiguator disambiguator = new SpanishRuleDisambiguator();
  private static final Unifier SPANISH_UNIFIER = new Unifier();

  public Locale getLocale() {
    return new Locale(getShortName());
  }

  public String getName() {
    return "Spanish";
  }

  public String getShortName() {
    return "es";
  }

  @Override
  public String[] getCountryVariants() {
    return COUNTRIES;
  }
  
  public Tagger getTagger() {
    return tagger;
  }
  
  public Disambiguator getDisambiguator() {
	    return disambiguator;
  }
  
  public Unifier getUnifier() {
	    return SPANISH_UNIFIER;
  }
  
  public final Synthesizer getSynthesizer() {
	    return synthesizer;
  }

  public final SentenceTokenizer getSentenceTokenizer() {
	    return sentenceTokenizer;
  }
  
  public Contributor[] getMaintainers() {
	  final Contributor contributor = new Contributor("Juan Martorell");
    contributor.setUrl("http://languagetool-es.blogspot.com/");
    return new Contributor[] { contributor };
  }

  public Set<String> getRelevantRuleIDs() {
    final Set<String> ids = new HashSet<String>();
    ids.add("COMMA_PARENTHESIS_WHITESPACE");
    ids.add("DOUBLE_PUNCTUATION");
    ids.add("UNPAIRED_BRACKETS");
    ids.add("UPPERCASE_SENTENCE_START");
    ids.add("WORD_REPEAT_RULE");
    ids.add("WHITESPACE_RULE");
    // specific to Spanish:
    // ids.add("EL_WITH_FEM");
    return ids;
  }

}
