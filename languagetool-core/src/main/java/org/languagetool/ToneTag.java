/*
 * LanguageTool, a natural language style checker
 * Copyright (c) 2023.  Stefan Viol (https://stevio.de)
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  USA
 */

package org.languagetool;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ToneTag {

  clarity, formal, professional, confident, academic, povrem, scientific, objective, persuasive, informal, povadd, positive, NO_TONE_RULE, ALL_TONE_RULES;

  public static final List<ToneTag> REAL_TONE_TAGS = Arrays.stream(ToneTag.values()).filter(toneTag -> toneTag != NO_TONE_RULE && toneTag != ALL_TONE_RULES).collect(Collectors.toList());
}
