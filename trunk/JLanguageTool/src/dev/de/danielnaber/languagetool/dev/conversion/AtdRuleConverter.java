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
        // for the "avoid" rules
        if (type.equals("avoid")) {
            if (Boolean.parseBoolean(rule.get("casesensitive"))) {
                ltRule.add(secondIndent + "<pattern case_sensitive=\"yes\">");
            } else {
                ltRule.add(secondIndent + "<pattern>");
            }            
            String[] pattern = rule.get("pattern").split("\\ +");
            if (isSpecialApostropheCase(pattern)) {
            	List<HashMap<String,String>> twoRules = splitOffRegularWords(rule);
            	List<String> twoRulesList = new ArrayList<String>();
            	for (HashMap<String,String> map : twoRules) {
            		List<String> singleRule = ltRuleAsList(map, RuleConverter.getSuitableID(map), RuleConverter.getSuitableName(map), type);
            		for (String line : singleRule) {
            			twoRulesList.add(line);
            		}
            	}
            	return twoRulesList;
            }
            for (int i=0;i<pattern.length;i++) {
                // for proper handling of apostrophes
                String e = pattern[i];
                if (e.contains("'") && !e.contains("|")) {
                    String[] temp = e.replaceAll("'", " ' ").split("\\ +");
                    for (String sTemp : temp) {
                        ltRule = addTokenHelper(ltRule,sTemp,thirdIndentInt);
                    }
                } else if (e.contains("'") & e.contains("|")) {
                	String[] temp = e.split("\\|");
                	String prefixes = "";
                	String suffix = "";
                	for (String sTemp : temp) {
                		String[] splitSTemp = sTemp.split("'");
                		prefixes = prefixes + splitSTemp[0] + "|";
                		suffix = splitSTemp[1];
                	}
                	prefixes = prefixes.substring(0, prefixes.length() - 1);
                	ltRule = addTokenHelper(ltRule, prefixes, thirdIndentInt);
                	ltRule = addTokenHelper(ltRule, "'", thirdIndentInt);
                	ltRule = addTokenHelper(ltRule, suffix, thirdIndentInt);

                } else {
                    ltRule = addTokenHelper(ltRule,e,thirdIndentInt);
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
            if (isSpecialApostropheCase(pattern)) {
            	List<HashMap<String,String>> twoRules = splitOffRegularWords(rule);
            	List<String> twoRulesList = new ArrayList<String>();
            	for (HashMap<String,String> map : twoRules) {
            		List<String> singleRule = ltRuleAsList(map, RuleConverter.getSuitableID(map), RuleConverter.getSuitableName(map), type);
            		for (String line : singleRule) {
            			twoRulesList.add(line);
            		}
            	}
            	return twoRulesList;
            }
            for (int i=0;i<pattern.length;i++) {
                // for proper handling of apostrophes
                String e = pattern[i];
                if (e.contains("'") && !e.contains("|")) {
                    String[] temp = e.replaceAll("'", " ' ").split("\\ +");
                    for (String sTemp : temp) {
                        ltRule = addTokenHelper(ltRule,sTemp,thirdIndentInt);
                    }
                } else if (e.contains("'") & e.contains("|")) {
                	String[] temp = e.split("\\|");
                	String prefixes = "";
                	String suffix = "";
                	for (String sTemp : temp) {
                		String[] splitSTemp = sTemp.split("'");
                		prefixes = prefixes + splitSTemp[0] + "|";
                		suffix = splitSTemp[1];
                	}
                	prefixes = prefixes.substring(0, prefixes.length() - 1);
                	ltRule = addTokenHelper(ltRule, prefixes, thirdIndentInt);
                	ltRule = addTokenHelper(ltRule, "'", thirdIndentInt);
                	ltRule = addTokenHelper(ltRule, suffix, thirdIndentInt);

                } else {
                    ltRule = addTokenHelper(ltRule,e,thirdIndentInt);
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
    
 // returns true if the rule contains and or pattern that has both regular and apostrophed words
    // (e.g. would|could|wouldn't|couldn't)
    // shouldn't match or patterns with just apostrophes (e.g. wouldn't|couldn't)
    public boolean isSpecialApostropheCase(String[] pattern) {
    	for (String s : pattern) {
    		if (s.contains("|") && s.contains("'")) {
    			String[] ss = s.split("\\|");
    			for (String sss : ss) {
    				if (!sss.contains("'")) {
    					return true;
    				}
    			}
    		}
    	}
    	return false;
    }
    
    public boolean isApostropheCase(String[] pattern) {
    	for (String s : pattern) {
    		if (s.contains("|") && s.contains("'")) {
    			String[] ss = s.split("\\|");
    			HashSet<String> suffixes = new HashSet<String>();
    			for (String sss : ss) {
    				String[] ssss = sss.split("'");
    				suffixes.add(ssss[ssss.length - 1]);
    			}
    			if (suffixes.size() == 1) {
    				return true;
    			}
    		}
    	}
    	return false;
    }
    
    public List<HashMap<String,String>> splitOffRegularWords(HashMap<String,String> rule) {
    	List<HashMap<String,String>> rulesList = new ArrayList<HashMap<String,String>>();
    	String pattern = rule.get("pattern");
    	String[] splitPattern = pattern.split("\\ +");
    	String regularPattern = "";
    	String apostrophedPattern = "";
    	for (String sp : splitPattern) {
    		if (sp.contains("|") && sp.contains("'")) {
    			List<String> apostrophedWords = new ArrayList<String>();
    			List<String> regularWords = new ArrayList<String>();
    			String[] temp = sp.split("\\|");
    			for (String sTemp : temp) {
    				if (sTemp.contains("'")) {
    					apostrophedWords.add(sTemp);
    				} else {
    					regularWords.add(sTemp);
    				}
    			}
    			for (int i=0;i<regularWords.size() - 1;i++) {
    				regularPattern = regularPattern + regularWords.get(i) + "|";
    			}
    			regularPattern = regularPattern + regularWords.get(regularWords.size() - 1);
    			for (int i=0;i<apostrophedWords.size() - 1;i++) {
    				apostrophedPattern = apostrophedPattern + apostrophedWords.get(i) + "|";
    			}
    			apostrophedPattern = apostrophedPattern + apostrophedWords.get(apostrophedWords.size() - 1);
    			regularPattern = regularPattern + " ";
    			apostrophedPattern = apostrophedPattern + " ";
    		} else {
    			regularPattern = regularPattern + sp + " ";
    			apostrophedPattern = apostrophedPattern + sp + " ";
    		}
    	}
    	regularPattern = regularPattern.trim();
    	apostrophedPattern = apostrophedPattern.trim();
    	HashMap<String,String> regularRule = new HashMap<String,String>();
    	HashMap<String,String> apostrophedRule = new HashMap<String,String>();
    	for (String key : rule.keySet()) {
    		regularRule.put(key, rule.get(key));
    		apostrophedRule.put(key, rule.get(key));
    	}
    	regularRule.put("pattern", regularPattern);
    	apostrophedRule.put("pattern", apostrophedPattern);
    	rulesList.add(regularRule);
    	rulesList.add(apostrophedRule);
    	
    	return rulesList;
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
    
    public ArrayList<String> addTokenHelper(ArrayList<String> ltRule, String e, int spaces) {
        // special cases of start and end of sentence anchors
        if (e.equals("0BEGIN.0")) {
            ltRule = addToken(ltRule, null, null, "sentstart", thirdIndentInt);
            return ltRule;
        }
        if (e.equals("0END.0")) {
        	ltRule = addToken(ltRule, null, null, "sentend", thirdIndentInt);
        	return ltRule;
        }
        if (hasPosTag(e)) {
            String[] parts = e.split("/");
            parts[1] = fixNoun(parts[1]);
            if (parts[0].equals(".*")) {
                if (isRegex(parts[1])) {
                    ltRule = addToken(ltRule, null, parts[1], "regexpostag", thirdIndentInt);
                } else {
                    ltRule = addToken(ltRule, null, parts[1], "postag", thirdIndentInt);
                }
                
            } else {
                if (isRegex(parts[0])) {
                    if (isRegex(parts[1])) {
                        ltRule = addToken(ltRule, parts[0], parts[1], "regexandregexpostag", thirdIndentInt);
                    } else {
                        ltRule = addToken(ltRule, parts[0], parts[1], "regexandpostag", thirdIndentInt);
                    }
                } else {
                    if (isRegex(parts[1])) {
                        ltRule = addToken(ltRule, parts[0], parts[1], "tokenandregexpostag", thirdIndentInt);
                    } else {
                        ltRule = addToken(ltRule, parts[0], parts[1], "tokenandpostag", thirdIndentInt);
                    }
                }
                
            }
        } else {
            if (isRegex(e)) {
                ltRule = addToken(ltRule, e, null, "regextoken", thirdIndentInt);
            } else if (isMacro(e)) {
                ltRule = addToken(ltRule, expandMacro(e), null, "regextoken", thirdIndentInt);
            } else {
                ltRule = addToken(ltRule, e, null, "token", thirdIndentInt);
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
        	if (hasSpecificPosTag(pattern[numMatched],"NNPS")) {
        		retString = "<match no=\"" + Integer.toString(numMatched + 1) + "\" postag=\"NNP\" />";
        	} else {
        		retString = "<match no=\"" + Integer.toString(numMatched + 1) + "\" postag=\"NN\" />";
        	}
        }
        else if (transform.equals("plural")) {
            if (hasSpecificPosTag(pattern[numMatched],"NNP")) {
            	retString = "<match no=\"" + Integer.toString(numMatched + 1) + "\" postag=\"NNPS\" />";
            } else {
            	retString = "<match no=\"" + Integer.toString(numMatched + 1) + "\" postag=\"NNS\" />";
            }
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
