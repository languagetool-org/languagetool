/* ReflectionUtils, helper methods to load classes dynamically 
 * Copyright (C) 2007 Andriy Rysin
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
package de.danielnaber.languagetool.tools;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ReflectionUtils {

  /**
   * @param classLoader Classloader to use for loading classes
   * @param packageName Package name to check classes in
   * @param classNameRegEx If not null limit class names to this regexp. This parameter 
   *  is checked before class is loaded so use it to improve performance by skipping loading extra classes
   * @param subdirLevel If more than 0 all subdirectories/subpackages up to <code>dirLevel</code> will be
   *  traversed This parameter is checked before class is loaded - use it to improve
   *  performance by skipping loading extra classes
   * @param classExtends If not null return only classes which extend this class
   * @param interfaceImplements If not null return only classes which implement this interface
   * @return Returns all classes inside given package
   * @throws ClassNotFoundException
   */
  public static Class[] findClasses(ClassLoader classLoader, String packageName,
      String classNameRegEx, int subdirLevel, Class classExtends, Class interfaceImplements)
      throws ClassNotFoundException {
    List<Class> foundClasses = new ArrayList<Class>();

    try {
      String packagePath = packageName.replace('.', '/');
      Enumeration<URL> resources_ = classLoader.getResources(packagePath);

      Set<URL> uniqResources = new HashSet<URL>();
      for (; resources_.hasMoreElements();) {
        URL resource = resources_.nextElement();
          uniqResources.add(resource);
      }

      for (URL resource : uniqResources) {
         //System.err.println("trying resource: " + resource);
        // jars and directories are treated differently
        if (resource.getProtocol().startsWith("jar")) {        
          // The LanguageTool ZIP contains two JARs with the core classes,
          // so ignore one of them to avoid rule duplication:          
          if (resource.getPath().contains("LanguageTool.uno.jar")) {
            continue;
          }
          findClassesInJar(packageName, classNameRegEx, subdirLevel, classExtends,
              interfaceImplements, foundClasses, resource);
        } else {
          findClassesInDirectory(classLoader, packageName, classNameRegEx, subdirLevel,
              classExtends, interfaceImplements, foundClasses, resource);
        }
      }
    } catch (Exception ex) {
      throw new ClassNotFoundException("Loading rules failed: " + ex.getMessage(), ex);
    }

    return foundClasses.toArray(new Class[0]);
  }

  private static void findClassesInDirectory(ClassLoader classLoader, String packageName, 
      String classNameRegEx, int subdirLevel, Class classExtends, Class interfaceImplements,
      List<Class> foundClasses, URL resource) throws URISyntaxException, Exception, ClassNotFoundException {
    File directory = new File(resource.toURI());

    if (!directory.exists() && !directory.isDirectory()) {
      throw new Exception("directory does not exist: " + directory.getAbsolutePath());
    }

    // read classes
    for (File file : directory.listFiles()) {
      if (file.isFile() && file.getName().endsWith(".class")) {
        String classShortNm = file.getName().substring(0, file.getName().lastIndexOf('.'));
        if (classNameRegEx == null || classShortNm.matches(classNameRegEx)) {
          Class clazz = Class.forName(packageName + "." + classShortNm);

          if (!isMaterial(clazz))
            continue;

          if (classExtends == null || isExtending(clazz, classExtends.getName())) {
            if (interfaceImplements == null || isImplementing(clazz, interfaceImplements)) {
              foundClasses.add(clazz);
              // System.err.println("Added rule from dir: " + classShortNm);
            }
          }
        }
      }
    }

    // then subdirectories if we're traversing
    if (subdirLevel > 0) {
      for (File dir : directory.listFiles()) {
        if (dir.isDirectory()) {
          Class[] subLevelClasses = findClasses(classLoader, packageName + "."
              + dir.getName(), classNameRegEx, subdirLevel - 1, classExtends,
              interfaceImplements);
          foundClasses.addAll(Arrays.asList(subLevelClasses));
        }
      }
    }
  }

  private static void findClassesInJar(String packageName, String classNameRegEx, 
      int subdirLevel, Class classExtends, Class interfaceImplements, List<Class> foundClasses,
      URL resource) throws IOException, URISyntaxException, ClassNotFoundException {
    JarURLConnection conn = (JarURLConnection) resource.openConnection();
    JarFile currentFile = conn.getJarFile(); //new JarFile(new File(resource.toURI()));
    // jars are flat containers:
    for (Enumeration<JarEntry> e = currentFile.entries(); e.hasMoreElements();) {
      JarEntry current = (JarEntry) e.nextElement();
      String name = current.getName();
      // System.err.println("jar entry: " + name);

      if (name.endsWith(".class")) {
        String classNm = name.replaceAll("/", ".").replace(".class", "");
        int pointIdx = classNm.lastIndexOf('.');
        String classShortNm = pointIdx == -1 ? classNm : classNm.substring(pointIdx + 1);

        if (classNm.startsWith(packageName)
            && (classNameRegEx == null || classShortNm.matches(classNameRegEx))) {
          String subName = classNm.substring(packageName.length() + 1);

          if (countOccurences(subName, '.') > subdirLevel)
            continue;

          Class clazz = Class.forName(classNm);
          if(foundClasses.contains(clazz)) {
            throw new RuntimeException("Duplicate class definition:\n" + clazz.getName() +
                ", found in\n" + currentFile.getName());
          }

          if (!isMaterial(clazz))
            continue;

          if (classExtends == null || isExtending(clazz, classExtends.getName())) {
            if (interfaceImplements == null || isImplementing(clazz, interfaceImplements)) {
              foundClasses.add(clazz);
              // System.err.println("Added class from jar: " + name);
            }
          }
        }
      }
    }
  }

  private static int countOccurences(String str, char ch) {
    int i = 0;
    int pos = str.indexOf(ch, 0);
    while (pos != -1) {
      i++;
      pos = str.indexOf(ch, pos + 1);
    }
    return i;
  }

  private static boolean isMaterial(Class clazz) {
    int mod = clazz.getModifiers();
    return (!Modifier.isAbstract(mod) && !Modifier.isInterface(mod));
  }

  /**
   * @param class1
   * @param superclassName
   * @return Returns true if class1 extends
   */
  private static boolean isExtending(Class class1, String superclassName) {
    Class superclass1 = class1.getSuperclass();
    while (superclass1 != null) {
      if (superclassName.equals(superclass1.getName()))
        return true;
      superclass1 = superclass1.getSuperclass();
    }
    return false;
  }

  private static boolean isImplementing(Class clazz, Class interfaze) {
    return Arrays.asList(clazz.getInterfaces()).contains(interfaze);
  }
  
}
