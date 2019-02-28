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
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import java.net.URL;

/**
 * A rule that matches words which should not be used and suggests correct ones instead. 
 * Romanian implementations. Loads the list of words from
 * <code>/ro/replace.txt</code>.
 *
 * @author Tiago F. Santos (localized from romanian)
 * @since 4.0
 */
public class GalicianBarbarismsRule extends AbstractSimpleReplaceRule2 {

  public static final String GALICIAN_BARBARISM_RULE = "GL_BARBARISM_REPLACE";

  private static final String FILE_NAME = "/gl/barbarisms.txt";
  private static final Locale GL_LOCALE = new Locale("gl");  // locale used on case-conversion

  @Override
  public final String getFileName() {
    return FILE_NAME;
  }

  public GalicianBarbarismsRule(ResourceBundle messages) throws IOException {
    super(messages, new Galician());
    super.setCategory(Categories.STYLE.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.LocaleViolation);
    addExamplePair(Example.wrong("<marker>curriculum vitae</marker>"),
                   Example.fixed("<marker>currículo</marker>"));
  }

  @Override
  public final String getId() {
    return GALICIAN_BARBARISM_RULE;
  }

  @Override
  public String getDescription() {
    return "Palabras de orixe estranxeira evitábeis";
  }

  @Override
  public String getShort() {
    return "Xenismo";
  }

  @Override
  public String getSuggestion() {
    return "'$match' é un xenismo. É preferíbel dicir $suggestions";
  }

  @Override
  public String getSuggestionsSeparator() {
    return " ou ";
  }

  @Override
  public URL getUrl() {
    return Tools.getUrl("https://gl.wikipedia.org/wiki/Xenismo");
  }

  @Override
  public Locale getLocale() {
    return GL_LOCALE;
  }

}
