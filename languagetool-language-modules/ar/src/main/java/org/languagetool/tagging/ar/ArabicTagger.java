/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging.ar;

import java.util.Locale;

import org.languagetool.tagging.BaseTagger;

/**
 * Arabic Part-of-speech tagger.
 * The POS tagset is described in
 * <a href="https://github.com/languagetool-org/languagetool/blob/master/languagetool-language-modules/ar/src/main/resources/org/languagetool/resource/ar/tagset.txt">tagset.txt</a>
 * 
 * @author Taha Zerrouki
 */
public class ArabicTagger extends BaseTagger {

  @Override
  public String getManualAdditionsFileName() {
    return "/ar/added.txt";
  }

  @Override
  public String getManualRemovalsFileName() {
    return "/ar/removed.txt";
  }

  public ArabicTagger() {
    super("/ar/arabic.dict");
  }
}
