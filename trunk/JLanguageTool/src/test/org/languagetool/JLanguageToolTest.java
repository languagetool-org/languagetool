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
package org.languagetool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.languagetool.JLanguageTool.ParagraphHandling;
import org.languagetool.rules.Category;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.Element;
import org.languagetool.rules.patterns.PatternRule;

/**
 * @author Daniel Naber
 */
public class JLanguageToolTest extends TestCase {

  // used on http://www.languagetool.org/usage/
  /*
  public void testDemo() throws IOException {
    JLanguageTool langTool = new JLanguageTool(Language.ENGLISH);
    langTool.activateDefaultPatternRules();
    List<RuleMatch> matches = langTool.check("A sentence " + 
        "with a error in the Hitchhiker's Guide tot he Galaxy");
    for (RuleMatch match : matches) {
      System.out.println("Potential error at line " +
          match.getEndLine() + ", column " +
          match.getColumn() + ": " + match.getMessage());
      System.out.println("Suggested correction: " +
          match.getSuggestedReplacements());
    }
  }
  */
  
  public void testEnglish() throws IOException {
    final JLanguageTool tool = new JLanguageTool(Language.ENGLISH);
    assertEquals(0, tool.check("A test that should not give errors.").size());
    assertEquals(1, tool.check("A test test that should give errors.").size());
    assertEquals(0, tool.check("I can give you more a detailed description.").size());
    assertEquals(10, tool.getAllRules().size());
    tool.activateDefaultPatternRules();
    assertTrue(tool.getAllRules().size() > 3);
    assertEquals(1, tool.check("I can give you more a detailed description.").size());
    tool.disableRule("MORE_A_JJ");
    assertEquals(0, tool.check("I can give you more a detailed description.").size());
    assertEquals(1, tool.check("I've go to go.").size());
    tool.disableCategory("Possible Typos");
    assertEquals(0, tool.check("I've go to go.").size());
  }
  
  public void testGerman() throws IOException {
    final JLanguageTool tool = new JLanguageTool(Language.GERMAN);
    assertEquals(0, tool.check("Ein Test, der keine Fehler geben sollte.").size());
    assertEquals(1, tool.check("Ein Test Test, der Fehler geben sollte.").size());
    tool.activateDefaultPatternRules();
    tool.setListUnknownWords(true);
    // no spelling mistakes as we have not created a variant:
    assertEquals(0, tool.check("I can give you more a detailed description").size());
    //test unknown words listing
    assertEquals("[I, can, detailed, give, more, you]", tool.getUnknownWords().toString());    
  }

  public void testGermanyGerman() throws IOException {
    final JLanguageTool tool = new JLanguageTool(Language.GERMANY_GERMAN);
    assertEquals(0, tool.check("Ein Test, der keine Fehler geben sollte.").size());
    assertEquals(1, tool.check("Ein Test Test, der Fehler geben sollte.").size());
    tool.activateDefaultPatternRules();
    tool.setListUnknownWords(true);
    // German rule has no effect with English error, but they are spelling mistakes:
    assertEquals(6, tool.check("I can give you more a detailed description").size());
    //test unknown words listing
    assertEquals("[I, can, detailed, give, more, you]", tool.getUnknownWords().toString());
  }

  public void testPositionsWithGerman() throws IOException {
    final JLanguageTool tool = new JLanguageTool(Language.GERMAN);
    tool.activateDefaultPatternRules();
    final List<RuleMatch> matches = tool.check("Stundenkilometer");
    assertEquals(1, matches.size());
    final RuleMatch match = matches.get(0);
    // TODO: values should be either 0-based or 1-based, it should not be mixed up!
    assertEquals(0, match.getLine());
    assertEquals(1, match.getColumn());
  }

  public void testPositionsWithEnglish() throws IOException {
    final JLanguageTool tool = new JLanguageTool(Language.AMERICAN_ENGLISH);
    final List<RuleMatch> matches = tool.check("A sentence with no period\n" +
            "A sentence. A typoh.");
    assertEquals(1, matches.size());
    final RuleMatch match = matches.get(0);
    assertEquals(1, match.getLine());
    assertEquals(15, match.getColumn());
  }

  public void testPositionsWithEnglishTwoLineBreaks() throws IOException {
    final JLanguageTool tool = new JLanguageTool(Language.AMERICAN_ENGLISH);
    final List<RuleMatch> matches = tool.check("This sentence.\n\n" +
            "A sentence. A typoh.");
    assertEquals(1, matches.size());
    final RuleMatch match = matches.get(0);
    assertEquals(2, match.getLine());
    assertEquals(14, match.getColumn());   // TODO: should actually be 15, as in testPositionsWithEnglish()
  }

