/* LanguageTool, a natural language style checker 
 * Copyright (C) 2013 Daniel Naber (www.danielnaber.de)
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
package org.languagetool;

import org.languagetool.language.Contributor;
import org.languagetool.rules.Rule;
import org.languagetool.rules.patterns.AbstractPatternRule;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class FakeLanguage extends Language {

  private final String langCode;
  private final String country;

  public FakeLanguage() {
    this.langCode = "yy";
    this.country = "YY";
  }

  public FakeLanguage(String langCode) {
    this.langCode = langCode;
    this.country = "YY";
  }

  public FakeLanguage(String langCode, String country) {
    this.langCode = langCode;
    this.country = country;
  }

  @Override
  protected synchronized List<AbstractPatternRule> getPatternRules() throws IOException {
    return Collections.emptyList();
  }

  @Override
  public String getShortCode() {
    return langCode;
  }

  @Override
  public String getName() {
    return "FakeLanguage";
  }

  @Override
  public String[] getCountries() {
    return new String[] {country};
  }

  @Override
  public Contributor[] getMaintainers() {
    return null;
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) {
    return Collections.emptyList();
  }
}
