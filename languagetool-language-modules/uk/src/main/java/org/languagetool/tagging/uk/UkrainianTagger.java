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
package org.languagetool.tagging.uk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.languagetool.tagging.BaseTagger;
import org.languagetool.AnalyzedToken;

/** 
 * Ukrainian part-of-speech tagger.
 * See README for details, the POS tagset is
 * described in tagset.txt
 * 
 * @author Andriy Rysin
 */
public class UkrainianTagger extends BaseTagger {
  private static final Pattern NUMBER = Pattern.compile("[+-]?[0-9]+(,[0-9]+)?([-–—][0-9]+(,[0-9]+)?)?(%|°С?)?");

  public static final Map<String, String> VIDMINKY_MAP;

  static {
    HashMap<String, String> map = new HashMap<String, String>();
    map.put("v_naz", "називний");
    map.put("v_rod", "родовий");
    map.put("v_dav", "давальний");
    map.put("v_zna", "знахідний");
    map.put("v_oru", "орудний");
    map.put("v_mis", "місцевий");
    map.put("v_kly", "кличний");
    VIDMINKY_MAP = Collections.unmodifiableMap(map);
  }

  @Override
  public final String getFileName() {
    return "/uk/ukrainian.dict";
  }

  public UkrainianTagger() {
    super();
    setLocale(new Locale("uk", "UA"));
    dontTagLowercaseWithUppercase();
  }

  @Override
  public List<AnalyzedToken> additionalTags(String word) {
    if ( NUMBER.matcher(word).matches() ){
      List<AnalyzedToken> additionalTaggedTokens  = new ArrayList<>();
      additionalTaggedTokens.add(new AnalyzedToken(word, IPOSTag.number.getText(), word));
      return additionalTaggedTokens;
    }
    return null;
  }

}
