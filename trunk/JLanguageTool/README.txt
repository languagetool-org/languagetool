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
   -cursor first jumps to start of text, only then to the error position
   -LanguageTool gets confused when the text is changed while the LanguageTool 
    dialog is open
   -LanguageTool doesn't work if you have a Windows username with special characters
   -changing options only takes effect on next check
   -some errors trigger two rules at the same position, this makes the "Change text"
    button work wrong
   -Checking of selected text sometimes throws java.lang.reflect.UndeclaredThrowableException
    at $Proxy17.gotoRange(Unknown Source)
   -usability: pressing Esc too long will close both dialogs, should close only one
   
TODO:
 -add more rules, especially agreement stuff
 -see "TODO" in the source
 -create abstract SentenceRule and TextRule classes to get rid of reset() method?
 -...
