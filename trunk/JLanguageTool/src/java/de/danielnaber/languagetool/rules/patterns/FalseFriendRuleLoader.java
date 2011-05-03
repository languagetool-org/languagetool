/* LanguageTool, a natural language style checker 
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
package de.danielnaber.languagetool.rules.patterns;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.Category;
import de.danielnaber.languagetool.rules.IncorrectExample;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * Loads {@link PatternRule}s from a false friends XML file.
 * 
 * @author Daniel Naber
 */
public class FalseFriendRuleLoader extends DefaultHandler {

  public FalseFriendRuleLoader() {
  }

  public final List<PatternRule> getRules(final InputStream file,
      final Language textLanguage, final Language motherTongue)
      throws ParserConfigurationException, SAXException, IOException {
    final FalseFriendRuleHandler handler = new FalseFriendRuleHandler(
        textLanguage, motherTongue);
    final SAXParserFactory factory = SAXParserFactory.newInstance();
    final SAXParser saxParser = factory.newSAXParser();
    saxParser.getXMLReader()
        .setFeature(
            "http://apache.org/xml/features/nonvalidating/load-external-dtd",
            false);
    saxParser.parse(file, handler);
    final List<PatternRule> rules = handler.getRules();
    // Add suggestions to each rule:
    final ResourceBundle messages = ResourceBundle.getBundle(
        "de.danielnaber.languagetool.MessagesBundle", motherTongue.getLocale());
    for (final PatternRule rule : rules) {
      final List<String> suggestionMap = handler.getSuggestionMap().get(rule.getId());
      if (suggestionMap != null) {
        final MessageFormat msgFormat = new MessageFormat(messages
            .getString("false_friend_suggestion"));
        final Object[] msg = new Object[] { formatSuggestions(suggestionMap) };
        rule.setMessage(rule.getMessage() + " " + msgFormat.format(msg));
      }
    }
    return rules;
  }

  private String formatSuggestions(final List<String> l) {
    final StringBuilder sb = new StringBuilder();
    for (final Iterator<String> iter = l.iterator(); iter.hasNext();) {
      final String s = iter.next();
      sb.append("<suggestion>");
      sb.append(s);
      sb.append("</suggestion>");
      if (iter.hasNext()) {
        sb.append(", ");
      }
    }
    return sb.toString();
  }

  /** Testing only. */
  public final void main(final String[] args)
      throws ParserConfigurationException, SAXException, IOException {
    final FalseFriendRuleLoader prg = new FalseFriendRuleLoader();
    List<PatternRule> l = prg.getRules(JLanguageTool.getDataBroker()
    		.getFromRulesDirAsStream("/false-friends.xml"), Language.ENGLISH,
        Language.GERMAN);
    System.out.println("Hints for German native speakers:");
    for (final PatternRule rule : l) {
      System.out.println(rule);
    }
    System.out.println("=======================================");
    System.out.println("Hints for English native speakers:");
    l = prg.getRules(JLanguageTool.getDataBroker()
    		.getFromRulesDirAsStream("/false-friends.xml"),
        Language.GERMAN, Language.ENGLISH);
    for (final PatternRule rule : l) {
      System.out.println(rule);
    }
  }

}

class FalseFriendRuleHandler extends XMLRuleHandler {

  private final ResourceBundle messages;
  private final MessageFormat formatter;

  private final Language textLanguage;
  private final Language motherTongue;

  private boolean defaultOff;

  private Language language;
  private Language translationLanguage;
  private Language currentTranslationLanguage;
  private List<StringBuilder> translations = new ArrayList<StringBuilder>();
  private StringBuilder translation = new StringBuilder();
  private final List<String> suggestions = new ArrayList<String>();
  // rule ID -> list of translations:
  private final Map<String, List<String>> suggestionMap = new HashMap<String, List<String>>();

  private boolean inTranslation;

