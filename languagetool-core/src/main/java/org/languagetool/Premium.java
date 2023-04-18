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
package org.languagetool;

import lombok.extern.slf4j.Slf4j;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.rules.Rule;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.*;

@Slf4j
/**
 * Information about premium-only rules.
 */
public abstract class Premium {
  
  private Optional<Properties> gitPremiumProps;

  public Premium() {
    try {
      InputStream in = JLanguageTool.getDataBroker().getAsStream("/git-premium.properties");
      if (in != null) {
        Properties props = new Properties();
        props.load(in);
        gitPremiumProps = Optional.of(props);
      } else {
        gitPremiumProps = Optional.empty();       
      }
    } catch (IOException e) {
      log.warn("Failed to read git-premium.properties file.", e);
    }
  }

  private static final List<String> tempNotPremiumRules = Arrays.asList();

  public static boolean isTempNotPremium(Rule rule) {
    return tempNotPremiumRules.contains(rule.getId());
  }
  
  public static boolean isPremiumStatusCheck(AnnotatedText text) {
    final String testRuleText = "languagetool testrule 8634756";
    final String testRuleText2 = "The languagetool testrule 8634756.";
    return testRuleText2.equals(text.getOriginalText()) || testRuleText.equals(text.getOriginalText());
  }

  public static Premium get() {
    String className = "org.languagetool.PremiumOn";
    try {
      Class<?> aClass = JLanguageTool.getClassBroker().forName(className);
      Constructor<?> constructor = aClass.getConstructor();
      return (Premium)constructor.newInstance();
    } catch (ClassNotFoundException e) {
      // 'PremiumOn' doesn't exist, thus this is the non-premium version
      return new PremiumOff();
    } catch (Exception e) {
      throw new RuntimeException("Object for class '" + className + "' could not be created", e);
    }
  }

  public static boolean isPremiumVersion() {
    String className = "org.languagetool.PremiumOn";
    try {
      Class<?> aClass = JLanguageTool.getClassBroker().forName(className);
      Constructor<?> constructor = aClass.getConstructor();
      constructor.newInstance();
      return true;
    } catch (ClassNotFoundException e) {
      // doesn't exist, thus this is the non-premium version
      return false;
    } catch (Exception e) {
      throw new RuntimeException("Object for class '" + className + "' could not be created", e);
    }
  }

  public abstract boolean isPremiumRule(Rule rule);
  
  public String getBuildDate() {
    return gitPremiumProps.map(properties -> properties.getProperty("git.build.time")).orElse(null);
  }
  
  public String getShortGitId() {
    return gitPremiumProps.map(properties -> properties.getProperty("git.commit.id.abbrev")).orElse(null);
  }
  
  public String getVersion() {
    return gitPremiumProps.map(properties -> properties.getProperty("git.build.version")).orElse(null);
  }

}
