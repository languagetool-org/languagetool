/* LanguageTool, a natural language style checker
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.broker;

import org.languagetool.JLanguageTool;

/**
 * Is responsible for loading the necessary classes for LanguageTool
 * library. It is necessary to provide the ability to load classes from
 * custom classloaders.
 * <p>
 *
 * Make sure that you never obtain any LanguageTool class by calling
 * {@code Class.forName(String)} directly. If you would like to
 * load class do always use {@link JLanguageTool#getClassBroker()}
 * which provides proper method for loading classes.
 *
 * @since 4.9
 */
public interface ClassBroker {
    /**
     * Returns the {@code Class} object associated with the class or
     * interface with the given string name.
     *
     * @param     qualifiedName the fully qualified name of the desired class.
     * @return    the {@code Class} object for the class with the specified name.
     * @exception ClassNotFoundException if the class cannot be located
     */
    Class<?> forName(String qualifiedName) throws ClassNotFoundException;
}
