
The *.dict files in the sub directories are produced by the
fsa tools (http://www.eti.pg.gda.pl/katedry/kiw/pracownicy/Jan.Daciuk/personal/fsa.html).

To export *.dict data to plain text:
export LANG=C
fsa_prefix -a -d ~/workspace/JLanguageTool/src/resource/de/german.dict >export

Import exported data back again to *.dict:
export LANG=C
gawk -f morph_data.awk <export | sort -u | fsa_build -O -o output.dict

See http://languagetool.wikidot.com/developing-a-tagger-dictionary
for more information
