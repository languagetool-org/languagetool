/* 
 * Taken from Heritrix 1.1.14. (org/archive/util/InterruptibleCharSequence.java)
 * Original copyright:
 *
 * Created on Jun 27, 2007
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of the Heritrix web crawler (crawler.archive.org).
 *
 * Heritrix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Heritrix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Heritrix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.languagetool.tools;

import org.jetbrains.annotations.NotNull;

/**
 * CharSequence that noticed thread interrupts -- as might be necessary 
 * to recover from a loose regex on unexpected challenging input. 
 *
 * @author gojomo
 */
public class InterruptibleCharSequence implements CharSequence {

  private CharSequence inner;

  public InterruptibleCharSequence(CharSequence inner) {
    super();
    this.inner = inner;
  }

  public char charAt(int index) {
    if (Thread.interrupted()) {
      throw new RuntimeException(new InterruptedException());
    }
    return inner.charAt(index);
  }

  public int length() {
    return inner.length();
  }

  public CharSequence subSequence(int start, int end) {
    return new InterruptibleCharSequence(inner.subSequence(start, end));
  }

  @NotNull
  @Override
  public String toString() {
    return inner.toString();
  }
}