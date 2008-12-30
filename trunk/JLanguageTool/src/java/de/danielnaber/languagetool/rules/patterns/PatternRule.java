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
import java.util.ArrayList;
import java.util.List;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.IncorrectExample;
import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * A Rule that describes a language error as a simple pattern of words or of
 * part-of-speech tags.
 * 
 * @author Daniel Naber
 */
public class PatternRule extends Rule {

  private String id;
  private String subId; // because there can be more than one rule in a rule
  // group

  private final Language language;

  private static final String SUGG_TAG = "<suggestion>";

  private String description;
  private String message;
  private String shortMessage;

  private int startPosCorr;
  private int endPosCorr;

  private List<Element> patternElements;

  /** Formatted suggestion elements. **/
  private List<Match> suggestionMatches;

  /**
   * A list of elements as they appear in XML file (phrases count as single
   * tokens in case of matches or skipping).
   */
  private List<Integer> elementNo;

  /**
   * This property is used for short-circuiting evaluation of the elementNo list
   * order.
   */
  private boolean useList;

  /**
   * Marks whether the rule is a member of a disjunctive set (in case of OR
   * operation on phraserefs).
   **/
  private boolean isMemberOfDisjunctiveSet = false;

  /**
   * @param id
   *          Id of the Rule
   * @param language
   *          Language of the Rule
   * @param elements
   *          Element (token) list
   * @param description
   *          Description to be shown (name)
   * @param message
   *          Message to be displayed to the user
   */

  PatternRule(final String id, final Language language,
      final List<Element> elements, final String description,
      final String message, final String shortMessage) {
    super();
    if (id == null) {
      throw new NullPointerException("id cannot be null");
    }
    if (language == null) {
      throw new NullPointerException("language cannot be null");
    }
    if (elements == null) {
      throw new NullPointerException("elements cannot be null");
    }
    if (description == null) {
      throw new NullPointerException("description cannot be null");
    }
    this.id = id;
    this.language = language;
    this.description = description;
    this.message = message;
    this.shortMessage = shortMessage;
    this.patternElements = new ArrayList<Element>(elements); // copy elements

    this.elementNo = new ArrayList<Integer>();
    String prevName = "";
    String curName = "";
    int cnt = 0;
    int loopCnt = 0;
    for (final Element e : patternElements) {
      if (!e.isPartOfPhrase()) {
        if (cnt > 0) {
          elementNo.add(cnt);
        }
        elementNo.add(1);
        loopCnt++;
      } else {
        curName = e.getPhraseName();
        if (prevName.equals(curName) || StringTools.isEmpty(prevName)) {
          cnt++;
          useList = true;
        } else {
          elementNo.add(cnt);
          prevName = "";
          curName = "";
          cnt = 0;
        }
        prevName = curName;
        loopCnt++;
        if (loopCnt == patternElements.size() && !StringTools.isEmpty(prevName)) {
          elementNo.add(cnt);
        }
      }
    }
  }

  PatternRule(final String id, final Language language,
      final List<Element> elements, final String description,
      final String message, final String shortMessage, final boolean isMember) {
    this(id, language, elements, description, message, shortMessage);
    this.isMemberOfDisjunctiveSet = isMember;
  }

  @Override
  public final String getId() {
    return id;
  }

  @Override
  public final String getDescription() {
    return description;
  }

  public final String getSubId() {
    return subId;
  }

  public final void setSubId(final String subId) {
    this.subId = subId;
  }

  public final String getMessage() {
    return message;
  }

  /**
   * Used for testing rules: only one of the set can match.
   * 
   * @return Whether the rule can non-match (as a member of disjunctive set of
   *         rules generated by phraseref in includephrases element).
   */
  public final boolean isWithComplexPhrase() {
    return isMemberOfDisjunctiveSet;
  }

  /** Reset complex status - used for testing. **/
  public final void notComplexPhrase() {
    isMemberOfDisjunctiveSet = false;
  }

  @Override
  public final String toString() {
    return id + ":" + patternElements + ":" + description;
  }