  public void testDutch() throws IOException {
    final JLanguageTool tool = new JLanguageTool(Language.DUTCH);
    tool.activateDefaultPatternRules();
    assertEquals(0, tool.check("Een test, die geen fouten mag geven.").size());
    assertEquals(2, tool.check("Een test test, die een fout moet geven.").size());
    assertEquals(1, tool.check("Dit is fout.!").size());
    //test uppercasing rule:
    /*  
    matches = tool.check("De Afdeling Beheer kan het");
    assertEquals(1, matches.size());   
    assertEquals("Als Afdeling geen deel uitmaakt van de naam, dan is juist:<suggestion>afdeling</suggestion>", matches.get(0).getMessage());
     */
    // Dutch rule has no effect with English error but they are spelling mistakes:
    assertEquals(5, tool.check("I can give you more a detailed description.").size());
  }
  
  public void testPolish() throws IOException {
    JLanguageTool tool = new JLanguageTool(Language.POLISH);
    assertEquals("[PL]", Arrays.toString(Language.POLISH.getCountryVariants()));
    List<RuleMatch> matches = tool.check("To jest całkowicie prawidłowe zdanie.");
    assertEquals(0, matches.size());
    matches = tool.check("To jest jest problem.");
    assertEquals(1, matches.size());
    //this rule is by default off
    matches = tool.check("Był on bowiem pięknym strzelcem bowiem.");
    assertEquals(0, matches.size());
    tool.enableDefaultOffRule("PL_WORD_REPEAT");
    matches = tool.check("Był on bowiem pięknym strzelcem bowiem.");
    assertEquals(1, matches.size());
    tool.activateDefaultPatternRules();
    matches = tool.check("Premier drapie się w ucho co i rusz.");
    assertEquals(1, matches.size());
    // Polish rule has no effect with English error but will get spelling activated:
    matches = tool.check("I can give you more a detailed description");
    assertEquals(6, matches.size());
    tool.setListUnknownWords(true);
    matches = tool.check("This is not a Polish text.");
    assertEquals(3, matches.size());
    assertEquals("[Polish, This, is, text]", tool.getUnknownWords().toString());
    //check positions relative to sentence ends    
    matches = tool.check("To jest tekst.\nTest 1. To jest linia w której nie ma przecinka.");
    assertEquals(17, matches.get(0).getColumn());
    //with a space...
    matches = tool.check("To jest tekst. \nTest 1. To jest linia w której nie ma przecinka.");
    assertEquals(16, matches.get(0).getColumn());
    matches = tool.check("To jest tekst. Test 1. To jest linia w której nie ma przecinka.");
    assertEquals(32, matches.get(0).getColumn());
    //recheck with the -b mode...
    final Language lang = Language.POLISH;
    lang.getSentenceTokenizer().setSingleLineBreaksMarksParagraph(true);
    tool = new JLanguageTool(lang);
    tool.activateDefaultPatternRules();
    matches = tool.check("To jest tekst.\nTest 1. To jest linia w której nie ma przecinka.");
    assertEquals(17, matches.get(0).getColumn());
    //with a space...
    matches = tool.check("To jest tekst. \nTest 1. To jest linia w której nie ma przecinka.");
    assertEquals(17, matches.get(0).getColumn());
    matches = tool.check("To jest tekst. To jest linia w której nie ma przecinka.");
    assertEquals(24, matches.get(0).getColumn());
    
    //and let's test other feats
    AnalyzedSentence sent = tool.getAnalyzedSentence("Z powodu pogody dobre buty są wskazane.");
    assertEquals("Disambiguator log: "
            + "\n\nMULTIWORD_CHUNKER: Z[z/prep:gen.inst] -> Z[z/prep:gen.inst,Z powodu/<PREP:GEN>]"
            + "\n\nMULTIWORD_CHUNKER: powodu[powód/subst:sg:gen:m3] -> powodu[powód/subst:sg:gen:m3,Z powodu/</PREP:GEN>]\n",
            sent.getAnnotations());
    sent = tool.getAnalyzedSentence("Nie mamy żadnej ryby.");
    assertEquals("Disambiguator log: "
            + "\n\nNIE_ADAMP: Nie[nie/qub,on/ppron3:pl:acc:f.m2.m3.n.p2.p3:ter:praep,on/ppron3:sg:acc:n:ter:praep] -> Nie[nie/qub]"
            + "\n\nunify_adj_subst: żadnej[żaden/adj:sg:dat:f:pos,żaden/adj:sg:gen:f:pos,żaden/adj:sg:loc:f:pos] -> żadnej[żaden/adj:sg:gen:f:pos]" +
            "\n\nunify_adj_subst: ryby[ryba/subst:pl:acc:f,ryba/subst:pl:nom:f,ryba/subst:pl:voc:f,ryba/subst:sg:gen:f] -> ryby[ryba/subst:sg:gen:f]\n",
            sent.getAnnotations());
    
  }
  
  public void testSlovenian() throws IOException {
    final JLanguageTool tool = new JLanguageTool(Language.SLOVENIAN);
    assertEquals(1, tool.check("Kupil je npr. jajca, moko in mleko.").size());
  }
  

  public void testJapanese() throws IOException {
	final JLanguageTool tool = new JLanguageTool(Language.JAPANESE);
	tool.activateDefaultPatternRules();
	assertEquals(0, tool.check("エラーを含まないテスト文です。").size());
	assertEquals(1, tool.check("エラーお含むテスト文です。").size());
  }
  
