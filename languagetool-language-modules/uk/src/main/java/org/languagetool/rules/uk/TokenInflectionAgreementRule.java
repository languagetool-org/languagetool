/* LanguageTool, a natural language style checker 
 * Copyright (C) 2013 Andriy Rysin
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
package org.languagetool.rules.uk;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tagging.uk.PosTagHelper;

/**
 * A rule that checks if tokens in the sentence agree on inflection etc
 * 
 * @author Andriy Rysin
 */
public class TokenInflectionAgreementRule extends Rule {
  private static final List<String> CONJ_FOR_PLURAL = Arrays.asList("і", "й", "та", "чи", "або");
  private static final String NO_VIDMINOK_SUBSTR = ":nv";
  private static final Pattern ADJ_INFLECTION_PATTERN = Pattern.compile(":([mfnp]):(v_...)(:r(in)?anim)?");
  private static final Pattern NOUN_INFLECTION_PATTERN = Pattern.compile(":((?:[iu]n)?anim):([mfnp]):(v_...)");

  private static final Map<String, Set<String>> inflectionControl = loadMap("/uk/case_government.txt");
  
  
  public TokenInflectionAgreementRule(ResourceBundle messages) throws IOException {
    super.setCategory(Categories.MISC.getCategory(messages));
    setDefaultOff();
  }

  @Override
  public final String getId() {
    return "UK_ADJ_NOUN_INFLECTION_AGREEMENT";
  }

  @Override
  public String getDescription() {
    return "Узгодження відмінків/числа/роду прикметників та іменників";
  }

  public String getShort() {
    return "Узгодження прикметників та іменників";
  }
  /**
   * Indicates if the rule is case-sensitive. 
   * @return true if the rule is case-sensitive, false otherwise.
   */
  public boolean isCaseSensitive() {
    return false;
  }

  @Override
  public final RuleMatch[] match(AnalyzedSentence text) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();    

    ArrayList<AnalyzedToken> adjTokenReadings = new ArrayList<>(); 
    AnalyzedTokenReadings adjAnalyzedTokenReadins = null;
    boolean adjpPresent = false;
    boolean numrPresent = false;
    boolean pronPresent = false;