  /**
   * Return the pattern as a string.
   * 
   * @since 0.9.2
   */
  public final String toPatternString() {
    final List<String> strList = new ArrayList<String>();
    for (Element patternElement : patternElements) {
      strList.add(patternElement.toString());
    }
    return StringTools.listToString(strList, ", ");
  }

  /**
   * Return the pattern as an XML string. FIXME: this is not complete,
   * information might be lost!
   * 
   * @since 0.9.3
   */
  public final String toXML() {
    final StringBuilder sb = new StringBuilder();
    sb.append("<rule id=\"");
    sb.append(StringTools.escapeXML(id));
    sb.append("\" name=\"");
    sb.append(StringTools.escapeXML(description));
    sb.append("\">\n");
    sb.append("<pattern mark_from=\"");
    sb.append(startPosCorr);
    sb.append("\" mark_to=\"");
    sb.append(endPosCorr);
    sb.append("\"");
    // for now, case sensitivity is per pattern, not per element,
    // so just use the setting of the first element:
    if (!patternElements.isEmpty() && patternElements.get(0).getCaseSensitive()) {
      sb.append(" case_sensitive=\"yes\"");
    }
    sb.append(">\n");
    for (Element patternElement : patternElements) {
      sb.append("<token");
      if (patternElement.getNegation()) {
        sb.append(" negate=\"yes\"");
      }
      if (patternElement.isRegularExpression()) {
        sb.append(" regexp=\"yes\"");
      }
      if (patternElement.getPOStag() != null) {
        sb.append(" postag=\"" + patternElement.getPOStag() + "\"");
      }
      if (patternElement.getPOSNegation()) {
        sb.append(" negate_pos=\"yes\"");
      }
      if (patternElement.isInflected()) {
        sb.append(" inflected=\"yes\"");
      }
      sb.append(">");
      if (patternElement.getString() != null) {
        sb.append(StringTools.escapeXML(patternElement.getString()));
      } else {
        // TODO
      }
      sb.append("</token>\n");
    }
    sb.append("</pattern>\n");
    sb.append("<message>");
    sb.append(StringTools.escapeXML(message));
    sb.append("</message>\n");
    if (getIncorrectExamples() != null) {
      for (IncorrectExample example : getIncorrectExamples()) {
        sb.append("<example type=\"incorrect\">");
        sb.append(StringTools.escapeXML(example.getExample()));
        sb.append("</example>\n");
      }
    }
    if (getCorrectExamples() != null) {
      for (String example : getCorrectExamples()) {
        sb.append("<example type=\"correct\">");
        sb.append(StringTools.escapeXML(example));
        sb.append("</example>\n");
      }
    }
    sb.append("</rule>");
    return sb.toString();
  }

  public final void setMessage(final String message) {
    this.message = message;
  }

  public final void setStartPositionCorrection(final int startPositionCorrection) {
    this.startPosCorr = startPositionCorrection;
  }

  public final void setEndPositionCorrection(final int endPositionCorrection) {
    this.endPosCorr = endPositionCorrection;
  }

