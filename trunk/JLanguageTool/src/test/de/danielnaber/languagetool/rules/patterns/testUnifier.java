package de.danielnaber.languagetool.rules.patterns;

import junit.framework.TestCase;
import de.danielnaber.languagetool.AnalyzedToken;

public class testUnifier extends TestCase {

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
  
  boolean satisfied = uni.isSatisfied(lower1, "case-sensitivity", "lowercase");
  satisfied &= uni.isSatisfied(lower2, "case-sensitivity", "lowercase");
  uni.startUnify();
  assertEquals(true, satisfied);
  uni.reset();
  satisfied = uni.isSatisfied(upper2, "case-sensitivity", "lowercase");
  uni.startUnify();
  satisfied &= uni.isSatisfied(lower2, "case-sensitivity", "lowercase");
  assertEquals(false, satisfied);
  uni.reset();
  satisfied = uni.isSatisfied(upper1, "case-sensitivity", "lowercase");
  uni.startUnify();
  satisfied &= uni.isSatisfied(lower1, "case-sensitivity", "lowercase");
  assertEquals(false, satisfied);
  uni.reset();
  satisfied = uni.isSatisfied(upper2, "case-sensitivity", "lowercase");
  uni.startUnify();
  satisfied &= uni.isSatisfied(upper1, "case-sensitivity", "lowercase");
  assertEquals(false, satisfied);
  uni.reset();
  satisfied = uni.isSatisfied(upper2, "case-sensitivity", "uppercase");
  uni.startUnify();
  satisfied &= uni.isSatisfied(upper1, "case-sensitivity", "uppercase");
  assertEquals(true, satisfied);
  uni.reset();
  satisfied = uni.isSatisfied(upper2, "case-sensitivity", "alluppercase");
  uni.startUnify();
  satisfied &= uni.isSatisfied(upper1, "case-sensitivity", "alluppercase");
  assertEquals(false, satisfied);
  uni.reset();
  satisfied = uni.isSatisfied(upperall2, "case-sensitivity", "alluppercase");
  uni.startUnify();
  satisfied &= uni.isSatisfied(upperall1, "case-sensitivity", "alluppercase");
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
  boolean satisfied = uni.isSatisfied(sing1, "number", "singular");
  satisfied &= uni.isSatisfied(sing2, "number", "singular");
  uni.startUnify();
  assertEquals(true, satisfied);
  uni.reset();
  
  //for multiple readings - OR for interpretations, AND for tokens
  AnalyzedToken sing1a = new AnalyzedToken("mały", "adj:pl:blahblah", "mały"); 
  satisfied = uni.isSatisfied(sing1, "number", "singular");
  satisfied |= uni.isSatisfied(sing1a, "number", "singular");
  uni.startUnify();
  satisfied &= uni.isSatisfied(sing2, "number", "singular");
  assertEquals(true, satisfied);
  uni.reset();
  
  //check if any of the equivalences is there
  sing1a = new AnalyzedToken("mały", "adj:pl:blahblah", "mały"); 
  satisfied = uni.isSatisfied(sing1, "number", "singular,plural");
  satisfied |= uni.isSatisfied(sing1a, "number", "singular,plural");
  uni.startUnify();
  satisfied &= uni.isSatisfied(sing2, "number", "singular,plural");
  assertEquals(true, satisfied);
  uni.reset();
  
//now test all possible feature equivalences by leaving type blank
  sing1a = new AnalyzedToken("mały", "adj:pl:blahblah", "mały"); 
  satisfied = uni.isSatisfied(sing1, "number", "");
  satisfied |= uni.isSatisfied(sing1a, "number", "");
  uni.startUnify();
  satisfied &= uni.isSatisfied(sing2, "number", "");
  assertEquals(true, satisfied);
  uni.reset();

//test non-agreeing tokens with blank types   
  satisfied = uni.isSatisfied(sing1a, "number", "");
  uni.startUnify();
  satisfied &= uni.isSatisfied(sing2, "number", "");
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
  femElement.setPosElement(".*[\\.:]f:.*", true, false);
  uni.setEquivalence("gender", "feminine", femElement);
  Element mascElement = new Element("", false, false, false);
  femElement.setPosElement(".*[\\.:]m:.*", true, false);
  uni.setEquivalence("gender", "masculine", mascElement);
  
  AnalyzedToken sing1 = new AnalyzedToken("mały", "adj:sg:blahblah:m", "mały");
  AnalyzedToken sing1a = new AnalyzedToken("mały", "adj:sg:blahblah:f", "mały");
  AnalyzedToken sing1b = new AnalyzedToken("mały", "adj:pl:blahblah:m", "mały");
  AnalyzedToken sing2 = new AnalyzedToken("człowiek", "subst:sg:blahblah:m", "człowiek");
  
  boolean satisfied = uni.isSatisfied(sing1, "number,gender", "");
  satisfied |= uni.isSatisfied(sing1a, "number,gender", "");
  satisfied |= uni.isSatisfied(sing1b, "number,gender", "");
  uni.startUnify();
  satisfied &= uni.isSatisfied(sing2, "number,gender", "");
  assertEquals(true, satisfied);
  uni.reset();    
  }
  
}
