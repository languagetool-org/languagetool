/* JLanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package de.danielnaber.languagetool.tagging.de;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.danielnaber.languagetool.tagging.Tagger;

/**
 * Experimental German tagger, only knows a few words for testing.
 * 
 * @author Daniel Naber
 */
public class GermanTagger implements Tagger {

  private static final String DATA_FILE = "rules/de/categories.txt";
  private Map word2cat = null;

  public GermanTagger() {  
  }

  private Map getWord2CategoryMapping() throws IOException {
    InputStreamReader isr = new InputStreamReader(new FileInputStream(DATA_FILE), "latin1");
    BufferedReader br = new BufferedReader(isr);
    String line;
    String fullform = null;
    Map mapping = new HashMap();
    while ((line = br.readLine()) != null) {
      line = line.trim();
      if (line.equals(""))
        continue;
      if (line.indexOf("wkl=VER") != -1 || line.indexOf("wkl=PA2") != -1)      // not yet used
        continue;
      if (line.startsWith("<form>")) {
        fullform = line.substring("<form>".length(), line.indexOf("</form>"));
      } else {
        line = line.replaceFirst("typ=[A-Z]{3}\\s+", "");       // FIXME: is it okay to remove it?
        //System.err.println(line);
        int startPos = line.indexOf("wkl=");
        int endPos =  line.indexOf("gen=");
        if (startPos != -1 && endPos != -1) {
          String cat = line.substring(startPos+4, endPos+7);
          cat = cat.replaceFirst("kas=", "");
          cat = cat.replaceFirst("num=", "");
          cat = cat.replaceFirst("gen=", "");
          //System.err.println(fullform  + "->" + cat);
          if (mapping.containsKey(fullform)) {
            List l = (List)mapping.get(fullform);
            l.add(cat);
            mapping.put(fullform, l);
          } else {
            List l = new ArrayList();
            l.add(cat);
            mapping.put(fullform, l);
          }
        } else {
          // TODO...
          //System.err.println("unknown format: " + line);
        }
      }
    }
    isr.close();
    br.close();
    return mapping;
  }

  public List tag(List tokens) throws IOException {
    if (word2cat == null)
      word2cat = getWord2CategoryMapping();
    List posTags = new ArrayList();
    for (Iterator iter = tokens.iterator(); iter.hasNext();) {
      String word = (String)iter.next();
      List l = (List)word2cat.get(word);
      if (l == null) {
        posTags.add(null);
      } else {
        posTags.add(l.toString());
      }
    }
    return posTags;
  }

}
