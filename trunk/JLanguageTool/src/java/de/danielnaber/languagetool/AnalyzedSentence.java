/* JLanguageTool, a natural language style checker 
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
package de.danielnaber.languagetool;

import java.util.Iterator;
import java.util.List;

/**
 * A sentence that has been tokenized and analyzed.
 * 
 * @author Daniel Naber
 */
public class AnalyzedSentence {

  private List tokens;
  
  public AnalyzedSentence(List tokens) {
    this.tokens = tokens;
  }

  /**
   * Returns the tokens of the analyzed text. Whitespace has its own tokens.
   */
  public List getTokens() {
    return tokens;
  }
  
  /**
   * Returns analyzed text as XML like this:
   * <pre>
   * &lt;s>&lt;t pos="0">This&lt;/t> &lt;t pos="5">is&lt;/t> &lt;t pos="8">it&lt;/t>&lt;t pos="10">.&lt;/t>&lt;/s>
   * </pre>
   */
  public String getXML() {
    StringBuffer sb = new StringBuffer();
    sb.append("<s>");
    int pos = 0;
    for (Iterator iter = tokens.iterator(); iter.hasNext();) {
      String token = (String) iter.next();
      if (token.trim().equals("")) {
        sb.append(token);
      } else {
        sb.append("<t startpos=\"");
        sb.append(pos);
        sb.append("\" endpos=\"");
        sb.append(pos+token.length());
        sb.append("\">");
        sb.append(token);
        sb.append("</t>");
      }
      pos += token.length();
    }
    sb.append("</s>");
    return sb.toString();
  }
  
}
