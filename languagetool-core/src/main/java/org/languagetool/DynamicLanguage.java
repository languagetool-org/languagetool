/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.rules.patterns.AbstractPatternRule;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

abstract class DynamicLanguage extends Language {

  protected final String name;
  protected final String code;
  protected final File dictPath;

  DynamicLanguage(String name, String code, File dictPath) {
    this.name = Objects.requireNonNull(name);
    this.code = Objects.requireNonNull(code);
    this.dictPath = Objects.requireNonNull(dictPath);
  }

  @Override
  public String getShortCode() {
    return code;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public List<String> getRuleFileNames() {
    return Collections.emptyList();
  }

  @Override
  protected List<AbstractPatternRule> getPatternRules() {
    return Collections.emptyList();
  }

  @Override
  public String getCommonWordsPath() {
    return new File(dictPath.getParentFile(), "common_words.txt").getAbsolutePath();
  }

  @Override
  public String[] getCountries() { return new String[0]; }

  @Override
  public Contributor[] getMaintainers() { return new Contributor[0]; }

  @Override
  public boolean isSpellcheckOnlyLanguage() {
    return true;
  }

}
