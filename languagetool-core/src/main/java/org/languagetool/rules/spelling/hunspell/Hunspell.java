package org.languagetool.rules.spelling.hunspell;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.languagetool.JLanguageTool;
import org.languagetool.broker.ResourceDataBroker;

@Slf4j
public final class Hunspell {
  private record PathPair(Path dictionary, Path affix) {
      private PathPair(Path dictionary, Path affix) {
        this.dictionary = Objects.requireNonNull(dictionary);
        this.affix = Objects.requireNonNull(affix);
      }
  }

  private record ResourcePair(String dictionaryPath, String affixPath) {
      private ResourcePair(String dictionaryPath, String affixPath) {
        this.dictionaryPath = Objects.requireNonNull(dictionaryPath);
        this.affixPath = Objects.requireNonNull(affixPath);
      }
  }

  /**
   * Cache for {@link HunspellDictionary Hunspell dictionaries} loaded from file paths (real files on disk)
   */
  private static final Map<PathPair, HunspellDictionary> pathCache = new HashMap<>();

  /**
   * Cache for {@link HunspellDictionary Hunspell dictionaries} loaded from resources (may involve temp files)
   */
  private static final Map<ResourcePair, HunspellDictionary> resourceCache = new HashMap<>();

  private static Factory hunspellDictionaryStreamFactory = viaTempFiles();

  private static Factory viaTempFiles() {
    return new Factory() {
      @Override
      public HunspellDictionary createFromLocalFiles(String languageCode, Path dictionary, Path affix) {
        // Local files on disk - no temp files, no cleanup needed
        return new DumontsHunspellDictionary(dictionary, affix, false);
      }

      @Override
      public HunspellDictionary createFromStreams(String language, InputStream dictionaryStream, InputStream affixStream) throws IOException {
        // Create temp files from streams - must clean up when dictionary is closed
        var tempFiles = createTempFilesFromStreams(language, dictionaryStream, affixStream);
        log.trace("Created temp files for language {}: {} and {}", language, tempFiles.dictionary, tempFiles.affix);
        return new DumontsHunspellDictionary(tempFiles.dictionary, tempFiles.affix, true);
      }
    };
  }

  private static PathPair createTempFilesFromStreams(String language, InputStream dictionaryStream, InputStream affixStream) throws IOException {
    Path dictionary = Files.createTempFile(language, ".dic");
    Path affix = Files.createTempFile(language, ".aff");
    Files.copy(dictionaryStream, dictionary, StandardCopyOption.REPLACE_EXISTING);
    Files.copy(affixStream, affix, StandardCopyOption.REPLACE_EXISTING);

    // Mark for deletion on JVM exit (for cached dictionaries that live for JVM lifetime)
    dictionary.toFile().deleteOnExit();
    affix.toFile().deleteOnExit();

    return new PathPair(dictionary, affix);
  }

  /**
   * Set a custom way to create {@link HunspellDictionary Hunspell dictionaries},
   * e.g., more efficient or portable than the default, possibly via Apache Lucene.
   * The default one is to use a native wrapper over the real Hunspell binary,
   * creating temporary files from the streams.
   */
  public static void setHunspellStreamFactory(Factory factory) {
    hunspellDictionaryStreamFactory = factory;
  }

