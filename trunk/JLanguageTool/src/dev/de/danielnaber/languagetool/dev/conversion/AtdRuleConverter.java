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
    

    private final static String MACRO_EXPANSIONS_FILE = "/en/macro_expansions.txt";	
    private static HashMap<String,String> macroExpansions = fillOutMacroExpansions();
    
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
        warnings = new ArrayList<String>();
        for (Object ruleObject : ruleObjects) {
        	String ruleString = (String)ruleObject;
        	HashMap<String,String> ruleMap = parseRule(ruleString);
        	List<String> ruleAsList = ltRuleAsList(ruleMap,generateId(ruleMap),generateName(ruleMap),this.ruleType);
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
        String currentWarning = "";
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
        	bigLtRule.add("<rulegroup name=\"" + name + "\" id=\"" + id + "\">");
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
            		currentWarning += "Apostrophes in the pattern may have affected the numbering in the suggestion.\n";
            	}
            }
            pattern = newpattern;
            for (int i=0;i<pattern.length;i++) {
                String e = pattern[i];
                currentWarning = getWarningsFromPatternElement(currentWarning, e);
                ltRule = addTokenHelper(ltRule,e,thirdIndentInt,exceptions);
            }
            ltRule.add(secondIndent + "</pattern>");
            if (suggestion != null) {
            	currentWarning = getWarningsFromSuggestion(currentWarning, suggestion);
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
                	currentWarning = getWarningsFromFilter(currentWarning,rule.get("filter"));
                }
            }
            ltRule.add(firstIndent + "</rule>");
            bigLtRule.addAll(ltRule);
        }
        if (outerList.size() > 1) {
        	bigLtRule.add("</rulegroup>");
        }
        warnings.add(currentWarning);
        return bigLtRule;
    }
    
    /**
     * Returns warnings from the pattern element of an AtD rule
     * @param curWarn
     * @param element
     * @return
     */
    private String getWarningsFromPatternElement(String curWarn, String element) {
    	String token;
    	if (hasPosTag(element)) {
    		token = element.split("/")[0];
    	} else {
    		token = element;
    	}
    	if (isRegex(token) && (token.contains("<") || token.contains(">"))) {
    		curWarn += "Angle brackets in regular expressions need to be written as &gt; or &lt;\n";
    	}
    	return curWarn;
    }
    
    /**
     * Returns warnings from the suggestion part of an AtD rule
     * @param curWarn
     * @param sugg
     * @return
     */
    private String getWarningsFromSuggestion(String curWarn, String sugg) {
    	String[] splitSuggs = sugg.split(",\\s*");
    	for (String ss : splitSuggs) {
    		String[] suggestionParts = ss.split("\\ +");
    		for (String sp : suggestionParts) {
    			String transform = getTransformString(sp);
    			if (transform.equals(":positive")) {
    				curWarn += "Positive transform not supported.\n";
    			} else if (transform.equals(":determiner")) {
    				curWarn += "Determiner transform relies on bigram probabilities. Current implementation just returns \"the\"\n";
    			} else if (transform.equals(":nosuffix")) {
    				curWarn += "No suffix transform doesn't work for match elements or regular expressions.\n";
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
    private String getWarningsFromFilter(String curWarn, String filter) {
    	if (filter.equals("indefarticle")) {
    		curWarn += "Indefarticle filter uses n-gram probabilities. Rules using it should probably be discarded.\n";
    	} else if (filter.equals("stats")) {
    		curWarn += "Stats filter uses n-gram probabilities. Rules using it should probably be discarded.\n";
    	} else if (filter.equals("nextonly")) {
    		curWarn += "Nextonly filter uses n-gram probabilities. Rules using it should probably be discarded.\n";
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
        	return macroExpansions.get(e);
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
    
    public static HashMap<String,String> fillOutMacroExpansions() {
        HashMap<String, String> macroExpansions = new HashMap<String,String>();
        ArrayList<String> lines = fileToListNoBlanks(MACRO_EXPANSIONS_FILE);
        for (String s : lines) {
            String[] sl = s.split("=");
            for (int i=0; i < sl.length; i++) {
                sl[i] = sl[i].trim();
            }
            macroExpansions.put(sl[0], sl[1]);
        }
        return macroExpansions;
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
    
    /*
    private String fixApostrophesSuggestion(String sugg, String[] oldp) {
    	String[] splitS = sugg.split(",");
    	
    	for (int i=0;i<splitS.length;i++) {
    		String curs = splitS[i];
    		String[] sc = curs.split("\\ +");
    		ArrayList<String> newSuggestion = new ArrayList<String>();
    		int numBump = 0;
    		for (int j=0;j<sc.length;j++) {
    			String e = sc[j];
    			int numMatched = getReference(e);
    			if (numMatched != -1) {
    				String transform = getTransformString(e);
        			if (oldp[numMatched].contains("'")) {
        				String[] newElements = oldp[numMatched].replaceAll("'", " ' ").split("\\ +");
        				numBump = newElements.length;
        				for (int k=0;k<newElements.length;k++) {
        					newSuggestion.add("\\\\" + numMatched++ + transform);
        				}
        			} else {
        				newSuggestion.add("\\\\" + (numMatched + numBump - 1) + transform);
        			}
    			} else {
    				newSuggestion.add(e);
    			}
    			
    		}
    		StringBuilder sb = new StringBuilder();
    		for (String s : newSuggestion) {
    			sb.append(s + " ");
    		}
    		splitS[i] = sb.toString().trim();
    	}
    	StringBuilder sb = new StringBuilder();
    	for (String s : splitS) {
    		sb.append(s + ",");
    	}
    	String retString = sb.toString();
    	return retString.substring(0, retString.length() - 1);
    }
    */
    
    

}
