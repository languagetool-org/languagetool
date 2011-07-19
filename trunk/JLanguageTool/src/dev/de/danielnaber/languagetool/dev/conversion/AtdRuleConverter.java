package de.danielnaber.languagetool.dev.conversion;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.WordData;

import de.danielnaber.languagetool.JLanguageTool;


public class AtdRuleConverter extends RuleConverter {
    

    private final static String MACRO_EXPANSIONS_FILE = "." + JLanguageTool.getDataBroker().getResourceDir() + "/en/macro_expansions.txt";	
    private static HashMap<String,String> macroExpansions = fillOutMacroExpansions();
    
    private static Pattern nounInPattern = Pattern.compile("NN(?!P|S|\\.)");
    
    // default constructor
    public AtdRuleConverter() {
        super();
    }
    
    // constructor with filename
    public AtdRuleConverter(String inFile, String outFile, String specificFileType) {
        super(inFile,outFile,specificFileType);
    }
    
    @Override
    public List<String> getRulesAsString() throws IOException {
        // open the input file
        Scanner in = new Scanner(new FileInputStream(inFileName));  // might need allowance for encoding?
        // list to hold the rules as strings
        List<String> ruleList = new ArrayList<String>();
        try {
            while (in.hasNextLine()) {
                String line = in.nextLine().trim();
                // toss the comments and blank lines
                if (line.startsWith("#") || line.equals("")) {
                    continue;
                } else {
                    if (line.contains("#")) {
                        line = line.substring(0,line.indexOf("#"));
                    }
                    ruleList.add(line);
                }
            }
        } finally {
            in.close();
        }  
        return ruleList;
    }


