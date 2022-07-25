/*
 * LanguageTool, a natural language style checker 
 * Copyright (c) 2022.  Stefan Viol (https://stevio.de)
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

package org.languagetool.rules.patterns;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

public class FailingDemoPatternRuleTest extends PatternRuleTest{
  
  @Test
  public void testRulesWithErrors1() throws IOException {
    runTestForLanguage(new DemoPatternRuleTest.DemoWithGrammarFile("grammar-fail1.xml"));
  }

  @Test
  public void testRulesWithErrors2() throws IOException {
    runTestForLanguage(new DemoPatternRuleTest.DemoWithGrammarFile("grammar-fail2.xml"));
  }

  @Override
  protected PatternRuleErrorCollector createPatternRuleErrorCollector() {
    return new PatternRuleErrorCollector(true);
  }

  @Override
  @Test
  @Ignore
  public void testSupportsLanguage() {
  }

  @Override
  @Test
  @Ignore
  public void shortMessageIsLongerThanErrorMessage() throws IOException {
  }
}
