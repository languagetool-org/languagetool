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
    String word = tokens[position].getToken().toString();

    if (wordRepetitionOf("had", tokens, position) && posIsIn(tokens, position - 2, "PRP", "NN")) {
      return true;   // "If I had had time, I would have gone to see him."
    } else if (wordRepetitionOf("that", tokens, position) && posIsIn(tokens, position+1, "NN", "PRP$", "JJ", "VBZ", "VBD")) {
      return true;   // "I don't think that that is a problem."
    } else if (wordRepetitionOf("can", tokens, position) && posIsIn(tokens, position-1, "NN")) {
      return true; // "The can can hold the water."
    } else if (wordRepetitionOf("hip", tokens, position) && (position + 1 < tokens.length) && tokens[position + 1].getToken().equalsIgnoreCase("hooray")) {
      return true;
    } else if (wordRepetitionOf("s", tokens, position) && position > 1 && tokens[position - 2].getToken().matches("['’`´‘]")) {
      return true; // It's S.T.E.A.M.
    } else if (wordRepetitionOf("a", tokens, position) && position > 1 && tokens[position - 2].getToken().matches(".")) {
      return true; // "a.k.a a"
    } else if (tokens[position - 1].getToken().equalsIgnoreCase(word) && (((position + 1 < tokens.length) && tokens[position + 1].getToken().equalsIgnoreCase(word)) || (position > 1 && tokens[position - 2].getToken().equalsIgnoreCase(word)))) {
      // three time word repetition
      return true;
    } else if (tokens[position].getToken().matches("(?i)^[a-z]$") && position > 1 && tokens[position - 2].getToken().matches("(?i)^[a-z]$") && (position + 1 < tokens.length) && tokens[position + 1].getToken().matches("(?i)^[a-z]$")) {
      // spelling with spaces in between: "b a s i c a l l y"
      return true;
    } else if (wordRepetitionOf("blah", tokens, position)) {
      return true;   // "blah blah"
    } else if (wordRepetitionOf("uh", tokens, position)) {
      return true;   // "uh uh"
    } else if (wordRepetitionOf("paw", tokens, position)) {
      return true;   // "paw paw"
    } else if (wordRepetitionOf("yum", tokens, position)) {
      return true;   // "yum yum"
    } else if (wordRepetitionOf("wop", tokens, position)) {
      return true;   // "wop wop"
    } else if (wordRepetitionOf("woop", tokens, position)) {
      return true;   // "woop woop"
    } else if (wordRepetitionOf("ha", tokens, position)) {
      return true;   // "ha ha"
    } else if (wordRepetitionOf("omg", tokens, position)) {
      return true;   // "omg omg"
    } else if (wordRepetitionOf("ta", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("la", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("x", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("hi", tokens, position)) {
      return true;   // "hi hi"
    } else if (wordRepetitionOf("ho", tokens, position)) {
      return true;   // "ho ho"
    } else if (wordRepetitionOf("heh", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("jay", tokens, position)) {
      return true; // Jay Jay (name)
    } else if (wordRepetitionOf("hey", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("hah", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("heh", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("oh", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("ouh", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("chop", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("ring", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("beep", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("bleep", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("yeah", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("wait", tokens, position) && position == 2) {
      return true;
    } else if (wordRepetitionOf("quack", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("meow", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("squawk", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("whoa", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("si", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("honk", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("brum", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("chi", tokens, position)) {
      // name
      return true;
    } else if (wordRepetitionOf("santorio", tokens, position)) {
      // name
      return true;
    } else if (wordRepetitionOf("lapu", tokens, position)) {
      // city
      return true;
    } else if (wordRepetitionOf("chow", tokens, position)) {
      // dog breed https://en.wikipedia.org/wiki/Chow_Chow
      return true;
    } else if (wordRepetitionOf("beep", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("shh", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("yummy", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("boom", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("bye", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("ah", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("aah", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("bang", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("woof", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("wink", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("yes", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("tsk", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("hush", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("ding", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("choo", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("Phi", tokens, position)) {
      return true;   // "Phi Phi Islands"
    } else if (wordRepetitionOf("Bora", tokens, position)) {
      return true;   // "Bora Bora"
    } else if (wordRepetitionOf("tuk", tokens, position)) {
      return true;
    } else if (wordRepetitionOf("yadda", tokens, position)) {
      return true;   // "yadda yadda"
    } else if (wordRepetitionOf("Pago", tokens, position)) {
      return true;   // "Pago Pago"
    } else if (wordRepetitionOf("Wagga", tokens, position)) {
      return true;   // "Wagga Wagga"
    } else if (wordRepetitionOf("Duran", tokens, position)) {
      return true;   // "Duran Duran"
    } else if (wordRepetitionOf("sapiens", tokens, position)) {
      return true;   // "Homo sapiens sapiens"
    } else if (wordRepetitionOf("tse", tokens, position)) {
      return true;   // "tse tse"
    } else if (wordRepetitionOf("Li", tokens, position)) {
      return true;   // "Li Li", Chinese name
    } else if (wordRepetitionOf("no", tokens, position)) {
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
    return false;
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

  private boolean wordRepetitionOf(String word, AnalyzedTokenReadings[] tokens, int position) {
    return tokens[position - 1].getToken().equalsIgnoreCase(word) && tokens[position].getToken().equalsIgnoreCase(word);
  }

}
