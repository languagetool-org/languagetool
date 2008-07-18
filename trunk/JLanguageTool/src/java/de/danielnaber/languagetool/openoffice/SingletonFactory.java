/**
 * 
 */
package de.danielnaber.languagetool.openoffice;

import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.uno.XComponentContext;

/**
 * @author Marcin Miłkowski
 *
 * This class is a factory that creates only a single instance,
 * or a singleton, of the Main class. Used for performance 
 * reasons and to allow various parts of code to interact.
 *
 */
public class SingletonFactory implements XSingleComponentFactory {

  private transient de.danielnaber.languagetool.openoffice.Main instance = null;

  public final Object createInstanceWithArgumentsAndContext(final Object[] arguments, 
      final XComponentContext xContext) throws com.sun.star.uno.Exception {    
    return createInstanceWithContext(xContext);
  }

  public final synchronized Object createInstanceWithContext(final XComponentContext xContext) throws com.sun.star.uno.Exception {    
    if (instance == null) {
      instance = new de.danielnaber.languagetool.openoffice.Main(xContext);
    } else {  
        instance.changeContext(xContext);      
    }
    return instance;
  }  
}
