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

import java.io.IOException;
import java.io.InputStream;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.*;

import de.danielnaber.languagetool.JLanguageTool;

public abstract class RuleConverter {

	// indent strings
    protected static final String firstIndent = "  ";
    protected static final String secondIndent = "    ";
    protected static final String thirdIndent = "      ";
    protected static final String fourthIndent = "        ";
    protected static final int firstIndentInt = 2;
    protected static final int secondIndentInt = 4;
    protected static final int thirdIndentInt = 6;
    protected static final int fourthIndentInt = 8;
    
    protected String inFileName;
    protected String outFileName;
    protected String ruleType;
    
    // lists of rules
    protected List<? extends Object> ruleObjects;
    protected ArrayList<List<String>> allLtRules;
    protected ArrayList<List<String>> ltRules;
    protected ArrayList<List<String>> disambiguationRules;
    protected ArrayList<String> originalRuleStrings;
    protected ArrayList<String[]> warnings;	// list as long as allLtRules containing warning strings generating during rule conversion process
    
    // for auto-generating Id and name attributes
    protected int idIndex;
    protected int nameIndex;
    
    // these should be able to be set depending on the language
    protected String SENT_START = "SENT_START";
    protected String SENT_END = "SENT_END";
    
    // to check identities
    private static Pattern regex = Pattern.compile("[\\.\\^\\$\\*\\+\\?\\{\\}\\[\\]\\|\\(\\)]");
    
    public static String xmlHeader = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<?xml-stylesheet type=\"text/xsl\" href=\"../print.xsl\" ?>\n" +
        "<?xml-stylesheet type=\"text/css\" href=\"../rules.css\"\n" + 
        "title=\"Easy editing stylesheet\" ?>\n" +
        "<!--\n" +
        "English Grammar and Typo Rules for LanguageTool\n" +
        "See tagset.txt for the meaning of the POS tags\n" +
        "Copyright (C) 2001-2007 Daniel Naber (http://www.danielnaber.de)\n" +
        "$Id: grammar.xml,v 1.129 2010-11-13 23:24:21 dnaber Exp $\n" +
        "-->\n" +
        "<!--suppress CheckTagEmptyBody -->\n" +
        "<rules lang=\"en\" xsi:noNamespaceSchemaLocation=\"../rules.xsd\" xmlns:xsi=\"http://\n" +
        "www.w3.org/2001/XMLSchema-instance\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n";
    
    // basic constructor
    public RuleConverter() {  
    	idIndex = 0;
    	nameIndex = 0;
    }
    
    // constructor with input and output rule files
    public RuleConverter(String inFileName, String outFileName, String ruleType) {
        this.inFileName = inFileName;
        this.outFileName = outFileName;
        if (ruleType == null)     {
            this.ruleType = "default";
        } else {
            this.ruleType = ruleType;
        }
        idIndex = 0;
        nameIndex = 0;
    }
    
    public List<? extends Object> getRules() {return this.ruleObjects;}
    public ArrayList<List<String>> getAllLtRules() {return this.allLtRules;}
    public ArrayList<List<String>> getLtRules() {return this.ltRules;}
    public ArrayList<List<String>> getDisambiguationRules() {return this.disambiguationRules;}
    public ArrayList<String> getOriginalRuleStrings() {return this.originalRuleStrings;}
    public ArrayList<String[]> getWarnings() {return this.warnings;}
    public String getInFile() {return inFileName;}
    public String getOutFile() {return outFileName;}
    public String getFileType() {return ruleType;}
    public String getSentStart() {return this.SENT_START;}
    public String getSentEnd() {return this.SENT_END;}
    
    public void setInFile(String filename) {this.inFileName = filename;}
    public void setOutFile(String filename) {this.outFileName = filename;}
    public void setFileType(String fileType) {this.ruleType = fileType;}
    public void setSentStart(String sent_start) {this.SENT_START = sent_start;}
    public void setSentEnd(String sent_end) {this.SENT_END = sent_end;}
    
    // Abstract methods
    
    /**
     * The main method: parses the input file and populates the rule lists
     * @throws IOException
     */
    public abstract void parseRuleFile() throws IOException;
    
    /**
     * Takes a rule object and returns the original string representation of the rule
     * @param ruleObject: element from getRules()
     * @return
     */
    public abstract String getOriginalRuleString(Object ruleObject);
    /**
     * Takes a rule object (element from getRules()), an id, a name, and a rule type (this.ruleType) and returns a 
     * list of strings, the rule in LanguageTool format. Almost always called by getLtRules, etc methods
     * @param rule
     * @param id
     * @param name
     * @param type
     * @return
     */
    public abstract List<String> ltRuleAsList(Object rule, String id, String name, String type);
    
