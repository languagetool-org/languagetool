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
package de.danielnaber.languagetool.tagging.br;

import java.util.Locale;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.tagging.BaseTagger;

/** Breton Tagger.
 * 
 * Based on Breton diction diction from apertium:
 *
 *  Copyright (C) 2008--2010 Francis Tyers <ftyers@prompsit.com>
 *  Copyright (C) 2009--2010 Fulup Jakez <fulup.jakez@ofis-bzh.org>
 *  Copyright (C) 2009       Gwenvael Jekel <jequelg@yahoo.fr>
 *  Development supported by:
 *  * Prompsit Language Engineering, S. L.
 *  * Ofis ar Brezhoneg
 *  * Grup Transducens, Universitat d'Alacant
 *
 * Implemented in FSA.
 * 
 * @author Dominique Pelle
 */
public class BretonTagger extends BaseTagger {

  @Override
  public final String getFileName() {
    return JLanguageTool.getDataBroker().getResourceDir() + "/br/breton.dict";    
  }
  
  public BretonTagger() {
    super();
    setLocale(new Locale("br"));
  }
}
