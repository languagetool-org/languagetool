/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.errorcorpus;

/**
 * An error from an error corpus.
 * @since 2.7
 */
public class Error {
  
  private final int startPos;
  private final int endPos;
  private final String correction;

  Error(int startPos, int endPos, String correction) {
    this.startPos = startPos;
    this.endPos = endPos;
    this.correction = correction;
  }

  public int getStartPos() {
    return startPos;
  }

  public int getEndPos() {
    return endPos;
  }

  public String getCorrection() {
    return correction;
  }

  public String getAppliedCorrection(String markupText) {
    String correctionApplied = markupText.substring(0, startPos) + correction + markupText.substring(endPos);
    return correctionApplied.replaceAll("<.*?>", "");
  }

  @Override
  public String toString() {
    return startPos + "-" + endPos + ":" + correction;
  }
}
