package org.languagetool.rules.patterns;

import org.languagetool.rules.IncorrectExample;
import org.languagetool.tools.StringTools;

/**
 * Serializes a PatternRule object to XML.
 *
 * @since 1.8
 */
class PatternRuleXmlCreator {

  /**
   * Return the pattern as an XML string. FIXME: this is not complete, information might be lost!
   */
  public final String toXML(PatternRule rule) {
    final StringBuilder sb = new StringBuilder();
    sb.append("<rule id=\"");
    sb.append(StringTools.escapeXML(rule.getId()));
    sb.append("\" name=\"");
    sb.append(StringTools.escapeXML(rule.getDescription()));
    sb.append("\">\n");
    sb.append("<pattern mark_from=\"");
    sb.append(rule.startPositionCorrection);
    sb.append("\" mark_to=\"");
    sb.append(rule.endPositionCorrection);
    sb.append('"');
    // for now, case sensitivity is per pattern, not per element,
    // so just use the setting of the first element:
    if (!rule.patternElements.isEmpty() && rule.patternElements.get(0).getCaseSensitive()) {
      sb.append(" case_sensitive=\"yes\"");
    }
    sb.append(">\n");
    for (Element patternElement : rule.patternElements) {
      sb.append("<token");
      if (patternElement.getNegation()) {
        sb.append(" negate=\"yes\"");
      }
      if (patternElement.isRegularExpression()) {
        sb.append(" regexp=\"yes\"");
      }
      if (patternElement.getPOStag() != null) {
        sb.append(" postag=\"");
        sb.append(patternElement.getPOStag());
        sb.append('"');
      }
      if (patternElement.getPOSNegation()) {
        sb.append(" negate_pos=\"yes\"");
      }
      if (patternElement.isInflected()) {
        sb.append(" inflected=\"yes\"");
      }
      sb.append('>');
      if (patternElement.getString() != null) {
        sb.append(StringTools.escapeXML(patternElement.getString()));
      } else {
        // TODO
      }
      sb.append("</token>\n");
    }
    sb.append("</pattern>\n");
    sb.append("<message>");
    sb.append(StringTools.escapeXML(rule.getMessage()));
    sb.append("</message>\n");
    if (rule.getIncorrectExamples() != null) {
      for (IncorrectExample example : rule.getIncorrectExamples()) {
        sb.append("<example type=\"incorrect\">");
        sb.append(StringTools.escapeXML(example.getExample()));
        sb.append("</example>\n");
      }
    }
    if (rule.getCorrectExamples() != null) {
      for (String example : rule.getCorrectExamples()) {
        sb.append("<example type=\"correct\">");
        sb.append(StringTools.escapeXML(example));
        sb.append("</example>\n");
      }
    }
    sb.append("</rule>");
    return sb.toString();
  }

}
