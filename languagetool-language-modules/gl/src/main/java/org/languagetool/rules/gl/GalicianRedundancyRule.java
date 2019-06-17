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
 * A rule that matches redundant expression. 
 * Galician implementations. Loads the list of words from
 * <code>/gl/redundancies.txt</code>.
 *
 * @author Tiago F. Santos 
 * @since 4.0
 */
public class GalicianRedundancyRule extends AbstractSimpleReplaceRule2 {

  public static final String GL_REDUNDANCY_REPLACE = "GL_REDUNDANCY_REPLACE";

  private static final String FILE_NAME = "/gl/redundancies.txt";
  private static final Locale GL_LOCALE = new Locale("gl");  // locale used on case-conversion

  @Override
  public final String getFileName() {
    return FILE_NAME;
  }

  public GalicianRedundancyRule(ResourceBundle messages) throws IOException {
    super(messages, new Galician());
    super.setCategory(Categories.REDUNDANCY.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Style);
    addExamplePair(Example.wrong("<marker>duna de area</marker>"),
                   Example.fixed("<marker>duna</marker>"));
  }

  @Override
  public final String getId() {
    return GL_REDUNDANCY_REPLACE;
  }

  @Override
  public String getDescription() {
    return "1. Pleonasmos e redundancias";
  }

  @Override
  public String getShort() {
    return "Pleonasmo";
  }

  @Override
  public String getSuggestion() {
    return "'$match' é un pleonasmo. É preferible dicir $suggestions";
  }

  @Override
  public String getSuggestionsSeparator() {
    return " ou ";
  }

  @Override
  public URL getUrl() {
    return Tools.getUrl("https://gl.wikipedia.org/wiki/Pleonasmo");
  }

  @Override
  public Locale getLocale() {
    return GL_LOCALE;
  }

}