  // TODO: divide this lengthy method into shorter ones!
  @Override
  public final RuleMatch[] match(final AnalyzedSentence text)
      throws IOException {
    final List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    final AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();
    final int[] tokenPositions = new int[tokens.length + 1];
    int tokenPos = 0;
    int prevSkipNext = 0;
    int skipNext = 0;
    int matchPos = 0;
    int skipShift = 0;
    // this variable keeps the total number
    // of tokens skipped - used to avoid
    // that nextPos gets back to unmatched tokens...
    int skipShiftTotal = 0;
    int firstMatchToken = -1;
    int lastMatchToken = -1;
    final int patternSize = patternElements.size();
    Element elem = null, prevElement = null;
    final boolean sentStart = patternElements.get(0).isSentStart();
    language.getUnifier().reset();
    boolean inUnification = false;
    boolean uniMatched = false;

    for (int i = 0; i < tokens.length; i++) {
      boolean allElementsMatch = true;
      // stop processing if rule is longer than the sentence
      // or stop looking for sent_start - it will never match any token except
      // the first
      if (patternSize + i > tokens.length || sentStart && i > 0) {
        allElementsMatch = false;
        break;
      }
      int matchingTokens = 0;
      for (int k = 0; (k < patternSize); k++) {
        if (elem != null) {
          prevElement = elem;
        }
        elem = patternElements.get(k);
        skipNext = translateElementNo(elem.getSkipNext());
        final int nextPos = tokenPos + k + skipShiftTotal;
        if (nextPos >= tokens.length) {
          allElementsMatch = false;
          break;
        }
        boolean skipMatch = false, thisMatched = false, prevMatched = false;
        boolean exceptionMatched = false;
        if (prevSkipNext + nextPos >= tokens.length || prevSkipNext < 0) { // SENT_END?
          prevSkipNext = tokens.length - (nextPos + 1);
        }
        for (int m = nextPos; m <= nextPos + prevSkipNext; m++) {
          boolean matched = false;
          final int numberOfReadings = tokens[m].getReadingsLength();
          for (int l = 0; l < numberOfReadings; l++) {
            final boolean lastReading = l + 1 == numberOfReadings;
            final AnalyzedToken matchToken = tokens[m].getAnalyzedToken(l);
            if (prevSkipNext > 0 && prevElement != null
                && prevElement.isMatchedByScopeNextException(matchToken)) {
              exceptionMatched = true;
              prevMatched = true;
            }
            if (elem.isReferenceElement()) {
              setupRef(firstMatchToken, elem, tokens);
            }
            if (elem.hasAndGroup()) {
              for (final Element andElement : elem.getAndGroup()) {
                if (andElement.isReferenceElement()) {
                  setupRef(firstMatchToken, andElement, tokens);
                }
              }
              if (l == 0) {
                elem.setupAndGroup();
              }
            }
            thisMatched |= elem.isMatchedCompletely(matchToken);
            if (thisMatched && elem.isUnified()) {
              if (inUnification) {
                uniMatched = uniMatched
                    || language.getUnifier().isSatisfied(matchToken,
                        elem.getUniFeature(), elem.getUniType());
                if (lastReading) {
                  thisMatched &= uniMatched;
                  language.getUnifier().startNextToken();
                }
              } else {
                if (elem.getUniNegation()) {
                  language.getUnifier().setNegation(true);
                }
                thisMatched |= language.getUnifier().isSatisfied(matchToken,
                    elem.getUniFeature(), elem.getUniType());
                if (lastReading) {
                  inUnification = true;
                  uniMatched = false;
                  language.getUnifier().startUnify();
                }
              }
            }
            if (!elem.isUnified()) {
              inUnification = false;
              uniMatched = false;
              language.getUnifier().reset();
            }
            if (lastReading && elem.hasAndGroup()) {
              thisMatched &= elem.checkAndGroup(thisMatched);
            }
            exceptionMatched |= elem.isExceptionMatchedCompletely(matchToken);
            if (!exceptionMatched && m > 0 && elem.hasPreviousException()) {
              final int numReadings = tokens[m - 1].getReadingsLength();
              for (int p = 0; p < numReadings; p++) {
                exceptionMatched |= elem
                    .isMatchedByScopePreviousException(tokens[m - 1]
                        .getAnalyzedToken(p));
              }
            }
            // Logical OR (cannot be AND):
            if (thisMatched || exceptionMatched) {
              matched = true;
              matchPos = m;
              skipShift = matchPos - nextPos;
              tokenPositions[matchingTokens] = skipShift + 1;
            } else {
              matched |= false;
            }
            skipMatch = (skipMatch || matched) && !exceptionMatched;
          }
          // disallow exceptions that should match only current tokens
          if (!(thisMatched || prevMatched)) {
            exceptionMatched = false;
            skipMatch = false;
          }
          if (skipMatch) {
            break;
          }
        }
        allElementsMatch = skipMatch;
        if (skipMatch) {
          prevSkipNext = skipNext;
          matchingTokens++;
          lastMatchToken = matchPos;
          if (firstMatchToken == -1) {
            firstMatchToken = matchPos;
          }
          skipShiftTotal += skipShift;
        } else {
          prevSkipNext = 0;
          skipShiftTotal = 0;
          break;
        }
      }
      tokenPos++;
      if (firstMatchToken + matchingTokens >= tokens.length) {
        matchingTokens = tokens.length - firstMatchToken;
      }
      if (firstMatchToken + skipShiftTotal + matchingTokens > tokens.length) {
        allElementsMatch = false;
      }
      if (allElementsMatch) {
        final RuleMatch rM = createRuleMatch(tokenPositions, tokens,
            firstMatchToken, lastMatchToken, matchingTokens);
        if (rM != null) {
          ruleMatches.add(rM);
        }
      }
      firstMatchToken = -1;
      lastMatchToken = -1;
      skipShiftTotal = 0;
      language.getUnifier().reset();
      inUnification = false;
      uniMatched = false;
    }
    return ruleMatches.toArray(new RuleMatch[ruleMatches.size()]);
  }

