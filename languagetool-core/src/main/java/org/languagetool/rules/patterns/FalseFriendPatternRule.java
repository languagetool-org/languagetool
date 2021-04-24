/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.patterns;

import org.languagetool.Language;
import org.languagetool.Tag;

import java.util.Collections;
import java.util.List;

/**
 * A pattern rule for finding false friends.
 */
public class FalseFriendPatternRule extends PatternRule {

  public FalseFriendPatternRule(String id, Language language, List<PatternToken> patternTokens, String description, String message, String shortMessage) {
    super(id, language, patternTokens, description, message, shortMessage);
    setTags(Collections.singletonList(Tag.picky));
  }

}
