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
package org.languagetool.rules.ru;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.languagetool.Language;
import org.languagetool.rules.Example;
import org.languagetool.rules.GenericUnpairedBracketsRule;

public class RussianUnpairedBracketsRule extends GenericUnpairedBracketsRule {

  private static final List<String> RU_START_SYMBOLS = Arrays.asList("(", "{", "„", "\"", "'");
  private static final List<String> RU_END_SYMBOLS   = Arrays.asList(")", "}", "“", "\"", "'");
  private static final Pattern NUMERALS_RU = 
          Pattern.compile("(?i)\\d{1,2}?[а-я]*|[а-я]|[А-Я]|[а-я][а-я]|[А-Я][А-Я]|(?i)\\d{1,2}?[a-z']*|M*(D?C{0,3}|C[DM])(L?X{0,3}|X[LC])(V?I{0,3}|I[VX])$");

  public RussianUnpairedBracketsRule(ResourceBundle messages, Language language) {
    super(messages, RU_START_SYMBOLS, RU_END_SYMBOLS, NUMERALS_RU);
    addExamplePair(Example.wrong("Самоотверженный поступок Оленина <marker>(</marker>подарок Лукашке коня вызывает лишь удивление и усиливает недоверие к нему станичников."),
                   Example.fixed("Самоотверженный поступок Оленина <marker>(</marker>подарок Лукашке коня) вызывает лишь удивление и усиливает недоверие к нему станичников."));
  }

  @Override
  public String getId() {
    return "RU_UNPAIRED_BRACKETS";
  }
}
