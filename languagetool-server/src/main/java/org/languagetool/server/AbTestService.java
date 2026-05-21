package org.languagetool.server;

import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.UserConfig;

import java.util.List;
import java.util.Map;

public interface AbTestService {
  @Nullable
  List<String> getActiveAbTestForClient(Map<String, String> params, HTTPServerConfig config);

  /**
   * This allows reconfiguring JLanguageTool instances based on the abtest parameter
   *
   * @param abTest     the enabled treatments
   * @param lt         the JLanguageTool instance to be reconfigured
   * @param lang
   * @param params
   * @param userConfig
   */
  default void configureLTForTreatment(List<String> abTest, JLanguageTool lt, Language lang, TextChecker.QueryParams params, UserConfig userConfig) {
  }
}
