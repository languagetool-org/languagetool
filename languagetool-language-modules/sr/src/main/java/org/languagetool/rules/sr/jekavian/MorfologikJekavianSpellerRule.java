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

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;


/** @since 4.0 */
public final class MorfologikJekavianSpellerRule extends MorfologikSpellerRule {

  public static final String RULE_ID = "MORFOLOGIK_RULE_SR_JEKAVIAN";
  
  private static final String BASE_DICTIONARY_PATH = "/sr/dictionary/jekavian/";

  public MorfologikJekavianSpellerRule(
          ResourceBundle messages,
          Language language, UserConfig userConfig, List<Language> altLanguages) throws IOException {
    super(messages, language, userConfig, altLanguages);
    /*addExamplePair(
            Example.wrong("Двије сам <marker>зивдјезе</marker> видјела."),
            Example.fixed("Двије сам <marker>звијезде</marker> видјела.")
    );*/
  }

  @Override
  public String getFileName() {
    return BASE_DICTIONARY_PATH + "serbian.dict";
  }

  @Override
  public String getId() {
    return RULE_ID;
  }

  @Override
  public String getSpellingFileName() {
    return BASE_DICTIONARY_PATH + "spelling.txt";
  }

  @Override
  // File with ignored words
  public String getIgnoreFileName() {
    return BASE_DICTIONARY_PATH + "ignored.txt";
  }

  @Override
  public String getProhibitFileName() {
    return BASE_DICTIONARY_PATH + "prohibit.txt";
  }
}
