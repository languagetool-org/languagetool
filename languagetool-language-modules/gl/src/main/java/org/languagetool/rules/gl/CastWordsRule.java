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

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.languagetool.rules.AbstractSimpleReplaceRule;

/**
 * A rule that matches words or phrases which should not be used and suggests
 * correct ones instead.
 *
 * Galician implementation. Loads the list of words from
 * <code>rules/gl/spanish.txt</code>.
 *
 * @author Susana Sotelo
 *
 * Based on pl/SimpleReplaceRule.java
 */
public class CastWordsRule extends AbstractSimpleReplaceRule {

  public static final String GL_CAST_WORDS_RULE = "GL_CAST_WORDS";

  private static final Map<String, List<String>> wrongWords = loadFromPath("/gl/spanish.txt");
  private static final Locale GL_LOCALE = new Locale("gl");

  @Override
  protected Map<String, List<String>> getWrongWords() {
    return wrongWords;
  }

  public CastWordsRule(ResourceBundle messages) throws IOException {
    super(messages);
  }

  @Override
  public final String getId() {
    return GL_CAST_WORDS_RULE;
  }

  @Override
  public String getDescription() {
    return "Corrección de erros léxicos (castelanismos).";
  }

  @Override
  public String getShort() {
    return "Castelanismos léxicos";
  }
  
  @Override
  public String getMessage(String tokenStr, List<String> replacements) {
    return tokenStr + " é un castelanismo. Empregue no seu sitio: "
        + String.join(", ", replacements) + ".";
  }

  @Override
  public boolean isCaseSensitive() {
    return false;
  }

  @Override
  public Locale getLocale() {
    return GL_LOCALE;
  }

}
