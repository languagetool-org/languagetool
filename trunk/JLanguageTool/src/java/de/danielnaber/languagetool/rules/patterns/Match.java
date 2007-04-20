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
import java.util.regex.Pattern;

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
        final CaseConversion caseConversionType) {
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
  
  public String toString() {
    String formattedString = "";
    if (formattedToken != null) {
      if (posTag == null) {
        if (pRegexMatch == null) {          
          switch (caseConversionType) {
            default : formattedString = formattedToken.getToken(); break;
            case NONE : formattedString = formattedToken.getToken(); break;
            case STARTLOWER : formattedString = formattedToken.getToken().
                    substring(0, 1).toLowerCase() 
                    + formattedToken.getToken().substring(1); break;
            case STARTUPPER : formattedString = formattedToken.getToken().
                  substring(0, 1).toUpperCase() 
                  + formattedToken.getToken().substring(1); break;
            case ALLUPPER : formattedString = formattedToken.getToken().
                  toUpperCase(); break;
            case ALLLOWER : formattedString = formattedToken.getToken().
                  toLowerCase(); break;              
          }
            
        } else {
        formattedString 
        = pRegexMatch.matcher(formattedToken
              .getToken()).replaceAll(regexReplace);
        }
      } else {
//FIXME: dummy implementation        
        formattedString 
          = pRegexMatch.matcher(formattedToken
              .getToken()).replaceAll(regexReplace);          
      }
    }
    return formattedString;
  }
}
