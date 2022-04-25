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

import org.apache.commons.lang3.StringUtils;
//import org.sweble.wikitext.engine.CompiledPage;
//import org.sweble.wikitext.engine.Compiler;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
//import org.sweble.wikitext.engine.utils.SimpleWikiConfiguration;

/**
 * Convert Wikipedia syntax to HTML using Sweble.
 */
public class SwebleWikipediaTextFilter implements TextMapFilter {

  private static final int WRAP_COL = Integer.MAX_VALUE;

  //private final SimpleWikiConfiguration config;
//  private final Compiler compiler;
//  private final PageId pageId;
  
  private boolean enableMapping = true;
  
  public SwebleWikipediaTextFilter() {
//    try {
//      config = new SimpleWikiConfiguration(
//              "classpath:/org/languagetool/resource/dev/SimpleWikiConfiguration.xml");
//      compiler = new Compiler(config);
//      PageTitle pageTitle = PageTitle.make(config, "fileTitle");
//      pageId = new PageId(pageTitle, -1);
//    } catch (Exception e) {
//      throw new RuntimeException("Could not set up text filter", e);
//    }
  }

  @Override
  public PlainTextMapping filter(String wikiText) {
//    try {
//      CompiledPage compiledPage = compiler.postprocess(pageId, wikiText, null);
//      TextConverter textConverter = new TextConverter(config, WRAP_COL);
//      textConverter.enableMapping(enableMapping);
//      String plainText = (String) textConverter.go(compiledPage.getPage());
//      if (enableMapping) {
//        return new PlainTextMapping(plainText, textConverter.getMapping());
//      } else {
//        return new PlainTextMapping(plainText, null);
//      }
//    } catch (Exception e) {
//      throw new RuntimeException("Could not extract plain text from MediaWiki syntax: '"
//              + StringUtils.abbreviate(wikiText, 500) + "'", e);
//    }
    return new PlainTextMapping("", null);
  }

  /**
   * Set this to {@code false} for better performance. The result of {@link #filter(String)}
   * will then have a {@code null} mapping.
   */
  public void enableMapping(boolean enable) {
    enableMapping = enable;
  }
}
