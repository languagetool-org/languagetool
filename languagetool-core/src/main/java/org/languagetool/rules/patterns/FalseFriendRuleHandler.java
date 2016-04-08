/* LanguageTool, a natural language style checker
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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

import org.apache.commons.lang.StringUtils;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.Categories;
import org.languagetool.rules.IncorrectExample;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

class FalseFriendRuleHandler extends XMLRuleHandler {

  // Definitions of values in XML files:
  private static final String TRANSLATION = "translation";

  private final ResourceBundle messages;
  private final MessageFormat formatter;
  private final Language textLanguage;
  private final Language motherTongue;
  private final Map<String, List<String>> suggestionMap = new HashMap<>();  // rule ID -> list of translations
  private final List<String> suggestions = new ArrayList<>();
  private final List<StringBuilder> translations = new ArrayList<>();

  private boolean defaultOff;
  private Language language;
  private Language translationLanguage;
  private Language currentTranslationLanguage;
  private StringBuilder translation = new StringBuilder();
  private boolean inTranslation;

  FalseFriendRuleHandler(Language textLanguage, Language motherTongue) {
    messages = ResourceBundle.getBundle(
        JLanguageTool.MESSAGE_BUNDLE, motherTongue.getLocale());
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
  public void startElement(String namespaceURI, String lName,
      String qName, Attributes attrs) throws SAXException {
    if (qName.equals(RULE)) {
      translations.clear();
      id = attrs.getValue("id");
      if (!(inRuleGroup && defaultOff)) {
        defaultOff = "off".equals(attrs.getValue("default"));
      }
      if (inRuleGroup && id == null) {
        id = ruleGroupId;
      }
      correctExamples = new ArrayList<>();
      incorrectExamples = new ArrayList<>();
    } else if (qName.equals(PATTERN)) {
      inPattern = true;
      String languageStr = attrs.getValue("lang");
      if (Languages.isLanguageSupported(languageStr)) {
        language = Languages.getLanguageForShortName(languageStr);
      }
    } else if (qName.equals(TOKEN)) {
      setToken(attrs);
    } else if (qName.equals(TRANSLATION)) {
      inTranslation = true;
      String languageStr = attrs.getValue("lang");
      if (Languages.isLanguageSupported(languageStr)) {
        Language tmpLang = Languages.getLanguageForShortName(languageStr);
        currentTranslationLanguage = tmpLang;
        if (tmpLang.equalsConsiderVariantsIfSpecified(motherTongue)) {
          translationLanguage = tmpLang;
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
    } else if (qName.equals(MESSAGE)) {
      inMessage = true;
      message = new StringBuilder();
    } else if (qName.equals(RULEGROUP)) {
      ruleGroupId = attrs.getValue("id");
      inRuleGroup = true;
      defaultOff = "off".equals(attrs.getValue(DEFAULT));
    }
  }

  @Override
  public void endElement(String namespaceURI, String sName,
      String qName) throws SAXException {
    switch (qName) {
      case RULE:
        if (language.equalsConsiderVariantsIfSpecified(textLanguage) && translationLanguage != null
                && translationLanguage.equalsConsiderVariantsIfSpecified(motherTongue) && language != motherTongue
                && !translations.isEmpty()) {
          formatter.applyPattern(messages.getString("false_friend_hint"));
          String tokensAsString = StringUtils.join(patternTokens, " ").replace('|', '/');
          Object[] messageArguments = {tokensAsString,
                  messages.getString(textLanguage.getShortName()),
                  formatTranslations(translations),
                  messages.getString(motherTongue.getShortName())};
          String description = formatter.format(messageArguments);
          PatternRule rule = new FalseFriendPatternRule(id, language, patternTokens,
                  messages.getString("false_friend_desc") + " "
                          + tokensAsString, description, messages.getString("false_friend"));
          rule.setCorrectExamples(correctExamples);
          rule.setIncorrectExamples(incorrectExamples);
          rule.setCategory(Categories.FALSE_FRIENDS.getCategory(messages));
          if (defaultOff) {
            rule.setDefaultOff();
          }
          rules.add(rule);
        }
        if (patternTokens != null) {
          patternTokens.clear();
        }
        break;
      case TOKEN:
        finalizeTokens();
        break;
      case PATTERN:
        inPattern = false;
        break;
      case TRANSLATION:
        if (currentTranslationLanguage != null && currentTranslationLanguage.equalsConsiderVariantsIfSpecified(motherTongue)) {
          // currentTranslationLanguage can be null if the language is not supported
          translations.add(translation);
        }
        if (currentTranslationLanguage != null && currentTranslationLanguage.equalsConsiderVariantsIfSpecified(textLanguage)
                && language.equalsConsiderVariantsIfSpecified(motherTongue)) {
          suggestions.add(translation.toString());
        }
        translation = new StringBuilder();
        inTranslation = false;
        currentTranslationLanguage = null;
        break;
      case EXAMPLE:
        if (inCorrectExample) {
          correctExamples.add(correctExample.toString());
        } else if (inIncorrectExample) {
          incorrectExamples.add(new IncorrectExample(incorrectExample.toString()));
        }
        inCorrectExample = false;
        inIncorrectExample = false;
        correctExample = new StringBuilder();
        incorrectExample = new StringBuilder();
        break;
      case MESSAGE:
        inMessage = false;
        break;
      case RULEGROUP:
        if (!suggestions.isEmpty()) {
          List<String> l = new ArrayList<>(suggestions);
          suggestionMap.put(id, l);
          suggestions.clear();
        }
        inRuleGroup = false;
        break;
    }
  }

  private String formatTranslations(List<StringBuilder> translations) {
    return translations.stream().map(o -> "\"" + o + "\"").collect(Collectors.joining(", "));
  }

  @Override
  public void characters(char[] buf, int offset, int len) {
    String s = new String(buf, offset, len);
    if (inToken && inPattern) {
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
