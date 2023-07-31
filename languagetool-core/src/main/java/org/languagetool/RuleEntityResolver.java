package org.languagetool;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Resolves an XML file's external entity URIs as relative paths.
 */
public class RuleEntityResolver implements EntityResolver {

  /**
   * Resolves the entity's absolute path by taking the relative path found in the source XML and combining it with the
   * path of the source XML file (which also must be resolved to a full absolute path).
   * @param publicId The public identifier of the external entity
   *        being referenced, or null if none was supplied.
   * @param systemId The system identifier of the external entity
   *        being referenced.
   * @return InputSource An InputSource constructed from the resolved path of the file the external entity points to
   */
  @Override
  public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
    if (systemId != null && systemId.endsWith(".ent")) {
      return new InputSource(getInputStreamLTEntities(systemId));
    }
    return null;
  }

  public String getPathFromLTResourceFolder(String input) {
    // we assume that the entities file is in the resource folder of each language
    return input.replaceAll(".*/resource/", "").replaceAll("\\.\\./", "");
  }

  public InputStream getInputStreamLTEntities(String input) {
    return JLanguageTool.getDataBroker()
      .getFromResourceDirAsStream(getPathFromLTResourceFolder(input));
  }
}
