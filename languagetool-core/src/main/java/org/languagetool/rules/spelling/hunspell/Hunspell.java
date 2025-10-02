package org.languagetool.rules.spelling.hunspell;

import org.languagetool.JLanguageTool;
import org.languagetool.broker.ResourceDataBroker;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.function.BiFunction;

public final class Hunspell {
  static class LanguageAndPath {
    private final Path dictionary;
    private final Path affix;
    LanguageAndPath(Path dictionary, Path affix) {
      this.dictionary = Objects.requireNonNull(dictionary);
      this.affix = Objects.requireNonNull(affix);
    }
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      LanguageAndPath that = (LanguageAndPath) o;
      return Objects.equals(dictionary, that.dictionary) &&
          Objects.equals(affix, that.affix);
    }
    @Override
    public int hashCode() {
      return Objects.hash(dictionary, affix);
    }
  }

  static final boolean FORCE_TEMP_FILES = "true".equals(System.getenv("HUNSPELL_FORCE_TEMP_FILES"));

  private static final Map<LanguageAndPath, HunspellDictionary> map = new HashMap<>();
  private static BiFunction<Path, Path, HunspellDictionary> hunspellDictionaryFactory = DumontsHunspellDictionary::new;
  private static Factory hunspellDictionaryStreamFactory = viaTempFiles(DumontsHunspellDictionary::new);

  /**
   * @deprecated Use {@link #setHunspellStreamFactory}
   */
  @Deprecated
  public static void setHunspellDictionaryFactory(BiFunction<Path, Path, HunspellDictionary> factory) {
    if (FORCE_TEMP_FILES) {
      hunspellDictionaryFactory = factory;
    } else {
      hunspellDictionaryStreamFactory = viaTempFiles(factory);
    }
  }

  private static Factory viaTempFiles(BiFunction<Path, Path, HunspellDictionary> factory) {
    return new Factory() {
      @Override
      public HunspellDictionary createFromLocalFiles(String languageCode, Path dictionary, Path affix) {
        return factory.apply(dictionary, affix);
      }

      @Override
      public HunspellDictionary createFromStreams(String language, InputStream dictionaryStream, InputStream affixStream) throws IOException {
        Path dictionary = Files.createTempFile(language, ".dic");
        Path affix = Files.createTempFile(language, ".aff");
        Files.copy(dictionaryStream, dictionary, StandardCopyOption.REPLACE_EXISTING);
        Files.copy(affixStream, affix, StandardCopyOption.REPLACE_EXISTING);
        try {
          return factory.apply(dictionary, affix);
        } finally {
          Files.deleteIfExists(dictionary);
          Files.deleteIfExists(affix);
        }
      }
    };
  }

  /**
   * Set a custom way to create Hunspell dictionaries,
   * e.g., more efficient or portable than the default, possibly via Apache Lucene.
   * The default one is to use a native wrapper over the real Hunspell binary,
   * creating temporary files from the streams.
   */
  public static void setHunspellStreamFactory(Factory factory) {
    hunspellDictionaryStreamFactory = factory;
  }

  public static synchronized HunspellDictionary getDictionary(Path dictionary, Path affix) {
    LanguageAndPath key = new LanguageAndPath(dictionary, affix);
    HunspellDictionary hunspell = map.get(key);
    if (hunspell != null && !hunspell.isClosed()) {
      return hunspell;
    }

    if (FORCE_TEMP_FILES) {
      HunspellDictionary newHunspell = hunspellDictionaryFactory.apply(dictionary, affix);
      map.put(key, newHunspell);
      return newHunspell;
    }

    try {
      HunspellDictionary newHunspell = hunspellDictionaryStreamFactory
        .createFromLocalFiles(dictionary.getFileName().toString(), dictionary, affix);
      map.put(key, newHunspell);
      return newHunspell;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static HunspellDictionary forDictionaryInResources(String language, String resourcePath) {
    return forDictionaryInResources(language, resourcePath + language + ".dic", resourcePath + language + ".aff");
  }

  public static HunspellDictionary forDictionaryInResources(String language, String dicPath, String affPath) {
    if (FORCE_TEMP_FILES) {
      try {
        ResourceDataBroker broker = JLanguageTool.getDataBroker();
        InputStream dictionaryStream = broker.getAsStream(dicPath);
        InputStream affixStream = broker.getAsStream(affPath);
        if (dictionaryStream == null || affixStream == null) {
          throw new RuntimeException("Could not find dictionary for language \"" + language + "\" in classpath");
        }
        Path dictionary = Files.createTempFile(language, ".dic");
        Path affix = Files.createTempFile(language, ".aff");
        Files.copy(dictionaryStream, dictionary, StandardCopyOption.REPLACE_EXISTING);
        Files.copy(affixStream, affix, StandardCopyOption.REPLACE_EXISTING);
        return hunspellDictionaryFactory.apply(dictionary, affix);
      } catch (IOException e) {
        throw new RuntimeException("Could not create temporary dictionaries for language \"" + language + "\"", e);
      }
    }

    ResourceDataBroker broker = JLanguageTool.getDataBroker();
    URL dicUrl = broker.getFromResourceDirAsUrl(dicPath);
    URL affUrl = broker.getFromResourceDirAsUrl(affPath);
    if (dicUrl != null && affUrl != null &&
      dicUrl.getProtocol().equals("file") && affUrl.getProtocol().equals("file")) {
      try {
        return hunspellDictionaryStreamFactory.createFromLocalFiles(language, Path.of(dicUrl.toURI()), Path.of(affUrl.toURI()));
      } catch (IOException | URISyntaxException e) {
        throw new RuntimeException(e);
      }
    }

    try (var dic = broker.getFromResourceDirAsStream(dicPath); var aff = broker.getFromResourceDirAsStream(affPath)) {
      if (dic == null || aff == null) {
        throw new RuntimeException("Could not find the dictionary for language \"" + language + "\" in the classpath");
      }
      return hunspellDictionaryStreamFactory.createFromStreams(language, dic, aff);
    } catch (IOException e) {
      throw new RuntimeException("Could not create temporary dictionaries for language \"" + language + "\"", e);
    }
  }

  public static HunspellDictionary forDictionaryInResources(String language) {
    return forDictionaryInResources(language, "");
  }

  public interface Factory {
    /**
     * An equivalent of {@link #createFromStreams(String, InputStream, InputStream)} that can be used
     * if the caller is sure that the Hunspell dictionaries are located in the files in the local file system.
     * This allows for more efficient implementation.
     */
    default HunspellDictionary createFromLocalFiles(String languageCode, Path dictionary, Path affix) throws IOException {
      try (InputStream dic = Files.newInputStream(dictionary); InputStream aff = Files.newInputStream(affix)) {
        return createFromStreams(languageCode, dic, aff);
      }
    }

    /**
     * Create a Hunspell dictionary from the given streams.
     * All necessary information should be extracted from both streams by the time this method returns.
     * Closing the streams is not necessary in the implementation of this method, as the caller will close them itself.
     */
    HunspellDictionary createFromStreams(String languageCode, InputStream dictionary, InputStream affix) throws IOException;
  }
}
