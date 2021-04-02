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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.tools.StringTools;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Check a Wikipedia page (without spell check), fetching the page via the MediaWiki API.
 */
public class WikipediaQuickCheck {

  private static final Pattern WIKIPEDIA_URL_REGEX = Pattern.compile("https?://(..)\\.wikipedia\\.org/wiki/(.*)"); 
  private static final Pattern SECURE_WIKIPEDIA_URL_REGEX = Pattern.compile("https://secure\\.wikimedia\\.org/wikipedia/(..)/wiki/(.*)");

  private final File ngramDir;
  private final int maxSizeBytes;

  private List<String> disabledRuleIds = new ArrayList<>();

  public WikipediaQuickCheck() {
    this(null, Integer.MAX_VALUE);
  }

  /**
   * @since 3.1
   * @param ngramDir directory with sub directories like 'en', 'de' etc that contain '1grams' etc directories with ngram data (Lucene indexes)
   */
  public WikipediaQuickCheck(File ngramDir) {
    this(ngramDir, Integer.MAX_VALUE);
  }

  /**
   * @since 3.3
   * @param ngramDir directory with sub directories like 'en', 'de' etc that contain '1grams' etc directories with ngram data (Lucene indexes)
   * @param maxSizeBytes the maximum bytes of XML for the methods that take an URL, longer content will throw an exception
   */
  public WikipediaQuickCheck(File ngramDir, int maxSizeBytes) {
    this.ngramDir = ngramDir;
    this.maxSizeBytes = maxSizeBytes;
  }

  public String getMediaWikiContent(URL wikipediaUrl) throws IOException {
    Language lang = getLanguage(wikipediaUrl);
    String pageTitle = getPageTitle(wikipediaUrl);
    String apiUrl = "https://" + lang.getShortCode() + ".wikipedia.org/w/api.php?titles=" 
            + URLEncoder.encode(pageTitle, "utf-8") + "&action=query&prop=revisions&rvprop=content|timestamp&format=xml";
    return getContent(new URL(apiUrl));
  }

  public Language getLanguage(URL url) {
    Matcher matcher = getUrlMatcher(url.toString());
    return Languages.getLanguageForShortCode(matcher.group(1));
  }

  public String getPageTitle(URL url) {
    Matcher matcher = getUrlMatcher(url.toString());
    return matcher.group(2);
  }
  
  private Matcher getUrlMatcher(String url) {
    Matcher matcher1 = WIKIPEDIA_URL_REGEX.matcher(url);
    Matcher matcher2 = SECURE_WIKIPEDIA_URL_REGEX.matcher(url);
    if (matcher1.matches()) {
      return matcher1;
    } else if (matcher2.matches()) {
      return matcher2;
    }
    throw new RuntimeException("URL does not seem to be a valid Wikipedia URL: " + url);
  }

  public void setDisabledRuleIds(List<String> ruleIds) {
    disabledRuleIds = ruleIds;
  }
  
  public List<String> getDisabledRuleIds() {
    return disabledRuleIds;
  }

  public MarkupAwareWikipediaResult checkPage(URL url) throws IOException, PageNotFoundException {
    return checkPage(url, null);
  }

  /**
   * @since 2.6
   */
  public MarkupAwareWikipediaResult checkPage(URL url, ErrorMarker errorMarker) throws IOException, PageNotFoundException {
    validateWikipediaUrl(url);
    String xml = getMediaWikiContent(url);
    if (xml.length() > maxSizeBytes) {
      throw new RuntimeException("Sorry, the content at " + url + " is too big - this process has been limited to " + maxSizeBytes +
              " bytes, but the content is " + xml.length() + " bytes");
    }
    MediaWikiContent wikiContent = getRevisionContent(xml);
    String content = wikiContent.getContent();
    if (content.trim().isEmpty()) {
      throw new PageNotFoundException("No content found at '" + url + "'");
    }
    if (content.toLowerCase().contains("#redirect")) {
      throw new PageNotFoundException("No content but redirect found at '" + url + "'");
    }
    return checkWikipediaMarkup(url, wikiContent, getLanguage(url), errorMarker);
  }

