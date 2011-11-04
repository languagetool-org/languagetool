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
package de.danielnaber.languagetool.tagging.disambiguation.rules;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import de.danielnaber.languagetool.rules.patterns.XMLRuleHandler;

/**
 * XML rule handler that loads disambiguation rules from XML and throws
 * exceptions on errors and warnings.
 * 
 * @author Daniel Naber
 */
class DisambXMLRuleHandler extends XMLRuleHandler {

  final List<DisambiguationPatternRule> rules = new ArrayList<DisambiguationPatternRule>();

  boolean inDisambiguation;
  
  List<DisambiguationPatternRule> getDisambRules() {
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
