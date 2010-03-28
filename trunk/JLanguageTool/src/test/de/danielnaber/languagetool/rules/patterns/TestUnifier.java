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

package de.danielnaber.languagetool.rules.patterns;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import junit.framework.TestCase;
import de.danielnaber.languagetool.AnalyzedToken;

public class TestUnifier extends TestCase {

  // trivial unification = test if the character case is the same
  public void testUnificationCase() {
  Unifier uni = new Unifier();
  Element elLower = new Element("\\p{Ll}+", true, true, false); 
  Element elUpper = new Element("\\p{Lu}\\p{Ll}+", true, true, false);
  Element elAllUpper = new Element("\\p{Lu}+$", true, true, false);
  uni.setEquivalence("case-sensitivity", "lowercase", elLower);
  uni.setEquivalence("case-sensitivity", "uppercase", elUpper);
  uni.setEquivalence("case-sensitivity", "alluppercase", elAllUpper);
  AnalyzedToken lower1 = new AnalyzedToken("lower", "JJR", "lower");
  AnalyzedToken lower2 = new AnalyzedToken("lowercase", "JJ", "lowercase");
  AnalyzedToken upper1 = new AnalyzedToken("Uppercase", "JJ", "Uppercase");
  AnalyzedToken upper2 = new AnalyzedToken("John", "NNP", "John");
  AnalyzedToken upperall1 = new AnalyzedToken("JOHN", "NNP", "John");
  AnalyzedToken upperall2 = new AnalyzedToken("JAMES", "NNP", "James");
  
  Map<String, List<String>> equiv = new HashMap<String, List<String>>();
  List<String> list1 = new ArrayList<String>();
  list1.add("lowercase");
  equiv.put("case-sensitivity", list1);
  boolean satisfied = uni.isSatisfied(lower1, equiv);
  satisfied &= uni.isSatisfied(lower2, equiv);
  uni.startUnify();
  assertEquals(true, satisfied);
  uni.reset();
  satisfied = uni.isSatisfied(upper2, equiv);
  uni.startUnify();
  satisfied &= uni.isSatisfied(lower2, equiv);
  assertEquals(false, satisfied);
  uni.reset();
  satisfied = uni.isSatisfied(upper1, equiv);
  uni.startUnify();
  satisfied &= uni.isSatisfied(lower1, equiv);
  assertEquals(false, satisfied);
  uni.reset();
  satisfied = uni.isSatisfied(upper2, equiv);
  uni.startUnify();
  satisfied &= uni.isSatisfied(upper1, equiv);
  assertEquals(false, satisfied);
  uni.reset();
  equiv.clear();
  list1.clear();
  list1.add("uppercase");
  equiv.put("case-sensitivity", list1);
  satisfied = uni.isSatisfied(upper2, equiv);
  uni.startUnify();
  satisfied &= uni.isSatisfied(upper1, equiv);
  assertEquals(true, satisfied);
  uni.reset();
  equiv.clear();
  list1.clear();
  list1.add("alluppercase");
  equiv.put("case-sensitivity", list1);
  satisfied = uni.isSatisfied(upper2, equiv);
  uni.startUnify();
  satisfied &= uni.isSatisfied(upper1, equiv);
  assertEquals(false, satisfied);
  uni.reset();
  satisfied = uni.isSatisfied(upperall2, equiv);
  uni.startUnify();
  satisfied &= uni.isSatisfied(upperall1, equiv);
  assertEquals(true, satisfied);
  }
  
