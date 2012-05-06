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
package org.languagetool.rules.br;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import org.languagetool.rules.AbstractSimpleReplaceRule;

/**
 * A rule that matches place names in French which should be
 * translated in Breton.
 *
 * Loads the list of words from <code>rules/br/topo.txt</code>.
 *
 * @author Dominique Pellé
 */
public class TopoReplaceRule extends AbstractSimpleReplaceRule {

  public static final String BRETON_TOPO = "BR_TOPO";

  private static final String FILE_NAME = "/br/topo.txt";
  // locale used on case-conversion
  private static final Locale BR_LOCALE = new Locale("br");

  @Override
  public final String getFileName() {
    return FILE_NAME;
  }

  public TopoReplaceRule(final ResourceBundle messages) throws IOException {
    super(messages);
  }

  @Override
  public final String getId() {
    return BRETON_TOPO;
  }

  @Override
  public String getDescription() {
    return "anvioù-lec’h e brezhoneg";
  }

  @Override
  public String getShort() {
    return "anvioù lec’h";
  }

  @Override
  public String getSuggestion() {
    return " a vez ul lec’h-anv gallek. E brezhoneg e vez graet ";
  }

  /**
   * locale used on case-conversion
   */
  @Override
  public Locale getLocale() {
    return BR_LOCALE;
  }
}
