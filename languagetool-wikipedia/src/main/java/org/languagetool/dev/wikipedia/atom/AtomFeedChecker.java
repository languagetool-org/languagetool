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
package org.languagetool.dev.wikipedia.atom;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.dev.wikipedia.LocationHelper;
import org.languagetool.dev.wikipedia.PlainTextMapping;
import org.languagetool.dev.wikipedia.SwebleWikipediaTextFilter;
import org.languagetool.dev.wikipedia.TextMapFilter;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.tools.ContextTools;
import xtc.tree.Location;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;

/**
 * Check the changes from a Wikipedia Atom feed with LanguageTool, only getting
 * the errors that have been introduced by that change.
 * @since 2.4
 */
class AtomFeedChecker {

  private static final int CONTEXT_SIZE = 60;
  private static final String USER_AGENT = "http://community.languagetool.org bot, contact: naber[@]danielnaber.de";
  
  private final JLanguageTool langTool;
  private final Language language;
  private final MatchDatabase matchDatabase;
  private final TextMapFilter textFilter = new SwebleWikipediaTextFilter();
  private final ContextTools contextTools = new ContextTools();

  AtomFeedChecker(Language language) throws IOException {
    this(language, null);
  }
  
  AtomFeedChecker(Language language, DatabaseConfig dbConfig) throws IOException {
    this.language = Objects.requireNonNull(language);
    langTool = new JLanguageTool(language);
    langTool.activateDefaultPatternRules();
    langTool.disableRule("UNPAIRED_BRACKETS");  // too many false alarms
    activateCategory("Wikipedia", langTool);
    if (dbConfig != null) {
      matchDatabase = new MatchDatabase(dbConfig.getUrl(), dbConfig.getUser(), dbConfig.getPassword());
    } else {
      matchDatabase = null;
    }
    contextTools.setContextSize(CONTEXT_SIZE);
    contextTools.setErrorMarkerStart("<err>");
    contextTools.setErrorMarkerEnd("</err>");
    contextTools.setEscapeHtml(false);
  }

  private void activateCategory(String categoryName, JLanguageTool langTool) {
    for (Rule rule : langTool.getAllRules()) {
      if (rule.getCategory().getName().equals(categoryName)) {
        System.out.println("Activating " + rule.getId() + " in category " + categoryName);
        langTool.enableDefaultOffRule(rule.getId());
      }
    }
  }

  CheckResult runCheck(InputStream feedStream) throws IOException {
    CheckResult checkResult = checkChanges(feedStream);
    storeResults(checkResult);
    return checkResult;
  }
  
  CheckResult runCheck(String url) throws IOException {
    CheckResult checkResult = checkChanges(new URL(url));
    storeResults(checkResult);
    return checkResult;
  }
  
  private void storeResults(CheckResult checkResult) throws IOException {
    List<ChangeAnalysis> checkResults = checkResult.getCheckResults();
    System.out.println("Check results:");
    for (ChangeAnalysis result : checkResults) {
      List<WikipediaRuleMatch> addedMatches = result.getAddedMatches();
      List<WikipediaRuleMatch> removedMatches = result.getRemovedMatches();
      if (addedMatches.size() > 0 || removedMatches.size() > 0) {
        System.out.println("'" + result.getTitle() + "' new and removed matches:");
        for (WikipediaRuleMatch match : addedMatches) {
          System.out.println("    [+] " + getId(match.getRule()) + ": " +  match.getErrorContext());
          if (matchDatabase != null) {
            matchDatabase.add(match);
          }
        }
        for (WikipediaRuleMatch match : removedMatches) {
          System.out.println("    [-] " + getId(match.getRule()) + ": " +  match.getErrorContext());
          if (matchDatabase != null) {
            matchDatabase.markedFixed(match);
          }
        }
        String diffLink = "https://" + language.getShortName() + ".wikipedia.org/w/index.php?title="
                + URLEncoder.encode(result.getTitle().replace(" ", "_"), "UTF-8") + "&diff=" + result.getDiffId();
        System.out.println("    " + diffLink);
      }
    }
  }

  private String getId(Rule rule) {
    if (rule instanceof PatternRule) {
      return rule.getId() + "[" + ((PatternRule) rule).getSubId() + "]";
    } else {
      return rule.getId();
    }
  }

