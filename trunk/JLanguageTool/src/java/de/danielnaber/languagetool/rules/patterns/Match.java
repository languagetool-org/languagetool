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
import de.danielnaber.languagetool.AnalyzedToken;
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
  private String posTagReplace;
  private CaseConversion caseConversionType;
  private boolean staticLemma = false;
  
  private AnalyzedTokenReadings formattedToken;  
  private AnalyzedTokenReadings matchedToken;
  
  /** Word form generator for POS tags. **/
  private Synthesizer synthesizer;
  
  /** Pattern used to define parts of the matched token. **/
  private Pattern pRegexMatch = null;  
  
  /** Pattern used to define parts of the matched POS token. **/
  private Pattern pPosRegexMatch = null;     
  
  Match(final String regMatch, final String regReplace, 
      final CaseConversion caseConvType) {
    this(null, null, false, regMatch, regReplace, caseConvType);
  }
  
  Match(final String posTag, final String posTagReplace,
      final boolean postagRegexp,      
      final String regexMatch,
      final String regexReplace,      
      final CaseConversion caseConversionType)  {
    this.posTag = posTag;
    this.postagRegexp = postagRegexp;
    this.caseConversionType = caseConversionType;

    if (regexMatch != null) {
      pRegexMatch = Pattern.compile(regexMatch);
    }
    if (postagRegexp & posTag != null) {
      pPosRegexMatch = Pattern.compile(posTag);
    }

    this.regexReplace = regexReplace;  
    this.posTagReplace = posTagReplace;
  }
  
  public void setToken(final AnalyzedTokenReadings token) {
    if (!staticLemma) {
      formattedToken = token;
    } else {
      matchedToken = token;
    }
  }
  
  public void setLemmaString(final String lemmaString) {
    if (lemmaString != null) {
      if (!lemmaString.equals("")) {
        formattedToken = new AnalyzedTokenReadings(new AnalyzedToken(lemmaString, null, lemmaString));
        staticLemma = true;
        postagRegexp = true;
        if (postagRegexp & posTag != null) {
          pPosRegexMatch = Pattern.compile(posTag);
        }
      }
    }
  }
  
  public void setSynthesizer(final Synthesizer synth) throws IOException {
    synthesizer = synth;
  }    
  
  public String[] toFinalString() throws IOException {
    String[] formattedString = new String[1];
    if (formattedToken != null) {
      if (posTag == null) {
        formattedString[0] = formattedToken.getToken();
        if (pRegexMatch != null) {          
          formattedString[0] 
          = pRegexMatch.matcher(formattedString[0]).replaceAll(regexReplace);
          }        
          switch (caseConversionType) {
            default : formattedString[0] = formattedString[0]; break;
            case NONE : formattedString[0] = formattedString[0]; break;
            case STARTLOWER : formattedString[0] = formattedString[0].
                    substring(0, 1).toLowerCase() 
                    + formattedToken.getToken().substring(1); break;
            case STARTUPPER : formattedString[0] = formattedString[0].
                  substring(0, 1).toUpperCase() 
                  + formattedToken.getToken().substring(1); break;
            case ALLUPPER : formattedString[0] = formattedString[0].
                  toUpperCase(); break;
            case ALLLOWER : formattedString[0] = formattedString[0].
                  toLowerCase(); break;              
          }         
        
      } else {
//TODO: add POS regexp replace mechanisms
        if (synthesizer == null) {
        formattedString[0] = formattedToken.getToken();
        } else if (postagRegexp) {
          int readingCount = formattedToken.getReadingsLength();
          String targetPosTag = posTag;
          if (staticLemma) {
            int numRead = matchedToken.getReadingsLength();
            for (int i = 0; i < numRead; i++) {
              String tst = matchedToken.getAnalyzedToken(i).getPOSTag();
              if (tst != null) {
              if (pPosRegexMatch.matcher(tst).matches()) {
                targetPosTag = matchedToken.getAnalyzedToken(i).getPOSTag();
                break;
              }
              }
            }            
            if (pPosRegexMatch != null & posTagReplace != null) {            
              targetPosTag = pPosRegexMatch.matcher(targetPosTag).replaceAll(posTagReplace);  
            }
            if (targetPosTag.indexOf("?") > 0) {
              targetPosTag = targetPosTag.replaceAll("\\?", "\\\\?");
              }
          } else {
          if (pPosRegexMatch != null & posTagReplace != null) {            
            targetPosTag = pPosRegexMatch.matcher(posTag).replaceAll(posTagReplace);  
          }
          }
          TreeSet<String> wordForms = new TreeSet<String>();          
          for (int i = 0; i < readingCount; i++) {
                String[] possibleWordForms = 
                  synthesizer.synthesize(
                    formattedToken.getAnalyzedToken(i),
                    targetPosTag, true);
                if (possibleWordForms != null) {
                  for (String form : possibleWordForms) {           
                    wordForms.add(form);
                  }
                }
            }
          if (wordForms != null) {
            if (wordForms.size() > 0) {
            formattedString = wordForms.toArray(new String[wordForms.size()]);
            } else {
            formattedString[0] = "(" + formattedToken.getToken() + ")";            
            }
          } else {
            formattedString[0] = formattedToken.getToken();
          }
        } else {
          int readingCount = formattedToken.getReadingsLength();
          TreeSet<String> wordForms = new TreeSet<String>();
          for (int i = 0; i < readingCount; i++) {
                String[] possibleWordForms = 
                  synthesizer.synthesize(
                    formattedToken.getAnalyzedToken(i),
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
