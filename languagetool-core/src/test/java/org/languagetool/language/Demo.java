/* LanguageTool, a natural language style checker 
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.language;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.chunking.Chunker;
import org.languagetool.chunking.xx.DemoChunker;
import org.languagetool.rules.Rule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.xx.DemoDisambiguator2;
import org.languagetool.tagging.xx.DemoTagger;

import java.util.*;

/**
 * A demo language that is part of languagetool-core and thus always available.
 */
public class Demo extends Language {

  public static final String SHORT_CODE = "xx";

  @Override
  public Locale getLocale() {
    return new Locale("en");
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new DemoDisambiguator2();
  }

  @Override
  public String getName() {
    return "Testlanguage";
  }

  @Override
  public String getShortCode() {
    return SHORT_CODE;
  }

  @Override
  public String[] getCountries() {
    return new String[] {"XX"};
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return new DemoTagger();
  }

  @Nullable
  @Override
  public Chunker createDefaultChunker() {
    return new DemoChunker();
  }

  @Override
  public Contributor[] getMaintainers() {
    return null;
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) {
    return Collections.emptyList();
  }

}
