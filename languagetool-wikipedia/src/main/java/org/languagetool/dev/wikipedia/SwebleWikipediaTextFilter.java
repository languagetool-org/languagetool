/* LanguageTool, a natural language style checker
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.dev.wikipedia;

import org.languagetool.TextFilter;
import org.sweble.wikitext.engine.CompiledPage;
import org.sweble.wikitext.engine.Compiler;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.utils.SimpleWikiConfiguration;

/**
 * Convert Wikipedia syntax to HTML using Sweble.
 */
public class SwebleWikipediaTextFilter implements TextFilter {

  private static final int WRAP_COL = Integer.MAX_VALUE;

  @Override
  public String filter(String wikiText) {
    try {
      final SimpleWikiConfiguration config = new SimpleWikiConfiguration(
              "classpath:/org/languagetool/resource/dev/SimpleWikiConfiguration.xml");
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
