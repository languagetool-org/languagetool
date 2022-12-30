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
package org.languagetool.rules.de;

import java.util.Arrays;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.LinguServices;
import org.languagetool.UserConfig;
import org.languagetool.rules.AbstractStatisticStyleRule;

/**
 * A rule that gives Hints about the use of German filler words.
 * The Hints are only given when the percentage of filler words per chapter/text exceeds the given limit.
 * A limit of 0 shows all used filler words. Direct speech or citation is excluded otherwise. 
 * This rule detects no grammar error but gives stylistic hints (default off).
 * @author Fred Kruse
 * @since 4.2
 */
public class GermanFillerWordsRule extends AbstractStatisticStyleRule {
  
  private static final int DEFAULT_MIN_PERCENT = 8;
  private static final String DEFAULT_SENTENCE_MSG1 = "Zwei potentielle Füllwörter hintereinander. Mindestens eins sollte gelöscht werden.";
  private static final String DEFAULT_SENTENCE_MSG2 = "Mehr als zwei potentielle Füllwörter in einem Satz. Mindestens eins sollte gelöscht werden.";

  private static final Set<String> fillerWords = new HashSet<>(Arrays.asList( "aber","abermals","allein","allemal","allenfalls","allenthalben","allerdings","allesamt","allzu","also",
      "alt","andauernd","andererseits","andernfalls","anscheinend","auch","auffallend","augenscheinlich","ausdrücklich","ausgerechnet","ausnahmslos",
      "außerdem","äußerst","beinahe","bekanntlich","bereits","besonders","bestenfalls","bestimmt","bloß","dabei","dadurch","dafür","dagegen","daher","damals",
      "danach","demgegenüber","demgemäß","demnach","denkbar","denn","dennoch","deshalb","deswegen","doch","durchaus","durchweg","eben","eigentlich",
      "einerseits","einfach","einige","einigermaßen","einmal","ergo","erheblich","etliche","etwa","etwas","fast","folgendermaßen","folglich","förmlich",
      "fortwährend","fraglos","freilich","ganz","gänzlich","gar","gelegentlich","gemeinhin","genau","geradezu","gewiss","gewissermaßen","glatt","gleichsam",
      "gleichwohl","glücklicherweise","gottseidank","größtenteils","häufig","hingegen","hinlänglich","höchst","höchstens","immer","immerhin","immerzu",
      "indessen","infolgedessen","insbesondere","inzwischen","irgend","irgendein","irgendjemand","irgendwann","irgendwie","irgendwo","ja","je",
      "jedenfalls","jedoch","jemals","kaum","keinesfalls","keineswegs","längst","lediglich","leider","letztlich","manchmal","mehrfach","meinetwegen",
      "meist","meistens","meistenteils","mindestens","mithin","mitunter","möglicherweise","möglichst","nämlich","naturgemäß","natürlich","neuerdings",
      "neuerlich","neulich","nichtsdestoweniger","nie","niemals","nun","nur","offenbar","offenkundig","offensichtlich","oft","ohnedies","partout",
      "plötzlich","praktisch","quasi","recht","reichlich","reiflich","relativ","restlos","richtiggehend","rundheraus","rundum","sattsam","schlicht",
      "schlichtweg","schließlich","schlussendlich","schon","sehr","selbst","selbstredend","selbstverständlich","selten","seltsamerweise","sicher",
      "sicherlich","so","sogar","sonst","sowieso","sozusagen","stellenweise","stets","trotzdem","überaus","überdies","überhaupt","übrigens",
      "umständehalber","unbedingt","unerhört","ungefähr","ungemein","ungewöhnlich","ungleich","unglücklicherweise","unlängst","unmaßgeblich",
      "unsagbar","unsäglich","unstreitig","unzweifelhaft","vergleichsweise","vermutlich","vielfach","vielleicht","voll","vollends","völlig",
      "vollkommen","vollständig","wahrscheinlich","weidlich","weitgehend","wenigstens","wieder","wiederum","wirklich","wohl","wohlgemerkt",
      "womöglich","ziemlich","zudem","zugegeben","zumeist","zusehends","zuweilen","zweifellos","zweifelsfrei","zweifelsohne"
  ));
  
  String sentenceMessage = null;
  
  public GermanFillerWordsRule(ResourceBundle messages, Language lang, UserConfig userConfig) {
    super(messages, lang, userConfig, DEFAULT_MIN_PERCENT);
    if (userConfig != null) {
      LinguServices linguServices = userConfig.getLinguServices();
      if (linguServices != null) {
        linguServices.setThesaurusRelevantRule(this);
      }
    }
  }

