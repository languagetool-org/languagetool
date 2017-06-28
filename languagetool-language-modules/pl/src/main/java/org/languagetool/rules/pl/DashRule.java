/* LanguageTool, a natural language style checker
 * Copyright (C) 2016 Marcin Miłkowski (http://www.languagetool.org)
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

package org.languagetool.rules.pl;

import org.languagetool.Languages;
import org.languagetool.rules.AbstractDashRule;
import org.languagetool.rules.patterns.PatternRule;

import java.io.IOException;
import java.util.List;

/**
 * Check for compounds written with dashes instead of hyphens (for example, Rabka — Zdrój).
 * @since 3.6
 */
public class DashRule extends AbstractDashRule {

  private static final List<PatternRule> dashRules = loadCompoundFile("/pl/compounds.txt",
          "Błędne użycie myślnika zamiast łącznika. Poprawnie: ", Languages.getLanguageForShortCode("pl"));

  public DashRule() throws IOException {
    super(dashRules);
  }

  @Override
  public String getDescription() {
    return "Sprawdza, czy wyrazy pisane z łącznikiem zapisano z myślnikami (np. „Lądek — Zdrój” zamiast „Lądek-Zdrój”).";
  }

}
