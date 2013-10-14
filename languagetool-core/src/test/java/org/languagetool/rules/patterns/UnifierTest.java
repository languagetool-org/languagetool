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

package org.languagetool.rules.patterns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.languagetool.AnalyzedToken;

public class UnifierTest extends TestCase {

  // trivial unification = test if the character case is the same
  public void testUnificationCase() {
    final UnifierConfiguration unifierConfig = new UnifierConfiguration();
    final Element elLower = new Element("\\p{Ll}+", true, true, false);
    final Element elUpper = new Element("\\p{Lu}\\p{Ll}+", true, true, false);
    final Element elAllUpper = new Element("\\p{Lu}+$", true, true, false);
    unifierConfig.setEquivalence("case-sensitivity", "lowercase", elLower);
    unifierConfig.setEquivalence("case-sensitivity", "uppercase", elUpper);
    unifierConfig.setEquivalence("case-sensitivity", "alluppercase", elAllUpper);
    final AnalyzedToken lower1 = new AnalyzedToken("lower", "JJR", "lower");
    final AnalyzedToken lower2 = new AnalyzedToken("lowercase", "JJ", "lowercase");
    final AnalyzedToken upper1 = new AnalyzedToken("Uppercase", "JJ", "Uppercase");
    final AnalyzedToken upper2 = new AnalyzedToken("John", "NNP", "John");
    final AnalyzedToken upperAll1 = new AnalyzedToken("JOHN", "NNP", "John");
    final AnalyzedToken upperAll2 = new AnalyzedToken("JAMES", "NNP", "James");

    final Unifier uni = unifierConfig.createUnifier();

    final Map<String, List<String>> equiv = new HashMap<>();
    final List<String> list1 = new ArrayList<>();
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

    satisfied = uni.isSatisfied(upperAll2, equiv);
    uni.startUnify();
    satisfied &= uni.isSatisfied(upperAll1, equiv);
    assertEquals(true, satisfied);
  }

