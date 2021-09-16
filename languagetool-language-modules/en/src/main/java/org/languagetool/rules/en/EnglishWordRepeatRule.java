/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.en;

import java.util.ResourceBundle;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.Example;
import org.languagetool.rules.WordRepeatRule;

/**
 * Word repeat rule for English, to avoid false alarms in the generic word repetition rule.
 */
public class EnglishWordRepeatRule extends WordRepeatRule {

  public EnglishWordRepeatRule(ResourceBundle messages, Language language) {
    super(messages, language);
    addExamplePair(Example.wrong("This <marker>is is</marker> just an example sentence."),
                   Example.fixed("This <marker>is</marker> just an example sentence."));
  }

  @Override
  public String getId() {
    return "ENGLISH_WORD_REPEAT_RULE";
  }

  @Override
  public boolean ignore(AnalyzedTokenReadings[] tokens, int position) {
    if (position == 0) {
      return false;
    }

    // TODO:
    // What that is is a ...
    // but you you're my best friend ...
    // I'm so so happy
    // I'm very very happy
    String word = tokens[position].getToken();

    if ((repetitionOf("did", tokens, position) || repetitionOf("do", tokens, position)
        || repetitionOf("does", tokens, position)) && (position + 1 < tokens.length)
        && tokens[position + 1].getToken().equalsIgnoreCase("n't")) {
      return true;
    } else if (repetitionOf("had", tokens, position) && posIsIn(tokens, position - 2, "PRP", "NN")) {
      return true;   // "If I had had time, I would have gone to see him."
    } else if (repetitionOf("that", tokens, position) && posIsIn(tokens, position+1, "MD", "NN", "PRP$", "JJ", "VBZ", "VBD")) {
      return true;   // "I don't think that that is a problem."
    } else if (repetitionOf("can", tokens, position) && posIsIn(tokens, position-1, "NN")) {
      return true; // "The can can hold the water."
    } else if (repetitionOf("hip", tokens, position) && (position + 1 < tokens.length) && tokens[position + 1].getToken().equalsIgnoreCase("hooray")) {
      return true;
    } else if (repetitionOf("bam", tokens, position) && (position + 1 < tokens.length) && tokens[position + 1].getToken().equalsIgnoreCase("bigelow")) {
      return true;
    } else if (repetitionOf("wild", tokens, position) && (position + 1 < tokens.length) && tokens[position + 1].getToken().equalsIgnoreCase("west")) {
      return true; // In the wild wild west (https://en.wikipedia.org/wiki/Wild_Wild_West)
    } else if (repetitionOf("far", tokens, position) && (position + 1 < tokens.length) && tokens[position + 1].getToken().equalsIgnoreCase("away")) {
      return true;
    } else if (repetitionOf("so", tokens, position) && (position + 1 < tokens.length) && tokens[position + 1].getToken().equalsIgnoreCase("much")) {
      return true;
    } else if (repetitionOf("so", tokens, position) && (position + 1 < tokens.length) && tokens[position + 1].getToken().equalsIgnoreCase("many")) {
      return true;
    } else if (repetitionOf("s", tokens, position) && position > 1 && tokens[position - 2].getToken().matches("['’`´‘]")) {
      return true; // It's S.T.E.A.M.
    } else if (repetitionOf("a", tokens, position) && position > 1 && tokens[position - 2].getToken().equals(".")) {
      return true; // "a.k.a a"
    } else if (repetitionOf("on", tokens, position) && position > 1 && tokens[position - 2].getToken().equals(".")) {
      return true; // "You can contact E.ON on Instagram"
    } else if (tokens[position - 1].getToken().equalsIgnoreCase(word) && (((position + 1 < tokens.length) && tokens[position + 1].getToken().equalsIgnoreCase(word)) || (position > 1 && tokens[position - 2].getToken().equalsIgnoreCase(word)))) {
      // three time word repetition
      return true;
    } else if (tokens[position].getToken().matches("(?i)^[a-z]$") && position > 1 && tokens[position - 2].getToken().matches("(?i)^[a-z]$") && (position + 1 < tokens.length) && tokens[position + 1].getToken().matches("(?i)^[a-z]$")) {
      // spelling with spaces in between: "b a s i c a l l y"
      return true;
    } else if (repetitionOf("blah", tokens, position)) {
      return true;   // "blah blah"
    } else if (repetitionOf("uh", tokens, position)) {
      return true;   // "uh uh"
    } else if (repetitionOf("paw", tokens, position)) {
      return true;   // "paw paw"
    } else if (repetitionOf("yum", tokens, position)) {
      return true;   // "yum yum"
    } else if (repetitionOf("wop", tokens, position)) {
      return true;   // "wop wop"
    } else if (repetitionOf("woop", tokens, position)) {
      return true;   // "woop woop"
    } else if (repetitionOf("fnarr", tokens, position)) {
      return true;   // "fnarr fnarr" https://www.lexico.com/definition/fnarr_fnarr
    } else if (repetitionOf("fnar", tokens, position)) {
      return true;   // "fnar fnar"
    } else if (repetitionOf("ha", tokens, position)) {
      return true;   // "ha ha"
    } else if (repetitionOf("omg", tokens, position)) {
      return true;   // "omg omg"
    } else if (repetitionOf("boo", tokens, position)) {
      return true;   // "boo boo"
    } else if (repetitionOf("tick", tokens, position)) {
      return true;   // "tick tick"
    } else if (repetitionOf("twinkle", tokens, position)) {
      return true;   // "twinkle twinkle little star"
    } else if (repetitionOf("ta", tokens, position)) {
      return true;
    } else if (repetitionOf("la", tokens, position)) {
      return true;
    } else if (repetitionOf("x", tokens, position)) {
      return true;
    } else if (repetitionOf("hi", tokens, position)) {
      return true;   // "hi hi"
    } else if (repetitionOf("ho", tokens, position)) {
      return true;   // "ho ho"
    } else if (repetitionOf("heh", tokens, position)) {
      return true;
    } else if (repetitionOf("jay", tokens, position)) {
      return true; // Jay Jay (name)
    } else if (repetitionOf("walla", tokens, position)) {
      return true; // Walla Walla is a city in Washington State
    } else if (repetitionOf("sri", tokens, position)) {
      return true; // Sri Sri (name)
    } else if (repetitionOf("hey", tokens, position)) {
      return true;
    } else if (repetitionOf("hah", tokens, position)) {
      return true;
    } else if (repetitionOf("heh", tokens, position)) {
      return true;
    } else if (repetitionOf("oh", tokens, position)) {
      return true;
    } else if (repetitionOf("ouh", tokens, position)) {
      return true;
    } else if (repetitionOf("chop", tokens, position)) {
      return true;
    } else if (repetitionOf("ring", tokens, position)) {
      return true;
    } else if (repetitionOf("beep", tokens, position)) {
      return true;
    } else if (repetitionOf("bleep", tokens, position)) {
      return true;
    } else if (repetitionOf("yeah", tokens, position)) {
      return true;
    } else if (repetitionOf("wait", tokens, position) && position == 2) {
      return true;
    } else if (repetitionOf("quack", tokens, position)) {
      return true;
    } else if (repetitionOf("meow", tokens, position)) {
      return true;
    } else if (repetitionOf("squawk", tokens, position)) {
      return true;
    } else if (repetitionOf("whoa", tokens, position)) {
      return true;
    } else if (repetitionOf("si", tokens, position)) {
      return true;
    } else if (repetitionOf("honk", tokens, position)) {
      return true;
    } else if (repetitionOf("brum", tokens, position)) {
      return true;
    } else if (repetitionOf("chi", tokens, position)) {
      // name
      return true;
    } else if (repetitionOf("santorio", tokens, position)) {
      // name
      return true;
    } else if (repetitionOf("lapu", tokens, position)) {
      // city
      return true;
    } else if (repetitionOf("chow", tokens, position)) {
      // dog breed https://en.wikipedia.org/wiki/Chow_Chow
      return true;
    } else if (repetitionOf("beep", tokens, position)) {
      return true;
    } else if (repetitionOf("shh", tokens, position)) {
      return true;
    } else if (repetitionOf("yummy", tokens, position)) {
      return true;
    } else if (repetitionOf("boom", tokens, position)) {
      return true;
    } else if (repetitionOf("bye", tokens, position)) {
      return true;
    } else if (repetitionOf("ah", tokens, position)) {
      return true;
    } else if (repetitionOf("aah", tokens, position)) {
      return true;
    } else if (repetitionOf("bang", tokens, position)) {
      return true;
    } else if (repetitionOf("woof", tokens, position)) {
      return true;
    } else if (repetitionOf("wink", tokens, position)) {
      return true;
    } else if (repetitionOf("yes", tokens, position)) {
      return true;
    } else if (repetitionOf("tsk", tokens, position)) {
      return true;
    } else if (repetitionOf("hush", tokens, position)) {
      return true;
    } else if (repetitionOf("ding", tokens, position)) {
      return true;
    } else if (repetitionOf("choo", tokens, position)) {
      return true;
    } else if (repetitionOf("miu", tokens, position)) {
      return true;
    } else if (repetitionOf("tuk", tokens, position)) {
      return true;
    } else if (repetitionOf("yadda", tokens, position)) {
      return true;   // "yadda yadda"
    } else if (repetitionOf("sapiens", tokens, position)) {
      return true;   // "Homo sapiens sapiens"
    } else if (repetitionOf("tse", tokens, position)) {
      return true;   // "tse tse"
    } else if (repetitionOf("no", tokens, position)) {
      return true;   // "no no"
    } else if (tokens[position].getToken().endsWith("ay")) {
      if (tokens[position - 1].getToken().equals("may") && tokens[position].getToken().equals("May")) {
        return true;   // "may May"
      }
      if (tokens[position - 1].getToken().equals("May") && tokens[position].getToken().equals("may")) {
        return true;   // "May may"
      }
      if (tokens[1].getToken().equals("May") && tokens[2].getToken().equals("May")) {
        return true;   // "May May" SENT_START
      }
    } else if (tokens[position].getToken().endsWith("ill")) {
      return (position > 0 && tokens[position - 1].getToken().equals("will") && tokens[position].getToken().equals("Will")) // will Wills
        || (tokens[position - 1].getToken().equals("Will") && tokens[position].getToken().equals("will")) // Will will ...
        || (tokens[1].getToken().equals("Will") && tokens[2].getToken().equals("Will")); // "Will Will" SENT_START
    }
    return super.ignore(tokens, position);
  }

  private boolean posIsIn(AnalyzedTokenReadings[] tokens, int position, String... posTags) {
    if (position >= 0 && position < tokens.length) {
      for (String posTag : posTags) {
        if (tokens[position].hasPartialPosTag(posTag)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean repetitionOf(String word, AnalyzedTokenReadings[] tokens, int position) {
    return position > 0 && tokens[position - 1].getToken().equalsIgnoreCase(word) && tokens[position].getToken().equalsIgnoreCase(word);
  }

}
