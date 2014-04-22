/* LanguageTool, a natural language style checker 
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev;

import org.dashnine.preditor.LanguageModel;
import sleep.runtime.Scalar;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AtDImporter {

  private void run() throws IOException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
    String file = "/prg/atd/models/model.bin";
    try(FileInputStream fis = new FileInputStream(file)) {
      ObjectInputStream oos = new ObjectInputStream(fis);
      Object c = oos.readObject();
      System.out.println(c.getClass());
      Scalar s = (Scalar)c;
      System.out.println(">s:"+s);
      System.out.println(">s:"+(s.objectValue()));
      Map languageModel = ((LanguageModel) s.objectValue()).getLanguageModel();
      System.out.println(">s:" + languageModel.size());
      Iterator iterator = languageModel.entrySet().iterator();

      for (int i = 0; i < 20; i++) {
        Object next = iterator.next();
        Field next1 = next.getClass().getDeclaredField("next");
        next1.setAccessible(true);
        System.out.println("F1: " + next1);
        System.out.println("F2: " + ((Map.Entry)next1.get(next)));
        System.out.println("F3: " + ((Map)next1.get(next)));
      }

      /*Field count = next.getClass().getDeclaredField("count");
      count.setAccessible(true);
      System.out.println("C: " + count);
      System.out.println("C: " + count.getName());

      System.out.println(">:" + next);
      System.out.println(">:" + iterator.next());
      System.out.println(">:" + iterator.next());
      System.out.println(">:" + iterator.next());*/
      /*//System.out.println(">s:"+s.doubleValue());
      System.out.println(">s:"+s.getActualValue());
      //System.out.println(">s:"+s.getHash());
      System.out.println(">s:"+s.getValue());
      //System.out.println(">s:"+s.getValue().);*/
      /*Iterator i = s.getArray().scalarIterator();
      while (i.hasNext()) {
        Scalar temp = (Scalar)i.next();
        System.out.println(temp);
      }*/
    }
  }

  public static void main(String[] args) throws Exception {
    AtDImporter importer = new AtDImporter();
    importer.run();
  }
}
