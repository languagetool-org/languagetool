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
package org.languagetool.language;

import org.jetbrains.annotations.NotNull;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.*;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.ja.JapaneseTagger;
import org.languagetool.tokenizers.*;
import org.languagetool.tokenizers.ja.JapaneseWordTokenizer;

import java.util.*;

public class Japanese extends Language {

  @Override
  public String getShortCode() {
    return "ja";
  }

  @Override
  public String getName() {
    return "Japanese";
  }

  @Override
  public String[] getCountries() {
    return new String[] { "JP" };
  }

  @Override
  public String getCommonWordsPath() {
    // TODO: provide common words file
    return null;
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] { new Contributor("Takahiro Shinkai")};
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) {
    return Arrays.asList(
            new DoublePunctuationRule(messages),
            new MultipleWhitespaceRule(messages, this)
    );
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return new JapaneseTagger();
  }

  @Override
  public Tokenizer createDefaultWordTokenizer() {
    return new JapaneseWordTokenizer();
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }

}
