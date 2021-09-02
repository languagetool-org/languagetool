/* LanguageTool, a natural language style checker 
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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

package org.languagetool.rules.fr;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.rules.AbstractMakeContractionsFilter;

public class MakeContractionsFilter extends AbstractMakeContractionsFilter {
  
  private static final Pattern DE_LE = Pattern.compile("\\bde le\\b", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern A_LE = Pattern.compile("\\bà le\\b", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern DE_LES = Pattern.compile("\\bde les\\b", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern A_LES = Pattern.compile("\\bà les\\b", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

  protected String fixContractions(String suggestion) {
    Matcher matcher = DE_LE.matcher(suggestion);
    suggestion = matcher.replaceAll("du");
    matcher = A_LE.matcher(suggestion);
    suggestion = matcher.replaceAll("au");
    matcher = DE_LES.matcher(suggestion);
    suggestion = matcher.replaceAll("des");
    matcher = A_LES.matcher(suggestion);
    suggestion = matcher.replaceAll("aux");
    return suggestion;
  }

}