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

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.CharacterUtils;
import org.apache.lucene.util.AttributeFactory;

import java.io.IOException;

/**
 * A tokenizer that renders the whole input as one token.
 * 
 * @author Tao Lin
 */
public final class AnyCharTokenizer extends Tokenizer {

  private static final int MAX_WORD_LEN = Integer.MAX_VALUE; // extend the word length!

  private final CharacterUtils.CharacterBuffer ioBuffer = CharacterUtils.newCharacterBuffer(4096);
  private final CharacterUtils charUtils = CharacterUtils.getInstance();
  private final CharTermAttribute termAtt = (CharTermAttribute)this.addAttribute(CharTermAttribute.class);
  private final OffsetAttribute offsetAtt = (OffsetAttribute)this.addAttribute(OffsetAttribute.class);

  private int bufferIndex = 0;
  private int dataLen = 0;
  private int offset = 0;
  private int finalOffset = 0;
  
  /**
   * Construct a new AnyCharTokenizer.
   */
  public AnyCharTokenizer() {
    super();
  }

  /**
   * Construct a new AnyCharTokenizer using a given
   * {@link org.apache.lucene.util.AttributeFactory}.
   * @param factory the attribute factory to use for this {@link org.apache.lucene.analysis.Tokenizer}
   */
  public AnyCharTokenizer(AttributeFactory factory) {
    super(factory);
  }

  /**
   * Collects any characters.
   */
  protected boolean isTokenChar(int c) {
    return true;
  }

  protected int normalize(int c) {
    return c;
  }

  @Override
  public boolean incrementToken() throws IOException {
    this.clearAttributes();
    int length = 0;
    int start = -1;
    int end = -1;
    char[] buffer = this.termAtt.buffer();

    while(true) {
      if(this.bufferIndex >= this.dataLen) {
        this.offset += this.dataLen;
        this.charUtils.fill(this.ioBuffer, this.input);
        if(this.ioBuffer.getLength() == 0) {
          this.dataLen = 0;
          if(length <= 0) {
            this.finalOffset = this.correctOffset(this.offset);
            return false;
          }
          break;
        }

        this.dataLen = this.ioBuffer.getLength();
        this.bufferIndex = 0;
      }

      int c = this.charUtils.codePointAt(this.ioBuffer.getBuffer(), this.bufferIndex, this.ioBuffer.getLength());
      int charCount = Character.charCount(c);
      this.bufferIndex += charCount;
      if(this.isTokenChar(c)) {
        if(length == 0) {
          assert start == -1;

          start = this.offset + this.bufferIndex - charCount;
          end = start;
        } else if(length >= buffer.length - 1) {
          buffer = this.termAtt.resizeBuffer(2 + length);
        }

        end += charCount;
        length += Character.toChars(this.normalize(c), buffer, length);
        if(length >= MAX_WORD_LEN) {
          break;
        }
      } else if(length > 0) {
        break;
      }
    }

    this.termAtt.setLength(length);

    assert start != -1;

    this.offsetAtt.setOffset(this.correctOffset(start), this.finalOffset = this.correctOffset(end));
    return true;
  }

  @Override
  public void end() throws IOException {
    super.end();
    this.offsetAtt.setOffset(this.finalOffset, this.finalOffset);
  }

  @Override
  public void reset() throws IOException {
    super.reset();
    this.bufferIndex = 0;
    this.offset = 0;
    this.dataLen = 0;
    this.finalOffset = 0;
    this.ioBuffer.reset();
  }

}
