/* LanguageTool, a natural language style checker
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.km;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.rules.GenericUnpairedBracketsRule;

public class KhmerUnpairedBracketsRule extends GenericUnpairedBracketsRule {
  
  private static final List<String> KM_START_SYMBOLS = Arrays.asList("[", "(", "{", "“", "\"", "'", "«");
  private static final List<String> KM_END_SYMBOLS   = Arrays.asList("]", ")", "}", "”", "\"", "'", "»");
  
  public KhmerUnpairedBracketsRule(ResourceBundle messages, Language language) {
    super(messages, KM_START_SYMBOLS, KM_END_SYMBOLS);
  }

  @Override
  public String getId() {
    return "KM_UNPAIRED_BRACKETS";
  }
}
