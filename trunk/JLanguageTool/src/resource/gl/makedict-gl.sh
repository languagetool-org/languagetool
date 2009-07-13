#!/bin/bash

##############################################
# Script que xera os dicionarios para galego
# e español
#
# Susana Sotelo Docío
##############################################
LANG=POSIX
LOCALE=POSIX
MYLANG=`echo $0 | sed -e "s/.*makedict-\(..\).sh/\1/"`
PATHTO="$HOME/proxectos/linguarum/openoffice/languagetool/data/$MYLANG"
BINPATH="/usr/local/bin"
DICTIONARY=$PATHTO/$MYLANG.dicc
TAGLIST=taglist.txt

_help()
{
  echo "makedict-$MYLANG.sh type"
  echo
  echo '  where type is one of'
  echo '       dict: generates the dictionary'
  echo '       taglist: generates a list of used tags'
  echo '       doc: generates doc files'
  echo '       all: performs all actions'

  exit
}

taglist()
{
  # Xeramos o listado de etiquetas
  echo -n "[$MYLANG] Generating taglist...     "
  gawk '{ print $3 }' < $DICTIONARY | sort -u > $TAGLIST
  echo "[ok]"
}

dictionary()
{
  # Compilamos o dicionario
  echo -n "[$MYLANG] Compiling dictionary...    "
  perl $BINPATH/morph_data.pl < $DICTIONARY | sort -u | fsa_build -O -o $LONGNAME.dict >& /dev/null
  echo "[ok]"
  echo -n "[$MYLANG] Testing FSA automaton...   "
  cat test.txt | fsa_morph -d $LONGNAME.dict > /tmp/$$outfile
  if [ "`diff /tmp/$$outfile test-tagged.txt`" != "" ]; then
    echo "[error]"
  else
    echo "[ok]"
  fi
}

docgen()
{
  echo -n "[$MYLANG] Generating doc files...    "
  echo 'The dictionary was mainly obtained from the Freeling project.' > README
  echo >> README
  echo "http://devel.cpl.upc.edu/freeling/svn/latest/freeling/data/$MYLANG/dicc.src" >> README
  echo 'http://garraf.epsevg.upc.es/freeling/' >> README
  echo >> README
  echo 'It is released under the GNU General Public License.' >> README
  echo "[ok]"
  echo -n "[$MYLANG] Generating doc cvs files...    "
  echo 'The dictionary was mainly obtained from the Freeling project.' > README.cvs
  echo >> README.cvs
  echo "http://devel.cpl.upc.edu/freeling/svn/latest/freeling/data/$MYLANG/dicc.src" >> README.cvs
  echo 'http://garraf.epsevg.upc.es/freeling/' >> README.cvs
  echo >> README.cvs
  echo 'It is released under the GNU General Public License.' >> README.cvs
  echo >> README.cvs
  echo >> README.cvs
  echo 'The freeling format is slightly different from LT, and it can be converted using' >> README.cvs
  echo 'freeling2lt.pl.' >> README.cvs
  echo >> README.cvs
  echo "The script makedict-$MYLANG.sh is provided to make easier the generation of the FSA" >> README.cvs
  echo "automaton from the dictionary." >> README.cvs
  echo "[ok]"
}

if [ $MYLANG == "gl" ]; then
  LONGNAME="galician"
elif [ $MYLANG == "es" ]; then
  LONGNAME="spanish"
fi

if [ -z "$1" ]; then
  _help
elif [ "$1" == "dict" ]; then
  dictionary
elif [ "$1" == "taglist" ]; then
  taglist
elif [ "$1" == "doc" ]; then
  docgen
elif [ "$1" == "all" ]; then
  dictionary
  taglist
  docgen
else
  echo "** $1 is not a valid option."
  echo
  _help
fi
exit
