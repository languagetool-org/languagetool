/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (www.danielnaber.de)
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

import org.jetbrains.annotations.NotNull;
import org.languagetool.Language;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.Example;

import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 * False friends for German native speakers who write English text, based on ngrams.
 * @since 4.6
 */
public class EnglishForGermansFalseFriendRule extends EnglishForL2SpeakersFalseFriendRule {

  public EnglishForGermansFalseFriendRule(ResourceBundle messages, LanguageModel languageModel, Language motherTongue, Language language)  {
    super(messages, languageModel, motherTongue, language);
    addExamplePair(Example.wrong("My <marker>handy</marker> is broken."),
                   Example.fixed("My <marker>phone</marker> is broken."));
  }

  @Override
  public String getId() {
    return "EN_FOR_DE_SPEAKERS_FALSE_FRIENDS";
  }

  @NotNull
  @Override
  protected List<String> getFilenames() {
    return Collections.singletonList("confusion_sets_l2_de.txt");
  }

}
