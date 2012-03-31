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
package org.languagetool.tokenizers.ca;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.languagetool.tokenizers.Tokenizer;


/**
 * Tokenizes a sentence into words. Punctuation and whitespace gets its own token.
 * Special treatment for hyphens and apostrophes in Catalan.
 *
 * @author Jaume Ortolà 
 */
public class CatalanWordTokenizer implements Tokenizer {
  
  //all possible forms of "pronoms febles" after a verb.
  private static final String PF = "('en|'hi|'ho|'l|'ls|'m|'n|'ns|'s|'t|-el|-els|-em|-en|-ens|-hi|-ho|-l|-la|-les|-li|-lo|-los|-m|-me|-n|-ne|-nos|-s|-se|-t|-te|-us|-vos)";
     
   
  public CatalanWordTokenizer() {
  }

  /**
   * @param text Text to tokenize
   * @return List of tokens.
   *         Note: a special string ##CA_APOS## is used to replace apostrophes
   *         during tokenizing (as in Dutch).
   */
  @Override
  public List<String> tokenize(final String text) {
    final List<String> l = new ArrayList<String>();
    final StringTokenizer st = new StringTokenizer(text.replaceAll("([\\p{L}])['’]([\\p{L}])", "$1##CA_APOS##$2"),
            "\u0020\u00A0\u115f\u1160\u1680" 
            + "\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007"
            + "\u2008\u2009\u200A\u200B\u200c\u200d\u200e\u200f"
            + "\u2013\u2014\u2015"
            + "\u2028\u2029\u202a\u202b\u202c\u202d\u202e\u202f"
            + "\u205F\u2060\u2061\u2062\u2063\u206A\u206b\u206c\u206d"
            + "\u206E\u206F\u3000\u3164\ufeff\uffa0\ufff9\ufffa\ufffb"
            + ",.;()[]{}<>!?:/\\\"'«»„”“‘’`´…¿¡\t\n\r", true);
    String s;
    String groupStr;
    
    // Apostrophe at the beginning of a word. Ex.: l'home, s'estima, n'omple, hivern, etc.
		// It creates 2 tokens: <token>l'</token><token>home</token>
    Pattern pattern1 = Pattern.compile("^([lnmtsd]')([^'\\-]*)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
    
    // Exceptions to (Match verb+1 pronom feble)
    // It creates 1 token: <token>qui-sap-lo</token>
    Pattern pattern2 = Pattern.compile("^(qui-sap-lo|qui-sap-la|qui-sap-los|qui-sap-les)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
    
    // Match verb+3 pronoms febles (rare but possible!). Ex: Emporta-te'ls-hi.
    // It creates 4 tokens: <token>Emporta</token><token>-te</token><token>'ls</token><token>-hi</token>
    Pattern pattern3 = Pattern.compile("^([lnmtsd]')(.*)"+PF+PF+PF+"$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
    Pattern pattern3b = Pattern.compile("^(.*)"+PF+PF+PF+"$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
    
    // Match verb+2 pronoms febles. Ex: Emporta-te'ls. 
    // It creates 3 tokens: <token>Emporta</token><token>-te</token><token>'ls</token>
    Pattern pattern4 = Pattern.compile("^([lnmtsd]')(.*)"+PF+PF+"$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
    Pattern pattern4b = Pattern.compile("^(.*)"+PF+PF+"$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
    
    // match verb+1 pronom feble. Ex: Emporta't, vés-hi, porta'm.
    // It creates 2 tokens: <token>Emporta</token><token>'t</token>
    Pattern pattern5 = Pattern.compile("^([lnmtsd]')(.*)"+PF+"$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
    Pattern pattern5b = Pattern.compile("^(.*)"+PF+"$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
    
    // d'emportar
    Pattern pattern6 = Pattern.compile("^([lnmtsd]')(.*)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
    
    while (st.hasMoreElements()) {
    	s=st.nextToken().replace("##CA_APOS##", "'");
			
      Matcher matcher = pattern1.matcher(s); 
      boolean matchFound = matcher.find();
			
			if (!matchFound){
      	matcher = pattern2.matcher(s); 
	      matchFound = matcher.find();
      } 
      
      if (!matchFound){
	      matcher = pattern3.matcher(s); 
	      matchFound = matcher.find();
      }
      if (!matchFound){
	      matcher = pattern3b.matcher(s); 
	      matchFound = matcher.find();
      }  
      
      if (!matchFound){
	      matcher = pattern4.matcher(s); 
	      matchFound = matcher.find();
      }
      if (!matchFound){
	      matcher = pattern4b.matcher(s); 
	      matchFound = matcher.find();
      }  
      
      if(!matchFound){
	      matcher = pattern5.matcher(s); 
	      matchFound = matcher.find();
      }
      if(!matchFound){
	      matcher = pattern5b.matcher(s); 
	      matchFound = matcher.find();
      }
      
      if(!matchFound){
	      matcher = pattern6.matcher(s); 
	      matchFound = matcher.find();
      }
        
      if (matchFound) {
			  for (int i=1; i<=matcher.groupCount(); i++) {
			    groupStr = matcher.group(i);
			    l.add(groupStr);
			  }
			}  
      else {
       l.add(s);
      }
    	   
    }
    return l;
  }

}
