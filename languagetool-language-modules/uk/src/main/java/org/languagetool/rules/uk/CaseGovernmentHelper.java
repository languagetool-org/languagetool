package org.languagetool.rules.uk;

import java.util.*;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.tagging.uk.PosTagHelper;

public class CaseGovernmentHelper {

  static final Map<String, Set<String>> CASE_GOVERNMENT_MAP = loadMap("/uk/case_government.txt");

  static {
    CASE_GOVERNMENT_MAP.put("згідно з", new HashSet<>(Arrays.asList("v_oru")));
  }
  

  
  private static Map<String, Set<String>> loadMap(String path) {
    Map<String, Set<String>> result = new HashMap<>();
    List<String> lines = JLanguageTool.getDataBroker().getFromResourceDirAsLines(path);
    for (String line : lines) {
      String[] parts = line.split(" ");
      String[] vidm = parts[1].split(":");
      result.put(parts[0], new LinkedHashSet<>(Arrays.asList(vidm)));
    }
    //        System.err.println("Found case governments: " + result.size());
    return result;
  }

  public static boolean hasCaseGovernment(AnalyzedTokenReadings analyzedTokenReadings, String rvCase) {
    for(AnalyzedToken token: analyzedTokenReadings.getReadings()) {
      if( rvCase.equals("v_oru") && PosTagHelper.hasPosTagPart(token, "adjp:pasv") )
        return true;
      
      if( CASE_GOVERNMENT_MAP.containsKey(token.getLemma())
          && CASE_GOVERNMENT_MAP.get(token.getLemma()).contains(rvCase) )
        return true;
    }
    return false;
  }

  public static Set<String> getCaseGovernments(AnalyzedTokenReadings analyzedTokenReadings, String startPosTag) {
    LinkedHashSet<String> list = new LinkedHashSet<>();
    for(AnalyzedToken token: analyzedTokenReadings.getReadings()) {
      if( ! token.hasNoTag()
          && (token.getPOSTag() != null && token.getPOSTag().startsWith(startPosTag) 
              || (startPosTag.equals("prep") && token.getPOSTag() != null && token.getPOSTag().equals("<prep>")) )
          && CASE_GOVERNMENT_MAP.containsKey(token.getLemma()) ) {

        Set<String> rvList = CASE_GOVERNMENT_MAP.get(token.getLemma());
        list.addAll(rvList);

        if( token.getPOSTag().contains("adjp:pasv") ) {
          rvList.add("v_oru");
        }
      }
    }
    return list;
  }
  
}
