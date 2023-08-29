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
package org.languagetool.rules.gl;

import org.languagetool.language.Galician;
import org.languagetool.rules.AbstractSimpleReplaceRule2;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.ITSIssueType;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A rule that matches wordy expressions. 
 * Galician implementation. Loads the list of words from
 * <code>/gl/wordiness.txt</code>.
 *
 * @author Tiago F. Santos 
 * @since 4.0
 */
public class GalicianWordinessRule extends AbstractSimpleReplaceRule2 {

  public static final String GL_WORDINESS_REPLACE = "GL_WORDINESS_REPLACE";

  private static final String FILE_NAME = "/gl/wordiness.txt";
  private static final Locale GL_LOCALE = new Locale("gl");  // locale used on case-conversion

  @Override
  public final List<String> getFileNames() {
    return Collections.singletonList(FILE_NAME);
  }

  public GalicianWordinessRule(ResourceBundle messages) throws IOException {
    super(messages, new Galician());
    setCategory(Categories.REDUNDANCY.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Style);
    addExamplePair(Example.wrong("<marker>Raramente é o caso en que acontece</marker> isto."),
                   Example.fixed("<marker>Raramente acontece</marker> isto."));
  }

  @Override
  public final String getId() {
    return GL_WORDINESS_REPLACE;
  }

  @Override
  public String getDescription() {
    return "2. Expresións prolixas";
  }

  @Override
  public String getShort() {
    return "Expresión prolixa";
  }

  @Override
  public String getMessage() {
    return "'$match' é unha expresión innecesariamente complexa. É preferíbel dicir $suggestions";
  }

  @Override
  public String getSuggestionsSeparator() {
    return " ou ";
  }

  /* @Override
   * public URL getUrl() {
   *  try {
   *    return new URL("https://gl.wikipedia.org/wiki/V%C3%ADcio_de_linguagem#Prolixidade_ou_preciosismo");
   *  } catch (MalformedURLException e) {
   *    throw new RuntimeException(e);
   *  }
    }*/

  @Override
  public Locale getLocale() {
    return GL_LOCALE;
  }

}
