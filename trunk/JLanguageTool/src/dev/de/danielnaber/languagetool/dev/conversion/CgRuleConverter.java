package de.danielnaber.languagetool.dev.conversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import de.danielnaber.languagetool.dev.conversion.cg.CgCompositeTag;
import de.danielnaber.languagetool.dev.conversion.cg.CgContextualTest;
import de.danielnaber.languagetool.dev.conversion.cg.CgGrammar;
import de.danielnaber.languagetool.dev.conversion.cg.CgSet;
import de.danielnaber.languagetool.dev.conversion.cg.CgTag;
import de.danielnaber.languagetool.dev.conversion.cg.CgTextualParser;
import de.danielnaber.languagetool.dev.conversion.cg.CgRule;
import de.danielnaber.languagetool.dev.conversion.cg.CgContextualTest.POS;

public class CgRuleConverter extends RuleConverter {

	private CgGrammar grammar;
	private String[] lines;
	private static String tagDelimiter = ":";
	
	// basic constructor
	public CgRuleConverter() {
		super();
	}
	
	public CgRuleConverter(String infile, String outfile, String specificFiletype) {
		super(infile, outfile, specificFiletype);
	}
	
	// Get methods
	public CgGrammar getGrammar() {return this.grammar;}
	
	// Set methods
	public void setGrammar(CgGrammar grammar) {this.grammar = grammar;}
	
	public void setTagDelimiter(String td) {tagDelimiter = td;}
	
	@Override
	public void parseRuleFile() throws IOException {
		parseCgFile();	// builds the grammar
		List<CgRule> ruleList = new ArrayList<CgRule>();
		for (CgRule rule : grammar.rule_by_number) {
			ruleList.add(rule);
		}
		ruleObjects	= ruleList;
		ltRules = new ArrayList<List<String>>();
		allLtRules = new ArrayList<List<String>>();
		disambiguationRules = new ArrayList<List<String>>();
		originalRuleStrings = new ArrayList<String>();
		warnings = new ArrayList<String[]>();
		for (Object ruleObject : ruleObjects) {
			CgRule cgrule = (CgRule) ruleObject;
			List<String> ruleAsList = ltRuleAsList(cgrule,generateId(ruleObject),generateName(ruleObject),cgrule.type.name());
			disambiguationRules.add(ruleAsList);
			allLtRules.add(ruleAsList);
			originalRuleStrings.add(lines[cgrule.line]);
		}
	}
	
	
	@Override
	public boolean isDisambiguationRule(Object ruleObject) {
		return true;	// all cg rules are disambiguation rules
	}
	
	public void parseCgFile() throws IOException { 
		File file = new File(inFileName);
		grammar = new CgGrammar();
		CgTextualParser parser = new CgTextualParser(grammar, file);
		int result = parser.parse_grammar_from_file(inFileName, null, null);
		if (result == 0) {
			//System.out.println("Successfully parsed constraint grammar file " + inFileName);
		} else {
			System.err.println("Failed to parse constraint grammar file " + inFileName);
		}
		getGrammarFileLines(inFileName);
	}
	
