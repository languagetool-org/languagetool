package org.languagetool.rules.ca;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class VerbsHelper {

  private static final List<String> lVerbsDicendi = Arrays.asList("amollar", "afluixar", "entaferrar", "espletar",
    "soltar", "engaltar", "acceptar", "aclarir", "aconsellar", "acusar", "adduir", "admetre", "advertir", "afegir",
    "afirmar", "agregar", "al·legar", "al·ludir", "amenaçar", "amonestar", "ampliar", "anunciar", "apuntar",
    "argumentar", "assegurar", "assentir", "assenyalar", "atorgar", "atribuir", "avançar", "avisar", "barbotejar",
    "bordar", "bramar", "calcular", "callar", "citar", "comentar", "concedir", "concloure", "concretar", "confessar",
    "confiar", "confirmar", "considerar", "contestar", "creure", "cridar", "culpar", "decidir", "declamar", "declarar"
    , "decretar", "defensar", "definir", "delimitar", "demanar", "descobrir", "descriure", "desitjar", "desmentir",
    "destacar", "desvelar", "detallar", "determinar", "dir", "dogmatitzar", "dubtar", "elogiar", "emfasitzar",
    "emfatitzar", "engegar", "enumerar", "esclafir", "escopir", "escridassar", "esgrimir", "esmentar", "especificar",
    "establir", "etzibar", "exclamar", "exigir", "explicar", "exposar", "expressar", "formular", "garantir", "gemegar"
    , "imaginar", "implorar", "imputar", "increpar", "indicar", "informar", "inquirir", "insinuar", "insistir",
    "insultar", "interrogar", "intervenir", "ironitzar", "jurar", "justificar", "lamentar", "lladrar", "lloar",
    "maleir", "manar", "manifestar", "matisar", "mentir", "mostrar", "murmurar", "negar", "observar", "oferir",
    "opinar", "ordenar", "pensar", "plantejar", "pontificar", "pregar", "preguntar", "presumir", "preveure",
    "prometre", "proposar", "protestar", "puntualitzar", "quequejar", "ratificar", "reafirmar", "rebutjar", "recalcar"
    , "recitar", "reclamar", "recomanar", "reconèixer", "referir", "refermar", "reflexionar", "refusar", "refutar",
    "relatar", "remarcar", "rematar", "remugar", "renegar", "renyar", "repetir", "replicar", "reprendre", "resar",
    "respondre", "retreure", "revelar", "sol·licitar", "somicar", "sospirar", "sospitar", "sostenir", "subratllar",
    "suggerir", "suposar", "xisclar", "xiuxiuejar", "trobar");

  private static final Pattern pKeepLooking = Pattern.compile("V.*|RG.*|LOC_ADV");

  public static boolean isVerbDicendiBefore(AnalyzedTokenReadings[] tokens, int i) {
    AnalyzedToken reading;
    while (i > 0 && i < tokens.length && (reading = tokens[i].readingWithTagRegex(pKeepLooking)) != null) {
      if (lVerbsDicendi.contains(reading.getLemma().toLowerCase())) {
        return true;
      }
      i--;
    }
    return false;
  }
}
