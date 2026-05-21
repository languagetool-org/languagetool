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
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.UserConfig;
import org.languagetool.rules.Rule;
import org.languagetool.rules.pt.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class BrazilianPortuguese extends Portuguese {
  private static final String LANGUAGE_SHORT_CODE = "pt-BR";

  private static volatile Throwable instantiationTrace;

  public BrazilianPortuguese() {
    this(false);
    Throwable trace = instantiationTrace;
    if (trace != null) {
      throw new RuntimeException("Language was already instantiated, see the cause stacktrace below.", trace);
    }
    instantiationTrace = new Throwable();
  }

  protected BrazilianPortuguese(boolean fakeValue) {
    super(fakeValue);
  }

  @Override
  public String getName() {
    return "Portuguese (Brazil)";
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    List<Rule> rules = new ArrayList<>();
    rules.addAll(super.getRelevantRules(messages, userConfig, motherTongue, altLanguages));
    rules.add(new PostReformPortugueseCompoundRule(messages, this, userConfig));
    rules.add(new PostReformPortugueseDashRule(messages));
    rules.add(new BrazilianPortugueseReplaceRule(messages, "/pt/pt-BR/replace.txt", this));
    rules.add(new PortugueseBarbarismsRule(messages, "/pt/pt-BR/barbarisms.txt", this));
    rules.add(new PortugueseArchaismsRule(messages, "/pt/pt-BR/archaisms.txt", this));
    rules.add(new PortugueseClicheRule(messages, "/pt/pt-BR/cliches.txt", this));
    rules.add(new PortugueseRedundancyRule(messages, "/pt/pt-BR/redundancies.txt", this));
    rules.add(new PortugueseWordinessRule(messages, "/pt/pt-BR/wordiness.txt", this));
    rules.add(new PortugueseWikipediaRule(messages, "/pt/pt-BR/wikipedia.txt", this));
    return rules;
  }

  @Override
  public String[] getCountries() {
    return new String[]{"BR"};
  }

  public static @NotNull Portuguese getInstance() {
    Language language = Objects.requireNonNull(Languages.getLanguageForShortCode(LANGUAGE_SHORT_CODE));
    if (language instanceof Portuguese brazilianPortuguese) {
      return brazilianPortuguese;
    }
    throw new RuntimeException("BrazilianPortuguese language expected, got " + language);
  }
}
