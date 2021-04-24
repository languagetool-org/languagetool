package org.languagetool.tagging.uk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.languagetool.AnalyzedToken;

/**
 * Helps to write down compound word tagging results so we can analyze the algorithm effectiveness
 */
class CompoundDebugLogger {
  private static final String DEBUG_COMPOUNDS_PROPERTY = "org.languagetool.tagging.uk.UkrainianTagger.debugCompounds";

  private BufferedWriter compoundUnknownDebugWriter;
  private BufferedWriter compoundTaggedDebugWriter;
  private BufferedWriter compoundGenderMixDebugWriter;
  private BufferedWriter compoundTaggedLemmaDebugWriter;

  public CompoundDebugLogger() {
    if( Boolean.valueOf( System.getProperty(DEBUG_COMPOUNDS_PROPERTY) ) ) {
      initDebugCompounds();
    }
  }
  
  private synchronized void initDebugCompounds() {
    if( compoundUnknownDebugWriter == null ) {
      try {
        compoundUnknownDebugWriter = createDebugOutFile("compounds-unknown.txt");
        compoundTaggedDebugWriter = createDebugOutFile("compounds-tagged.txt");
        compoundTaggedLemmaDebugWriter = createDebugOutFile("compounds-lemma-tagged.txt");
        compoundGenderMixDebugWriter = createDebugOutFile("gender-mix.txt");
      } catch (IOException ex) {
        System.err.println("Failed to open debug compounds file");
      }
    }
  }

  private BufferedWriter createDebugOutFile(String filename) throws IOException {
    Path unknownFile = Paths.get(filename);
    Files.deleteIfExists(unknownFile);
    unknownFile = Files.createFile(unknownFile);
    return Files.newBufferedWriter(unknownFile, Charset.defaultCharset());
  }

  public void logTaggedCompound(List<AnalyzedToken> guessedCompoundTags) {
    if( compoundTaggedDebugWriter == null || guessedCompoundTags == null )
      return;

    debug_tagged_write(guessedCompoundTags, compoundTaggedDebugWriter);

    guessedCompoundTags.stream().map(t -> t.getLemma()).collect(Collectors.toSet()).forEach( w ->
        logLine(compoundTaggedLemmaDebugWriter, w)
    );
  }

  private static int cnt = 0;
  public void logLine(BufferedWriter writer, String word) {
    if( writer == null )
      return;
    
    try {
      writer.append(word).append('\n');
      if( ++cnt % 10 == 0) writer.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  void logUnknownCompound(String word) {
    logLine(compoundUnknownDebugWriter, word);
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
          writer.append(token).append(' ');
          prevToken = token;
          firstTag = true;
        }
        
        String lemma = analyzedToken.getLemma();

        if (! prevLemma.equals(lemma)) {
          if( prevLemma.length() > 0 ) {
            writer.append(", ");
          }
          writer.append(lemma); //.append(' ');
          prevLemma = lemma;
          firstTag = true;
        }

        writer.append(firstTag ? " " : "|").append(analyzedToken.getPOSTag());
        firstTag = false;
      }
      writer.newLine();
      if( ++cnt % 10 == 0) writer.flush();
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
