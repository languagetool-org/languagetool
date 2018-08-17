/* LanguageTool, a natural language style checker
 * Copyright (C) 2018 Oleg Serikov
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
package org.languagetool.languagemodel;

import org.languagetool.rules.ngrams.Probability;

import java.util.List;

/**
 * Produces zero probability for any passed text.
 */
public class MockLanguageModel implements LanguageModel {
  @Override
  public Probability getPseudoProbability(List<String> context) {
    return new Probability(0, 0);
  }

  @Override
  public void close() {}
}
