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
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.ContextTools;
import xtc.tree.Location;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Check the changes from a Wikipedia Atom feed with LanguageTool, only getting
 * the errors that have been introduced by that change.
 * @since 2.4
 */
class AtomFeedChecker {

  private static final int CONTEXT_SIZE = 60;
  
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

  CheckResult runCheck(String url, long latestDiffId) throws IOException {
    CheckResult checkResult = checkChanges(new URL(url), latestDiffId);
    List<ChangeAnalysis> checkResults = checkResult.getCheckResults();
    System.out.println("Check results:");
    for (ChangeAnalysis result : checkResults) {
      List<WikipediaRuleMatch> addedMatches = result.getAddedMatches();
      List<WikipediaRuleMatch> removedMatches = result.getRemovedMatches();
      if (addedMatches.size() > 0 || removedMatches.size() > 0) {
        System.out.println("'" + result.getTitle() + "' new and removed matches:");
        for (WikipediaRuleMatch match : addedMatches) {
          System.out.println("    [+] " + match.getRule().getId() + ": " +  match.getErrorContext());
          if (matchDatabase != null) {
            matchDatabase.add(match);
          }
        }
        for (WikipediaRuleMatch match : removedMatches) {
          System.out.println("    [-] " + match.getRule().getId() + ": " +  match.getErrorContext());
          if (matchDatabase != null) {
            matchDatabase.markedFixed(match);
          }
        }
        String diffLink = "https://" + language.getShortName() + ".wikipedia.org/w/index.php?title="
                + URLEncoder.encode(result.getTitle().replace(" ", "_"), "UTF-8") + "&diff=" + result.getDiffId();
        System.out.println("    " + diffLink);
      }
    }
    return checkResult;
  }

  CheckResult checkChanges(URL atomFeedUrl, long lastCheckedDiffId) throws IOException {
    System.out.println("Getting atom feed from " + atomFeedUrl);
    InputStream xml = getXmlStream(atomFeedUrl);
    return checkChanges(xml, lastCheckedDiffId);
  }

  CheckResult checkChanges(InputStream xml, long lastCheckedDiffId) throws IOException {
    List<ChangeAnalysis> result = new ArrayList<>();
    long latestDiffId = 0;
    try {
      List<AtomFeedItem> items = new AtomFeedParser().getAtomFeedItems(xml);
      Collections.reverse(items);   // older items must come first so we iterate in the order in which the changes were made
      for (AtomFeedItem item : items) {
        if (lastCheckedDiffId > 0 && item.getDiffId() < lastCheckedDiffId) {
          System.out.println("Skipping "  + item.getTitle() + ", diff id " + item.getDiffId() + " < " + lastCheckedDiffId);
        } else {
          try {
            System.out.println("Checking "  + item.getTitle() + ", diff id " + item.getDiffId());
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
    return new CheckResult(result, latestDiffId);
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
      result.add(new WikipediaRuleMatch(ruleMatch, errorContext, item.getTitle(), item.getDate()));
    }
    return result;
  }

  private InputStream getXmlStream(URL atomFeedUrl) throws IOException {
    return (InputStream) atomFeedUrl.getContent();
  }

}
