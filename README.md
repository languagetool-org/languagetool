LanguageTool is an Open Source proofreading software for English, French, German, Polish, Russian,
and more than 20 other languages. It finds many errors that a simple spell checker cannot detect.

LanguageTool is freely available under the LGPL 2.1 or later.

For more information, please see our homepage at http://languagetool.org and [this README](https://raw.githubusercontent.com/languagetool-org/languagetool/master/languagetool-standalone/README.txt).

[![Build Status](https://travis-ci.org/languagetool-org/languagetool.svg?branch=master)](https://travis-ci.org/languagetool-org/languagetool)

## Running the HTTP Server
Clone this repository on your system. In your command prompt/Terminal, go to the folder where the repo has been cloned and follow the following instructions 

`ankita@Vhagar~/languagetool$ cd languagetool-standalone/target/LanguageTool-2.9-SNAPSHOT/LanguageTool-2.9-SNAPSHOT`
`ankita@Vhagar~/languagetool/languagetool-standalone/target/LanguageTool-2.9-SNAPSHOT/LanguageTool-2.9-SNAPSHOT$ java -cp languagetool-server.jar org.languagetool.server.HTTPServer --port 8081` 

Host the files available in the folder `languagetool-query` on a server. Edit the address of the file `proxy.php` in `index.html.en`. Open the `index.html.en` in a browser. 

You should be all set.