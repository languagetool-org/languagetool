/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2018 Fabian Richter
 *  *
 *  * This library is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU Lesser General Public
 *  * License as published by the Free Software Foundation; either
 *  * version 2.1 of the License, or (at your option) any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this library; if not, write to the Free Software
 *  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  * USA
 *
 */

package org.languagetool.rules.spelling.suggestions;

import org.languagetool.rules.SuggestedReplacement;

import java.util.List;

/**
 * Implementing classes must provide confidence values with the SuggestedReplacement objects returned by orderSuggestions
 */
public interface SuggestionsRanker extends SuggestionsOrderer {
  /**
   * Model output should have been calibrated using a precision-recall curve evaluation, so that
   * a threshold for confidence values with sufficiently high precision for auto correction is known
   * @param rankedSuggestions suggestions returned by orderSuggestions
   * @return if confidence is high enough for auto correction
   */
  boolean shouldAutoCorrect(List<SuggestedReplacement> rankedSuggestions);
}
