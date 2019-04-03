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
public class NewZealandReplaceRule extends AbstractSimpleReplaceRule {

  public static final String NEW_ZEALAND_SIMPLE_REPLACE_RULE = "EN_NZ_SIMPLE_REPLACE";

  private static final Map<String, List<String>> wrongWords = loadFromPath("/en/en-NZ/replace.txt");
  private static final Locale EN_NZ_LOCALE = new Locale("en-NZ");

  @Override
  protected Map<String, List<String>> getWrongWords() {
    return wrongWords;
  }

  public NewZealandReplaceRule(ResourceBundle messages) throws IOException {
    super(messages);
    setLocQualityIssueType(ITSIssueType.LocaleViolation);
    addExamplePair(Example.wrong("A <marker>sidewalk</marker> is a path along the side of a road."),
                   Example.fixed("A <marker>footpath</marker> is a path along the side of a road."));
  //by ahsen
    addExamplePair(Example.wrong("Book starts with a <marker>dialog</marker>."),
      Example.fixed("Book starts with a <marker>dialogue</marker>."));
    addExamplePair(Example.wrong("She was <marker>focused</marker>."),
      Example.fixed("She was <marker>focussed</marker>."));
    addExamplePair(Example.wrong("Buildings were <marker>gray</marker>."),
      Example.fixed("Buildings were <marker>grey</marker>."));
    addExamplePair(Example.wrong("The <marker>judgment</marker> was harsh."),
      Example.fixed("The <marker>judgement</marker> was harsh."));
    addExamplePair(Example.wrong("He used to love <marker>math</marker>."),
      Example.fixed("He used to love <marker>maths</marker>."));
    addExamplePair(Example.wrong("He has <marker>mustache</marker>."),
      Example.fixed("He has <marker>moustache</marker>."));
    //end of by ahsen
    //by Lumpus99
    addExamplePair(Example.wrong("<marker>Aluminum</marker> is a chemical element with symbol Al"),
      Example.fixed("<marker>Aluminium</marker> is a chemical element with symbol Al"));
    addExamplePair(Example.wrong("Got to the <marker>center</marker> of the room."),
      Example.fixed("Got to the <marker>centre</marker> of the room."));
    addExamplePair(Example.wrong("The wall is 1 <marker>meter</marker> high."),
      Example.fixed("The wall is 1 <marker>metre</marker> high."));
    addExamplePair(Example.wrong("I will sign the <marker>check</marker>"),
      Example.fixed("I will sign the <marker>cheque</marker>"));
    addExamplePair(Example.wrong("Red is my favorite <marker>color</marker>"),
      Example.fixed("Red is my favorite <marker>colour</marker>"));
    addExamplePair(Example.wrong("She walked to the deep end, then she <marker>dove</marker> in."),
      Example.fixed("She walked to the deep end, then she <marker>dived</marker> in."));
    //end of by Lumpus99
    //by Grisworld
    addExamplePair(Example.wrong("My uncle’s<marker>plow</marker> has a great effect on soil(s) and multiple blades that he sharpens once a month."),
      Example.fixed("My uncle’s<marker>plough</marker> has a great effect on soil(s) and multiple blades that he sharpens once a month."));
    addExamplePair(Example.wrong("A computer<marker> program</marker> is written by students and teachers to aim important jobs in real life."),
      Example.fixed("A computer<marker> programme</marker> is written by students and teachers to aim important jobs in real life."));
    addExamplePair(Example.wrong("You should <marker>staunch</marker> a wounded person's blood flow while he/she is bleeding."),
      Example.fixed("You should <marker>stanch</marker> a wounded person's blood flow while he/she is bleeding."));
    addExamplePair(Example.wrong("Nothing is found after earthquake in this bulding,expect it's upper <marker>story</marker>"),
      Example.fixed("Nothing is found after earthquake in this bulding,expect it's upper <marker>storey</marker>"));
    addExamplePair(Example.wrong("A curious <marker>traveler</marker> always notes every place where he/she goes around the world."),
      Example.fixed("A curious <marker>traveller</marker> always notes every place where he/she goes around the world."));
    addExamplePair(Example.wrong("He forgot the <marker>labeled</marker> paper on gift.Therefore, his friend may learn its prize!"),
      Example.fixed("He forgot the <marker>labelled</marker> paper on gift.Therefore, his friend may learn its prize!"));
    //end of by Grisworld
  }

  @Override
  public final String getId() {
    return NEW_ZEALAND_SIMPLE_REPLACE_RULE;
  }

  @Override
  public String getDescription() {
    return "English words easily confused in New Zealand English";
  }

  @Override
  public String getShort() {
    return "Not a New Zealand English word";
  }
  
  @Override
  public String getMessage(String tokenStr, List<String> replacements) {
    return tokenStr + " is a non-standard expression, in New Zealand English it is more common to use: "
        + String.join(", ", replacements) + ".";
  }

  @Override
  public boolean isCaseSensitive() {
    return false;
  }

  @Override
  public Locale getLocale() {
    return EN_NZ_LOCALE;
  }

}
