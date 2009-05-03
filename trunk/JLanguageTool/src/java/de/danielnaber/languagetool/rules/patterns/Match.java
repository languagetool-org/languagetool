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
import java.util.TreeSet;
import java.util.regex.Pattern;

import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.synthesis.Synthesizer;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * Reference to a matched token in a pattern, can be formatted and used for
 * matching & suggestions.
 * 
 * @author Marcin Miłkowski
 */
public class Match {

  /** Possible string case conversions. **/
  public enum CaseConversion {
    NONE, STARTLOWER, STARTUPPER, ALLLOWER, ALLUPPER;

    /**
     * Converts string to the constant enum.
     * 
     * @param str
     *          String value to be converted.
     * @return CaseConversion enum.
     */
    public static CaseConversion toCase(final String str) {
      try {
        return valueOf(str);
      } catch (final Exception ex) {
        return NONE;
      }
    }
  };

  private final String posTag;
  private boolean postagRegexp;
  private final String regexReplace;
  private final String posTagReplace;
  private final CaseConversion caseConversionType;

  /**
   * True if this match element formats a statically defined lemma which is
   * enclosed by the element, e.g., <tt>&lt;match...&gt;word&lt;/word&gt;</tt>.
   */
  private boolean staticLemma;

  /**
   * True if this match element is used for formatting POS token.
   */
  private final boolean setPos;

  private AnalyzedTokenReadings formattedToken;
  private AnalyzedTokenReadings matchedToken;

  private int tokenRef;

  /** Word form generator for POS tags. **/
  private Synthesizer synthesizer;

  /** Pattern used to define parts of the matched token. **/
  private Pattern pRegexMatch;

  /** Pattern used to define parts of the matched POS token. **/
  private Pattern pPosRegexMatch;

  /**
   * True when the match is not in the suggestion.
   */
  private boolean inMessageOnly;

  public Match(final String posTag, final String posTagReplace,
      final boolean postagRegexp, final String regexMatch,
      final String regexReplace, final CaseConversion caseConversionType,
      final boolean setPOS) {
    this.posTag = posTag;
    this.postagRegexp = postagRegexp;
    this.caseConversionType = caseConversionType;

    if (regexMatch != null) {
      pRegexMatch = Pattern.compile(regexMatch);
    }
    if (postagRegexp && posTag != null) {
      pPosRegexMatch = Pattern.compile(posTag);
    }

    this.regexReplace = regexReplace;
    this.posTagReplace = posTagReplace;
    setPos = setPOS;
  }

  /**
   * Sets the token that will be formatted or otherwise used in the class.
   * 
   * @param token
   * @AnalyzedTokenReadings
   * 
   */
  public final void setToken(final AnalyzedTokenReadings token) {
    if (staticLemma) {
      matchedToken = token;
    } else {
      formattedToken = token;
    }
  }

  /**
   * Checks if the Match element is used for setting the part of speech Element.
   * 
   * @return True if Match sets POS.
   */
  public final boolean setsPos() {
    return setPos;
  }

  /**
   * Checks if the Match element uses regexp-based form of the POS tag.
   * 
   * @return True if regexp is used in POS.
   */
  public final boolean posRegExp() {
    return postagRegexp;
  }

  /**
   * Sets a base form (lemma) that will be formatted, or synthesized, using the
   * specified POS regular expressions.
   * 
   * @param lemmaString
   * @String that specifies the base form.
   */
  public final void setLemmaString(final String lemmaString) {
    if (!StringTools.isEmpty(lemmaString)) {
      formattedToken = new AnalyzedTokenReadings(new AnalyzedToken(lemmaString,
          posTag, lemmaString));
      staticLemma = true;
      postagRegexp = true;
      if (postagRegexp && posTag != null) {
        pPosRegexMatch = Pattern.compile(posTag);
      }
    }
  }

  /**
   * Sets a synthesizer used for grammatical synthesis of forms based on
   * formatted POS values.
   * 
   * @param synth
   * @Synthesizer class.
   */
  public final void setSynthesizer(final Synthesizer synth) {
    synthesizer = synth;
  }

