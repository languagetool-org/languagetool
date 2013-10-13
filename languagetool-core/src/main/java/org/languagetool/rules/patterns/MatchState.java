/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
 * Copyright (C) 2013 Stefan Lotties
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
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.patterns.Match.IncludeRange;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tools.StringTools;

/**
 * The state of a matching process. This state is <strong>NOT</strong> thread-safe.
 *
 * @author Stefan Lotties
 * @since 2.3
 */
public class MatchState {

  private final Match match;
  private final Synthesizer synthesizer;

  private AnalyzedTokenReadings formattedToken;
  private AnalyzedTokenReadings matchedToken;
  private String skippedTokens;

  public MatchState(Match match, Synthesizer synthesizer) {
    this.match = match;
    this.synthesizer = synthesizer;
    final String lemma = match.getLemma();
    if (!StringUtils.isEmpty(lemma)) {
      formattedToken = new AnalyzedTokenReadings(new AnalyzedToken(lemma, match.getPosTag(), lemma), 0);
    }
  }

  public void setToken(AnalyzedTokenReadings token) {
    if (match.isStaticLemma()) {
      this.matchedToken = token;
    } else {
      this.formattedToken = token;
    }
  }

  /**
   * Sets the token to be formatted etc. and includes the support for
   * including the skipped tokens.
   *
   * @param tokens Array of tokens
   * @param index Index of the token to be formatted
   * @param next Position of the next token (the skipped tokens are the ones between the tokens[index] and tokens[next]
   */
  public final void setToken(final AnalyzedTokenReadings[] tokens, final int index, final int next) {
    int idx = index;
    if (index >= tokens.length) {
      // TODO: hacky workaround, find a proper solution. See EnglishPatternRuleTest.testBug()
      idx = tokens.length - 1;
    }
    setToken(tokens[idx]);
    IncludeRange includeSkipped = match.getIncludeSkipped();
    if (next > 1 && includeSkipped != IncludeRange.NONE) {
      final StringBuilder sb = new StringBuilder();
      if (includeSkipped == IncludeRange.FOLLOWING) {
        formattedToken = null;
      }
      for (int k = index + 1; k < index + next; k++) {
        if (tokens[k].isWhitespaceBefore()
            && !(k == index + 1 && includeSkipped == IncludeRange.FOLLOWING)) {
          sb.append(' ');
        }
        sb.append(tokens[k].getToken());
      }
      skippedTokens = sb.toString();
    } else {
      skippedTokens = "";
    }
  }

  public final AnalyzedTokenReadings filterReadings() {
    final ArrayList<AnalyzedToken> l = new ArrayList<>();
    if (formattedToken != null) {
      if (match.isStaticLemma()) {
        matchedToken.leaveReading(new AnalyzedToken(matchedToken
            .getToken(), match.getPosTag(), formattedToken.getToken()));
        formattedToken = matchedToken;
      }
      String token = formattedToken.getToken();
      Pattern regexMatch = match.getRegexMatch();
      String regexReplace = match.getRegexReplace();
      if (regexMatch != null && regexReplace != null) {
        /* only replace if it is something to replace */
        token = regexMatch.matcher(token).replaceAll(regexReplace);
      }
      token = convertCase(token, token);

      String posTag = match.getPosTag();
      if (posTag != null) {
        final int numRead = formattedToken.getReadingsLength();
        if (match.isPostagRegexp()) {
          Pattern pPosRegexMatch = match.getPosRegexMatch();
          String posTagReplace = match.getPosTagReplace();
          String targetPosTag;
          for (int i = 0; i < numRead; i++) {
            final String tst = formattedToken.getAnalyzedToken(i).getPOSTag();
            if (tst != null && pPosRegexMatch.matcher(tst).matches()) {
              targetPosTag = formattedToken.getAnalyzedToken(i).getPOSTag();
              if (posTagReplace != null) {
                targetPosTag = pPosRegexMatch.matcher(targetPosTag).replaceAll(posTagReplace);
              }
              l.add(new AnalyzedToken(token, targetPosTag,
                  formattedToken.getAnalyzedToken(i).getLemma()));
              l.get(l.size() - 1).setWhitespaceBefore(formattedToken.isWhitespaceBefore());
            }
          }
          if (l.isEmpty()) {
            l.addAll(getNewToken(numRead, token));
          }
        } else {
          l.addAll(getNewToken(numRead, token));
        }
        if (formattedToken.isSentenceEnd()) {
          l.add(new AnalyzedToken(formattedToken.getToken(),
              JLanguageTool.SENTENCE_END_TAGNAME, formattedToken.getAnalyzedToken(0).getLemma()));
        }
        if (formattedToken.isParagraphEnd()) {
          l.add(new AnalyzedToken(formattedToken.getToken(),
              JLanguageTool.PARAGRAPH_END_TAGNAME, formattedToken.getAnalyzedToken(0).getLemma()));
        }

      }
    }
    if (l.isEmpty()) {
      return formattedToken;
    }
    final AnalyzedTokenReadings anTkRead = new AnalyzedTokenReadings(
        l.toArray(new AnalyzedToken[l.size()]),
        formattedToken.getStartPos());
    anTkRead.setWhitespaceBefore(formattedToken.isWhitespaceBefore());
    if (!formattedToken.getChunkTags().isEmpty()) {
      anTkRead.setChunkTags(formattedToken.getChunkTags());
    }
    return anTkRead;
  }

