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

import javax.swing.JOptionPane;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.AbstractFillerWordsRule;

/**
 * A rule that gives Hints about the use of German filler words.
 * The Hints are only given when the percentage of filler words per paragraph exceeds the given limit.
 * A limit of 0 shows all used filler words. Direct speech or citation is excluded otherwise. 
 * This rule detects no grammar error but gives stylistic hints (default off).
 * @author Fred Kruse
 * @since 4.2
 */
public class GermanFillerWordsRule extends AbstractFillerWordsRule {

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
  
  public GermanFillerWordsRule(ResourceBundle messages, Language lang, UserConfig userConfig) {
    super(messages, lang, userConfig);
  }

  @Override
  public String getId() {
    return RULE_ID + "_DE";
  }

  @Override
  protected boolean isFillerWord(String token) {
    return fillerWords.contains(token);
  }

  @Override
  public boolean isException(AnalyzedTokenReadings[] tokens, int num) {
    if ("aber".equals(tokens[num].getToken()) && num >= 1 && ",".equals(tokens[num - 1].getToken())) {
      return true;
    }
    return false;
  }
  
}