	/**
	 * Grabs the original lines of the CG grammar file. Mainly for retrieving the original rule strings to include in comments.
	 * @param filename
	 */
	public void getGrammarFileLines(String filename) {
		BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        String inArray = "";
        // put some buffer space at the beginning, just to be sure
        sb.append("    ");
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
            int c = reader.read();
            while (c != -1) {
                sb.append((char)c);
                c = reader.read();
            }
            inArray = sb.toString();
            reader.close();
        } catch (IOException e) {
            System.err.println("Error opening grammar file");
            System.exit(1);
        } 
        String[] lines = inArray.split("\n");
        this.lines = lines;;
	}
	
	/**
	 * Takes a single {@link CgRule} and converts it into a list of lines of a LT rule.
	 * Sometimes the CG rule has to be split into several LT rules. In this case, the rule 
	 * is added as a bunch of rules in a rulegroup
	 */
	@Override
	public List<String> ltRuleAsList(Object ruleObject, String id, String name, String type) {
		CgRule rule = (CgRule)ruleObject;
		type = rule.type.name();	// like K_SELECT or K_REMOVE
		List<String> ltRule = new ArrayList<String>();
		ArrayList<String> currentWarnings = new ArrayList<String>();

		String cgRuleString = lines[rule.line];
		ltRule.add("<!-- " + cgRuleString + " -->");
//		ArrayList<CgRule> rules = splitUnificationRule(mainRule, grammar);
//		for (CgRule rule : rules) {
		ArrayList<Token> tokensList = new ArrayList<Token>();
		ArrayList<ArrayList<Token>> outerList = new ArrayList<ArrayList<Token>>(); 	// in case we need to split the rule into several rules
		ArrayList<Token[]> processedLists = new ArrayList<Token[]>();
		
		CgSet targetSet = expandSetSets(grammar.getSet(rule.target));
		Token target = new Token(targetSet,false,0,false,false,new CgSet(),false,0,false);
		if (!isOrCompatible(target)) {
			System.err.println("Target for rule on line " + rule.line + " cannot be represented as one LT rule. Consider rewriting it.");
			return new ArrayList<String>();
		}
		tokensList.add(target);
		ArrayList<CgContextualTest> sortedTestsHeads = new ArrayList<CgContextualTest>();
		// puts the parent test at the end, so it's processed last
		for (CgContextualTest test : rule.test_heads) {
			if (test.isParentTest()) {
				sortedTestsHeads.add(test);
			} else {
				sortedTestsHeads.add(0, test);
			}
		}
		
		for (CgContextualTest test : sortedTestsHeads) {
			if (test.isNormalTest()) {
				Token testToken = getTokenFromNormalTest(test);
				tokensList.add(testToken);
				// only accounts for a single branching (i.e. one parent test)
			} else if (test.isParentTest()) {
				if (!outerList.isEmpty()) {
					System.err.println("Can't have two parent tests in one test on line " + rule.line + "\nTry splitting it up.");
					System.exit(1);
				}
				for (int testInt : test.ors) {
					ArrayList<Token> newTokenList = copyTokenList(tokensList);
					CgContextualTest childTest = rule.test_map.get(testInt);
					if (childTest.isNormalTest()) {
						Token childTestToken = getTokenFromNormalTest(childTest);
						newTokenList.add(childTestToken);
					} else if (childTest.isLinkedTest()) {
						ArrayList<CgContextualTest> linkedTests = new ArrayList<CgContextualTest>();
						CgContextualTest curTest = childTest;
						while (curTest.next != 0) {	// while there are still more tests to link to
							linkedTests.add(curTest);
							curTest = rule.test_map.get(curTest.next);
						} 	
						linkedTests.add(curTest); 	// add the last linked test
						
						Token headLinkedToken = getLinkedTokens(linkedTests);	// modifies the offsets for the linked tests
						newTokenList.add(headLinkedToken);
					}
					
					outerList.add(newTokenList);
				}
			} else if (test.isLinkedTest()) {
				// add all the linked tests to a list
				ArrayList<CgContextualTest> linkedTests = new ArrayList<CgContextualTest>();
				CgContextualTest curTest = test;
				while (curTest.next != 0) {	// while there are still more tests to link to
					linkedTests.add(curTest);
					curTest = rule.test_map.get(curTest.next);
				} 	
				linkedTests.add(curTest); 	// add the last linked test
				
				Token headLinkedToken = getLinkedTokens(linkedTests);	// modifies the offsets for the linked tests
				tokensList.add(headLinkedToken);
				
			}
		}
		// if the outerList is empty, we haven't had a parent test, so we can just add the tokensList to the outerList and process it
		if (outerList.isEmpty()) {	
			outerList.add(tokensList);
		}
		// pre-process/split all the tests
		// they come off outerList and go back onto processedLists
		// first split off the special case of the negative backward barrier scan
		for (int i=0;i<outerList.size();i++) {
			Token[] tokens = outerList.get(i).toArray(new Token[outerList.get(i).size()]);
			if (negativeBackwardBarrierScan(tokens)) {
				ArrayList<List<Token>> split = splitNegativeBackwardBarrierScan(tokens);
				outerList.remove(i);
				for (List<Token> splitList : split) {
					outerList.add(i, new ArrayList<Token>(splitList));
					i++;
				}
			}
		}
		
		for (int i=0;i<outerList.size();i++) {
			Token[] tokens = outerList.get(i).toArray(new Token[outerList.get(i).size()]);
			Arrays.sort(tokens);
			tokens = addGapTokens(tokens);
			if (skipSafe(tokens)) {
				tokens = addSkipTokens(tokens);
				tokens = resolveLinkedTokens(tokens);
				if (!singleRuleCompatible(tokens)) {
					ArrayList<List<Token>> singleRuleCompatibleTokens = splitForSingleRule(tokens);
					for (List<Token> srctl : singleRuleCompatibleTokens) {
						Token[] srcta = srctl.toArray(new Token[srctl.size()]);
						processedLists.add(srcta);
					}
				} else {
					processedLists.add(tokens);
				}
			}
			else {
				ArrayList<List<Token>> splitTokenLists = getSkipSafeTokens(tokens);
				for (int j=0;j<splitTokenLists.size();j++) {
					Token[] indSplitTokenList = splitTokenLists.get(j).toArray(new Token[splitTokenLists.get(j).size()]);
					indSplitTokenList = addSkipTokens(indSplitTokenList);
					indSplitTokenList = resolveLinkedTokens(indSplitTokenList);
					Arrays.sort(indSplitTokenList);
					indSplitTokenList = addGapTokens(indSplitTokenList);
					if (!singleRuleCompatible(indSplitTokenList)) {
						ArrayList<List<Token>> singleRuleCompatibleTokens = splitForSingleRule(indSplitTokenList);
						for (List<Token> srctl : singleRuleCompatibleTokens) {
							Token[] srcta = srctl.toArray(new Token[srctl.size()]);
							processedLists.add(srcta);
						}
					} else {
						processedLists.add(indSplitTokenList);
					}
				}
			}
		}
		
		// the actual rule generation
		if (processedLists.size() == 1) {
			Token[] tokens = processedLists.get(0);
			List<String> ltRule2 = getRuleByType(targetSet, tokens, rule, id, name, type);
			ltRule.addAll(ltRule2);
		} else {
			ltRule.add("<rulegroup name=\"" + generateName(ruleObject) + "\">");
			for (Token[] tokens : processedLists) {
				List<String> ltRule2 = getRuleByType(targetSet, tokens, rule, null, null, type);
				ltRule.addAll(ltRule2);
			}
			ltRule.add("</rulegroup>");
		}
		
//		}
		warnings.add(currentWarnings.toArray(new String[currentWarnings.size()]));
		return ltRule;
	}
	
	// ** METHODS THAT SPLIT A RULE INTO MULTIPLE RULES **
	
	/**
	 * For if there's multiple surface/base forms and postags in a single token that we can't "or" together
	 * in one LT token. E.g. if a token includes ("man" or "woman" or NN or NNP), it'd have to be split.
	 * @param tokens
	 * @return
	 */
	public ArrayList<List<Token>> splitForSingleRule(Token[] tokens) {
		ArrayList<List<Token>> list = new ArrayList<List<Token>>();
		ArrayList<Token> tokenList = new ArrayList<Token>(Arrays.asList(tokens));
		list.add(tokenList);
		boolean notdone = true;
		while (notdone) {
			for (int i=0;i<list.size();i++) {
				List<Token> insideList = list.get(i);
				if (singleRuleCompatible(insideList.toArray(new Token[insideList.size()]))) {
					if (i == list.size()-1) notdone = false;
					continue;
				} else {
					list.remove(i);
					ArrayList<List<Token>> splitTokens = splitListForSingleRule(insideList);
					for (List<Token> ind : splitTokens) {
						list.add(ind);
					}
					break;
				}
			}
		}
		
		return list;
	}
	
	/**
	 * Actually performs the splitting for wrapper method splitForSingleRule. Only performs a split for a single token.
	 * @param tokens
	 * @return
	 */
	public ArrayList<List<Token>> splitListForSingleRule(List<Token> tokens) {
		ArrayList<List<Token>> list = new ArrayList<List<Token>>();
		final ArrayList<Token> firstList = new ArrayList<Token>();
		list.add(firstList);
		int i=0;
		for (i=0;i<tokens.size();i++) {
			if (isOrCompatible(tokens.get(i))) {
				firstList.add(tokens.get(i));
			} else {
				list.remove(firstList);
				ArrayList<CgSet> newSets = splitCgSet(tokens.get(i).target);
				for (CgSet set : newSets) {
					Token newToken = new Token(tokens.get(i));
					newToken.target = expandSetSets(set);
					newToken.postags = newToken.target.getPostagsString();
					newToken.baseforms = newToken.target.getSingleTagBaseformsString();
					newToken.surfaceforms = newToken.target.getSingleTagSurfaceformsString();
					newToken.compositeTags = newToken.target.getCompositeTags();
					// clone the first list
					ArrayList<Token> newList = new ArrayList<Token>();
					for (Token token : firstList) {
						newList.add(new Token(token));
					}
					// add the new token
					newList.add(newToken);
					// add the new list
					list.add(newList);
				}
				break;
			}
		}
		// finish up the list
		for (int j=i+1;j<tokens.size();j++) {
			for (int k=0;k<list.size();k++) {
				ArrayList<Token> insideList = (ArrayList<Token>)list.get(k);
				insideList.add(tokens.get(j));
				list.set(k,insideList);
			}
		}
		return list;
	}
	
	/**
	 * Splits off part of a CgSet that can't be represented in a single LT token
	 * @param target
	 * @return
	 */
	public ArrayList<CgSet> splitCgSet(CgSet target) {
		// setting up the lists to perform the check
		ArrayList<CgSet> newSets = new ArrayList<CgSet>();
		CgTag[] postags = target.getSingleTagPostags();
		CgTag[] baseforms = target.getSingleTagBaseforms();
		CgTag[] surfaceforms = target.getSingleTagSurfaceforms();
		CgCompositeTag[] compositePostags = target.getCompositePostags();
		
		// actually checking and doing the splitting
		if (postags.length > 0 && baseforms.length > 0) {
			CgSet set1 = new CgSet(target);
			CgSet set2 = new CgSet(set1);
			set1.single_tags.removeAll(Arrays.asList(postags));
			set1.tags.removeAll(Arrays.asList(compositePostags));
			set2.single_tags.removeAll(Arrays.asList(baseforms));
			newSets.add(set1);
			newSets.add(set2);
			return newSets;
		}
		if (postags.length > 0 && surfaceforms.length > 0) {
			CgSet set1 = new CgSet(target);
			CgSet set2 = new CgSet(target);
			set1.single_tags.removeAll(Arrays.asList(postags));
			set1.tags.removeAll(Arrays.asList(compositePostags));
			set2.single_tags.removeAll(Arrays.asList(surfaceforms));
			newSets.add(set1);
			newSets.add(set2);
			return newSets;
		}
		if (surfaceforms.length > 0 && baseforms.length > 0) {
			CgSet set1 = new CgSet(target);
			CgSet set2 = new CgSet(target);
			set1.single_tags.removeAll(Arrays.asList(surfaceforms));
			set2.single_tags.removeAll(Arrays.asList(baseforms));
			newSets.add(set1);
			newSets.add(set2);
			return newSets;
		}
		// if we didn't catch the culprit in the single tags, it must be in the composite tags,
		// which means that there exists two composite tags that have different types of tags in them.
		// I could try to do this in a principled way, or I could just split off each composite tag. 
		// This seems like the better idea for now.
		newSets = groupCompositeTags(target);
		/*
		for (CgCompositeTag ctag : compositeTags) {
			CgSet set1 = new CgSet(target);
			CgSet set2 = new CgSet(target);
			set1.tags.remove(ctag);
			set2.tags.removeAll(Arrays.asList(compositeTags));
			set2.tags.removeAll(Arrays.asList(compositePostags));
			set2.single_tags = new HashSet<CgTag>();
			set2.tags.add(ctag);
			twoSets.add(set1);
			twoSets.add(set2);
			return twoSets;
		}
		*/
		// it should never get to here, because it should never get a set that doesn't need to be split passed to it.
		return newSets;
	}
	
	/**
	 * Groups the composite tags along lines that can be represented in a single LT rule
	 * @param target
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<CgSet> groupCompositeTags(CgSet target) {
		HashMap<String,ArrayList<CgCompositeTag>> bf = new HashMap<String,ArrayList<CgCompositeTag>>();
		HashMap<String,ArrayList<CgCompositeTag>> sf = new HashMap<String,ArrayList<CgCompositeTag>>();
		HashMap<String,CgCompositeTag> dict = new HashMap<String,CgCompositeTag>();	// dictionary of sorts
		for (CgCompositeTag ctag : target.tags) {
			CgCompositeTag postags = new CgCompositeTag();
			CgCompositeTag baseforms = new CgCompositeTag();
			CgCompositeTag surfaceforms = new CgCompositeTag();
			for (CgTag tag : ctag.tags) {
				if (isBaseForm(tag.tag)) {
					baseforms.addTag(tag);
				} else if (isSurfaceForm(tag.tag)) {
					surfaceforms.addTag(tag);
				} else if (isPostag(tag.tag)) {
					postags.addTag(tag);
				}
			}
			if (!postags.isEmpty()) {
				if (!baseforms.isEmpty()) {
					bf = (HashMap)smartPut(bf, postags.toString(), baseforms);
				}
				else if (!surfaceforms.isEmpty()) {	// assumes there won't be both sf and bf in the same composite tag
					sf = (HashMap)smartPut(sf, postags.toString(), surfaceforms);
				}
			}
			dict.put(postags.toString(), postags);
		}
		ArrayList<CgSet> ret = new ArrayList<CgSet>();
		for (String postagSet : bf.keySet()) {
			CgSet newSet = new CgSet(target);
			newSet.tags = new HashSet<CgCompositeTag>();
			ArrayList<CgCompositeTag> bfs = bf.get(postagSet);
			for (CgCompositeTag singleBf : bfs) {
				CgCompositeTag newTotalTag = new CgCompositeTag();
				for (CgTag tag : dict.get(postagSet).tags) {
					newTotalTag.addTag(tag);
				}
				for (CgTag tag : singleBf.tags) {
					newTotalTag.addTag(tag);
				}
				newSet.addCompositeTag(newTotalTag);
			}
			ret.add(newSet);
		}
		return ret;
	}
	
	/**
	 * returns separate Token arrays, each of which is safe for dealing with scanning tokens
	 * and each of which will be a separate rule. This applies when we have a scanning token
	 * before a non-scanning token, offset wise
	 */
	public ArrayList<List<Token>> getSkipSafeTokens(Token[] tokens) {
		ArrayList<List<Token>> list = new ArrayList<List<Token>>();
		List<Token> tokenList = Arrays.asList(tokens);
		list.add(tokenList);
		boolean notdone = true;
		while (notdone) {
			for (int i=0;i<list.size();i++) {
				List<Token> insideList = list.get(i);
				if (skipSafe(insideList.toArray(new Token[insideList.size()]))) {
					if (i == list.size()-1) {
						notdone = false;
					}
				} else {
					list.remove(i);
					ArrayList<List<Token>> splitTokens = splitOutSkipTokens(insideList);
					for (List<Token> isl : splitTokens) {
						list.add(isl);
					}
					break;
				}
			}
			
		}
		return list;
	}
	
	/**
	 * Actually does the splitting for wrapper method getSkipSafeTokens
	 * @param tokens
	 * @return
	 */
	public ArrayList<List<Token>> splitOutSkipTokens(List<Token> tokens) {
		ArrayList<List<Token>> list = new ArrayList<List<Token>>();
		ArrayList<Token> scanningTokens = new ArrayList<Token>();
		ArrayList<Token> reverseScanningTokens = new ArrayList<Token>();
		ArrayList<Token> normalTokens = new ArrayList<Token>();
		for (Token token : tokens) {
			if (token.scanahead) scanningTokens.add(token);
			else if (token.scanbehind) reverseScanningTokens.add(token);
			else normalTokens.add(token);
		}
		// forward scans
		for (int s=0;s<scanningTokens.size();s++) {
			final Token scanning = scanningTokens.get(s);
			for (int n=0;n<normalTokens.size();n++) {
				final Token normal = normalTokens.get(n);
				if (normal.offset >= scanning.offset) {
					ArrayList<Token> newTokenList1 = new ArrayList<Token>();
					ArrayList<Token> newTokenList2 = new ArrayList<Token>();
					for (Token ntoken : normalTokens) {
						newTokenList1.add(ntoken);
						newTokenList2.add(ntoken);
					}
					Token newNormalToken = new Token(scanning);
					newNormalToken.scanahead = false;
					newTokenList1.add(newNormalToken);
					scanning.offset++;
					newTokenList2.add(scanning);
					list.add(newTokenList1);
					list.add(newTokenList2);
					return list;
				}
			}
		}
		// backward scans
		for (int s=0;s<reverseScanningTokens.size();s++) {
			final Token scanning = reverseScanningTokens.get(s);
			for (int n=0;n<normalTokens.size();n++) {
				final Token normal = normalTokens.get(n);
				if (normal.offset <= scanning.offset) {
					ArrayList<Token> newTokenList1 = new ArrayList<Token>();
					ArrayList<Token> newTokenList2 = new ArrayList<Token>();
					for (Token ntoken : normalTokens) {
						newTokenList1.add(ntoken);
						newTokenList2.add(ntoken);
					}
					Token newNormalToken = new Token(scanning);
					newNormalToken.scanbehind = false;
					newTokenList1.add(newNormalToken);
					scanning.offset--;
					newTokenList2.add(scanning);
					list.add(newTokenList1);
					list.add(newTokenList2);
					return list;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Handles special case of negative backwards barrier scan (e.g. (NOT -1* Verb BARRIER CLB));
	 * @param tokens
	 * @return
	 */
	public ArrayList<List<Token>> splitNegativeBackwardBarrierScan(Token[] tokens) {
		ArrayList<List<Token>> list = new ArrayList<List<Token>>();
		ArrayList<Token> newTokenList1 = new ArrayList<Token>();
		ArrayList<Token> newTokenList2 = new ArrayList<Token>();
		int index=0;
		for (index = 0;index<tokens.length;index++) {
			if (tokens[index].scanbehind && tokens[index].negate && !tokens[index].barrier.isEmpty()) {
				Token newToken = new Token(tokens[index]);
				newToken.barrier = new CgSet();
				newTokenList1.add(tokens[index]);
				newTokenList2.add(newToken);
				break;
			} else {
				newTokenList1.add(tokens[index]);
				newTokenList2.add(tokens[index]);
			}
		}
		// finish off the rest
		for (index=index+1;index<tokens.length;index++) {
			newTokenList1.add(tokens[index]);
			newTokenList2.add(tokens[index]);
		}
		list.add(newTokenList1);
		list.add(newTokenList2);
		return list;
	}
	
//	// takes a rule that has unification tags (e.g. $$NUMBER) and splits it into several easier to handle rules
//	public ArrayList<CgRule> splitUnificationRule(CgRule rule, CgGrammar grammar) {
//		ArrayList<CgRule> rules = new ArrayList<CgRule>();
//		// go over all the tests in the test_map, which should contain all tests for the entire rule, and every time you see a 
//		// unification tag, put in one of its component parts and add that modified rule to the list of new rules.
//		HashSet<CgSet> unifyingSets = new HashSet<CgSet>();
//		// add all the unifying sets in the rule
//		for (Iterator<Integer> iter = rule.test_map.keySet().iterator(); iter.hasNext(); ) {
//			CgContextualTest curTest = rule.test_map.get(iter.next());
//			CgSet target = grammar.getSet(curTest.target);
//			for (Integer setint : target.sets) {
//				CgSet set = grammar.getSet(setint);
//				if (set.type.contains(ST.ST_TAG_UNIFY.value)) {
//					unifyingSets.add(set);
//				}
//			}
//		}
//		CgSet targetSet = grammar.getSet(rule.target);
//		for (int setint : targetSet.sets) {
//			if (grammar.getSet(setint).type.contains(ST.ST_TAG_UNIFY.value)) {
//				unifyingSets.add(grammar.getSet(setint));
//			}
//		}
//		// if no unifying sets, just return the rule
//		if (unifyingSets.size() == 0) {
//			rules.add(rule);
//		}
//		for (CgSet unifyingSet : unifyingSets) {
//			CgSet unifyingSetExpanded = expandSetSets(unifyingSet);
//			for (CgTag tag : unifyingSetExpanded.single_tags) {
//				CgSet oldTargetSet = new CgSet(grammar.getSet(rule.target));
//				CgRule newRule = new CgRule(rule);
//				if (oldTargetSet.sets.contains(unifyingSet.hash)) {
//					oldTargetSet.sets.remove((Object)unifyingSet.hash);
//					oldTargetSet.single_tags.add(tag);
//					oldTargetSet.rehash();
//					grammar.addSet(oldTargetSet);
//					newRule.target = oldTargetSet.hash;
//				}
//				
//				for (Iterator<Integer> iter = newRule.test_map.keySet().iterator(); iter.hasNext();) {
//					int testKey = iter.next();
//					CgContextualTest test = newRule.test_map.get(testKey);
//					CgSet oldTestTargetSet = new CgSet(grammar.getSet(test.target));
//					if (oldTestTargetSet.sets.contains(unifyingSet.hash)) {
//						oldTestTargetSet.sets.remove(unifyingSet);
//						oldTestTargetSet.single_tags.add(tag);
//						oldTestTargetSet.rehash();
//						grammar.addSet(oldTestTargetSet);
//						test.target = oldTestTargetSet.hash;
//					}
//					newRule.test_map.put(testKey, test);
//				}
//				rules.add(newRule);
//			}
//		}
//		return rules;
//	}
	
	
	// ** METHODS THAT MODIFY A SINGLE TOKEN LIST **
	
	/**
	 * Expands the tests in a linked test to multiple tokens
	 */
	public Token[] resolveLinkedTokens(Token[] tokens) {
		ArrayList<Token> tokenList = new ArrayList<Token>(Arrays.asList(tokens));
		boolean notdone = true;
		while (notdone) {
			for (int i=0;i<tokenList.size();i++) {
				Token curToken = tokenList.get(i);
				if (curToken.nextToken != null) {
					Token tempToken = new Token(curToken.nextToken);
					tempToken.offset = curToken.offset + tempToken.relativeOffset;	// to fix the offsets
					tokenList.add(i+1, tempToken);
					Token temp2 = new Token(curToken);
					temp2.nextToken = null;
					tokenList.set(i, temp2);
					break;
				} else {
					if (i == tokenList.size()-1) notdone = false;
				}
			}
		}
		return tokenList.toArray(new Token[tokenList.size()]);
	}
	
	/**
	 * contains the list of linked CgContextualTests, to translate into Token format
	 * returns only the head of the linked tokens, have to iterate through them later when you add them
	 */
	public Token getLinkedTokens(ArrayList<CgContextualTest> tests) {
		ArrayList<Token> tokens = new ArrayList<Token>();
		for (int i=0;i<tests.size();i++) {
			if (i == 0) {
				// this kind of assumes that it won't be a parent test
				tokens.add(getTokenFromNormalTest(tests.get(i)));
			} else {
				Token token = getTokenFromNormalTest(tests.get(i));
				token.relativeOffset = token.offset;
				token.offset = token.offset + tokens.get(i-1).offset;
				token.prevToken = tokens.get(i-1);
				tokens.add(token);
			}
		}
		// sort tokens by offset and rearrange them
		Token[] ts = tokens.toArray(new Token[tokens.size()]);
		Arrays.sort(ts);
		// this assumes there's only one scan in the linked tests
		// the rationale here is that if it switches positions of tokens, and the 
		// one that's a scan token gets pushed further back in the list, its scan flag will go back to the front.
		for (int i=0;i<ts.length;i++) {
			if (ts[i].scanahead) {
				ts[i].scanahead = false;
				ts[0].scanahead = true;
				break;
			}
		}
		// check to make sure there's not another scan in the linked tests:
		for (int i=1;i<ts.length;i++) {
			if (ts[i].scanahead) {
				System.err.println("Two scan tests in one series of linked tests. This is really hard to represent in LT format. Try to split it into several rules");
				System.exit(1);
			}
		}
		ts = addLinkedGapTokens(ts);
		for (int i=0;i<ts.length;i++) {
			 if (i == 0) {
				 ts[i].relativeOffset = 0;
			 } else {
				 ts[i].relativeOffset = 1;
			 }
			 if (i != ts.length - 1) ts[i].nextToken = ts[i+1];
			 if (i != 0) ts[i].prevToken = ts[i-1];
		}
		return ts[0];
	}
	
	/**
	 * Adds gap tokens in the case of a linked (sub) list of tokens
	 * @param ts
	 * @return
	 */
	public Token[] addLinkedGapTokens(Token[] ts) {
		ArrayList<Token> tokens = new ArrayList<Token>(Arrays.asList(ts));
		boolean notdone = true;
		while (notdone) {
			for (int i=0;i<tokens.size();i++) {
				if (i==0) continue;
				else if (i > 0 && i < tokens.size()) {
					if (tokens.get(i).offset == tokens.get(i-1).offset || tokens.get(i).offset == (tokens.get(i-1).offset + 1)) {
						if (i == tokens.size() - 1) notdone = false;
						continue;
					}
					else {
						Token newToken = new Token(new CgSet(), false, tokens.get(i-1).offset + 1, false, false, new CgSet(), false, 0, false);
						newToken.relativeOffset = tokens.get(i-1).relativeOffset + 1;
						Token oldToken = tokens.get(i-1);
						oldToken.relativeOffset = -1;
						tokens.set(i-1, oldToken);
						tokens.add(i,newToken);
						break;
					}
				}
				if (i == tokens.size() - 1) {
					notdone = false;
				}
			}
		}
		return tokens.toArray(new Token[tokens.size()]);
	}
	
	/**
	 * only gets called if it's safe to add skip tokens in a straightforward manner, otherwise, we split them out
	 * if it's a negative scanning string (NOT 1* Verb BARRIER CLB), then the exception string is the target
	 * if it's a positive scanning string (1* Verb BARRIER CLB), then the exception string is the barrier and the end of sentence
	 * it always goes in the previous token with scope next
	 */
	public Token[] addSkipTokens(Token[] tokens) {
		ArrayList<Token> tokenList = new ArrayList<Token>(Arrays.asList(tokens));
		for (int i=0;i<tokenList.size();i++) {
			// forward scans (1* Verb)
			if (tokenList.get(i).scanahead) {
				if (i == 0) {
					Token newToken = new Token(new CgSet(), false, tokenList.get(i).offset - 1, false, false, new CgSet(), false, -1, false);
					if (!tokenList.get(i).barrier.isEmpty() || tokenList.get(i).negate) {
						newToken.exceptionString = getBarrierExceptionStringFromToken(tokenList.get(i));
					}
					Token oldToken = tokenList.get(i);
					// if it's a negative scan (NOT 1* Noun), then the target of the next token becomes the barrier + SENT_END
					if (oldToken.negate) {	
						CgSet newTarget = oldToken.barrier;
						CgTag sentEndTag = new CgTag();
						sentEndTag.tag = SENT_END;
						newTarget.single_tags.add(sentEndTag);
						oldToken.target = newTarget;
						oldToken.postags = oldToken.target.getPostagsString();
						oldToken.baseforms = oldToken.target.getSingleTagBaseformsString();
						oldToken.surfaceforms = oldToken.target.getSingleTagSurfaceformsString();
						oldToken.compositeTags = oldToken.target.getCompositeTags();
						tokenList.set(0, oldToken);
					}
					tokenList.add(0, newToken);
				} else {
					int index = i-1;
					String exceptionString = null;
					if (!tokenList.get(i).barrier.isEmpty() || tokenList.get(i).negate) {
						exceptionString = getBarrierExceptionStringFromToken(tokenList.get(i));
					}
					
					int prevOffset = tokenList.get(index).offset;
					while (index >= 0 && tokenList.get(index).offset == prevOffset) {
						Token prevToken = tokenList.get(index);
						prevToken.skip = -1;
						prevToken.exceptionString = exceptionString;
						tokenList.set(index, prevToken);
						index--;
					}
					Token oldToken = tokenList.get(i);
					if (oldToken.negate) {
						CgSet newTarget = oldToken.barrier;
						CgTag sentEndTag = new CgTag();
						sentEndTag.tag = SENT_END;
						newTarget.single_tags.add(sentEndTag);
						oldToken.target = newTarget;
						oldToken.postags = oldToken.target.getPostagsString();
						oldToken.baseforms = oldToken.target.getSingleTagBaseformsString();
						oldToken.surfaceforms = oldToken.target.getSingleTagSurfaceformsString();
						oldToken.compositeTags = oldToken.target.getCompositeTags();
						oldToken.negate = false;
						tokenList.set(i,oldToken);
					}
				}
			}
			// reverse scans (-1* Verb)
			else if (tokenList.get(i).scanbehind) {
				Token newToken = new Token(new CgSet(), false, tokenList.get(i).offset - 1, false, false, new CgSet(), false, -1, false);
				String exceptionString = null;
				if (!tokenList.get(i).barrier.isEmpty() || tokenList.get(i).negate) {
					exceptionString = getBarrierExceptionStringFromToken(tokenList.get(i));
				}
				CgSet newTarget = newToken.target;
				CgTag sentStartTag = new CgTag();
				sentStartTag.tag = SENT_START;
				newTarget.single_tags.add(sentStartTag);
				newToken.target = newTarget;
				newToken.postags = newToken.target.getPostagsString();
				newToken.baseforms = newToken.target.getSingleTagBaseformsString();
				newToken.surfaceforms = newToken.target.getSingleTagSurfaceformsString();
				newToken.compositeTags = newToken.target.getCompositeTags();
				newToken.skip = -1;
				Token oldToken = tokenList.get(i);
				oldToken.skip = -1;
				oldToken.scanbehind = false;
				// if there's no barrier
				if (oldToken.barrier.isEmpty()) {
					if (oldToken.negate) {
						newToken.exceptionString = exceptionString;
						newToken.offset++;
						tokenList.set(i, newToken);
					} else {
						tokenList.set(i, oldToken);
						tokenList.add(i, newToken);
						i++;
					}
				}
				// if there IS a barrier
				else {
					if (oldToken.negate) {
						oldToken.target = oldToken.barrier;
						oldToken.postags = oldToken.target.getPostagsString();
						oldToken.baseforms = oldToken.target.getSingleTagBaseformsString();
						oldToken.surfaceforms = oldToken.target.getSingleTagSurfaceformsString();
						oldToken.compositeTags = oldToken.target.getCompositeTags();
						oldToken.exceptionString = exceptionString;
						tokenList.set(i,oldToken);
						tokenList.add(i,newToken);
					} else {
						oldToken.exceptionString = exceptionString;
						tokenList.set(i, oldToken);
						tokenList.add(i, newToken);
						i++;
					}
				}
			}
		}
		return tokenList.toArray(new Token[tokenList.size()]);
	}
	
	/**
	 * For cases where there needs to be an empty token inserted in order to make a proper LT pattern
	 * e.g. REMOVE N IF (1 Verb) (3 Det);
	 * @param tokens
	 * @return
	 */
	public Token[] addGapTokens(Token[] tokens) {
		boolean notdone = true;
		ArrayList<Token> tokenList = new ArrayList<Token>(Arrays.asList(tokens));
		while (notdone) {
			for (int i=0;i<tokenList.size();i++) {
				if (i == 0) continue;
				else if (i > 0 && i < tokenList.size()){
					if (tokenList.get(i).offset == tokenList.get(i-1).offset) {
						if (i == tokenList.size()-1) notdone = false;
						continue;
					}
					if (tokenList.get(i).offset != (tokenList.get(i-1).offset + 1) && tokenList.get(i).prevToken == null) {
						tokenList.add(i, new Token(new CgSet(), false, tokenList.get(i-1).offset + 1, false, false, new CgSet(), false, 0, false));
						break;
					}
				}
				if (i == tokenList.size() - 1) {
					notdone = false;
				}
			}
		}
		return tokenList.toArray(new Token[tokenList.size()]);
	}
	
	public ArrayList<Token> copyTokenList(ArrayList<Token> tokens) {
		ArrayList<Token> newList = new ArrayList<Token>();
		for (Token token : tokens) {
			newList.add(new Token(token));
		}
		return newList;
	}
	
	public ArrayList<Token> removeExtraEmptyTokens(ArrayList<Token> tokens) {
		if (tokens.size() == 1) {
			return tokens;
		} else {
			ArrayList<Token> newTokenList = new ArrayList<Token>();
			for (Token token : tokens) {
				if (!token.isEmpty()) {
					newTokenList.add(token);
				}
			}
			if (newTokenList.isEmpty()) {
				newTokenList.add(tokens.get(0));
			}
			return newTokenList;
		}
	}
	
	
	// ** METHODS THAT CHECK THE PROPERTIES OF A RULE **
	
	/**
	 * returns if the rule is safe to add the "skip" attribute to the previous token, as we do normally;
	 * otherwise, splits the tokens and returns multiple rules.
	 */
	public boolean skipSafe(Token[] tokens) {
		HashSet<Token> scanningTokens = new HashSet<Token>();
		HashSet<Token> reverseScanningTokens = new HashSet<Token>();
		HashSet<Token> normalTokens = new HashSet<Token>();
		for (Token token : tokens) {
			if (token.scanahead) {
				scanningTokens.add(token);
			} else if (token.scanbehind) {
				reverseScanningTokens.add(token);
			} else {
				normalTokens.add(token);
			}
		}
		for (Token s : scanningTokens) {
			for (Token o : normalTokens) {
				if (s.offset <= o.offset) return false;
			}
		}
		for (Token s : reverseScanningTokens) {
			for (Token o : normalTokens) {
				if (s.offset >= o.offset) return false;
			}
		}
		return true;
	}
	
	/**
	 * returns true of the rule contains an example of a negative backwards barrier scan (NOT -1* Adj BARRIER Noun), e.g. 
	 * these rules have to be treated as special cases
	 */
	public boolean negativeBackwardBarrierScan(Token[] tokens) {
		for (Token token : tokens) {
			if (token.scanbehind && token.negate && !token.barrier.isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param tokens
	 * @return
	 */
	public boolean singleRuleCompatible(Token[] tokens) {
		for (Token token : tokens) {
			if (!isOrCompatible(token)) return false;
		}
		return true;
	}
	
	/**
	 * Returns true if the {@link Token} can be represented in a single LT rule, false otherwise.
	 * Example: "are"|noun cannot be represented as one LT token
	 * @param token
	 * @return
	 */
	public boolean isOrCompatible(Token token) {
		if (token.postags.length > 0 && (token.baseforms.length > 0 || token.surfaceforms.length > 0)) {
			return false;
		}
		if (token.baseforms.length > 0 && token.surfaceforms.length > 0) {
			return false;
		}
		if (token.compositeTags.length > 0 && (token.postags.length > 0 || token.baseforms.length > 0 || token.surfaceforms.length > 0)) {
			return false;
		}
		HashSet<String> pos = new HashSet<String>();
		HashSet<String> base = new HashSet<String>();
		HashSet<String> surf = new HashSet<String>();
		for (CgCompositeTag ctag : token.compositeTags) {
			CgCompositeTag postagCompile = new CgCompositeTag();
			for (CgTag tag : ctag.tags) {
				if (isPostag(tag.tag)) {
					postagCompile.addTag(tag);
				} else if (isSurfaceForm(tag.tag)) {
					surf.add(tag.tag);
				} else if (isBaseForm(tag.tag)) {
					base.add(tag.tag);
				}
			}
			pos.add(postagCompile.toString());
		}
		if (pos.size() > 1 && (surf.size() > 1 || base.size() > 1)) {
			return false;
		}
		if (surf.size() > 1 && base.size() > 1) {
			return false;
		}
		if (surf.size() > 0 && base.size() > 0 && pos.size() > 0) {
			return false;
		}
		return true;
	}

	
	
	
	// ** METHODS THAT ACTUALLY GENERATE THE LT RULES **
	
	/**
	 * Actual LT rule generation
	 */
	@SuppressWarnings("unchecked")
	public List<String> getRuleByType(CgSet target, Token[] tokens, CgRule rule, String id, String name, String type) {
		ArrayList<String> ltRule = new ArrayList<String>();
		TreeMap<Integer,ArrayList<Token>> tokenmap = new TreeMap<Integer,ArrayList<Token>>();
		for (Token token : tokens) {
			tokenmap = (TreeMap)smartPut(tokenmap,token.offset,token);
		}
		
		if (name != null || id != null) {
			ltRule.add("<rule id=\"" + id + "\" name=\"" + name + "\">");
		} else {
			ltRule.add("<rule>");
		}
		
		int mark = getPositionOfTarget(tokens);
		ltRule.add(firstIndent + "<pattern mark=\"" + mark + "\">");
		for (Iterator<Integer> iter = tokenmap.keySet().iterator(); iter.hasNext();) {
			int key = iter.next();
			ArrayList<Token> value = tokenmap.get(key);
			// remove duplicates, so we don't have unnecessary "and"s floating around
			value = removeExtraEmptyTokens(value);
			if (value.size() == 1) {
				Token token = value.get(0);
				ltRule = addCgToken(ltRule,token,secondIndentInt);
			}
			// if the number of tokens at the given offset is more than 1, we have to and them together
			else {
				ltRule.add(secondIndent + "<and>");
				
				for (Token token : value) {
					ltRule = addCgToken(ltRule,token,thirdIndentInt);
				}
				ltRule.add(secondIndent + "</and>");
			}
			
		}
		ltRule.add(firstIndent + "</pattern>");
		// REMOVE
		if (type.equals("K_REMOVE")) {
			ltRule.add(firstIndent + "<disambig action=\"remove\">" + removeTarget(target) + "</disambig>");
		}
		// SELECT
		else if (type.equals("K_SELECT")) {
			ltRule.add(firstIndent + filterTarget(target, mark + 1));
		} 
		// MAP
		else if (type.equals("K_MAP")) {
			ltRule.add(firstIndent + "<disambig action=\"add\" postag=\"" + addRegexp(rule.maplist) + "\" postag_regexp=\"yes\"/>");
		}
		ltRule.add("</rule>");
		return ltRule;
	}
	
	/**
	 * Helper for getRuleByType
	 * @param ltRule
	 * @param token
	 * @param indent
	 * @return
	 */
	public ArrayList<String> addCgToken(ArrayList<String> ltRule, Token token, int indent) {
		String postags = postagsToString(token.postags);
		String baseforms = glueWords(cleanForms(token.baseforms));
		String surfaceforms = glueWords(cleanForms(token.surfaceforms));
		CgCompositeTag[] compositeTags = token.compositeTags;
		// should never have both composite tags and any of the postags/baseforms/surfaceforms
		// also, if there're composite tags, they should have the same postags component, and not both base and surface forms
		if (compositeTags.length != 0) {
			ArrayList<String> baseformsList = new ArrayList<String>();
			ArrayList<String> surfaceformsList = new ArrayList<String>();
			for (CgCompositeTag ctag : compositeTags) {
				CgCompositeTag postagCompiled = new CgCompositeTag();
				for (CgTag tag : ctag.tags) {
					if (isPostag(tag.tag)) {
						postagCompiled.addTag(tag);
					} else if (isBaseForm(tag.tag)) {
						baseformsList.add(tag.tag);
					} else if (isSurfaceForm(tag.tag)) {
						surfaceformsList.add(tag.tag);
					}
				}
				postags = compositePostagToString(postagCompiled);
			}
			baseforms = glueWords(cleanForms(baseformsList.toArray(new String[baseformsList.size()])));
			surfaceforms = glueWords(cleanForms(surfaceformsList.toArray(new String[surfaceformsList.size()])));
		}
		
		boolean careful = token.careful;
		boolean negate = token.negate;
		String exceptions = token.exceptionString;
		int skip = token.skip;
		
		// the special case of the generic token:
		if (postags.equals("") && baseforms.equals("") && surfaceforms.equals("")) {
			ltRule = addToken(ltRule,baseforms,postags,exceptions,careful,false,negate,skip,indent);
			return ltRule;
		}
		
		if (!baseforms.equals("")) {
			ltRule = addToken(ltRule, baseforms, postags, exceptions, careful, true, negate, skip, indent);
		} else if (!surfaceforms.equals("")) {
			ltRule = addToken(ltRule, surfaceforms, postags, exceptions, careful, false, negate, skip, indent);
		} else {
			ltRule = addToken(ltRule, surfaceforms, postags, exceptions, careful, false, negate, skip, indent);
		}
		return ltRule;
	}
	
	/**
	 * Helper for getRuleByType
	 * @param token
	 * @return
	 */
	public String getBarrierExceptionStringFromToken(Token token) {
		boolean not = token.negate;
		boolean inflected = true;
		String barrierPos = glueWords(expandSetSets(token.barrier).getPostagsString());
		
		if (token.scanahead) {
			barrierPos = barrierPos.concat("|" + SENT_END);
		}
		String barrierToken = glueWords(token.barrier.getSingleTagBaseformsString());
		if (barrierToken.isEmpty()) {
			barrierToken = glueWords(token.barrier.getSingleTagSurfaceformsString());
			inflected = false;
		}
		String targetPos = glueWords(token.postags);
		String targetToken = glueWords(token.baseforms);
		if (targetToken.isEmpty()) {
			targetToken = glueWords(token.surfaceforms);
			if (not) inflected = false;
		}
		String postagString = "";
		String tokenString = "";
		String inflectedString = "";
		String regexpString = "";
		String postagRegexpString = "";
		if (not) {
			if (!targetPos.isEmpty()) {
				postagString = " postag=\"" + targetPos + "\"";
				if (isRegex(targetPos)) {
					postagRegexpString = " postag_regexp=\"yes\"";
				}
			}
			if (!targetToken.isEmpty()) {
				tokenString = " ".concat(targetToken);
				if (isRegex(tokenString)) {
					regexpString = " regexp=\"yes\"";
				}
			}
		} else {
			if (!barrierPos.isEmpty()) {
				postagString = " postag=\"" + barrierPos + "\"";
			}
			if (isRegex(barrierPos)) {
				postagRegexpString = " postag_regexp=\"yes\"";
			}
			if (!barrierToken.isEmpty()) {
				tokenString = barrierToken;
				if (isRegex(tokenString)) {
					regexpString = " postag_regexp=\"yes\"";
				}
			}
		}
		if (inflected) {
			inflectedString = " inflected=\"yes\"";
		}
		String retString = "<exception" + postagString + inflectedString + regexpString + postagRegexpString + " scope=\"next\">" + tokenString + "</exception>";
		return retString;
	}
	
	/** 
	 * Returns the position of the target token, which is always relative to the furthest back token in the rules
	 * @param tokens
	 * @return
	 */
	public int getPositionOfTarget(Token[] tokens) {
		Token firstToken = tokens[0];
		return -1 * firstToken.offset;
	}
	
	/**
	 * Helper that takes a normal contextual test (i.e. not a Parent or a Linked test, e.g. (1 Noun))
	 * and returns the properly filled-out Token object
	 * @param test
	 * @return
	 */
	public Token getTokenFromNormalTest(CgContextualTest test) {
		CgSet testTarget = expandSetSets(grammar.getSet(test.target));
		boolean testCareful = test.pos.contains(POS.POS_CAREFUL.value);
		int testOffset = test.offset;
		boolean testScanAhead = test.pos.contains(POS.POS_SCANFIRST.value) && testOffset >= 0;
		boolean testScanBehind = test.pos.contains(POS.POS_SCANFIRST.value) && testOffset < 0;
		boolean testNot = test.pos.contains(POS.POS_NOT.value);
		CgSet testBarrier = grammar.getSet(test.barrier);
		CgSet testCBarrier = grammar.getSet(test.cbarrier);
		CgSet barrier = null;
		boolean cbarrier = false;
		if (testBarrier != null && testCBarrier != null) {
			System.err.println("Can't have both a barrier and a careful barrier");
			System.exit(1);
		}
		if (testBarrier != null) {
			barrier = testBarrier;
			cbarrier = false;
		} else if (testCBarrier != null) {
			barrier = testCBarrier;
			cbarrier = true;
		} else {
			barrier = new CgSet();
			cbarrier = false;
		}
		if (test.line == 548 && test.offset == 1) {
			System.out.println();
		}
		return new Token(testTarget,testCareful,testOffset,testScanAhead,testScanBehind,barrier,cbarrier,0,testNot);
		
	}
	
	/**
	 * takes a CgSet and, if it contains nested sets, expands them according to the proper
	 * set operators (set_ops) and returns the new set.
	 */
	public CgSet expandSetSets(CgSet set) {
		CgSet newSet = new CgSet();
		newSet.line = set.line;
		newSet.type = set.type;
		newSet.name	= set.name;
		if (set.sets.isEmpty()) {
			return set;
		}
		else if (set.sets.size() > 1 && set.set_ops.isEmpty()) {
			System.err.println("Error: something wonky with the set on line " + set.line);
			System.exit(1);
		}
		else if (set.set_ops.isEmpty()) {
			CgSet expandedSet = expandSetSets(grammar.getSet(set.sets.get(0)));
			for (CgCompositeTag ctag : expandedSet.tags) {
				newSet.tags.add(ctag);
			}
			for (CgTag tag : expandedSet.single_tags) {
				newSet.single_tags.add(tag);
			}
		}
		else {
			for (int op=0;op<set.set_ops.size();op++) {
				CgSet expandedSet1 = expandSetSets(grammar.getSet(set.sets.get(op)));
				CgSet expandedSet2 = expandSetSets(grammar.getSet(set.sets.get(op+1)));
				// Cartesian set product (+)
				if (set.set_ops.get(op) == 4) {
					for (CgTag tag : expandedSet1.single_tags) {
						for (CgTag tag2 : expandedSet2.single_tags) {
							if (tag.tag.equals(tag2.tag)) {
								newSet.addTag(tag);
							} else {
								CgCompositeTag ctag = new CgCompositeTag();
								ctag.addTag(tag);
								ctag.addTag(tag2);
								newSet.addCompositeTag(ctag);
							}
						}
					}
					for (CgCompositeTag ctag : expandedSet1.tags) {
						for (CgTag tag : expandedSet2.single_tags) {
							if (ctag.tags.contains(tag)) {
								newSet.addCompositeTag(ctag);
							} else {
								CgCompositeTag ctagnew = new CgCompositeTag();
								for (CgTag tag2 : ctag.tags) {
									ctagnew.addTag(tag2);
								}
								ctagnew.addTag(tag);
								newSet.addCompositeTag(ctagnew);
							}
						}
					}
					for (CgCompositeTag ctag : expandedSet2.tags) {
						for (CgTag tag : expandedSet1.single_tags) {
							if (ctag.tags.contains(tag)) {
								newSet.addCompositeTag(ctag);
							} else {
								CgCompositeTag ctagnew = new CgCompositeTag();
								for (CgTag tag2 : ctag.tags) {
									ctagnew.addTag(tag2);
								}
								ctagnew.addTag(tag);
								newSet.addCompositeTag(ctagnew);
							}
						}
					}
					for (CgCompositeTag ctag : expandedSet1.tags) {
						for (CgCompositeTag ctag2 : expandedSet2.tags) {
							CgCompositeTag ctagnew = new CgCompositeTag();
							for (CgTag tag : ctag.tags) {
								if (!ctagnew.tags.contains(tag)) {
									ctagnew.addTag(tag);
								}
							}
							for (CgTag tag : ctag2.tags) {
								if (!ctagnew.tags.contains(tag)) {
									ctagnew.addTag(tag);
								}
							}
							newSet.addCompositeTag(ctagnew);
						}
					}
				}
				// OR or | 
				else if (set.set_ops.get(op) == 3) {
					for (CgCompositeTag ctag : expandedSet1.tags) {
						newSet.addCompositeTag(ctag);
					}
					for (CgCompositeTag ctag : expandedSet2.tags) {
						newSet.addCompositeTag(ctag);
					}
					for (CgTag tag : expandedSet1.single_tags) {
						newSet.addTag(tag);
					}
					for (CgTag tag : expandedSet2.single_tags) {
						newSet.addTag(tag);
					}
				}
			}
		}
		return newSet;
	}
	

	
	
	
	
	
	
	
	
	@Override
	public String generateName(Object ruleObject) {
		CgRule rule = (CgRule) ruleObject;
		String name = rule.name;
		if (name == null) {
			name = "rule_" + nameIndex;
			nameIndex++;
		}
		return name;
	}
	
	@Override
	public String generateId(Object ruleObject) {
		CgRule rule = (CgRule) ruleObject;
		String name = rule.name;
		if (name == null) {
			name = "rule_" + idIndex;
			idIndex++;
		}
		return name;
	}
	
	@Override
	public String getOriginalRuleString(Object ruleObject) {
		CgRule rule = (CgRule) ruleObject;
		return lines[rule.line];
	}
	
	@Override
	public String[] getAcceptableFileTypes() {
		String[] ft = {"default"};
		return ft;
	}
	
	
	/**
	 * Token class: contains the elements that will be written into <token> elements in the resulting LT rule
	 * @author mbryant
	 *
	 */
	public class Token implements Comparable<Token> {

		// target consists of the expanded CgSet, with no nested sets.
		public CgSet target;
		public String[] postags;
		public String[] surfaceforms;
		public String[] baseforms;
		public CgCompositeTag[] compositeTags;
		public boolean careful;
		public int offset;
		public boolean scanahead;
		public boolean scanbehind;
		public CgSet barrier;
		public boolean cbarrier;
		public int skip;
		public boolean negate;
		public int relativeOffset;
		public Token nextToken;
		public Token prevToken;
		public String exceptionString;
		
		public Token() {
			// nothing
		}
		
		// copy constructor
		public Token(Token another) {
			this.target = new CgSet(another.target);
			this.postags = target.getPostagsString();
			this.surfaceforms = target.getSingleTagSurfaceformsString();
			this.baseforms = target.getSingleTagBaseformsString();
			this.compositeTags = target.getCompositeTags();
			this.careful = another.careful;
			this.offset = another.offset;
			this.scanahead = another.scanahead;
			this.scanbehind = another.scanbehind;
			this.barrier = new CgSet(another.barrier);
			this.cbarrier = another.cbarrier;
			this.skip = another.skip;
			this.negate = another.negate;
			this.nextToken = another.nextToken;
			this.prevToken = another.prevToken;
			this.relativeOffset = another.relativeOffset;
			this.exceptionString = another.exceptionString;
		}
		
		public Token(CgSet target,
					 boolean careful, int offset, boolean scanahead, boolean scanbehind,
					 CgSet barrier, boolean cbarrier, int skip, boolean negate) {
			this.target = target;
			this.postags = target.getPostagsString();
			this.surfaceforms = target.getSingleTagSurfaceformsString();
			this.baseforms = target.getSingleTagBaseformsString();
			this.compositeTags = target.getCompositeTags();
			this.careful = careful;
			this.offset = offset;
			this.scanahead = scanahead;
			this.scanbehind = scanbehind;
			this.barrier = barrier;
			this.cbarrier = cbarrier;
			this.skip = skip;
			this.negate = negate;
			this.nextToken = null;
			this.prevToken = null;
			this.relativeOffset = 0;
		}
		
		public int compareTo(Token token) {
			if (this.offset < token.offset) {
				return -1;
			} else if (this.offset == token.offset) {
				return 0;
			} else {
				return 1;
			}
		}
		
		public boolean isEmpty() {
			return (this.postags.length == 0) &&
				   (this.baseforms.length == 0) &&
				   (this.surfaceforms.length == 0) &&
				   (this.compositeTags.length == 0);
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (String postag : this.postags) {
				sb.append(postag + " ");
			}
			for (String baseform : this.baseforms) {
				sb.append(baseform + " ");
			}
			for (String surfaceform : this.surfaceforms) {
				sb.append(surfaceform + " ");
			}
			for (CgCompositeTag ctag : this.compositeTags) {
				sb.append(ctag.toString() + " ");
			}
			return sb.toString();
		}
	}
	
	
	// ** SOME STATIC STRING-CHECKING METHODS **
	
	public static boolean isPostag(String tag) {
		return !(tag.matches("\"\\<.*\\>\"r?i?") || tag.matches("\".*\"r?i?"));
	}
	
	public static boolean isSurfaceForm(String form) {
		return (form.matches("\"\\<.*\\>\"r?i?") || form.matches("\"\"\\<.*\\>\"r?i?\""));
	}
	
	public static boolean isBaseForm(String form) {
		return (form.matches("\"[^<]*[^>]\"r?i?") || form.matches("\"\"[^<]*[^>]\"r?i?\""));
	}
	
	public static boolean isCompositePostag(CgCompositeTag ctag) {
		for (CgTag tag : ctag.tags) {
			if (isBaseForm(tag.tag) || isSurfaceForm(tag.tag)) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean sameStrings(String[] s1, String[] s2) {
		if (s1.length != s2.length) {
			return false;
		} else {
			for (String ss1 : s1) {
				for (String ss2 : s2) {
					if (!ss1.equals(ss2)) return false;
				}
			}
		}
		return true;
	}
	
	
	
	// ** METHODS THAT RETURN WRITABLE FORMS FOR CONSTRAINT GRAMMAR TAGS/SETS **
	
	/** 
	 * Removes the quotation marks, angle brackets, and suffixes from surface/base forms
	 * @param words
	 * @return
	 */
	public static String[] cleanForms(String[] words) {
		for (int i=0;i<words.length;i++) {
			words[i] = words[i].replaceAll(">\"r?i?", "").replaceAll(">\"", "").replaceAll("\"<","").replaceAll("\"r?i?", "");
		}
		return words;
	}
	
	// this is problematic, in that I don't know how to write these regular expressions when we have
	// different morphological tag syntax, as Polish or French.
	public static String filterRegexp(CgSet target) {
		String[] postags = target.getPostagsString();
		String postagString = glueWords(postags);
		postagString = "(".concat(postagString).concat(")");
		String postagRegexp = toStringRegexpFormat(postagString);
		
		StringBuilder sb = new StringBuilder();
		sb.append("(?!");
		sb.append(postagRegexp);
		sb.append(").*");
		return sb.toString();
	}
	
	public static String filterTarget(CgSet target, int targetNo) {
		// <match no=\"" + (mark + 1) + "\" postag=\"" + replaceRegexp(target) + "\" postag_regexp=\"yes\"/>
		StringBuilder sb = new StringBuilder();
		
		String[] lemmas = cleanForms(target.getSingleTagBaseformsString());
		String[] postags = cleanForms(target.getPostagsString());
		CgCompositeTag[] compositeTags = target.getCompositeTags();
		String[] surfaceforms = cleanForms(target.getSingleTagSurfaceformsString());
		
		if (lemmas.length > 0 && (compositeTags.length > 0 || postags.length > 0 || surfaceforms.length > 0)) {
			System.err.println("Error: something went wrong here.");
		}
		
		// assumes there can't be both lemmas and postags
		if (lemmas.length > 0) {
			sb.append("<disambig action=\"filter\"><match no=\"" + targetNo + "\">" + glueWords(lemmas) + "</match></disambig>");
		}
		if (postags.length > 0) {
			String postagRegexp = "";
			if (isRegex(glueWords(postags))) {
				postagRegexp = " postag_regexp=\"yes\"";
			}
			sb.append("<disambig postag=\"" + glueWords(postags) + "\"" + postagRegexp + "/>");
		}
		
		return sb.toString();
		
	}
	
	// formats the target for use with disambiguation action="remove" keyword
	// assuming they support regular expressions, which they currently don't but kind of have to in order to work
	public static String removeTarget(CgSet target) {
		StringBuilder sb = new StringBuilder();
		sb.append("<wd ");
		// these should always be the correct lengths, because if they weren't, they should have been split earlier.
		String[] lemmas = target.getSingleTagBaseformsString();
		String[] postags = target.getPostagsString();
		CgCompositeTag[] compositeTags = target.getCompositeTags();
		String[] surfaceforms = target.getSingleTagSurfaceformsString();
		
		if (lemmas.length > 0 && (compositeTags.length > 0 || postags.length > 0 || surfaceforms.length > 0)) {
			System.err.println("Error: something went wrong here.");
		}
		
		if (lemmas.length > 0) {
			sb.append("lemma=" + glueWords(lemmas));
		}
		if (postags.length > 0) {
			sb.append("pos=\"" + glueWords(postags) + "\"");
		}
		
		sb.append("/>");
		return sb.toString();
	}
	
	public static String replaceRegexp(CgSet target) {
		String[] postags = target.getPostagsString();
		String postagString = glueWords(postags);
		postagString = "(".concat(postagString).concat(")");
		String postagRegexp = toStringRegexpFormat(postagString);
		return postagRegexp;
		
	}
	
	public static String addRegexp(CgSet target) {
		String[] postags = target.getSingleTagPostagsString();
		if (postags.length != 1) {
			System.err.println("Error: trying to map more than one mapping tag on line " + target.line);
			System.exit(1);
		}
		String postag = postags[0];
		return postag;
	}
	
	// ** LANGUAGE-DEPENDENT METHODS **
	
	public static String postagsToString(String[] postags) {
		
		if (postags.length == 0) {
			return "";
		}
		/*
		StringBuilder sb = new StringBuilder();
		sb.append("(.*" + tagDelimiter + ")?");
		String postagsGlued = glueWords(postags);
		sb.append(postagsGlued);
		sb.append("(" + tagDelimiter + ".*)?");
		*/
		// The simplest possible way to do this
		StringBuilder sb = new StringBuilder();
		for (String pos : postags) {
			sb.append(pos + "|");
		}
		String ret = sb.toString();
		return ret.substring(0,ret.length() - 1);
	}
	
	public static String toStringRegexpFormat(String t) {
		/*
		StringBuilder sb = new StringBuilder();
		sb.append("^(.*" + tagDelimiter + ")?");
		sb.append(t);
		sb.append("(" + tagDelimiter + ".*)?$");
		return sb.toString();
		*/
		return t;
		
	}
	
	//TODO: only a stand-in for now; depends on the language-specific multiple-tag string representation
	// some complicated regex stuff going on here.
	// only should be applied to composite postags. Composite tags with postags + s/b-forms get split in different ways
	public static String compositePostagToString(CgCompositeTag ctag) {
		/*
		StringBuilder sb = new StringBuilder();
		String gluedPostag = "";
		int noComponents = 0;
		for (CgTag tag : ctag.tags) {
			if (isPostag(tag.tag)) {
				gluedPostag = gluedPostag.concat(tag.tag).concat("|");
				noComponents++;
			}
		}
		gluedPostag = "(".concat(gluedPostag.substring(0, gluedPostag.length() - 1)).concat(")");
		sb.append("^(.*" + tagDelimiter + ")?");
		for (int i=0;i<noComponents;i++) {
			sb.append(gluedPostag);
			if (i < noComponents - 1) {
				sb.append("(" + tagDelimiter + ".*" + tagDelimiter + "?)");
			}			
		}
		sb.append("(" + tagDelimiter + ".*)?$");
		*/
		// The simplest possible way to represent this
		StringBuilder sb = new StringBuilder();
		for (CgTag tag : ctag.tags) {
			if (isPostag(tag.tag)) {
				sb.append(tag.tag);
				sb.append(tagDelimiter);
			}
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(".*");
		return sb.toString();
	}
	
	// language-specific way of representing tags. Relies on a tagDelimiter, which appears to be different in different languages.
	// For example, in French, the tags look like: "N f s" (i.e. Noun feminine singular). But in Polish they look like "N:f:s" (same).
	// so this syntax takes care of both of those cases (as long as you change the tagDelimiter), wherever in the postag string 
	// the tag appears.
	public static String tagToString(CgTag tag) {
		/*
		if (tag.tag.equals(sent_end)) {
			return sent_end;
		}
		if (tag.tag.equals(sent_start)) {
			return sent_start;
		}
		StringBuilder sb = new StringBuilder();
		String t = tag.tag;
		sb.append("^" + t + "$|");											// the tag is the only tag
		sb.append("^" + t + tagDelimiter + ".*|");							// the tag is the first tag
		sb.append(".*" + tagDelimiter + t + tagDelimiter + ".*|");			// the tag is in the middle somewhere
		sb.append(".*" + tagDelimiter + t + "$");							// the tag is the last tag
		
		return sb.toString();
		*/
		// The simplest possible way to do this
		return tag.tag;
	}
	
	/**
	 * Helper to properly put an item to a map where the values are lists
	 * @param <K>
	 * @param <V>
	 * @param map
	 * @param key
	 * @param value
	 * @return
	 */
	public static <K,V> Map<K,ArrayList<V>> smartPut(Map<K,ArrayList<V>> map, K key, V value) {
		if (map.containsKey(key)) {
			ArrayList<V> original = map.get(key);
			original.add(value);
			map.put(key, original);
		} else {
			ArrayList<V> newcollection = new ArrayList<V>();
			newcollection.add(value);
			map.put(key, newcollection);
		}
		return map;
	}

}