  public FalseFriendRuleHandler(final Language textLanguage,
      final Language motherTongue) {
    messages = ResourceBundle.getBundle(
        "de.danielnaber.languagetool.MessagesBundle", motherTongue.getLocale());
    formatter = new MessageFormat("");
    formatter.setLocale(motherTongue.getLocale());
    this.textLanguage = textLanguage;
    this.motherTongue = motherTongue;
  }

  public Map<String, List<String>> getSuggestionMap() {
    return suggestionMap;
  }

  // ===========================================================
  // SAX DocumentHandler methods
  // ===========================================================

  @Override
  public void startElement(final String namespaceURI, final String lName,
      final String qName, final Attributes attrs) throws SAXException {
    if (qName.equals("rule")) {
      translations = new ArrayList<StringBuilder>();
      id = attrs.getValue("id");
      if (!(inRuleGroup && defaultOff)) {
        defaultOff = "off".equals(attrs.getValue("default"));
      }
      if (inRuleGroup && id == null) {
        id = ruleGroupId;
      }
      correctExamples = new ArrayList<String>();
      incorrectExamples = new ArrayList<IncorrectExample>();
    } else if (qName.equals("pattern")) {
      inPattern = true;
      final String languageStr = attrs.getValue("lang");
      language = Language.getLanguageForShortName(languageStr);
      if (language == null) {
        throw new SAXException("Unknown language '" + languageStr + "'");
      }
    } else if (qName.equals("exception")) {
      inException = true;
      exceptions = new StringBuilder();

      if (attrs.getValue(NEGATE) != null) {
        exceptionStringNegation = attrs.getValue(NEGATE).equals(YES);
      }
      if (attrs.getValue(SCOPE) != null) {
        exceptionValidNext = attrs.getValue(SCOPE).equals("next");
        exceptionValidPrev = attrs.getValue(SCOPE).equals("previous");
      }
      if (attrs.getValue(INFLECTED) != null) {
        exceptionStringInflected = attrs.getValue(INFLECTED).equals(YES);
      }
      if (attrs.getValue(POSTAG) != null) {
        exceptionPosToken = attrs.getValue(POSTAG);
        if (attrs.getValue(POSTAG_REGEXP) != null) {
          exceptionPosRegExp = attrs.getValue(POSTAG_REGEXP).equals(YES);
        }
        if (attrs.getValue(NEGATE_POS) != null) {
          exceptionPosNegation = attrs.getValue(NEGATE_POS).equals(YES);
        }
      }
      if (attrs.getValue(REGEXP) != null) {
        exceptionStringRegExp = attrs.getValue(REGEXP).equals(YES);
      }

    } else if (qName.equals(TOKEN)) {
      setToken(attrs);
    } else if (qName.equals("translation")) {
      inTranslation = true;
      final String languageStr = attrs.getValue("lang");
      final Language tmpLang = Language.getLanguageForShortName(languageStr);
      currentTranslationLanguage = tmpLang;
      if (tmpLang == motherTongue) {
        translationLanguage = tmpLang;
        if (translationLanguage == null) {
          throw new SAXException("Unknown language '" + languageStr + "'");
        }
      }
    } else if (qName.equals(EXAMPLE)
        && attrs.getValue(TYPE).equals("correct")) {
      inCorrectExample = true;
      correctExample = new StringBuilder();
    } else if (qName.equals(EXAMPLE)
        && attrs.getValue(TYPE).equals("incorrect")) {
      inIncorrectExample = true;
      incorrectExample = new StringBuilder();
    } else if (qName.equals("message")) {
      inMessage = true;
      message = new StringBuilder();
    } else if (qName.equals("rulegroup")) {
      ruleGroupId = attrs.getValue("id");
      inRuleGroup = true;
      defaultOff = "off".equals(attrs.getValue(DEFAULT));
    }
  }

