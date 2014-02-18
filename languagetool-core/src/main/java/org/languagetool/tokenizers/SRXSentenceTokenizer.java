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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.segment.TextIterator;
import net.sourceforge.segment.srx.SrxDocument;
import net.sourceforge.segment.srx.SrxParser;
import net.sourceforge.segment.srx.SrxTextIterator;
import net.sourceforge.segment.srx.io.Srx2SaxParser;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;

/**
 * Class to tokenize sentences using an SRX file.
 * 
 * @author Marcin Miłkowski
 * @author Jarek Lipski
 */
public class SRXSentenceTokenizer implements SentenceTokenizer {

  private static final String RULES = "/segment.srx";
  private static final SrxDocument DOCUMENT = createSrxDocument();

  private final String languageCode;

  private String parCode;

  private static SrxDocument createSrxDocument() {
    BufferedReader srxReader = null;
    try {
      srxReader = new BufferedReader(new InputStreamReader(
              JLanguageTool.getDataBroker().getFromResourceDirAsStream(RULES), "utf-8"));
      final Map<String, Object> parserParameters = new HashMap<>();
      parserParameters.put(Srx2SaxParser.VALIDATE_PARAMETER, true);
      final SrxParser srxParser = new Srx2SaxParser(parserParameters);
      return srxParser.parse(srxReader);
    } catch (IOException e) {
      throw new RuntimeException("Could not load rules " + RULES + " from resource dir "
              + JLanguageTool.getDataBroker().getResourceDir(), e);
    } finally {
      closeQuietly(srxReader);
    }
  }

  private static void closeQuietly(BufferedReader srxReader) {
    if (srxReader != null) {
      try {
        srxReader.close();
      } catch (IOException e) {
        // can't do anything useful
      }
    }
  }

  public SRXSentenceTokenizer(final Language language) {
    this.languageCode = language.getShortName();
    setSingleLineBreaksMarksParagraph(false);
  }

  @Override
  public final List<String> tokenize(final String text) {
    final List<String> segments = new ArrayList<>();
    final TextIterator textIterator = new SrxTextIterator(DOCUMENT, languageCode + parCode, text);
    while (textIterator.hasNext()) {
      segments.add(textIterator.next());
    }
    return segments;
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
