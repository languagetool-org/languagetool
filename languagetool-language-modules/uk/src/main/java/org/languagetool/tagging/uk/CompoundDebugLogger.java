package org.languagetool.tagging.uk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.languagetool.AnalyzedToken;

/**
 * Helps to write down compound word tagging results so we can analyze the algorithm effectiveness
 */
class CompoundDebugLogger {
  private static final String DEBUG_COMPOUNDS_PROPERTY = "org.languagetool.tagging.uk.UkrainianTagger.debugCompounds";

  private BufferedWriter compoundUnknownDebugWriter;
  private BufferedWriter compoundTaggedDebugWriter;
  private BufferedWriter compoundGenderMixDebugWriter;

  public CompoundDebugLogger() {
    if( Boolean.valueOf( System.getProperty(DEBUG_COMPOUNDS_PROPERTY) ) ) {
      initDebugCompounds();
    }
  }
  
  private void initDebugCompounds() {
    try {
      Path unknownFile = Paths.get("compounds-unknown.txt");
      Files.deleteIfExists(unknownFile);
      unknownFile = Files.createFile(unknownFile);
      compoundUnknownDebugWriter = Files.newBufferedWriter(unknownFile, Charset.defaultCharset());

      Path taggedFile = Paths.get("compounds-tagged.txt");
      Files.deleteIfExists(taggedFile);
      taggedFile = Files.createFile(taggedFile);
      compoundTaggedDebugWriter = Files.newBufferedWriter(taggedFile, Charset.defaultCharset());

      Path genderMixFile = Paths.get("gender-mix.txt");
      Files.deleteIfExists(genderMixFile);
      genderMixFile = Files.createFile(genderMixFile);
      compoundGenderMixDebugWriter = Files.newBufferedWriter(genderMixFile, Charset.defaultCharset());

//      Path tagged2File = Paths.get("tagged.txt");
//      Files.deleteIfExists(tagged2File);
//      taggedFile = Files.createFile(tagged2File);
//      taggedDebugWriter = Files.newBufferedWriter(tagged2File, Charset.defaultCharset());
    } catch (IOException ex) {
//      throw new RuntimeException(ex);
      System.err.println("Failed to open debug compounds file");
    }
  }

  public void logTaggedCompound(List<AnalyzedToken> guessedCompoundTags) {
    if( compoundTaggedDebugWriter == null || guessedCompoundTags == null )
      return;

    debug_tagged_write(guessedCompoundTags, compoundTaggedDebugWriter);
  }

  public void logUnknownCompound(String word) {
    if( compoundUnknownDebugWriter == null )
      return;
    
    try {
      compoundUnknownDebugWriter.append(word);
      compoundUnknownDebugWriter.newLine();
      compoundUnknownDebugWriter.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  private void debug_tagged_write(List<AnalyzedToken> analyzedTokens, BufferedWriter writer) {
    if( analyzedTokens.isEmpty()
        || analyzedTokens.get(0).getLemma() == null 
        || analyzedTokens.get(0).getToken().trim().isEmpty() )
      return;

    try {
      String prevToken = "";
      String prevLemma = "";
      for (AnalyzedToken analyzedToken : analyzedTokens) {
        String token = analyzedToken.getToken();
        
        boolean firstTag = false;
        if (! prevToken.equals(token)) {
          if( prevToken.length() > 0 ) {
            writer.append(";  ");
            prevLemma = "";
          }
          writer.append(token).append(" ");
          prevToken = token;
          firstTag = true;
        }
        
        String lemma = analyzedToken.getLemma();

        if (! prevLemma.equals(lemma)) {
          if( prevLemma.length() > 0 ) {
            writer.append(", ");
          }
          writer.append(lemma); //.append(" ");
          prevLemma = lemma;
          firstTag = true;
        }

        writer.append(firstTag ? " " : "|").append(analyzedToken.getPOSTag());
        firstTag = false;
      }
      writer.newLine();
      writer.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void logGenderMix(String word, boolean leftNv, String leftPosTag, String rightPosTag) {
    if( compoundGenderMixDebugWriter != null ) {
      try {
        compoundGenderMixDebugWriter.append(word + " " + (leftNv ? rightPosTag : leftPosTag));
        compoundGenderMixDebugWriter.newLine();
        compoundGenderMixDebugWriter.flush();
      } catch (IOException e) {
        System.err.println("Failed to write into gender mix file");
      }
    }
  }

}
