/* LanguageTool, a natural language style checker 
 * Copyright (C) 2009 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tokenizers;

import net.sourceforge.segment.srx.SrxDocument;
import org.languagetool.Language;

import java.util.List;

/**
 * Class to tokenize sentences using LanguageTool's global SRX file for all
 * languages. If you add a language that's not part of the official LanguageTool
 * distribution, see {@link LocalSRXSentenceTokenizer} instead.
 * 
 * @author Marcin Miłkowski
 * @author Jarek Lipski
 */
public class SRXSentenceTokenizer implements SentenceTokenizer {

  private static final SrxDocument DOCUMENT = SrxTools.createSrxDocument("/segment.srx");

  private final String languageCode;

  private String parCode;

  public SRXSentenceTokenizer(final Language language) {
    this.languageCode = language.getShortName();
    setSingleLineBreaksMarksParagraph(false);
  }

  @Override
  public final List<String> tokenize(final String text) {
    return SrxTools.tokenize(text, DOCUMENT, languageCode + parCode);
  }

  @Override
  public final boolean singleLineBreaksMarksPara() {
    return "_one".equals(parCode);
  }

  /**
   * @param lineBreakParagraphs
   *          if <code>true</code>, single lines breaks are assumed to end a
   *          paragraph; if <code>false</code>, only two ore more consecutive
   *          line breaks end a paragraph
   */
  @Override
  public final void setSingleLineBreaksMarksParagraph(
      final boolean lineBreakParagraphs) {
    if (lineBreakParagraphs) {
      parCode = "_one";
    } else {
      parCode = "_two";
    }
  }

}