    for (int i = 0; i < tokens.length; i++) {
      AnalyzedTokenReadings tokenReadings = tokens[i];

      String posTag = tokenReadings.getAnalyzedToken(0).getPOSTag();

      //TODO: skip conj напр. «бодай»

      if (posTag == null
          || posTag.equals(JLanguageTool.SENTENCE_START_TAGNAME) ){
      	adjTokenReadings.clear();
        continue;
      }

      
      // grab initial adjective inflections
      
      if( adjTokenReadings.isEmpty() ) {
        if( i == tokens.length - 1 ) // no need to start checking on last token
          continue;

        adjpPresent = false;
        numrPresent = false;
        pronPresent = false;
        
        for (AnalyzedToken token: tokenReadings) {
          String adjPosTag = token.getPOSTag();
          
          if( adjPosTag == null ) {
            System.err.println("Null tag: " + tokenReadings);
            continue;
          }
          
          if( adjPosTag.startsWith("adj") && ! adjPosTag.contains(NO_VIDMINOK_SUBSTR) ) {		//TODO: nv still can be wrong if :np/:ns is present
            adjTokenReadings.add(token);
            adjAnalyzedTokenReadins = tokenReadings;

            if( adjPosTag.contains("&pron") ) {
              pronPresent = true;
//              adjTokenReadings.clear();
//              break;
            }
            
            if( adjPosTag.startsWith("<") ) {
            	adjTokenReadings.clear();
            	break;
            }
            
            // кількох десятих відсотка
            if( Arrays.asList("десятий", "сотий", "тисячний", "мільйонний", "мільярдний").contains(token.getLemma().toLowerCase())
                  && adjPosTag.matches(".*:[fp]:.*") ) {
            	adjTokenReadings.clear();
            	break;
            }

            if( i>1 && Arrays.asList("який", "котрий").contains(token.getLemma()) 
            		&& reverseConjFind(tokens, i-1, 3) ) {
            	adjTokenReadings.clear();
            	break;
            }
            
            // 33 народних обранці
            if( i>1 && adjPosTag.contains(":p:v_rod") 
            		&& tokens[i-1].getAnalyzedToken(0).getLemma() != null
            		&& tokens[i-1].getAnalyzedToken(0).getLemma().matches(".*[2-4]|два|три|чотири") 
            		&& PosTagHelper.hasPosTag(tokens[i+1], ".*:p:v_naz.*")) {
            	adjTokenReadings.clear();
            	break;
            }

            // Водночас сам Рибалко
            if( Arrays.asList("сам", "саме").contains(token.getToken().toLowerCase()) ) {
            	adjTokenReadings.clear();
            	break;
            }
            
            if( i > 0 && PosTagHelper.hasPosTag(tokens[i-1], "(</)?prep.*") ) {
            	if( Arrays.asList("який", "якийсь", "такий", "котрий", "увесь", "весь", "останній").contains(token.getLemma()) ) {
            		Collection<String> governedCases = getPrepGovernedCases(tokens[i-1]);
            		boolean boo = false;
            		for (String governedCase : governedCases) {
              		if( token.getPOSTag().contains(governedCase) ) {
              			boo = true;
              			break;
              		}
            		}
            		if( boo ) { 
                	adjTokenReadings.clear();
                	break;
            		}
            	}
            }

            if( adjPosTag.contains("adjp:pasv") ) { // could be :&adjp or :&_adjp
              adjpPresent = true;
            }
            
            if( adjPosTag.contains(":&numr") ) {
              // Ставши 2003-го прем’єром
            	if( token.getLemma().matches("([12][0-9])?[0-9][0-9]-.*") ) {
            		adjTokenReadings.clear();
            		break;
            	}
            	if( i > 0 && Arrays.asList("на", "в", "у", "за", "о").contains(tokens[i-1].getAnalyzedToken(0).getLemma()) ) {
            		adjTokenReadings.clear();
            		break;
            	}
            
              numrPresent = true;
            }
            
            if( i > 0 && adjPosTag.contains(":v_oru") && hasButyLemma(tokens[i-1]) ) {
              	adjTokenReadings.clear();
              	break;
            }

          }
          else if ( adjPosTag.equals(JLanguageTool.SENTENCE_END_TAGNAME) ) {
            continue;
          }
          else {
            adjTokenReadings.clear();
            break;
          }
        }
        
        continue;
      }
      
      
      // see if we can check
//      System.out.println("Check for noun: " + tokenReadings);
            
      ArrayList<AnalyzedToken> slaveTokenReadings = new ArrayList<>(); 
      for (AnalyzedToken token: tokenReadings) {
         String posTag2 = token.getPOSTag();

         if( posTag2 == null ) {
           System.err.println("Null tag: " + tokenReadings);
           continue;
         }
         
         if( posTag2.startsWith("noun") && ! posTag2.contains(NO_VIDMINOK_SUBSTR) ) {
           
           //TODO: temp: ignore &pron
           if( posTag2.contains("&pron") ) {
          	 slaveTokenReadings.clear();
             break;
           }

           if( token.getToken().equals("ім.") ) {
           	adjTokenReadings.clear();
           	break;
           }

           if( token.getToken().equals("віком") && adjAnalyzedTokenReadins.getAnalyzedToken(0).getToken().matches(".*старший|.*молодший") ) {
            	adjTokenReadings.clear();
            	break;
            }

           if( posTag2.startsWith("</") ) {
           	adjTokenReadings.clear();
           	break;
           }
           
           if( adjpPresent && posTag2.contains("v_oru") ) {
          	 slaveTokenReadings.clear();
             break;
           }
           else if( numrPresent && Arrays.asList("ранок", "день", "вечір", "ніч", "пополудень").contains(token.getLemma()) && token.getPOSTag().contains("v_rod") ) {
          	 slaveTokenReadings.clear();
             break;
           }
           else if( pronPresent && token.getLemma().equals("решта") ) {
          	 slaveTokenReadings.clear();
             break;
           }
           
           slaveTokenReadings.add(token);
         }
         else if ( posTag2.equals(JLanguageTool.SENTENCE_END_TAGNAME) ) {
           continue;
         }
         else {
           slaveTokenReadings.clear();
           break;
         }
      }

      // no slave token - restart
      
      if( slaveTokenReadings.isEmpty() ) {
      	adjTokenReadings.clear();
      	continue;
      }
      
//      System.err.println("=== Checking ");
//      System.err.println("\t" + adjTokenReadings);
//      System.err.println("\t" + slaveTokenReadings);
      
      // perform check
      
      List<Inflection> masterInflections = getAdjInflections(adjTokenReadings);

      List<Inflection> slaveInflections = getNounInflections(slaveTokenReadings);

      if( Collections.disjoint(masterInflections, slaveInflections) ) {

      	// моїх маму й сестер
      	if( i < tokens.length - 2
      			&& PosTagHelper.hasPosTag(adjAnalyzedTokenReadins, "adj:p:.*")
      			&& forwardConjFind(tokens, i+1, 2)
      			&& ! disjointIgnoreGender(masterInflections, slaveInflections) ) {
      		//    					&& PosTagHelper.hasPosTag(tokens[i+2], "(numr|noun):.*") ) {	// TODO: must match 1st noun inflection (but number may differ)
      		adjTokenReadings.clear();
      		continue;
      	}

      	// два нових горнятка (див. #1 нижче)
      	if( i > 1
      			&& PosTagHelper.hasPosTag(tokenReadings, "noun:.*:p:(v_naz|v_zna).*")
      			&& Arrays.asList("два", "три", "чотири").contains(tokens[i-2].getAnalyzedToken(0).getLemma())
      			&& PosTagHelper.hasPosTag(adjAnalyzedTokenReadins, "adj.*:p:v_rod.*") ) {
      		adjTokenReadings.clear();
      		continue;
      	}
      	// порядок денний
      	if( i > 1 
      			&& PosTagHelper.hasPosTag(tokens[i-2], "noun:.*")
      			&& ! Collections.disjoint(masterInflections, getNounInflections(tokens[i-2].getReadings())) ) {
      		adjTokenReadings.clear();
      		continue;
      	}

      	// навчальної та середньої шкіл
      	if( i > 2 
      			&& PosTagHelper.hasPosTag(tokenReadings, "noun:.*:p:.*")
      			&& reverseConjFind(tokens, i-2, 2)
      			&& ! disjointIgnoreGender(masterInflections, slaveInflections) ) {
      		//      				&& PosTagHelper.hasPosTag(tokens[i-3], "adj.*") ) {	// TODO: must match 2nd adj inflection
      		adjTokenReadings.clear();
      		continue;
      	}

      	// ані судова, ані правоохоронна системи
      	if( i > 2 
      			&& PosTagHelper.hasPosTag(tokenReadings, "noun:.*:p:.*")
      			&& Arrays.asList("ані", "також").contains(tokens[i-2].getAnalyzedToken(0).getLemma())
      			&& ! disjointIgnoreGender(masterInflections, slaveInflections) ) {
      		adjTokenReadings.clear();
      		continue;
      	}

      	// зробити відкритим доступ
      	if( i > 1
      			&& PosTagHelper.hasPosTag(tokens[i-2], "verb:.*")
      			&& PosTagHelper.hasPosTag(tokens[i-1], "adj.*:v_oru.*")
      			&& PosTagHelper.hasPosTag(tokens[i], "noun:.*:v_zna.*") 
      			&& genderMatches(masterInflections, slaveInflections, "v_oru", "v_zna") ) {
      		adjTokenReadings.clear();
      		continue;
      	}

      	
      	if( inflectionControlMatches(adjTokenReadings, slaveInflections) ) {
      		adjTokenReadings.clear();
      		continue;
      	}
      	
//        System.err.println("===");
//        System.err.println("\t" + adjAnalyzedTokenReadins.getToken() + ": " + masterInflections + " // " + adjAnalyzedTokenReadins);
//        System.err.println("\t" + slaveTokenReadings.get(0).getToken() + ": " + slaveInflections);
        
        String msg = String.format("Неузгоджені прикметник з іменником: \"%s\" (%s) і \"%s\" (%s)", 
            adjTokenReadings.get(0).getToken(), masterInflections, slaveTokenReadings.get(0).getToken(), slaveInflections);
				RuleMatch potentialRuleMatch = new RuleMatch(this, adjAnalyzedTokenReadins.getStartPos(), tokenReadings.getEndPos(), msg, getShort());
      	ruleMatches.add(potentialRuleMatch);
      }

      adjTokenReadings.clear();
    }
    
