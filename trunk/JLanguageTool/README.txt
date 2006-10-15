LanguageTool, a language style checker for English, German, and Polish
Copyright (C) 2005,2006 Daniel Naber (naber at danielnaber de)
Version ###VERSION###, ###DATE###
Homepage: http://www.danielnaber.de/languagetool

Requirements:
 -Java 1.5 or later

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
 LGPL, see file COPYING.txt
 The German data for part-of-speech tagging is taken from Morphy
 (http://www.wolfganglezius.de/doku.php?id=public:cl:morphy)
 
Known bugs:
 -OpenOffice.org integration:
   -doesn't check table content
   -getParagraphContent() iterates differently than OOoDialog.showError() so
    that there's an offset when showing the errors in documents with tables,
    i.e. the wrong text is marked
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
 -GUI: icon in window corner, better icon for system tray
 -Externalize strings in Main
 -Auto-reload rules if file timestamp has changed?
 -check if and how unification can be added to the XML rules
 -check if there's a nice design that lets us extends PatternRule and PatternRuleLoader
  to make them more powerful, but without having all features in these classes
 -clean up rule descriptions so that they coherently contain the error or the rule
  (e.g. "did + baseform" vs. "did + non-baseform")
 -use Java 1.5 generics everywhere (maybe also instead of arrays?)
 -add more rules, especially agreement stuff
 -add UnpairedQuotesBracketsRule (for pairing "(), [], {}, ��, "", ��, depending on the language)
 -add simple sentence/word complexity test like that: http://www.ooomacros.org/user.php#111318 
 -use regular expressions and POS tags to create complex suggestions (for example, suggesting another
  grammatical case or a word without an apostrophe)
 -see "TODO" in the source
 -create abstract SentenceRule and TextRule classes to get rid of reset() method?
  -...

Using LanguageTool from .NET:
 Thanks to IKVM (http://www.ikvm.net/) you can easily turn LanguageTool
 into a .NET exe or dll (without the GUI and the OpenOffice.org integration).
 Just adapt these commands to you local path names (this example shows using mono):

 export MONO_PATH=/path/to/ikvm/bin
 mono /path/to/ikvm/bin/ikvmc.exe -target:library -r:/path/to/ikvm/bin/IKVM.GNU.Classpath.dll libs/lucene-core-2.0.0.jar
 mono /path/to/ikvm/bin/ikvmc.exe -target:library -r:/path/to/ikvm/bin/IKVM.GNU.Classpath.dll libs/trove.jar
 mono /path/to/ikvm/bin/ikvmc.exe -target:library -r:/path/to/ikvm/bin/IKVM.GNU.Classpath.dll -r:trove.dll libs/maxent-2.4.0.jar
 mono /path/to/ikvm/bin/ikvmc.exe -target:library -r:/path/to/ikvm/bin/IKVM.GNU.Classpath.dll -r:trove.dll -r:maxent-2.4.0.dll libs/opennlp-tools-1.3.0.jar
 mono /path/to/ikvm/bin/ikvmc.exe -r:/path/to/ikvm/bin/IKVM.GNU.Classpath.dll -r:trove.dll -r:lucene-core-2.0.0..dll -r:opennlp-tools-1.3.0.dll LanguageTool.jar

 However, the resulting LanguageTool.exe has not been tested much yet.
