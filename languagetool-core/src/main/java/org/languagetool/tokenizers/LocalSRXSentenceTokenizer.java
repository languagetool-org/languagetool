/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

/**
 * Class to tokenize sentences using an SRX file.
 * See <a href="http://wiki.languagetool.org/customizing-sentence-segmentation-in-srx-rules">our wiki</a>
 * for a description of how we use SRX.
 * @see SRXSentenceTokenizer
 * @since 2.6
 */
public class LocalSRXSentenceTokenizer implements SentenceTokenizer {

  private final SrxDocument srxDocument;
  private final Language language;

  private String parCode;

  /**
   * @param srxInClassPath the path to an SRX file in the classpath 
   */
  public LocalSRXSentenceTokenizer(Language language, String srxInClassPath) {
    this.language = Objects.requireNonNull(language);
    InputStream stream = this.getClass().getResourceAsStream(srxInClassPath);
    if (stream == null) {
      throw new RuntimeException("Could not find SRX file in classpath: " + srxInClassPath);
    }
    this.srxDocument = SrxTools.createSrxDocument(stream);  // will close the stream on its own
    setSingleLineBreaksMarksParagraph(false);
  }

  @Override
  public final List<String> tokenize(final String text) {
    return SrxTools.tokenize(text, srxDocument, language.getShortName() + parCode);
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
