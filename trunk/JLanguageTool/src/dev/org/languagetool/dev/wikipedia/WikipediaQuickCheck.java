/* LanguageTool, a natural language style checker 
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TextFilter;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.StringTools;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Check a Wikipedia page, fetching the page via the MediaWiki API.
 */
public class WikipediaQuickCheck {

  // disable some rules because of too many false alarms:
  private static final List<String> disabledRuleIds = Arrays.asList("WHITESPACE_RULE", "DE_CASE",
          "UNPAIRED_BRACKETS", "UPPERCASE_SENTENCE_START", "COMMA_PARENTHESIS_WHITESPACE",
          "DE_AGREEMENT", "PFEILE", "BISSTRICH", "AUSLASSUNGSPUNKTE");

  public List<String> getDisabledRuleIds() {
    return disabledRuleIds;
  }

  public String getMediaWikiContent(URL wikipediaUrl) throws IOException {
    validateWikipediaUrl(wikipediaUrl);
    final String shortLangName;
    final String pageTitle;
    try {
      shortLangName = getLanguage(wikipediaUrl);
      pageTitle = wikipediaUrl.toString().substring("http://xx.wikipedia.org/wiki/".length());
    } catch (StringIndexOutOfBoundsException e) {
      throw new RuntimeException("URL does not seem to be a valid URL: " + wikipediaUrl.toString(), e);
    }
    final String apiUrl = "http://" + shortLangName + ".wikipedia.org/w/api.php?titles=" 
            + pageTitle + "&action=query&prop=revisions&rvprop=content&format=xml";
    return getContent(new URL(apiUrl));
  }

  private String getLanguage(URL wikipediaUrl) {
    final String[] parts = wikipediaUrl.getHost().split("\\.");
    if (parts.length != 3) {
      throw new RuntimeException("Does not look like a Wikipedia server: " + wikipediaUrl);
    }
    return parts[0];
  }

  public WikipediaQuickCheckResult checkPage(String plainText, Language lang) throws IOException {
    final JLanguageTool langTool = getLanguageTool(lang);
    final List<RuleMatch> ruleMatches = langTool.check(plainText);
    return new WikipediaQuickCheckResult(plainText, ruleMatches, lang.getShortName());
  }

  public void validateWikipediaUrl(URL wikipediaUrl) {
    if (!wikipediaUrl.toString().matches("http://..\\.wikipedia\\.org/wiki/.*")) {
      throw new RuntimeException("URL does not seem to be a Wikipedia URL: " + wikipediaUrl);
    }
  }

  String getPlainText(String completeWikiContent) {
    final String wikiContent = getRevisionContent(completeWikiContent);
    final TextFilter filter = new SwebleWikipediaTextFilter();
    final String plainText = filter.filter(wikiContent);
    return plainText;
  }

  private String getRevisionContent(String completeWikiContent) {
    final SAXParserFactory factory = SAXParserFactory.newInstance();
    final SAXParser saxParser;
    final RevisionContentHandler handler  = new RevisionContentHandler();
    try {
      saxParser = factory.newSAXParser();
      saxParser.parse(new InputSource(new StringReader(completeWikiContent)), handler);
    } catch (Exception e) {
      throw new RuntimeException("Could not parse XML: " + completeWikiContent, e);
    }
    return handler.getRevisionContent();
  }

  private JLanguageTool getLanguageTool(Language lang) throws IOException {
    final JLanguageTool langTool = new JLanguageTool(lang);
    langTool.activateDefaultPatternRules();
    for (String disabledRuleId : disabledRuleIds) {
      langTool.disableRule(disabledRuleId);
    }
    return langTool;
  }

  private String getContent(URL wikipediaUrl) throws IOException {
    final InputStream contentStream = (InputStream) wikipediaUrl.getContent();
    return StringTools.streamToString(contentStream);
  }

  public static void mainTest(String[] args) throws IOException {
      final TextFilter filter = new SwebleWikipediaTextFilter();
      final String plainText = filter.filter("hallo\n* eins\n* zwei");
      System.out.println(plainText);
  }
    
  public static void main(String[] args) throws IOException {
    final WikipediaQuickCheck check = new WikipediaQuickCheck();
    //final String url = "http://de.wikipedia.org/wiki/Hof";
    //final String url = "http://de.wikipedia.org/wiki/Gütersloh";
    //final String url = "http://de.wikipedia.org/wiki/Bielefeld";
    //final String url = "http://de.wikipedia.org/wiki/Berlin";
    //final String url = "http://de.wikipedia.org/wiki/Köln";
    //final String url = "http://de.wikipedia.org/wiki/Angela_Merkel";
    //final String url = "http://de.wikipedia.org/wiki/Wortschatz";
    final String urlString = "http://de.wikipedia.org/wiki/Benutzer_Diskussion:Dnaber";
    final URL url = new URL(urlString);
    final String language = check.getLanguage(url);
    final String mediaWikiContent = check.getMediaWikiContent(url);
    final String plainText = check.getPlainText(mediaWikiContent);
    final WikipediaQuickCheckResult checkResult = check.checkPage(plainText, Language.getLanguageForShortName(language));
    for (RuleMatch ruleMatch : checkResult.getRuleMatches()) {
      System.out.println(ruleMatch.getMessage());
      final String context = StringTools.getContext(ruleMatch.getFromPos(), ruleMatch.getToPos(), checkResult.getText());
      System.out.println(context);
    }
  }
  
  class RevisionContentHandler extends DefaultHandler {

    private final StringBuilder revisionText = new StringBuilder();
    private boolean inRevision = false;

    @Override
    public void startElement(final String namespaceURI, final String lName,
        final String qName, final Attributes attrs) throws SAXException {
      if ("rev".equals(qName)) {
        inRevision = true;
      }
    }

    @Override
    public void endElement(final String namespaceURI, final String sName,
        final String qName) throws SAXException {
      if ("rev".equals(qName)) {
        inRevision = false;
      }
    }
    
    @Override
    public void characters(final char[] buf, final int offset, final int len) {
      final String s = new String(buf, offset, len);
      if (inRevision) {
        revisionText.append(s);
      }
    }

    public String getRevisionContent() {
      return revisionText.toString();
    }
  }

}
