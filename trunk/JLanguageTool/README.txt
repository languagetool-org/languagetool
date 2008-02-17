LanguageTool, a language style checker for English, German, Polish,
and Dutch with initial support for Spanish, French, and Italian
Copyright (C) 2005-2008 Daniel Naber (naber at danielnaber de)
Version ###VERSION###, ###DATE###
Homepage: http://www.danielnaber.de/languagetool

Requirements:
 -Java 1.5 or later

Usage:
 -To integrate LanguageTool into OpenOffice.org, you
 can use two methods:
 
 1. Double-click LanguageTool-###VERSION###.oxt. If you
 have OpenOffice.org >2.0.4 integrated into the environment,
 the extension should start installing. Follow the on-screen
 instructions.
 
 2. If the above method doesn't work, in OpenOffice.org, 
  call Tools -> Extension Manager -> Add... and browse for the 
  LanguageTool-###VERSION###.oxt file. 
  
  Open a new document (File -> New ->  Text document) and 
  you'll have a new menu item "LanguageTool" under the "Tools"
  menu.
  
 -To use the simple demo GUI, first rename the .oxt file
  to zip, then unzip it to a new directory and call:
  java -jar LanguageToolGUI.jar

 -To check plain text files from the command line:
  java -jar LanguageTool.jar <filename>

Known bugs:
 -OpenOffice.org integration:
   -doesn't work correctly with documents that contain revisions
   -doesn't check table content
   -getParagraphContent() iterates differently than OOoDialog.showError() so
    that there's an offset when showing the errors in documents with tables,
    i.e., the wrong text is marked
   -LanguageTool gets confused when the text is changed while the LanguageTool 
    dialog is open
   -changing options only takes effect on next check
   -some errors trigger two rules at the same position, this makes the "Change text"
    button work wrong
   -Checking of selected text sometimes throws java.lang.reflect.UndeclaredThrowableException
    at $Proxy17.gotoRange(Unknown Source)
   -usability: pressing Esc too long will close both dialogs, should close only one
 -general:
   -for some rules there may be a lot of false alarms, i.e., LanguageTool complains
    about text which is actually correct
   
TODO:
 -update languagetool.xml.update automatically (i.e. replace @version@)
 -add more redundancy rules, see e.g.
  http://grammar.about.com/od/words/a/redundancies.htm?p=1
 -"&" is shown in buttons, not interpreted as shortcut anymore (OOo only?!)
 -menu and toolbar icon is broken (black square or empty) for me
 -feedback from http://extensions.services.openoffice.org/project/languagetool:
  1) It does not seem to understand global multi-user installs. If you 
   install from root it it pushed to all users, however when you try and
   customize your own settings it's trying to write into the global settings
   area and fails. Settings should go into $HOME
  2) The [ Close ] button fails to close the UI and requires the "X" of the 
   window manager
 -use hunspell via jni, see http://tkltrans.sourceforge.net/spell.htm and
  http://tkltrans.sourceforge.net/magyar/huncheck.tar.gz
 -rules: I/You/We/You/They does (not)-> do (not), He/She/It do (not)
 -finish "Add language..." work
 -put licenses in extra subdir
 -check if some rules from xml-copy-editor.sourceforge.net are useful for us
  (see its source in the xmlcopyeditor-1.0.9.5/src/rulesets directory)
 -make the dist-src work (= compile out of the box)
 -add a layer to use the simple XML so the LanguageTool GUIs can use An Gramadoir?
 -load (language-specific) abbreviations from an external file
 -OpenOffice.org: make the dialog modal
 -stand-alone GUI: mark errors in upper part of window
 -Auto-reload rules if file timestamp has changed?
 -enable style registers and/or rule classes
 -clean up rule descriptions so that they coherently contain the error or the rule
  (e.g., "did + baseform" vs. "did + non-baseform")
 -add more rules, especially agreement stuff 
 -fix AvsAnRule: use 'an' before an abbreviation that begins with a vowel
  sound (like 'an MSc').
 -add a simple sentence/word complexity test like that: http://www.ooomacros.org/user.php#111318 
 -German rule: Vergleichs vs Vergleiches etc -> only one variant per document should be used
 -add unification to the XML rules in two steps:
 	-create XML definitions of unification rules based on
 	POS regular expression transformations
 -create abstract SentenceRule and TextRule classes to get rid of reset() method?
 -check if there's a nice design that lets us extend PatternRule and PatternRuleLoader
  to make them more powerful, but without having all features in these classes
 -add more docs and examples 
 -Make adding language possible without changing the LanguageTool core code:
 	-make rule loading dynamic by using reflection (in progress)
 	-create the list of languages using reflection (add a LanguageInformation
 	 interface that each language needs to implement)
 	-Add an "Add language pack..." menu to both the stand-alone version and the
 	 OpenOffice.org version
 -create a general mechanism for setting and storing rule parameters (including
  Java rules and XML rules) like sensitivity level  
 -create a Firefox/Thunderbird extension using some of SpellBound extension code
 -German:
 	"*Ich kaufe den Hund einen Knochen" (den -> dem), aber:
 	"*Ich kaufe dem Hund." (dem -> den)
 -see if it's feasible to use native AWT library in OOo or configuration dialogs
 -see if it's feasible to check bitexts (especially looking for false friends),
  for example for checking translation files in xliff format
 -see "TODO" / "FIXME" in the source:
 	find . -iname "*.java" -exec egrep -H "TODO|FIXME" {} \;
 -...

