/*
 * Created on 06.05.2007
 */
package de.danielnaber.languagetool.dev;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.dawidweiss.fsa.FSA;

/**
 * Export German nouns as a serialized Java HashSet, to be used
 * by jWordSplitter.  
 * 
 * @author Daniel Naber
 */
public class ExportGermanNouns {

  private static final String DICT_FILENAME = "/resource/de/german.dict";
  
  private ExportGermanNouns() {
  }
  
  private Set<String> getWords() throws IOException {
    FSA fsa = FSA.getInstance(this.getClass().getResourceAsStream(DICT_FILENAME), "iso-8859-1");
    String lastTerm = null;
    Set<String> set = new HashSet<String>();
    for (Iterator i = fsa.getTraversalHelper().getAllSubsequences( fsa.getStartNode() ); i.hasNext();) {
      final byte [] sequence = (byte []) i.next();
      String output = new String(sequence, "iso-8859-1");
      if (output.indexOf("+SUB:") != -1 && output.indexOf(":ADJ") == -1) {
        String[] parts = output.split("\\+");
        String term = parts[0].toLowerCase();
        if (lastTerm == null || !lastTerm.equals(parts[0])) {
          //System.out.println(parts[0]);
          set.add(term);
        }
        lastTerm = term;
      }
    }
    return set;
  }
  
  private void serialize(Set<String> words, File outputFile) throws IOException {
    FileOutputStream fos = new FileOutputStream(outputFile);
    ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeObject(words);
    oos.close();
    fos.close();
  }
  
  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: ExportGermanNouns <outputFile>");
      System.exit(1);
    }
    ExportGermanNouns prg = new ExportGermanNouns();
    Set<String> words = prg.getWords();
    prg.serialize(words, new File(args[0]));
  }
    
}
