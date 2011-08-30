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
package de.danielnaber.languagetool;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import de.danielnaber.languagetool.JLanguageTool.ParagraphHandling;
import de.danielnaber.languagetool.rules.RuleMatch;

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
    assertEquals(9, tool.getAllRules().size());
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
    // German rule has no effect with English error:
    assertEquals(0, tool.check("I can give you more a detailed description").size());
    //test unknown words listing
    assertEquals("[I, can, detailed, give, more, you]", tool.getUnknownWords().toString());    
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
    // Dutch rule has no effect with English error:
    assertEquals(0, tool.check("I can give you more a detailed description.").size());
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
    // Polish rule has no effect with English error:
    matches = tool.check("I can give you more a detailed description");
    assertEquals(0, matches.size());
    tool.setListUnknownWords(true);
    matches = tool.check("This is not a Polish text.");
    assertEquals(0, matches.size());
    assertEquals("[Polish, This, is]", tool.getUnknownWords().toString());
    //check positions relative to sentence ends    
    matches = tool.check("To jest tekst.\nTest 1. To jest linia w której nie ma przecinka.");
    assertEquals(16, matches.get(0).getColumn());
    //with a space...
    matches = tool.check("To jest tekst. \nTest 1. To jest linia w której nie ma przecinka.");
    assertEquals(16, matches.get(0).getColumn());
    matches = tool.check("To jest tekst. Test 1. To jest linia w której nie ma przecinka.");
    assertEquals(30, matches.get(0).getColumn());
    //recheck with the -b mode...
    final Language lang = Language.POLISH;
    lang.getSentenceTokenizer().setSingleLineBreaksMarksParagraph(true);
    tool = new JLanguageTool(lang);
    tool.activateDefaultPatternRules();
    matches = tool.check("To jest tekst.\nTest 1. To jest linia w której nie ma przecinka.");
    assertEquals(16, matches.get(0).getColumn());
    //with a space...
    matches = tool.check("To jest tekst. \nTest 1. To jest linia w której nie ma przecinka.");
    assertEquals(16, matches.get(0).getColumn());
    matches = tool.check("To jest tekst. To jest linia w której nie ma przecinka.");
    assertEquals(23, matches.get(0).getColumn());
  }
  
  public void testSlovenian() throws IOException {
    final JLanguageTool tool = new JLanguageTool(Language.SLOVENIAN);
    assertEquals(0, tool.check("Kupil je npr. jajca, moko in mleko.").size());
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
  
}
