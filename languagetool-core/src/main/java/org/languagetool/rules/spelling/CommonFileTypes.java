/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.spelling;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @since 5.4
 */
public class CommonFileTypes {

  private final static List<String> COMMON_FILE_TYPES = Arrays.asList(
    "jpeg", "jpg", "gif", "png", "bmp", "svg", "ai", "sketch", "ico", "ps", "psd", "tiff", "tif",
    "mp3", "wav", "midi", "mid", "aif", "mpa", "ogg", "wma", "wpl", "cda",
    "7z", "arj", "deb", "pkg", "plist", "rar", "rpm", "tar.gz", "tar", "zip",
    "bin", "dmg", "iso", "toast", "vcd", "csv", "dat", "db", "log", "mdb", "sav", "sql", "xml",
    "apk", "bat", "bin", "cgi", "com", "exe", "gadget", "jar", "py", "js", "jsx", "json", "wsf", "ts", "tsx",
    "fnt", "fon", "otf", "ttf", "woff", "woff2",
    "rb", "java", "php", "html", "asp", "aspx", "cer", "cfm", "cgi", "pl", "css", "scss", "htm", "jsp", "part", "rss", "xhtml",
    "key", "odp", "pps", "ppt", "pptx", "class", "cpp", "cs", "h", "sh", "swift", "vb",
    "ods", "odt", "xlr", "xls", "xlsx", "xlt", "xltx", "bak", "cab", "cfg", "cpl", "cur", "dll", "dmp", "msi", "ini", "tmp",
    "3g2", "3gp", "avi", "flv", "h264", "m4v", "mkv", "mov", "mp4", "mpg", "mpeg", "rm", "swf", "vob", "wmv",
    "doc", "docx", "dot", "dotx", "pdf", "rtf", "srx", "text", "tex", "wks", "wps", "wpd", "txt", "yaml", "yml", "csl", "md", "adm"
  );
  
  private final static Pattern suffixPattern = 
    //Pattern.compile(".+\\.(" + String.join("|", COMMON_FILE_TYPES) + ")", Pattern.CASE_INSENSITIVE); 
    Pattern.compile("[\\wáàâóòìíéèùúôîêûäöüß\\-.()]*?.+\\.(" + String.join("|", COMMON_FILE_TYPES) + ")", Pattern.CASE_INSENSITIVE); 

  public static Pattern getSuffixPattern() {
    return suffixPattern;
  }

  private CommonFileTypes() {}
}
