package org.languagetool.dev.wikipedia;

public interface TextMapFilter {

  /**
   * Filter the given text, keeping a mapping from plain text to original markup positions.
   */
  PlainTextMapping filter(String text);

}
