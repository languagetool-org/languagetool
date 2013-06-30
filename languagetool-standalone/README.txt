LanguageTool, a proof-reading tool for English, German, Polish,
Chinese, French, Dutch, Russian, Italian, Danish and more languages
(see http://www.languagetool.org/languages/ for a list)

Copyright (C) 2005-2013 Daniel Naber (naber at danielnaber de)
Version 2.2, 2013-06-30
Homepage: http://www.languagetool.org

Requirements:
 -Java 6.0 or later (Oracle Java or IcedTea; GIJ is not supported)
 -For OpenOffice.org/LibreOffice integration: OpenOffice 3.0.1 or later
  or LibreOffice. We recommend LibreOffice 3.5.4 (or later) or
  Apache OpenOffice 3.4.1 (or later) because they contain an
  important bug fix.

Usage:
 -To integrate LanguageTool into OpenOffice.org or LibreOffice, you
  can use two methods:
 
 1. Double-click LanguageTool-2.1.oxt. The extension should
  start installing. Follow the on-screen instructions.
 
 2. If the above method doesn't work, call Tools > Extension 
  Manager > Add... in OpenOffice.org/LibreOffice and browse for the
  LanguageTool-2.1.oxt file. 
  
  Close and restart OpenOffice.org/LibreOffice Writer. Remember to close
  the QuickStarter as well if you use it. Type text with
  an error, e.g. "This is an test." - make sure the text language 
  is set to English for this example.
  You should see a blue underline under the word "an". Opening
  the context menu with the right mouse button offers you a
  description of the error and, if available, a correction.

  If you are using LibreOffice 3.5+ and you want to check English texts:
  Use Options -> Language Settings -> Writing Aids -> Edit... to disable
  LightProof and enable LanguageTool for English.
  
  If you are using LibreOffice 3.5+ and you want to check Russian texts:
  Use Options -> Language Settings -> Writing Aids -> Edit... to disable
  LightProof and enable LanguageTool for Russian.

  Note that there will also be a new menu item "LanguageTool"
  under the "Tools" menu.
  If the native spelling and grammar dialog doesn't check grammar,
  make sure that the check box "Check Grammar" is checked in it
  (if the window closes because of no mistakes in the document,
  simply make any spelling mistake to make it open for a longer
  time, and check the box). Check also if LanguageTool is visible
  under "Grammar" in Tools > Options > Language Settings > Spelling
  for your language. Note: you can disable the grammar check without
  uninstalling LanguageTool simply by clearing the check box next to
  LanguageTool in the same dialog.
  
  Please see http://www.languagetool.org/issues/ if you
  experience problems
  
 -To use the simple demo GUI, double click on the languagetool-standalone.jar file
  or call 'java -jar languagetool-standalone.jar'

 -To check plain text files from the command line:
  java -jar languagetool-commandline.jar <filename>

------------------------------------------------ 

Using LanguageTool from .NET:

 Thanks to IKVM (http://www.ikvm.net/) you can easily turn LanguageTool
 into a .NET exe or dll (without the GUI and the OpenOffice.org integration).
 You will need a fairly recent version of IKVM that uses OpenJDK to make it work.
 Just adapt these commands to you local path names (this example shows using mono):

 export MONO_PATH=/path/to/ikvm/bin
 mono /path/to/ikvm/bin/ikvmc.exe -target:library -r:/path/to/ikvm/bin/IKVM.OpenJDK.Core.dll libs/###morfologik.lib###
 mono /path/to/ikvm/bin/ikvmc.exe -target:library -r:/path/to/ikvm/bin/IKVM.OpenJDK.Core.dll libs/jWordSplitter.jar
 mono /path/to/ikvm/bin/ikvmc.exe -r:/path/to/ikvm/bin/IKVM.OpenJDK.Core.dll -r:###morfologik.lib### -r:jWordSplitter.dll LanguageTool.jar

 However, the resulting LanguageTool.exe has not been tested much yet. You can expect
 problems with resource loading (path names are not recognized properly).

------------------------------------------------ 

License:
 
 Unless otherwise noted, this software is distributed under 
 the LGPL, see file COPYING.txt
 
 See README-license.txt for the copyright of the external libraries

 Language detection (*.ngp):
 The process for automatic language detection is described at
 http://languagetool.wikidot.com/adding-a-new-language-to-automatic-language-detection
 The Wikipedia-based training data can be downloaded from
 http://www.languagetool.org/download/language-training-data/
 
 Asturian:
 The Asturian data for part-of-speech tagging are from the Freeling dictionary,
 licensed under GNU General Public License.
 Contributor(s):
   Xesús González Rato <esbardu@softastur.org>

 Breton:
 The Breton data for part-of-speech tagging is based on the Apertium Breton
 dictionary under GNU General Public License with permission of its authors:
    Copyright (C) 2008--2010 Francis Tyers <ftyers@prompsit.com>
    Copyright (C) 2009--2010 Fulup Jakez <fulup.jakez@ofis-bzh.org>
    Copyright (C) 2009       Gwenvael Jekel <jequelg@yahoo.fr>
    Development supported by:
    * Prompsit Language Engineering, S. L.
    * Ofis ar Brezhoneg
    * Grup Transducens, Universitat d'Alacant

 The Breton FSA spelling dictionary is based on the Breton Hunspell dictionary
 "Difazier Hunspell an Drouizig" (0.10) licensed under the Lesser GNU Public
 License (LGPL), available at:
 http://extensions.libreoffice.org/extension-center/an-drouizig-breton-spellchecker/pscreleasefolder.2012-08-22.5582266403/0.10/dict-br_0.10.oxt

 Catalan:
 The Catalan data for part-of-speech tagging were created by Jaume Ortolà 
 based on the Freeling 3.0 and Softcatalà 2.5.0 dictionaries, both released 
 under the GNU General Public License. 
 
 Chinese:
 The Chinese data and code for part-of-speech tagging is based on ictclas4j project
 (http://code.google.com/p/ictclas4j/) under Apache License 2.0.

 Danish:
 The Danish tagger is based upon data from Stavekontrolden - Danish dictionary
 for Hunspell. © 2012 Foreningen for frit tilgængelige sprogværktøjer
 These files are published under the following open source licenses:
 GNU GPL version 2.0, GNU LGPL version 2.1, Mozilla MPL version 1.1
 http://www.stavekontrolden.dk

 Dutch:
 The Dutch data are partly based on Alpino parser for Dutch by Gertjan van 
 Noord and is released on LGPL license. Alpino is available at 
 http://www.let.rug.nl/~vannoord/alp/Alpino/. The POS tag system and values
 come mostly from OpenTaal, www.opentaal.org.

 French:
 The French data for part-of-speech tagging are from the Dicollecte project. 
 They are made available here under LGPL. See detailed information in 
 resource/fr/README_lexique.txt

 Galician:
 The Galician data for part-of-speech tagging were created by Susana Sotelo
 Docio based on Freeling and Apertium dictionaries. Both are licensed under GPL.
 
 German:
 The German data for part-of-speech tagging is taken from Morphy
 (http://www.wolfganglezius.de/doku.php?id=public:cl:morphy)
 under Creative Commons Attribution-Share Alike 3.0

 Italian:
 The Italian data for part-of-speech tagging is taken from Morph-it!, 
 licensed under the Creative Commons Attribution ShareAlike 2.0 License 
 and the GNU Lesser General Public License (LGPL) 
 (see http://sslmitdev-online.sslmit.unibo.it/linguistics/morph-it.php).

 Polish:
 The Polish data for part-of-speech tagging is from Morfologik project,
 licensed on BSD (see http://morfologik.blogspot.com).

 Romanian:
 The Romanian data for part-of-speech tagging is developed by Ionuț Păduraru
 (http://www.archeus.ro). It's being released here on LGPL license.

 Russian:
 Russian dictionary originally developed by www.aot.ru and licensed under LGPL.
 http://www.aot.ru/download.php file rus-src-morph.tar.gz
 or http://seman.svn.sourceforge.net/viewvc/seman/trunk/Dicts/SrcMorph/RusSrc/morphs.mrd?revision=99
 It was partially converted to fsa format in 2008-2011 by Yakov Reztsov.  

 Slovak:
 The Slovak data were created by Zdenko Podobný based on Slovak National
 Corpus data (http://korpus.juls.savba.sk/). They are released here on
 LGPL license.

 Spanish:
 The dictionary was mainly obtained from the Freeling project.
	http://devel.cpl.upc.edu/freeling/svn/latest/freeling/data/es/dicc.src
	http://garraf.epsevg.upc.es/freeling/
 It is released under the GNU General Public License.

 Swedish:
 The Swedish data are based on DSSO. The Initial Developer of the Original Code is Göran Andersson.
 Contributor(s):
   Tom Westerberg <tweg@welho.com>
   Niklas Johansson <sleeping.pillow@gmail.com>
 The Swedish Dictionary may be used under the terms of the GNU Lesser General Public License Version 2.1 or later 
 (the "LGPL"). 
 http://dsso.se
 
 Tagalog:
 The Tagalog Tagset was designed by Nathaniel Oco.
 The words for the Tagger Dictionary were taken from the Philippine Literature Domain of Dalos D. Miguel's Comparative Analysis of Tagalog POS Taggers.
 The Tagger Dictionary and the Tagset are made available under LGPL.
 The Trigram Training Data is available at: The Trigram Training Data is available at: https://sourceforge.net/projects/tattoi.u/files/Trigram%20Text/

 Ukrainian:
 The Ukrainian data for part-of-speech tagging were created by Andriy Rysin
 It's based on spell-uk (http://sourceforge.net/projects/ispell-uk/) which is licensed under GPL/LGPL.

------------------------------------------------ 
 
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
