/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Marcin Milkowski (http://www.languagetool.org)
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
package org.languagetool.rules.spelling;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

/**
 * An abstract rule for spellchecking rules.
 *
 * @author Marcin Miłkowski
 */
public abstract class SpellingCheckRule extends Rule {

  protected final Language language;

  private static final String SPELLING_IGNORE_FILE = "/hunspell/ignore.txt";
  private final Set<String> wordsToBeIgnored = new HashSet<String>();

  public SpellingCheckRule(final ResourceBundle messages, final Language language) {
    super(messages);
    this.language = language;
  }

  @Override
  public abstract String getId();

  @Override
  public abstract String getDescription();

  @Override
  public abstract RuleMatch[] match(AnalyzedSentence text) throws IOException;

  @Override
  public boolean isSpellingRule() {
    return true;
  }

  @Override
  public void reset() {
  }

  protected boolean ignoreWord(String word) throws IOException {
    // TODO?: this is needed at least for German as Hunspell tokenization includes the dot:
    final String cleanWord = word.endsWith(".") ? word.substring(0, word.length() - 1) : word;
    return wordsToBeIgnored.contains(cleanWord);
  }

  protected void init() throws IOException {
    loadFileIfExists(language.getShortName() + SPELLING_IGNORE_FILE);
    loadFileIfExists(language.getShortNameWithVariant() + SPELLING_IGNORE_FILE);
  }

  private void loadFileIfExists(String filename) throws IOException {
    final boolean ignoreFileExists = JLanguageTool.getDataBroker().resourceExists(filename);
    if (!ignoreFileExists) {
      return;
    }
    loadWordsToBeIgnored(filename);
  }

  private void loadWordsToBeIgnored(String ignoreFile) throws IOException {
    final InputStream inputStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(ignoreFile);
    try {
      final Scanner scanner = new Scanner(inputStream);
      try {
        while (scanner.hasNextLine()) {
          final String line = scanner.nextLine();
          final boolean isComment = line.startsWith("#");
          if (isComment) {
            continue;
          }
          wordsToBeIgnored.add(line);
        }
      } finally {
        scanner.close();
      }
    } finally {
      inputStream.close();
    }
  }

}
