#!/bin/bash
#Extract strings from webtranslateit.com, and create one plain text file per project and language

#website
wget https://webtranslateit.com/api/projects/proj_pub_AwnE5l0q1_WwtLDSHUd3mg/zip_file -O website-languagetool-org.zip
#browser add-on
wget https://webtranslateit.com/api/projects/proj_pub_4YkcE50r8Q9ujP8nLkFeoA/zip_file -O languagetool-browser-add-on.zip
#LT Core
wget https://webtranslateit.com/api/projects/proj_pub_tiZbpv4jQZpfL6x3191OBg/zip_file -O lt-core.zip
#Apple Apps
wget https://webtranslateit.com/api/projects/proj_pub_DWWXLulL2TDImiSwwTxR2Q/zip_file -O apple-apps.zip

for zip in *.zip
do
	echo $zip
	dirname=`echo $zip | sed 's/\.zip$//'`
	echo $dirname
	rm -rf "$dirname"
	if mkdir "$dirname"
	then
    	if cd "$dirname"
    	then
    		unzip ../"$zip"
      		cd ..
      		rm -f $zip 
    	else
      	echo "Could not unpack $zip - cd failed"
    	fi
  	else
    	echo "Could not unpack $zip - mkdir failed"
  	fi
done

rm -rf text-files
mkdir text-files

project=languagetool-browser-add-on
path=$project/src/_locales

for lang in ca de en es fr it nl pl pt_BR pt_PT ru uk
do
	grep '"message": ' $path/$lang/messages.json | sed 's/.*"message": "\(.*\)"/\1/' | sed 's/<[^>]*>//g'> text-files/$lang-$project.txt
done

project=website-languagetool-org
path=$project/resources/lang

for lang in ca de en es fr it nl pl pt pt-BR ru uk
#TODO: normalize the language codes for Portuguese
do
	python3 extract-txt-from-php.py $path/$lang/js.php > text-files/$lang-$project.txt
	python3 extract-txt-from-php.py $path/$lang/messages.php >> text-files/$lang-$project.txt
	
done

project=lt-core
#English
lang="en"
file=$project/languagetool-core/src/main/resources/org/languagetool/MessagesBundle_$lang.properties
python3 extract-txt-from-ltcore.py $file >text-files/$lang-$project.txt
#other languages
for lang in ar ast be br ca da de el eo es fa fr gl it ja km lt nl pl pt_BR pt_PT ro ru sk sl sv ta tl uk zh
do 
	langdir=$lang
	if [[ $lang == *"pt"* ]]
	then
		langdir="pt"
	fi
	file=$project/languagetool-language-modules/$langdir/src/main/resources/org/languagetool/MessagesBundle_$lang.properties
	python3 extract-txt-from-ltcore.py $file >text-files/$lang-$project.txt
done

project="apple-apps"
for lang in de en es fr
do
	file=$project/src/LanguageTool/$lang.lproj/Localizable.strings
	grep " = " $file | sed 's/".*" = "\(.*\)";/\1/' > text-files/$lang-$project.txt
done


