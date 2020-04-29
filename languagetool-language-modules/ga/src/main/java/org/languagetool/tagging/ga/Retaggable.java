/*
 * Copyright 2019 Jim O'Regan <jaoregan@tcd.ie>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.languagetool.tagging.ga;

import java.util.Objects;

public class Retaggable {
  
  private String word;
  private String restrictToPos;
  private String appendTag;
  private String prefix;
  
  Retaggable(String word, String restrictToPos, String appendTag) {
    this.word = Objects.requireNonNull(word, "word must not be null");
    this.restrictToPos = restrictToPos;
    this.appendTag = appendTag;
  }
  
  Retaggable(String word, String restrictToPos, String appendTag, String prefix) {
    this(word, restrictToPos, appendTag);
    this.prefix = prefix;
  }

  void setAppendTag(String appendTag) {
    if(this.appendTag == null || "".equals(this.appendTag)) {
      this.appendTag = appendTag;
    } else {
      this.appendTag = this.appendTag + appendTag;
    }
  }

  void setRestrictToPos(String restrictToPos) {
    if(this.restrictToPos == null || "".equals(this.restrictToPos)) {
      this.restrictToPos = restrictToPos;
    } else {
      this.restrictToPos = this.restrictToPos + "|" + restrictToPos;
    }
  }

  public String getWord() {
    return word;
  }
  public String getRestrictToPos() {
    return restrictToPos;
  }
  public String getAppendTag() {
    return appendTag;
  }
  String getPrefix() {
    return prefix;
  }
}
