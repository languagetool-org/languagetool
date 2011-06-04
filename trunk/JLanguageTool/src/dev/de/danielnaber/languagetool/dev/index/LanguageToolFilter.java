package de.danielnaber.languagetool.dev.index;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

import de.danielnaber.languagetool.JLanguageTool;

public class LanguageToolFilter extends TokenFilter {
  private JLanguageTool lt;

  protected LanguageToolFilter(TokenStream input, JLanguageTool lt) {
    super(input);
    this.lt = lt;

  }

  @Override
  public boolean incrementToken() throws IOException {
    // TODO Tao: use JLanguageTool to make tokens
    return false;
  }

}
