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

import net.loomchild.segment.srx.SrxDocument;
import org.languagetool.Language;

import java.util.List;
import java.util.Objects;

/**
 * Class to tokenize sentences using rules from an SRX file.
 * @author Marcin Miłkowski
 * @author Jarek Lipski
 */
public class SRXSentenceTokenizer implements SentenceTokenizer {

  private final SrxDocument srxDocument;
  private final Language language;

  private String parCode;

  /**
   * Build a sentence tokenizer based on the rules in the {@code segment.srx} file
   * that comes with LanguageTool.
   */
  public SRXSentenceTokenizer(Language language) {
    this(language, "/segment.srx");
  }

  /**
   * @param srxInClassPath the path to an SRX file in the classpath 
   * @since 3.2
   */
  public SRXSentenceTokenizer(Language language, String srxInClassPath) {
    this.language = Objects.requireNonNull(language);
    this.srxDocument = SrxTools.createSrxDocument(srxInClassPath);
    setSingleLineBreaksMarksParagraph(false);
  }

  @Override
  public final List<String> tokenize(String text) {
    return SrxTools.tokenize(text, srxDocument, language.getShortCode() + parCode);
  }

  @Override
  public final boolean singleLineBreaksMarksPara() {
    return "_one".equals(parCode);
  }

  /**
   * @param lineBreakParagraphs if <code>true</code>, single lines breaks are assumed to end a
   *   paragraph; if <code>false</code>, only two ore more consecutive line breaks end a paragraph
   */
  @Override
  public final void setSingleLineBreaksMarksParagraph(boolean lineBreakParagraphs) {
    if (lineBreakParagraphs) {
      parCode = "_one";
    } else {
      parCode = "_two";
    }
  }

}