  /**
   * Converts case of the string token according to match element attributes.
   *
   * @param s Token to be converted.
   * @param sample the sample string used to determine how the original string looks like (used on case preservation)
   * @return Converted string.
   */
  private String convertCase(final String s, String sample) {
    if (StringTools.isEmpty(s)) {
      return s;
    }
    String token = s;
    switch (match.getCaseConversionType()) {
    case NONE:
      break;
    case PRESERVE:
      if (StringTools.startsWithUppercase(sample)) {
        if (StringTools.isAllUppercase(sample)) {
          token = token.toUpperCase(Locale.ENGLISH);
        } else {
          token = StringTools.uppercaseFirstChar(token);
        }
      }
      break;
    case STARTLOWER:
      token = token.substring(0, 1).toLowerCase() + token.substring(1);
      break;
    case STARTUPPER:
      token = token.substring(0, 1).toUpperCase(Locale.ENGLISH) + token.substring(1);
      break;
    case ALLUPPER:
      token = token.toUpperCase(Locale.ENGLISH);
      break;
    case ALLLOWER:
      token = token.toLowerCase();
      break;
    default:
      break;
    }
    return token;
  }

  private List<AnalyzedToken> getNewToken(final int numRead, final String token) {
    String posTag = match.getPosTag();
    final List<AnalyzedToken> list = new ArrayList<>();
    String lemma = "";
    for (int j = 0; j < numRead; j++) {
      if (formattedToken.getAnalyzedToken(j).getPOSTag() != null) {
        if (formattedToken.getAnalyzedToken(j).getPOSTag().equals(posTag)
            && formattedToken.getAnalyzedToken(j).getLemma() != null) {
          lemma = formattedToken.getAnalyzedToken(j).getLemma();
        }
        if (StringTools.isEmpty(lemma)) {
          lemma = formattedToken.getAnalyzedToken(0).getLemma();
        }
        list.add(new AnalyzedToken(token, posTag, lemma));
        list.get(list.size() - 1).setWhitespaceBefore(
            formattedToken.isWhitespaceBefore());
      }
    }
    return list;
  }

