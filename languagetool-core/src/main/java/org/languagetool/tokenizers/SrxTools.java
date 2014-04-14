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

import net.sourceforge.segment.TextIterator;
import net.sourceforge.segment.srx.SrxDocument;
import net.sourceforge.segment.srx.SrxParser;
import net.sourceforge.segment.srx.SrxTextIterator;
import net.sourceforge.segment.srx.io.Srx2SaxParser;
import org.languagetool.JLanguageTool;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tools for loading an SRX tokenizer file.
 * @since 2.6
 */
final class SrxTools {

  private SrxTools() {
  }

  /**
   * Note: will consume and close the stream
   */
  static SrxDocument createSrxDocument(InputStream inputStream) {
    BufferedReader srxReader = null;
    try {
      srxReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
      Map<String, Object> parserParameters = new HashMap<>();
      parserParameters.put(Srx2SaxParser.VALIDATE_PARAMETER, true);
      SrxParser srxParser = new Srx2SaxParser(parserParameters);
      return srxParser.parse(srxReader);
    } catch (IOException e) {
      throw new RuntimeException("Could not load rules from resource dir "
              + JLanguageTool.getDataBroker().getResourceDir(), e);
    } finally {
      closeQuietly(srxReader);
      closeQuietly(inputStream);
    }
  }

  private static void closeQuietly(Closeable srxReader) {
    if (srxReader != null) {
      try {
        srxReader.close();
      } catch (IOException e) {
        // can't do anything useful
      }
    }
  }

  static List<String> tokenize(String text, SrxDocument srxDocument, String code) {
    List<String> segments = new ArrayList<>();
    TextIterator textIterator = new SrxTextIterator(srxDocument, code, text);
    while (textIterator.hasNext()) {
      segments.add(textIterator.next());
    }
    return segments;
  }

}
