/* LanguageTool, a natural language style checker 
 * Copyright (C) 2024 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.en;

import org.languagetool.Language;
import org.languagetool.Tag;
import org.languagetool.rules.AbstractSimpleReplaceRule2;
import org.languagetool.rules.Categories;
import org.languagetool.tools.Tools;

import java.util.*;

/**
 * A rule that matches words which should not be used. There are no suggestion.
 * Loads the relevant words from <code>rules/en/replace_profanity.txt</code>.
 */
public class SimpleReplaceProfanityRule extends AbstractSimpleReplaceRule2 {

  private static final Locale EN_LOCALE = new Locale("EN");

  public SimpleReplaceProfanityRule(ResourceBundle messages, Language language) {
    super(messages, language);
    //useSubRuleSpecificIds();
    setCategory(Categories.STYLE.getCategory(messages));
    this.setUrl(Tools.getUrl("https://en.wiktionary.org/wiki/Category:English_offensive_terms"));
    setTags(Collections.singletonList(Tag.picky));
    setRuleHasSuggestions(false);
  }

  @Override
  public List<String> getFileNames() {
    return Arrays.asList("/en/replace_profanity.txt");
  }

  @Override
  public final String getId() {
    return "PROFANITY";
  }

  @Override
  public String getDescription() {
    return "Profanity";
  }

  @Override
  public String getShort() {
    return "Profanity";
  }

  @Override
  public String getMessage() {
    return "This expression can be considered offensive.";
  }

  @Override
  public Locale getLocale() {
    return EN_LOCALE;
  }

}
