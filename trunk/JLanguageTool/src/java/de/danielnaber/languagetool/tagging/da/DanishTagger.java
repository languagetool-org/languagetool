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
package de.danielnaber.languagetool.tagging.da;

import java.util.Locale;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.tagging.BaseTagger;

/**
 * Danish Part-of-speech tagger.
 *
 * Based on the spellcheck dictionary Stavekontrolden
 * published by Foreningen for frit tilgængelige sprogværktøjer
 * under the terms of the GNU LGPL version 2.1 and Mozilla MPL version 1.1.
 *
 * www.stavekontrolden.dk
 *
 * Stavekontrolden is based on data from Det Danske Sprog- og Litteraturselskab
 * (The Danish Society for Language and Literature), http://www.dsl.dk.
 * 
 * @author Esben Aaberg
 */
public class DanishTagger extends BaseTagger {

  @Override
  public final String getFileName() {
    return JLanguageTool.getDataBroker().getResourceDir() + "/da/danish.dict";    
  }
  
  public DanishTagger() {
    super();
    setLocale(new Locale("da"));
  }
}
