#/bin/sh
java -cp languagetool.jar org.languagetool.tools.POSDictionaryBuilder -i dic_ru_with_ie.dump -info org/languagetool/resource/ru/russian.info -o russian.dict
java -cp languagetool.jar org.languagetool.tools.SynthDictionaryBuilder -i dic_ru.dump -info org/languagetool/resource/ru/russian_synth.info  -o russian_synth.dict