  // slightly non-trivial unification = test if the grammatical number is the same
  public void testUnificationNumber() {
    final UnifierConfiguration unifierConfig = new UnifierConfiguration();

    final Element sgElement = new Element("", false, false, false);
    sgElement.setPosElement(".*[\\.:]sg:.*", true, false);
    unifierConfig.setEquivalence("number", "singular", sgElement);
    final Element plElement = new Element("", false, false, false);
    plElement.setPosElement(".*[\\.:]pl:.*", true, false);
    unifierConfig.setEquivalence("number", "plural", plElement);

    final Unifier uni = unifierConfig.createUnifier();

    final AnalyzedToken sing1 = new AnalyzedToken("mały", "adj:sg:blahblah", "mały");
    final AnalyzedToken sing2 = new AnalyzedToken("człowiek", "subst:sg:blahblah", "człowiek");

    final Map<String, List<String>> equiv = new HashMap<>();
    final List<String> list1 = new ArrayList<>();
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

  //slightly non-trivial unification = test if the grammatical number & gender is the same
  public void testUnificationNumberGender() {
    final UnifierConfiguration unifierConfig = new UnifierConfiguration();

    final Element sgElement = new Element("", false, false, false);
    sgElement.setPosElement(".*[\\.:]sg:.*", true, false);
    unifierConfig.setEquivalence("number", "singular", sgElement);
    final Element plElement = new Element("", false, false, false);
    plElement.setPosElement(".*[\\.:]pl:.*", true, false);
    unifierConfig.setEquivalence("number", "plural", plElement);

    final Element femElement = new Element("", false, false, false);
    femElement.setPosElement(".*[\\.:]f", true, false);
    unifierConfig.setEquivalence("gender", "feminine", femElement);

    final Element mascElement = new Element("", false, false, false);
    mascElement.setPosElement(".*[\\.:]m", true, false);
    unifierConfig.setEquivalence("gender", "masculine", mascElement);

    final Unifier uni = unifierConfig.createUnifier();

    final AnalyzedToken sing1 = new AnalyzedToken("mały", "adj:sg:blahblah:m", "mały");
    final AnalyzedToken sing1a = new AnalyzedToken("mała", "adj:sg:blahblah:f", "mały");
    final AnalyzedToken sing1b = new AnalyzedToken("małe", "adj:pl:blahblah:m", "mały");
    final AnalyzedToken sing2 = new AnalyzedToken("człowiek", "subst:sg:blahblah:m", "człowiek");

    final Map<String, List<String>> equiv = new HashMap<>();
    equiv.put("number", null);
    equiv.put("gender", null);

    boolean satisfied = uni.isSatisfied(sing1, equiv);
    satisfied |= uni.isSatisfied(sing1a, equiv);
    satisfied |= uni.isSatisfied(sing1b, equiv);
    uni.startUnify();
    satisfied &= uni.isSatisfied(sing2, equiv);
    uni.startNextToken();
    assertEquals(true, satisfied);
    assertEquals("[mały[mały/adj:sg:blahblah:m*], człowiek[człowiek/subst:sg:blahblah:m*]]", Arrays.toString(uni.getUnifiedTokens()));
    uni.reset();
  }

  // checks if all tokens share the same set of features to be unified
  public void testMultipleFeats() {
    final UnifierConfiguration unifierConfig = new UnifierConfiguration();
    final Element sgElement = new Element("", false, false, false);
    sgElement.setPosElement(".*[\\.:]sg:.*", true, false);
    unifierConfig.setEquivalence("number", "singular", sgElement);
    final Element plElement = new Element("", false, false, false);
    plElement.setPosElement(".*[\\.:]pl:.*", true, false);
    unifierConfig.setEquivalence("number", "plural", plElement);
    final Element femElement = new Element("", false, false, false);
    femElement.setPosElement(".*[\\.:]f([\\.:].*)?", true, false);
    unifierConfig.setEquivalence("gender", "feminine", femElement);
    final Element mascElement = new Element("", false, false, false);
    mascElement.setPosElement(".*[\\.:]m([\\.:].*)?", true, false);
    unifierConfig.setEquivalence("gender", "masculine", mascElement);
    final Element neutElement = new Element("", false, false, false);
    neutElement.setPosElement(".*[\\.:]n([\\.:].*)?", true, false);
    unifierConfig.setEquivalence("gender", "neutral", neutElement);

    final Unifier uni = unifierConfig.createUnifier();

    final AnalyzedToken sing1 = new AnalyzedToken("mały", "adj:sg:blahblah:m", "mały");
    AnalyzedToken sing1a = new AnalyzedToken("mały", "adj:pl:blahblah:f", "mały");
    AnalyzedToken sing1b = new AnalyzedToken("mały", "adj:pl:blahblah:f", "mały");
    AnalyzedToken sing2 = new AnalyzedToken("zgarbiony", "adj:pl:blahblah:f", "zgarbiony");
    final AnalyzedToken sing3 = new AnalyzedToken("człowiek", "subst:sg:blahblah:m", "człowiek");

    final Map<String, List<String>> equiv = new HashMap<>();
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
    uni.isUnified(sing1, equiv, false);
    uni.isUnified(sing1a, equiv, false);
    uni.isUnified(sing1b, equiv, true);
    uni.isUnified(sing2, equiv, true);
    assertEquals(false, uni.isUnified(sing3, equiv, true));
    uni.reset();

    sing1a = new AnalyzedToken("osobiste", "adj:pl:nom.acc.voc:f.n.m2.m3:pos:aff", "osobisty");
    sing1b = new AnalyzedToken("osobiste", "adj:sg:nom.acc.voc:n:pos:aff", "osobisty");
    sing2 = new AnalyzedToken("godło", "subst:sg:nom.acc.voc:n", "godło");

    uni.isUnified(sing1a, equiv, false);
    uni.isUnified(sing1b, equiv, true);
    assertEquals(true, uni.isUnified(sing2, equiv, true));
    assertEquals("[osobiste[osobisty/adj:sg:nom.acc.voc:n:pos:aff*], godło[godło/subst:sg:nom.acc.voc:n*]]", Arrays.toString(uni.getFinalUnified()));
    uni.reset();

    //now test a case when the last reading doesn't match at all

    sing1a = new AnalyzedToken("osobiste", "adj:pl:nom.acc.voc:f.n.m2.m3:pos:aff", "osobisty");
    sing1b = new AnalyzedToken("osobiste", "adj:sg:nom.acc.voc:n:pos:aff", "osobisty");
    final AnalyzedToken sing2a = new AnalyzedToken("godło", "subst:sg:nom.acc.voc:n", "godło");
    final AnalyzedToken sing2b = new AnalyzedToken("godło", "indecl", "godło");

    uni.isUnified(sing1a, equiv, false);
    uni.isUnified(sing1b, equiv, true);
    uni.isUnified(sing2a, equiv, false);
    assertEquals(true, uni.isUnified(sing2b, equiv, true));
    assertEquals("[osobiste[osobisty/adj:sg:nom.acc.voc:n:pos:aff*], godło[godło/subst:sg:nom.acc.voc:n*]]", Arrays.toString(uni.getFinalUnified()));
    uni.reset();

    //check if two features are left out correctly (both match)
    AnalyzedToken plur1 = new AnalyzedToken("zgarbieni", "adj:pl:foobar:m", "zgarbiony");
    AnalyzedToken plur2 = new AnalyzedToken("zgarbieni", "adj:pl:blabla:m", "zgarbiony");

    AnalyzedToken plur3 = new AnalyzedToken("ludzie", "subst:pl:blabla:m", "człowiek");
    AnalyzedToken plur4 = new AnalyzedToken("ludzie", "subst:pl:pampam:m", "człowiek");

    uni.isUnified(plur1, equiv, false);
    uni.isUnified(plur2, equiv, true);
    uni.isUnified(plur3, equiv, false);
    assertTrue(uni.isUnified(plur4, equiv, true));
    assertEquals("[zgarbieni[zgarbiony/adj:pl:foobar:m*,zgarbiony/adj:pl:blabla:m*], " +
        "ludzie[człowiek/subst:pl:blabla:m*,człowiek/subst:pl:pampam:m*]]", Arrays.toString(uni.getFinalUnified()));

    //check with a sequence of many tokens

    uni.reset();

    AnalyzedToken case1a = new AnalyzedToken("xx", "abc:sg:f", "xx");
    AnalyzedToken case1b = new AnalyzedToken("xx", "cde:pl:f", "xx");

    AnalyzedToken case2a = new AnalyzedToken("yy", "abc:pl:f", "yy");
    AnalyzedToken case2b = new AnalyzedToken("yy", "cde:as:f", "yy");
    AnalyzedToken case2c = new AnalyzedToken("yy", "cde:pl:c", "yy");
    AnalyzedToken case2d = new AnalyzedToken("yy", "abc:sg:f", "yy");
    AnalyzedToken case2e = new AnalyzedToken("yy", "efg:aa:e", "yy");

    uni.isUnified(case1a, equiv, false);
    uni.isUnified(case1b, equiv, true);

    uni.isUnified(case2a, equiv, false);
    uni.isUnified(case2b, equiv, false);
    uni.isUnified(case2c, equiv, false);
    uni.isUnified(case2d, equiv, false);
    assertTrue(uni.isUnified(case2e, equiv, true));
    assertEquals(Arrays.toString(uni.getFinalUnified()),
        "[xx[xx/abc:sg:f*,xx/cde:pl:f*], yy[yy/abc:pl:f*,yy/abc:sg:f*]]");
  }

  public void testNegation() {
    final UnifierConfiguration unifierConfig = new UnifierConfiguration();
    final Element sgElement = new Element("", false, false, false);
    sgElement.setPosElement(".*[\\.:]sg:.*", true, false);
    unifierConfig.setEquivalence("number", "singular", sgElement);
    final Element plElement = new Element("", false, false, false);
    plElement.setPosElement(".*[\\.:]pl:.*", true, false);
    unifierConfig.setEquivalence("number", "plural", plElement);
    final Element femElement = new Element("", false, false, false);
    femElement.setPosElement(".*:f", true, false);
    unifierConfig.setEquivalence("gender", "feminine", femElement);
    final Element mascElement = new Element("", false, false, false);
    mascElement.setPosElement(".*:m", true, false);
    unifierConfig.setEquivalence("gender", "masculine", mascElement);

    final Unifier uni = unifierConfig.createUnifier();

    //Latin adjectives
    final AnalyzedToken sing_masc = new AnalyzedToken("parvus", "adj:sg:blahblah:m", "parvus");
    final AnalyzedToken plur_masc = new AnalyzedToken("parvi", "adj:sg:blahblah:m", "parvus");
    final AnalyzedToken plur_fem = new AnalyzedToken("parvae", "adj:pl:blahblah:f", "parvus");
    final AnalyzedToken sing_fem = new AnalyzedToken("parva", "adj:sg:blahblah:f", "parvus");

    //Let's pretend Latin has determiners
    final AnalyzedToken det_sing_fem = new AnalyzedToken("una", "det:sg:blahblah:f", "unus");
    final AnalyzedToken det_plur_fem = new AnalyzedToken("unae", "det:pl:blahblah:f", "unus");
    final AnalyzedToken det_sing_masc = new AnalyzedToken("unus", "det:sg:blahblah:m", "unus");
    final AnalyzedToken det_plur_masc = new AnalyzedToken("uni", "det:sg:blahblah:m", "unus");

    //and nouns
    final AnalyzedToken subst_sing_fem = new AnalyzedToken("discrepatio", "subst:sg:blahblah:f", "discrepatio");
    final AnalyzedToken subst_plur_fem = new AnalyzedToken("discrepationes", "subst:sg:blahblah:f", "discrepatio");
    final AnalyzedToken subst_sing_masc = new AnalyzedToken("homo", "sg:sg:blahblah:m", "homo");
    final AnalyzedToken subst_plur_masc = new AnalyzedToken("homines", "sg:sg:blahblah:m", "homo");

    //now we should have 4x4x4 combinations...

    final Map<String, List<String>> equiv = new HashMap<>();
    equiv.put("number", null);
    equiv.put("gender", null);

    boolean satisfied = uni.isSatisfied(det_sing_masc, equiv);
    uni.startUnify();
    satisfied &= uni.isSatisfied(sing_masc, equiv);
    uni.startNextToken();
    satisfied &= uni.isSatisfied(subst_sing_masc, equiv);
    uni.startNextToken();
    assertEquals(true, satisfied);
    uni.reset();

    //now test the simplified interface
    uni.isUnified(det_sing_masc, equiv, true);
    uni.isUnified(sing_masc, equiv, true);
    assertEquals(true, uni.isUnified(subst_sing_masc, equiv, true));
    uni.reset();

    //now let's negate this

    //traditional way
    satisfied = uni.isSatisfied(det_sing_masc, equiv);
    uni.startUnify();
    satisfied &= uni.isSatisfied(sing_masc, equiv);
    uni.startNextToken();
    satisfied &= uni.isSatisfied(subst_sing_masc, equiv);
    uni.startNextToken();
    assertEquals(false, !satisfied);
    uni.reset();

    //now test the simplified interface
    uni.isUnified(det_sing_masc, equiv, true);
    uni.isUnified(sing_masc, equiv, true);
    assertEquals(false, !uni.isUnified(subst_sing_masc, equiv, true));
    uni.reset();

    //OK, so let's test it with something that is not correct
    uni.isUnified(det_sing_fem, equiv, true);
    uni.isUnified(sing_masc, equiv, true);
    assertEquals(true, !uni.isUnified(subst_sing_masc, equiv, true));
    uni.reset();

    //OK, so let's test it with something that is not correct
    uni.isUnified(det_sing_masc, equiv, true);
    uni.isUnified(sing_fem, equiv, true);
    assertEquals(true, !uni.isUnified(subst_sing_masc, equiv, true));
    uni.reset();

    //OK, second token does not match
    uni.isUnified(det_sing_masc, equiv, true);
    uni.isUnified(sing_masc, equiv, true);
    assertEquals(true, !uni.isUnified(subst_sing_fem, equiv, true));
    uni.reset();

    //OK, second token does not match
    uni.isUnified(det_sing_masc, equiv, true);
    uni.isUnified(plur_masc, equiv, true);
    assertEquals(true, !uni.isUnified(subst_sing_fem, equiv, true));
    uni.reset();

    //OK, second token does not match
    uni.isUnified(det_sing_masc, equiv, true);
    uni.isUnified(plur_fem, equiv, true);
    assertEquals(true, !uni.isUnified(subst_sing_fem, equiv, true));
    uni.reset();

    //and another one
    uni.isUnified(det_plur_fem, equiv, true);
    uni.isUnified(plur_fem, equiv, true);
    assertEquals(true, !uni.isUnified(subst_sing_fem, equiv, true));
    uni.reset();

    //and another one
    uni.isUnified(det_sing_fem, equiv, true);
    uni.isUnified(plur_fem, equiv, true);
    assertEquals(true, !uni.isUnified(subst_plur_fem, equiv, true));
    uni.reset();

    //and another one
    uni.isUnified(det_sing_fem, equiv, true);
    uni.isUnified(plur_fem, equiv, true);
    assertEquals(true, !uni.isUnified(subst_plur_masc, equiv, true));
    uni.reset();

    //and another one
    uni.isUnified(det_plur_masc, equiv, true);
    uni.isUnified(plur_fem, equiv, true);
    assertEquals(true, !uni.isUnified(subst_plur_masc, equiv, true));
    uni.reset();
  }

}
