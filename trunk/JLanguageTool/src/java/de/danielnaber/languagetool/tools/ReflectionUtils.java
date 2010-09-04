/* ReflectionUtils, helper methods to load classes dynamically 
 * Copyright (C) 2007 Andriy Rysin, Marcin Milkowski, Daniel Naber
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
import java.net.URI;
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

public final class ReflectionUtils {

  private ReflectionUtils() {
    // a static singleton class
  }

  /**
   * @param classLoader
   *          Classloader to use for loading classes
   * @param packageName
   *          Package name to check classes in
   * @param classNameRegEx
   *          If not null limit class names to this regexp. This parameter is
   *          checked before class is loaded so use it to improve performance by
   *          skipping loading extra classes
   * @param subdirLevel
   *          If more than 0 all subdirectories/subpackages up to
   *          <code>dirLevel</code> will be traversed This parameter is checked
   *          before class is loaded - use it to improve performance by skipping
   *          loading extra classes
   * @param classExtends
   *          If not null return only classes which extend this class
   * @param interfaceImplements
   *          If not null return only classes which implement this interface
   * @return Returns all classes inside given package
   * @throws ClassNotFoundException
   */
  public static Class[] findClasses(final ClassLoader classLoader,
      final String packageName, final String classNameRegEx,
      final int subdirLevel, final Class classExtends,
      final Class interfaceImplements) throws ClassNotFoundException {
    final List<Class> foundClasses = new ArrayList<Class>();

    try {
      final String packagePath = packageName.replace('.', '/');
      final Enumeration<URL> resources_ = classLoader.getResources(packagePath);

      final Set<URI> uniqResources = new HashSet<URI>();
      while (resources_.hasMoreElements()) {
        final URI resource = resources_.nextElement().toURI();
        uniqResources.add(resource);
      }

      for (final URI res : uniqResources) {
        final URL resource = res.toURL();
        // System.err.println("trying resource: " + resource);
        // jars and directories are treated differently
        if (resource.getProtocol().startsWith("jar")) {
          findClassesInJar(packageName, classNameRegEx, subdirLevel,
              classExtends, interfaceImplements, foundClasses, resource);
        } else {
          findClassesInDirectory(classLoader, packageName, classNameRegEx,
              subdirLevel, classExtends, interfaceImplements, foundClasses,
              resource);
        }
      }
    } catch (final Exception ex) {
      throw new ClassNotFoundException("Loading rules failed: "
          + ex.getMessage(), ex);
    }

    return foundClasses.toArray(new Class[foundClasses.size()]);
  }

  private static void findClassesInDirectory(final ClassLoader classLoader,
      final String packageName, final String classNameRegEx,
      final int subdirLevel, final Class classExtends,
      final Class interfaceImplements, final List<Class> foundClasses,
      final URL resource) throws Exception {
    final File directory = new File(resource.toURI());

    if (!directory.exists() && !directory.isDirectory()) {
      throw new Exception("directory does not exist: "
          + directory.getAbsolutePath());
    }

    // read classes
    for (final File file : directory.listFiles()) {
      if (file.isFile() && file.getName().endsWith(".class")) {
        final String classShortNm = file.getName().substring(0,
            file.getName().lastIndexOf('.'));
        if (classNameRegEx == null || classShortNm.matches(classNameRegEx)) {
          final Class clazz = Class.forName(packageName + "." + classShortNm);

          if (!isMaterial(clazz)) {
            continue;
          }

          if (classExtends == null
              || isExtending(clazz, classExtends.getName())
              && interfaceImplements == null
              || isImplementing(clazz, interfaceImplements)) {
            foundClasses.add(clazz);
            // System.err.println("Added rule from dir: " + classShortNm);
          }
        }
      }
    }

    // then subdirectories if we're traversing
    if (subdirLevel > 0) {
      for (final File dir : directory.listFiles()) {
        if (dir.isDirectory()) {
          final Class[] subLevelClasses = findClasses(classLoader, packageName
              + "." + dir.getName(), classNameRegEx, subdirLevel - 1,
              classExtends, interfaceImplements);
          foundClasses.addAll(Arrays.asList(subLevelClasses));
        }
      }
    }
  }

  private static void findClassesInJar(final String packageName,
      final String classNameRegEx, final int subdirLevel,
      final Class classExtends, final Class interfaceImplements,
      final List<Class> foundClasses, final URL resource) throws IOException,
      URISyntaxException, ClassNotFoundException {
    final JarURLConnection conn = (JarURLConnection) resource.openConnection();
    final JarFile currentFile = conn.getJarFile(); // new JarFile(new
    // File(resource.toURI()));
    // jars are flat containers:
    for (final Enumeration<JarEntry> e = currentFile.entries(); e
        .hasMoreElements();) {
      final JarEntry current = e.nextElement();
      final String name = current.getName();
      // System.err.println("jar entry: " + name);

      if (name.endsWith(".class")) {
        final String classNm = name.replaceAll("/", ".").replace(".class", "");
        final int pointIdx = classNm.lastIndexOf('.');
        final String classShortNm = pointIdx == -1 ? classNm : classNm
            .substring(pointIdx + 1);

        if (classNm.startsWith(packageName)
            && (classNameRegEx == null || classShortNm.matches(classNameRegEx))) {
          final String subName = classNm.substring(packageName.length() + 1);

          if (countOccurrences(subName, '.') > subdirLevel) {
            continue;
          }

          final Class clazz = Class.forName(classNm);
          if (foundClasses.contains(clazz)) {
            throw new RuntimeException("Duplicate class definition:\n"
                + clazz.getName() + ", found in\n" + currentFile.getName());
          }

          if (!isMaterial(clazz)) {
            continue;
          }

          if (classExtends == null
              || isExtending(clazz, classExtends.getName())
              && interfaceImplements == null
              || isImplementing(clazz, interfaceImplements)) {
            foundClasses.add(clazz);
            // System.err.println("Added class from jar: " + name);
          }
        }
      }
    }
  }

  private static int countOccurrences(final String str, final char ch) {
    int i = 0;
    int pos = str.indexOf(ch, 0);
    while (pos != -1) {
      i++;
      pos = str.indexOf(ch, pos + 1);
    }
    return i;
  }

  private static boolean isMaterial(final Class clazz) {
    final int mod = clazz.getModifiers();
    return !Modifier.isAbstract(mod) && !Modifier.isInterface(mod);
  }

  /**
   * @return Returns true if clazz extends superClassName
   */
  private static boolean isExtending(final Class clazz,
      final String superClassName) {
    Class tmpSuperClass = clazz.getSuperclass();
    while (tmpSuperClass != null) {
      if (superClassName.equals(tmpSuperClass.getName())) {
        return true;
      }
      tmpSuperClass = tmpSuperClass.getSuperclass();
    }
    return false;
  }

  private static boolean isImplementing(final Class clazz, final Class interfaze) {
    return Arrays.asList(clazz.getInterfaces()).contains(interfaze);
  }

}
