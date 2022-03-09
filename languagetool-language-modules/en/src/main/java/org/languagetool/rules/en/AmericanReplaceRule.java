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
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.AbstractSimpleReplaceRule2;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.ITSIssueType;

import java.util.*;

/**
 * A rule that matches words or phrases which should not be used and suggests
 * correct ones instead.
 * @author Marcin Miłkowski
 */
public class AmericanReplaceRule extends AbstractSimpleReplaceRule2 {

  public static final String AMERICAN_SIMPLE_REPLACE_RULE = "EN_US_SIMPLE_REPLACE";

  private static final Locale EN_US_LOCALE = new Locale("en-US");

  private final String PATH;

  @Override
  public List<String> getFileNames() {
	  return Collections.singletonList(PATH);
  }

  public AmericanReplaceRule(ResourceBundle messages, String path) {
    super(messages, new AmericanEnglish());
    this.PATH = Objects.requireNonNull(path);
    
    super.setCategory(Categories.STYLE.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.LocaleViolation);
    addExamplePair(Example.wrong("Are baked <marker>crisps</marker> healthy?"),
                   Example.fixed("Are baked <marker>chips</marker> healthy?"));
  }

  @Override
  public final String getId() {
    return AMERICAN_SIMPLE_REPLACE_RULE;
  }

  @Override
  public String getDescription() {
    return "British words easily confused in American English";
  }

  @Override
  public String getShort() {
    return "British word";
  }
  
  @Override
  public String getMessage() {
	  return "'$match' is a common British expression. Consider using expressions more common to American English.";
  }


  @Override
  public Locale getLocale() {
    return EN_US_LOCALE;
  }

}
