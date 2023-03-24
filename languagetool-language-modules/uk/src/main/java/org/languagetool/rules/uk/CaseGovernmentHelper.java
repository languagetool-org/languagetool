package org.languagetool.rules.uk;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.tagging.uk.PosTagHelper;

public class CaseGovernmentHelper {

  public static final Map<String, Set<String>> CASE_GOVERNMENT_MAP = loadMap("/uk/case_government.txt");

  static {
    CASE_GOVERNMENT_MAP.put("згідно з", new HashSet<>(Arrays.asList("v_oru")));
  }
  

  
  private static Map<String, Set<String>> loadMap(String path) {
    Map<String, Set<String>> result = new HashMap<>();
    List<String> lines = JLanguageTool.getDataBroker().getFromResourceDirAsLines(path);
    for (String line : lines) {
      String[] parts = line.split(" ");
      String[] vidm = parts[1].split(":");
      
      if( result.containsKey(parts[0]) ) {
        result.get(parts[0]).addAll(Arrays.asList(vidm));
      }
      else {
        result.put(parts[0], new LinkedHashSet<>(Arrays.asList(vidm)));
      }
    }
    //        System.err.println("Found case governments: " + result.size());
    return result;
  }

  public static boolean hasCaseGovernment(AnalyzedTokenReadings analyzedTokenReadings, String rvCase) {
    return hasCaseGovernment(analyzedTokenReadings, null, rvCase);
  }
  
  public static boolean hasCaseGovernment(AnalyzedTokenReadings analyzedTokenReadings, Pattern startPosTag, String rvCase) {
    return getCaseGovernments(analyzedTokenReadings, startPosTag).contains(rvCase);
  /*
    // special case - only some inflections of мати
    if( LemmaHelper.hasLemma(analyzedTokenReadings, Arrays.asList("мати"), Pattern.compile("verb:imperf:(futr|past|pres).*")) 
        && rvCase.equals("v_inf")  )
      return true;
    if( LemmaHelper.hasLemma(analyzedTokenReadings, Arrays.asList("бути"), Pattern.compile("verb:imperf:(futr).*")) 
        && rvCase.equals("v_inf")  )
      return true;
    
    for(AnalyzedToken token: analyzedTokenReadings.getReadings()) {
      if( token.getPOSTag() == null )
        continue;
      if( startPosTag != null && ! startPosTag.matcher(token.getPOSTag()).matches() )
        continue;
      
      if( rvCase.equals("v_oru") && PosTagHelper.hasPosTagPart(token, "adjp:pasv") )
        return true;
      
      if( CASE_GOVERNMENT_MAP.containsKey(token.getLemma()) 
            && CASE_GOVERNMENT_MAP.get(token.getLemma()).contains(rvCase) ) {
          return true;
      }

      // TODO: more universal advp -> verb conversion
      if( token.getPOSTag().startsWith("advp") ) {
        String vLemma = getAdvpVerbLemma(token);
        
        if( CASE_GOVERNMENT_MAP.containsKey(vLemma)
            && CASE_GOVERNMENT_MAP.get(vLemma).contains(rvCase) )
          return true;
      }
      
    }
    return false;
    */
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

  public static Set<String> getCaseGovernments(AnalyzedTokenReadings analyzedTokenReadings, Pattern posTag) {
    LinkedHashSet<String> list = new LinkedHashSet<>();

    // special case - only some inflections of мати
    if( LemmaHelper.hasLemma(analyzedTokenReadings, Arrays.asList("мати"), Pattern.compile("verb:imperf:(futr|past|pres).*")) ) {
      list.add("v_inf");
    }
    else if( LemmaHelper.hasLemma(analyzedTokenReadings, Arrays.asList("бути"), Pattern.compile("verb:imperf:futr.*")) ) {
      list.add("v_inf");
    }
    else if( LemmaHelper.hasLemma(analyzedTokenReadings, Arrays.asList(
        "вимагатися", "випадати", "випасти", "личити", "належати", "тягнути", "щастити",
        "плануватися", "рекомендуватися", "пропонуватися", "сподобатися", "плануватися", "прийтися",
        "удатися", "годитися", "доводитися"), 
        Pattern.compile("verb(:rev)?:(im)?perf:(pres:s:3|futr:s:3|past:n).*")) ) {
      list.add("v_inf");
    }
    else if( LemmaHelper.hasLemma(analyzedTokenReadings, Arrays.asList("належить"), 
        Pattern.compile("verb:imperf:inf.*")) ) {
      list.add("v_inf");
    }
    
    for(AnalyzedToken token: analyzedTokenReadings.getReadings()) {
      if( token.hasNoTag() )
        continue;
        
      if( posTag == null ||
           (token.getPOSTag() != null && posTag.matcher(token.getPOSTag()).matches()) ) {

        String vLemma = token.getLemma();
        if ( ! CASE_GOVERNMENT_MAP.containsKey(vLemma) ) {
          if( token.getPOSTag() != null && token.getPOSTag().startsWith("advp") ) {
            vLemma = getAdvpVerbLemma(token);
          }
        }

        if ( CASE_GOVERNMENT_MAP.containsKey(vLemma) ) {
          Set<String> rvList = CASE_GOVERNMENT_MAP.get(vLemma);
          list.addAll(rvList);
        }
      }

      if( PosTagHelper.hasPosTagPart(token, "adjp:pasv") ) {
        list.add("v_oru");
      }
    }

    return list;
  }

  private static String getAdvpVerbLemma(AnalyzedToken token) {
    String vLemma = token.getLemma();
    if( vLemma.equals("даючи") ) {
      vLemma = "давати";
    }
    else if( vLemma.equals("змушуючи") ) {
      vLemma = "змушувати";
    }
    else {
      vLemma = token.getLemma()
        .replaceFirst("лячи(с[яь])?", "ити$1")
        .replaceFirst("(ючи|вши)(с[яь])?", "ти$2");
    }
   return vLemma; 
  }

  static final String USED_U_INSTEAD_OF_A_MSG = ". Можливо, вжито невнормований родовий відмінок ч.р. з закінченням -у/-ю замість -а/-я (така тенденція є в сучасній мові)?";
  
}
