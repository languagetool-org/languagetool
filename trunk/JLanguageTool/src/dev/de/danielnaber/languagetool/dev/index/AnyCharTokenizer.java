package de.danielnaber.languagetool.dev.index;

import java.io.Reader;

import org.apache.lucene.analysis.CharTokenizer;

//TODO Tao: make sure AnyCharTokenizer collect any characters and render them as a Token
public class AnyCharTokenizer extends CharTokenizer {

  public AnyCharTokenizer(AttributeFactory factory, Reader input) {
    super(factory, input);
  }

}
