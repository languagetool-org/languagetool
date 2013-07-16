/* LanguageTool, a natural language style checker 
 * Copyright (C) 2010 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.wikipedia;

import java.util.Date;
import java.util.List;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.RuleMatch;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Read the Wikipedia XML dump, check texts with LanguageTool, and
 * let result be handled in sub classes.
 */
abstract class BaseWikipediaDumpHandler extends DefaultHandler {

  protected static final int CONTEXT_SIZE = 50; 
  protected static final String MARKER_START = "<err>";
  protected static final String MARKER_END = "</err>";
  protected static final String LANG_MARKER = "XX";
  protected static final String URL_PREFIX = "http://" + LANG_MARKER + ".wikipedia.org/wiki/";

  protected Date dumpDate;
  protected String langCode;
  protected int maxErrors = 0;
  protected int errorCount = 0;

  private final JLanguageTool languageTool;
  private int ruleMatchCount = 0;
  private int maxArticles = 0;
  private int articleCount = 0;

  private boolean inText = false;
  private StringBuilder text = new StringBuilder();
  private String title;

  private TextMapFilter textFilter = new SwebleWikipediaTextFilter();

  protected BaseWikipediaDumpHandler(JLanguageTool languageTool, Date dumpDate, String langCode, Language lang) {
    this.languageTool = languageTool;
    this.dumpDate = dumpDate;
    this.langCode = langCode;
    textFilter = TextFilterTools.getTextFilter(lang);
  }

  public void setMaximumArticles(int maxArticles) {
    this.maxArticles = maxArticles;
  }

  public void setMaximumErrors(int maxErrors) {
    this.maxErrors = maxErrors;
  }

  int getArticleCount() {
    return articleCount;
  }

  int getRuleMatchCount() {
    return ruleMatchCount;
  }

  //===========================================================
  // SAX DocumentHandler methods
  //===========================================================

  @Override
  @SuppressWarnings("unused")
  public void startElement(String namespaceURI, String lName, String qName,
      Attributes attrs) throws SAXException {
    if (qName.equals("title")) {
      inText = true;
    } else if (qName.equals("text")) {
      inText = true;
    }
  }

  @Override
  @SuppressWarnings("unused")
  public void endElement(String namespaceURI, String sName, String qName) {
    if (qName.equals("title")) {
      title = text.toString();
      text = new StringBuilder();
    } else if (qName.equals("text")) {
      final PlainTextMapping mapping = textFilter.filter(text.toString());
      final String textToCheck = mapping.getPlainText();
      if (!textToCheck.contains("#REDIRECT")) {
        articleCount++;
        if (maxArticles > 0 && articleCount > maxArticles) {
          throw new ArticleLimitReachedException(maxArticles);
        }
        try {
          final List<RuleMatch> ruleMatches = languageTool.check(textToCheck);
          ruleMatchCount += ruleMatches.size();
          System.out.println("Checking article " + articleCount + " (" +
                  textToCheck.length()/1024 + "KB, '" + title + "')" +
                  ", found " + ruleMatches.size() + " matches");
          handleResult(title, ruleMatches, textToCheck, languageTool.getLanguage());
        } catch (ErrorLimitReachedException e) {
          throw e;
        } catch (Exception e) {
          throw new RuntimeException("Error checking '" + title + "' (" + articleCount + ")", e);
        }
      }
      text = new StringBuilder();
    }
    inText = false;
  }

  @Override
  public void characters(char buf[], int offset, int len) {
    final String s = new String(buf, offset, len);
    if (inText) {
      text.append(s);
    }
  }
  
  protected abstract void handleResult(String title, List<RuleMatch> ruleMatches,
      String text, Language language) throws Exception;

  protected abstract void close();

}