  /**
   * Get a {@link HunspellDictionary} for files that already exist on disk.
   * The dictionary is cached and reused for subsequent requests with the same {@link Path paths}.
   * The files are NOT deleted when the dictionary is closed (caller owns the files).
   *
   * @param dictionary {@link Path} to dictionary file (.dic) on disk
   * @param affix {@link Path} to affix file (.aff) on disk
   * @return {@link HunspellDictionary} that is either cached or newly created
   */
  @NotNull
  public static synchronized HunspellDictionary getDictionary(Path dictionary, Path affix) {
    PathPair key = new PathPair(dictionary, affix);
    HunspellDictionary hunspell = pathCache.get(key);
    if (hunspell != null && !hunspell.isClosed()) {
      log.trace("Returning cached dictionary for {} and {}", dictionary, affix);
      return hunspell;
    }

    try {
      HunspellDictionary newHunspell = hunspellDictionaryStreamFactory
        .createFromLocalFiles(dictionary.getFileName().toString(), dictionary, affix);
      pathCache.put(key, newHunspell);
      log.trace("Created and cached new dictionary for {} and {}", dictionary, affix);
      return newHunspell;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @NotNull
  public static HunspellDictionary forDictionaryInResources(String language, String resourcePath) {
    return forDictionaryInResources(language, resourcePath + language + ".dic", resourcePath + language + ".aff");
  }

  /**
   * Get a {@link HunspellDictionary} from classpath resources.
   * The dictionary is cached by resource path and reused for subsequent requests.
   * If the resource is a {@code file://} URL, uses the file directly.
   * If the resource is in a JAR, creates temp files that live for the JVM lifetime.
   *
   * @param language Language code (for error messages and temp file naming)
   * @param dicPath Classpath resource path to dictionary file (.dic)
   * @param affPath Classpath resource path to affix file (.aff)
   * @return {@link HunspellDictionary} that is either cached or newly created
   */
  @NotNull
  public static synchronized HunspellDictionary forDictionaryInResources(String language, String dicPath, String affPath) {
    // Check cache first using resource paths as key (before creating any temp files)
    var key = new ResourcePair(dicPath, affPath);
    var cached = resourceCache.get(key);
    if (cached != null && !cached.isClosed()) {
      log.trace("Returning cached dictionary for resource language {}", language);
      return cached;
    }

    ResourceDataBroker broker = JLanguageTool.getDataBroker();

    // Try to get file:// URLs - if available, use files directly (no temp files needed)
    URL dicUrl = broker.getFromResourceDirAsUrl(dicPath);
    URL affUrl = broker.getFromResourceDirAsUrl(affPath);
    if (dicUrl != null && affUrl != null &&
      dicUrl.getProtocol().equals("file") && affUrl.getProtocol().equals("file")) {
      try {
        // Resources are real files on disk - use getDictionary which caches by path
        var dict = getDictionary(Path.of(dicUrl.toURI()), Path.of(affUrl.toURI()));
        // Also cache in resource cache for faster lookup next time
        resourceCache.put(key, dict);
        log.trace("Cached dictionary from file:// resource for language {}", language);
        return dict;
      } catch (URISyntaxException e) {
        throw new RuntimeException("Failed to convert resource URL to file path", e);
      }
    }

    // Resources are in JARs or other non-file sources - must create temp files
    // These temp files live for the JVM lifetime (deleteOnClose=false)
    try (var dictionaryStream = broker.getFromResourceDirAsStream(dicPath);
         var affixStream = broker.getFromResourceDirAsStream(affPath)) {
      if (dictionaryStream == null || affixStream == null) {
        throw new RuntimeException("Could not find the dictionary for language \"" + language + "\" in the classpath");
      }

      var tempFiles = createTempFilesFromStreams(language, dictionaryStream, affixStream);
      var dict = new DumontsHunspellDictionary(tempFiles.dictionary, tempFiles.affix, false);

      // Cache by resource path for future lookups (fixes #11380)
      resourceCache.put(key, dict);
      log.trace("Created and cached dictionary from JAR resource for language {}: {} and {}",
                language, tempFiles.dictionary, tempFiles.affix);

      return dict;
    } catch (IOException e) {
      throw new RuntimeException("Could not create temporary dictionaries for language \"" + language + "\"", e);
    }
  }

  @NotNull
  public static HunspellDictionary forDictionaryInResources(String language) {
    return forDictionaryInResources(language, "");
  }

  public interface Factory {
    /**
     * Create a {@link HunspellDictionary} from files that already exist on the local filesystem.
     * These files are owned by the caller and should NOT be deleted when the dictionary is closed.
     * The returned dictionary should have {@code deleteOnClose=false}.
     *
     * @param languageCode Language code for the dictionary
     * @param dictionary {@link Path} to dictionary file (.dic) on disk
     * @param affix {@link Path} to affix file (.aff) on disk
     * @return {@link HunspellDictionary} that will not delete the files on close
     * @throws IOException if an I/O error occurs while reading the files
     */
    default HunspellDictionary createFromLocalFiles(String languageCode, Path dictionary, Path affix) throws IOException {
      try (InputStream dic = Files.newInputStream(dictionary); InputStream aff = Files.newInputStream(affix)) {
        return createFromStreams(languageCode, dic, aff);
      }
    }

    /**
     * Create a {@link HunspellDictionary} from {@link InputStream InputStreams}.
     * Implementations must extract all necessary data from the streams before returning.
     * <p>
     * <strong>Important:</strong> The caller will close the streams, so implementations should not close them.
     * <p>
     * Implementations typically create temporary files and should use {@code deleteOnClose=true}
     * to clean them up when the dictionary is closed (unless the dictionary will be cached
     * for the lifetime of the JVM).
     *
     * @param languageCode Language code for the dictionary
     * @param dictionary {@link InputStream} for dictionary data (.dic)
     * @param affix {@link InputStream} for affix data (.aff)
     * @return {@link HunspellDictionary} (usually with {@code deleteOnClose=true} for temp file cleanup)
     * @throws IOException if an I/O error occurs while reading the streams
     */
    HunspellDictionary createFromStreams(String languageCode, InputStream dictionary, InputStream affix) throws IOException;
  }
}
