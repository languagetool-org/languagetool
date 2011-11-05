/* LanguageTool, a natural language style checker 
 * Copyright (C) 2010 Daniel Naber (http://www.languagetool.org)
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

package de.danielnaber.languagetool.dev.conversion;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.net.URISyntaxException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.danielnaber.languagetool.JLanguageTool;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;
import morfologik.stemming.WordData;


public class AtdRuleConverter extends RuleConverter {
    
    private static final Pattern nounInPattern = Pattern.compile("NN(?!P|S|\\.)");
    private static final Pattern wordReference = Pattern.compile("\\\\(\\d+)"); // a word reference, e.g. \1
    private static final Pattern wordReferenceTransform = Pattern.compile("\\\\(\\d+):([^:]+)");
    private static final Pattern uppercase = Pattern.compile("[A-Z]");
    
    private static IStemmer dictLookup = (DictionaryLookup) loadDictionary();
    
    private String avoidMessage = "";
    
    // default constructor
    public AtdRuleConverter() {
        super();
    }
    
    // constructor with filename
    public AtdRuleConverter(String inFile, String outFile, String specificFileType) {
        super(inFile,outFile,specificFileType);
    }
    
    @Override
    public String getOriginalRuleString(Object ruleObject) {
    	return (String)ruleObject;
    }
    
    @Override
	public String generateName(Object ruleObject) {
		String name = "rule_" + nameIndex;
		nameIndex++;
		return name;
	}
	
	@Override
	public String generateId(Object ruleObject) {
		String name = "rule_" + idIndex;
		idIndex++;
		return name;
	}
    
    @Override
    public String[] getAcceptableFileTypes() {
    	String[] ft = {"default","avoid"};
    	return ft;
    }
    
    @Override
    public void parseRuleFile() throws IOException {
    	// open the input file
        Scanner in = new Scanner(new FileInputStream(inFileName));
        // list to hold the rules as strings
        List<String> ruleList = new ArrayList<String>();
        try {
        	int lineCount = 0;
            while (in.hasNextLine()) {
                String line = in.nextLine().trim();
                // toss the comments and blank lines
                // check for the explanation you can give you an interro-hash at the beginning of "avoid" files
                if (line.startsWith("#?") && lineCount == 0 && ruleType.equals("avoid")) {
                	avoidMessage = line.substring(2);
                }
                else if (line.startsWith("#") || line.equals("")) {
                    continue;
                } else {
                    if (line.contains("#")) {
                        line = line.substring(0,line.indexOf("#"));
                    }
                    ruleList.add(line);
                }
                lineCount++;
            }
        } finally {
            in.close();
        }  
        ruleObjects = ruleList;
        allLtRules = new ArrayList<List<String>>();
        ltRules = new ArrayList<List<String>>();
        disambiguationRules = new ArrayList<List<String>>();
        originalRuleStrings = new ArrayList<String>();
        warnings = new ArrayList<String[]>();
        for (Object ruleObject : ruleObjects) {
        	String ruleString = (String)ruleObject;
        	HashMap<String,String> ruleMap = parseRule(ruleString);
        	List<String> ruleAsList = ltRuleAsList(ruleMap,getIdFromName(ruleMap.get("name")),fixName(ruleMap.get("name")),this.ruleType);
        	if (notKilledRule(ruleMap)) {
        		ltRules.add(ruleAsList);
        	} else {
        		disambiguationRules.add(ruleAsList);
        	}
        	allLtRules.add(ruleAsList);
        	originalRuleStrings.add(ruleMap.get("ruleString"));
        }
    }
    
    @Override
    public boolean isDisambiguationRule(Object ruleObject) {
    	String rule = (String)ruleObject;
    	HashMap<String,String> ruleMap = parseRule(rule);
    	return (ruleMap.containsKey("filter") && (ruleMap.get("filter").equals("kill") || ruleMap.get("filter").equals("die")));
    }
    
    public HashMap<String,String> parseRule(String rule) {
        HashMap<String,String> outRule = new HashMap<String,String>();
        if (this.ruleType == "default") {
            String[] splitRule = rule.split("::");
            outRule.put("pattern", splitRule[0]);
            outRule.put("name", getNameFromPattern(splitRule[0]));
            if (splitRule.length > 1) {
                for (int i=1;i<splitRule.length;i++) {
                    // add with key=declaration, value=terms 
                    String[] splitDeclaration = splitRule[i].split("=");
                    try {
                    	outRule.put(splitDeclaration[0], splitDeclaration[1]);
                    } catch (ArrayIndexOutOfBoundsException e) {
                    	System.err.println("Incorrect declaration for rule " + rule + "; rule skipped");
                    }
                    
                }
            }
        }
        else if (this.ruleType == "avoid") {
        	// sometimes avoid rules still have word:: declarations
        	if (rule.contains("::word")) {
        		String[] splitRule = rule.split("::");
        		outRule.put("pattern", splitRule[0]);
        		outRule.put("name",getNameFromPattern(splitRule[0]));
        		if (splitRule.length > 1) {
                    for (int i=1;i<splitRule.length;i++) {
                        // add with key=declaration, value=terms 
                        String[] splitDeclaration = splitRule[i].split("=");
                        try {
                        	outRule.put(splitDeclaration[0], splitDeclaration[1]);
                        } catch (ArrayIndexOutOfBoundsException e) {
                        	System.err.println("Incorrect declaration for rule " + rule + "; rule skipped");
                        }
                    }
                }
        	} else {
        		String[] splitRule = rule.split("\t+");
        		outRule.put("pattern", splitRule[0]);
        		outRule.put("name", getNameFromPattern(splitRule[0]));
                // if there's no tab, or no term after the tab, like clichedb.txt
                if (splitRule.length > 1) {
                	outRule.put("explanation", splitRule[1]);
                } else {
                	outRule.put("explanation", "");
                }
        	}
        }
        // accounting for the fact that AtD is case sensitive
        if (isCaseSensitiveRule(outRule.get("pattern"))) {
            outRule.put("casesensitive","true");
        } else {
            outRule.put("casesensitive","false");
        }
        // store the string of the rule itself
        outRule.put("ruleString", rule);
        
        return outRule;
    }
    
    /**
     * Takes a HashMap of an AtD rule, and returns a list of lines of XML in LT format.
     * 
     * @param rule: HashMap of values like "pattern" and "message"
     * @param id: String of rule id
     * @param name: String of rule name
     * 
     * @return list of XML lines
     * 
     */
    @SuppressWarnings("unchecked")
	@Override
    public List<String> ltRuleAsList(Object ruleObject, String id, String name, String type) {
        ArrayList<HashMap<String,String>> outerList = new ArrayList<HashMap<String,String>>();
        HashMap<String,String> mainRule = (HashMap<String,String>)ruleObject;
        ArrayList<String> currentWarnings = new ArrayList<String>();
        // first expand the macros in the pattern, in case we have to fix an apostrophe case
        String[] mainPattern = mainRule.get("pattern").split("\\ +");
        for (int i=0;i<mainPattern.length;i++) {
        	mainPattern[i] = expandMacro(mainPattern[i]);
        }
        mainRule.put("pattern",gluePattern(mainPattern));
        if (isApostropheCase(mainRule.get("pattern").split("\\ +"))) {
        	ArrayList<HashMap<String,String>> splitRules = handleApostropheCase(mainRule,ruleObject,type);
        	for (HashMap<String,String> splitRule : splitRules) {
        		outerList.add(splitRule);
        	}
        } else {
        	outerList.add(mainRule);
        }
 
        ArrayList<String> bigLtRule = new ArrayList<String>();
        bigLtRule.add("<!-- " + mainRule.get("ruleString") + " -->");
        if (outerList.size() > 1) {
        	bigLtRule.add("<rulegroup id=\"" + id + "\" name=\"" + name + "\">");
        }
        for (HashMap<String,String> rule : outerList) {
        	ArrayList<String> ltRule = new ArrayList<String>();
        	if (outerList.size() == 1) {
        		ltRule.add(firstIndent + "<rule id=\"" + id + "\" name=\"" + name + "\">");	// if the rule had to be split
        	} else {
        		ltRule.add(firstIndent + "<rule>");
        	}
        	String exceptions = null;
        	// (avoid=...) words that should make the rule fail if they appear in the pattern
            if (rule.containsKey("avoid")) {	
            	exceptions = getAvoidWords(rule.get("avoid"));
            }
            // add the pattern element
            if (Boolean.parseBoolean(rule.get("casesensitive"))) {
                ltRule.add(secondIndent + "<pattern case_sensitive=\"yes\">");
            } else {
                ltRule.add(secondIndent + "<pattern>");
            }
            
            String[] pattern = rule.get("pattern").split("\\ +");
           
            String[] newpattern = fixApostrophes(pattern);
            String suggestion = null;
            if (rule.containsKey("word")) {
            	suggestion = rule.get("word");
            	// suggestion = fixApostrophesSuggestion(suggestion,pattern);	// too problematic of a method to currently call
            	if (rule.get("pattern").contains("'")) {
            		if (!currentWarnings.contains(WARNINGS.APOSTROPHES.value)) {
            			currentWarnings.add(WARNINGS.APOSTROPHES.value);
            		}
            		
            	}
            }
            pattern = newpattern;
            for (int i=0;i<pattern.length;i++) {
                String e = pattern[i];
                currentWarnings = getWarningsFromPatternElement(currentWarnings, e);
                ltRule = addTokenHelper(ltRule,e,thirdIndentInt,exceptions);
            }
            ltRule.add(secondIndent + "</pattern>");
            if (suggestion != null) {
            	currentWarnings = getWarningsFromSuggestion(currentWarnings, suggestion);
                ltRule = addSuggestion(ltRule, suggestion, pattern, secondIndentInt);
            }
            // for the "avoid" type rules, like biasdb.txt and avoiddb.txt
            if (rule.containsKey("explanation")) {
            	String explanation = rule.get("explanation");
            	ltRule = addExplanation(ltRule,explanation,secondIndentInt);
            }
            if (rule.containsKey("filter")) {
                if (rule.get("filter").equals("kill") || rule.get("filter").equals("die")) {
                    ltRule.add(secondIndent + "<disambig action=\"immunize\"/>");
                } else { 
                	currentWarnings = getWarningsFromFilter(currentWarnings,rule.get("filter"));
                }
            }
            ltRule.add(firstIndent + "</rule>");
            bigLtRule.addAll(ltRule);
        }
        if (outerList.size() > 1) {
        	bigLtRule.add("</rulegroup>");
        }
        warnings.add(currentWarnings.toArray(new String[currentWarnings.size()]));
        return bigLtRule;
    }
    
    // heuristic method to get an easy-to-understand name from the rule's (atd) pattern
    private String getNameFromPattern(String pattern) {
    	String[] sp = pattern.split("\\s+");
    	StringBuilder name = new StringBuilder();
    	for (String s : sp) {
    		if (!hasPosTag(s)) {
    			String[] orSplit = s.split("\\|");
    			if (orSplit.length > 3) {
    				String truncOr = orSplit[0] + "|" + orSplit[1] + "|" + orSplit[2];
    				name.append(truncOr + " ");
    			} else {
    				name.append(s + " ");
    			}
    		} else if (justPosTag(s)) {
    			String[] ss = s.split("/");
    			name.append(ss[1] + " ");
    		} else {
    			name.append(s + " ");
    		}
    	}
    	return name.toString().trim();
    }

    // transforming the name-from-pattern string into a suitable ID
    // doesn't deal at all with possible duplicate IDs
    private String getIdFromName(String name) {
    	name = name.replaceAll("\\ +", "_");
    	name = name.replaceAll("<|>|!|&|\\.|\\*|\\+|/|\\[.*?\\]","");
    	name = name.toUpperCase();
    	return name;
    }
    
    private String fixName(String name) {
    	name = name.replaceAll("<|>|!|&|\\.|\\*|\\+|/|\\[.*?\\]","");
    	return name;
    }
    
    /**
     * Returns warnings from the pattern element of an AtD rule
     * @param curWarn
     * @param element
     * @return
     */
    private ArrayList<String> getWarningsFromPatternElement(ArrayList<String> curWarn, String element) {
    	String token;
    	if (hasPosTag(element)) {
    		token = element.split("/")[0];
    	} else {
    		token = element;
    	}
    	if (isRegex(token) && (token.contains("<") || token.contains(">"))) {
    		if (!curWarn.contains(WARNINGS.ANGLE_BRACKETS.value)) {
    			curWarn.add(WARNINGS.ANGLE_BRACKETS.value);
    		}
    	}
    	if (justPosTag(element)) {
    		if (!curWarn.contains(WARNINGS.EXCLUSIVE.value)) {
    			curWarn.add(WARNINGS.EXCLUSIVE.value);
    		}
    	}
    	return curWarn;
    }
    
    /**
     * Returns warnings from the suggestion part of an AtD rule
     * @param curWarn
     * @param sugg
     * @return
     */
    private ArrayList<String> getWarningsFromSuggestion(ArrayList<String> curWarn, String sugg) {
    	String[] splitSuggs = sugg.split(",\\s*");
    	for (String ss : splitSuggs) {
    		String[] suggestionParts = ss.split("\\ +");
    		for (String sp : suggestionParts) {
    			String transform = getTransformString(sp);
    			if (transform.equals(":positive")) {
    				if (!curWarn.contains(WARNINGS.POSITIVE.value)) {
    					curWarn.add(WARNINGS.POSITIVE.value);
    				}
    			} else if (transform.equals(":determiner")) {
    				if (!curWarn.contains(WARNINGS.DETERMINER.value)) {
    					curWarn.add(WARNINGS.DETERMINER.value);
    				}
    			} else if (transform.equals(":nosuffix")) {
    				if (!curWarn.contains(WARNINGS.NO_SUFFIX.value)) {
    					curWarn.add(WARNINGS.NO_SUFFIX.value);
    				}
    			}
    		}
    	}
    	return curWarn;
    }
    
    /**
     * Returns warnings based on the filter specified in the AtD rule
     * @param curWarn
     * @param filter
     * @return
     */
    private ArrayList<String> getWarningsFromFilter(ArrayList<String> curWarn, String filter) {
    	if (filter.equals("indefarticle")) {
    		if (!curWarn.contains(WARNINGS.INDEFARTICLE.value)) {
    			curWarn.add(WARNINGS.INDEFARTICLE.value);
    		}
    	} else if (filter.equals("stats")) {
    		if (!curWarn.contains(WARNINGS.STATS.value)) {
    			curWarn.add(WARNINGS.STATS.value);
    		}
    	} else if (filter.equals("nextonly")) {
    		if (!curWarn.contains(WARNINGS.NEXTONLY.value)) {
    			curWarn.add(WARNINGS.NEXTONLY.value);
    		}
    	}
    	return curWarn;
    }
    
    /**
     * Fixes the normal apostrophes in a pattern, so the LT tokens can be correctly generated
     * For example, turns "don't be cruel" into "don ' t be cruel".
     * @param p
     * @return
     */
    private String[] fixApostrophes(String[] p) {
    	ArrayList<String> retList = new ArrayList<String>();
    	for (String s : p) {
    		if (s.equals("'")) {
    			retList.add(s);
    		} else if (s.contains("'")) {
    			String[] temp = s.replaceAll("'"," ' ").split("\\ +");
    			for (String sTemp : temp) {
    				retList.add(sTemp);
    			}
    		} else {
    			retList.add(s);
    		}
    	}
    	return retList.toArray(new String[retList.size()]);
    }
    
    
    
    /**
     * Returns true if the pattern contains an or regexp with apostrophes. E.g. wouldn't|couldn't|would|could
     * @param pattern
     * @return
     */
    public boolean isApostropheCase(String[] pattern) {
    	for (String s : pattern) {
    		if (s.contains("'") && s.contains("|")) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Properly splits up the apostrophe'd or patterns, and re-calls ltRuleAsList with appropriate pattern arguments
     * @param rule
     * @param type
     * @return
     */
    public ArrayList<HashMap<String,String>> handleApostropheCase(HashMap<String,String> rule, Object ruleObject, String type) {
    	String[] pattern = rule.get("pattern").split("\\ +");
    	String oldSuggestion = null;
    	if (rule.containsKey("word")) {
    		oldSuggestion = rule.get("word");
    	}
    	String offendingToken = "";
    	int offendingTokenIndex = 0;
    	for (int i=0;i<pattern.length;i++) {
    		String token = pattern[i];
    		if (token.contains("'") && token.contains("|")) {
    			offendingToken = token;
    			offendingTokenIndex = i;
    			break;
    		}
    	}
    	String[] brokenToken = offendingToken.split("\\|");
    	HashMap<String,ArrayList<String>> suffixMap = new HashMap<String,ArrayList<String>>();
    	for (String token : brokenToken) {
    		if (!token.contains("'")) {
    			suffixMap = addItemSmart(suffixMap, "regular", token);
    		} else {
    			String[] splitToken = token.split("'");
    			// for the case where the apostrophe is at the end (e.g. mothers')
    			if (splitToken.length == 1) {
    				suffixMap = addItemSmart(suffixMap, "", splitToken[0]);
    			} else {
    				suffixMap = addItemSmart(suffixMap, splitToken[1], splitToken[0]);
    			}
    		}
    	}
    	ArrayList<String> newPatterns = new ArrayList<String>();
    	for (String suffix : suffixMap.keySet()) {
    		String newPattern = "";
    		for (int i=0;i<pattern.length;i++) {
    			if (i == offendingTokenIndex) {
    				ArrayList<String> prefixes = suffixMap.get(suffix);
    				String prefixString = "";
    				for (String prefix : prefixes) {
    					prefixString = prefixString + prefix + "|";
    				}
    				prefixString = prefixString.substring(0, prefixString.length() - 1);
    				if (suffix.equals("regular")) {
    					newPattern = newPattern + prefixString + " ";
    				} else {
    					newPattern = newPattern + prefixString + " ' " + suffix + " ";
    				}    				
    			} else {
    				newPattern = newPattern + pattern[i] + " ";
    			}
    		}
    		newPattern = newPattern.trim();
    		newPatterns.add(newPattern);
    	}
    	ArrayList<HashMap<String,String>> allRules = new ArrayList<HashMap<String,String>>();
    	for (String newPattern : newPatterns) {
    		HashMap<String,String> r = new HashMap<String,String>(rule);
    		r.put("pattern",newPattern);
    		if (oldSuggestion != null) {
    			// this isn't working, have to take care of this
    		}
    		allRules.add(r);
    	}
    	return allRules;
    	
    	
    }
    
    public static boolean isMacro(String e) {
        return e.contains("&");
    }
    
    /**
     * expands AtD macros like &uncountables and &irregular_verbs
 	 * need to take the macros out of the atd file rules.sl and put them in some
     * accessible (and easily extendible) format
     */
    public static String expandMacro(String e) {
    	if (e.charAt(0) == '&') {
    		return MACRO_EXPANSIONS.valueOf(e.substring(1)).value;
    	} else {
    		return e;
    	}
    }
    
    /** 
     * Get an or-ed list of words to include as exceptions in the rule
     * @param exceptions
     * @return
     */
    public String getAvoidWords(String exceptions) {
    	String[] avoidWords = exceptions.split(", ");
    	String retString = "";
    	for (String s : avoidWords) {
    		retString = retString + s + "|";
    	}
    	retString = retString.substring(0,retString.length() - 1);
    	return retString;
    }
    
    /**
     * Takes a LT rule and a word from the AtD pattern and adds the proper token to the LT rule
     * @param ltRule
     * @param e
     * @param spaces
     * @param exceptions
     * @return
     */
    public ArrayList<String> addTokenHelper(ArrayList<String> ltRule, String e, int spaces, String exceptions) {
        // special cases of start and end of sentence anchors
        if (e.equals("0BEGIN.0")) {
            ltRule = addToken(ltRule, "", SENT_START, null, false, false, false, 0, thirdIndentInt);
            return ltRule;
        }
        if (e.equals("0END.0")) {
        	ltRule = addToken(ltRule, "", SENT_END, null, false, false, false, 0, thirdIndentInt);
        	return ltRule;
        }
        if (hasPosTag(e)) {
            String[] parts = e.split("/");
            parts[1] = fixNoun(parts[1]);
            ltRule = addToken(ltRule, parts[0], parts[1], exceptions, false, false, false, 0, thirdIndentInt);
        } else {
            ltRule = addToken(ltRule, e, null, exceptions, false, false, false, 0, thirdIndentInt);
        }
        return ltRule;
    }
    
    private static ArrayList<String> addSuggestion(ArrayList<String> orig, String suggestion, String[] pattern, int indent) {
        String space = getSpace(indent) ;
        orig.add(space + "<message> " + expandSuggestion(suggestion,pattern) + "</message>");
        return orig;
    }

    
    private ArrayList<String> addExplanation(ArrayList<String> orig, String explanation, int indent) {
    	String space = getSpace(indent);
    	String explanationString = "";
    	if (!explanation.equals("")) {
    		explanationString = " \"" + explanation + "\"";
    	}
    	orig.add(space + "<message>" + avoidMessage + explanationString + "</message>");
    	return orig;
    }
    
  
    /**
     * Takes the word=... suggestion from an AtD rule and puts it in LT <message> format
     * @param suggestion
     * @param pattern
     * @return
     */
    public static String expandSuggestion(String suggestion, String[] pattern) {
        String[] splitSuggestion = suggestion.split(",");   // split into wholly separate suggestions
        StringBuilder sb = new StringBuilder();
        sb.append("Did you mean ");
        for (int i=0;i<splitSuggestion.length;i++) {
            String s = splitSuggestion[i];  
            String[] ss = s.split("\\ +");
            for (int j=0;j<ss.length;j++) {
                // process each word and put the processed version into ss
                String e = ss[j];
                Matcher m2 = wordReferenceTransform.matcher(e);
                Matcher m1 = wordReference.matcher(e);
                if (m2.find()) {
                    ss[j] = expandTransform(e, pattern);                 
                } else if (m1.find()) {
                    int numMatched = Integer.parseInt(m1.group().replaceAll("\\\\", ""));
                    if (Arrays.asList(pattern).contains("0BEGIN.0")) {
                        numMatched++;   // for proper back referencing
                    }
                    ss[j] = "<match no=\"" + Integer.toString(numMatched + 1) + "\"/>";
                }  
            }
            // reformat ss into a string, and append ss to sb
            sb.append("<suggestion>");
            for (int j=0;j<ss.length;j++) {
                sb.append(ss[j]);
                if (j < ss.length - 1) {
                    sb.append(" ");
                }
            }
            sb.append("</suggestion>");
            if (i < splitSuggestion.length - 1 && splitSuggestion.length > 1) {
                sb.append(" or ");
            }
        }
        sb.append("?");
        return sb.toString();
    }
    
 
    /** 
     * Expands the contiguous reference with transform (\1:upper, \2:singular, e.g.)
     * @param element: reference with transform
     * @param pattern: the pattern the reference references
     * @return
     */
    public static String expandTransform(String element, String[] pattern) {
        String[] se = element.split(":");
        int numMatched = Integer.parseInt(se[0].replaceAll("\\\\", ""));
        if (Arrays.asList(pattern).contains("0BEGIN.0")) {
            numMatched++;   // for proper back referencing
        }
        String transform = se[1];
        String retString = null;
        String refWord = pattern[numMatched];
        // TODO: currently only works for non regexps. should be fixed
        if (transform.equals("nosuffix")) {
            // the ATD nosuffix heuristic
            if (refWord.endsWith("able") || refWord.endsWith("ible")) {
                String strip = refWord.substring(0,refWord.length() - 4);
                if (inDictionary(strip.concat("ated"))) {
                    retString = strip.concat("ated");
                }
                else if (inDictionary(strip.concat("e"))) {
                    retString = strip.concat("e");
                }
                else if (inDictionary(strip.concat("y"))) {
                    retString = strip.concat("y");
                }
                else if (inDictionary(strip)) {
                    retString = strip;
                } 
                else {
                    retString = refWord;
                }
            } else {
                retString = refWord;
            }
            
        } 
        else if (transform.equals("upper")) {
            retString = "<match no=\"" + Integer.toString(numMatched + 1) + "\" case_conversion=\"startupper\" />";
        }  
        else if (transform.equals("lower")) {
            retString = "<match no=\"" + Integer.toString(numMatched + 1) + "\" case_conversion=\"alllower\" />";
        }
        else if (transform.equals("singular")) {
        	retString = "<match no=\"" + Integer.toString(numMatched + 1) + "\" postag=\"NNP|NN(:U.?)?\" postag_regexp=\"yes\" />";
        }
        else if (transform.equals("plural")) {
           	retString = "<match no=\"" + Integer.toString(numMatched + 1) + "\" postag=\"NNPS|NNS\" postag_regexp=\"yes\" />";
        }
        else if (transform.equals("participle")) {
            retString = "<match no=\"" + Integer.toString(numMatched + 1) + "\" postag=\"VBN\" />";
        }
        else if (transform.equals("base")) {
            retString = "<match no=\"" + Integer.toString(numMatched + 1) + "\" postag=\"VB\" />";
        }
        else if (transform.equals("past")) {
            retString = "<match no=\"" + Integer.toString(numMatched + 1) + "\" postag=\"VBD\" />";
        }
        else if (transform.equals("present")) {
            retString = "<match no=\"" + Integer.toString(numMatched + 1) + "\" postag=\"VBG\" />";
        }
        else if (transform.equals("determiner") || transform.equals("determiner2")) {
        	// currently a hack, as I can't see how to replace AtD's bigram probability.
        	// should be good for a lot of situations
        	retString = "the";
        }
        else if (transform.equals("positive")) {
        	// don't know how i'll work this for non explicitly stated words (ones that need \1, i.e.)
        }
        else if (transform.equals("possessive")) {
        	retString = "<match no=\"" + Integer.toString(numMatched + 1) + "\" postag=\"NN(:U.?)?\" postag_regexp=\"yes\" />'s";
        }
        return retString;
    }
    
    /**
     * Checks if there's an uppercase word in the AtD rule pattern, so we can set the casesensitive flag
     * @param pattern
     * @return
     */
    public boolean isCaseSensitiveRule(String pattern) {
        boolean caseSensitive = false;
        String[] splitPattern = pattern.split("\\ +");
        for (String s : splitPattern) {
        	if (s.equals("0BEGIN.0") || s.equals("0END.0")) {
        		continue;
        	}
            String[] splitS = s.split("/");
            if (uppercase.matcher(splitS[0]).find()) {
                caseSensitive = true;
            }
        }
        return caseSensitive;
    }
    
    /**
     * Replaces NN with NN|NN:UN?, to account for the new LT mass noun tags
     * @param postag
     * @return
     */
    public static String fixNoun(String postag) {
    	Matcher m = nounInPattern.matcher(postag);
    	if (m.find()) {
    		postag = postag.replaceFirst("NN(?!P|S|\\.)","NN|NN:UN?");
    	}
    	return postag;
    }
    
    /**
     * Applies to the AtD false alarm rules
     * @param rule
     * @return
     */
    public boolean notKilledRule(HashMap<String,String> rule) {
    	if (rule.containsKey("filter")) {
    		if (rule.get("filter").equals("kill") || rule.get("filter").equals("die")) {
    			return false;
    		}
    	}
    	return true;
    }
    
    /**
     * Glues a split pattern back together
     * @param p
     * @return
     */
    private static String gluePattern(String[] p) {
    	StringBuilder sb = new StringBuilder();
    	for (String s : p) {
    		sb.append(s);
    		sb.append(' ');
    	}
    	return sb.toString().trim();
    }
    
    private static String getTransformString(String ref) {
    	Matcher m2 = wordReferenceTransform.matcher(ref);
    	if (m2.find()) {
    		return ":" + m2.group(2);
    	}
    	return "";
    }
    
    // ** DICTIONARY METHODS ** 
    
    // if this is done in a non-static way it might save some time or be cleaner
    // changes for later
    private static boolean inDictionary(String word) {
        if (dictLookup == null) {
            dictLookup = (DictionaryLookup) loadDictionary();
        }
        return !dictLookup.lookup(word).isEmpty();
    }
    
    // this should be general, not specific to English
    private static IStemmer loadDictionary() {
        IStemmer dictLookup = null;
        String fileName = "/en/english.dict";
        URL url = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(fileName);
        File dictFile = null;
        try {
        	dictFile = new File(url.toURI());
        } catch (URISyntaxException e) {
        	e.printStackTrace();
        }
        try {
            dictLookup = new DictionaryLookup(Dictionary.read(dictFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dictLookup;
    }
    
    /**
     * Checks if an element of the AtD pattern has a specific postag.
     * @param word: an element of an AtD pattern; (accepts both .\*\/NN and <word> types)
     * @param postag: a specific postag (no regexes)
     * @return
     */
    public static boolean hasSpecificPosTag(String word, String postag) {
    	if (dictLookup == null) {
    		dictLookup = (DictionaryLookup) loadDictionary();
    	}
    	if (hasPosTag(word)) {
    		String[] splitWord = word.split("/");
    		if (Pattern.matches(splitWord[1], postag)) {
    			return true;
    		}
    		return false;
    	}
    	List<WordData> lwd = dictLookup.lookup(word);
    	for (WordData wd : lwd) {
    		if (wd.getTag().toString().equals(postag)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Helper method to appropriate add an item to a HashMap
     * @param map
     * @param key
     * @param item
     * @return
     */
    public static HashMap<String,ArrayList<String>> addItemSmart(HashMap<String,ArrayList<String>> map, String key, String item) {
    	if (map.containsKey(key)) {
    		ArrayList<String> existing = map.get(key);
    		existing.add(item);
    		map.put(key,existing);
    	} else {
    		ArrayList<String> newList = new ArrayList<String>();
    		newList.add(item);
    		map.put(key, newList);
    	}
    	return map;
    }
    
    /**
     * @param e: AtD token
     * @return if the token contains a "/", indicating that it contains POS tag information
     */
    private static boolean hasPosTag(String e) {
        return e.contains("/");
    }
    
    /**
     * Returns true if AtD token is just a POS tag
     */
    private static boolean justPosTag(String e) {
    	if (e.length() < 3) return false;
    	return (e.charAt(0) == '.' && e.charAt(1) == '*' && e.charAt(2) == '/');
    }
    
    private enum WARNINGS {
    	
    	APOSTROPHES ("Apostrophes in the pattern may have affected the numbering in the suggestion."),
    	ANGLE_BRACKETS ("Angle brackets in regular expressions need to be written as &gt; or &lt;"),
    	EXCLUSIVE ("AtD POS tags are not exclusive, and so rules may be too greedy. Consider adding this."),
    	POSITIVE ("Positive transform not supported."),
    	DETERMINER ("Determiner transform relies on bigram probabilities. Current implementation just returns \"the\""),
    	NO_SUFFIX ("No suffix transform doesn't work for match elements or regular expressions."),
    	INDEFARTICLE ("Indefarticle filter uses n-gram probabilities. Rules using it should probably be discarded."),
    	STATS ("Stats filter uses n-gram probabilities. Rules using it should probably be discarded."),
    	NEXTONLY ("Nextonly filter uses n-gram probabilities. Rules using it should probably be discarded.");
    	
    	public String value;
    	WARNINGS(String v) {
    		this.value = v;
    	}
    }
    
    private enum MACRO_EXPANSIONS {
    	
    	selfwords ("self-education|self-administering|self-fertilization|self-consequence|self-preservation|self-preservative" +
    			"|self-interested|self-mastery|self-conceit|self-protection|self-identity|self-distrust|self-dissatisfaction" +
    			"|self-depreciation|self-congratulation|self-government|self-pity|self-closing|self-consistent|self-propelling" +
    			"|self-declared|self-starter|self-effacing|self-produced|self-constituted|self-exertion|self-cleaning|self-seeking" +
    			"|self-limited|self-hypnosis|self-surrender|self-contained|self-complacency|self-sacrifice|self-congratulatory" +
    			"|self-absorption|self-development|self-exile|self-aggrandizing|self-sufficient|self-enrichment|self-proclaimed" +
    			"|self-repression|self-affirmation|self-contradictory|self-taught|self-deprecating|self-deprecation|self-perpetuating" +
    			"|self-satisfied|self-explanatory|self-indulgence|self-sustained|self-aware|self-generating|self-devotion" +
    			"|self-maintenance|self-ruling|self-laudation|self-abasement|self-luminous|self-complacent|self-criticism" +
    			"|self-deceiving|self-pitying|self-evident|self-contradiction|self-originated|self-trust|self-revelation" +
    			"|self-reflection|self-reflective|self-worth|self-regulatory|self-conceited|self-determining|self-flagellation" +
    			"|self-inflicted|self-employed|self-regulating|self-regulation|self-injury|self-trained|self-similar|self-serve" +
    			"|self-determination|self-same|self-absorbed|self-adjusting|self-sown|self-propagating|self-defence|self-awareness" +
    			"|self-organization|self-defense|self-defeating|self-lighting|self-confident|self-containment|self-centered" +
    			"|self-indulgent|self-justification|self-rule|self-sustaining|self-selection|self-addressed|self-knowledge" +
    			"|self-deception|self-possessed|self-assertiveness|self-incrimination|self-exaltation|self-improvement" +
    			"|self-righteous|self-reflexive|self-training|self-glorification|self-fertilize|self-delusion|self-correcting" +
    			"|self-responsibility|self-forgetfulness|self-confessed|self-destructive|self-educated|self-employment" +
    			"|self-propelled|self-destruction|self-healing|self-sacrificingly|self-exalting|self-examination|self-doubt" +
    			"|self-immolation|self-justified|self-named|self-existent|self-satisfaction|self-control|self-estrangement" +
    			"|self-starting|self-analysis|self-limiting|self-importance|self-gratulation|self-regarding|self-help" +
    			"|self-treatment|self-respecting|self-instruction|self-governing|self-sufficiently|self-condemnation" +
    			"|self-renunciation|self-heal|self-contemplation|self-deluded|self-respect|self-sufficiency|self-deceived" +
    			"|self-hatred|self-winding|self-love|self-abuse|self-communion|self-convicted|self-torture|self-dual|self-initiated" +
    			"|self-realization|self-denying|self-representation|self-reproach|self-activity|self-cultivation|self-evaluation" +
    			"|self-created|self-suggestion|self-attraction|self-command|self-assertive|self-asserting|self-assertion" +
    			"|self-derived|self-fulfilling|self-assured|self-betrayal|self-loading|self-possession|self-study|self-dependence" +
    			"|self-reliant|self-appointed|self-injurious|self-sufficing|self-action|self-acting|self-imposed|self-serving" +
    			"|self-restrained|self-service|self-effacement|self-elected|self-caused|self-reform|self-expression|self-assurance" +
    			"|self-raising|self-restraining|self-aggrandizement|self-flattery|self-adornment|self-approval|self-dependent" +
    			"|self-image|self-division|self-interest|self-regulated|self-will|self-regard|self-shining|self-written|self-abnegation" +
    			"|self-important|self-consciousness|self-obsessed|self-pride|self-subsistent|self-condemned|self-restraint" +
    			"|self-portrait|self-determined|self-righteousness|self-gratification|self-powered|self-defensive|self-ownership" +
    			"|self-denial|self-induced|self-governed|self-forgetful|self-sacrificing|self-generated|self-moving|self-given" +
    			"|self-identification|self-discipline|self-reliance|self-care|self-appreciation|self-definition|self-accusatory" +
    			"|self-mortification|self-esteem|self-professed|self-reference|self-disgust|self-controlled|self-supporting" +
    			"|self-mutilation|self-styled|self-culture|self-accusation|self-directed|self-devoted|self-advertisement" +
    			"|self-confidence|self-described|self-conscious|self-administered|self-opinion|self-support|self-centred|self-made" +
    			"|self-critical|self-consciously|self-conquest|self-repressed|self-willed"),
    	absolutes ("dead|disappeared|empty|false|full|gone|illegal|infinite|invaluable|legal|perfect|pervasive|pregnant" +
    			"|professional|true|whole|vanished|(omni[a-z]+)"),
    	uncountable ("accommodation|advice|access|baggage|bread|equipment|garbage|luggage|money|cattle|knowledge|sand|furniture" +
    			"|meat|food|news|pasta|progress|research|water|freedom|maturity|intelligence|travel|pollution|traffic"),		
    	modal_verbs ("can|could|may|must|should|will|would|can't|couldn't|mustn't|shouldn't|won't|wouldn't"),
    	comparisons_base ("good|bad|hot|cold|lame|less|more|great|heavy|light|smart|dumb|cheap|sexy|tall|short|fast|slow|old" +
    			"|young|easy|hard|high|low|large|small|big|soon|late|strong|loud|quiet|dark|bright"),
    	comparisons ("hotter|colder|lamer|less|lesser|more|greater|heavier|lighter|better|worse|smarter|dumber|cheaper|sexier" +
    			"|taller|shorter|faster|slower|older|younger|easier|harder|farther|closer|higher|lower|larger|smaller|sooner" +
    			"|later|weaker|stronger|louder|quieter|darker|brighter|Hotter|Colder|Lamer|Less|Lesser|More|Greater|Heavier" +
    			"|Lighter|Better|Worse|Smarter|Dumber|Cheaper|Sexier|Taller|Shorter|Faster|Slower|Older|Younger|Easier|Harder" +
    			"|Farther|Closer|Higher|Lower|Larger|Smaller|Sooner|Later|Weaker|Stronger|Louder|Quieter|Darker|Brighter"),
    	past ("\\w+ed|awoken|borne|beaten|become|begun|bent|bet|bitten|bled|blown|broken|bred|brought|built|burnt|burst|bought" +
    			"|caught|chosen|come|cost|cut|dealt|done|drawn|dreamt|drunk|driven|eaten|made|meant|met|paid|put|quit|read|ridden" +
    			"|rung|risen|run|said|seen|sought|sold|sent|set|shaken|shone|shot|shown|shut|sung|sunk|sat|slept|smelt|spoken" +
    			"|spent|spilt|spoilt|spread|stood|stolen|stuck|stung|stunk|struck|sworn|swum|taken|taught|torn|told|thought|thrown" +
    			"|understood|woken|worn|wept|won|written"),
    	irregular_nouns_plural ("addenda|alumni|analyses|axes|bacilli|bacteria|bases|calves|crises|criteria|curricula|data|dice" +
    			"|diagnoses|elves|ellipses|emphases|errata|firemen|feet|genera|geese|halves|hypotheses|knives|leaves|lives|loaves" +
    			"|lice|men|matrices|media|memoranda|mice|neuroses|nuclei|oases|ova|paralyses|parentheses|people|phenomena|selves" +
    			"|shelves|stimuli|strata|syntheses|synopses|those|theses|thieves|these|teeth|wives|wolves|women"),
    	irregular_verb_past_perfect ("arisen|awoken|backbitten|been|beaten|befallen|begotten|begun|begirt|bespoken|bestridden|betaken" +
    			"|bidden|bided|bitten|blawn|blown|bowstrung|broken|chosen|cleeked|counterdrawn|cowritten|crash-dived|crib-bitten|cross" +
    			"-bitten|crowed|dared|deep-frozen|dived|done|drawn|drunk|driven|eaten|fallen|farebeaten|flash-frozen|flown|flyblown" +
    			"|forbidden|fordone|foregone|foreknown|foreseen|forespoken|forgotten|forgiven|forlorn|forsaken|forsworn|free-fallen" +
    			"|frozen|frostbitten|ghostwritten|given|gone|grown|hagridden|halterbroken|hand-ridden|handwritten|hewn|hidden|hoten" +
    			"|housebroken|interwoven|known|lain|mischosen|misdone|misfallen|misgiven|misknown|misspoken|missworn|mistaken|misworn" +
    			"|miswritten|mown|outdone|outdrawn|outdrunk|outdriven|outflown|outgrown|outridden|outseen|outsung|outspoken|outsprung" +
    			"|outsworn|outswum|outthrown|outworn|outwritten|overborne|overblown|overdone|overdrawn|overdrunk|overdriven|overeaten" +
    			"|overflown|overgrown|overlain|overridden|overseen|overspoken|oversprung|overstridden|overtaken|overthrown|overworn" +
    			"|overwritten|partaken|predone|preshrunk|quick-frozen|redone|redrawn|regrown|retaken|retorn|retrodden|reworn|rewritten" +
    			"|ridden|rung|risen|rough-hewn|seen|shaken|shown|shrunk|shriven|sightseen|sung|sunk|skywritten|slain|smitten|sown|spoken" +
    			"|spun|sprung|stolen|stunk|stridden|striven|sworn|swollen|swum|swonken|taken|torn|test-driven|test-flown|thrown|trodden" +
    			"|typewritten|underdone|undergone|underlain|undertaken|underwritten|undone|undrawn|undrawn|unfrozen|unhidden|unspoken" +
    			"|unsworn|untrodden|unwoven|unwritten|uprisen|upsprung|uptorn|woken|worn|woven|wiredrawn|withdrawn|written"),
    	irregular_verb_past ("arose|ate|awoke|bade|beat|became|befell|began|begot|bespoke|bestrode|betook|bit|blew|bode|bore|broke" +
    			"|built|came|chose|cowrote|crew|did|dove|drank|drew|drove|fell|flew|forbade|forbore|foresaw|forewent|forgave|forgot" +
    			"|forsook|froze|gave|grew|hewed|hid|hight|knew|lay|misgave|misspoke|mistook|mowed|outdid|outgrew|outran|overbore" +
    			"|overcame|overlay|overran|overrode|oversaw|overthrew|overtook|partook|ran|rang|reawoke|redid|redrew|retook|rewrote" +
    			"|rived|rode|rose|sang|sank|saw|shook|shore|showed|shrank|slew|smote|sowed|span|spoke|sprang|stank|stole|strewed" +
    			"|strode|strove|swam|swelled|swore|threw|took|tore|trod|underlay|undertook|underwent|underwrote|undid|uprose|was|went" +
    			"|withdrew|woke|wore|wove|wrote"),
    	irregular_verb_base ("abide|alight|arise|awake|backlight|be|bear|befall|beget|begin|behold|belay|bend|beseech|bespeak|betake" +
    			"|bethink|bid|bide|bind|bite|bleed|blend|bless|blow|bowstring|break|breed|bring|build|burn|buy|catch|chide|choose" +
    			"|clap|cleave|cling|clothe|creep|crossbreed|crow|dare|daydream|deal|dig|disprove|dive|do|dogfight|dow|draw|dream|drink" +
    			"|drive|dwell|eat|engrave|fall|feed|feel|fight|find|flee|fling|fly|forbear|forbid|forego|foresee|foretell|forget|forgive" +
    			"|forsake|forswear|freeze|frostbite|gainsay|gaslight|geld|get|gild|gin|gird|give|gnaw|go|grave|grind|grow|hamstring" +
    			"|hang|have|hear|heave|hew|hide|hoist|hold|inbreed|inlay|interbreed|interweave|keep|ken|kneel|know|lade|landslide|lay" +
    			"|lead|lean|leap|learn|leave|lend|lie|light|lose|make|mean|meet|melt|mislead|misspell|mistake|misunderstand|moonlight" +
    			"|mow|outdo|outgrow|outlay|outride|outshine|overdo|overeat|overhang|overhear|overlay|overleap|overlie|overpass|override" +
    			"|oversee|overshoot|overspill|overtake|overthrow|overwrite|partake|pay|pen|plead|prove|rap|rebuild|redo|redraw|reeve" +
    			"|regrow|relay|relight|remake|rend|repay|resell|retake|retell|rethink|retrofit|rewind|rewrite|ride|ring|rise|saw|say" +
    			"|see|seek|sell|send|sew|shake|shave|shear|shew|shine|shoe|shoot|show|shrink|sing|sink|sit|slay|sleep|slide|sling|slink" +
    			"|smell|smite|sneak|sow|speak|speed|spell|spend|spill|spin|spoil|spring|stand|stave|steal|stick|sting|stink|strew|stride" +
    			"|strike|string|strip|strive|sunburn|swear|sweep|swell|swim|swing|take|teach|tear|tell|think|thrive|throw|tine|tread" +
    			"|troubleshoot|typewrite|unbend|unbind|undergo|underlay|underlie|underpay|undersell|undershoot|understand|undertake" +
    			"|underwrite|undo|unlearn|unmake|unsay|unwind|uphold|uprise|vex|wake|waylay|wear|weave|wed|weep|wend|whipsaw|win|wind" +
    			"|wit|withdraw|withhold|withstand|work|wrap|wreak|wring|write|zinc"),
    	determiner_wanted ("absence|adult|affair|agreement|airport|alliance|amount|angle|announcement|apartment|appearance|appointment" +
    			"|argument|arrangement|arrival|assertion|assumption|atmosphere|atom|attitude|aunt|author|automobile|bag|ballot|bar|barrel" +
    			"|beast|bird|birthday|bit|blade|boat|bottle|bottom|bow|breast|bridge|brother|bullet|bundle|burden|cabin|cabinet|canal" +
    			"|candle|car|career|carriage|case|castle|cat|cave|ceiling|centre|chamber|chapter|charm|chest|child|circumstance|citizen" +
    			"|clerk|clip|clock|coalition|colleague|collection|combination|companion|complaint|concept|conclusion|condition|constitution" +
    			"|continent|corner|coup|couple|cousin|cow|creator|creature|crew|crowd|crown|decade|default|defect|departure|description" +
    			"|desk|device|distance|dock|doctor|doctrine|document|dog|dome|dozen|draft|duration|ear|earthquake|edge|editorial|egg" +
    			"|election|employer|encyclopedia|endorsement|engagement|envelope|episode|equation|eruption|essay|establishment|estate|event" +
    			"|exception|expedition|explosion|extension|extent|fan|farmer|feast|fee|fence|field|finger|flood|floor|flower|fool|forehead" +
    			"|formation|fraction|framework|friend|frontier|future|gadget|gallon|gap|garden|gate|generation|gift|glance|glimpse" +
    			"|grandfather|group|guy|handful|harbour|hat|height|hero|hole|holiday|horizon|horse|hospital|hotel|hour|household|husband" +
    			"|iPhone|iPod|illustration|impression|impulse|institution|instrument|intention|interior|interval|interview|introduction" +
    			"|investigation|invitation|island|job|joke|journal|journey|kid|kitchen|knife|knight|lamp|laptop|lawsuit|lawyer|leg|legislature" +
    			"|lesson|lifetime|lion|lot|lover|manner|manuscript|margin|meal|message|method|mile|mill|mind|minimum|minute|mirror|mission" +
    			"|mixture|moment|monarch|monster|month|monument|moon|mouth|movie|museum|name|nation|needle|neighborhood|nest|nose|notebook" +
    			"|notion|nurse|oath|obligation|opinion|opponent|orchestra|organ|organism|ounce|outbreak|outcome|oven|pair|parent|partnership" +
    			"|path|patient|patron|pattern|peasant|pen|pencil|period|person|phenomenon|photo|photograph|phrase|picture|piece|pile|pilot" +
    			"|pint|pipe|plane|planet|plot|pocket|poem|portrait|pot|pound|presence|presentation|price|principle|prisoner|problem|product" +
    			"|profession|project|proposal|proposition|province|publication|pupil|puzzle|race|reader|realm|recession|redirect|refusal" +
    			"|regiment|region|reign|relationship|remainder|report|reporter|reputation|request|requirement|resolution|restaurant|ring|road" +
    			"|role|roof|rope|row|rumor|sake|scene|sea|seat|sentence|servant|shadow|shaft|ship|shore|signature|sister|situation|skin|slave" +
    			"|slope|smile|soldier|song|soul|speaker|sphere|stage|statement|statue|stomach|storm|stranger|street|student|successor|suggestion" +
    			"|sum|summit|sun|surface|sword|symbol|tail|tale|telescope|template|temple|term|theme|thing|threat|throat|throne|thumb|tide|tip" +
    			"|title|tomb|tongue|topic|transition|tree|trend|triangle|trick|trip|trunk|type|uncle|universe|verb|vessel|village|visitor|volcano" +
    			"|voyage|weapon|web|wedding|week|weekend|widow|window|winner|world|yard|effort|environment|genre|list|photo|picture|population" +
    			"|range|response|stake|suburb|thing|type|understanding|view|warning"),
    	irregular_verb ("abide|abode|alight|arise|arose|ate|awake|awoke|be|bear|became|befall|befell|began|beget|begin|begot|behold|bend" +
    			"|beseech|betake|bethink|betook|bind|bit|bite|bleed|blew|blow|bore|break|breed|bring|broke|browbeat|build|burn|buy|came" +
    			"|catch|chide|choose|chose|clap|cling|clothe|creep|dare|daydream|deal|did|dig|disprove|dive|do|dove|drank|draw|dream" +
    			"|drew|drink|drive|drove|dwell|eat|fall|feed|feel|fell|fight|find|flee|flew|fling|fly|forbade|forbear|forbid|forbore" +
    			"|forego|foresaw|foresee|foretell|forewent|forgave|forget|forgive|forgot|forsake|forsook|forswear|freeze|frostbite|froze" +
    			"|gainsay|gave|get|gild|give|go|grew|grind|grow|hang|have|hear|heave|hew|hewed|hid|hide|hold|inbreed|inlay|keep|kneel" +
    			"|knew|know|lade|landslide|lay|lead|lean|leap|learn|leave|lend|lie|light|lose|make|mean|meet|mislead|misspell|mistake" +
    			"|mistook|misunderstand|mow|outdid|outdo|outgrew|outgrow|outlay|outran|outride|outshine|overbore|overcame|overdo|overeat" +
    			"|overhang|overhear|overlay|overlay|overleap|overlie|overran|override|oversaw|oversee|overtake|overthrew|overthrow|overtook" +
    			"|overwrite|partake|partook|pay|plead|prove|ran|rang|rebuild|redid|redo|reeve|refit|regrow|relay|relight|remake|rend|repay" +
    			"|retake|retell|rethink|retook|rewind|rewrite|rewrote|ride|ring|rise|rived|rode|rose|sang|sank|saw|saw|say|see|seek|sell" +
    			"|send|sew|shake|shave|shear|sheared|shine|shoe|shook|shoot|show|showed|shrank|shrink|sing|sink|sit|slay|sleep|slide|sling" +
    			"|slink|smell|smite|sneak|sow|sowed|speak|speed|spell|spend|spill|spin|spoil|spoke|sprang|spring|stand|stank|stave|steal" +
    			"|stick|sting|stink|stole|strew|strewed|stride|strike|string|strip|strive|strode|strove|sunburn|swam|swear|sweep|swell" +
    			"|swelled|swim|swing|swore|take|teach|tear|tell|think|threw|thrive|throve|throw|took|tore|tread|troubleshoot|typewrite" +
    			"|unbend|unbind|undergo|underlay|underlay|underlie|undersell|understand|undertake|undertook|underwent|undid|undo|unlearn" +
    			"|unmake|unsay|unwind|uphold|vex|wake|was|waylay|wear|weave|wed|weep|went|whet|win|wind|withdraw|withdrew|withhold|withstand" +
    			"|woke|wore|wove|wring|write|wrote");
    	private String value;
    	MACRO_EXPANSIONS(String v) {
    		this.value = v;
    	}
    }
    

}
