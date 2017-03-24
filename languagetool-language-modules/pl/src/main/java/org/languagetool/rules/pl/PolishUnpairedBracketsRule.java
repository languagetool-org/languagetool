/* LanguageTool, a natural language style checker 
 * Copyright (C) 2010 Marcin Miłkowski (http://www.languagetool.org)
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

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.rules.Example;
import org.languagetool.rules.GenericUnpairedBracketsRule;

public class PolishUnpairedBracketsRule extends GenericUnpairedBracketsRule {

  private static final List<String> PL_START_SYMBOLS = Arrays.asList("[", "(", "{", "„", "»", "\"");
  private static final List<String> PL_END_SYMBOLS   = Arrays.asList("]", ")", "}", "”", "«", "\"");
  
  public PolishUnpairedBracketsRule(ResourceBundle messages, Language language) {
    super(messages, PL_START_SYMBOLS, PL_END_SYMBOLS);
    addExamplePair(Example.wrong("To jest zdanie z <marker>„</marker>cudzysłowem."),
                   Example.fixed("To jest zdanie z <marker>„</marker>cudzysłowem”."));
  }

  @Override
  public String getId() {
    return "PL_UNPAIRED_BRACKETS";
  }
}
