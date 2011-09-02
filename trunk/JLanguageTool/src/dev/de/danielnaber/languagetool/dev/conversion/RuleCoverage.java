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

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.rules.patterns.Element;
import de.danielnaber.languagetool.rules.patterns.Match;
import de.danielnaber.languagetool.rules.patterns.PatternRule;
import de.danielnaber.languagetool.rules.patterns.PatternRuleLoader;
import de.danielnaber.languagetool.dev.conversion.RuleConverter;

public class RuleCoverage {

    private JLanguageTool tool;    
    private DictionaryIterator dictIterator;
    private DictionaryLookup dictLookup;
    private Language language;
    private String filename;
    private File dictFile;
    
    private String ruleFileHeader = RuleConverter.xmlHeader;
    private String categoriesString = "<category name=\"test\">";
    private String endCategoriesString = "</category>";
    private String endRulesString = "</rules>"; 
    
    private static Pattern regexSet = Pattern.compile("^\\[([^\\-])*?\\]$");

    // default constructor; defaults to English
    public RuleCoverage() throws IOException {
    	language = Language.ENGLISH;
    	tool = new JLanguageTool(language);
        tool.activateDefaultPatternRules();
        tool.disableRule("UPPERCASE_SENTENCE_START");
        tool.disableRule("EN_UNPAIRED_BRACKETS");
        tool.disableRule("EN_A_VS_AN");
        setupDictionaryFiles();
    }
    
    // disable some of the default rules in the constructors
    //TODO: disable the right rules for each language
    // though this matters less when we return an array of all covering rules
    public RuleCoverage(Language language) throws IOException {
    	this.language = language;
    	tool = new JLanguageTool(language);
        tool.activateDefaultPatternRules();
        setupDictionaryFiles();
    }
    
    // for testing purposes, defaults to English
    public RuleCoverage(String dictFileName) throws IOException {
    	language = Language.ENGLISH;
    	tool = new JLanguageTool(language);
        tool.activateDefaultPatternRules();
        tool.disableRule("UPPERCASE_SENTENCE_START");
        tool.disableRule("EN_UNPAIRED_BRACKETS");
        tool.disableRule("EN_A_VS_AN");
        this.filename = dictFileName;
        this.dictFile = new File(filename);
        setupDictionaryFiles();
    }
    
    public JLanguageTool getLanguageTool() {
    	return tool;
    }

    // not really used anymore
    public void evaluateRules(String grammarfile) throws IOException {
        List<PatternRule> rules = loadPatternRules(grammarfile);
        for (PatternRule rule : rules) {
            String example = generateIncorrectExample(rule);
            System.out.println("Rule " + rule.getId() + " is covered by " + isCoveredBy(example) + " for example " + example);
        }
    }
    