  /**
   * Gets all strings formatted using the match element.
   *
   * @return array of strings
   * @throws IOException in case of synthesizer-related I/O problems
   */
  public final String[] toFinalString(Language lang) throws IOException {
    String[] formattedString = new String[1];
    if (formattedToken != null) {
      final int readingCount = formattedToken.getReadingsLength();
      formattedString[0] = formattedToken.getToken();

      Pattern pRegexMatch = match.getRegexMatch();
      String regexReplace = match.getRegexReplace();
      if (pRegexMatch != null) {
        formattedString[0] = pRegexMatch.matcher(formattedString[0]).replaceAll(regexReplace);
      }

      String posTag = match.getPosTag();
      if (posTag != null) {
        if (synthesizer == null) {
          formattedString[0] = formattedToken.getToken();
        } else if (match.isPostagRegexp()) {
          final TreeSet<String> wordForms = new TreeSet<>();
          boolean oneForm = false;
          for (int k = 0; k < readingCount; k++) {
            if (formattedToken.getAnalyzedToken(k).getLemma() == null) {
              final String posUnique = formattedToken
                  .getAnalyzedToken(k).getPOSTag();
              if (posUnique == null) {
                wordForms.add(formattedToken.getToken());
                oneForm = true;
              } else {
                if (JLanguageTool.SENTENCE_START_TAGNAME.equals(posUnique)
                    || JLanguageTool.SENTENCE_END_TAGNAME.equals(posUnique)
                    || JLanguageTool.PARAGRAPH_END_TAGNAME.equals(posUnique)) {
                  if (!oneForm) {
                    wordForms.add(formattedToken.getToken());
                  }
                  oneForm = true;
                } else {
                  oneForm = false;
                }
              }
            }
          }
          final String targetPosTag = getTargetPosTag();
          if (!oneForm) {
            for (int i = 0; i < readingCount; i++) {
              final String[] possibleWordForms = synthesizer.synthesize(
                  formattedToken.getAnalyzedToken(i), targetPosTag, true);
              if (possibleWordForms != null) {
                wordForms.addAll(Arrays.asList(possibleWordForms));
              }
            }
          }
          if (wordForms.isEmpty()) {
            if (match.checksSpelling()) {
              formattedString[0] = "";
            } else {
              formattedString[0] = "(" + formattedToken.getToken() + ")";
            }
          } else {
            formattedString = wordForms.toArray(new String[wordForms.size()]);
          }
        } else {
          final TreeSet<String> wordForms = new TreeSet<>();
          for (int i = 0; i < readingCount; i++) {
            final String[] possibleWordForms = synthesizer
                .synthesize(formattedToken.getAnalyzedToken(i), posTag);
            if (possibleWordForms != null) {
              wordForms.addAll(Arrays.asList(possibleWordForms));
            }
          }
          formattedString = wordForms.toArray(new String[wordForms.size()]);
        }
      }
    }
    final String original;
    if (match.isStaticLemma()) {
      original = matchedToken != null ? matchedToken.getToken() : "";
    } else {
      original = formattedToken != null ? formattedToken.getToken() : "";
    }
    for (int i = 0; i < formattedString.length; i++) {
      formattedString[i] = convertCase(formattedString[i], original);
    }
    // TODO should case conversion happen before or after including skipped tokens?
    IncludeRange includeSkipped = match.getIncludeSkipped();
    if (includeSkipped != IncludeRange.NONE && skippedTokens != null
        && !"".equals(skippedTokens)) {
      final String[] helper = new String[formattedString.length];
      for (int i = 0; i < formattedString.length; i++) {
        if (formattedString[i] == null) {
          formattedString[i] = "";
        }
        helper[i] = formattedString[i] + skippedTokens;
      }
      formattedString = helper;
    }
    if (match.checksSpelling() && lang != null) {
      final List<String> formattedStringElements = Arrays
          .asList(formattedString);
      // tagger-based speller
      final List<AnalyzedTokenReadings> analyzed = lang.getTagger().tag(
          formattedStringElements);
      for (int i = 0; i < formattedString.length; i++) {
        final AnalyzedToken analyzedToken = analyzed.get(i)
            .getAnalyzedToken(0);
        if (analyzedToken.getLemma() == null
            && analyzedToken.hasNoTag()) {
          formattedString[i] = "";
        }
      }
    }
    return formattedString;
  }

  /**
   * Format POS tag using parameters already defined in the class.
   *
   * @return Formatted POS tag as String.
   */
  // FIXME: gets only the first POS tag that matches, this can be wrong
  // on the other hand, many POS tags = too many suggestions?
  public final String getTargetPosTag() {
    String targetPosTag = match.getPosTag();
    final List<String> posTags = new ArrayList<>();
    Pattern pPosRegexMatch = match.getPosRegexMatch();
    String posTagReplace = match.getPosTagReplace();

    if (match.isStaticLemma()) {
      for (AnalyzedToken analyzedToken : matchedToken) {
        final String tst = analyzedToken.getPOSTag();
        if (tst != null && pPosRegexMatch.matcher(tst).matches()) {
          targetPosTag = analyzedToken.getPOSTag();
          posTags.add(targetPosTag);
        }
      }

      if (pPosRegexMatch != null && posTagReplace != null) {
        targetPosTag = pPosRegexMatch.matcher(targetPosTag).replaceAll(
            posTagReplace);
      }
    } else {
      for (AnalyzedToken analyzedToken : formattedToken) {
        final String tst = analyzedToken.getPOSTag();
        if (tst != null && pPosRegexMatch.matcher(tst).matches()) {
          targetPosTag = analyzedToken.getPOSTag();
          posTags.add(targetPosTag);
        }
      }
      if (pPosRegexMatch != null && posTagReplace != null) {
        if (posTags.isEmpty()) {
          posTags.add(targetPosTag);
        }
        final StringBuilder sb = new StringBuilder();
        final int posTagLen = posTags.size();
        int l = 0;
        for (String lPosTag : posTags) {
          l++;
          lPosTag = pPosRegexMatch.matcher(lPosTag).replaceAll(
              posTagReplace);
          if (match.setsPos()) {
            lPosTag = synthesizer.getPosTagCorrection(lPosTag);
          }
          sb.append(lPosTag);
          if (l < posTagLen) {
            sb.append('|');
          }
        }
        targetPosTag = sb.toString();
      }
    }
    return targetPosTag;
  }

  /**
   * Method for getting the formatted match as a single string. In case of
   * multiple matches, it joins them using a regular expression operator "|".
   *
   * @return Formatted string of the matched token.
   */
  public final String toTokenString() throws IOException {
    final StringBuilder output = new StringBuilder();
    final String[] stringToFormat = toFinalString(null);
    for (int i = 0; i < stringToFormat.length; i++) {
      output.append(stringToFormat[i]);
      if (i + 1 < stringToFormat.length) {
        output.append('|');
      }
    }
    return output.toString();
  }

  public Match getMatch() {
    return match;
  }
}
