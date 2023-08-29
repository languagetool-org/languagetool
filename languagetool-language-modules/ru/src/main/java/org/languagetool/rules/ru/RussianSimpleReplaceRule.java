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
package org.languagetool.rules.ru;

import org.languagetool.rules.AbstractSimpleReplaceRule2;
import org.languagetool.rules.Example;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.languagetool.language.Russian;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Category;
import org.languagetool.rules.CategoryId;

/**
 * A rule that matches words or phrases which should not be used and suggests
 * correct ones instead. 
 *
 * Russian implementations. Loads the
 * relevant words from <code>rules/ru/replace.txt</code>.
 *
 * @author  Yakov Reztsov
 */
 

public class RussianSimpleReplaceRule extends AbstractSimpleReplaceRule2 {

  public static final String RUSSIAN_SIMPLE_REPLACE_RULE = "RU_SIMPLE_REPLACE";
  
  private static final Locale RU_LOCALE = new Locale("ru");

  public RussianSimpleReplaceRule(ResourceBundle messages) throws IOException {
    super(messages, new Russian());
    setLocQualityIssueType(ITSIssueType.Misspelling);
    setCategory(new Category(new CategoryId("MISC"), "Общие правила"));
    addExamplePair(Example.wrong("<marker>Экспрессо</marker> – крепкий кофе, приготовленный из хорошо обжаренных и тонко помолотых кофейных зёрен."),
                   Example.fixed("<marker>Эспрессо</marker> – крепкий кофе, приготовленный из хорошо обжаренных и тонко помолотых кофейных зёрен."));
  }

  @Override
  public List<String> getFileNames() {
    return Collections.singletonList("/ru/replace.txt");
  }

  @Override
  public final String getId() {
    return RUSSIAN_SIMPLE_REPLACE_RULE;
  }

  @Override
  public String getDescription() {
    return "Поиск просторечий и ошибочных фраз";
  }

  @Override
  public String getShort() {
    return "Ошибка?";
  }

  @Override
  public String getMessage() {
    return "«$match» — просторечие, исправление: $suggestions";
  }

  @Override
  public Locale getLocale() {
    return RU_LOCALE;
  }

}