  /**
   * Gets all strings formatted using the match element.
   * 
   * @return
   * @String[] array of strings
   * @throws IOException
   *           in case of synthesizer-related disk problems.
   */
  public final String[] toFinalString() throws IOException {
    String[] formattedString = new String[1];
    if (formattedToken != null) {
      final int readingCount = formattedToken.getReadingsLength();
      formattedString[0] = formattedToken.getToken();
      if (pRegexMatch != null) {
        formattedString[0] = pRegexMatch.matcher(formattedString[0])
            .replaceAll(regexReplace);
      }
      formattedString[0] = convertCase(formattedString[0]);
      if (posTag != null) {
        if (synthesizer == null) {
          formattedString[0] = formattedToken.getToken();
        } else if (postagRegexp) {
          final TreeSet<String> wordForms = new TreeSet<String>();
          boolean oneForm = false;
          for (int k = 0; k < readingCount; k++) {
            if (formattedToken.getAnalyzedToken(k).getLemma() == null) {
              final String posUnique = formattedToken.getAnalyzedToken(k)
                  .getPOSTag();
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
                for (final String form : possibleWordForms) {
                  wordForms.add(form);
                }
              }
            }
          }
          if (wordForms.isEmpty()) {
            formattedString[0] = "(" + formattedToken.getToken() + ")";
          } else {
            formattedString = wordForms.toArray(new String[wordForms.size()]);
          }
        } else {
          final TreeSet<String> wordForms = new TreeSet<String>();
          for (int i = 0; i < readingCount; i++) {
            final String[] possibleWordForms = synthesizer.synthesize(
                formattedToken.getAnalyzedToken(i), posTag);
            if (possibleWordForms != null) {
              for (final String form : possibleWordForms) {
                wordForms.add(form);
              }
            }
          }
          formattedString = wordForms.toArray(new String[wordForms.size()]);
        }
      }
    }
    return formattedString;
  }

  /**
   * Format POS tag using parameters already defined in the class.
   * 
   * @return Formatted POS tag as
   * @String.
   */
  // FIXME: gets only the first POS tag that matches, this can be wrong
  // on the other hand, many POS tags = too many suggestions?
  public final String getTargetPosTag() {
    String targetPosTag = posTag;
    final List<String> posTags = new ArrayList<String>();
    if (staticLemma) {
      final int numRead = matchedToken.getReadingsLength();
      for (int i = 0; i < numRead; i++) {
        final String tst = matchedToken.getAnalyzedToken(i).getPOSTag();
        if (tst != null && pPosRegexMatch.matcher(tst).matches()) {
          targetPosTag = matchedToken.getAnalyzedToken(i).getPOSTag();
          posTags.add(targetPosTag);
        }
      }
      if (pPosRegexMatch != null && posTagReplace != null) {
        targetPosTag = pPosRegexMatch.matcher(targetPosTag).replaceAll(
            posTagReplace);
      }
    } else {
      final int numRead = formattedToken.getReadingsLength();
      for (int i = 0; i < numRead; i++) {
        final String tst = formattedToken.getAnalyzedToken(i).getPOSTag();
        if (tst != null && pPosRegexMatch.matcher(tst).matches()) {
          targetPosTag = formattedToken.getAnalyzedToken(i).getPOSTag();
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
        for (String lposTag : posTags) {
          l++;
          lposTag = pPosRegexMatch.matcher(lposTag).replaceAll(posTagReplace);
          if (setPos) {
            lposTag = synthesizer.getPosTagCorrection(lposTag);
          }
          sb.append(lposTag);
          if (l < posTagLen) {
            sb.append("|");
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
    final String[] stringToFormat = toFinalString();
    for (int i = 0; i < stringToFormat.length; i++) {
      output.append(stringToFormat[i]);
      if (i + 1 < stringToFormat.length) {
        output.append("|");
      }
    }
    return output.toString();
  }

  /**
   * Sets the token number referenced by the match.
   * 
   * @param i
   *          Token number.
   */
  public final void setTokenRef(final int i) {
    tokenRef = i;
  }

  /**
   * Gets the token number referenced by the match.
   * 
   * @return int - token number.
   */
  public final int getTokenRef() {
    return tokenRef;
  }

  /**
   * Converts case of the string token according to match element attributes.
   * 
   * @param s
   * @String Token to be converted.
   * @return @String Converted string.
   */
  private String convertCase(final String s) {
    String token = s;
    switch (caseConversionType) {
    case NONE:
      break;
    case STARTLOWER:
      token = token.substring(0, 1).toLowerCase() + token.substring(1);
      break;
    case STARTUPPER:
      token = token.substring(0, 1).toUpperCase() + token.substring(1);
      break;
    case ALLUPPER:
      token = token.toUpperCase();
      break;
    case ALLLOWER:
      token = token.toLowerCase();
      break;
    default:
      break;
    }
    return token;
  }

  /**
   * Used to let LT know that it should change the case of the match.
   * 
   * @return true if match converts the case of the token.
   */
  public final boolean convertsCase() {
    return caseConversionType.equals(CaseConversion.NONE) ^ true;
  }

  public final AnalyzedTokenReadings filterReadings() {
    final ArrayList<AnalyzedToken> l = new ArrayList<AnalyzedToken>();
    if (formattedToken != null) {
      if (staticLemma) {
        formattedToken = new AnalyzedTokenReadings(new AnalyzedToken(
            matchedToken.getToken(), posTag, formattedToken.getToken(),
            matchedToken.getStartPos()));
        formattedToken.setWhitespaceBefore(matchedToken.isWhitespaceBefore());
      }
      String token = formattedToken.getToken();
      if (pRegexMatch != null) {
        token = pRegexMatch.matcher(token).replaceAll(regexReplace);
      }
      token = convertCase(token);
      if (posTag != null) {
        final int numRead = formattedToken.getReadingsLength();
        if (postagRegexp) {
          String targetPosTag = posTag;
          for (int i = 0; i < numRead; i++) {
            final String tst = formattedToken.getAnalyzedToken(i).getPOSTag();
            if (tst != null && pPosRegexMatch.matcher(tst).matches()) {
              targetPosTag = formattedToken.getAnalyzedToken(i).getPOSTag();
              if (posTagReplace != null) {
                targetPosTag = pPosRegexMatch.matcher(targetPosTag).replaceAll(
                    posTagReplace);
              }
              l
                  .add(new AnalyzedToken(token, targetPosTag, formattedToken
                      .getAnalyzedToken(i).getLemma(), formattedToken
                      .getStartPos()));
              l.get(l.size() - 1).setWhitespaceBefore(formattedToken.isWhitespaceBefore());
            }
          }
          if (l.isEmpty()) {
            for (final AnalyzedToken anaTok : getNewToken(numRead, token)) {
              l.add(anaTok);              
            }
          }
        } else {
          for (final AnalyzedToken anaTok : getNewToken(numRead, token)) {
            l.add(anaTok);
          }
        }
      }
    }
    if (l.isEmpty()) {
      return formattedToken;
    }
    return new AnalyzedTokenReadings(l.toArray(new AnalyzedToken[l.size()]));
  }

  private AnalyzedToken[] getNewToken(final int numRead, final String token) {
    final List<AnalyzedToken> list = new ArrayList<AnalyzedToken>();
    String lemma = "";
    for (int j = 0; j < numRead; j++) {
      if (formattedToken.getAnalyzedToken(j).getPOSTag() != null) {
        if (formattedToken.getAnalyzedToken(j).getPOSTag().equals(posTag)
            && (formattedToken.getAnalyzedToken(j).getLemma() != null)) {
          lemma = formattedToken.getAnalyzedToken(j).getLemma();
        }
        if (StringTools.isEmpty(lemma)) {
          lemma = formattedToken.getAnalyzedToken(0).getLemma();
        }
        list.add(new AnalyzedToken(token, posTag, lemma, formattedToken
            .getStartPos()));
        list.get(list.size() - 1).
          setWhitespaceBefore(formattedToken.isWhitespaceBefore());
      }
    }
    return list.toArray(new AnalyzedToken[list.size()]);
  }

  /**
   * @param inMessageOnly
   *          the inMessageOnly to set
   */
  public void setInMessageOnly(final boolean inMessageOnly) {
    this.inMessageOnly = inMessageOnly;
  }

  /**
   * @return the inMessageOnly
   */
  public boolean isInMessageOnly() {
    return inMessageOnly;
  }

}
