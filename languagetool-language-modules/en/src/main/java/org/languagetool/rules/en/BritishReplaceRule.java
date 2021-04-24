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
package org.languagetool.rules.en;

import org.languagetool.Languages;
import org.languagetool.language.BritishEnglish;
import org.languagetool.rules.AbstractSimpleReplaceRule2;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.ITSIssueType;


import java.util.*;

/**
 * A rule that matches words or phrases which should not be used and suggests
 * correct ones instead.
 *
 * @author Marcin Miłkowski
 */
public class BritishReplaceRule extends AbstractSimpleReplaceRule2 {

  public static final String BRITISH_SIMPLE_REPLACE_RULE = "EN_GB_SIMPLE_REPLACE";

  private static final Locale EN_GB_LOCALE = new Locale("en-GB");
  
  private final String PATH;

  @Override
  public List<String> getFileNames() {
	  return Collections.singletonList(PATH);
  }

  public BritishReplaceRule(ResourceBundle messages, String path) {
    super(messages, new BritishEnglish());
    this.PATH = Objects.requireNonNull(path);
    
    super.setCategory(Categories.STYLE.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.LocaleViolation);
    addExamplePair(Example.wrong("We can produce <marker>drapes</marker> of any size or shape from a choice of over 500 different fabrics."),
                   Example.fixed("We can produce <marker>curtains</marker> of any size or shape from a choice of over 500 different fabrics."));
  }

  @Override
  public String getId() {
    return BRITISH_SIMPLE_REPLACE_RULE;
  }

  @Override
  public String getDescription() {
    return "American words easily confused in British English";
  }

  @Override
  public String getShort() {
    return "American word";
  }
  
  @Override
  public String getMessage() {
    return "'$match' is a common American expression. Consider using expressions more common to British English.";
  }

  @Override
  public Locale getLocale() {
    return EN_GB_LOCALE;
  }

}
