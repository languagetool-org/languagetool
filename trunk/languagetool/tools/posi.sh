cd ..
python TextChecker.py -l hu -x tools/$1.txt >tools/$1_1.txt
awk -f tools/posi.awk <tools/$1_1.txt >tools/$1_2.txt
sort -n tools/$1_2.txt >tools/$1_3.txt 
