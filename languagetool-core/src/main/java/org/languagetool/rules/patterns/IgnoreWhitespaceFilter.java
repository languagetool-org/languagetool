/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.patterns;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSParserFilter;

/**
 * XML Filter that ignores whitespace-only nodes.
 */
class IgnoreWhitespaceFilter implements LSParserFilter {

  @Override
  public short acceptNode(Node nodeArg) {
    String textContent = nodeArg.getTextContent();
    if (textContent.trim().isEmpty()) {
      return LSParserFilter.FILTER_REJECT;
    } else {
      return LSParserFilter.FILTER_ACCEPT;
    }
  }

  @Override
  public short startElement(Element elementArg) {
    return LSParserFilter.FILTER_ACCEPT;
  }

  @Override
  public int getWhatToShow() {
    return Node.NOTATION_NODE;
  }

}
