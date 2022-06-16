package org.languagetool.rules.uk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedToken;

/**
 * @since 3.6
 */
public class InflectionHelper {

  private InflectionHelper() {
  }

  public static class Inflection implements Comparable<Inflection> {
    final String gender;
    final String _case;
    final String animTag;
  
    Inflection(String gender, String _case, String animTag) {
      this.gender = gender;
      this._case = _case;
      this.animTag = animTag;
    }
  
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((_case == null) ? 0 : _case.hashCode());
//      result = prime * result + ((animTag == null) ? 0 : animTag.hashCode());
      result = prime * result + ((gender == null) ? 0 : gender.hashCode());
      return result;
    }
  
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
  
      Inflection other = (Inflection) obj;
      return genderEquals(gender, other.gender)
          && _case.equals(other._case)
          && (animTag == null || other.animTag == null 
          || ! animMatters() || ! other.isAnimalSensitive() || animTag.equals(other.animTag));
    }
  
    private boolean genderEquals(String gender1, String gender2) {
      if( gender1.equals(gender2) )
        return true;
      
      if( gender1.equals("s") && gender2.matches("[mfn]") 
          || gender2.equals("s") && gender1.matches("[mfn]") )
        return true;

      return false;
    }

    public boolean equalsIgnoreGender(Inflection other) {
      return //gender.equals(other.gender)
          _case.equals(other._case)
          && (animTag == null || other.animTag == null 
          || ! animMatters() || animTag.equals(other.animTag));
    }
  
    boolean animMatters() {
      return animTag != null && ! "unanim".equals(animTag) && _case.equals("v_zna") && isAnimalSensitive();
    }
  
    private boolean isAnimalSensitive() {
      return "mp".contains(gender);
    }
  
    @Override
    public String toString() {
      return ":" + gender + ":" + _case
          + (animMatters() ? "_"+animTag : "");
    }

    @Override
    public int compareTo(Inflection o) {
      if( GEN_ORDER.get(gender) == null ) System.err.println ("unknown gender for " + gender + " for " + o);
      
      int compared = GEN_ORDER.get(gender).compareTo(GEN_ORDER.get(o.gender));
      if( compared != 0 )
        return compared;
      
      compared = VIDM_ORDER.get(_case).compareTo(VIDM_ORDER.get(o._case));
      return compared;
    }
  
  }

  public static List<Inflection> getAdjInflections(List<AnalyzedToken> adjTokenReadings) {
    return getAdjInflections(adjTokenReadings, "adj");
  }

  public static List<Inflection> getNumrInflections(List<AnalyzedToken> adjTokenReadings) {
    return getAdjInflections(adjTokenReadings, "numr");
  }

  public static List<Inflection> getAdjInflections(List<AnalyzedToken> adjTokenReadings, String postagStart) {
    List<Inflection> masterInflections = new ArrayList<>();
    for (AnalyzedToken token: adjTokenReadings) {
      String posTag = token.getPOSTag();
  
      if( posTag == null || ! posTag.startsWith(postagStart) )
        continue;
  
      Matcher matcher = TokenAgreementAdjNounRule.ADJ_INFLECTION_PATTERN.matcher(posTag);
      matcher.find();
  
      String gen = matcher.group(1);
      String vidm = matcher.group(2);
      String animTag = null;
      if (matcher.group(3) != null) {
        animTag = matcher.group(3).substring(2);	// :rinanim/:ranim
      }
  
      Inflection inflection = new Inflection(gen, vidm, animTag);
      if( ! masterInflections.contains(inflection) ) {
        masterInflections.add(inflection);
      }
    }
    return masterInflections;
  }

  static List<Inflection> getNounInflections(List<AnalyzedToken> nounTokenReadings) {
    return getNounInflections(nounTokenReadings, null);
  }

  public static List<Inflection> getNounInflections(List<AnalyzedToken> nounTokenReadings, Pattern ignoreTag) {
    List<Inflection> slaveInflections = new ArrayList<>();
    for (AnalyzedToken token: nounTokenReadings) {
      String posTag2 = token.getPOSTag();
      if( posTag2 == null )
        continue;

      if( ignoreTag != null && ignoreTag.matcher(posTag2).find() ) {
        continue;
      }

      Matcher matcher = TokenAgreementAdjNounRule.NOUN_INFLECTION_PATTERN.matcher(posTag2);
      if( ! matcher.find() ) {
        //  			System.err.println("Failed to find slave inflection tag in " + posTag2 + " for " + nounTokenReadings);
        continue;
      }
      String gen = matcher.group(2);
      String vidm = matcher.group(3);
      String animTag = matcher.group(1);

      Inflection inflection = new Inflection(gen, vidm, animTag);
      if( ! slaveInflections.contains(inflection) ) {
        slaveInflections.add(inflection);
      }
    }
    return slaveInflections;
  }

  static final Map<String,Integer> GEN_ORDER = new HashMap<>();
  private static final Map<String,Integer> VIDM_ORDER = new HashMap<>();
  
  static {
    GEN_ORDER.put("m", 0);
    GEN_ORDER.put("f", 1);
    GEN_ORDER.put("n", 3);
    GEN_ORDER.put("s", 4);      // for pron
    GEN_ORDER.put("p", 5);
    GEN_ORDER.put("i", 6);      // verb:inf
    GEN_ORDER.put("o", 7);      // verb:impers

    VIDM_ORDER.put("v_naz", 10);
    VIDM_ORDER.put("v_rod", 20);
    VIDM_ORDER.put("v_dav", 30);
    VIDM_ORDER.put("v_zna", 40);
    VIDM_ORDER.put("v_oru", 50);
    VIDM_ORDER.put("v_mis", 60);
    VIDM_ORDER.put("v_kly", 70);
  }

}
