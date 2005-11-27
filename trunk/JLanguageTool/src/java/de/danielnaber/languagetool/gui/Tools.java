/*
 * Created on 27.11.2005
 */
package de.danielnaber.languagetool.gui;

public class Tools {

  private static final int DEFAULT_CONTEXT_SIZE = 25;

  public static String getContext(int fromPos, int toPos, String text) {
    return getContext(fromPos, toPos, text, DEFAULT_CONTEXT_SIZE);
  }
  
  public static String getContext(int fromPos, int toPos, String fileContents, int contextSize) {
    fileContents = fileContents.replaceAll("\n", " ");
    // calculate context region:
    int startContent = fromPos - contextSize;
    String prefix = "...";
    String postfix = "...";
    String markerPrefix = "   ";
    if (startContent < 0) {
      prefix = "";
      markerPrefix = "";
      startContent = 0;
    }
    int endContent = toPos + contextSize;
    if (endContent > fileContents.length()) {
      postfix = "";
      endContent = fileContents.length();
    }
    // make "^" marker. inefficient but robust implementation:
    StringBuffer marker = new StringBuffer();
    for (int i = 0; i < fileContents.length() + prefix.length(); i++) {
      if (i >= fromPos && i < toPos)
        marker.append("^");
      else
        marker.append(" ");
    }
    // now build context string plus marker:
    StringBuffer sb = new StringBuffer();
    sb.append(prefix);
    sb.append(fileContents.substring(startContent, endContent));
    sb.append(postfix);
    String markerStr = markerPrefix + marker.substring(startContent, endContent);
    int startMark = markerStr.indexOf("^");
    int endMark = markerStr.lastIndexOf("^");
    String result = sb.toString();
    result = result.substring(0, startMark) + "<b><font color=\"red\">" + 
      result.substring(startMark, endMark+1) + "</font></b>" + result.substring(endMark+1);
    //String result = sb.s
    return result;
  }

}