    public abstract String generateId(Object ruleObject);
    public abstract String generateName(Object ruleObject);
    /**
     * Returns a list of acceptable file types
     * @return
     */
    public abstract String[] getAcceptableFileTypes();
    
    /**
     * Returns true if the rule object is a disambiguation rule (i.e. should go into the disambiguation.xml file)
     * @param ruleObject
     * @return
     */
    public abstract boolean isDisambiguationRule(Object ruleObject);
    
    /**
     * Takes a LT rule list and elements of a token, and adds the proper <token> element to the rule list.
     * @param orig
     * @param token
     * @param postag
     * @param exceptions
     * @param careful
     * @param inflected
     * @param negate
     * @param skip
     * @param indent
     * @return
     */
    protected static ArrayList<String> addToken(ArrayList<String> orig, String token, String postag, String exceptions, 
    											boolean careful, boolean inflected, boolean negate, int skip, int indent) {
        String space = getSpace(indent);
        
        // fix the case of the "everything" token
        if (token.equals(".*")) {
        	token = "";
        }
        
        String inflectedString = "";
        if (inflected) {
        	inflectedString = " inflected=\"yes\"";
        }
        String skipString = "";
        if (skip == -1) {
        	skipString = " skip=\"-1\"";
        }
        String regexpString = "";
        if (isRegex(token)) {
        	regexpString = " regexp=\"yes\"";
        }
        String exceptionString = "";
        if (exceptions != null) {
        	if (exceptions.contains("<exception")) {
        		exceptionString = exceptions;
        	} else {
        		exceptionString = "<exception regexp=\"yes\">" + exceptions + "</exception>";
        	}
        }
        String postagRegexp = "";
        if (isRegex(postag)) {
        	postagRegexp = " postag_regexp=\"yes\"";
        }
        String postagString = "";
        if (postag != null) {
        	if (!postag.isEmpty()) {
        		postagString = " postag=\"" + postag + "\"";
        	}
        }
        String carefulString = "";
        if (careful) {
        	carefulString = "<exception" + postagString + postagRegexp + " negate_pos=\"yes\"/>";
        }
        String negateString = "";
        if (!token.isEmpty() && negate) {
        	negateString = " negate=\"yes\"";
        }
        String negatePosString = "";
        if (!postagString.isEmpty() && negate) {
        	negatePosString = " negate_pos=\"yes\"";
        }
        
       	orig.add(space + "<token" + inflectedString + skipString + regexpString + postagString + postagRegexp + negateString + negatePosString + ">" + token + carefulString + exceptionString + "</token>");
        
        return orig;
    }
    
    /**
     * Takes a file and returns it as a list of strings, blank lines omitted
     */
    public static ArrayList<String> fileToListNoBlanks(String filename) {
        ArrayList<String> returnList = new ArrayList<String>();
        Scanner in = null;
        InputStream is = null;
        try {
        	is = JLanguageTool.getDataBroker().getFromResourceDirAsStream(filename);
            in = new Scanner(is);
            while (in.hasNextLine()) {
                String line = in.nextLine();
                if (!line.equals("") && !line.equals("\n")) {
                    returnList.add(line);
                }
            } 
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            in.close();
        }
        return returnList;
    }
    
    
    
    /**
     * returns if the string contains a character that might indicate it's a regex
     */
    protected static boolean isRegex(String e) {
        if (e == null) {
        	return false;
        } 
    	Matcher m = regex.matcher(e);
        return m.find();        
    }
    
    protected static String getSpace(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<indent;i++) {
            sb.append(' ');
        }
        return sb.toString();
    }
    
    public static String getRuleStringFromList(List<String> rule) {
    	StringBuilder sb = new StringBuilder();
    	for (String line : rule) {
    		sb.append(line);
    		sb.append('\n');
    	}
    	return sb.toString();
    }
    
    // ** Helpers to "or" sets of words together
    
    public static String glueWords(ArrayList<String> words) {
		StringBuilder sb = new StringBuilder();
		if (words == null) {
			return "";
		}
		for (String word : words) {
			sb.append(word);
			sb.append("|");
		}
		String str = sb.toString();
		if (str.length() > 1) {
			return str.substring(0,str.length()-1);
		} else {
			return str;
		}
	}
	
	public static String glueWords(String[] words) {
		StringBuilder sb = new StringBuilder();
		if (words == null) {
			return "";
		}
		for (String word : words) {
			sb.append(word);
			sb.append("|");
		}
		String str = sb.toString();
		if (str.length() > 1) {
			return str.substring(0,str.length()-1);
		} else {
			return str;
		}
	}
    
    
    
}