  // slightly non-trivial unification = 
  // test if the grammatical number is the same
  public void testUnificationNumber() {
  Unifier uni = new Unifier();
  Element sgElement = new Element("", false, false, false);
  sgElement.setPosElement(".*[\\.:]sg:.*", true, false);
  uni.setEquivalence("number", "singular", sgElement);
  Element plElement = new Element("", false, false, false);
  plElement.setPosElement(".*[\\.:]pl:.*", true, false);
  uni.setEquivalence("number", "plural", plElement);
  
  AnalyzedToken sing1 = new AnalyzedToken("mały", "adj:sg:blahblah", "mały");
  AnalyzedToken sing2 = new AnalyzedToken("człowiek", "subst:sg:blahblah", "człowiek");
  
  Map<String, List<String>> equiv = new HashMap<String, List<String>>();
  List<String> list1 = new ArrayList<String>();
  list1.add("singular");
  equiv.put("number", list1);
  
  boolean satisfied = uni.isSatisfied(sing1, equiv);
  uni.startUnify();
  satisfied &= uni.isSatisfied(sing2, equiv);  
  assertEquals(true, satisfied);
  uni.reset();
  
  //for multiple readings - OR for interpretations, AND for tokens
  AnalyzedToken sing1a = new AnalyzedToken("mały", "adj:pl:blahblah", "mały"); 
  satisfied = uni.isSatisfied(sing1, equiv);
  satisfied |= uni.isSatisfied(sing1a, equiv);
  uni.startUnify();
  satisfied &= uni.isSatisfied(sing2, equiv);
  assertEquals(true, satisfied);
  uni.reset();
  
  //check if any of the equivalences is there
  list1.add("plural");
  equiv.clear();
  equiv.put("number", list1);
  sing1a = new AnalyzedToken("mały", "adj:pl:blahblah", "mały"); 
  satisfied = uni.isSatisfied(sing1, equiv);
  satisfied |= uni.isSatisfied(sing1a, equiv);
  uni.startUnify();
  satisfied &= uni.isSatisfied(sing2, equiv);
  assertEquals(true, satisfied);
  uni.reset();
  
//now test all possible feature equivalences by leaving type blank
  sing1a = new AnalyzedToken("mały", "adj:pl:blahblah", "mały");
  equiv.clear();
  equiv.put("number", null);
  satisfied = uni.isSatisfied(sing1, equiv);
  satisfied |= uni.isSatisfied(sing1a, equiv);
  uni.startUnify();
  satisfied &= uni.isSatisfied(sing2, equiv);
  assertEquals(true, satisfied);
  uni.reset();

//test non-agreeing tokens with blank types   
  satisfied = uni.isSatisfied(sing1a, equiv);
  uni.startUnify();
  satisfied &= uni.isSatisfied(sing2, equiv);
  assertEquals(false, satisfied);
  uni.reset();    
  }
  
//slightly non-trivial unification = 
  // test if the grammatical number is the same
  public void testUnificationNumberGender() {
  Unifier uni = new Unifier();
  Element sgElement = new Element("", false, false, false);
  sgElement.setPosElement(".*[\\.:]sg:.*", true, false);
  uni.setEquivalence("number", "singular", sgElement);
  Element plElement = new Element("", false, false, false);
  plElement.setPosElement(".*[\\.:]pl:.*", true, false);
  uni.setEquivalence("number", "plural", plElement);
  
  Element femElement = new Element("", false, false, false);
  femElement.setPosElement(".*[\\.:]f", true, false);
  uni.setEquivalence("gender", "feminine", femElement);
  
  Element mascElement = new Element("", false, false, false);
  mascElement.setPosElement(".*[\\.:]m", true, false);
  uni.setEquivalence("gender", "masculine", mascElement);
  
  AnalyzedToken sing1 = new AnalyzedToken("mały", "adj:sg:blahblah:m", "mały");
  AnalyzedToken sing1a = new AnalyzedToken("mały", "adj:sg:blahblah:f", "mały");
  AnalyzedToken sing1b = new AnalyzedToken("mały", "adj:pl:blahblah:m", "mały");
  AnalyzedToken sing2 = new AnalyzedToken("człowiek", "subst:sg:blahblah:m", "człowiek");
  
  Map<String, List<String>> equiv = new HashMap<String, List<String>>();
  equiv.put("number", null);
  equiv.put("gender", null);
  
  boolean satisfied = uni.isSatisfied(sing1, equiv);
  satisfied |= uni.isSatisfied(sing1a, equiv);
  satisfied |= uni.isSatisfied(sing1b, equiv);
  uni.startUnify();
  satisfied &= uni.isSatisfied(sing2, equiv);
  uni.startNextToken();
  assertEquals(true, satisfied);
  assertEquals("[mały/adj:sg:blahblah:m, człowiek/subst:sg:blahblah:m]", Arrays.toString(uni.getUnifiedTokens()));
  uni.reset();    
  }
  
