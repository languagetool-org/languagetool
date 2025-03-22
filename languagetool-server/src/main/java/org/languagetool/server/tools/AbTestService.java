package org.languagetool.server.tools;

import org.jetbrains.annotations.Nullable;
import org.languagetool.server.HTTPServerConfig;

import java.util.List;
import java.util.Map;

public interface AbTestService {
  @Nullable
  List<String> getActiveAbTestForClient(Map<String, String> params, HTTPServerConfig config);
}
