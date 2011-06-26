package de.danielnaber.languagetool.dev.index;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Version;

import de.danielnaber.languagetool.JLanguageTool;

/**
 * LanguageToolAnalyzer emits the entire input (i.e. a sentence) as a single token by
 * AnyCharTokenizer, and then use JLanguageTool to analyze and tag the tokens by LanguageToolFilter.
 * 
 * @author Tao Lin
 * 
 */
public class LanguageToolAnalyzer extends Analyzer {

  private final JLanguageTool languageTool;

  private final Version matchVersion;

  public LanguageToolAnalyzer(Version matchVersion, JLanguageTool languageTool) {
    super();
    this.matchVersion = matchVersion;
    this.languageTool = languageTool;
  }

  @Override
  public TokenStream tokenStream(String fieldName, Reader reader) {
    TokenStream result = new AnyCharTokenizer(this.matchVersion, reader);
    result = new LanguageToolFilter(result, languageTool);
    return result;
  }

}