    @Override
    public HashMap<String,String> parseRule(String rule) {
        HashMap<String,String> outRule = new HashMap<String,String>();
        if (this.ruleType == "default") {
            String[] splitRule = rule.split("::");
            outRule.put("pattern", splitRule[0]);
            if (splitRule.length > 1) {
                for (int i=1;i<splitRule.length;i++) {
                    // add with key=declaration, value=terms 
                    String[] splitDeclaration = splitRule[i].split("=");
                    outRule.put(splitDeclaration[0], splitDeclaration[1]);
                }
            }
        }
        else if (this.ruleType == "avoid") {
            String[] splitRule = rule.split("\t+");    // should include checking if there's no tab, or no term after the tab
            outRule.put("pattern", splitRule[0]);
            outRule.put("explanation", splitRule[1]);    // so this'll have to change            
        }
        // accounting for the fact that AtD is case sensitive
        if (isCaseSensitiveRule(outRule.get("pattern"))) {
            outRule.put("casesensitive","true");
        } else {
            outRule.put("casesensitive","false");
        }
        
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
    @Override
    public List<String> ltRuleAsList(HashMap<String,String> rule, String id, String name, String type) {
        ArrayList<String> ltRule = new ArrayList<String>();
        if (id != null && name != null) {
            ltRule.add(firstIndent + "<rule " + "id=\"" + id + "\" name=\"" + name + "\">");
        } else {
            ltRule.add(firstIndent + "<rule>");
        }   
        String exceptions = null;
        if (rule.containsKey("avoid")) {	// (avoid=...) words that should make the rule fail if they appear in the pattern
        	exceptions = getAvoidWords(rule.get("avoid"));
        }
        // for the "avoid" rules
        if (type.equals("avoid")) {
            if (Boolean.parseBoolean(rule.get("casesensitive"))) {
                ltRule.add(secondIndent + "<pattern case_sensitive=\"yes\">");
            } else {
                ltRule.add(secondIndent + "<pattern>");
            }            
            String[] pattern = rule.get("pattern").split("\\ +");
            if (isApostropheCase(pattern)) {
            	return handleApostropheCase(rule, type);
            }
            for (int i=0;i<pattern.length;i++) {
                String e = pattern[i];
                if (e.contains("'")) {
                    ltRule = handleRegularApostrophe(ltRule,e,thirdIndentInt,exceptions);
                } else {
                    ltRule = addTokenHelper(ltRule,e,thirdIndentInt,exceptions);
                }
            }
            ltRule.add(secondIndent + "</pattern>");
            ltRule.add(secondIndent + "<message>" + rule.get("explanation") + "</message>");
            ltRule.add(firstIndent + "</rule>");   
        }
        // for the default rules ( pattern::declaration="..."::declaration="..." )
        else if (type.equals("default")) {
        	// because AtD is case sensitive and LT is not (by default)
            if (Boolean.parseBoolean(rule.get("casesensitive"))) {
                ltRule.add(secondIndent + "<pattern case_sensitive=\"yes\">");
            } else {
                ltRule.add(secondIndent + "<pattern>");
            }
            String[] pattern = rule.get("pattern").split("\\ +");
            if (isApostropheCase(pattern)) {
            	return handleApostropheCase(rule, type);
            }
            for (int i=0;i<pattern.length;i++) {
                String e = pattern[i];
                if (e.contains("'")) {
                    ltRule = handleRegularApostrophe(ltRule,e,thirdIndentInt,exceptions);
                } else {
                    ltRule = addTokenHelper(ltRule,e,thirdIndentInt,exceptions);
                }
            }
            ltRule.add(secondIndent + "</pattern>");
            if (rule.containsKey("word")) {
                ltRule = addSuggestion(ltRule, rule.get("word"), pattern, secondIndentInt);
            }
            if (rule.containsKey("filter")) {
                if (rule.get("filter").equals("kill")) {
                    ltRule.add(secondIndent + "<disambig action=\"immunize\"/>");
                }
            }
            ltRule.add(firstIndent + "</rule>");
            
        }
        return ltRule;
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
    public List<String> handleApostropheCase(HashMap<String,String> rule, String type) {
    	String[] pattern = rule.get("pattern").split("\\ +");
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
    	ArrayList<String> allRules = new ArrayList<String>();
    	for (String newPattern : newPatterns) {
    		rule.put("pattern",newPattern);
    		List<String> newLtRule = ltRuleAsList(rule,getSuitableID(rule),getSuitableName(rule),type);
    		allRules.addAll(newLtRule);
    	}
    	return allRules;
    	
    	
    }
    
    /**
     * Helper method to appropriate add an item to a HashMap
     * @param map
     * @param key
     * @param item
     * @return
     */
    public HashMap<String,ArrayList<String>> addItemSmart(HashMap<String,ArrayList<String>> map, String key, String item) {
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
     * Properly splits up words with apostrophes in them and adds the tokens to the LT rule
     * @param ltRule
     * @param element
     * @param indent
     * @return
     */
    public ArrayList<String> handleRegularApostrophe(ArrayList<String> ltRule, String element, int indent, String exceptions) {
    	if (element.equals("'")) {
    		ltRule = addTokenHelper(ltRule,element,indent,exceptions);
    		return ltRule;
    	}
    	String[] temp = element.replaceAll("'", " ' ").split("\\ +");
        for (String sTemp : temp) {
            ltRule = addTokenHelper(ltRule,sTemp,indent,exceptions);
        }
        return ltRule;
    }

    
    public static boolean isMacro(String e) {
        return e.contains("&");
    }
    
    // expands AtD macros like &uncountables and &irregular_verbs
    // need to take the macros out of the atd file rules.sl and put them in some
    // accessible (and easily extendible) format
    public static String expandMacro(String e) {
        return macroExpansions.get(e);
    }
    
    public String getAvoidWords(String exceptions) {
    	String[] avoidWords = exceptions.split(", ");
    	String retString = "";
    	for (String s : avoidWords) {
    		retString = retString + s + "|";
    	}
    	retString = retString.substring(0,retString.length() - 1);
    	return retString;
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
    
    public ArrayList<String> addTokenHelper(ArrayList<String> ltRule, String e, int spaces, String exceptions) {
        // special cases of start and end of sentence anchors
        if (e.equals("0BEGIN.0")) {
            ltRule = addToken(ltRule, null, null, "sentstart", thirdIndentInt, exceptions);
            return ltRule;
        }
        if (e.equals("0END.0")) {
        	ltRule = addToken(ltRule, null, null, "sentend", thirdIndentInt, exceptions);
        	return ltRule;
        }
        if (hasPosTag(e)) {
            String[] parts = e.split("/");
            parts[1] = fixNoun(parts[1]);
            if (parts[0].equals(".*")) {
                if (isRegex(parts[1])) {
                    ltRule = addToken(ltRule, null, parts[1], "regexpostag", thirdIndentInt, exceptions);
                } else {
                    ltRule = addToken(ltRule, null, parts[1], "postag", thirdIndentInt, exceptions);
                }  
            } else {
                if (isRegex(parts[0])) {
                    if (isRegex(parts[1])) {
                        ltRule = addToken(ltRule, parts[0], parts[1], "regexandregexpostag", thirdIndentInt, exceptions);
                    } else {
                        ltRule = addToken(ltRule, parts[0], parts[1], "regexandpostag", thirdIndentInt, exceptions);
                    }
                } else {
                    if (isRegex(parts[1])) {
                        ltRule = addToken(ltRule, parts[0], parts[1], "tokenandregexpostag", thirdIndentInt, exceptions);
                    } else {
                        ltRule = addToken(ltRule, parts[0], parts[1], "tokenandpostag", thirdIndentInt, exceptions);
                    }
                }  
            }
        } else {
            if (isRegex(e)) {
                ltRule = addToken(ltRule, e, null, "regextoken", thirdIndentInt, null);
            } else if (isMacro(e)) {
                ltRule = addToken(ltRule, expandMacro(e), null, "regextoken", thirdIndentInt, exceptions);
            } else {
                ltRule = addToken(ltRule, e, null, "token", thirdIndentInt, null);
            }
        }
        return ltRule;
    }
    
    public static ArrayList<String> addSuggestion(ArrayList<String> orig, String suggestion, String[] pattern, int indent) {
        String space = getSpace(indent) ;
        orig.add(space + "<message> " + expandSuggestion(suggestion,pattern) + "</message>");
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
    
    

}
