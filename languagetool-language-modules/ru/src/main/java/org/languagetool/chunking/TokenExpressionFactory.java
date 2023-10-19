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

import edu.washington.cs.knowitall.logic.LogicExpression;
import edu.washington.cs.knowitall.regex.Expression;
import edu.washington.cs.knowitall.regex.ExpressionFactory;

/**
 * @since 5.6
 */
final class TokenExpressionFactory extends ExpressionFactory<ChunkTaggedToken> {

  private final boolean caseSensitive;

  /**
   * @param caseSensitive whether word tokens should be compared case-sensitively - also used for regular expressions
   */
  TokenExpressionFactory(boolean caseSensitive) {
    this.caseSensitive = caseSensitive;
  }

  @Override
  public Expression.BaseExpression<ChunkTaggedToken> create(String expr) {
    LogicExpression<ChunkTaggedToken> logicExpression = LogicExpression.compile(expr, input -> new TokenPredicate(input, caseSensitive));
    return new Expression.BaseExpression<ChunkTaggedToken>(expr) {
      @Override
      public boolean apply(ChunkTaggedToken token) {
        return logicExpression.apply(token);
      }
    };
  }

}