  private static boolean isException(AnalyzedTokenReadings[] tokens, int num) {
    if (num == 1 || ",".equals(tokens[num - 1].getToken())) {
      return true;
    }
    if ("allein".equals(tokens[num].getToken())) {
      for(int i = 1; i < tokens.length; i++) {
        if (tokens[i].hasLemma("sein")) {
          return true;
        }
      }
      return false;
    }
    if ("recht".equals(tokens[num].getToken())) {
      for(int i = 1; i < tokens.length; i++) {
        if (tokens[i].hasAnyLemma("haben", "geben")) {
          return true;
        }
      }
    }
    if (num < tokens.length - 1 && ("so".equals(tokens[num].getToken()) || "besonders".equals(tokens[num].getToken())) 
        && tokens[num + 1].hasPosTagStartingWith("ADJ")) {
      return true;
    }
    if(tokens[num].hasPosTagStartingWith("ADJ") && "so".equals(tokens[num - 1].getToken())) {
      return true;
    }
    if ("nur".equals(tokens[num].getToken()) && "nicht".equals(tokens[num - 1].getToken())) {
      for(int i = num + 1; i < tokens.length - 2; i++) {
        if (",".equals(tokens[i].getToken()) && ("auch".equals(tokens[i + 1].getToken()) 
            || ("sondern".equals(tokens[i + 1].getToken()) && "auch".equals(tokens[i + 2].getToken())))) {
          return true;
        }
      }
    }
    if (num > 2 && "auch".equals(tokens[num].getToken()) && "sondern".equals(tokens[num - 1].getToken()) && ",".equals(tokens[num - 2].getToken())) {
      for(int i = 1; i < num - 2; i++) {
        if ("nicht".equals(tokens[i].getToken()) && "nur".equals(tokens[i + 1].getToken())){
          return true;
        }
      }
    }
    return false;
  }

  @Override
  protected int conditionFulfilled(AnalyzedTokenReadings[] tokens, int nToken) {
    if (fillerWords.contains(tokens[nToken].getToken()) && !isException(tokens, nToken) 
         && (nToken < 2 || !isTwoWordException(tokens[nToken - 1].getToken(), tokens[nToken].getToken())) 
         && (nToken > tokens.length - 2 || !isTwoWordException(tokens[nToken].getToken(), tokens[nToken + 1].getToken()))
         ) {
      return nToken;
    }
    return -1;
  }
  
  private static boolean isTwoWordException(String first, String second) {
    return (("aber".equals(first) && ("nur".equals(second) || "auch".equals(second)))
        || ("auch".equals(first) && "nur".equals(second))
        || ("immer".equals(first) && "wieder".equals(second))
        || ("genau".equals(first) && "so".equals(second))
        || ("so".equals(first) && ("etwas".equals(second) || "viel".equals(second) || "oft".equals(second)))
        || ("schon".equals(first) && "fast".equals(second))
        );
  }

  @Override
  protected boolean sentenceConditionFulfilled(AnalyzedTokenReadings[] tokens, int nToken) {
    if ((nToken > 1 && fillerWords.contains(tokens[nToken - 1].getToken()) && !isException(tokens, nToken - 1)) || 
        (nToken < tokens.length - 1 && fillerWords.contains(tokens[nToken + 1].getToken()) && !isException(tokens, nToken + 1))) {
      sentenceMessage = DEFAULT_SENTENCE_MSG1;
      return true;
    }
    int n = 0;
    for (int i = nToken - 2; i > 0; i--) {
      if (conditionFulfilled(tokens, i) == i) {
        n++;
        if (n > 1) {
          sentenceMessage = DEFAULT_SENTENCE_MSG2;
          return true;
        }
      }
    }
    for (int i = nToken + 2; i < tokens.length; i++) {
      if (conditionFulfilled(tokens, i) == i) {
        n++;
        if (n > 1) {
          sentenceMessage = DEFAULT_SENTENCE_MSG2;
          return true;
        }
      }
    }
    return false;
  }

  @Override
  protected boolean excludeDirectSpeech() {
    return true;
  }

  @Override
  protected String getLimitMessage(int limit, double percent) {
    if (limit == 0) {
      return "Dieses Wort könnte ein Füllwort sein. Möglicherweise ist es besser es zu löschen.";
    }
    return "Mehr als " + limit + "% Füllwörter {" + ((int) (percent +0.5d)) + "%} gefunden. Möglicherweise ist es besser dieses potentielle Füllwort zu löschen.";
  }

  @Override
  protected String getSentenceMessage() {
    return sentenceMessage;
  }

  @Override
  public String getId() {
    return "FILLER_WORDS_DE";
  }

  @Override
  public String getDescription() {
    return "Statistische Stilanalyse: Füllwörter";
  }

  @Override
  public String getConfigureText() {
    return "Anzeigen wenn mehr als ...% eines Kapitels Füllwörter sind:";
  }

}
