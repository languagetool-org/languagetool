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

import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.synthesis.Synthesizer;
import java.util.regex.Pattern;
import java.util.TreeSet;
import java.io.IOException;

/**
 * Reference to a matched token in a pattern,
 * can be formatted and used for matching & suggestions.
 * 
 * @author Marcin Miłkowski
 */
public class Match {

  /** Possible string case conversions. **/
  public enum CaseConversion { 
    NONE, STARTLOWER, STARTUPPER, ALLLOWER, ALLUPPER; 
  
    /** Converts string to the constant enum.
     * @param str String value to be converted.
     * @return CaseConversion enum.
     */
    public static CaseConversion toCase(final String str) {    
      try {
          return valueOf(str);
          } catch (Exception ex) {
          return NONE;
         }  
    }
  };
  
  private String posTag = null;
  private boolean postagRegexp = false;
  private String regexReplace;
  private CaseConversion caseConversionType;
  
  private AnalyzedTokenReadings formattedToken;
  
  /** Word form generator for POS tags **/
  private Synthesizer synthesizer;
  
  /** Pattern used to define parts of the matched token. **/
  private Pattern pRegexMatch = null;  
  
  /** Pattern used to define parts of the matched POS token. **/
  private Pattern pPosRegexMatch = null;
  
  /** Pattern used to define parts of the generated token with a 
   * given POS tag. **/
  private Pattern pPosFindRegexMatch = null;
   
  
  Match(final String regMatch, final String regReplace, 
      final CaseConversion caseConvType) {
    this(null, false, regMatch, regReplace, caseConvType);
  }
  
  Match(final String posTag, final boolean postagRegexp, 
        final String regexMatch,
        final String regexReplace,
        final CaseConversion caseConversionType)  {
  this.posTag = posTag;
  this.postagRegexp = postagRegexp;
  this.caseConversionType = caseConversionType;
  
  if (posTag == null) {
    if (regexMatch != null) {
    pRegexMatch = Pattern.compile(regexMatch);
    }
  } else {
    if (regexMatch != null) { 
    pPosRegexMatch = Pattern.compile(regexMatch);
    }
  }
  
  if (postagRegexp) {
    pPosFindRegexMatch = Pattern.compile(posTag);
  }
  
  this.regexReplace = regexReplace;  
  
  }
  
  public void setToken (final AnalyzedTokenReadings token) {
    formattedToken = token;
  }
  
  public void setSynthesizer(final Synthesizer synth) throws IOException {
    synthesizer = synth;
  }    
  
  public String[] toFinalString() throws IOException {
    String[] formattedString = new String[1];
    if (formattedToken != null) {
      if (posTag == null) {
        if (pRegexMatch == null) {          
          switch (caseConversionType) {
            default : formattedString[0] = formattedToken.getToken(); break;
            case NONE : formattedString[0] = formattedToken.getToken(); break;
            case STARTLOWER : formattedString[0] = formattedToken.getToken().
                    substring(0, 1).toLowerCase() 
                    + formattedToken.getToken().substring(1); break;
            case STARTUPPER : formattedString[0] = formattedToken.getToken().
                  substring(0, 1).toUpperCase() 
                  + formattedToken.getToken().substring(1); break;
            case ALLUPPER : formattedString[0] = formattedToken.getToken().
                  toUpperCase(); break;
            case ALLLOWER : formattedString[0] = formattedToken.getToken().
                  toLowerCase(); break;              
          }         
        } else {
        formattedString[0] 
        = pRegexMatch.matcher(formattedToken
              .getToken()).replaceAll(regexReplace);
        }
      } else {
//TODO: add POS regexp mechanisms
        if (synthesizer == null) {
        formattedString[0] = formattedToken.getToken();
          //= pRegexMatch.matcher(formattedToken
            //  .getToken()).replaceAll(regexReplace);
        } else {
          int readingCount = formattedToken.getReadingsLength();
          TreeSet<String> wordForms = new TreeSet<String>();
          for (int i = 0; i < readingCount; i++) {
                String[] possibleWordForms = 
                  synthesizer.synthesize(
                    formattedToken.getAnalyzedToken(i).getLemma(),
                    posTag);
                if (possibleWordForms != null) {
                  for (String form : possibleWordForms) {           
                    wordForms.add(form);
                  }
                }
            }
          if (wordForms != null) {
            formattedString = wordForms.toArray(new String[wordForms.size()]);
          } else {
            formattedString[0] = formattedToken.getToken();
          }
        }
      }
    }
    return formattedString;
  }
}
