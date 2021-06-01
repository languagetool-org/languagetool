## LanguageTool

**LanguageTool** is an Open Source proofreading software for English, French, German,
Polish, Russian, and [more than 20 other languages](https://languagetool.org/languages/).
It finds many errors that a simple spell checker cannot detect.

* **[Jobs at LanguageTool](https://languagetool.org/careers)**
* [How to run your own LanguageTool server](https://dev.languagetool.org/http-server)
* [HTTP API documentation](https://languagetool.org/http-api/swagger-ui/#!/default/post_check)
* [How to use our public server via HTTP](https://dev.languagetool.org/public-http-api)
* [How to use LanguageTool from Java](https://dev.languagetool.org/java-api) ([Javadoc](https://languagetool.org/development/api/index.html?org/languagetool/JLanguageTool.html))

For more information, please see our homepage at https://languagetool.org,
[this README](https://github.com/languagetool-org/languagetool/blob/master/languagetool-standalone/README.md),
and [CHANGES](https://github.com/languagetool-org/languagetool/blob/master/languagetool-standalone/CHANGES.md).

LanguageTool is freely available under the LGPL 2.1 or later.

## Docker

Try one of the following projects for a community-contributed Docker file:

- https://github.com/Erikvl87/docker-languagetool ([Docker Hub](https://hub.docker.com/r/erikvl87/languagetool))
- https://github.com/silvio/docker-languagetool ([Docker Hub](https://hub.docker.com/r/silviof/docker-languagetool))

## Contributions

[The development overview](https://dev.languagetool.org/development-overview) describes
how you can contribute error detection rules.

See ['easy fix' issues](https://github.com/languagetool-org/languagetool/issues?q=is%3Aopen+is%3Aissue+label%3A%22easy+fix%22)
for issues to get started.

For more technical details, see [our dev pages](https://dev.languagetool.org).

## Scripted installation and building
To install or build using a script, simply type:
```
curl -L https://raw.githubusercontent.com/languagetool-org/languagetool/master/install.sh | sudo bash <options>
```

If you wish to have more options, download the install.sh script. Usage options follow:

```
sudo bash install.sh <options>

Usage: install.sh <option> <package>
Options:
   -h --help                   Show help
   -b --build                  Builds packages from the bleeding edge development copy of LanguageTool
   -c --command <command>      Specifies post-installation command to run (default gui when screen is detected)
   -q --quiet                  Shut up LanguageTool installer! Only tell me important stuff!
   -t --text <file>            Specifies what text to be spellchecked by LanguageTool command line (default spellcheck.txt)
   -d --depth <value>          Specifies the depth to clone when building LanguageTool yourself (default 1).
   -p --package <package>      Specifies package to install when building (default all)
   -o --override <OS>          Override automatic OS detection with <OS>
   -a --accept                 Accept the oracle license at http://java.com/license. Only run this if you have seen the license and agree to its terms!
   -r --remove <all/partial>   Removes LanguageTool install. <all> uninstalls the dependencies that were auto-installed. (default partial)

Packages(only if -b is specified):
   standalone                  Installs standalone package
   wikipedia                   Installs Wikipedia package
   office-extension            Installs the LibreOffice/OpenOffice extension package

Commands:
   GUI                         Runs GUI version of LanguageTool
   commandline                 Runs command line version of LanguageTool
   server                      Runs server version of LanguageTool
```

## Alternate way to build from source

Before start: you will need to clone from GitHub and install Java 8 and Apache Maven.

Warning: a complete clone requires downloading more than 360 MB and needs more than 500 MB on disk.
This can be reduced if you only need the last few revisions of the master branch
by creating a shallow clone:

    git clone --depth 5 https://github.com/languagetool-org/languagetool.git

A shallow clone downloads less than 60 MB and needs less than 200 MB on disk.

In the root project folder, run:

    mvn clean test

(sometimes you can skip Maven step for repeated builds)

    ./build.sh languagetool-standalone package -DskipTests

Test the result in `languagetool-standalone/target/`.

    ./build.sh languagetool-wikipedia package -DskipTests

Test the result in `languagetool-wikipedia/target`.

    ./build.sh languagetool-office-extension package -DskipTests

Test the result in `languagetool-office-extension/target`, rename the `*.zip` to `*.oxt` to install it in LibreOffice/OpenOffice.

Now you can use the bleeding edge development copy of LanguageTool `*.jar` files, be aware that it might contain regressions.

### License

Unless otherwise noted, this software is distributed under the LGPL, see file [COPYING.txt](https://github.com/languagetool-org/languagetool/blob/master/COPYING.txt).
