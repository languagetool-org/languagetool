package org.languagetool.rules.spelling.hunspell;

import org.languagetool.JLanguageTool;
import org.languagetool.broker.ResourceDataBroker;

import java.io.*;
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

  private static final Map<LanguageAndPath, HunspellDictionary> map = new HashMap<>();
  private static Factory hunspellDictionaryFactory = viaTempFiles(DumontsHunspellDictionary::new);

  /**
   * @deprecated Use {@link #setHunspellStreamFactory}
   */
  @Deprecated
  public static void setHunspellDictionaryFactory(BiFunction<Path, Path, HunspellDictionary> factory) {
    hunspellDictionaryFactory = viaTempFiles(factory);
  }

  private static Factory viaTempFiles(BiFunction<Path, Path, HunspellDictionary> factory) {
    return (language, dictionaryStream, affixStream) -> {
      Path dictionary = Files.createTempFile(language, ".dic");
      Path affix = Files.createTempFile(language, ".aff");
      Files.copy(dictionaryStream, dictionary, StandardCopyOption.REPLACE_EXISTING);
      Files.copy(affixStream, affix, StandardCopyOption.REPLACE_EXISTING);
      return factory.apply(dictionary, affix);
    };
  }

  /**
   * Set a custom way to create Hunspell dictionaries,
   * e.g., more efficient or portable than the default, possibly via Apache Lucene.
   * The default one is to use a native wrapper over the real Hunspell binary,
   * creating temporary files from the streams.
   */
  public static void setHunspellStreamFactory(Factory factory) {
    hunspellDictionaryFactory = factory;
  }

  public static synchronized HunspellDictionary getDictionary(Path dictionary, Path affix) {
    LanguageAndPath key = new LanguageAndPath(dictionary, affix);
    HunspellDictionary hunspell = map.get(key);
    if (hunspell != null && !hunspell.isClosed()) {
      return hunspell;
    }
    try {
      HunspellDictionary newHunspell = hunspellDictionaryFactory.create(
        dictionary.getFileName().toString(),
        Files.newInputStream(dictionary),
        Files.newInputStream(affix));
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
    try {
      ResourceDataBroker broker = JLanguageTool.getDataBroker();
      InputStream dictionaryStream = broker.getFromResourceDirAsStream(dicPath);
      InputStream affixStream = broker.getFromResourceDirAsStream(affPath);
      if (dictionaryStream == null || affixStream == null) {
        throw new RuntimeException("Could not find the dictionary for language \"" + language + "\" in the classpath");
      }
      return hunspellDictionaryFactory.create(language, dictionaryStream, affixStream);
    } catch (IOException e) {
      throw new RuntimeException("Could not create temporary dictionaries for language \"" + language + "\"", e);
    }
  }

  public static HunspellDictionary forDictionaryInResources(String language) {
    return forDictionaryInResources(language, "");
  }

  public interface Factory {
    HunspellDictionary create(String languageCode, InputStream dictionary, InputStream affix) throws IOException;
  }
}
