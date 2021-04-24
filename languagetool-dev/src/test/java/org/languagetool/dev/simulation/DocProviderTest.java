/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.simulation;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

public class DocProviderTest {

  @Ignore("for interactive use only")
  @Test
  public void testDistribution() {
    DocProvider docProvider = new DocProvider(null);
    Map<Integer,Integer> map = new TreeMap<>();
    for (int i = 0; i < 100; i++) {
      //int next = docProvider.getRandomMaxLength();
      int next = docProvider.getWeightedRandomLength();
      //System.out.println(next);
      int key = next - next % 50;
      map.compute(key, (k,v) -> v == null ? 1 : map.get(k) + 1);
    }
    int rest = 0;
    for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
      if (entry.getKey() >= 550) {
        rest += entry.getValue();
      } else {
        System.out.println(entry.getKey() + " -> " + entry.getValue());
      }
    }
    System.out.println("550+ -> " + rest);
  }

  @Ignore("for interactive use only")
  @Test
  public void testGetDoc() throws IOException {
    DocProvider docProvider = new DocProvider(Files.readAllLines(Paths.get("/home/dnaber/data/corpus/tatoeba/20191014/tatoeba-sentences-de-20191014.txt")));
    for (int i = 0; i < 100; i++) {
      System.out.println("=========================================");
      String next = docProvider.getDoc();
      System.out.println(next);
    }
  }

}