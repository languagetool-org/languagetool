/* LanguageTool, a natural language style checker 
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.bigdata;

import org.languagetool.AnalyzedTokenReadings;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 3.3
 */
public class ContextBuilder {

  private final String startMarker;
  private final String endMarker;

  public ContextBuilder() {
    this.startMarker = "_START_";
    this.endMarker = "_END_";
  }
  
  public ContextBuilder(String startMarker, String endMarker) {
    this.startMarker = startMarker;
    this.endMarker = endMarker;
  }
  
  public List<String> getContext(AnalyzedTokenReadings[] tokens, int pos, int contextSize) {
    List<String> l = new ArrayList<>();
    int i = 0;
    for (AnalyzedTokenReadings token : tokens) {
      if (i == pos) {
        l.addAll(getLeftContext(tokens, pos, contextSize));
        l.add(token.getToken());
        l.addAll(getRightContext(tokens, pos, contextSize));
        break;
      }
      i++;
    }
    return l;
  }

  private List<String> getLeftContext(AnalyzedTokenReadings[] tokens, int pos, int contextSize) {
    List<String> l = new ArrayList<>();
    for (int i = pos - 1; i >= 0 && l.size() < contextSize; i--) {
      if (i == 0) {
        l.add(0, startMarker);
      } else {
        l.add(0, tokens[i].getToken());
      }
    }
    return l;
  }

  private List<String> getRightContext(AnalyzedTokenReadings[] tokens, int pos, int contextSize) {
    List<String> l = new ArrayList<>();
    for (int i = pos + 1; i <= tokens.length && l.size() < contextSize; i++) {
      if (i == tokens.length) {
        l.add(endMarker);
      } else {
        l.add(tokens[i].getToken());
      }
    }
    return l;
  }
}
