/* LanguageTool, a natural language style checker 
 * Copyright (C) 2010 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.dev.wikipedia;

import info.bliki.wiki.model.WikiModel;

import org.apache.commons.lang.StringEscapeUtils;

import de.danielnaber.languagetool.TextFilter;

/**
 * Convert Wikipedia syntax to HTML using Bliki and then try to clean it up (this is
 * rather ugly).
 */
class WikipediaTextFilter implements TextFilter {

  public String filter(String s) {
    // TODO: find general HTML to Text converter?!:
    final WikiModel wikiModel = new WikiModel("${image}", "${title}");
    s = wikiModel.render(s);
    //System.out.println("0####"+s);
    s = s.replaceAll("\\{\\{.*?\\}\\}", "");
    s = s.replaceAll("</p>", "\n\n");
    s = s.replaceAll("</dt>", "\n\n");
    s = s.replaceAll("</dl>", "\n\n");
    s = s.replaceAll("</h\\d>", "\n\n");
    s = s.replaceAll("<a href=\"http://[a-zA-Z-]+\\.wikipedia\\.org/wiki/.*?\">.*?</a>", "");
    s = s.replaceAll("<.*?>", "");
    s = s.replaceAll("\n\n*", "\n\n");    // single line break isn't detected as paragraph in LT by default
    s = StringEscapeUtils.unescapeHtml(s);
    //System.out.println("1############################################\n"+s);
    //System.out.println("/############################################"+s);
    return s;
  }

}
