This folder is for the developers of the dictionary and is not part of LanguageTool build.
The dictionary is published to maven repository (local for SNAPSHOT and central for releases) and consumed in LT as a dependency 
(to update the version see ../../../../../../../../../../pom.xml)

This gradle script generates POS tag dictionary with morfologik from pos_dict.txt file using languagetool-tools package
It'll also generate spelling and synthesizer dictionaries.

pos_dict.txt file format:
`<word> <lemma> <tags>`
