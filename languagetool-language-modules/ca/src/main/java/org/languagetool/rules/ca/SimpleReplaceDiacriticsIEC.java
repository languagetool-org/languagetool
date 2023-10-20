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
package org.languagetool.rules.ca;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.language.Catalan;
import org.languagetool.rules.AbstractSimpleReplaceRule;
import org.languagetool.rules.Category;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.ITSIssueType;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Dicritics with IEC rules. 
 * 
 * Catalan implementations. Loads the
 * relevant word forms from <code>rules/ca/replace_diacritics_iec.txt</code>.
 * 
 * @author Jaume Ortolà
 */
public class SimpleReplaceDiacriticsIEC extends AbstractSimpleReplaceRule {

  private static final Map<String, List<String>> wrongWords = loadFromPath("/ca/replace_diacritics_iec.txt");
  private static final Locale CA_LOCALE = new Locale("CA");

  @Override
  public Map<String, List<String>> getWrongWords() {
    return wrongWords;
  }
  
  public SimpleReplaceDiacriticsIEC(ResourceBundle messages, Language language) throws IOException {
    super(messages, language);
    super.setCategory(new Category(new CategoryId("DIACRITICS_IEC"), "Z) Accents diacrítics segons l'IEC"));
    super.setLocQualityIssueType(ITSIssueType.Misspelling);
    super.setDefaultOn();
    this.setCheckLemmas(false);
    super.useSubRuleSpecificIds();
  }  

  @Override
  public final String getId() {
    return "CA_SIMPLE_REPLACE_DIACRITICS_IEC";
  }

 @Override
  public String getDescription() {
    return "Accents diacrítics segons les normes noves (2017): $match";
  }

  @Override
  public String getShort() {
    return "Hi sobra l'accent.";
  }
  
  @Override
  public String getMessage(String tokenStr,List<String> replacements) {
    return "Hi sobra l'accent diacrític (segons les normes noves; desactiveu la regla si voleu les normes tradicionals).";
  }
  
  @Override
  public boolean isCaseSensitive() {
    return false;
  }

  @Override
  public Locale getLocale() {
    return CA_LOCALE;
  }

  @Override
  protected boolean isTokenException(AnalyzedTokenReadings atr) {
    return atr.hasPosTagStartingWith("NP");
  }
}
