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
package de.danielnaber.languagetool.rules.patterns;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

class XMLRuleHandler extends DefaultHandler {

  List<PatternRule> rules = new ArrayList<PatternRule>();

  StringBuffer pattern = null;
  StringBuffer correctExample = new StringBuffer();
  StringBuffer incorrectExample = new StringBuffer();
  StringBuffer message = new StringBuffer();
  StringBuffer elements = null;
  StringBuffer exceptions = null;

  List<String> correctExamples = new ArrayList<String>();
  List<String> incorrectExamples = new ArrayList<String>();

  boolean inPattern = false;
  boolean inCorrectExample = false;
  boolean inIncorrectExample = false;
  boolean inMessage = false;
  boolean inRuleGroup = false;
  boolean inToken= false;
  boolean inException = false;

  List<PatternRule> getRules() {
    return rules;
  }
  
  public void warning (SAXParseException e) throws SAXException {
    throw e;
  }
  
  public void error (SAXParseException e) throws SAXException {
    throw e;
  }

}
