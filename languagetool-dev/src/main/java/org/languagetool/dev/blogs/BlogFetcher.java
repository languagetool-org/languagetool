/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.blogs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import de.abelssoft.tools.FileTools;
import org.languagetool.tools.StringTools;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Downloads blog content with the help of the readability.com API.
 * @since 2.7
 */
class BlogFetcher {

  private static final String READABILITY_API_KEY_FILE = "/home/dnaber/.readability-parser.txt";
  // e.g. <link rel="alternate" type="application/rss+xml" href="http://www.mimikama.at/feed/" />:
  private static final Pattern linkPattern = Pattern.compile("<link[^>]+?type=\"application/rss\\+xml\"[^>]+?/>", Pattern.DOTALL);
  private static final Pattern linkHrefPattern = Pattern.compile("href=[\"'](.*?)[\"']");

  private final String secretReadabilityToken;

  BlogFetcher(String secretReadabilityToken) {
    this.secretReadabilityToken = secretReadabilityToken;
  }

  private List<String> getBlogContent(String url) throws IOException {
    List<String> result = new ArrayList<>();
    String content = getContent(new URL(url));
    Matcher matcher = linkPattern.matcher(content);
    if (matcher.find()) {
      String linkContent = matcher.group();
      Matcher hrefMatcher = linkHrefPattern.matcher(linkContent);
      if (hrefMatcher.find()) {
        String feedUrl = hrefMatcher.group(1);
        SyndFeedInput input = new SyndFeedInput();
        try {
          SyndFeed feed = input.build(new XmlReader(new URL(feedUrl)));
          List<String> contentList = getContent(feed.getEntries());
          result.addAll(contentList);
          return result;
        } catch (Exception e) {
          throw new RuntimeException("Could not get feed data from " + feedUrl, e);
        }
      } else {
        System.err.println("No 'href' found for feed: " + url);
      }
    }
    System.err.println("No '<link>' found for feed: " + url);
    return result;
  }

  private List<String> getContent(List entries) throws IOException {
    List<String> result = new ArrayList<>();
    for (Object entry : entries) {
      SyndEntryImpl syndEntry = (SyndEntryImpl) entry;
      System.out.println("  Getting " + syndEntry.getUri());
      String json = getPageContent(syndEntry.getUri());
      ObjectMapper mapper = new ObjectMapper();
      Map map = mapper.readValue(json, Map.class);
      //System.out.println("json: " + json);
      //System.out.println("map: " + o);
      //System.out.println("content: " + o.get("content"));
      result.add(map.get("content").toString());
    }
    return result;
  }

  private String getContent(URL pageUrl) throws IOException {
    try (InputStream inputStream = pageUrl.openStream()) {
      return StringTools.streamToString(inputStream, "utf-8");
    }
  }

  private String getPageContent(String pageUrl) throws IOException {
    if (!pageUrl.startsWith("http")) {
      throw new IllegalArgumentException("Invalid feed URL: " + pageUrl);
    }
    URL url = new URL("https://www.readability.com/api/content/v1/parser?url=" + pageUrl + "&token=" + secretReadabilityToken);
    return getContent(url);
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.err.println("Usage: " + BlogFetcher.class.getSimpleName() + " <urlListFile> <outputDir>");
      System.exit(1);
    }
    String secret = FileTools.loadFile(new FileInputStream(READABILITY_API_KEY_FILE), "utf-8").trim();
    BlogFetcher fetcher = new BlogFetcher(secret);
    File outputDir = new File(args[1]);
    if (!outputDir.exists() || !outputDir.isDirectory()) {
      System.err.println("Output directory does not exist or is not a directory: " + outputDir);
      System.exit(1);
    }
    try (Scanner scanner = new Scanner(new File(args[0]))) {
      while (scanner.hasNextLine()) {
        String url = scanner.nextLine();
        try {
          File output = new File(outputDir, new URL(url).getHost());
          System.out.println("Working on " + url + ", writing result to " + output);
          List<String> blogContentList = fetcher.getBlogContent(url);
          try (FileWriter writer = new FileWriter(output)) {
            for (String content : blogContentList) {
              writer.write(content);
              writer.write("\n");
            }
          }
        } catch (Exception e) {
          //noinspection CallToPrintStackTrace
          e.printStackTrace();
        }
      }
    }
  }

}
