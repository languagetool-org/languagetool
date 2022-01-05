/*
 * LanguageTool, a natural language style checker
 * Copyright (C) 2021 Jaume Ortolà
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
package org.languagetool.rules.es;

import java.io.IOException;
import org.languagetool.language.Spanish;
import org.languagetool.rules.AbstractSuppressMisspelledSuggestionsFilter;

public class SpanishSuppressMisspelledSuggestionsFilter extends AbstractSuppressMisspelledSuggestionsFilter {

  public SpanishSuppressMisspelledSuggestionsFilter() throws IOException {
    super(new Spanish());
  }

}
