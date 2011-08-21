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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Random;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryIterator;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;
import morfologik.stemming.WordData;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.rules.patterns.Element;
import de.danielnaber.languagetool.rules.patterns.PatternRule;
import de.danielnaber.languagetool.rules.patterns.PatternRuleLoader;
import de.danielnaber.languagetool.dev.conversion.RuleConverter;

public class RuleCoverage {

    private JLanguageTool tool;    
    private DictionaryIterator dictIterator;
    private DictionaryLookup dictLookup;
    String fileName = "." + JLanguageTool.getDataBroker().getResourceDir() + "/en/english.dict";
    private File dictFile = new File(fileName);
    
    private String ruleFileHeader = RuleConverter.xmlHeader;
    private String categoriesString = "<category name=\"test\">";
    private String endCategoriesString = "</category>";
    private String endRulesString = "</rules>"; 
    
    
    public RuleCoverage(Language language) throws IOException {
        tool = new JLanguageTool(language);
        tool.activateDefaultPatternRules();
        tool.disableRule("UPPERCASE_SENTENCE_START");
        tool.disableRule("EN_UNPAIRED_BRACKETS");
        tool.disableRule("EN_A_VS_AN");
        dictLookup = (DictionaryLookup) loadDictionary(); 
        dictIterator = resetDictIter();
    }
    
    public RuleCoverage() throws IOException {
    	tool = new JLanguageTool(Language.ENGLISH);
        tool.activateDefaultPatternRules();
        tool.disableRule("UPPERCASE_SENTENCE_START");
        tool.disableRule("EN_UNPAIRED_BRACKETS");
        tool.disableRule("EN_A_VS_AN");
        dictLookup = (DictionaryLookup) loadDictionary();
        dictIterator = resetDictIter();
    }
    
    // for testing purposes
    public RuleCoverage(String dictFileName) throws IOException {
    	tool = new JLanguageTool(Language.ENGLISH);
        tool.activateDefaultPatternRules();
        tool.disableRule("UPPERCASE_SENTENCE_START");
        tool.disableRule("EN_UNPAIRED_BRACKETS");
        tool.disableRule("EN_A_VS_AN");
        this.fileName = dictFileName;
        this.dictFile = new File(fileName);
        dictLookup = (DictionaryLookup) loadDictionary();
        dictIterator = resetDictIter();
    }
    
    public JLanguageTool getLanguageTool() {
    	return tool;
    }
    
    public void evaluateRules(String grammarfile) throws IOException {
        List<PatternRule> rules = loadPatternRules(grammarfile);
        for (PatternRule rule : rules) {
            String example = generateExample(rule);
            System.out.println("Rule " + rule.getId() + " is covered by " + isCoveredBy(example) + " for example " + example);
        }
    }
    
    public void splitOutCoveredRules(String grammarfile, String discardfile) throws IOException {
    	List<PatternRule> rules = loadPatternRules(grammarfile);
    	
    	PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(grammarfile),"UTF-8"));
    	PrintWriter w2 = null;
    	int discardedRules = 0;
    	
        
    	for (PatternRule rule : rules) {
    		String example = generateExample(rule);
    		if (isCoveredBy(example) == null) {
    			w.write(rule.toXML());
    		} else {
    			if (w2 == null) {
    				w2 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(discardfile),"UTF-8")); 
    			}
    			discardedRules++;
    			w2.write(rule.toXML());
    		}
    	}
    	
    	if (discardedRules > 0) {
    		System.out.println(Integer.toString(discardedRules) + " rules already covered, written to " + discardfile);
    	}
    	w.close();
    	if (w2 != null) {
    		w2.close();
    	}
    }
    
    /**
     * Returns true if the input string is covered by an existing JLanguageTool error 
     * @param str: input error string
     * @return: true if (entire) string is considered an error, false o.w.; this doesn't work
     * @throws IOException
     */
    public boolean isCovered(String str) throws IOException {
        List<RuleMatch> matches = tool.check(str);
        return (matches.size() > 0);        
    }
    
