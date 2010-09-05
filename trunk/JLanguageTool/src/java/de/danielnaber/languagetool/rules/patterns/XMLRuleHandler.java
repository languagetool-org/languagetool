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

import de.danielnaber.languagetool.rules.IncorrectExample;

/**
 * XML rule handler that loads rules from XML and throws
 * exceptions on errors and warnings.
 * 
 * @author Daniel Naber
 */
class XMLRuleHandler extends DefaultHandler {

  List<PatternRule> rules = new ArrayList<PatternRule>();

  StringBuilder correctExample = new StringBuilder();
  StringBuilder incorrectExample = new StringBuilder();
  StringBuilder exampleCorrection = new StringBuilder();
  StringBuilder message = new StringBuilder();
  StringBuilder match = new StringBuilder();
  StringBuilder elements;
  StringBuilder exceptions;
  
  List<String> correctExamples = new ArrayList<String>();
  List<IncorrectExample> incorrectExamples = new ArrayList<IncorrectExample>();

  boolean inPattern;
  boolean inCorrectExample;
  boolean inIncorrectExample;
  boolean inMessage;
  boolean inMatch;
  boolean inRuleGroup;
  boolean inToken;
  boolean inException;
  boolean inPhrases;
  boolean inAndGroup;

  /** Defines "yes" value in XML files. */
  protected static final String YES = "yes";
  protected static final String POSTAG = "postag";
  protected static final String POSTAG_REGEXP = "postag_regexp";
  protected static final String REGEXP = "regexp";
  protected static final String NEGATE = "negate";
  protected static final String INFLECTED = "inflected";
  protected static final String NEGATE_POS = "negate_pos";
  protected static final String MARKER = "marker";
  protected static final String DEFAULT = "default";
  protected static final String TYPE = "type";
  protected static final String SPACEBEFORE = "spacebefore";
  protected static final String EXAMPLE = "example";
  protected static final String SCOPE = "scope";
  
  List<PatternRule> getRules() {
    return rules;
  }
  
  public void warning (final SAXParseException e) throws SAXException {
    throw e;
  }
  
  public void error (final SAXParseException e) throws SAXException {
    throw e;
  }

}