  MarkupAwareWikipediaResult checkWikipediaMarkup(URL url, MediaWikiContent wikiContent, Language language, ErrorMarker errorMarker) throws IOException {
    SwebleWikipediaTextFilter filter = new SwebleWikipediaTextFilter();
    PlainTextMapping mapping = filter.filter(wikiContent.getContent());
    MultiThreadedJLanguageTool lt = getLanguageTool(language);
    List<AppliedRuleMatch> appliedMatches = new ArrayList<>();
    List<RuleMatch> matches;
    try {
      matches = lt.check(mapping.getPlainText());
    } finally {
      lt.shutdown();
    }
    int internalErrors = 0;
    for (RuleMatch match : matches) {
      SuggestionReplacer replacer = errorMarker != null ? 
              new SuggestionReplacer(mapping, wikiContent.getContent(), errorMarker) :
              new SuggestionReplacer(mapping, wikiContent.getContent());
      try {
        List<RuleMatchApplication> ruleMatchApplications = replacer.applySuggestionsToOriginalText(match);
        appliedMatches.add(new AppliedRuleMatch(match, ruleMatchApplications));
      } catch (Exception e) {
        System.err.println("Failed to apply suggestion for rule match '" + match + "' for URL " + url + ": " + e);
        internalErrors++;
      }
    }
    return new MarkupAwareWikipediaResult(wikiContent, appliedMatches, internalErrors);
  }

  public WikipediaQuickCheckResult checkPage(String plainText, Language lang) throws IOException {
    MultiThreadedJLanguageTool lt = getLanguageTool(lang);
    try {
      List<RuleMatch> ruleMatches = lt.check(plainText);
      return new WikipediaQuickCheckResult(plainText, ruleMatches, lang.getShortCode());
    } finally {
      lt.shutdown();
    }
  }

  public void validateWikipediaUrl(URL wikipediaUrl) {
    // will throw exception if URL is not valid:
    getUrlMatcher(wikipediaUrl.toString());
  }

  /**
   * @param completeWikiContent the Mediawiki syntax as it comes from the API, including surrounding XML
   */
  public String getPlainText(String completeWikiContent) {
    MediaWikiContent wikiContent = getRevisionContent(completeWikiContent);
    String cleanedWikiContent = removeWikipediaLinks(wikiContent.getContent());
    TextMapFilter filter = new SwebleWikipediaTextFilter();
    return filter.filter(cleanedWikiContent).getPlainText();
  }

  /**
   * @param completeWikiContent the Mediawiki syntax as it comes from the API, including surrounding XML
   */
  public PlainTextMapping getPlainTextMapping(String completeWikiContent) {
    MediaWikiContent wikiContent = getRevisionContent(completeWikiContent);
    SwebleWikipediaTextFilter filter = new SwebleWikipediaTextFilter();
    return filter.filter(wikiContent.getContent());
  }

  // catches most, not all links ("[[pt:Linux]]", but not "[[zh-min-nan:Linux]]"). Might remove some non-interlanguage links.
  String removeWikipediaLinks(String wikiContent) {
    // interlanguage links
    return wikiContent
        .replaceAll("\\[\\[[a-z]{2,6}:.*?\\]\\]", "")
        // category links
        .replaceAll(
            "\\[\\[:?(Category|Categoria|Categoría|Catégorie|Kategorie):.*?\\]\\]", "")
        // file links, keeps alt and caption
        .replaceAll(
            "(File|Fitxer|Fichero|Ficheiro|Fichier|Datei):.*?\\.(png|jpg|svg|jpeg|tiff|gif|PNG|JPG|SVG|JPEG|TIFF|GIF)\\|((thumb|miniatur)\\|)?((right|left)\\|)?", "");
  }

  private MediaWikiContent getRevisionContent(String completeWikiContent) {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser saxParser;
    RevisionContentHandler handler = new RevisionContentHandler();
    try {
      saxParser = factory.newSAXParser();
      saxParser.parse(new InputSource(new StringReader(completeWikiContent)), handler);
    } catch (Exception e) {
      throw new RuntimeException("Could not parse XML: " + completeWikiContent, e);
    }
    return new MediaWikiContent(handler.getRevisionContent(), handler.getTimestamp());
  }