  @Override
  public void endElement(final String namespaceURI, final String sName,
      final String qName) {
    if (qName.equals("rule")) {
      if (language == textLanguage && translationLanguage != null
          && translationLanguage == motherTongue && language != motherTongue
          && !translations.isEmpty()) {
        formatter.applyPattern(messages.getString("false_friend_hint"));
        final Object[] messageArguments = {
            elements.toString().replace('|', '/'),
            messages.getString(textLanguage.getShortName()),
            formatTranslations(translations),
            messages.getString(motherTongue.getShortName()) };
        final String description = formatter.format(messageArguments);
        final PatternRule rule = new PatternRule(id, language, elementList,
            messages.getString("false_friend_desc") + " "
                + elements.toString().replace('|', '/'), description, messages
                .getString("false_friend"));
        rule.setCorrectExamples(correctExamples);
        rule.setIncorrectExamples(incorrectExamples);
        rule.setCategory(new Category(messages
            .getString("category_false_friend")));
        if (defaultOff) {
          rule.setDefaultOff();
        }
        rules.add(rule);
      }
      
      if (elementList != null) {
        elementList.clear();
      }

    } else if (qName.equals("exception")) {
      inException = false;
      if (!exceptionSet) {
        tokenElement = new Element(elements.toString(), caseSensitive,
            regExpression, tokenInflected);
        exceptionSet = true;
      }
      tokenElement.setNegation(tokenNegated);
      if (!StringTools.isEmpty(exceptions.toString())) {
        tokenElement.setStringException(exceptions.toString(),
            exceptionStringRegExp, exceptionStringInflected,
            exceptionStringNegation, exceptionValidNext, exceptionValidPrev);
      }
      if (exceptionPosToken != null) {
        tokenElement.setPosException(exceptionPosToken, exceptionPosRegExp,
            exceptionPosNegation, exceptionValidNext, exceptionValidPrev);
        exceptionPosToken = null;
      }
    } else if (qName.equals(TOKEN)) {
      finalizeTokens();
    } else if (qName.equals("pattern")) {  	  
      inPattern = false;
    } else if (qName.equals("translation")) {
      if (currentTranslationLanguage == motherTongue) {
        translations.add(translation);
      }
      if (currentTranslationLanguage == textLanguage && language == motherTongue) {
        suggestions.add(translation.toString());
      }
      translation = new StringBuilder();
      inTranslation = false;
      currentTranslationLanguage = null;
    } else if (qName.equals(EXAMPLE)) {
      if (inCorrectExample) {
        correctExamples.add(correctExample.toString());
      } else if (inIncorrectExample) {
        incorrectExamples
            .add(new IncorrectExample(incorrectExample.toString()));
      }
      inCorrectExample = false;
      inIncorrectExample = false;
      correctExample = new StringBuilder();
      incorrectExample = new StringBuilder();
    } else if (qName.equals("message")) {
      inMessage = false;
    } else if (qName.equals("rulegroup")) {
      if (!suggestions.isEmpty()) {
        final List<String> l = new ArrayList<String>(suggestions);
        suggestionMap.put(id, l);
        suggestions.clear();
      }
      inRuleGroup = false;
    }
  }

  private String formatTranslations(final List<StringBuilder> translations) {
    final StringBuilder sb = new StringBuilder();
    for (final Iterator<StringBuilder> iter = translations.iterator(); iter
        .hasNext();) {
      final StringBuilder trans = iter.next();
      sb.append('"');
      sb.append(trans.toString());
      sb.append('"');
      if (iter.hasNext()) {
        sb.append(", ");
      }
    }
    return sb.toString();
  }

  @Override
  public void characters(final char[] buf, final int offset, final int len) {
    final String s = new String(buf, offset, len);
    if (inException) {
      exceptions.append(s);
    } else if (inToken && inPattern) {
      elements.append(s);
    } else if (inCorrectExample) {
      correctExample.append(s);
    } else if (inIncorrectExample) {
      incorrectExample.append(s);
    } else if (inTranslation) {
      translation.append(s);
    }
  }

}
