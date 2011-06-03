package de.danielnaber.languagetool.dev.index;

import java.io.Reader;

import org.apache.lucene.analysis.CharTokenizer;

public class AnyCharTokenizer extends CharTokenizer {

  public AnyCharTokenizer(AttributeFactory factory, Reader input) {
    super(factory, input);
    // TODO Auto-generated constructor stub
  }

}
