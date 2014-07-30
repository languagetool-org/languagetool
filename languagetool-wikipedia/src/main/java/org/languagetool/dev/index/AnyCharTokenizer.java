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
package org.languagetool.dev.index;

import java.io.Reader;

import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.util.AttributeFactory;
import org.apache.lucene.util.Version;

/**
 * A tokenizer that renders the whole input as one token.
 * 
 * @author Tao Lin
 */
public final class AnyCharTokenizer extends CharTokenizer {

  /**
   * Construct a new AnyCharTokenizer.
   * 
   * @param matchVersion Lucene version to match
   * @param in the input to split up into tokens
   */
  public AnyCharTokenizer(Version matchVersion, Reader in) {
    super(matchVersion, in);
  }

  /**
   * Construct a new AnyCharTokenizer using a given
   * {@link org.apache.lucene.util.AttributeFactory}.
   * 
   * @param matchVersion Lucene version to match
   * @param factory the attribute factory to use for this {@link org.apache.lucene.analysis.Tokenizer}
   * @param in the input to split up into tokens
   */
  public AnyCharTokenizer(Version matchVersion, AttributeFactory factory, Reader in) {
    super(matchVersion, factory, in);
  }

  /**
   * Collects any characters.
   */
  @Override
  protected boolean isTokenChar(int c) {
    return true;
  }

}
