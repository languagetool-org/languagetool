/* LanguageTool, a natural language style checker
 * Copyright (C) 2026 Jaume Ortolà
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
package org.languagetool.rules.ca;

import org.languagetool.AnalyzedSentence;
import org.languagetool.Language;
import org.languagetool.rules.PunctuationMarkAtParagraphEnd;

import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Catalan version of {@link PunctuationMarkAtParagraphEnd} that ignores
 * cross-reference lines that do not end with a punctuation mark, e.g.
 * "Annex: Videojocs cancel·lats" or "Article principal: Videojocs cancel·lats".
 *
 * @since 6.9
 */
public class CatalanPunctuationMarkAtParagraphEnd extends PunctuationMarkAtParagraphEnd {

  private static final Pattern IGNORED_PREFIXES = Pattern.compile(
      "^(Annex|Article principal)\\s*:",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

  public CatalanPunctuationMarkAtParagraphEnd(ResourceBundle messages, Language lang) {
    super(messages, lang, true);
  }

  @Override
  protected boolean isIgnoredParagraph(AnalyzedSentence firstSentenceInParagraph) {
    return IGNORED_PREFIXES.matcher(firstSentenceInParagraph.getText().trim()).find();
  }

}
