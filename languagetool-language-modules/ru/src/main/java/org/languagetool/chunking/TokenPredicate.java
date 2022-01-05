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
import org.languagetool.rules.patterns.StringMatcher;

import java.util.function.Predicate;

final class TokenPredicate extends Expression.Arg.Pred<ChunkTaggedToken> {
  private final Predicate<ChunkTaggedToken> predicate;

  TokenPredicate(String description, boolean caseSensitive) {
    super(description);
    predicate = compile(description, caseSensitive);
  }

  private Predicate<ChunkTaggedToken> compile(String description, boolean caseSensitive) {
    String[] parts = description.split("=");
    String exprType;
    String exprValue;
    if (parts.length == 1) {
      exprType = "string";
      exprValue = unquote(parts[0]);
    } else if (parts.length == 2) {
      exprType = parts[0];
      exprValue = unquote(parts[1]);
    } else {
      throw new RuntimeException("Could not parse expression: " + getDescription());
    }

    switch (exprType) {
      case "string":
      case "regex":
      case "regexCS":
        StringMatcher matcher = StringMatcher.create(exprValue, !"string".equals(exprType), caseSensitive || "regexCS".equals(exprType));
        return analyzedToken -> matcher.matches(analyzedToken.getToken());

      case "chunk":
        StringMatcher chunkPattern = StringMatcher.create(exprValue, true, true);
        return analyzedToken -> {
          for (ChunkTag chunkTag : analyzedToken.getChunkTags()) {
            if (chunkPattern.matches(chunkTag.getChunkTag())) {
              return true;
            }
          }
          return false;
        };

      case "pos":
        return analyzedToken -> {
          AnalyzedTokenReadings readings = analyzedToken.getReadings();
          if (readings != null) {
            for (AnalyzedToken token : readings) {
              if (token.getPOSTag() != null && token.getPOSTag().contains(exprValue)) {
                return true;
              }
            }
          }
          return false;
        };

      case "posre":
      case "posregex":
        StringMatcher posPattern = StringMatcher.create(exprValue, true, true);
        return analyzedToken -> {
          AnalyzedTokenReadings readings = analyzedToken.getReadings();
          if (readings != null) {
            for (AnalyzedToken token : readings) {
              if (token.getPOSTag() != null && posPattern.matches(token.getPOSTag())) {
                return true;
              }
            }
          }
          return false;
        };

      default:
        throw new RuntimeException("Expression type not supported: '" + exprType + "'");
    }

  }

  private static String unquote(String s) {
    return s.startsWith("'") && s.endsWith("'") ? s.substring(1, s.length() - 1) : s;
  }

  @Override
  public boolean apply(ChunkTaggedToken analyzedToken) {
    return predicate.test(analyzedToken);
  }
}
