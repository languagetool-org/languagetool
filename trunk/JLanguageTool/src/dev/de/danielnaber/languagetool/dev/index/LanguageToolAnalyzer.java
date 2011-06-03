package de.danielnaber.languagetool.dev.index;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Version;

import de.danielnaber.languagetool.JLanguageTool;

public class LanguageToolAnalyzer extends Analyzer {
  private JLanguageTool lt;

  private Version matchVersion;

  public LanguageToolAnalyzer(Version matchVersion, JLanguageTool lt) {
    super();
    this.matchVersion = matchVersion;
    this.lt = lt;
  }

  @Override
  public TokenStream tokenStream(String fieldName, Reader reader) {
    // TODO Auto-generated method stub
    return null;
  }

}
