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
package de.danielnaber.languagetool.openoffice;

import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.uno.XComponentContext;

/**
 * This class is a factory that creates only a single instance,
 * or a singleton, of the Main class. Used for performance 
 * reasons and to allow various parts of code to interact.
 *
 * @author Marcin Miłkowski
 */
public class SingletonFactory implements XSingleComponentFactory {

  private transient de.danielnaber.languagetool.openoffice.Main instance;

  @Override
  public final Object createInstanceWithArgumentsAndContext(final Object[] arguments, 
      final XComponentContext xContext) throws com.sun.star.uno.Exception {    
    return createInstanceWithContext(xContext);
  }

  @Override
  public final Object createInstanceWithContext(final XComponentContext xContext) throws com.sun.star.uno.Exception {    
    if (instance == null) {     
      instance = new de.danielnaber.languagetool.openoffice.Main(xContext);      
    } else {  
      instance.changeContext(xContext);      
    }
    return instance;
  }  
}
