#!/bin/sh
echo "Make wordlist. You must convert WordData.txt to UTF-8 encoding first!"
#Make wordlist frequency data from AOT's WordData.txt for LT spellcheck dictionary
gawk -f freq-wordlist.awk WordData.txt   > wordlist_ru.xml
echo "Done!"
