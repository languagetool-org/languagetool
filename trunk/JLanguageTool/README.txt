LanguageTool, a natural language style checker for English and German
Copyright (C) 2005,2006 Daniel Naber (naber at danielnaber de)
Version ###VERSION###, ###DATE###
Homepage: http://www.danielnaber.de/languagetool

Requirements:
 -Java 1.4 or later

Usage:
 -To integrate LanguageTool into OpenOffice.org:
  In OpenOffice.org, call Tools -> Package Manager -> Add... and
  add the LanguageTool ZIP file. Open a new document (File -> New ->
  Text document) and you'll have an new menu item "LanguageTool"
  which checks your text.
  
 -To use the simple demo GUI:
  java -jar LanguageToolGUI.jar

 -To check plain text files from the command line:
  java -jar LanguageTool.jar <filename>

 To compile the code, call "ant" and the *.jar files 
 will be created in the "dist" directory (you need the source
 package for this)
 
License:
 LGPL, see file COPYING
 
Known bugs:
 -OpenOffice.org integration:
   -LanguageTool doesn't work if you have a Windows username with special characters
   -LanguageTool gets confused when the text is changed while the LanguageTool 
    dialog is open
   -Checking of selected text sometimes throws java.lang.reflect.UndeclaredThrowableException
    at $Proxy17.gotoRange(Unknown Source)
   -some errors trigger two rules at the same position, this makes the "Change text"
    button work wrong
   -cursor first jumps to start of text, only then to the error position
   
TODO:
 -add more rules, especially agreement stuff
 -parse and use false friend rules
 -see "TODO" in the source
 -further increase test case coverage
 -fix false alerts:
 	...des Leipziger Verlages Faber... -> alle Adjektiv-Lesarten der Städte fehlen beim Tagging
	...gewann mit den Los Angeles Sparks am...
	...den Vorwurf, seine beiden Söhne im Alter von vier...
 -errors not detected:
 	...mein große Feind...
 -make gui.Main use the Configuration class to save its settings?
 -add information to rules about when they can give false alarm?