  private MultiThreadedJLanguageTool getLanguageTool(Language lang) throws IOException {
    MultiThreadedJLanguageTool lt = new MultiThreadedJLanguageTool(lang);
    enableWikipediaRules(lt);
    for (String disabledRuleId : disabledRuleIds) {
      lt.disableRule(disabledRuleId);
    }
    if (ngramDir != null) {
      lt.activateLanguageModelRules(ngramDir);
    }
    disableSpellingRules(lt);
    return lt;
  }

  private void enableWikipediaRules(JLanguageTool lt) {
    List<Rule> allRules = lt.getAllRules();
    for (Rule rule : allRules) {
      if (rule.getCategory().getName().equals("Wikipedia")) {
        lt.enableRule(rule.getId());
      }
    }
  }

  private void disableSpellingRules(JLanguageTool languageTool) {
    List<Rule> allActiveRules = languageTool.getAllActiveRules();
    for (Rule rule : allActiveRules) {
      if (rule.isDictionaryBasedSpellingRule()) {
        languageTool.disableRule(rule.getId());
      }
    }
  }

  private String getContent(URL wikipediaUrl) throws IOException {
    try {
      HttpURLConnection conn = (HttpURLConnection) wikipediaUrl.openConnection();
      conn.setRequestMethod("GET");
      conn.setConnectTimeout(30_000);
      conn.setReadTimeout(30_000);
      conn.connect();
      try (InputStream contentStream = (InputStream) conn.getContent()) {
        return StringTools.streamToString(contentStream, "UTF-8");
      }
    } catch (SocketTimeoutException e) {
      throw new RuntimeException("Timeout accessing " + wikipediaUrl, e);
    }
  }

  /*public static void mainTest(String[] args) throws IOException {
      TextFilter filter = new SwebleWikipediaTextFilter();
      String plainText = filter.filter("hallo\n* eins\n* zwei");
      System.out.println(plainText);
  }*/
    
  public static void main(String[] args) throws IOException, PageNotFoundException {
    if (args.length != 1) {
      System.out.println("Usage: " + WikipediaQuickCheck.class.getName() + " <url>");
      System.exit(1);
    }
    WikipediaQuickCheck check = new WikipediaQuickCheck();
    // URL examples:
    //String urlString = "http://de.wikipedia.org/wiki/Angela_Merkel";
    //String urlString = "https://de.wikipedia.org/wiki/Benutzer_Diskussion:Dnaber";
    //String urlString = "https://secure.wikimedia.org/wikipedia/de/wiki/Gütersloh";
    String urlString = args[0];
    MarkupAwareWikipediaResult result = check.checkPage(new URL(urlString), new ErrorMarker("***", "***"));
    int errorCount = 0;
    for (AppliedRuleMatch match : result.getAppliedRuleMatches()) {
      RuleMatchApplication matchApplication = match.getRuleMatchApplications().get(0);
      RuleMatch ruleMatch = match.getRuleMatch();
      Rule rule = ruleMatch.getRule();
      System.out.println();
      String message = ruleMatch.getMessage().replace("<suggestion>", "'").replace("</suggestion>", "'");
      errorCount++;
      System.out.print(errorCount + ". " + message);
      if (rule instanceof AbstractPatternRule) {
        System.out.println(" (" + rule.getFullId() + ")");
      } else {
        System.out.println(" (" + rule.getId() + ")");
      }
      System.out.println("    ..." + matchApplication.getOriginalErrorContext(50).replace("\n", "\\n") + "...");
    }
  }
  
  class RevisionContentHandler extends DefaultHandler {

    private final StringBuilder revisionText = new StringBuilder();

    private String timestamp;
    private boolean inRevision = false;

    @Override
    public void startElement(String namespaceURI, String lName,
        String qName, Attributes attrs) throws SAXException {
      if ("rev".equals(qName)) {
        timestamp = attrs.getValue("timestamp");
        inRevision = true;
      }
    }

    @Override
    public void endElement(String namespaceURI, String sName,
        String qName) throws SAXException {
      if ("rev".equals(qName)) {
        inRevision = false;
      }
    }
    
    @Override
    public void characters(char[] buf, int offset, int len) {
      String s = new String(buf, offset, len);
      if (inRevision) {
        revisionText.append(s);
      }
    }

    public String getRevisionContent() {
      return revisionText.toString();
    }

    public String getTimestamp() {
      return timestamp;
    }
  }

}
