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
package org.languagetool.rules.patterns;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;
import org.languagetool.tools.StringTools;

/**
 * A Rule that describes a language error as a simple pattern of words or of
 * part-of-speech tags.
 * 
 * @author Daniel Naber
 */
public class PatternRule extends AbstractTokenBasedRule {

  private final String shortMessage;

  // A list of elements as they appear in XML file (phrases count as single tokens in case of matches or skipping).
  private final List<Integer> elementNo;

  // This property is used for short-circuiting evaluation of the elementNo list order:
  private final boolean useList;
  
  private boolean interpretPosTagsPreDisambiguation;

  // Marks whether the rule is a member of a disjunctive set (in case of OR operation on phraserefs).
  private boolean isMemberOfDisjunctiveSet;

  /**
   * @param id Id of the Rule. Used in configuration. Should not contain special characters and should
   *        be stable over time, unless the rule changes completely.
   * @param language Language of the Rule
   * @param description Description to be shown (name)
   * @param message Message to be displayed to the user
   * @param shortMessage Message to be displayed to the user in the context menu in OpenOffice.org/LibreOffice
   */
  public PatternRule(String id, Language language,
      List<PatternToken> patternTokens, String description,
      String message, String shortMessage, String suggestionsOutMsg) {
    super(id, description, language, patternTokens, false);
    this.message = Objects.requireNonNull(message);
    this.shortMessage = Objects.requireNonNull(shortMessage);
    this.elementNo = new ArrayList<>(0);
    Objects.requireNonNull(suggestionsOutMsg);
    this.suggestionsOutMsg = suggestionsOutMsg.isEmpty() ? "" : suggestionsOutMsg;
    String prevName = "";
    String curName;
    int cnt = 0;
    int loopCnt = 0;
    boolean tempUseList = false;

    for (PatternToken pToken : patternTokens) {
      if (pToken.isPartOfPhrase()) {
        curName = pToken.getPhraseName();
        if (StringTools.isEmpty(prevName) || prevName.equals(curName)) {
          cnt++;
          tempUseList = true;
        } else {
          elementNo.add(cnt);
          curName = "";
          cnt = 0;
        }
        prevName = curName;
        loopCnt++;
        if (loopCnt == this.patternTokens.size() && !StringTools.isEmpty(prevName)) {
          elementNo.add(cnt);
        }
      } else {
        if (cnt > 0) {
          elementNo.add(cnt);
        }
        elementNo.add(1);
        loopCnt++;
      }
    }
    useList = tempUseList;
  }
  
  public PatternRule(String id, Language language,
      List<PatternToken> patternTokens, String description,
      String message, String shortMessage) {
    this(id, language, patternTokens, description, message, shortMessage, "");
  }

  public PatternRule(String id, Language language,
      List<PatternToken> patternTokens, String description,
      String message, String shortMessage, String suggestionsOutMsg,
      boolean isMember) {
    this(id, language, patternTokens, description, message, shortMessage, suggestionsOutMsg, isMember, false);
  }

  /**
   * @since 4.5
   */
  public PatternRule(String id, Language language,
      List<PatternToken> patternTokens, String description,
      String message, String shortMessage, String suggestionsOutMsg,
      boolean isMember, boolean interpretPosTagsPreDisambiguation) {
    this(id, language, patternTokens, description, message, shortMessage, suggestionsOutMsg);
    this.isMemberOfDisjunctiveSet = isMember;
    this.interpretPosTagsPreDisambiguation = interpretPosTagsPreDisambiguation;
  }

  @Override
  public int estimateContextForSureMatch() {
    int extendAfterMarker = 0;
    boolean markerSeen = false;
    for (PatternToken pToken : this.patternTokens) {
      if (markerSeen && !pToken.isInsideMarker()) {
        extendAfterMarker++;
      }
      if (JLanguageTool.SENTENCE_END_TAGNAME.equals(pToken.getPOStag())) {
        // e.g. for DT_JJ_NO_NOUN and all rules that match the sentence end
        extendAfterMarker++;
      }
      if (pToken.isInsideMarker()) {
        markerSeen = true;
      }
      if (pToken.getSkipNext() == -1) {
        return -1;
      } else {
        extendAfterMarker += pToken.getSkipNext();
      }
    }
    List<Integer> antiPatternLengths = getAntiPatterns().stream().map(p -> p.patternTokens.size()).collect(Collectors.toList());
    int longestAntiPattern = antiPatternLengths.stream().max(Comparator.comparing(i -> i)).orElse(0);
    int longestSkip = 0;
    for (DisambiguationPatternRule antiPattern : getAntiPatterns()) {
      for (PatternToken token : antiPattern.getPatternTokens()) {
        if (token.getSkipNext() == -1) {
          return -1;
        } else if (token.getSkipNext() > longestSkip) {
          longestSkip = token.getSkipNext();
        }
      }
    }
    //System.out.println("extendAfterMarker: " + extendAfterMarker + ", antiPatternLengths: " + antiPatternLengths + ", longestSkip: " + longestSkip);
    return extendAfterMarker + Math.max(longestAntiPattern, longestAntiPattern + longestSkip);
  }

  /**
   * Whether any POS tags from this rule should refer to the POS tags of the analyzed
   * sentence *before* disambiguation.
   * @since 4.5
   */
  boolean isInterpretPosTagsPreDisambiguation() {
    return interpretPosTagsPreDisambiguation;
  }
  
  /**
   * Used for testing rules: only one of the set can match.
   * @return Whether the rule can non-match (as a member of disjunctive set of
   *         rules generated by phraseref in includephrases element).
   */
  final boolean isWithComplexPhrase() {
    return isMemberOfDisjunctiveSet;
  }

  /** Reset complex status - used for testing. **/
  final void notComplexPhrase() {
    isMemberOfDisjunctiveSet = false;
  }

  /**
   * Return the pattern as a string, using toString() on the pattern elements.
   * @since 0.9.2
   */
  public final String toPatternString() {
    List<String> strList = new ArrayList<>();
    for (PatternToken patternPatternToken : patternTokens) {
      strList.add(patternPatternToken.toString());
    }
    return String.join(", ", strList);
  }

  /**
   * Return the rule's definition as an XML string, loaded from the XML rule files.
   * @since 0.9.3
   */
  public final String toXML() {
    return new PatternRuleXmlCreator().toXML(new PatternRuleId(getId(), getSubId()), getLanguage());
  }

  @Override
  public final RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    if (canBeIgnoredFor(sentence)) return RuleMatch.EMPTY_ARRAY;
    
    RuleMatcher matcher = new PatternRuleMatcher(this, useList);
    return checkForAntiPatterns(sentence, matcher, matcher.match(sentence));

  }

  private RuleMatch[] checkForAntiPatterns(AnalyzedSentence sentence, RuleMatcher matcher, RuleMatch[] matches) throws IOException {
    if (matches != null && matches.length > 0 && !getAntiPatterns().isEmpty()) {
      AnalyzedSentence immunized = getSentenceWithImmunization(sentence);
      if (Arrays.stream(immunized.getTokens()).anyMatch(AnalyzedTokenReadings::isImmunized)) {
        return matcher.match(immunized);
      }
    }
    return matches;
  }

  List<Integer> getElementNo() {
    return elementNo;
  }

  /* (non-Javadoc)
   * @see org.languagetool.rules.patterns.AbstractPatternRule#getShortMessage()
   */
  @Override
  public String getShortMessage() {
    return shortMessage;
  }
  
}