    return toRuleMatchArray(ruleMatches);
  }

  private static boolean hasButyLemma(AnalyzedTokenReadings analyzedTokenReadings) {
  	for(AnalyzedToken analyzedToken: analyzedTokenReadings.getReadings()) {
  		if( "бути".equalsIgnoreCase(analyzedToken.getLemma()) )
  			return true;
  	}
  	return false;
  }

	private boolean genderMatches(List<Inflection> masterInflections, List<Inflection> slaveInflections, String masterCaseFilter, String slaveCaseFilter) {
//  	System.err.println("master " + masterInflections + " / " + slaveInflections);
  	for (Inflection masterInflection : masterInflections) {
  		for (Inflection slaveInflection : slaveInflections) {
//  			System.err.println("matching gender " + masterInflection.gender + " = " + slaveInflection.gender );
  			if( masterInflection._case.equals(masterCaseFilter)
  					&& slaveInflection._case.equals(slaveCaseFilter)
  					&& slaveInflection.gender.equals(masterInflection.gender) ) 
  				return true;
  		}
  	}
  	return false;
	}

	private Collection<String> getPrepGovernedCases(AnalyzedTokenReadings analyzedTokenReadings) {
  	ArrayList<String> reqCases = new ArrayList<>(); 
  	for(AnalyzedToken reading: analyzedTokenReadings.getReadings()) {
  		String posTag = reading.getPOSTag();
  		if( posTag != null && posTag.contains("rv_") ) {
  			Matcher matcher = TokenAgreementRule.REQUIRE_VIDMINOK_REGEX.matcher(posTag);
  			while( matcher.find() ) {
  				reqCases.add(matcher.group(1));
  			}
  			break;
  		}
  	}
  	return reqCases;
  }

	private boolean reverseConjFind(AnalyzedTokenReadings[] tokens, int pos, int depth) {
  	for(int i=pos; i>pos-depth && i>=0; i--) {
  		if( CONJ_FOR_PLURAL.contains(tokens[i].getAnalyzedToken(0).getLemma())
  				|| tokens[i].getAnalyzedToken(0).getToken().equals(",") )
  			return true;
  	}
		return false;
  }

  private boolean forwardConjFind(AnalyzedTokenReadings[] tokens, int pos, int depth) {
  	for(int i=pos; i<tokens.length && i<= pos+depth; i++) {
  		if( CONJ_FOR_PLURAL.contains(tokens[i].getAnalyzedToken(0).getLemma())
  				|| tokens[i].getAnalyzedToken(0).getToken().equals(",") )
  			return true;
  	}
		return false;
  }

  private boolean inflectionControlMatches(List<AnalyzedToken> adjTokenReadings, List<Inflection> slaveInflections) {
    // TODO: key tags (e.g. pos) should be part of the map key
  	return adjTokenReadings.stream().map(p -> p.getLemma()).distinct().anyMatch( item -> {
  	  	Set<String> inflections = inflectionControl.get( item );
//  	  	System.err.println("Found inflections " + item + ": " + inflections);
  	  	  if( inflections != null ) {
    		for (Inflection inflection : slaveInflections) {
      		  if( inflections.contains(inflection._case) )
      			return true;
      		}
      	  }
      	  return false;
      	}
  	);
  }

	private List<Inflection> getAdjInflections(List<AnalyzedToken> adjTokenReadings) {
	List<Inflection> masterInflections = new ArrayList<>();
	for (AnalyzedToken token: adjTokenReadings) {
	  String posTag2 = token.getPOSTag();
		Matcher matcher = ADJ_INFLECTION_PATTERN.matcher(posTag2);
	  matcher.find();
	  
		String gen = matcher.group(1);
		String vidm = matcher.group(2);
		String animTag = null;
		if (matcher.group(3) != null) {
			animTag = matcher.group(3).substring(2);	// :rinanim/:ranim
		}
		
		masterInflections.add(new Inflection(gen, vidm, animTag));
	}
	return masterInflections;
	}

  private List<Inflection> getNounInflections(List<AnalyzedToken> nounTokenReadings) {
  	List<Inflection> slaveInflections = new ArrayList<>();
  	for (AnalyzedToken token: nounTokenReadings) {
  		String posTag2 = token.getPOSTag();
  		Matcher matcher = NOUN_INFLECTION_PATTERN.matcher(posTag2);
  		if( ! matcher.find() ) {
//  			System.err.println("Failed to find slave inflection tag in " + posTag2 + " for " + nounTokenReadings);
  			continue;
  		}
  		String gen = matcher.group(2);
  		String vidm = matcher.group(3);
  		String animTag = matcher.group(1);

  		slaveInflections.add(new Inflection(gen, vidm, animTag));
  	}
  	return slaveInflections;
  }


  private boolean disjointIgnoreGender(List<Inflection> masterInflections, List<Inflection> slaveInflections) {
  	for (Inflection mInflection : masterInflections) {
  		for(Inflection sInflection : slaveInflections) {
  			if( mInflection.equalsIgnoreGender(sInflection) )
  				return false;
  		}
  	}
  	return true;
	}

	@Override
  public void reset() {
  }

  private static class Inflection {
  	final String gender;
  	final String _case;
  	final String animTag;

  	public Inflection(String gender, String _case, String animTag) {
  		this.gender = gender;
  		this._case = _case;
  		this.animTag = animTag;
  	}

	@Override
  	public int hashCode() {
  		final int prime = 31;
  		int result = 1;
  		result = prime * result + ((_case == null) ? 0 : _case.hashCode());
  		result = prime * result + ((animTag == null) ? 0 : animTag.hashCode());
  		result = prime * result + ((gender == null) ? 0 : gender.hashCode());
  		return result;
  	}

  	@Override
  	public boolean equals(Object obj) {
  		if (this == obj)
  			return true;
  		if (obj == null)
  			return false;
  		if (getClass() != obj.getClass())
  			return false;

  		Inflection other = (Inflection) obj;
  		return gender.equals(other.gender)
  				&& _case.equals(other._case)
  				&& (animTag == null || other.animTag == null 
  				|| ! animMatters() || ! other.isAnimalSensitive() || animTag.equals(other.animTag));
  	}

  	public boolean equalsIgnoreGender(Inflection other) {
  		return //gender.equals(other.gender)
  				_case.equals(other._case)
  				&& (animTag == null || other.animTag == null 
  				|| ! animMatters() || animTag.equals(other.animTag));
  	}

  	private boolean animMatters() {
  		return _case.equals("v_zna") && isAnimalSensitive();
  	}

  	private boolean isAnimalSensitive() {
  		return "mp".contains(gender);
  	}

  	@Override
  	public String toString() {
  		return ":" + gender + ":" + _case
  				+ (animMatters() ? "_"+animTag : "");
  	}

  }


  private static Map<String, Set<String>> loadMap(String path) {
  	Map<String, Set<String>> result = new HashMap<>();
  	try (InputStream is = JLanguageTool.getDataBroker().getFromResourceDirAsStream(path);
  			Scanner scanner = new Scanner(is, "UTF-8")) {
  		while (scanner.hasNextLine()) {
  			String line = scanner.nextLine();
  			String[] parts = line.split(" ");
  			String[] vidm = parts[1].split(":");
  			result.put(parts[0], new HashSet<String>(Arrays.asList(vidm)));
  		}
//  		System.err.println("Found case governments: " + result.size());
  		return result;
  	} catch (IOException e) {
  		throw new RuntimeException(e);
  	}
  }

// #1 Із «Теоретичної граматики» (с.173):
//	
//	"Якщо в числівниково-іменникових конструкціях із числівниками два, три,  
//	чотири (а також зі складеними числівниками, де кінцевими компонентами  
//	виступають два, три, чотири) у формах називного — знахідного відмінка множини
//	вживаються прикметники, дієприкметники або займенникові прикметники, то
//	ці означальні компоненти або узгоджуються з іменником, набуваючи форм  
//	відповідно називного чи знахідного відмінка множини, або функціонують у  
//	формі родового відмінка множини, напр.: Тенор переплітається з сопраном,  
//	неначе дві срібні нитки (І. Нечуй-Левицький,); Дві людських руки вкупі— се кільце,
//	за яке, ухопившися, можна зрушити землю (Ю. Яновський)."

  
}
