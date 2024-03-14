package org.languagetool.server.tools;

import org.languagetool.server.HTTPServerConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class LocalAbTestService implements AbTestService {

  @Override
  public List<String> getActiveAbTestForClient(Map<String, String> params, HTTPServerConfig config) {
    List<String> abTest = null;
    String agent = params.getOrDefault("useragent", "unknown");
    String paramActivatedAbTest = params.get("abtest");
    Pattern abTestClients = config.getAbTestClients();
    if (paramActivatedAbTest != null && abTestClients != null && abTestClients.matcher(agent).matches()) {
      String[] abParams = paramActivatedAbTest.trim().split(",");
      List<String> tmpAb = new ArrayList<>();
      for (String abParam : abParams) {
        if (config.getAbTest().contains(abParam)) {
          tmpAb.add(abParam.trim());
        }
      }
      if (!tmpAb.isEmpty()) {
        abTest = Collections.unmodifiableList(tmpAb);
      }
    }
    return abTest;
  }
}