  public void testCountLines() {
    assertEquals(0, JLanguageTool.countLineBreaks(""));
    assertEquals(1, JLanguageTool.countLineBreaks("Hallo,\nnächste Zeile"));
    assertEquals(2, JLanguageTool.countLineBreaks("\nZweite\nDritte"));
    assertEquals(4, JLanguageTool.countLineBreaks("\nZweite\nDritte\n\n"));
  }
  
  public void testAnalyzedSentence() throws IOException {
    final JLanguageTool tool = new JLanguageTool(Language.ENGLISH);
    //test soft-hyphen ignoring:
    assertEquals("<S> This[this/DT]  is[be/VBZ]  a[a/DT]  test­ed[tested/JJ,test/VBD,test/VBN,test­ed]  sentence[sentence/NN,sentence/VB,sentence/VBP].[./.,</S>]", tool.getAnalyzedSentence("This is a test\u00aded sentence.").toString());
    //test paragraph ends adding
    assertEquals("<S> </S><P/> ", tool.getAnalyzedSentence("\n").toString());
  }  
  
  public void testParagraphRules() throws IOException {
    final JLanguageTool tool = new JLanguageTool(Language.ENGLISH);
    
    //run normally
    List<RuleMatch> matches = tool.check("(This is an quote.\n It ends in the second sentence.");
    assertEquals(2, matches.size());
    assertEquals(2, tool.getSentenceCount());
    
    //run in a sentence-only mode
    matches = tool.check("(This is an quote.\n It ends in the second sentence.", false, ParagraphHandling.ONLYNONPARA);
    assertEquals(1, matches.size());
    assertEquals("EN_A_VS_AN", matches.get(0).getRule().getId());
    assertEquals(1, tool.getSentenceCount());
    
    //run in a paragraph mode - single sentence
    matches = tool.check("(This is an quote.\n It ends in the second sentence.", false, ParagraphHandling.ONLYPARA);
    assertEquals(1, matches.size());
    assertEquals("EN_UNPAIRED_BRACKETS", matches.get(0).getRule().getId());
    assertEquals(1, tool.getSentenceCount());
    
    //run in a paragraph mode - many sentences
    matches = tool.check("(This is an quote.\n It ends in the second sentence.", true, ParagraphHandling.ONLYPARA);
    assertEquals(1, matches.size());
    assertEquals("EN_UNPAIRED_BRACKETS", matches.get(0).getRule().getId());
    assertEquals(2, tool.getSentenceCount());
  }  
    
  public void testWhitespace() throws IOException {
    final JLanguageTool tool = new JLanguageTool(Language.ENGLISH);
    final AnalyzedSentence raw = tool.getRawAnalyzedSentence("Let's do a \"test\", do you understand?");
    final AnalyzedSentence cooked = tool.getAnalyzedSentence("Let's do a \"test\", do you understand?");
    //test if there was a change
    assertFalse(raw.equals(cooked));
    //see if nothing has been deleted
    assertEquals(raw.getTokens().length, cooked.getTokens().length);
    int i = 0;
    for (final AnalyzedTokenReadings atr : raw.getTokens()) {
      assertEquals(atr.isWhitespaceBefore(), 
          cooked.getTokens()[i].isWhitespaceBefore());
      i++;
    }
  }

  public void testOverlapFilter() throws IOException {
    final Category category = new Category("test category");
    final List<Element> elements1 = Arrays.asList(new Element("one", true, false, false));
    final PatternRule rule1 = new PatternRule("id1", Language.ENGLISH, elements1, "desc1", "msg1", "shortMsg1");
    rule1.setSubId("1");
    rule1.setCategory(category);

    final List<Element> elements2 = Arrays.asList(new Element("one", true, false, false), new Element("two", true, false, false));
    final PatternRule rule2 = new PatternRule("id1", Language.ENGLISH, elements2, "desc2", "msg2", "shortMsg2");
    rule2.setSubId("2");
    rule2.setCategory(category);

    final JLanguageTool tool = new JLanguageTool(Language.ENGLISH);
    tool.addRule(rule1);
    tool.addRule(rule2);

    final List<RuleMatch> ruleMatches1 = tool.check("And one two three.");
    assertEquals("one overlapping rule must be filtered out", 1, ruleMatches1.size());
    assertEquals("msg1", ruleMatches1.get(0).getMessage());

    final String sentence = "And one two three.";
    final AnalyzedSentence analyzedSentence = tool.getAnalyzedSentence(sentence);
    final List<Rule> bothRules = new ArrayList<Rule>(Arrays.asList(rule1, rule2));
    final List<RuleMatch> ruleMatches2 = tool.checkAnalyzedSentence(ParagraphHandling.NORMAL, bothRules, 0, 0, 0, sentence, analyzedSentence);
    assertEquals("one overlapping rule must be filtered out", 1, ruleMatches2.size());
    assertEquals("msg1", ruleMatches2.get(0).getMessage());
  }
}