  private void setupRef(final int firstMatchToken, final Element elem,
      final AnalyzedTokenReadings[] tokens) {
    final int refPos = firstMatchToken + elem.getMatch().getTokenRef();
    if (refPos < tokens.length) {
      elem.compile(tokens[refPos], language.getSynthesizer());
    }
  }

  private RuleMatch createRuleMatch(final int[] tokenPositions,
      final AnalyzedTokenReadings[] tokens, final int firstMatchToken,
      final int lastMatchToken, final int matchingTokens) throws IOException {
    final String errMessage = formatMatches(tokens, tokenPositions,
        firstMatchToken, message);
    int correctedStPos = 0;
    if (startPosCorr > 0) {
      for (int l = 0; l <= startPosCorr; l++) {
        correctedStPos += tokenPositions[l];
      }
      correctedStPos--;
    }
    int correctedEndPos = 0;
    if (endPosCorr < 0) {
      int l = 0;
      while (l > endPosCorr) {
        correctedEndPos -= tokenPositions[matchingTokens + l - 1];
        l--;
      }
    }
    AnalyzedTokenReadings firstMatchTokenObj = tokens[firstMatchToken
        + correctedStPos];
    boolean startsWithUppercase = StringTools
        .startsWithUppercase(firstMatchTokenObj.getToken())
        && !matchConvertsCase();

    if (firstMatchTokenObj.isSentStart()
        && tokens.length > firstMatchToken + correctedStPos + 1) {
      // make uppercasing work also at sentence start:
      firstMatchTokenObj = tokens[firstMatchToken + correctedStPos + 1];
      startsWithUppercase = StringTools.startsWithUppercase(firstMatchTokenObj
          .getToken());
    }
    int fromPos = tokens[firstMatchToken + correctedStPos].getStartPos();
    // FIXME: this is fishy, assumes that comma should always come before
    // whitespace
    if (errMessage.contains(SUGG_TAG + ",")
        && firstMatchToken + correctedStPos >= 1) {
      fromPos = tokens[firstMatchToken + correctedStPos - 1].getStartPos()
          + tokens[firstMatchToken + correctedStPos - 1].getToken().length();
    }

    final int toPos = tokens[lastMatchToken + correctedEndPos].getStartPos()
        + tokens[lastMatchToken + correctedEndPos].getToken().length();
    if (fromPos < toPos) { // this can happen with some skip="-1" when the last
      // token is not matched
      final RuleMatch ruleMatch = new RuleMatch(this, fromPos, toPos,
          errMessage, shortMessage, startsWithUppercase);
      return ruleMatch;
    } else { // failed to create any rule match...
      return null;
    }
  }

  /**
   * Checks if the suggestion starts with a match that is supposed to convert
   * case. If it does, stop the default conversion to uppercase.
   * 
   * @return true, if the match converts the case of the token.
   */
  private boolean matchConvertsCase() {
    boolean convertsCase = false;
    if (suggestionMatches != null && !suggestionMatches.isEmpty()) {
      final int sugStart = message.indexOf(SUGG_TAG) + SUGG_TAG.length();
      convertsCase = (suggestionMatches.get(0).convertsCase() && message
          .charAt(sugStart) == '\\');
    }
    return convertsCase;
  }

  public final void addSuggestionMatch(final Match m) {
    if (suggestionMatches == null) {
      suggestionMatches = new ArrayList<Match>();
    }
    suggestionMatches.add(m);
  }

