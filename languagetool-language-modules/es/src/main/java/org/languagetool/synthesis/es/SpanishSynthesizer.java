/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.synthesis.es;

import org.languagetool.Language;
import org.languagetool.synthesis.BaseSynthesizer;

/**
 * Spanish word form synthesizer.
 *
 * Based on Dutch word from synthesizer
 *
 * @author Juan Martorell
 */
public class SpanishSynthesizer extends BaseSynthesizer {

  private static final String RESOURCE_FILENAME = "/es/es-ES_synth.dict";
  private static final String TAGS_FILE_NAME = "/es/es-ES_tags.txt";

  public SpanishSynthesizer(Language lang) {
    super(RESOURCE_FILENAME, TAGS_FILE_NAME, lang);
  }
  
}