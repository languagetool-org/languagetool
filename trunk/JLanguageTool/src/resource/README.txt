
The *.dict files in the sub directories are produced by the
fsa tools (http://www.eti.pg.gda.pl/katedry/kiw/pracownicy/Jan.Daciuk/personal/fsa.html).

To export *.dict data to plain text:
export LANG=C
fsa_prefix -a -d ~/workspace/JLanguageTool/src/resource/de/german.dict >export

Import exported data back again to *.dict:
export LANG=C
sort -u export | fsa_build -O -o output.dict

If you want to edit the data in the tabbed format, use de_morph_data.awk script 
from fsa:

gawk -f de_morph_data.awk < export > export.txt

To compile the dictionary into binary form, you will have to use 
morph_data.awk again, i.e.:

export LANG=C
gawk -f morph_data.awk export.txt | sort -u | fsa_build -O -o output.dic 

Note: the .dict files are accompanied with .info files that describe the 
encoding used and if there was infix compression used as well. In the 
case of infix compression, you need to use de_morph_infix.awk script 
instead. Recompiling will require using morph_infix.awk

See http://languagetool.wikidot.com/developing-a-tagger-dictionary
for more information
