#!/bin/sh

echo "Make wordlist. You must convert WordData.txt to UTF-8 encoding first!"
#Make wordlist frequency data from AOT's WordData.txt for LT spellcheck dictionary
echo "Make dict" > f.txt
gawk -f freq-wordlist.awk WordData.txt   > wordlist_ru.txt
echo "Done!"
