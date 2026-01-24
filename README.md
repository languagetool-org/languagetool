## LanguageTool

**LanguageTool** is open source proofreading software for English, Spanish, French, German,
Portuguese, Polish, Dutch, and [more than 20 other languages](https://languagetool.org/languages/).
It finds many errors that a simple spell checker cannot detect.

* [LanguageTool Forum](https://forum.languagetool.org)

* [How to run your own LanguageTool server](https://dev.languagetool.org/http-server)

* [HTTP API documentation](https://languagetool.org/http-api/swagger-ui/#!/default/post_check)

* [How to use our public server via HTTP](https://dev.languagetool.org/public-http-api)

* [How to use LanguageTool from Java](https://dev.languagetool.org/java-api) ([Javadoc](https://languagetool.org/development/api/index.html?org/languagetool/JLanguageTool.html))

For more information, please see our homepage, at [`languagetool.org`](https://languagetool.org),
[this `README`](https://github.com/languagetool-org/languagetool/blob/master/languagetool-standalone/README.md),
and [`CHANGES`](https://github.com/languagetool-org/languagetool/blob/master/languagetool-standalone/CHANGES.md).

The LanguageTool core (this repo) is freely available under the LGPL 2.1 or later.

## Docker

Try one of the following projects for a community-contributed Docker file:

`github.com`                                                                      | `hub.docker.com/r`
----------------------------------------------------------------------------------|--------------------------------------------------------------------------------------
[`meyayl/docker-languagetool`](https://github.com/meyayl/docker-languagetool)     | [`meyay/languagetool`](https://hub.docker.com/r/meyay/languagetool)
[`Erikvl87/docker-languagetool`](https://github.com/Erikvl87/docker-languagetool) | [`erikvl87/languagetool`](https://hub.docker.com/r/erikvl87/languagetool)
[`silvio/docker-languagetool`](https://github.com/silvio/docker-languagetool)     | [`silviof/docker-languagetool`](https://hub.docker.com/r/silviof/docker-languagetool)

## Contributions

[The development overview](https://dev.languagetool.org/development-overview) describes
how you can contribute error detection rules.

For more technical details, see [our dev pages](https://dev.languagetool.org).

## Scripted installation and building

To install or build using a script, simply type:

```sh
#!/usr/bin/env sh
curl -L https://raw.githubusercontent.com/languagetool-org/languagetool/master/install.sh | sudo bash $options
```

If you wish to have more options, download the `install.sh` script. Usage options follow:

<blockquote>

```HTML
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

</blockquote>

## Alternate way to build from source

Before start: you will need to clone from GitHub and install Java 17 and Apache Maven.

Warning: a complete clone requires downloading more than 500 MiB and needs more than 1 500 MiB on disk.
This can be reduced, if you only need the last few revisions of the master branch
by creating a shallow clone:

```sh
#!/usr/bin/env sh
git clone --depth 5 https://github.com/languagetool-org/languagetool.git
```

A shallow clone downloads less than 60 MiB, and needs less than 200 MiB on disk.

In the root project folder, run:

```sh
#!/usr/bin/env sh
mvn clean test
```

(Sometimes, you can skip Maven step for repeated builds.)

```sh
#!/usr/bin/env sh
./build.sh languagetool-standalone package -DskipTests
```

Test the result in `languagetool-standalone/target/`.

```sh
#!/usr/bin/env sh
./build.sh languagetool-wikipedia package -DskipTests
```

Test the result in `languagetool-wikipedia/target`.

Now, you can use the bleeding edge development copy of LanguageTool `*.jar` files, be aware that it might contain regressions.

### How to run under Mac M1 or M2

1. Install Brew for Rosetta:

   ```sh
   #!/usr/bin/env sh
   arch -x86_64 /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install.sh)"
   ```

1. Install OpenJDK for Rosetta:

   ```sh
   #!/usr/bin/env sh
   arch -x86_64 brew install openjdk
   ```

1. Install Maven for Rosetta:

   ```sh
   #!/usr/bin/env sh
   arch -x86_64 brew install maven
   ```

1. Now, run the build scripts.

### License

Unless otherwise noted, this software – the LanguageTool core – is distributed under the LGPL; see
[`COPYING.txt`](https://github.com/languagetool-org/languagetool/blob/master/COPYING.txt).
