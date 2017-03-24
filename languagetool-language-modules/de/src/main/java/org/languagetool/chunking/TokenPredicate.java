/* LanguageTool, a natural language style checker
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.chunking;

import edu.washington.cs.knowitall.logic.Expression;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;

import java.util.regex.Pattern;

final class TokenPredicate extends Expression.Arg.Pred<ChunkTaggedToken> {

  private final boolean caseSensitive;

  TokenPredicate(String description, boolean caseSensitive) {
    super(description);
    this.caseSensitive = caseSensitive;
  }

  @Override
  public boolean apply(ChunkTaggedToken analyzedToken) {
    String[] parts = getDescription().split("=");
    String exprType;
    String exprValue;
    if (parts.length == 1) {
      exprType = "string";
      exprValue = parts[0];
    } else if (parts.length == 2) {
      exprType = parts[0];
      exprValue = parts[1];
    } else {
      throw new RuntimeException("Could not parse expression: " + getDescription());
    }
    if (exprValue.startsWith("'") && exprValue.endsWith("'")) {
      exprValue = exprValue.substring(1, exprValue.length()-1);
    }
    switch (exprType) {

      case "string":
        if (caseSensitive) {
          return analyzedToken.getToken().equals(exprValue);
        } else {
          return analyzedToken.getToken().equalsIgnoreCase(exprValue);
        }

      case "regex":
        Pattern p1 = caseSensitive ? Pattern.compile(exprValue) : Pattern.compile(exprValue, Pattern.CASE_INSENSITIVE);
        return p1.matcher(analyzedToken.getToken()).matches();

      case "regexCS":  // case sensitive
        Pattern p2 = Pattern.compile(exprValue);
        return p2.matcher(analyzedToken.getToken()).matches();

      case "chunk":
        Pattern chunkPattern = Pattern.compile(exprValue);
        for (ChunkTag chunkTag : analyzedToken.getChunkTags()) {
          if (chunkPattern.matcher(chunkTag.getChunkTag()).matches()) {
            return true;
          }
        }
        return false;

      case "pos":
        AnalyzedTokenReadings readings = analyzedToken.getReadings();
        if (readings != null) {
          for (AnalyzedToken token : readings) {
            if (token.getPOSTag() != null && token.getPOSTag().contains(exprValue)) {
              return true;
            }
          }
        }
        return false;

      case "posre":
      case "posregex":
        Pattern posPattern = Pattern.compile(exprValue);
        AnalyzedTokenReadings readings2 = analyzedToken.getReadings();
        if (readings2 != null) {
          for (AnalyzedToken token : readings2) {
            if (token.getPOSTag() != null && posPattern.matcher(token.getPOSTag()).matches()) {
              return true;
            }
          }
        }
        return false;

      default:
        throw new RuntimeException("Expression type not supported: '" + exprType + "'");
    }
  }
}
