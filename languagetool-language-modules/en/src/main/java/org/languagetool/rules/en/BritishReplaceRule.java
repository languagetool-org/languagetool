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

import org.languagetool.rules.AbstractSimpleReplaceRule;
import org.languagetool.rules.Example;
import org.languagetool.rules.ITSIssueType;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A rule that matches words or phrases which should not be used and suggests
 * correct ones instead.
 *
 * @author Marcin Miłkowski
 */
public class BritishReplaceRule extends AbstractSimpleReplaceRule {

  public static final String BRITISH_SIMPLE_REPLACE_RULE = "EN_GB_SIMPLE_REPLACE";

  private static final Map<String, List<String>> wrongWords = loadFromPath("/en/en-GB/replace.txt");
  private static final Locale EN_GB_LOCALE = new Locale("en-GB");

  @Override
  protected Map<String, List<String>> getWrongWords() {
    return wrongWords;
  }

  public BritishReplaceRule(ResourceBundle messages) throws IOException {
    super(messages);
    setLocQualityIssueType(ITSIssueType.LocaleViolation);
    addExamplePair(Example.wrong("We can produce <marker>drapes</marker> of any size or shape from a choice of over 500 different fabrics."),
                   Example.fixed("We can produce <marker>curtains</marker> of any size or shape from a choice of over 500 different fabrics."));
    //to be reviewed addition by ahsen on 18.03.19
    addExamplePair(Example.wrong("<marker>theater</marker>"),
            Example.fixed("<marker>theatre</marker>"));
    addExamplePair(Example.wrong("<marker>gray</marker>"),
            Example.fixed("<marker>grey</marker>"));
    addExamplePair(Example.wrong("<marker>apartment</marker>"),
            Example.fixed("<marker>flat</marker>"));
    addExamplePair(Example.wrong("<marker>subway</marker>"),
            Example.fixed("<marker>underground</marker>"));
    addExamplePair(Example.wrong("<marker>line</marker>"),
            Example.fixed("<marker>queue</marker>"));
  //end of the addition
    //by Grisworld
    addExamplePair(Example.wrong("<marker>sandbox</marker>"),
      Example.fixed("<marker>sandpit</marker>"));
    addExamplePair(Example.wrong("<marker>shopping cart</marker>"),
      Example.fixed("<marker>trolley</marker>"));
    addExamplePair(Example.wrong("<marker>closet</marker>"),
      Example.fixed("<marker>wardrobe</marker>"));
    addExamplePair(Example.wrong("<marker>diaper</marker>"),
      Example.fixed("<marker>nappy</marker>"));
    addExamplePair(Example.wrong("<marker>lollipop</marker>"),
      Example.fixed("<marker>lolly</marker>"));
     //end of by Grisworld
  }


  @Override
  public final String getId() {
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
  public String getMessage(String tokenStr, List<String> replacements) {
    return tokenStr + " is a common American expression, in British English it is more common to use: "
        + String.join(", ", replacements) + ".";
  }

  @Override
  public boolean isCaseSensitive() {
    return false;
  }

  @Override
  public Locale getLocale() {
    return EN_GB_LOCALE;
  }

}
