/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.sr.jekavian;

import org.languagetool.language.JekavianSerbian;
import org.languagetool.rules.Example;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;

import java.io.IOException;
import java.util.ResourceBundle;


/** @since 4.0 */
public class MorfologikJekavianSpellerRule extends MorfologikSpellerRule {

  public static final String RULE_ID = "SR_JEKAVIAN_MORFOLOGIK_SPELLER_RULE";

  private static final String RESOURCE_FILENAME = "/sr/dictionary/jekavian/serbian_hunspell.dict";

  public MorfologikJekavianSpellerRule(
          ResourceBundle messages,
          JekavianSerbian language) throws IOException {

    super(messages, language);
    addExamplePair(
            Example.wrong("Двије сам <marker>зивједзе</marker> видјела."),
            Example.fixed("Двије сам <marker>звијезде</marker> видјела.")
    );
  }

  @Override
  public String getFileName() {
    return RESOURCE_FILENAME;
  }

  @Override
  public String getId() {
    return RULE_ID;
  }
}
