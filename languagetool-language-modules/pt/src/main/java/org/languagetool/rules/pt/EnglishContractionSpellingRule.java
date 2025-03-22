/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.pt;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.rules.*;

/**
 * A rule that matches words or phrases which should not be used and suggests
 * correct ones instead, e.g. {@code Hasnt} instead of {@code Hasn't}.
 * This was copied to Portuguese from English to help users with code-switching and improve
 * tagging tokens with _english_ignore_.
 *
 * @author Marcin Miłkowski
 * @since 2.5
 */
public class EnglishContractionSpellingRule extends AbstractSimpleReplaceRule {

  public static final String ENGLISH_CONTRACTION_SPELLING_RULE = "PT_ENGLISH_CONTRACTION_ORTHOGRAPHY";

  private static final Map<String, List<String>> wrongWords = loadFromPath("/pt/english_contractions.txt");
  private static final Locale PT_LOCALE = new Locale("pt");

  @Override
  public Map<String, List<String>> getWrongWords() {
    return wrongWords;
  }

  public EnglishContractionSpellingRule(ResourceBundle messages, Language language) {
    super(messages, language);
    super.setCategory(Categories.TYPOS.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Misspelling);
    addExamplePair(Example.wrong("Ele adorava assistir <marker>whats</marker> cooking às sextas-feiras."),
      Example.fixed("Ele adorava assistir <marker>what's</marker> cooking às sextas-feiras."));
//    setUrl(Tools.getUrl("https://languagetool.org/insights/post/grammar-contractions/"));
    super.setCheckLemmas(false);
  }

  @Override
  public final String getId() {
    return ENGLISH_CONTRACTION_SPELLING_RULE;
  }

  @Override
  public String getDescription() {
    return "Ortografia de contrações inglesas";
  }

  @Override
  public String getShort() {
    return "Erro de ortografia inglesa";
  }

  @Override
  public String getMessage(String tokenStr, List<String> replacements) {
    return "Caso seja uma contração da língua inglesa, prefira \"" + replacements.get(0) + "\".";
  }

  @Override
  public boolean isCaseSensitive() {
    return true;
  }

  @Override
  public Locale getLocale() {
    return PT_LOCALE;
  }

}

