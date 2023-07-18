package org.languagetool;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

/**
 * Resolves an XML file's external entity URIs as relative paths.
 */
public class RuleEntityResolver implements EntityResolver {
  private final URL xmlUrl;

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
    if ((publicId != null && publicId.endsWith(".ent")) || systemId.endsWith(".ent")) {
      URL entitiesUrl = new URL(xmlUrl, new URL(systemId).getPath());
      if (Objects.equals(entitiesUrl.getProtocol(), "jar")) {
        InputStream entitiesStream = JLanguageTool.getDataBroker().getAsStream(entitiesUrl.toString().split("!")[1]);
        return new InputSource(entitiesStream);
      } else {
        return new InputSource(entitiesUrl.getPath());
      }
    }
    return null;
  }

  /**
   * @param xmlUrl path to the source XML where the external entity relative path is found.
   */
  public RuleEntityResolver(URL xmlUrl) {
    this.xmlUrl = xmlUrl;
  }
}
