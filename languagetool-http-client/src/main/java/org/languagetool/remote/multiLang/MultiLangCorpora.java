/*
 * LanguageTool, a natural language style checker
 * Copyright (c) 2022.  Stefan Viol (https://stevio.de)
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  USA
 */

package org.languagetool.remote.multiLang;

import java.util.ArrayList;
import java.util.List;

public class MultiLangCorpora {

  private String language;
  private String text = "";
  private List<InjectedSentence> injectedSentences = new ArrayList<>();

  private int sentencesInText;

  public MultiLangCorpora(String language) {
    this.language = language;
  }

  public String getLanguage() {
    return language;
  }

  public String getText() {
    return text.trim();
  }

  public List<InjectedSentence> getInjectedSentences() {
    return injectedSentences;
  }

  public void injectOtherSentence(String injectLanguage, String sentence) {
    //System.out.println("Add otherLangText: " + sentence);
    this.text += " " + sentence;
    this.injectedSentences.add(new InjectedSentence(injectLanguage, sentence));
    this.sentencesInText++;
  }

  public void addSentence(String sentence) {
    //System.out.println("Add mainLangText: " + sentence);
    this.text += " " + sentence;
    this.sentencesInText++;
  }

  public int getSentencesInText() {
    return sentencesInText;
  }
}