  CheckResult checkChanges(URL atomFeedUrl) throws IOException {
    System.out.println("Getting atom feed from " + atomFeedUrl);
    InputStream xml = getXmlStream(atomFeedUrl);
    return checkChanges(xml);
  }

  CheckResult checkChanges(InputStream xml) throws IOException {
    Date lastDateOfPreviousRun = matchDatabase != null ? matchDatabase.getLatestDate() : null;
    List<ChangeAnalysis> result = new ArrayList<>();
    long latestDiffId = 0;
    int skipCount = 0;
    try {
      List<AtomFeedItem> items = new AtomFeedParser().getAtomFeedItems(xml);
      Collections.reverse(items);   // older items must come first so we iterate in the order in which the changes were made
      printDates(items, lastDateOfPreviousRun);
      for (AtomFeedItem item : items) {
        // Note: this skipping is not exact for two reasons:
        // 1) We only have the latest date of a change that actually led to a rule match (this kind of doesn't
        //    matter, because a new check won't find any matches either)
        // 2) A resolution of one second may not be enough, considering the amount of changes happening,
        //    but I didn't find an id that's constantly increasing (diff=... often but not always increases)
        if (lastDateOfPreviousRun != null && (item.getDate().before(lastDateOfPreviousRun) || item.getDate().equals(lastDateOfPreviousRun))) {
          System.out.println("Skipping " + item.getTitle() + ", date " + item.getDate());
          skipCount++;
        } else {
          try {
            System.out.println("Checking " + item.getTitle() + ", diff #" + item.getDiffId());
            List<WikipediaRuleMatch> oldMatches = getMatches(item, item.getOldContent());
            List<WikipediaRuleMatch> newMatches = getMatches(item, item.getNewContent());
            ChangeAnalysis changeAnalysis = new ChangeAnalysis(item.getTitle(), item.getDiffId(), oldMatches, newMatches);
            result.add(changeAnalysis);
            if (item.getDiffId() > latestDiffId) {
              latestDiffId = item.getDiffId();
            }
          } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();  // don't just stop because of Sweble conversion problems etc.
          }
        }
      }
    } catch (XMLStreamException e) {
      throw new RuntimeException(e);
    }
    if (lastDateOfPreviousRun != null && skipCount == 0) {
      System.err.println("Warning: no items from the Atom feed were skipped - this means that changes might be missing");
    }
    return new CheckResult(result, latestDiffId);
  }

  private void printDates(List<AtomFeedItem> items, Date lastDateOfPreviousRun) {
    if (items.size() > 0) {
      Date firstDate = items.get(0).getDate();
      Date lastDate = items.get(items.size()-1).getDate();
      System.out.println("Latest date in database: " + lastDateOfPreviousRun);
      System.out.println("Dates in Atom Feed:      " + firstDate + " - " + lastDate);
    }
  }

  private List<WikipediaRuleMatch> getMatches(AtomFeedItem item, List<String> texts) throws IOException {
    List<WikipediaRuleMatch> oldMatches = new ArrayList<>();
    for (String text : texts) {
      PlainTextMapping filteredContent = textFilter.filter(text);
      List<RuleMatch> ruleMatches = langTool.check(filteredContent.getPlainText());
      oldMatches.addAll(toWikipediaRuleMatches(text, filteredContent, ruleMatches, item));
    }
    return oldMatches;
  }

  private List<WikipediaRuleMatch> toWikipediaRuleMatches(String content, PlainTextMapping filteredContent, List<RuleMatch> ruleMatches, AtomFeedItem item) {
    List<WikipediaRuleMatch> result = new ArrayList<>();
    for (RuleMatch ruleMatch : ruleMatches) {
      Location fromPos = filteredContent.getOriginalTextPositionFor(ruleMatch.getFromPos() + 1);
      Location toPos = filteredContent.getOriginalTextPositionFor(ruleMatch.getToPos() + 1);
      int origFrom = LocationHelper.absolutePositionFor(fromPos, content);
      int origTo = LocationHelper.absolutePositionFor(toPos, content);
      String errorContext = contextTools.getContext(origFrom, origTo, content);
      result.add(new WikipediaRuleMatch(language, ruleMatch, errorContext, item));
    }
    return result;
  }

  private InputStream getXmlStream(URL url) throws IOException {
    URLConnection conn = url.openConnection();
    conn.setRequestProperty("User-Agent", USER_AGENT);
    return conn.getInputStream();
  }

}
