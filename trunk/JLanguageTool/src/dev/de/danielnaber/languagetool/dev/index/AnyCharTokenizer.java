package de.danielnaber.languagetool.dev.index;

import java.io.Reader;

import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.util.Version;

//TODO Tao: make sure AnyCharTokenizer collect any characters and render them as a Token
public class AnyCharTokenizer extends CharTokenizer {

  /**
   * Construct a new AnyCharTokenizer. * @param matchVersion Lucene version to match See
   * {@link <a href="#version">above</a>}
   * 
   * @param in
   *          the input to split up into tokens
   */
  public AnyCharTokenizer(Version matchVersion, Reader in) {
    super(matchVersion, in);
  }

}