  /**
   * Gets the index of the element indexed by i, adding any offsets because of
   * the phrases in the rule.
   * 
   * @param i
   *          Current element index.
   * @return int Index translated into XML element no.
   */
  private int translateElementNo(final int i) {
    if (!useList || i < 0) {
      return i;
    }
    int j = 0;
    for (int k = 0; k < i; k++) {
      j += elementNo.get(k);
    }
    return j;
  }

  /**
   * Returns true when the token in the rule references a phrase composed of
   * many tokens.
   * 
   * @param i
   *          The index of the token.
   * @return true if the phrase is under the index, false otherwise.
   **/
  private int phraseLen(final int i) {
    if (!useList || i > (elementNo.size() - 1)) {
      return 1;
    }
    return elementNo.get(i);
  }

  /**
   * Creates a Cartesian product of the arrays stored in the input array.
   * 
   * @param input
   *          Array of string arrays to combine.
   * @param output
   *          Work array of strings.
   * @param r
   *          Starting parameter (use 0 to get all combinations).
   * @return Combined array of @String.
   */
  private static String[] combineLists(final String[][] input,
      final String[] output, final int r, final Language lang) {
    final List<String> outputList = new ArrayList<String>();
    if (r == input.length) {
      final StringBuilder sb = new StringBuilder();
      for (int k = 0; k < output.length; k++) {
        sb.append(output[k]);
        if (k < output.length - 1) {
          sb.append(StringTools.addSpace(output[k + 1], lang));
        }
      }
      outputList.add(sb.toString());
    } else {
      for (int c = 0; c < input[r].length; c++) {
        output[r] = input[r][c];
        String[] sList;
        sList = combineLists(input, output, r + 1, lang);
        for (final String s : sList) {
          outputList.add(s);
        }
      }
    }
    return outputList.toArray(new String[outputList.size()]);
  }

  /**
   * Concatenates the matches, and takes care of phrases (including inflection
   * using synthesis).
   * 
   * @param start
   *          Position of the element as referenced by match element in the
   *          rule.
   * @param index
   *          The index of the element found in the matching sentence.
   * @param tokenIndex
   *          The position of the token in the AnalyzedTokenReadings array.
   * @param tokens
   *          Array of @AnalyzedTokenReadings
   * @return @String[] Array of concatenated strings
   * @throws IOException
   *           in case disk operations (used in synthesizer) go wrong.
   */
  private String[] concatMatches(final int start, final int index,
      final int tokenIndex, final AnalyzedTokenReadings[] tokens)
      throws IOException {
    String[] finalMatch = null;
    if (suggestionMatches.get(start) != null) {
      final int len = phraseLen(index);
      if (len == 1) {
        suggestionMatches.get(start).setToken(tokens[tokenIndex - 1]);
        suggestionMatches.get(start).setSynthesizer(language.getSynthesizer());
        finalMatch = suggestionMatches.get(start).toFinalString();
      } else {
        final List<String[]> matchList = new ArrayList<String[]>();
        for (int i = 0; i < len; i++) {
          suggestionMatches.get(start).setToken(tokens[tokenIndex - 1 + i]);
          suggestionMatches.get(start)
              .setSynthesizer(language.getSynthesizer());
          matchList.add(suggestionMatches.get(start).toFinalString());
        }
        return combineLists(matchList.toArray(new String[matchList.size()][]),
            new String[matchList.size()], 0, language);
      }
    }
    return finalMatch;
  }

