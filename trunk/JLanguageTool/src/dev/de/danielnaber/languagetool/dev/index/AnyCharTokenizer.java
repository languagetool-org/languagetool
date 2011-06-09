package de.danielnaber.languagetool.dev.index;

import java.io.Reader;

import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.Version;

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

  /**
   * Construct a new AnyCharTokenizer using a given {@link AttributeSource}.
   * 
   * @param matchVersion
   *          Lucene version to match See {@link <a href="#version">above</a>}
   * @param source
   *          the attribute source to use for this {@link Tokenizer}
   * @param in
   *          the input to split up into tokens
   */
  public AnyCharTokenizer(Version matchVersion, AttributeSource source, Reader in) {
    super(matchVersion, source, in);
  }

  /**
   * Construct a new AnyCharTokenizer using a given
   * {@link org.apache.lucene.util.AttributeSource.AttributeFactory}.
   * 
   * @param matchVersion
   *          Lucene version to match See {@link <a href="#version">above</a>}
   * @param factory
   *          the attribute factory to use for this {@link Tokenizer}
   * @param in
   *          the input to split up into tokens
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
