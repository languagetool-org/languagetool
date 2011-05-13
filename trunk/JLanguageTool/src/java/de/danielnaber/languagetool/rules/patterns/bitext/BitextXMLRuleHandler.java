/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.rules.patterns.bitext;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import de.danielnaber.languagetool.bitext.StringPair;
import de.danielnaber.languagetool.rules.bitext.IncorrectBitextExample;
import de.danielnaber.languagetool.rules.patterns.XMLRuleHandler;

/**
 * XML rule handler that loads rules from XML and throws
 * exceptions on errors and warnings.
 * 
 * @author Daniel Naber
 */
class BitextXMLRuleHandler extends XMLRuleHandler {

  List<BitextPatternRule> rules = new ArrayList<BitextPatternRule>();

  List<StringPair> correctExamples = new ArrayList<StringPair>();
  List<IncorrectBitextExample> incorrectExamples = new ArrayList<IncorrectBitextExample>();

  List<BitextPatternRule> getBitextRules() {
    return rules;
  }
  
  @Override
  public void warning (final SAXParseException e) throws SAXException {
    throw e;
  }
  
  @Override
  public void error (final SAXParseException e) throws SAXException {
    throw e;
  }

}