    // not really used anymore
    public void splitOutCoveredRules(String grammarfile, String discardfile) throws IOException {
    	List<PatternRule> rules = loadPatternRules(grammarfile);
    	
    	PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(grammarfile),"UTF-8"));
    	PrintWriter w2 = null;
    	int discardedRules = 0;
    	
        
    	for (PatternRule rule : rules) {
    		String example = generateIncorrectExample(rule);
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
    
    /**
     * Returns a list of covering rules for the given example string
     */
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
    
    public String[] isCoveredBy(PatternRule rule) throws IOException {
    	ArrayList<String> coverages = new ArrayList<String>();
    	String example = generateIncorrectExample(rule);
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
    public String generateIncorrectExample(PatternRule patternrule) {
        ArrayList<String> examples = new ArrayList<String>();
        List<Element> elements = patternrule.getElements();
        for (int i=0;i<elements.size();i++) {
        	List<Element> prevExceptions;
        	if (i == elements.size()-1) {
        		prevExceptions = new ArrayList<Element>();
        	} else {
        		prevExceptions = elements.get(i+1).getPreviousExceptionList();
        		if (prevExceptions == null) prevExceptions = new ArrayList<Element>();
        	}
            examples.add(getSpecificExample(elements.get(i),prevExceptions,elements,examples));
        }
        // it's okay to not deal with apostrophes as long as we turn off the unpaired brackets rule, for English at least
        StringBuilder sb = new StringBuilder();
        //TODO: doesn't deal with spacebefore=no
        for (String example : examples) {
        	sb.append(example + " ");
        }
        String s = sb.toString().replaceAll("\\ \\.\\ ", ".").trim();	// to fix the period problem 
        return s;
    }
    
    // Not using this method yet
//    public String generateCorrectExample(PatternRule patternrule) {
//    	String incorrectExample = generateIncorrectExample(patternrule);
//    	AnalyzedSentence analyzedSentence = null;
//    	try {
//    		analyzedSentence = tool.getAnalyzedSentence(incorrectExample);
//    		RuleMatch[] ruleMatches = patternrule.match(analyzedSentence);
//    		for (RuleMatch rm : ruleMatches) {
//    			patternrule.addRuleMatch(rm);
//    		}
//    	} catch (IOException e) {
//    		e.printStackTrace();
//    	}
//
//    	ArrayList<String> examples = new ArrayList<String>();
//    	List<Match> matches = patternrule.getSuggestionMatches();
//    	ArrayList<Element> elements = new ArrayList<Element>();
//    	for (Match m : matches) {
//    		int ref = m.getTokenRef();
//    		Element refElement = patternrule.getElements().get(ref);
//    		elements.add(refElement);
//    	}
//    	for (int i=0;i<elements.size();i++) {
//        	List<Element> prevExceptions;
//        	if (i == elements.size()-1) {
//        		prevExceptions = new ArrayList<Element>();
//        	} else {
//        		prevExceptions = elements.get(i+1).getPreviousExceptionList();
//        		if (prevExceptions == null) prevExceptions = new ArrayList<Element>();
//        	}
//            examples.add(getSpecificExample(elements.get(i),prevExceptions,elements,examples));
//        }
//        // it's okay to not deal with apostrophes as long as we turn off the unpaired brackets rule, for English at least
//        StringBuilder sb = new StringBuilder();
//        //TODO: doesn't deal with spacebefore=no
//        for (String example : examples) {
//        	sb.append(example + " ");
//        }
//        String s = sb.toString().replaceAll("\\ \\.\\ ", ".").trim();	// to fix the period problem 
//        return s;
//    }
//    
    
    /**
     * Generates a word that matches the given Element 
     * @param e
     * @return
     */
    //TODO: doesn't deal with skipped tokens
    @SuppressWarnings("unchecked")
	public String getSpecificExample(Element e, List<Element> prevExceptions, List<Element> elements, ArrayList<String> examples) {
        // if this is part of (the first of) a list of and-ed tokens
    	if (e.hasAndGroup()) {
        	List<Element> andGroup = e.getAndGroup();
        	andGroup.add(e); // add the token itself to the and group, so we can process them together
        	// still, if one of the tokens in the and group is just a (non-regexp) token, we can return that as the example
        	for (Element and : andGroup) {
        		if (isJustToken(and)) {
        			return and.getString();
        		}
        		if (isPunctuation(and)) {
        			return getOnePunc(and);
        		}
        	}
        	// get the patterns of all the and-ed elements, to make processing faster
        	ArrayList<Pattern> tokenPatterns = new ArrayList<Pattern>(andGroup.size());
        	ArrayList<Pattern> posPatterns = new ArrayList<Pattern>(andGroup.size());
        	// get all the exceptions and attributes
        	ArrayList<Element> allExceptions = new ArrayList<Element>();
        	allExceptions.addAll(prevExceptions);	// add all the exceptions from the next token with scope="previous"
        	for (int a=0;a<andGroup.size();a++) {
        		Element and = andGroup.get(a);
        		List<Element> ex = and.getExceptionList();
        		if (ex != null) {
        			allExceptions.addAll(and.getExceptionList());
        		}
        		if (and.isReferenceElement()) {
        			and = getReferenceElement(and,elements,examples);	// gets the string for the element if it's a match token
        		}
        		String andPostag = and.getPOStag();
        		String andToken = and.getString();
        		tokenPatterns.add(Pattern.compile(andToken));
        		if (andPostag != null) {
        			if (and.isPOStagRegularExpression()) {
        				posPatterns.add(Pattern.compile(andPostag));
        			} else {
        				posPatterns.add(Pattern.compile(Pattern.quote(andPostag)));
        			}
        			
        		} else {
        			posPatterns.add(null);
        		}
        		andGroup.set(a,and);
        	}
        	// get exceptions in attribute form for faster processings
        	ArrayList<ArrayList> exceptionAttributes = getExceptionAttributes(allExceptions);
        	
        	// do the dictionary iteration thing; this part could take a while, depending on how far through the dict we have to go
        	int numResets = 0;
            while (numResets < 2) {
            	if (!dictIterator.hasNext()) {
            		dictIterator = resetDictIter();
            		numResets++;
            	}
                String word = dictIterator.next().getWord().toString();
                // check if the word meets all the and-ed criteria
                boolean matched = true;
                for (int i=0;i<andGroup.size();i++) {
                	if (!isExampleOf(word, tokenPatterns.get(i), posPatterns.get(i), andGroup.get(i))) {
                		matched = false;
                		break;
                	}
                }
                if (matched) {
                	if (!inExceptionList(word,exceptionAttributes,allExceptions)) {
                		return word;
                	}
                } 
            } 
        } 
    	// just a single (non-and-ed) token
    	else {
    		if (e.isReferenceElement()) {
    			e = getReferenceElement(e, elements, examples);
    		}
        	String token = e.getString();
        	String postag = e.getPOStag();
            List<Element> exceptions = e.getExceptionList();
            if (exceptions == null) {
            	exceptions = new ArrayList<Element>();
            }
            exceptions.addAll(prevExceptions);
            
            ArrayList<ArrayList> exceptionAttributes = getExceptionAttributes(exceptions);

            if (e.isSentStart()) {
                return "";
            }
            // <token>word</token>
            if (isJustToken(e)) {
                return token;
            }
            if (isPunctuation(e)) {
    			return getOnePunc(e);
    		}
            
            // need smarter example generation, especially for simple or-ed lists of words. 
            if (isSimpleOrRegex(e)) {
            	// pick an element from the or-ed list at random
            	return randomOredElement(e);
            }
            
            Pattern tokenPattern = Pattern.compile(token);
            Pattern posPattern;
            if (postag != null) {
            	if (e.isPOStagRegularExpression()) {
            		posPattern = Pattern.compile(postag);
            	} else {
            		posPattern = Pattern.compile(Pattern.quote(postag));
            	}
            	
            	if (postag.equals("SENT_END")) {
            		posPattern = null;
            	}
            	
            } else {
            	posPattern = null;
            }
            
            // only allows approx. one pass through the dictionary
            int numResets = 0;
            while (numResets < 2) {
            	if (!dictIterator.hasNext()) {
            		dictIterator = resetDictIter();
            		numResets++;
            	}
                String word = dictIterator.next().getWord().toString();
                if (isExampleOf(word, tokenPattern, posPattern, e) &&
                	!inExceptionList(word, exceptionAttributes, exceptions)) {
                    return word;
                }
            } 
        }
   
        return null;	// if no example can be found
    }
    
    /**
     * Returns an element with the string set as the previously matched element
     * @param e
     * @param elements
     * @param examples
     * @return
     */
    private Element getReferenceElement(Element e, List<Element> elements, ArrayList<String> examples) {
    	int r = e.getMatch().getTokenRef();
    	Element newElement = new Element(examples.get(r), elements.get(r).getCaseSensitive(), false, false);
    	newElement.setNegation(e.getNegation());
    	return newElement;
    	
    }
    
    /**
     * Gets all the attributes of each element of the exception, so we don't have to keep compiling the Pattern,
     * which wastes a lot of time
     * @param exceptions
     * @return
     */
    @SuppressWarnings("unchecked")
	private ArrayList<ArrayList> getExceptionAttributes(List<Element> exceptions) {
    	if (exceptions.size() == 0) {
    		return new ArrayList<ArrayList>();
    	} 
    	int size = exceptions.size();
    	ArrayList<ArrayList> ret = new ArrayList<ArrayList>(6);
    	ArrayList<Pattern> tokenPatterns = new ArrayList<Pattern>(size);
    	ArrayList<Pattern> posPatterns = new ArrayList<Pattern>(size);
    	for (Element e : exceptions) {
    		String token = e.getString();
    		String postag = e.getPOStag();
    		Pattern tokenPattern = Pattern.compile(token);
    		Pattern posPattern;
            if (postag != null) {
            	posPattern = Pattern.compile(postag);
            } else {
            	posPattern = null;
            }
            
            tokenPatterns.add(tokenPattern);
            posPatterns.add(posPattern);
            
    	}
    	ret.add(tokenPatterns);
    	ret.add(posPatterns);
    	return ret;
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
     * Faster version of inExceptionList, because we don't have to re-compile the Patterns for the exception elements
     * @param word
     * @param exceptionAttributes
     * @param numExceptions
     * @return
     */
    @SuppressWarnings("unchecked")
	private boolean inExceptionList(String word, ArrayList<ArrayList> exceptionAttributes, List<Element> exceptions) {
    	if (exceptions.size() == 0) {
    		return false;
    	}
    	ArrayList<Pattern> tokenPatterns = exceptionAttributes.get(0);
    	ArrayList<Pattern> posPatterns = exceptionAttributes.get(1);
    	
    	for (int i=0;i<exceptions.size();i++) {
    		Element curException = exceptions.get(i);
    		if (isExampleOf(word,tokenPatterns.get(i),
    				posPatterns.get(i),
    				curException)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    
    /**
     * Faster version of isExampleOf, since you don't have to recompile the Patterns every time
     * @param word
     * @param tokenPattern
     * @param posPattern
     * @param isTokenEmpty
     * @param hasPosTag
     * @param negate
     * @param postagNegate
     * @return
     */
    public boolean isExampleOf(String word, Pattern tokenPattern, Pattern posPattern, Element e) {
    	if (tokenPattern.pattern().isEmpty() && posPattern == null) {
        	return true;
        }
    	boolean tokenMatches = true;
        boolean postagMatches = false;
        boolean isTokenEmpty = e.getString().isEmpty();
        boolean hasPosTag = (posPattern != null);
        boolean negate = e.getNegation();
        boolean postagNegate = e.getPOSNegation();
        boolean inflected = e.isInflected();
        
        if (posPattern == null) {
        	postagMatches = true;
        }
        if (!isTokenEmpty) {
        	Matcher m;
        	boolean matches = false;
        	// checking inflected matches
        	if (inflected) {
        		if (isInflectedStringMatch(word,e)) {
        			matches = true;
        		}
        	} else {
        		m = tokenPattern.matcher(word);
        		if (m.matches()) matches = true;
        	}
            
            if (matches) {
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
            List<String> postags = getPosTags(word);
            for (String s : postags) {
                Matcher m = posPattern.matcher(s);
                if (m.matches()) {
                    if (!postagNegate) {
                        postagMatches = true;
                        break;
                    }
                } else {
                    if (postagNegate) {
                        postagMatches = true;
                        break;
                    }
                }
            }
            if (postags.size() == 0) {
                postagMatches = false;
            }
            
        }
        return (tokenMatches && postagMatches);
    }
    
    private boolean isInflectedStringMatch(String word, Element e) {
    	Matcher m;
    	Pattern lemmaPattern = Pattern.compile(RuleConverter.glueWords(getLemmas(e)));
		ArrayList<String> wordLemmas = getLemmas(word);
		for (String lemma : wordLemmas) {
			m = lemmaPattern.matcher(lemma);
			if (m.matches()) {
				return true;
			}
		}
		return false;
    }
    
    
    
//    /**
//     * True if given word is an example of element; slower than other version
//     * @param word
//     * @param element
//     * @return
//     * @deprecated
//     */
//    public boolean isExampleOf(String word, Element element) {
//        String token = element.getString();
//        String posTag = element.getPOStag();
//        // the empty token case matches everything
//        if (token.isEmpty() && posTag == null) {
//        	return true;
//        }
//        boolean isTokenEmpty = token.isEmpty();
//        boolean hasPosTag = (posTag != null);
//        boolean negate = element.getNegation();
//        boolean postagNegate = element.getPOSNegation();
//        boolean tokenMatches = true;
//        boolean postagMatches = false;
//        if (posTag == null) {	// if there's no postag, default postagMatches to true, because it matches everything
//        	postagMatches = true;
//        }
//        
//        
//        if (!isTokenEmpty) {
//            Pattern p = Pattern.compile(token);
//            Matcher m = p.matcher(word);
//            if (m.matches()) {
//                if (negate) {
//                    tokenMatches = false; 
//                }
//            } else {
//                if (!negate) {
//                    tokenMatches = false;
//                }
//            }
//        }
//        if (hasPosTag) {
//            Pattern p = Pattern.compile(posTag);
//            List<String> postags = getPosTags(word);
//            for (String s : postags) {
//                Matcher m = p.matcher(s);
//                if (m.matches()) {
//                    if (!postagNegate) {
//                        postagMatches = true;
//                        break;
//                    } 
//                } else {
//                	if (postagNegate) {
//                		postagMatches = true;
//                		break;
//                	}
//                }
//            }
//            if (postags.size() == 0) {
//                postagMatches = false;
//            }
//            
//        }
//        return (tokenMatches && postagMatches);
//    }
//    
//    /**
//     * True if the given word matches one of the exceptions; slower than other version
//     * @param word
//     * @param exceptions
//     * @return
//     * @deprecated	
//     */
//    public boolean inExceptionList(String word, List<Element> exceptions) {
//    	for (Element element : exceptions) {
//            if (isExampleOf(word, element)) {
//                return true;
//            }
//        }
//        return false;       
//    }
    
    /**
     * Returns a list of the word's POS tags
     * @param word
     * @return
     */
    private List<String> getPosTags(String word) {
        List<WordData> lwd = dictLookup.lookup(word);
        ArrayList<String> postags = new ArrayList<String>();
        for (WordData wd : lwd) {
            postags.add(wd.getTag().toString());
        }
        return postags;
    }
    /**
     * Returns an or-ed group of the lemmas of a word
     * @param word
     * @return
     */
    private ArrayList<String> getLemmas(String word) {
    	List<WordData> lwd = dictLookup.lookup(word);
    	ArrayList<String> lemmas = new ArrayList<String>();
    	for (WordData wd : lwd) {
    		if (!lemmas.contains(wd.getStem())) {
    			lemmas.add(wd.getStem().toString());
    		}
    	}
    	return lemmas;
    }
    
    // returns the lemmas of an element; 
    // the point of this method is that so we can get the lemmas of a bunch of or-ed words
    private ArrayList<String> getLemmas(Element e) {
    	if (!e.isRegularExpression()) {
    		return getLemmas(e.getString());
    	} else {
    		if (isOrRegex(e)) {
    			ArrayList<String> lemmas = new ArrayList<String>();
    			String[] words = e.getString().split("\\|");
    			for (String word : words) {
    				lemmas.addAll(getLemmas(word));
    			}
    			return lemmas;
    		}
    		return null;
    	}
    }
    
    
    /**
     * Returns true if the element has a (non-regexp, non-negated) token and no exception list
     * @param e
     * @return
     */
    private static boolean isJustToken(Element e) {
    	return (!e.getString().isEmpty() && !e.isRegularExpression() && !e.getNegation() && e.getExceptionList() == null);
    }
    
    /**
     * Returns true if the given element's string is a regex set of punctuation.
     * e.g. ['"] or [.,;:?!]
     * @param e
     * @return
     */
    public static boolean isPunctuation(Element e) {
    	if (regexSet.matcher(e.getString()).matches() && !e.getNegation() && e.getPOStag() == null) {
    		return true;
    	}
    	return false;
    }
    
    /**
     * Grabs the first element of a punctuation set matched by the above method.
     * @param e
     * @return
     */
    public String getOnePunc(Element e) {
    	String set = e.getString();
    	Matcher m = regexSet.matcher(set);
    	m.find();
    	return m.group(1);
    }
    
    /** 
     * Returns true if the element is an or-ed list of words, without a specified pos-tag.
     * e.g. can|could|would|should
     * @param e
     * @return
     */
    private static boolean isSimpleOrRegex(Element e) {
    	// any number of conditions that could halt this check
    	if (e.getString().isEmpty()) return false;
    	if (e.getPOStag() != null) return false;
    	if (e.getNegation()) return false;
    	if (!e.isRegularExpression()) return false;
    	if (e.hasAndGroup()) return false;
    	if (e.getExceptionList() != null) return false;
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
    
    private static boolean isOrRegex(Element e) {
    	if (e.getString().isEmpty()) return false;
    	String token = e.getString();
    	String[] ors = token.split("\\|");
    	for (String s : ors) {
    		if (RuleConverter.isRegex(s)) {
    			return false;
    		}
    	}
    	return true; 
    }
    
    // ** DICTIONARY METHODS ** 
    
    private DictionaryIterator resetDictIter() {
        DictionaryIterator ret = null;
        try {
        	ret = new DictionaryIterator(Dictionary.read(dictFile), Charset.forName("utf8").newDecoder(), true);
        } catch (IOException e) {
        	e.printStackTrace();
        }
        return ret;        
    }
    
    private IStemmer loadDictionary() throws IOException {
        IStemmer dictLookup = null;
        dictLookup = new DictionaryLookup(Dictionary.read(dictFile));
        return dictLookup;
    }
    
    // try several ways to open the dictionary file
    private void setupDictionaryFiles() {
   		try {
   			filename = "." +  JLanguageTool.getDataBroker().getResourceDir() + "/" + 
   						language.getShortName() + "/" + language.getName().toLowerCase() + ".dict";
   			dictFile = new File(filename);
        	dictLookup = (DictionaryLookup) loadDictionary();
        	dictIterator = resetDictIter();
        } catch (IOException e) {
        	try {
        		// a different formulation of the filename
        		filename = "./src/" +  JLanguageTool.getDataBroker().getResourceDir() + "/" + 
							language.getShortName() + "/" + language.getName().toLowerCase() + ".dict";
        		dictFile = new File(filename);
        		dictLookup = (DictionaryLookup) loadDictionary();
            	dictIterator = resetDictIter();
        	} catch (IOException e2) {
        		e2.printStackTrace();
        	}
        }
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
    
    public List<PatternRule> parsePatternRuleExtraTokens(final String ruleString) {
    	String rs = ruleString;
    	rs = rs.replace("<pattern>\n", "<pattern>\n<token/>\n");
		rs = rs.replace("</pattern>\n", "<token/>\n</pattern>\n");
    	final PatternRuleLoader ruleLoader = new PatternRuleLoader();
    	String ruleFileString = ruleFileHeader + categoriesString + rs + endCategoriesString + endRulesString;
    	InputStream is = new ByteArrayInputStream(ruleFileString.getBytes());
    	try {
    		return ruleLoader.getRules(is,null);
    	} catch (IOException e) {
    		return new ArrayList<PatternRule>();
    	}
    }
    
    public void enableRule(String id) {
    	tool.enableDefaultOffRule(id);
    }
    
    
}