  /**
   * Replace back references generated with &lt;match&gt; and \\1 in message
   * using Match class, and take care of skipping. *
   * 
   * @param toks
   *          Array of AnalyzedTokenReadings that were matched against the
   *          pattern
   * @param positions
   *          Array of relative positions of matched tokens
   * @param firstMatchTok
   *          Position of the first matched token
   * @param errorMsg
   *          String containing suggestion markup
   * @return String Formatted message.
   * @throws IOException
   * 
   **/
  private String formatMatches(final AnalyzedTokenReadings[] toks,
      final int[] positions, final int firstMatchTok, final String errorMsg)
      throws IOException {
    String errorMessage = errorMsg;
    int matchCounter = 0;
    final int[] numbersToMatches = new int[errorMsg.length()];
    boolean newWay = false;
    int errLen = errorMessage.length();
    int errMarker = errorMessage.indexOf('\\');
    boolean numberFollows = false;
    if (errMarker > 0 && errMarker < errLen - 1) {
      numberFollows = StringTools.isPositiveNumber(errorMessage
          .charAt(errMarker + 1));
    }
    while (errMarker > 0 && numberFollows) {
      final int ind = errorMessage.indexOf('\\');
      if (ind > 0 && StringTools.isPositiveNumber(errorMessage.charAt(ind + 1))) {
        int numLen = 1;
        while (ind + numLen < errorMessage.length()
            && StringTools.isPositiveNumber(errorMessage.charAt(ind + numLen))) {
          numLen++;
        }
        final int j = Integer.parseInt(errorMessage.substring(ind + 1, ind
            + numLen)) - 1;
        int repTokenPos = 0;
        for (int l = 0; l <= j; l++) {
          repTokenPos += positions[l];
        }
        if (suggestionMatches != null) {
          if (matchCounter < suggestionMatches.size()) {
            numbersToMatches[j] = matchCounter;
            if (suggestionMatches.get(matchCounter) != null) {
              final String[] matches = concatMatches(matchCounter, j,
                  firstMatchTok + repTokenPos, toks);
              final String leftSide = errorMessage.substring(0, ind);
              final String rightSide = errorMessage.substring(ind + numLen);
              if (matches.length == 1) {
                errorMessage = leftSide + matches[0] + rightSide;
              } else {
                errorMessage = formatMultipleSynthesis(matches, leftSide,
                    rightSide);
              }
              matchCounter++;
              newWay = true;
            }
          } else {
            // FIXME: is this correct? this is how we deal with multiple
            // matches
            suggestionMatches.add(suggestionMatches.get(numbersToMatches[j]));
          }
        }

        if (!newWay) {
          // in case <match> elements weren't used (yet)
          errorMessage = errorMessage.replace("\\" + (j + 1),
              toks[firstMatchTok + repTokenPos - 1].getToken());
        }
      }
      errMarker = errorMessage.indexOf('\\');
      numberFollows = false;
      errLen = errorMessage.length();
      if (errMarker > 0 && errMarker < errLen - 1) {
        numberFollows = StringTools.isPositiveNumber(errorMessage
            .charAt(errMarker + 1));
      }
    }
    return errorMessage;
  }

  private String formatMultipleSynthesis(final String[] matches,
      final String leftSide, final String rightSide) {
    String errorMessage = "";
    String suggestionLeft = "";
    String suggestionRight = "";
    String rightSideNew = rightSide;
    final int sPos = leftSide.lastIndexOf(SUGG_TAG);
    if (sPos > 0) {
      suggestionLeft = leftSide.substring(sPos + SUGG_TAG.length());
    }
    if (StringTools.isEmpty(suggestionLeft)) {
      errorMessage = leftSide;
    } else {
      errorMessage = leftSide.substring(0, leftSide.lastIndexOf(SUGG_TAG))
          + SUGG_TAG;
    }
    final int rPos = rightSide.indexOf("</suggestion>");
    if (rPos > 0) {
      suggestionRight = rightSide.substring(0, rPos);
    }
    if (!StringTools.isEmpty(suggestionRight)) {
      rightSideNew = rightSide.substring(rightSide.indexOf("</suggestion>"));
    }
    final int lastLeftSugEnd = leftSide.indexOf("</suggestion>");
    final int lastLeftSugStart = leftSide.lastIndexOf(SUGG_TAG);
    final StringBuilder sb = new StringBuilder();
    sb.append(errorMessage);
    for (int z = 0; z < matches.length; z++) {
      sb.append(suggestionLeft);
      sb.append(matches[z]);
      sb.append(suggestionRight);
      if ((z < matches.length - 1) && lastLeftSugEnd < lastLeftSugStart) {
        sb.append("</suggestion>, ");
        sb.append(SUGG_TAG);
      }
    }
    sb.append(rightSideNew);
    return sb.toString();
  }

  /**
   * For testing only.
   */
  public final List<Element> getElements() {
    return patternElements;
  }

  @Override
  public void reset() {
    // nothing
  }

}