//    public String isCoveredBy(String str) throws IOException {
//        List<RuleMatch> matches = tool.check(str);
//        if (matches.size() > 0) {
//            return matches.get(0).getRule().getId();
//        }
//        return null;
//    }
    
    public String[] isCoveredBy(String str) throws IOException {
    	List<RuleMatch> matches = tool.check(str);
    	ArrayList<String> coverages = new ArrayList<String>();
    	if (matches.size() > 0) {
    		for (RuleMatch match : matches) {
    			coverages.add(match.getRule().getId());
    		}
    	}
    	return coverages.toArray(new String[coverages.size()]);
    }
    
//    public String isCoveredBy(PatternRule rule) throws IOException {
//    	String example = generateExample(rule);
//    	List<RuleMatch> matches = tool.check(example);
//    	if (matches.size() > 0) {
//    		return matches.get(0).getRule().getId();
//    	}
//    	return "";
//    }
    
    public String[] isCoveredBy(PatternRule rule) throws IOException {
    	ArrayList<String> coverages = new ArrayList<String>();
    	String example = generateExample(rule);
    	List<RuleMatch> matches = tool.check(example);
    	if (matches.size() > 0) {
    		for (RuleMatch match : matches) {
    			coverages.add(match.getRule().getId());
    		}
    	}
    	return coverages.toArray(new String[coverages.size()]);
    }
    
    public ArrayList<String[]> isCoveredBy(List<PatternRule> rules) throws IOException {
    	ArrayList<String[]> coverages = new ArrayList<String[]>();
    	for (PatternRule rule : rules) {
    		String[] cov = isCoveredBy(rule);
    		coverages.add(cov);
    	}
    	return coverages;
    }
    
    /**
     * Generates an error string that matches the given PatternRule object 
     * @param pattern
     * @return
     */
    public String generateExample(PatternRule pattern) {
        StringBuilder sb = new StringBuilder();
        List<Element> elements = pattern.getElements();
        for (Element e : elements) {
            sb.append(getSpecificExample(e) + " ");
        }
        // it's okay to not deal with apostrophes as long as we turn off the unpaired brackets rule
        return sb.toString().trim();
    }
    
    /**
     * Generates a word that matches the given Element 
     * @param e
     * @return
     */
    public String getSpecificExample(Element e) {
        String token = e.getString();
        List<Element> exceptions = e.getExceptionList();
        if (exceptions == null) {
            exceptions = new ArrayList<Element>();
        }
        if (e.isSentStart()) {
            return "";
        }
        // <token>word</token>
        if (!token.isEmpty() && !e.isRegularExpression()) {
            return token;
        // all other token types
        }
        
        // just in case there's no example, stop after a ridiculous number (several turns through the dictionary)
        // need smarter example generation, especially for simple or-ed lists of words. 
        if (!token.isEmpty() && isSimpleOrRegex(e)) {
        	// pick an element from the or-ed list at random
        	return randomOredElement(e);
        }
        
        int count = 0;
        while (count < 400000) {
        	if (!dictIterator.hasNext()) {
        		dictIterator = resetDictIter();
        	}
            String word = dictIterator.next().getWord().toString();
            if (isExampleOf(word, e) && !inExceptionList(word, exceptions)) {
                return word;
            }
            count++;
        } 
        return null;
    }
    
    /** 
     * Returns true if the element is an or-ed list of words, without a specified pos-tag.
     * e.g. can|could|would|should
     * @param e
     * @return
     */
    private boolean isSimpleOrRegex(Element e) {
    	// any number of conditions that could halt this check
    	if (e.getPOStag() != null) return false;
    	if (e.getNegation()) return false;
    	if (e.isInflected()) return false;
    	if (!e.isRegularExpression()) return false;
    	if (e.hasAndGroup()) return false;
    	if (e.hasExceptionList()) return false;
    	if (e.isReferenceElement()) return false;
    	if (e.isSentStart()) return false;
    	
    	String token = e.getString();
    	String[] ors = token.split("\\|");
    	for (String s : ors) {
    		if (RuleConverter.isRegex(s)) {
    			return false;
    		}
    	}
    	return true;
    }
    
    /**
     * Returns a random one of the or-ed elements. Random seems like the right thing to do here.
     * Only applied to simple or-ed lists of words, e.g. this|that|those
     * @param e
     * @return
     */
    private String randomOredElement(Element e) {
    	String[] split = e.getString().split("\\|");
    	Random rng = new Random();
    	int index = rng.nextInt(split.length);
    	return split[index];
    }
    
    /**
     * True if the given word matches one of the exceptions
     * @param word
     * @param exceptions
     * @return
     */
    public boolean inExceptionList(String word, List<Element> exceptions) {
        for (Element element : exceptions) {
            if (isExampleOf(word, element)) {
                return true;
            }
        }
        return false;        
    }
    
    /**
     * True if given word is an example of element
     * @param word
     * @param element
     * @return
     */
    public boolean isExampleOf(String word, Element element) {
        String token = element.getString();
        String posTag = element.getPOStag();
        boolean isTokenEmpty = token.isEmpty();
        boolean hasPosTag = (posTag != null);
        boolean negate = element.getNegation();
        boolean postagNegate = element.getPOSNegation();
        boolean tokenMatches = true;
        boolean postagMatches = true;
        if (!isTokenEmpty) {
            Pattern p = Pattern.compile(token);
            Matcher m = p.matcher(word);
            if (m.matches()) {
                if (negate) {
                    tokenMatches = false; 
                }
            } else {
                if (!negate) {
                    tokenMatches = false;
                }
            }
        }
        if (hasPosTag) {
            Pattern p = Pattern.compile(posTag);
            List<String> postags = getPosTags(word);
            for (String s : postags) {
                Matcher m = p.matcher(s);
                if (m.matches()) {
                    if (postagNegate) {
                        postagMatches = false;
                    }
                } else {
                    if (!postagNegate) {
                        postagMatches = false;
                    }
                }
            }
            if (postags.size() == 0) {
                postagMatches = false;
            }
            
        }
        return (tokenMatches && postagMatches);
    }
    
    /**
     * Returns a list of the word's POS tags
     * @param word
     * @return
     */
    public List<String> getPosTags(String word) {
        List<WordData> lwd = dictLookup.lookup(word);
        ArrayList<String> postags = new ArrayList<String>();
        for (WordData wd : lwd) {
            postags.add(wd.getTag().toString());
        }
        return postags;
    }
    
    public DictionaryIterator resetDictIter() {
        DictionaryIterator ret = null;
        try {
            ret = new DictionaryIterator(Dictionary.read(dictFile), Charset.forName("utf8").newDecoder(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;        
    }
    
    public IStemmer loadDictionary() {
        IStemmer dictLookup = null;
        try {
            dictLookup = new DictionaryLookup(Dictionary.read(dictFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dictLookup;
    }
    
    public List<PatternRule> loadPatternRules(final String filename)
        throws IOException {
      final PatternRuleLoader ruleLoader = new PatternRuleLoader();
      InputStream is = this.getClass().getResourceAsStream(filename);
      if (is == null) {
          // happens for external rules plugged in as an XML file:
          is = new FileInputStream(filename);
      }
      return ruleLoader.getRules(is, filename);
    }
    
    public List<PatternRule> parsePatternRule(final String ruleString) {
    	final PatternRuleLoader ruleLoader = new PatternRuleLoader();
    	String ruleFileString = ruleFileHeader + categoriesString + ruleString + endCategoriesString + endRulesString;
    	InputStream is = new ByteArrayInputStream(ruleFileString.getBytes());
    	try {
    		return ruleLoader.getRules(is,null);
    	} catch (IOException e) {
    		return new ArrayList<PatternRule>();
    	}
    }
    
    
    
    
}
