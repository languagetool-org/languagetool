#!/bin/bash

JAVA_OPTS="-Xmx2048m"
LANG=POSIX
LOCALE=POSIX
PATHTO="$HOME/proxectos/linguarum/openoffice/languagetool/resources/dict"
FSA="/usr/local/morfologik/morfologik-tools-1.5.2-standalone.jar"
SFSA="/usr/local/fsa"
DICTIONARY=$PATHTO/gl.dicc
TAGLIST=galician_tags.txt

_help()
{
  echo "makedict-gl.sh type"
  echo
  echo '  where type is one of'
  echo '       dict: generates the dictionary'
  echo '       synthesis: generates the synthetizer'
  echo '       taglist: generates a list of used tags'
  echo '       doc: generates doc files'
  echo '       all: performs all actions'

  exit
}

taglist()
{
  echo -n ">> Generating taglist...     "
  cut -f3 < $DICTIONARY | sort -u > $TAGLIST
  echo "[ok]"
}

dictionary()
{
  echo -n ">> Compiling dictionary...    "

  #perl $BINPATH/morph_data.pl < $DICTIONARY | sort -u | fsa_build -O -o $LONGNAME.dict >& /dev/null

  java ${JAVA_OPTS} -jar ${FSA} tab2morph -i ${DICTIONARY} -o $$temp-output.txt
  java ${JAVA_OPTS} -jar ${FSA} fsa_build -i $$temp-output.txt -o galician.dict
  rm -f $$temp-output.txt
 
  echo "[ok]"

  # deprecated

  #echo -n ">> Testing FSA automaton...   "
  #cat test.txt | fsa_morph -d galician.dict > /tmp/$$outfile
  #if [ "`diff /tmp/$$outfile test-tagged.txt`" != "" ]; then
  #  echo "[error]"
  #else
  #  echo "[ok]"
  #fi
}

synthesis()
{
  echo -n ">> Compiling synthetizer...   "

  #awk -f synthesis$$.awk ${DICTIONARY} > $$temp-output.txt
  #perl -ne '{ chomp;@a=split/\t/; print "$a[1]\t$a[2]\t$a[0]\n"; }' < ${DICTIONARY} > $$temp-output.txt
  #grep -v ":.*:" ${DICTIONARY} | perl -ne '{ chomp;@a=split/\t/; print "$a[1]\t$a[2]\t$a[0]\n"; }' > $$temp-output.txt
  #java ${JAVA_OPTS} -jar ${FSA} tab2morph -i $$temp-output.txt -o $$encoded.txt
  #java ${JAVA_OPTS} -jar ${FSA} fsa_build -i $$encoded.txt -o galician_synth.dict
  #rm -f $$temp-output.txt $$encoded.txt

  awk -f /usr/local/bin/synth.awk ${DICTIONARY} |awk -f /usr/local/bin/morph_data.awk | sort -u |${SFSA}/fsa_build -O -o galician_synth.dict
}

docgen()
{
  echo -n ">> Generating doc files...    "
  echo '
The Galician dictionary was obtained from two main sources:

    - The Freeling project.

      http://devel.cpl.upc.edu/freeling/svn/latest/freeling/data/gl/dicc.src
      http://garraf.epsevg.upc.es/freeling/

    - Apertium machine translation engine

Both are released under the GNU General Public License.

http://www.gnu.org/copyleft/gpl.html
' > README
  echo "[ok]"
}

if [ -z "$1" ]; then
  _help
elif [ "$1" == "dict" ]; then
  dictionary
elif [ "$1" == "taglist" ]; then
  taglist
elif [ "$1" == "synthesis" ]; then
  synthesis
elif [ "$1" == "doc" ]; then
  docgen
elif [ "$1" == "all" ]; then
  dictionary
  taglist
  synthesis
  docgen
else
  echo "** $1 is not a valid option."
  echo
  _help
fi
exit
