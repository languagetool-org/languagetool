package org.languagetool.rules.uk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
    
    Map<String, Set<String>> DERIVATIVES_MAP = loadMap("/uk/derivats.txt");
    for(Entry<String, Set<String>> entry: DERIVATIVES_MAP.entrySet()) {
      HashSet<String> set = new HashSet<>();
      CASE_GOVERNMENT_MAP.put(entry.getKey(), set);
      for(String verb: entry.getValue()) {
        Set<String> rvs = CASE_GOVERNMENT_MAP.get(verb);
        if( rvs != null ) {
          set.addAll(rvs);
        }
      }
    }
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
  }

  public static Set<String> getCaseGovernments(AnalyzedTokenReadings analyzedTokenReadings, String startPosTag) {
    if( "verb".equals(startPosTag) && PosTagHelper.hasPosTagStart(analyzedTokenReadings.getReadings().get(0), "advp") ) {
      startPosTag = "advp";
    }
    
    LinkedHashSet<String> list = new LinkedHashSet<>();
    
    list.addAll(getCustomGovs(analyzedTokenReadings));

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
//    if( "verb".equals(startPosTag) && PosTagHelper.hasPosTagStart(analyzedTokenReadings.getReadings().get(0), "advp") ) {
//      startPosTag = "advp";
//    }

    LinkedHashSet<String> list = new LinkedHashSet<>();

    list.addAll(getCustomGovs(analyzedTokenReadings));
    
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

  private static ArrayList<String> getCustomGovs(AnalyzedTokenReadings analyzedTokenReadings) {
    ArrayList<String> list = new ArrayList<>();
    // special case - only some inflections of the verbs
    if( LemmaHelper.hasLemma(analyzedTokenReadings, Arrays.asList("мати"), Pattern.compile("verb:imperf:(futr|past|pres).*")) ) {
      list.add("v_inf");
    } // є, буде, було
    else if( LemmaHelper.hasLemma(analyzedTokenReadings, Arrays.asList("бути"), Pattern.compile("verb:imperf:(futr|past:n|pres:s:3).*")) ) {
      list.add("v_inf");
    }
    else if( LemmaHelper.hasLemma(analyzedTokenReadings, Arrays.asList(
        "вимагатися", "випадати", "випасти", "личити", "належати", "тягнути", "щастити",
        "плануватися", "рекомендуватися", "пропонуватися", "сподобатися", "плануватися", "прийтися",
        "удатися", "годитися", "доводитися"), 
        Pattern.compile("verb.*(pres:s:3|futr:s:3|past:n).*")) ) {
      list.add("v_inf");
    }
    else if( LemmaHelper.hasLemma(analyzedTokenReadings, Arrays.asList("належить"), 
        Pattern.compile("verb:imperf:inf.*")) ) {
      list.add("v_inf");
    }
    else if( LemmaHelper.hasLemma(analyzedTokenReadings, Pattern.compile("(по)?більшати|(по)?меншати"), 
        Pattern.compile("verb.*(inf|pres:s:3|futr:s:3|past:n).*")) ) {
      list.add("v_rod");
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
