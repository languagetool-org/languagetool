package org.languagetool.rules.uk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedToken;

class VerbInflectionHelper {

  private static final Pattern VERB_INFLECTION_PATTERN = Pattern.compile(":([mfnps])(:([123])?|$)");
  private static final Pattern NOUN_INFLECTION_PATTERN = Pattern.compile("(?::((?:[iu]n)?anim))?:([mfnps]):(v_naz)");
  private static final Pattern ADJ_INFLECTION_PATTERN = Pattern.compile("(adj|numr):([mfnps]):(v_naz)");
  private static final Pattern NOUN_PERSON_PATTERN = Pattern.compile(":([123])");

  static List<VerbInflectionHelper.Inflection> getVerbInflections(List<AnalyzedToken> nounTokenReadings) {
    List<VerbInflectionHelper.Inflection> verbGenders = new ArrayList<>();
    for (AnalyzedToken token: nounTokenReadings) {
      String posTag = token.getPOSTag();

      if( posTag == null || ! posTag.startsWith("verb") )
        continue;

      if( posTag.contains(":inf") ) {
        verbGenders.add(new VerbInflectionHelper.Inflection("i", null));
        continue;
      }

      if( posTag.contains(":impers") ) {
        verbGenders.add(new VerbInflectionHelper.Inflection("o", null));
        continue;
      }

      Matcher matcher = VERB_INFLECTION_PATTERN.matcher(posTag);
      matcher.find();

      String gen = matcher.group(1);
      String person = matcher.group(3);

      verbGenders.add(new VerbInflectionHelper.Inflection(gen, person));
    }
//    System.err.println("verbInfl: " + verbGenders);
    return verbGenders;
  }


  static List<VerbInflectionHelper.Inflection> getNounInflections(List<AnalyzedToken> nounTokenReadings) {
    List<VerbInflectionHelper.Inflection> slaveInflections = new ArrayList<>();
    for (AnalyzedToken token: nounTokenReadings) {
      String posTag2 = token.getPOSTag();
      if( posTag2 == null )
        continue;

      Matcher matcher = NOUN_INFLECTION_PATTERN.matcher(posTag2);
      if( ! matcher.find() ) {
        //        System.err.println("Failed to find slave inflection tag in " + posTag2 + " for " + nounTokenReadings);
        continue;
      }
      String gen = matcher.group(2);
      
      Matcher matcherPerson = NOUN_PERSON_PATTERN.matcher(posTag2);
      String person = matcherPerson.find() ? matcherPerson.group(1) : null;
      
      slaveInflections.add(new VerbInflectionHelper.Inflection(gen, person));
    }
//    System.err.println("nounInfl: " + slaveInflections);
    return slaveInflections;
  }

  static List<VerbInflectionHelper.Inflection> getAdjInflections(List<AnalyzedToken> nounTokenReadings) {
    List<VerbInflectionHelper.Inflection> slaveInflections = new ArrayList<>();
    for (AnalyzedToken token: nounTokenReadings) {
      String posTag2 = token.getPOSTag();
      if( posTag2 == null )
        continue;

      Matcher matcher = ADJ_INFLECTION_PATTERN.matcher(posTag2);
      if( ! matcher.find() ) {
        //        System.err.println("Failed to find slave inflection tag in " + posTag2 + " for " + nounTokenReadings);
        continue;
      }
      String gen = matcher.group(2);
      
      Matcher matcherPerson = NOUN_PERSON_PATTERN.matcher(posTag2);
      String person = matcherPerson.find() ? matcherPerson.group(1) : null;
      
      slaveInflections.add(new VerbInflectionHelper.Inflection(gen, person));
    }
    return slaveInflections;
  }

  static boolean inflectionsOverlap(List<AnalyzedToken> verbTokenReadings, List<AnalyzedToken> nounTokenReadings) {
    return ! Collections.disjoint(
      getVerbInflections(verbTokenReadings), getNounInflections(nounTokenReadings)
    );
  }

  static class Inflection implements Comparable<Inflection> {
      final String gender;
      final String plural;
      final String person;
  
      Inflection(String gender, String person) {
        if( gender.equals("s") || gender.equals("p") ) {
          this.gender = null;
          this.plural = gender;
        }
        else if( gender.equals("i") ) {
          this.gender = gender;
          this.plural = gender;
        }
        else {
          this.gender = gender;
          this.plural = "s";
        }
        this.person = person;
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
  
        if( person != null && other.person != null ) {
          if( ! person.equals(other.person) )
            return false;
        }
        
        if( gender != null && other.gender != null ) {
  
          // infinitive matches all for now, otherwise too many false positives
          // e.g. чи могла вона програти
  //        if( gender.equals("i") || other.gender.equals("i") )
  //          return true;
  
          if( ! gender.equals(other.gender) )
            return false;
        }
  
        return plural.equals(other.plural);
      }
  
      @Override
      public int hashCode() {
          final int prime = 31;
          int result = 1;
          result = prime * result + ((gender == null) ? 0 : gender.hashCode());
          result = prime * result + ((plural == null) ? 0 : plural.hashCode());
          result = prime * result + ((person == null) ? 0 : person.hashCode());
          return result;
      }
  
  
      @Override
      public String toString() {
          return "Gender: " + gender + "/" + plural + "/" + person;
      }
  
      @Override
      public int compareTo(Inflection o) {
        Integer thisOrder = gender != null ? InflectionHelper.GEN_ORDER.get(gender) : 0;
        Integer otherOrder = o.gender != null ? InflectionHelper.GEN_ORDER.get(o.gender) : 0;
        
        int compared = thisOrder.compareTo(otherOrder);
  //      if( compared != 0 )
          return compared;
        
  //      compared = VIDM_ORDER.get(_case).compareTo(VIDM_ORDER.get(o._case));
  //      return compared;
      }
    
  
    }

}
