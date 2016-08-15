LanguageTool is an Open Source proofreading software for English, French, German,
Polish, Russian, and [more than 20 other languages](https://languagetool.org/languages/).
It finds many errors that a simple spell checker cannot detect.

LanguageTool is freely available under the LGPL 2.1 or later.

For more information, please see our homepage at https://languagetool.org,
[this README](https://github.com/languagetool-org/languagetool/blob/master/languagetool-standalone/README.md),
and [CHANGES](https://github.com/languagetool-org/languagetool/blob/master/languagetool-standalone/CHANGES.md).

#### Contributions

[The development overview](http://wiki.languagetool.org/development-overview) describes
how you can contribute error detection rules.

See ['easy fix' issues](https://github.com/languagetool-org/languagetool/issues?q=is%3Aopen+is%3Aissue+label%3A%22easy+fix%22)
for issues to get started.

For more technical details, see [our wiki](http://wiki.languagetool.org).

#### How to build from source

Before start: you will need to clone from GitHub (warning, 300+ MB download) and install Java 8 and Apache Maven

In root project folder run:

    mvn clean test

(sometimes you can skip Maven step for repeated builds)

    ./build.sh languagetool-standalone package -DskipTests

test the result in languagetool-standalone/target/

    ./build.sh languagetool-wikipedia package -DskipTests

test the result in languagetool-wikipedia/target

    ./build.sh languagetool-office-extension package -DskipTests

test the result in languagetool-office-extension/target, rename the *zip to *oxt and to install it in LibreOffice/OpenOffice

Now you can use the bleeding edge development copy of LanguageTool *.jar files, be aware that it can contain regressions or troublesome issues.