Using LanguageTool from .NET:
 Thanks to IKVM (http://www.ikvm.net/) you can easily turn LanguageTool
 into a .NET exe or dll (without the GUI and the OpenOffice.org integration).
 Just adapt these commands to you local path names (this example shows using mono):

 export MONO_PATH=/path/to/ikvm/bin
 mono /path/to/ikvm/bin/ikvmc.exe -target:library -r:/path/to/ikvm/bin/IKVM.GNU.Classpath.dll libs/stempelator-nodict-1.0.6.jar
 mono /path/to/ikvm/bin/ikvmc.exe -r:/path/to/ikvm/bin/IKVM.GNU.Classpath.dll -r:trove.dll -r:stempelator-nodict-1.0.6.dll LanguageTool.jar

 However, the resulting LanguageTool.exe has not been tested much yet.

License:
 
 Unless otherwise noted, this software is distributed under 
 the LGPL, see file COPYING.txt
 
 See README-license.txt for the copyright of the external libraries

 German:
 The German data for part-of-speech tagging is taken from Morphy
 (http://www.wolfganglezius.de/doku.php?id=public:cl:morphy)

 Polish:
 The Polish data for part-of-speech tagging is from Morfologik project,
 licensed as LGPL (see http://morfologik.blogspot.com).

 Italian:
 The Italian data for part-of-speech tagging is taken from Morph-it!, 
 licensed under the Creative Commons Attribution ShareAlike 2.0 License 
 (see http://sslmitdev-online.sslmit.unibo.it/linguistics/morph-it.php).

 Spanish:
 The Spanish data were generated by Marcin Milkowski using FreeLing 1.5 
 and the whole Spanish Wikipedia, and subsequently cleaned using current 
 OpenOffice.org Spanish spell checker. It's being released here on LGPL 
 license.

 Dutch:
 The Dutch data are based on Alpino parser for Dutch by Gertjan van 
 Noord and is released on LGPL license. Alpino is available at 
 http://www.let.rug.nl/~vannoord/alp/Alpino/.
 
 French:
 The French data for part-of-speech tagging are based on InDICO, created 
 by Myriam Lechelt Laurent Godard and released on LGPL terms. The lemmas 
 were added by Marcin Milkowski using original DICO files (in public domain),
 and manually. It remains LGPL. The following is a copyright notice about DICO.

------------------------------------------------ 
License ABU
-=-=-=-=-=-
Version 1.1, Aout 1999

Copyright (C) 1999 Association de Bibliophiles Universels
   http://abu.cnam.fr/
   abu@cnam.fr

La base de textes de l'Association des Bibliophiles Universels (ABU)
est une oeuvre de compilation, elle peut être copiée, diffusée et
modifiée dans les conditions suivantes :

1.  Toute copie à des fins privées, à des fins d'illustration de l'enseignement
    ou de recherche scientifique est autorisée.

2.  Toute diffusion ou inclusion dans une autre oeuvre doit

     a) soit inclure la presente licence s'appliquant a l'ensemble de la
        diffusion ou de l'oeuvre dérivee.

     b) soit permettre aux bénéficiaires de cette diffusion ou de cette
        oeuvre dérivée d'en extraire facilement et gratuitement une version
        numérisée de chaque texte inclu, muni de la présente licence.  Cette
        possibilité doit être mentionnée explicitement et de façon claire,
        ainsi que le fait que la présente notice s'applique aux documents
        extraits.

     c) permettre aux bénéficiaires de cette diffusion ou de cette
        oeuvre dérivée d'en extraire facilement et gratuitement la version
        numérisée originale, munie le cas échéant des améliorations visées au
        paragraphe 6, si elles sont présentent dans la diffusion ou la nouvelle
        oeuvre. Cette possibilité doit être mentionnée explicitement et de
        façon claire, ainsi que le fait que la présente notice s'applique aux
        documents extraits.

   Dans tous les autres cas, la présente licence sera réputée s'appliquer
   à l'ensemble de la diffusion ou de l'oeuvre dérivée.


3. L'en-tête qui accompagne chaque fichier doit être intégralement 
   conservée au sein de la copie.

4. La mention du producteur original doit être conservée, ainsi
   que celle des contributeurs ultérieurs.

5. Toute modification ultérieure, par correction d'erreurs,
   additions de variantes, mise en forme dans un autre format, ou autre,
   doit être indiquée.  L'indication des diverses contributions devra être
   aussi précise que possible, et datée.

6. Ce copyright s'applique obligatoirement à toute amélioration
   par simple correction d'erreurs ou d'oublis mineurs (orthographe,
   phrase manquante, ...), c'est-à-dire ne correspondant pas à
   l'adjonction d'une autre variante connue du texte, qui devra donc
   comporter la présente notice.
 
 English:
 The English data for part-of-speech tagging are based on:
 
 1) Automatically Generated Inflection Database (AGID) version 4, 
 Copyright 2000-2003 by Kevin Atkinson <kevina@gnu.org>
 The part-of-speech database is taken from Alan Beale 2of12id 
 and the WordNet database which is under the following copyright:

    This software and database is being provided to you, the LICENSEE, by
    Princeton University under the following license.  By obtaining, using  
    and/or copying this software and database, you agree that you have  
    read, understood, and will comply with these terms and conditions.:  
  
    Permission to use, copy, modify and distribute this software and
    database and its documentation for any purpose and without fee or
    royalty is hereby granted, provided that you agree to comply with  
    the following copyright notice and statements, including the disclaimer,  
    and that the same appear on ALL copies of the software, database and  
    documentation, including modifications that you make for internal  
    use or for distribution.  
  
    WordNet 1.6 Copyright 1997 by Princeton University.  All rights reserved.  
  
    THIS SOFTWARE AND DATABASE IS PROVIDED "AS IS" AND PRINCETON  
    UNIVERSITY MAKES NO REPRESENTATIONS OR WARRANTIES, EXPRESS OR  
    IMPLIED.  BY WAY OF EXAMPLE, BUT NOT LIMITATION, PRINCETON  
    UNIVERSITY MAKES NO REPRESENTATIONS OR WARRANTIES OF MERCHANT-  
    ABILITY OR FITNESS FOR ANY PARTICULAR PURPOSE OR THAT THE USE  
    OF THE LICENSED SOFTWARE, DATABASE OR DOCUMENTATION WILL NOT  
    INFRINGE ANY THIRD PARTY PATENTS, COPYRIGHTS, TRADEMARKS OR  
    OTHER RIGHTS.
  
    The name of Princeton University or Princeton may not be used in  
    advertising or publicity pertaining to distribution of the software  
    and/or database.  Title to copyright in this software, database and  
    any associated documentation shall at all times remain with  
    Princeton University and LICENSEE agrees to preserve same.  

  Alan Beale 2of12id.txt is indirectly derived from the Moby part-of-speech
  database and the WordNet database.  The Moby part-of-speech is in the
  public domain:

    The Moby lexicon project is complete and has
    been place into the public domain. Use, sell,
    rework, excerpt and use in any way on any platform.
    
    Placing this material on internal or public servers is
    also encouraged. The compiler is not aware of any
    export restrictions so freely distribute world-wide.
    
    You can verify the public domain status by contacting
    
    Grady Ward
    3449 Martha Ct.
    Arcata, CA  95521-4884
    
    grady@netcom.com
    grady@northcoast.com

	For more information on wordlists used, see agid-readme.txt.
	
  2) Part Of Speech Database, compiled by Kevin Atkinson 
  <kevina@users.sourceforge.net>
   The part-of-speech.txt file contains is a combination of 
   "Moby (tm) Part-of-Speech II" and the WordNet database (see above and 
   pos-readme.txt).
   
  3) 2of12inf wordlist, released to public domain, 
  see 12dicts-readme.html.
  
  4) Public domain Moby wordlists were used also for generating 
  POS tag information for common proper names.
  
  For more information, see the scripts in the source directory 
  en/resource/.  