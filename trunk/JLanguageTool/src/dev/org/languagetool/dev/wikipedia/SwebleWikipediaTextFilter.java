package org.languagetool.dev.wikipedia;

import org.languagetool.TextFilter;
import org.sweble.wikitext.engine.Compiler;
import org.sweble.wikitext.engine.CompiledPage;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.utils.SimpleWikiConfiguration;

/**
 * Convert Wikipedia syntax to HTML using Sweble.
 */
public class SwebleWikipediaTextFilter implements TextFilter {

  private static final int WRAP_COL = 80;

  @Override
  public String filter(String wikiText) {
    try {
      final SimpleWikiConfiguration config = new SimpleWikiConfiguration(
              "classpath:/resources/SimpleWikiConfiguration.xml");
      final Compiler compiler = new Compiler(config);
      final PageTitle pageTitle = PageTitle.make(config, "fileTitle");
      final PageId pageId = new PageId(pageTitle, -1);
      final CompiledPage compiledPage = compiler.postprocess(pageId, wikiText, null);
      final TextConverter textConverter = new TextConverter(config, WRAP_COL);
      return (String) textConverter.go(compiledPage.getPage());
    } catch (Exception e) {
      throw new RuntimeException("Could not extract plain text from MediaWiki syntax: " + wikiText, e);
    }
  }

}
