package org.languagetool.rules.ga;
/*
 * Copyright © 2019 Jim O'Regan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class TriailError {
  int fromX;
  int fromY;
  int toX;
  int toY;
  String ruleID;
  String msg;
  String context;
  int contextOffset;
  int errorLength;

  public int getFromX() {
    return fromX;
  }

  public void setFromX(int fromX) {
    this.fromX = fromX;
  }

  public int getFromY() {
    return fromY;
  }

  public void setFromY(int fromY) {
    this.fromY = fromY;
  }

  public int getToX() {
    return toX;
  }

  public void setToX(int toX) {
    this.toX = toX;
  }

  public int getToY() {
    return toY;
  }

  public void setToY(int toY) {
    this.toY = toY;
  }

  public String getRuleID() {
    return ruleID;
  }

  public void setRuleID(String ruleID) {
    this.ruleID = ruleID;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
  }

  public int getContextOffset() {
    return contextOffset;
  }

  public void setContextOffset(int contextOffset) {
    this.contextOffset = contextOffset;
  }

  public int getErrorLength() {
    return errorLength;
  }

  public void setErrorLength(int errorLength) {
    this.errorLength = errorLength;
  }

  TriailError() {}
  TriailError(int fromY, int fromX, int toY, int toX, String ruleID, String msg, String context, int contextOffset, int errorLength) {
    this.fromX = fromX;
    this.fromY = fromY;
    this.toX = toX;
    this.toY = toY;
    this.ruleID = ruleID;
    this.msg = msg;
    this.context = context;
    this.contextOffset = contextOffset;
    this.errorLength = errorLength;
  }
}