  // checks if all tokens share the same set of 
  // features to be unified
  public void testMultiplefeats() {
  Unifier uni = new Unifier();
  Element sgElement = new Element("", false, false, false);
  sgElement.setPosElement(".*[\\.:]sg:.*", true, false);
  uni.setEquivalence("number", "singular", sgElement);
  Element plElement = new Element("", false, false, false);
  plElement.setPosElement(".*[\\.:]pl:.*", true, false);
  uni.setEquivalence("number", "plural", plElement);
  Element femElement = new Element("", false, false, false);
  femElement.setPosElement(".*[\\.:]f([\\.:].*)?", true, false);
  uni.setEquivalence("gender", "feminine", femElement);
  Element mascElement = new Element("", false, false, false);
  mascElement.setPosElement(".*[\\.:]m([\\.:].*)?", true, false);
  uni.setEquivalence("gender", "masculine", mascElement);
  Element neutElement = new Element("", false, false, false);
  neutElement.setPosElement(".*[\\.:]n([\\.:].*)?", true, false);
  uni.setEquivalence("gender", "neutral", neutElement);  
  
  AnalyzedToken sing1 = new AnalyzedToken("mały", "adj:sg:blahblah:m", "mały");
  AnalyzedToken sing1a = new AnalyzedToken("mały", "adj:pl:blahblah:f", "mały");
  AnalyzedToken sing1b = new AnalyzedToken("mały", "adj:pl:blahblah:f", "mały");
  AnalyzedToken sing2 = new AnalyzedToken("zgarbiony", "adj:pl:blahblah:f", "zgarbiony");
  AnalyzedToken sing3 = new AnalyzedToken("człowiek", "subst:sg:blahblah:m", "człowiek");
  
  Map<String, List<String>> equiv = new HashMap<String, List<String>>();
  equiv.put("number", null);
  equiv.put("gender", null);
  
  boolean satisfied = uni.isSatisfied(sing1, equiv);
  satisfied |= uni.isSatisfied(sing1a, equiv);
  satisfied |= uni.isSatisfied(sing1b, equiv);
  uni.startUnify();
  satisfied &= uni.isSatisfied(sing2, equiv);
  uni.startNextToken();
  satisfied &= uni.isSatisfied(sing3, equiv);
  uni.startNextToken();
  assertEquals(false, satisfied);  
  uni.reset();
  
  //now test the simplified interface
  satisfied = true; //this must be true to start with...
  satisfied &= uni.isUnified(sing1, equiv, false, false);
  satisfied &= uni.isUnified(sing1a, equiv, false, false);
  satisfied &= uni.isUnified(sing1b, equiv, false, true);
  satisfied &= uni.isUnified(sing2, equiv, false, true);
  satisfied &= uni.isUnified(sing3, equiv, false, true);
  assertEquals(false, satisfied);
  uni.reset();
  
  sing1a = new AnalyzedToken("osobiste", "adj:pl:nom.acc.voc:f.n.m2.m3:pos:aff", "osobisty");
  sing1b = new AnalyzedToken("osobiste", "adj:sg:nom.acc.voc:n:pos:aff", "osobisty");
  sing2 = new AnalyzedToken("godło", "subst:sg:nom.acc.voc:n", "godło");
  
  satisfied = true;
  satisfied &= uni.isUnified(sing1a, equiv, false, false);
  satisfied &= uni.isUnified(sing1b, equiv, false, true);
  satisfied &= uni.isUnified(sing2, equiv, false, true);
  assertEquals(true, satisfied);
  assertEquals("[osobisty/adj:sg:nom.acc.voc:n:pos:aff, godło/subst:sg:nom.acc.voc:n]", Arrays.toString(uni.getFinalUnified()));
  uni.reset();
  
  //now test a case when the last reading doesn't match at all
  
  sing1a = new AnalyzedToken("osobiste", "adj:pl:nom.acc.voc:f.n.m2.m3:pos:aff", "osobisty");
  sing1b = new AnalyzedToken("osobiste", "adj:sg:nom.acc.voc:n:pos:aff", "osobisty");
  AnalyzedToken sing2a = new AnalyzedToken("godło", "subst:sg:nom.acc.voc:n", "godło");
  AnalyzedToken sing2b = new AnalyzedToken("godło", "indecl", "godło");
  
  satisfied = true;
  satisfied &= uni.isUnified(sing1a, equiv, false, false);
  satisfied &= uni.isUnified(sing1b, equiv, false, true);
  satisfied &= uni.isUnified(sing2a, equiv, false, false);
  satisfied &= uni.isUnified(sing2b, equiv, false, true);
  assertEquals(true, satisfied);
  assertEquals("[osobisty/adj:sg:nom.acc.voc:n:pos:aff, godło/subst:sg:nom.acc.voc:n]", Arrays.toString(uni.getFinalUnified()));
  uni.reset();
  
  }
    
  
}
